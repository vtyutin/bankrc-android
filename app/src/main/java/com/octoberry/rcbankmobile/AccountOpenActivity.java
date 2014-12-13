package com.octoberry.rcbankmobile;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import org.json.JSONObject;

import com.octoberry.rcbankmobile.chat.ChatActivity;
import com.octoberry.rcbankmobile.db.DataBaseManager;
import com.octoberry.rcbankmobile.db.SharedPreferenceManager;
import com.octoberry.rcbankmobile.handler.Document;
import com.octoberry.rcbankmobile.net.AsyncFileLoader;
import com.octoberry.rcbankmobile.net.AsyncFileUploader;
import com.octoberry.rcbankmobile.net.AsyncJSONLoader;
import com.octoberry.rcbankmobile.net.FileResponseListener;
import com.octoberry.rcbankmobile.net.JSONResponseListener;
import com.octoberry.rcbankmobile.ui.CardListAdapter;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;

public class AccountOpenActivity extends Activity {
	private final static String TAG = AccountOpenActivity.class.getName();
	
	private TextView mAccountNumberTextView;
	private TextView mGenerateACPTextView;
	private TextView mUploadDocumentsTextView;
	private TextView mPrepareDocumentsTextView;
	private TextView mAllDoneTextView;
	private ImageView mCredsImageView;
	private ImageView mChatImageView;
	private LinearLayout mMenuLinearLayout;
	private View mShadowView;
	private TextView mSmsTextView;
	private TextView mEmailTextView;
	private TextView mCancelTextView;
	private RelativeLayout mCheckListLayout;
	private RelativeLayout mRootLayout;
	private RelativeLayout mSnilsLayout;
	private RelativeLayout mFounderListLayout;
	private ImageView mCloseCheckListIV;
	private ImageView mCloseSnilsImageView;
	private ListView mCheckListView;
	private CheckListAdapter mCheckListAdapter;
	private EditText mSnilsEditText;
	private TextView mCheckSnilsTextView;
	private ProgressBar mSnilsProgress;
	private ListView mFounderListView;
	private FounderListAdapter mFounderListAdapter;
	private ImageView mCloseFounderListIV;
	
	private String mToken;
	
	private ArrayList<Document> mDocumentsCheckList = null;
	private ArrayList<Document> mFounderCheckList = null;
	
	private int mScreenHeight = 0;
	
	public final static int REQUEST_CAMERA = 0;
	public final static int SELECT_FILE = 1;
	
