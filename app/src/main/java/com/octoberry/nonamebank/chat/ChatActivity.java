package com.octoberry.nonamebank.chat;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.octoberry.nonamebank.GeneralUtils;
import com.octoberry.nonamebank.R;
import com.octoberry.nonamebank.db.SharedPreferenceManager;
import com.octoberry.nonamebank.ui.CardListAdapter;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.model.QBFile;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class ChatActivity extends Activity {
	private ListView mMessagesContainer;
	private EditText mMessageEditText;
	private ImageView mStatusImageView;
	private ImageView mCloseImageView;
	private ImageView mSendImageView;
	private ProgressBar mProgressBar;
    private ProgressBar mUploadImageProgressBar;
    private ImageView mChatAttachImageView;
    private ImageView mCancelAttachImageView;
    private RelativeLayout mPreviewRelativeLayout;
    private ImageView mPreviewImageView;
    private ImageView mClosePreviewImageView;

    private ObjectAnimator mAppearAnimator;
    private ObjectAnimator mDisappearAnimator;

	private ChatAdapter adapter;
	
	private static final String TAG = ChatActivity.class.getSimpleName();	
	
	private static ChatActivity instance = null;
	
	boolean bound = false;
	ServiceConnection sConn;
	ChatService mChatService;
	BroadcastReceiver mReceiver;

    int mImageWith;
    int mImageHeight;

    int mImagePreviewWidth;
    int mImagePreviewHeight;
	
	public static ChatActivity getInstance() {
		return instance;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		instance = this;
		
		setContentView(R.layout.activity_chat);

		mMessagesContainer = (ListView) findViewById(R.id.messagesContainer);
		mMessageEditText = (EditText) findViewById(R.id.messageEdit);
		mStatusImageView = (ImageView) findViewById(R.id.statusImageView);
		mCloseImageView = (ImageView) findViewById(R.id.closeImageView);
		mSendImageView = (ImageView) findViewById(R.id.chatSendImageView);
		mSendImageView.setEnabled(false);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mUploadImageProgressBar = (ProgressBar) findViewById(R.id.uploadImageProgressBar);
        mChatAttachImageView = (ImageView) findViewById(R.id.chatAttachButton);
        mCancelAttachImageView = (ImageView) findViewById(R.id.cancelAttachButton);
        mPreviewRelativeLayout = (RelativeLayout) findViewById(R.id.imagePreviewLayout);
        mPreviewImageView = (ImageView) findViewById(R.id.previewImageView);
        mClosePreviewImageView = (ImageView) findViewById(R.id.closePreviewImageView);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mImagePreviewHeight = size.y;
        mImagePreviewWidth = size.x;
        mPreviewRelativeLayout.setTranslationY(mImagePreviewHeight);
        mAppearAnimator = ObjectAnimator.ofFloat(mPreviewRelativeLayout, "translationY", mImagePreviewHeight, 0);
        mAppearAnimator.setDuration(1000);
        mDisappearAnimator = ObjectAnimator.ofFloat(mPreviewRelativeLayout, "translationY", 0, mImagePreviewHeight);
        mDisappearAnimator.setDuration(1000);
		
		mProgressBar.setVisibility(View.VISIBLE);
		mSendImageView.setEnabled(false);
		mStatusImageView.setImageResource(R.drawable.mes_offline);
		
		mSendImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String messageText = mMessageEditText.getText().toString();
                SharedPreferenceManager manager = SharedPreferenceManager.getInstance(ChatActivity.this);
				if ((messageText.length() == 0) && (manager.getUploadDocPath() == null)) {
					return;
				}
                Log.d("###", "send message");
				boolean result = mChatService.sendMessage(messageText, manager.getUploadDocId(), manager.getUploadDocPath());
                manager.clearUploadData();
                mChatAttachImageView.setImageResource(R.drawable.mes_add);
                mCancelAttachImageView.setVisibility(View.GONE);
				mMessageEditText.setText("");
			}
		});	
		
		mCloseImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

        mChatAttachImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        mChatAttachImageView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right,
                                       int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mImageHeight = bottom - top;
                mImageWith = right - left;
            }
        });

        mCancelAttachImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferenceManager manager = SharedPreferenceManager.getInstance(ChatActivity.this);
                manager.clearUploadData();
                mChatAttachImageView.setImageResource(R.drawable.mes_add);
                mCancelAttachImageView.setVisibility(View.GONE);
            }
        });

        mClosePreviewImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDisappearAnimator.start();
            }
        });
		
		sConn = new ServiceConnection() {
			public void onServiceConnected(ComponentName name, IBinder binder) {
				Log.d(TAG, "MainActivity onServiceConnected");
				mChatService = ((ChatService.ChatBinder) binder).getService();
				bound = true;
                if (mChatService.initDone) {
                    createAdapter();
                }
			}

			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "MainActivity onServiceDisconnected");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("###", "disconnect service");
                        mChatService = null;
                    }
                });
                bound = false;
			}
		};
		
		mReceiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				if (mChatService == null) {
					return;
				}
				switch (intent.getIntExtra(ChatService.MSG_ID, 0)) {
				case ChatService.INIT_DONE:
                    createAdapter();
					break;
				case ChatService.INCOMMING_MESSAGE:
                    if (adapter != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.swapCursor(ChatDBManager.getInstance(ChatActivity.this).getAllMessages()).close();
                                adapter.notifyDataSetChanged();
                                mMessagesContainer.setSelection(adapter.getCount() - 1);
                            }
                        });
                    }
					break;
				default:
					Log.w(TAG,
							"invalid message id got in receiver: "
									+ intent.getIntExtra(ChatService.MSG_ID, 0));
					break;
				}
			}
		};
	}

    public void startImagePreview(String pathToFile) {
        GeneralUtils utils = new GeneralUtils();
        mPreviewImageView.setImageBitmap(utils.decodeImageFromFile(pathToFile, mImagePreviewWidth, mImagePreviewHeight));
        mAppearAnimator.start();
    }

    private void createAdapter() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = ChatDBManager.getInstance(ChatActivity.this).getAllMessages();
                adapter = new ChatAdapter(ChatActivity.this, R.layout.list_item_message, cursor, 0);
                adapter.setUser(mChatService.getUser());
                mMessagesContainer.setAdapter(adapter);
                mMessagesContainer.setSelection(adapter.getCount() - 1);

                mProgressBar.setVisibility(View.GONE);
                mSendImageView.setEnabled(true);
                mStatusImageView.setImageResource(R.drawable.mes_online);
            }
        });
    }
	
	@Override
    protected void onResume() {
		IntentFilter intFilt = new IntentFilter(ChatService.BROADCAST_ACTION);
	    registerReceiver(mReceiver, intFilt);
	    
	    mProgressBar.setVisibility(View.VISIBLE);
		mSendImageView.setEnabled(false);
		mStatusImageView.setImageResource(R.drawable.mes_offline);
		
		Intent intent = new Intent(this, ChatService.class);
		bindService(intent, sConn, BIND_AUTO_CREATE);
        super.onResume();
    }
	
	@Override
	protected void onPause() {
		Log.d("###", "onPause");
		
		if (bound) unbindService(sConn);
	    bound = false;
	    unregisterReceiver(mReceiver);
		super.onPause();
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("###", "requestCode: " + requestCode + " resultCode: " + resultCode);
        final SharedPreferenceManager manager = SharedPreferenceManager.getInstance(ChatActivity.this);
        if (resultCode == RESULT_OK) {
            Uri uriToResolve;
            String path;
            GeneralUtils utils = new GeneralUtils();
            if (requestCode == CardListAdapter.SELECT_FILE) {
                uriToResolve = data.getData();
                path = utils.getPath(getApplicationContext(), uriToResolve);
            } else {
                path = manager.getUploadDocPath();
                Log.d("###", "path: " + path);
            }
            if (path == null) {
                Toast.makeText(getApplicationContext(), "can't upload file", Toast.LENGTH_LONG).show();
                manager.clearUploadData();
                return;
            }

            mChatAttachImageView.setImageBitmap(utils.decodeImageFromFile(path, mImageWith, mImageHeight));
            mUploadImageProgressBar.setVisibility(View.VISIBLE);
            mSendImageView.setEnabled(false);

            File file = new File(path);
            final String pathToFile = path;
            QBContent.uploadFileTask(file, true, null, new QBEntityCallbackImpl<QBFile>() {
                @Override
                public void onSuccess(QBFile qbFile, Bundle params) {
                    mUploadImageProgressBar.setVisibility(View.GONE);
                    mSendImageView.setEnabled(true);
                    mCancelAttachImageView.setVisibility(View.VISIBLE);
                    manager.setUploadDocPath(qbFile.getPublicUrl());
                    manager.setUploadDocId("" + qbFile.getId().intValue());
                }

                @Override
                public void onError(List<String> errors) {
                    Log.e(TAG, "error in uploading file: " + pathToFile);
                    for (String error: errors) {
                        Log.e(TAG, "error: " + error);
                    }
                    mChatAttachImageView.setImageResource(R.drawable.mes_add);
                    mUploadImageProgressBar.setVisibility(View.GONE);
                    mSendImageView.setEnabled(true);
                    manager.clearUploadData();
                }
            });

        } else {
            manager.clearUploadData();
        }
    }

    private void selectImage() {
        final Context context = this;
        Resources res = getResources();
        final String[] items = { res.getString(R.string.PHOTO_TAKE_NEW),
                res.getString(R.string.PHOTO_GALLERY),
                res.getString(R.string.CANCEL_BUTTON) };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                Resources res = getResources();
                SharedPreferenceManager manager = SharedPreferenceManager.getInstance(context);
                if (items[item].equals(res.getString(R.string.PHOTO_TAKE_NEW))) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    String fileName;
                    Calendar cal = Calendar.getInstance();
                    fileName = String.format(Locale.getDefault(), "attachment_%d-%d-%d %d-%d.jpg", cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH), cal.get(Calendar.DATE), cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE));
                    File f = new File(android.os.Environment.getExternalStorageDirectory() + "/octoberry", fileName);
                    manager.setUploadDocPath(f.getAbsolutePath());
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    ChatActivity.this.startActivityForResult(intent, GeneralUtils.REQUEST_CAMERA);
                } else if (items[item].equals(res.getString(R.string.PHOTO_GALLERY))) {
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    ChatActivity.this.startActivityForResult(Intent.createChooser(intent, "Select File"), GeneralUtils.SELECT_FILE);
                } else if (items[item].equals(res.getString(R.string.CANCEL_BUTTON))) {
                    manager.clearUploadData();
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
}
