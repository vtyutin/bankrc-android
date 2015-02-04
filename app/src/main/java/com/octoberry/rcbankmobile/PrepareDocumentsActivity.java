package com.octoberry.rcbankmobile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;

import com.flurry.android.FlurryAgent;
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
import android.os.Environment;
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

public class PrepareDocumentsActivity extends Activity {
	private final static String TAG = PrepareDocumentsActivity.class.getName();
	
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
	
	private boolean isACPGenerated = false;
	private boolean isCheckListCompleted = false;
	private boolean isAllDocumentsUploaded = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_prepare_documents);

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
		
		mToken = DataBaseManager.getInstance(getApplicationContext()).getCrmToken();
		
		mDocumentsCheckList = getIntent().getParcelableArrayListExtra("check_list_documents");
		mCheckListAdapter = new CheckListAdapter(PrepareDocumentsActivity.this);
		mCheckListView.setAdapter(mCheckListAdapter);
		
		mFounderCheckList = getIntent().getParcelableArrayListExtra("founder_check_list");
		mFounderListAdapter = new FounderListAdapter(PrepareDocumentsActivity.this);
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
            isACPGenerated = true;
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
	            intent.putExtra("sms_body", SharedPreferenceManager.getInstance(PrepareDocumentsActivity.this).getCreds());
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
				
				File f = new File(android.os.Environment.getExternalStorageDirectory() + "/octoberry", "credentials.pdf");
				if (f.exists()) {
					intent.putExtra(Intent.EXTRA_STREAM, Uri.parse( "file://" + f.getAbsolutePath()));
				} else {
					intent.putExtra(Intent.EXTRA_TEXT, SharedPreferenceManager.getInstance(PrepareDocumentsActivity.this).getCreds());
				}
				try {
				    startActivity(Intent.createChooser(intent, SharedPreferenceManager.getInstance(PrepareDocumentsActivity.this).getCredsTitle()));
				} catch (android.content.ActivityNotFoundException ex) {
				    Toast.makeText(PrepareDocumentsActivity.this, "There are no email clients installed.", Toast.LENGTH_LONG).show();
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
				AsyncJSONLoader loader = new AsyncJSONLoader(PrepareDocumentsActivity.this);
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
					DataBaseManager.getInstance(PrepareDocumentsActivity.this).setAccountStatus(DataBaseManager.ACCOUNT_STATUS_PREPARED);
					Intent intent = new Intent(PrepareDocumentsActivity.this, SetPincodeActivity.class);
					startActivity(intent);
					finish();
				}
			}
		});
		
		mChatImageView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(PrepareDocumentsActivity.this, ChatActivity.class);
				startActivity(intent);
			}
		});
		
		/* get credentials */		
		File f = new File(android.os.Environment.getExternalStorageDirectory() + "/octoberry", "credentials.pdf");
		if (f.exists() == false) {			
			AsyncFileLoader loader = new AsyncFileLoader(this, f.getAbsolutePath());
			loader.registryListener(new GetCredentialsHandler());
			Bundle params = new Bundle();
			params.putString("requestType", "GET");
			params.putString("endpoint", "/api/organization.pdf");
			Bundle headerParams = new Bundle();
			headerParams.putString("Authorization", mToken);
			headerParams.putString("Accept", "application/pdf");
			loader.execute(params, headerParams, null);
		}

        checkActivityCompleted();

        SharedPreferenceManager manager = SharedPreferenceManager.getInstance(PrepareDocumentsActivity.this);
        if (manager.getUploadDocId() != null) {
            showView(mFounderListLayout);
        }
	}
	
	private void checkActivityCompleted() {
        SharedPreferenceManager manager = SharedPreferenceManager.getInstance(this);
        if (mDocumentsCheckList != null) {
            isCheckListCompleted = true;
            for (Document doc: mDocumentsCheckList) {
                if (manager.getCheckDocStatus(doc.getTitle()) == false) {
                    isCheckListCompleted = false;
                    break;
                }
            }
        }

        if (mFounderCheckList != null) {
            isAllDocumentsUploaded = true;
            for (Document doc: mFounderCheckList) {
                if (manager.getUploadedDocPath(doc.getFirstPageId()) == null) {
                    isAllDocumentsUploaded = false;
                    break;
                }
                if (doc.getType().equals(Document.TYPE_PASSPORT)) {
                    if (manager.getUploadedDocPath(doc.getSecondPageId()) == null) {
                        isAllDocumentsUploaded = false;
                        break;
                    }
                }
            }
        }

        if (isAllDocumentsUploaded) {
            mUploadDocumentsTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check, R.drawable.hor_line_grey);
        } else {
            mUploadDocumentsTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.list_item_arrow, R.drawable.hor_line_grey);
        }

        if (isACPGenerated) {
            mGenerateACPTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check, R.drawable.hor_line_grey);
        } else {
            mGenerateACPTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.list_item_arrow, R.drawable.hor_line_grey);
        }

        if (isCheckListCompleted) {
            mPrepareDocumentsTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check, R.drawable.hor_line_grey);
        } else {
            mPrepareDocumentsTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.list_item_arrow, R.drawable.hor_line_grey);
        }

		if (isACPGenerated && isAllDocumentsUploaded) {
			mAllDoneTextView.setEnabled(true);
			mAllDoneTextView.setBackgroundColor(Color.BLACK);
			mAllDoneTextView.setTextColor(Color.WHITE);
		} else {
            mAllDoneTextView.setEnabled(false);
            mAllDoneTextView.setBackgroundColor(Color.parseColor("#CCCCCC"));
            mAllDoneTextView.setTextColor(Color.parseColor("#333333"));
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
				Toast.makeText(PrepareDocumentsActivity.this, path, Toast.LENGTH_LONG).show();
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
                        SharedPreferenceManager.getInstance(PrepareDocumentsActivity.this).setSignatureCreated(true);
						isACPGenerated = true;
						checkActivityCompleted();

                        // log flurry event
                        Map<String, String> articleParams = new HashMap<String, String>();
                        articleParams.put("snils", keepNumbersOnly(mSnilsEditText.getText().toString()));
                        FlurryAgent.logEvent("snils successfully verified", articleParams);
					} else {
						Toast.makeText(PrepareDocumentsActivity.this, "error: " + response.getString("message") , Toast.LENGTH_LONG).show();

                        // log flurry event
                        Map<String, String> articleParams = new HashMap<String, String>();
                        articleParams.put("message", response.getString("message"));
                        FlurryAgent.logEvent("snils verification failed", articleParams);
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
            SharedPreferenceManager manager = SharedPreferenceManager.getInstance(PrepareDocumentsActivity.this);
			TextView textView = (TextView)rowView.findViewById(R.id.titleTextView);
			textView.setText(mDocumentsCheckList.get(position).getTitle());
			if (manager.getCheckDocStatus(mDocumentsCheckList.get(pos).getTitle())) {
				textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			} else {
				textView.setPaintFlags(0);
			}
			
			textView.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
                    SharedPreferenceManager manager = SharedPreferenceManager.getInstance(PrepareDocumentsActivity.this);
                    manager.setCheckDocStatus(mDocumentsCheckList.get(pos).getTitle(), !manager.getCheckDocStatus(mDocumentsCheckList.get(pos).getTitle()));
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
            boolean isPassport = Document.TYPE_PASSPORT.equals(mFounderCheckList.get(position).getType());
			if (isPassport) {
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(R.layout.passports_list_row, null);
			} else {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.legal_list_row, null);
            }
			TextView textView = (TextView)rowView.findViewById(R.id.titleTextView);
			textView.setText(mFounderCheckList.get(position).getTitle());

			TextView firstPageTextView = (TextView)rowView.findViewById(R.id.firstPageTextView);
			firstPageTextView.setOnClickListener(new UploadDocumentListener(mFounderCheckList.get(position).getFirstPageId(),
                    0, mFounderCheckList.get(position).getType(), mFounderCheckList.get(position).getFirstPageTitle()));
            firstPageTextView.setText(mFounderCheckList.get(position).getFirstPageTitle());
            TextView secondPageTextView = (TextView) rowView.findViewById(R.id.secondPageTextView);
            if (isPassport) {
                secondPageTextView.setOnClickListener(new UploadDocumentListener(mFounderCheckList.get(position).getSecondPageId(),
                        1, mFounderCheckList.get(position).getType(), mFounderCheckList.get(position).getSecondPageTitle()));
                secondPageTextView.setText(mFounderCheckList.get(position).getSecondPageTitle());
            }

			ImageView firstPageThumbnail = (ImageView)rowView.findViewById(R.id.firstPageThumbnail);
            ImageView secondPageThumbnail = (ImageView) rowView.findViewById(R.id.secondPageThumbnail);
			ImageView firstPageRemove = (ImageView)rowView.findViewById(R.id.firstPageRemove);
            ImageView secondPageRemove = (ImageView) rowView.findViewById(R.id.secondPageRemove);

            SharedPreferenceManager manager = SharedPreferenceManager.getInstance(PrepareDocumentsActivity.this);
            String path = manager.getUploadedDocPath(mFounderCheckList.get(position).getFirstPageId());
            if (path != null) {
				Bitmap bitmap = BitmapFactory.decodeFile(path);
				firstPageThumbnail.setImageBitmap(bitmap);
				firstPageThumbnail.setVisibility(View.VISIBLE);
				firstPageRemove.setVisibility(View.VISIBLE);
                firstPageTextView.setOnClickListener(null);
                firstPageRemove.setOnClickListener(new RemoveDocumentListener(mFounderCheckList.get(position).getFirstPageId()));
			} else {
                firstPageThumbnail.setVisibility(View.GONE);
                firstPageRemove.setVisibility(View.GONE);
            }
            if (isPassport) {
                path = manager.getUploadedDocPath(mFounderCheckList.get(position).getSecondPageId());
                if (path != null) {
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    secondPageThumbnail.setImageBitmap(bitmap);
                    secondPageThumbnail.setVisibility(View.VISIBLE);
                    secondPageRemove.setVisibility(View.VISIBLE);
                    secondPageTextView.setOnClickListener(null);
                    secondPageRemove.setOnClickListener(new RemoveDocumentListener(mFounderCheckList.get(position).getSecondPageId()));
                } else {
                    secondPageThumbnail.setVisibility(View.GONE);
                    secondPageRemove.setVisibility(View.GONE);
                }
            }

			ProgressBar firstPageProgressBar = (ProgressBar)rowView.findViewById(R.id.firstPageProgressBar);
			ProgressBar secondPageProgressBar = (ProgressBar)rowView.findViewById(R.id.secondPageProgressBar);
			firstPageProgressBar.setVisibility(View.GONE);
            if (isPassport) {
                secondPageProgressBar.setVisibility(View.GONE);
            }

			if (mFounderCheckList.get(position).getFirstPageId().equals(manager.getUploadDocId())) {
                firstPageProgressBar.setVisibility(View.VISIBLE);
			}
            if (isPassport) {
                if (mFounderCheckList.get(position).getSecondPageId().equals(manager.getUploadDocId())) {
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
            private String documentType = null;
            private String documentTitle = null;
			UploadDocumentListener(String documentId, int pageIndex, String documentType, String documentTitle) {
				this.documentId = documentId;
				this.pageIndex = pageIndex;
                this.documentType = documentType;
                this.documentTitle = documentTitle;
			}
			@Override
			public void onClick(View v) {
				SharedPreferenceManager manager = SharedPreferenceManager.getInstance(context);
				if (manager.getUploadDocId() != null) {
					Toast.makeText(context, context.getResources().getString(R.string.UPLOAD_IN_PROGRESS), Toast.LENGTH_LONG).show();
				} else {
					manager.setUploadDocId(documentId);
					manager.setUploadDocPage(pageIndex);
                    manager.setUploadDocType(documentType);
                    manager.setUploadDocTitle(documentTitle);
					selectImage();
					notifyDataSetChanged();
				}
			}
		}

        class RemoveDocumentListener implements OnClickListener {
            private String documentId = null;
            RemoveDocumentListener(String documentId) {
                this.documentId = documentId;
            }
            @Override
            public void onClick(View v) {
                SharedPreferenceManager manager = SharedPreferenceManager.getInstance(context);
                String path = manager.getUploadedDocPath(documentId);
                if (path == null) {
                    return;
                }
                File file = new File(path);
                if (file.exists()) {
                    file.delete();
                }
                manager.clearUploadedDocPath(documentId);
                notifyDataSetChanged();
                checkActivityCompleted();
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
						File f = new File(android.os.Environment.getExternalStorageDirectory() + "/octoberry", fileName);
						manager.setUploadDocPath(f.getAbsolutePath());
						intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
						PrepareDocumentsActivity.this.startActivityForResult(intent, GeneralUtils.REQUEST_CAMERA);
					} else if (items[item].equals(res.getString(R.string.PHOTO_GALLERY))) {
						Intent intent = new Intent(Intent.ACTION_PICK,
								android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
						intent.setType("image/*");
						PrepareDocumentsActivity.this.startActivityForResult(Intent.createChooser(intent, "Select File"), GeneralUtils.SELECT_FILE);
					} else if (items[item].equals(res.getString(R.string.CANCEL_BUTTON))) {
						manager.clearUploadData();
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
			SharedPreferenceManager manager = SharedPreferenceManager.getInstance(PrepareDocumentsActivity.this);
			if (response != null) {
				Log.d("###", response.toString());
				try {
					int resultCode = response.getInt("status");
					if (resultCode == 200) {
						Log.d("###", "file is uploaded to server");						
						String id = manager.getUploadDocId();
                        String path = manager.getUploadDocPath();
                        Log.d("###", "id: " + id);
                        Log.d("###", "path: " + path);
                        GeneralUtils utils = new GeneralUtils();
                        String thumbnailPath = utils.decodeImageFromFile(path, manager.getUploadDocId(), 200, 200);
                        Log.d("###", "decoded image path: " + thumbnailPath);
                        manager.setUploadedDocPath(id, thumbnailPath);

                        boolean isDocFound = false;
						for (int i = 0; i < mFounderCheckList.size(); i++) {
							Document doc = mFounderCheckList.get(i);
							if ((id != null) && (id.equals(doc.getFirstPageId()))) {
                                doc.setFirstPageUploaded(true);
                                doc.setFirstPageFilePath(manager.getUploadDocPath());
                                isDocFound = true;
                            }
                            if ((id != null) && (id.equals(doc.getSecondPageId()))) {
                                doc.setSecondPageUploaded(true);
                                doc.setSecondPageFilePath(manager.getUploadDocPath());
                                isDocFound = true;
                            }
                            if (doc.getType().equals(Document.TYPE_PASSPORT)) {
                                if (doc.isFirstPageUploaded() && doc.isSecondPageUploaded()) {
                                    doc.setCompleted(true);
                                }
                            } else {
                                if (doc.isFirstPageUploaded()) {
                                    doc.setCompleted(true);
                                }
                            }
                            if (isDocFound) {
                                break;
                            }
						}
						checkActivityCompleted();
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
            manager.clearUploadData();
			mFounderListAdapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("###", "requestCode: " + requestCode + " resultCode: " + resultCode);
		SharedPreferenceManager manager = SharedPreferenceManager.getInstance(PrepareDocumentsActivity.this);
		if (resultCode == RESULT_OK) {
			Uri uriToResolve;
			String path;
			if (requestCode == CardListAdapter.SELECT_FILE) {
				uriToResolve = data.getData();
                GeneralUtils utils = new GeneralUtils();
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
            manager.setUploadDocPath(path);

			AsyncFileUploader asyncLoader = new AsyncFileUploader(this);
			asyncLoader.registryListener(new FileUploadHandler());
			Bundle params = new Bundle();
			params.putString("requestType", "POST");
			params.putString("endpoint", "/api/organization/upload");
			params.putString("filePath", path);
			Bundle headerParams = new Bundle();
            Bundle bodyParams = new Bundle();
            bodyParams.putString("doc_id", manager.getUploadDocId());
            bodyParams.putString("type", manager.getUploadDocType());
            bodyParams.putString("title", manager.getUploadDocTitle());
			headerParams.putString("Authorization", mToken);
			asyncLoader.execute(params, headerParams, bodyParams);
		} else {
            manager.clearUploadData();
		}
	}
}