	private boolean isACPGenerated = false;
	private boolean isCheckListCompleted = false;
	private boolean isAllDocumentsUploaded = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account_open);

		mAccountNumberTextView = (TextView)findViewById(R.id.accountNumberTextView);
		mGenerateACPTextView = (TextView)findViewById(R.id.generateACPTextView);
		mUploadDocumentsTextView = (TextView)findViewById(R.id.uploadTextView);
		mPrepareDocumentsTextView = (TextView)findViewById(R.id.prepareTextView);
		mAllDoneTextView = (TextView)findViewById(R.id.allDoneTextView);
		mCredsImageView = (ImageView)findViewById(R.id.credsImageView);
		mChatImageView = (ImageView)findViewById(R.id.chatImageView);
		mMenuLinearLayout = (LinearLayout) findViewById(R.id.menuLinearLayout);
		mSmsTextView = (TextView) findViewById(R.id.sendSmsTextView);
		mEmailTextView = (TextView) findViewById(R.id.sendEmailTextView);
		mCancelTextView = (TextView) findViewById(R.id.cancelTextView);
		mShadowView = findViewById(R.id.shadowView);
		mCheckListLayout = (RelativeLayout)findViewById(R.id.checkListLayout);
		mSnilsLayout = (RelativeLayout)findViewById(R.id.snilsLayout);
		mFounderListLayout = (RelativeLayout)findViewById(R.id.founderListLayout);
		mCloseCheckListIV = (ImageView)mCheckListLayout.findViewById(R.id.closeImageView);
		mCloseSnilsImageView = (ImageView)mSnilsLayout.findViewById(R.id.closeImageView);
		mCheckListView = (ListView)findViewById(R.id.checkListView);
		mSnilsEditText = (EditText)mSnilsLayout.findViewById(R.id.snilsEditText);
		mCheckSnilsTextView = (TextView)mSnilsLayout.findViewById(R.id.checkSnilsTextView);
		mSnilsProgress = (ProgressBar)mSnilsLayout.findViewById(R.id.progressBar);
		mFounderListView = (ListView)mFounderListLayout.findViewById(R.id.passportsListView);
		mCloseFounderListIV = (ImageView)mFounderListLayout.findViewById(R.id.closeImageView);
		
		mToken = DataBaseManager.getInstance(getApplicationContext()).getActiveToken();
		
		mDocumentsCheckList = getIntent().getParcelableArrayListExtra("check_list_documents");
		mCheckListAdapter = new CheckListAdapter(AccountOpenActivity.this);
		mCheckListView.setAdapter(mCheckListAdapter);
		
		mFounderCheckList = getIntent().getParcelableArrayListExtra("founder_check_list");
		mFounderListAdapter = new FounderListAdapter(AccountOpenActivity.this);
		mFounderListView.setAdapter(mFounderListAdapter);
		
		mRootLayout = (RelativeLayout)findViewById(R.id.rootLayout);
		mRootLayout.addOnLayoutChangeListener(new OnLayoutChangeListener() {			
			@Override
			public void onLayoutChange(View v, int left, int top, int right,
					int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				mScreenHeight = bottom - top;
				mCheckListLayout.setTranslationY(mScreenHeight);
				mSnilsLayout.setTranslationY(mScreenHeight); 
				mRootLayout.removeOnLayoutChangeListener(this);
			}
		});
		
		String accountNumber = SharedPreferenceManager.getInstance(this).getAccountNumber();
		if (accountNumber != null) {
			mAccountNumberTextView.setText(accountNumber);
		}
		
		if (SharedPreferenceManager.getInstance(this).isSignatureCreated()) {
			mGenerateACPTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check, R.drawable.hor_line_grey);
		}
		
		mShadowView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				arg0.performClick();
				return true;
			}
		});
		
		mSmsTextView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				String uri= "smsto:";
	            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(uri));
	            intent.putExtra("sms_body", SharedPreferenceManager.getInstance(AccountOpenActivity.this).getCreds());
	            intent.putExtra("compose_mode", true);
	            startActivity(intent);			
			}
		});
		
		mEmailTextView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("message/rfc822");
				intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.ACCOUNT_DETAILS));
				
				File f = new File(android.os.Environment.getExternalStorageDirectory(), "credentials.pdf");
				if (f.exists()) {
					intent.putExtra(Intent.EXTRA_STREAM, Uri.parse( "file://" + f.getAbsolutePath()));
				} else {
					intent.putExtra(Intent.EXTRA_TEXT, SharedPreferenceManager.getInstance(AccountOpenActivity.this).getCreds());
				}
				try {
				    startActivity(Intent.createChooser(intent, SharedPreferenceManager.getInstance(AccountOpenActivity.this).getCredsTitle()));
				} catch (android.content.ActivityNotFoundException ex) {
				    Toast.makeText(AccountOpenActivity.this, "There are no email clients installed.", Toast.LENGTH_LONG).show();
				}
			}
		});
		
		mCancelTextView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				mMenuLinearLayout.setVisibility(View.GONE);
				mShadowView.setVisibility(View.GONE);
			}
		});
		
		mCredsImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mMenuLinearLayout.setVisibility(View.VISIBLE);
				mShadowView.setVisibility(View.VISIBLE);
			}
		});
		
		mUploadDocumentsTextView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				showView(mFounderListLayout);
			}
		});
		
		mCloseFounderListIV.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				hideView(mFounderListLayout);				
			}
		});
		
		mPrepareDocumentsTextView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				showView(mCheckListLayout);
			}
		});
		
		mCloseCheckListIV.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				hideView(mCheckListLayout);				
			}
		});
		
		mGenerateACPTextView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				showView(mSnilsLayout);
			}
		});
		
		mCloseSnilsImageView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				hideView(mSnilsLayout);				
			}
		});
		
		mSnilsEditText.addTextChangedListener(new TextWatcher() {			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.toString().length() > 10) {
					mCheckSnilsTextView.setVisibility(View.VISIBLE);
				} else {
					mCheckSnilsTextView.setVisibility(View.GONE);
				}
			}			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}			
			@Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                String numbersOnly = keepNumbersOnly(input);
                String code = formatNumbersAsCode(numbersOnly);

                mSnilsEditText.removeTextChangedListener(this);
                mSnilsEditText.setText(code);
                // You could also remember the previous position of the cursor
                mSnilsEditText.setSelection(code.length());
                mSnilsEditText.addTextChangedListener(this);
            }			

            private String formatNumbersAsCode(CharSequence s) {
                int groupDigits = 0;
                int groups = 0;
                String tmp = "";
                for (int i = 0; i < s.length(); ++i) {
                    tmp += s.charAt(i);
                    ++groupDigits;
                    if (groupDigits == 3) {
                    	if (groups >= 3) {
                    		tmp += " ";
                    	} else {
                    		tmp += "-";
                    	}
                        groupDigits = 0;
                        groups++;
                    }
                }
                return tmp;
            }
		});
		
		mCheckSnilsTextView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				mSnilsProgress.setVisibility(View.VISIBLE);
				AsyncJSONLoader loader = new AsyncJSONLoader(AccountOpenActivity.this);		
				loader.registryListener(new SnilsHandler());
				Bundle params = new Bundle();
				Bundle bodyParams = new Bundle();
				Bundle headerParams = new Bundle();
				params.putString("requestType", "POST");
				headerParams.putString("Authorization", mToken);
				params.putString("endpoint", "/api/bank/signature");										
				bodyParams.putString("snils", keepNumbersOnly(mSnilsEditText.getText().toString()));		
				Log.d("###", "snils: " + bodyParams.getString("snils"));
				Log.d("###", "token: " + mToken);
				mCheckSnilsTextView.setVisibility(View.GONE);
				mSnilsEditText.setText("");
				loader.execute(params, headerParams, bodyParams);
			}
		});
		
		mAllDoneTextView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if (mAllDoneTextView.getTextColors().getDefaultColor() == Color.WHITE) {
					DataBaseManager.getInstance(AccountOpenActivity.this).setAccountStatus(DataBaseManager.ACCOUNT_STATUS_PREPARED);
					Intent intent = new Intent(AccountOpenActivity.this, AccountReadyActivity.class);
					startActivity(intent);
					finish();
				}
			}
		});
		
		mChatImageView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(AccountOpenActivity.this, ChatActivity.class);
				startActivity(intent);
			}
		});
		
		/* get credentials */		
		File f = new File(android.os.Environment.getExternalStorageDirectory(), "credentials.pdf");
		if (f.exists() == false) {			
			AsyncFileLoader loader = new AsyncFileLoader(this, f.getAbsolutePath());
			loader.registryListener(new GetCredentialsHandler());
			Bundle params = new Bundle();
			params.putString("requestType", "GET");
			params.putString("endpoint", "/api/organization/creds");
			Bundle headerParams = new Bundle();
			headerParams.putString("Authorization", mToken);
			headerParams.putString("Accept", "application/pdf");
			loader.execute(params, headerParams, null);
		}
	}
	
	private void checkActivityCompleted() {
		if (isACPGenerated && isCheckListCompleted && isAllDocumentsUploaded) {
			mAllDoneTextView.setEnabled(true);
			mAllDoneTextView.setBackgroundColor(Color.BLACK);
			mAllDoneTextView.setTextColor(Color.WHITE);
		}
	}
	
	private void showView(View view) {
		view.setTranslationY(mScreenHeight);
		view.setVisibility(View.VISIBLE);
		ObjectAnimator appearAnimator = ObjectAnimator.ofFloat(view, "translationY", mScreenHeight, 0);
		appearAnimator.setDuration(1000);
		appearAnimator.start();
	}
	
	private void hideView(View view) {
		final View viewToHide = view;
		ObjectAnimator disappearAnimator = ObjectAnimator.ofFloat(viewToHide, "translationY", 0, mScreenHeight);
		disappearAnimator.setDuration(1000);
		disappearAnimator.addListener(new AnimatorListener() {					
			@Override
			public void onAnimationStart(Animator animation) {}					
			@Override
			public void onAnimationRepeat(Animator animation) {}					
			@Override
			public void onAnimationEnd(Animator animation) {
				viewToHide.setVisibility(View.GONE);
			}					
			@Override
			public void onAnimationCancel(Animator animation) {}
		});
		disappearAnimator.start();	
	}
	
	private String keepNumbersOnly(CharSequence s) {
        return s.toString().replaceAll("[^0-9]", ""); // Should of course be more robust
    }
	
	private void checkListCompletion() {
		if (mDocumentsCheckList != null) {
			boolean listIsCompleted = true;
			for (int i = 0; i < mDocumentsCheckList.size(); i++) {
				if (!mDocumentsCheckList.get(i).isCompleted()) {
					listIsCompleted = false;
					break;
				}
			}
			if (listIsCompleted) {
				isCheckListCompleted = true;
				mPrepareDocumentsTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check, R.drawable.hor_line_grey);
			} else {
				mPrepareDocumentsTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.list_item_arrow, R.drawable.hor_line_grey);
			}
			checkActivityCompleted();
		}		
	}

	class GetCredentialsHandler implements FileResponseListener {
		@Override
		public void handleResponse(int result, String path) {
			if (result == 200) {
				Log.d(TAG, "Credentials file downloaded: " + path);
				mCredsImageView.setVisibility(View.VISIBLE);
			} else {
				Toast.makeText(AccountOpenActivity.this, path, Toast.LENGTH_LONG).show();
				Log.e(TAG, "can't load credentials file. response code: " + result);
			}			
		}		
	}
	
	class SnilsHandler implements JSONResponseListener {

		@Override
		public void handleResponse(int result, JSONObject response, String error) {
			mSnilsProgress.setVisibility(View.GONE);
			mGenerateACPTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.list_item_arrow, R.drawable.hor_line_grey);
			if (response != null) {
				Log.d("###", response.toString());
				try {
					int status = response.getInt("status");
					if (status == 200) {
						mGenerateACPTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check, R.drawable.hor_line_grey);
						hideView(mSnilsLayout);
						isACPGenerated = true;
						checkActivityCompleted();
					} else {
						Toast.makeText(AccountOpenActivity.this, "error: " + response.getString("message") , Toast.LENGTH_LONG).show();
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}			
		}		
	}
	
	private class CheckListAdapter extends ArrayAdapter<Object> {
		private final Context context;
		
		public CheckListAdapter(Context context) {
			super(context, R.layout.check_list_card_row);
			this.context = context;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {			
			View rowView = convertView;
			final int pos = position;
			if (rowView == null) {
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(R.layout.check_list_row, null);				
			}
			TextView textView = (TextView)rowView.findViewById(R.id.titleTextView);
			textView.setText(mDocumentsCheckList.get(position).getTitle());
			if (mDocumentsCheckList.get(pos).isCompleted()) {				
				textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			} else {
				textView.setPaintFlags(0);
			}
			
			textView.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					mDocumentsCheckList.get(pos).setCompleted(!mDocumentsCheckList.get(pos).isCompleted());
					checkListCompletion();
					mCheckListAdapter.notifyDataSetChanged();									
				}
			});
			
			return rowView;
		}
		
		@Override
		public int getCount() {
			if (mDocumentsCheckList != null) {
				return mDocumentsCheckList.size();
			}
			return 0;
		}
	}
	
	private class FounderListAdapter extends ArrayAdapter<Object> {
		private final Context context;
		
		public FounderListAdapter(Context context) {
			super(context, R.layout.passports_list_row);
			this.context = context;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {			
			View rowView = convertView;
			if (rowView == null) {
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(R.layout.passports_list_row, null);				
			}
			TextView textView = (TextView)rowView.findViewById(R.id.titleTextView);
			textView.setText(mFounderCheckList.get(position).getTitle());			
			
			TextView firstPageTextView = (TextView)rowView.findViewById(R.id.firstPageTextView);
			firstPageTextView.setOnClickListener(new UploadDocumentListener(mFounderCheckList.get(position).getId(), 0));
			TextView secondPageTextView = (TextView)rowView.findViewById(R.id.secondPageTextView);
			secondPageTextView.setOnClickListener(new UploadDocumentListener(mFounderCheckList.get(position).getId(), 1));
			
			ImageView firstPageThumbnail = (ImageView)rowView.findViewById(R.id.firstPageThumbnail);
			ImageView secondPageThumbnail = (ImageView)rowView.findViewById(R.id.secondPageThumbnail);
			ImageView firstPageRemove = (ImageView)rowView.findViewById(R.id.firstPageRemove);
			ImageView secondPageRemove = (ImageView)rowView.findViewById(R.id.secondPageRemove);			
			if (mFounderCheckList.get(position).isFirstPageUploaded()) {
				Bitmap bitmap = BitmapFactory.decodeFile(mFounderCheckList.get(position).getFirstPageFilePath());
				firstPageThumbnail.setImageBitmap(bitmap);
				firstPageThumbnail.setVisibility(View.VISIBLE);
				firstPageRemove.setVisibility(View.VISIBLE);
			}
			if (mFounderCheckList.get(position).isSecondPageUploaded()) {
				Bitmap bitmap = BitmapFactory.decodeFile(mFounderCheckList.get(position).getSecondPageFilePath());
				secondPageThumbnail.setImageBitmap(bitmap);
				secondPageThumbnail.setVisibility(View.VISIBLE);
				secondPageRemove.setVisibility(View.VISIBLE);
			}			
			
			ProgressBar firstPageProgressBar = (ProgressBar)rowView.findViewById(R.id.firstPageProgressBar);
			ProgressBar secondPageProgressBar = (ProgressBar)rowView.findViewById(R.id.secondPageProgressBar);
			firstPageProgressBar.setVisibility(View.GONE);
			secondPageProgressBar.setVisibility(View.GONE);
			SharedPreferenceManager manager = SharedPreferenceManager.getInstance(context);
			if (mFounderCheckList.get(position).getId().equals(manager.getUploadDocId())) {
				if (manager.getUploadDocPage() == 0) {
					firstPageProgressBar.setVisibility(View.VISIBLE);
				} else if (manager.getUploadDocPage() == 1) {
					secondPageProgressBar.setVisibility(View.VISIBLE);
				}
			}
			
			return rowView;
		}
		
		@Override
		public int getCount() {
			if (mFounderCheckList != null) {
				return mFounderCheckList.size();
			}
			return 0;
		}
		
		class UploadDocumentListener implements OnClickListener {
			private String documentId = null;
			private int pageIndex = -1;
			UploadDocumentListener(String documentId, int pageIndex) {
				this.documentId = documentId;
				this.pageIndex = pageIndex;
			}
			@Override
			public void onClick(View v) {
				SharedPreferenceManager manager = SharedPreferenceManager.getInstance(context);			
				if (manager.getUploadDocId() != null) {
					Toast.makeText(context, context.getResources().getString(R.string.UPLOAD_IN_PROGRESS), Toast.LENGTH_LONG).show();
				} else {
					manager.setUploadDocId(documentId);
					manager.setUploadDocPage(pageIndex);
					selectImage();
					notifyDataSetChanged();
				}			
			}
		}
		
		private void selectImage() {
			Resources res = context.getResources();
			final String[] items = { res.getString(R.string.PHOTO_TAKE_NEW),
					res.getString(R.string.PHOTO_GALLERY),
					res.getString(R.string.CANCEL_BUTTON) };
			
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setItems(items, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int item) {
					Resources res = context.getResources();
					SharedPreferenceManager manager = SharedPreferenceManager.getInstance(context);
					if (items[item].equals(res.getString(R.string.PHOTO_TAKE_NEW))) {
						Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
						String fileName;
						Calendar cal = Calendar.getInstance();
						fileName = String.format(Locale.getDefault(), "scan_%d-%d-%d %d-%d.jpg", cal.get(Calendar.YEAR),
								cal.get(Calendar.MONTH), cal.get(Calendar.DATE), cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE));
						File f = new File(android.os.Environment.getExternalStorageDirectory(), fileName);
						manager.setUploadDocPath(f.getAbsolutePath());
						intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
						AccountOpenActivity.this.startActivityForResult(intent, REQUEST_CAMERA);
					} else if (items[item].equals(res.getString(R.string.PHOTO_GALLERY))) {
						Intent intent = new Intent(Intent.ACTION_PICK,
								android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
						intent.setType("image/*");
						AccountOpenActivity.this.startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
					} else if (items[item].equals(res.getString(R.string.CANCEL_BUTTON))) {
						manager.clearUploadDocId();
						manager.clearUploadDocPage();
						manager.clearUploadDocPath();
						dialog.dismiss();
						mFounderListAdapter.notifyDataSetChanged();
					}
				}
			});
			builder.show();
		}
	}
	
	class FileUploadHandler implements JSONResponseListener {
		@Override
		public void handleResponse(int result, JSONObject response, String error) {
			SharedPreferenceManager manager = SharedPreferenceManager.getInstance(AccountOpenActivity.this);
			if (response != null) {
				Log.d("###", response.toString());
				try {
					int resultCode = response.getInt("status");
					if (resultCode == 200) {
						Log.d("###", "file is uploaded to server");						
						String id = manager.getUploadDocId();
						int pageIndex = manager.getUploadDocPage();
						for (int i = 0; i < mFounderCheckList.size(); i++) {
							Document doc = mFounderCheckList.get(i);
							if ((id != null) && (id.equals(doc.getId()))) {								
								switch (pageIndex) {
									case 0:
										doc.setFirstPageUploaded(true);
										doc.setFirstPageFilePath(manager.getUploadDocPath());
										break;
									case 1:
										doc.setSecondPageUploaded(true);
										doc.setSecondPageFilePath(manager.getUploadDocPath());
										break;	
									default:
										break;
								}
								if (doc.isFirstPageUploaded() && doc.isSecondPageUploaded()) {
									doc.setCompleted(true);
								}
								break;
							}
						}
						boolean isALLUploaded = true;
						for (int i = 0; i < mFounderCheckList.size(); i++) {
							Document doc = mFounderCheckList.get(i);
							if (!doc.isCompleted()) {
								isALLUploaded = false;
								break;
							}
						}
						if (isALLUploaded) {
							mUploadDocumentsTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check, R.drawable.hor_line_grey);
						}
						isAllDocumentsUploaded = isALLUploaded;
						checkActivityCompleted();
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
			manager.clearUploadDocId();
			manager.clearUploadDocPage();
			manager.clearUploadDocPath();
			mFounderListAdapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("###", "requestCode: " + requestCode + " resultCode: " + resultCode);
		SharedPreferenceManager manager = SharedPreferenceManager.getInstance(AccountOpenActivity.this);
		if (resultCode == RESULT_OK) {
			Uri uriToResolve;
			String path;
			if (requestCode == CardListAdapter.SELECT_FILE) {
				uriToResolve = data.getData();
				path = getPath(getApplicationContext(), uriToResolve);
			} else {				
				path = manager.getUploadDocPath();
				Log.d("###", "path: " + path);
			}			
			if (path == null) {
				Toast.makeText(getApplicationContext(), "can't upload file", Toast.LENGTH_LONG).show();
				manager.clearUploadDocId();
				manager.clearUploadDocPage();
				manager.clearUploadDocPath();
				return;
			}
			manager.setUploadDocPath(path);

			AsyncFileUploader asyncLoader = new AsyncFileUploader(this);
			asyncLoader.registryListener(new FileUploadHandler());
			Bundle params = new Bundle();
			params.putString("requestType", "POST");
			params.putString("endpoint", "/api/organization/upload");
			params.putString("filePath", path);
			Bundle headerParams = new Bundle();
			headerParams.putString("Authorization", mToken);
			asyncLoader.execute(params, headerParams, null);
		} else {
			manager.clearUploadDocId();
			manager.clearUploadDocPage();
			manager.clearUploadDocPath();
		}
	}

	private String getPath(final Context context, final Uri uri) {
		// MediaStore (and general)
		if ("content".equalsIgnoreCase(uri.getScheme())) {
			// Return the remote address
			if (isGooglePhotosUri(uri)) {
				String path = uri.getLastPathSegment();
				if (path.startsWith("/"))
					path = path.substring(1);
				return path;
			}
			return getDataColumn(context, uri, null, null);
		}
		return null;
	}

	public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };
		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	public static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri.getAuthority());
	}
}
