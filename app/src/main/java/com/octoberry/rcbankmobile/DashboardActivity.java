package com.octoberry.rcbankmobile;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.octoberry.rcbankmobile.chat.ChatActivity;
import com.octoberry.rcbankmobile.db.DataBaseManager;
import com.octoberry.rcbankmobile.db.SharedPreferenceManager;
import com.octoberry.rcbankmobile.handler.Correspondent;
import com.octoberry.rcbankmobile.handler.UserAccount;
import com.octoberry.rcbankmobile.net.AsyncFileLoader;
import com.octoberry.rcbankmobile.net.AsyncJSONLoader;
import com.octoberry.rcbankmobile.net.FileResponseListener;
import com.octoberry.rcbankmobile.net.JSONResponseListener;
import com.octoberry.rcbankmobile.payment.Payment;
import com.octoberry.rcbankmobile.payment.PaymentMenuActivity;
import com.octoberry.rcbankmobile.payment.PaymentSummActivity;
import com.octoberry.rcbankmobile.ui.DynamicListView;
import com.octoberry.rcbankmobile.ui.OnItemSwitchedListener;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DashboardActivity extends Activity {
	private final static String TAG = DashboardActivity.class.getName();

    private final static float BALANCE_TEXT_SIZE = 12.0f;
	
	private ImageView mAccountsImageView;
	private ImageView mChatImageView;
	private ImageView mActionImageView;
	private TextView mNameTextView;
    private TextView mNameCredsTextView;
	private TextView mBalanceTextView;
	private RelativeLayout mAccountDetailsLayout;
	private RelativeLayout mCredsDetailsLayout;
	private RelativeLayout mTopRelativeLayout;
	private ImageView mCredsCloseImageView;
	private ImageView mHandleImageView;
	private LinearLayout mMenuLinearLayout;
	private TextView mSmsTextView;
	private TextView mEmailTextView;
	private TextView mCancelTextView;
	private View mShadowView;
	private RelativeLayout mInitViewLayout;
	private TextView mFavouritePartnersTextView;
	private ListView mCorrespondersListView;
    private DynamicListView mAccountsListView;
    private TextView mExitTextView;
    private RelativeLayout mTransferRelativeLayout;
	
	private int mAccountViewHeight = 0;
	private int mTopViewHeight = 0;
	private int mHandleImageViewHeight = 0;
	private int mTopInitPosition = 0;
	private Rect mHandleButtonRect;
	
	private GestureDetectorCompat mDetector;
	private SimpleGestureListener mSimpleGestureListener = new SimpleGestureListener();
	
	private ArrayList<Correspondent> mCorrespondersList = new ArrayList<Correspondent>();
	private CorrespondersListAdapter mCorrespondersAdapter;

    private ArrayList<UserAccount> mAccountsList = new ArrayList<UserAccount>();
    private AccountsListAdapter mAccountsAdapter;
	
	private String mCrmToken;
	private String mBankToken;
	private String mCompanyName;
	private String mBalance;
	boolean isDataReceived = false;

    // TODO temp code
    private String bankName;
    private String inn;
    private String kpp;
    private String corrNumber;
    private String bik;
    // TODO temp code ended

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dashboard);
		
		mAccountsImageView = (ImageView)findViewById(R.id.accountsImageView);
		mChatImageView = (ImageView)findViewById(R.id.chatImageView);
		mActionImageView = (ImageView)findViewById(R.id.actionImageView);
		mNameTextView = (TextView)findViewById(R.id.nameTextView);
        mNameCredsTextView = (TextView)findViewById(R.id.nameCredsTextView);
		mBalanceTextView = (TextView)findViewById(R.id.balanceTextView);
		mCredsCloseImageView = (ImageView)findViewById(R.id.closeImageView);
		mHandleImageView = (ImageView)findViewById(R.id.handleImageView);
		mMenuLinearLayout = (LinearLayout) findViewById(R.id.menuLinearLayout);
		mSmsTextView = (TextView) findViewById(R.id.sendSmsTextView);
		mEmailTextView = (TextView) findViewById(R.id.sendEmailTextView);
		mCancelTextView = (TextView) findViewById(R.id.cancelTextView);
		mShadowView = findViewById(R.id.shadowView);
		mFavouritePartnersTextView = (TextView) findViewById(R.id.favouritePartnersTextView);
		mCorrespondersListView = (ListView) findViewById(R.id.activeListView);
        mAccountsListView = (DynamicListView) findViewById(R.id.accountListView);
        mExitTextView = (TextView) findViewById(R.id.exitTextView);
		
		mAccountDetailsLayout = (RelativeLayout)findViewById(R.id.accountDetailsLayout);
		mCredsDetailsLayout = (RelativeLayout)findViewById(R.id.credsDetailsLayout);
		mTopRelativeLayout = (RelativeLayout)findViewById(R.id.topRelativeLayout);
		mInitViewLayout = (RelativeLayout)findViewById(R.id.initMessageLayout);
        mTransferRelativeLayout = (RelativeLayout)findViewById(R.id.transferRelativeLayout);
		
		mCorrespondersAdapter = new CorrespondersListAdapter(this);
		mCorrespondersListView.setAdapter(mCorrespondersAdapter);
        mCorrespondersListView.setDivider(null);
        mCorrespondersListView.setDividerHeight(0);

        mAccountsAdapter = new AccountsListAdapter(this);
        mAccountsListView.setAdapter(mAccountsAdapter);
		
		mCrmToken = DataBaseManager.getInstance(this).getCrmToken();
		mBankToken = DataBaseManager.getInstance(this).getBankToken();

        if (mBankToken == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

		mDetector = new GestureDetectorCompat(this, mSimpleGestureListener);

        mTopRelativeLayout.setVisibility(View.INVISIBLE);

		mTopRelativeLayout.addOnLayoutChangeListener(new OnLayoutChangeListener() {			
			@Override
			public void onLayoutChange(View v, int left, int top, int right,
					int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				mTopViewHeight = bottom - top;
				if (isDataReceived) {
					mTopRelativeLayout.removeOnLayoutChangeListener(this);
                    mTopRelativeLayout.setVisibility(View.VISIBLE);
				}
				initViews();
			}
		});

		mAccountDetailsLayout.addOnLayoutChangeListener(new OnLayoutChangeListener() {			
			@Override
			public void onLayoutChange(View v, int left, int top, int right,
					int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				mAccountViewHeight = bottom - top;
			}
		});
		
		mHandleImageView.addOnLayoutChangeListener(new OnLayoutChangeListener() {			
			@Override
			public void onLayoutChange(View v, int left, int top, int right,
					int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				mHandleImageViewHeight = bottom - top;
				mHandleButtonRect = new Rect(left, top, right, bottom);
				if (isDataReceived) {
					mHandleImageView.removeOnLayoutChangeListener(this);
				}
			}
		});

		mTopRelativeLayout.setOnTouchListener(new OnTouchListener() {			
			@Override
			public boolean onTouch(View v, MotionEvent event) {	
				return mDetector.onTouchEvent(event);
			}
		});
		
		mChatImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(DashboardActivity.this, ChatActivity.class);
				DashboardActivity.this.startActivity(intent);
			}
		});
		
		mActionImageView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(DashboardActivity.this, PaymentMenuActivity.class);
				startActivity(intent);
			}
		});
		
		mCredsCloseImageView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				rollupCredsView();
			}
		});
		
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
	            intent.putExtra("sms_body", SharedPreferenceManager.getInstance(DashboardActivity.this).getCreds());
	            intent.putExtra("compose_mode", true);
	            startActivity(intent);		
	            mMenuLinearLayout.setVisibility(View.GONE);
				mShadowView.setVisibility(View.GONE);
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
					intent.putExtra(Intent.EXTRA_TEXT, SharedPreferenceManager.getInstance(DashboardActivity.this).getCreds());
				}
				try {
				    startActivity(Intent.createChooser(intent, SharedPreferenceManager.getInstance(DashboardActivity.this).getCredsTitle()));
				    mMenuLinearLayout.setVisibility(View.GONE);
					mShadowView.setVisibility(View.GONE);
				} catch (android.content.ActivityNotFoundException ex) {
				    Toast.makeText(DashboardActivity.this, "There are no email clients installed.", Toast.LENGTH_LONG).show();
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

        if (SharedPreferenceManager.getInstance(this).isDashboardInfoViwed()) {
            mInitViewLayout.setVisibility(View.GONE);
            mFavouritePartnersTextView.setVisibility(View.VISIBLE);
        } else {
            SharedPreferenceManager.getInstance(this).setDashboardInfoViewed();
        }
		
		mInitViewLayout.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				mInitViewLayout.setVisibility(View.GONE);
				mFavouritePartnersTextView.setVisibility(View.VISIBLE);
			}
		});


		
		mAccountsImageView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
			Intent intent = new Intent(DashboardActivity.this, TimelineActivity.class);
			DashboardActivity.this.startActivity(intent);
			}
		});

        mExitTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DashboardActivity.this, SplashActivity.class);
                DashboardActivity.this.startActivity(intent);
                finish();
            }
        });

        mAccountsListView.setOnItemSwitchedListener(new OnItemSwitchedListener() {
            @Override
            public void onItemSwitched(final int initialPosition, final int finalPosition) {
                Log.d("###", "onItemSwitched from: " + initialPosition + " to: " + finalPosition);
                ((TextView)mTransferRelativeLayout.findViewById(R.id.accountSourceTextView)).setText(mAccountsList.get(initialPosition).getInfo());
                ((TextView)mTransferRelativeLayout.findViewById(R.id.accountTargetTextView)).setText(mAccountsList.get(finalPosition).getInfo());
                mTransferRelativeLayout.setVisibility(View.VISIBLE);
                mShadowView.setVisibility(View.VISIBLE);

                mTransferRelativeLayout.findViewById(R.id.transferCloseImageView).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mTransferRelativeLayout.setVisibility(View.GONE);
                        mShadowView.setVisibility(View.GONE);
                    }
                });
                final EditText summEditText = (EditText)mTransferRelativeLayout.findViewById(R.id.summTextEdit);
                final TextView makeTransferTextView = (TextView)mTransferRelativeLayout.findViewById(R.id.makeTransferTextView);
                makeTransferTextView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (summEditText.getText().length() > 0) {
                            try {
                                Double summ = Double.parseDouble(summEditText.getText().toString());
                                summEditText.setEnabled(false);
                                makeTransferTextView.setEnabled(false);
                                AsyncJSONLoader corrLoader = new AsyncJSONLoader(DashboardActivity.this);
                                corrLoader.registryListener(new JSONResponseListener() {
                                    @Override
                                    public void handleResponse(int result, JSONObject response, String error) {
                                        if (response != null) {
                                            Log.d("###", response.toString());
                                            try {
                                                int status = response.getInt("status");
                                                if (status == 200) {
                                                    Log.d("###", "transfer completed success");
                                                } else {
                                                    Log.d("###", "transfer failed");
                                                    Toast.makeText(DashboardActivity.this, "failed to send: " + response.getString("message"), Toast.LENGTH_LONG).show();
                                                }
                                            } catch (Exception exc) {
                                                Log.e(TAG, exc.getMessage());
                                            }
                                        }
                                        mTransferRelativeLayout.setVisibility(View.GONE);
                                        mShadowView.setVisibility(View.GONE);
                                        mTransferRelativeLayout.findViewById(R.id.transferProgressBar).setVisibility(View.GONE);
                                        summEditText.setEnabled(true);
                                        makeTransferTextView.setEnabled(true);
                                        summEditText.setText("");
                                    }
                                });
                                Bundle params = new Bundle();
                                params.putString("endpoint", "/api/bank/self_payment");
                                params.putString("requestType", "POST");
                                Bundle headerParams = new Bundle();
                                headerParams.putString("Authorization", mBankToken);
                                Bundle bodyParams = new Bundle();
                                bodyParams.putString("account_number", mAccountsList.get(initialPosition).getNumber());
                                bodyParams.putString("corr_account_number", mAccountsList.get(finalPosition).getNumber());
                                bodyParams.putString("amount", summ.toString());
                                corrLoader.execute(params, headerParams, bodyParams);
                                mTransferRelativeLayout.findViewById(R.id.transferProgressBar).setVisibility(View.VISIBLE);
                            } catch (Exception exc) {
                                Toast.makeText(DashboardActivity.this, R.string.ENTER_VALID_SUMM, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            }

            @Override
            public void onItemClicked(int position) {
                Intent intent = new Intent(DashboardActivity.this, TimelineActivity.class);
                intent.putParcelableArrayListExtra("account_list", mAccountsList);
                intent.putExtra("account_id", mAccountsList.get(position).getId());
                DashboardActivity.this.startActivity(intent);
            }
        });

		AsyncJSONLoader loader = new AsyncJSONLoader(this);		
		loader.registryListener(new AccountDataHandler());
		Bundle params = new Bundle();
		Bundle headerParams = new Bundle();
		params.putString("requestType", "GET");
		headerParams.putString("Authorization", mBankToken);
		params.putString("endpoint", "/api/bank/account");										
		loader.execute(params, headerParams, null);

		AsyncJSONLoader corrLoader = new AsyncJSONLoader(DashboardActivity.this);
		corrLoader.registryListener(new CommonCorrHandler());
		Bundle corrParams = new Bundle();
		corrParams.putString("endpoint", "/api/bank/common_corr");
		corrParams.putString("requestType", "GET");
		Bundle corrHeaderParams = new Bundle();
		corrHeaderParams.putString("Authorization", mBankToken);
		corrLoader.execute(corrParams, corrHeaderParams, null);

		/* get credentials */		
		File f = new File(android.os.Environment.getExternalStorageDirectory() + "/octoberry", "credentials.pdf");
		if (f.exists() == false) {			
			AsyncFileLoader credsLoader = new AsyncFileLoader(this, f.getAbsolutePath());
			credsLoader.registryListener(new GetCredentialsHandler());
			Bundle credsParams = new Bundle();
			credsParams.putString("requestType", "GET");
			credsParams.putString("endpoint", "/api/organization.pdf");
			Bundle credsHeaderParams = new Bundle();
			credsHeaderParams.putString("Authorization", mCrmToken);
			credsHeaderParams.putString("Accept", "application/pdf");
			credsLoader.execute(credsParams, credsHeaderParams, null);
		}
	}
	
	class GetCredentialsHandler implements FileResponseListener {
		@Override
		public void handleResponse(int result, String path) {
			if (result == 200) {
				Log.d(TAG, "Credentials file downloaded: " + path);
			} else if (result == 403) {
                Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
				Toast.makeText(DashboardActivity.this, path, Toast.LENGTH_LONG).show();
				Log.e(TAG, "can't load credentials file. response code: " + result);
			}			
		}		
	}
	
	private void initViews() {				
    	mTopInitPosition = -(mTopViewHeight - mAccountViewHeight - mHandleImageViewHeight);
		RelativeLayout.LayoutParams relativeParams = (RelativeLayout.LayoutParams)mTopRelativeLayout.getLayoutParams();
		relativeParams.setMargins(0, mTopInitPosition, 0, 0);
    	mTopRelativeLayout.setLayoutParams(relativeParams);
	}
	
	private void expandCredsView() {
		int paddingInitValue = mTopRelativeLayout.getPaddingTop();
    	int paddingTargetValue = -mTopInitPosition;		
    	ValueAnimator vAnimator = ValueAnimator.ofInt(paddingInitValue, paddingTargetValue);
    	vAnimator.setDuration(500);
    	vAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int padding = ((Integer) animation.getAnimatedValue());
                float offset = ((float) padding) / (-mTopInitPosition);
                mAccountDetailsLayout.setAlpha(1.0f - offset);
                mCredsDetailsLayout.setAlpha(offset);
                mTopRelativeLayout.setPadding(0, padding, 0, 0);
            }
        });
    	vAnimator.start();
	}
	
	private void rollupCredsView() {
		int paddingInitValue = mTopRelativeLayout.getPaddingTop();		
    	int paddingTargetValue = 0;	 
    	ValueAnimator vAnimator = ValueAnimator.ofInt(paddingInitValue, paddingTargetValue);
    	vAnimator.setDuration(500);
    	vAnimator.addUpdateListener(new AnimatorUpdateListener() {				
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				int padding = ((Integer)animation.getAnimatedValue());
				float offset = ((float)padding) / (-mTopInitPosition);
				mAccountDetailsLayout.setAlpha(1.0f - offset);													
				mCredsDetailsLayout.setAlpha(offset);
				mTopRelativeLayout.setPadding(0, padding, 0, 0);
			}
		});
    	vAnimator.start();
	}
	
	private class SimpleGestureListener extends GestureDetector.SimpleOnGestureListener  {
		@Override
	    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
	    	int padding = (int)(mTopRelativeLayout.getPaddingTop() - distanceY);	    	
	    	if (padding < 0) {
	        	padding = 0;
	        } else if (padding > (-mTopInitPosition)) {
	        	padding = -mTopInitPosition;
	        }
	        float offset = ((float)padding) / (-mTopInitPosition);
			mAccountDetailsLayout.setAlpha(1.0f - offset);													
			mCredsDetailsLayout.setAlpha(offset);
			mTopRelativeLayout.setPadding(0, padding, 0, 0);
	        return true;	     
	    }
	    @Override
	    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
	    	int paddingInitValue = mTopRelativeLayout.getPaddingTop();
	    	int paddingTargetValue;
	    	long duration;
	    	if (velocityY > 0) {
	    		paddingTargetValue = -mTopInitPosition;
	    		duration = (long)(velocityY / 100);
	    	} else {
	    		paddingTargetValue = 0;
	    		duration = (long)(-velocityY / 100);
	    	}
	    	
	    	ValueAnimator vAnimator = ValueAnimator.ofInt(paddingInitValue, paddingTargetValue);
	    	vAnimator.setDuration(duration);
	    	vAnimator.addUpdateListener(new AnimatorUpdateListener() {				
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					int padding = ((Integer)animation.getAnimatedValue());
					float offset = ((float)padding) / (-mTopInitPosition);
					mAccountDetailsLayout.setAlpha(1.0f - offset);													
					mCredsDetailsLayout.setAlpha(offset);
					mTopRelativeLayout.setPadding(0, padding, 0, 0);
				}
			});
	    	vAnimator.start();
	        return true;
	    }
	    @Override
		public boolean onSingleTapUp(MotionEvent e) {
			Rect r = new Rect(mHandleButtonRect.left, mHandleButtonRect.top + mTopRelativeLayout.getPaddingTop(), 
					mHandleButtonRect.right, mHandleButtonRect.bottom +  + mTopRelativeLayout.getPaddingTop());
			if (r.contains((int)e.getX(), (int)e.getY())) {
				if (mTopRelativeLayout.getPaddingTop() == 0) {
					expandCredsView();
				} else {
					rollupCredsView();					
				}
			}
			return true;
		}
	    
	    @Override
	    public boolean onDown(MotionEvent e) {
	    	return true;
	    }
	}
	
	private String doubleCurrencyToString(Double dValue, String currency) {
		long first = dValue.longValue();
		long last = Double.valueOf(dValue * 100).longValue() - (first * 100);
		String fisrtStr = String.valueOf(first);
		int groupDigits = 0;
        StringBuilder tmpBuilder = new StringBuilder();
        for (int i = fisrtStr.length() - 1; i >= 0; i--) {
        	tmpBuilder.insert(0, fisrtStr.charAt(i));
        	++groupDigits;
            if (groupDigits == 3) {
            	tmpBuilder.insert(0, " ");
            	groupDigits = 0;
            }
        }
        fisrtStr = tmpBuilder.toString();
		
		
		String lastStr = String.valueOf(last);
		if (last < 10) {
			lastStr = "0" + String.valueOf(last);
		}		
		String result;
		if (currency.equals("RUR")) {
			result = String.format("%s.%s Р", fisrtStr, lastStr);
		} else if (currency.equals("USD")) {
			result = String.format("%s.%s $", fisrtStr, lastStr);
		} else {
			result = String.format("%s.%s %s", fisrtStr, lastStr, currency);
		}
		return result;		
	}
	
	private class CorrespondersListAdapter extends ArrayAdapter<Object> {
		private final Context context;
		
		public CorrespondersListAdapter(Context context) {
			super(context, R.layout.corresponders_list_row);
			this.context = context;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {			
			View rowView = convertView;
			if (rowView == null) {
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(R.layout.corresponders_list_row, null);				
			}
			TextView letterTextView = (TextView)rowView.findViewById(R.id.letterTextView);
			letterTextView.setText(mCorrespondersList.get(position).getLetter());
			TextView nameTextView = (TextView)rowView.findViewById(R.id.nameTextView);
			nameTextView.setText(mCorrespondersList.get(position).getCutName());
			TextView detailsTextView = (TextView)rowView.findViewById(R.id.detailsTextView);
			detailsTextView.setText(mCorrespondersList.get(position).getDetails(getResources().getString(R.string.LAST_PAYMENT)));
			final int pos = position;
			rowView.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
                    Payment payment = new Payment();
                    Correspondent corr = mCorrespondersList.get(pos);
                    payment.setNds(corr.getNds());
                    payment.setDescription(corr.getDescription());
                    payment.setCorrName(corr.getName());
                    payment.setBankCorrAccount(corr.getBankCorrAccount());
                    payment.setCorrAccountNumber(corr.getAccountNumber());
                    payment.setCorrKpp(corr.getKpp());
                    payment.setCorrBankBik(corr.getBankBik());
                    payment.setCorrInn(corr.getInn());
                    payment.setCorrBankName(corr.getBankName());
                    payment.addToPreference(DashboardActivity.this);
                    Intent intent = new Intent(DashboardActivity.this, PaymentSummActivity.class);
                    startActivity(intent);
				}
			});
			
			return rowView;
		}
		
		@Override
		public int getCount() {
			if (mCorrespondersList != null) {
				if (mCorrespondersList.size() > 0) {
					mFavouritePartnersTextView.setVisibility(View.GONE);
				} else {
					mFavouritePartnersTextView.setVisibility(View.VISIBLE);
				}
				return mCorrespondersList.size();
			}
			return 0;
		}
	}

    private class AccountsListAdapter extends ArrayAdapter<Object> {
        private final Context context;

        public AccountsListAdapter(Context context) {
            super(context, R.layout.account_creds_list_row);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.account_creds_list_row, null);
            }
            ((TextView)rowView.findViewById(R.id.accountNameTextView)).setText(mAccountsList.get(position).getName());
            ((TextView)rowView.findViewById(R.id.accountBalanceTextView)).setText(mAccountsList.get(position).getActualBalance() +
                    " " + mAccountsList.get(position).getCurrency());
            if (mAccountsList.get(position).isCard()) {
                ((ImageView)rowView.findViewById(R.id.accountTypeImageView)).setImageResource(R.drawable.icon_card_account_white);
            }

            // TODO teamp code
            ((TextView)rowView.findViewById(R.id.bankNameValueTextView)).setText(bankName);
            ((TextView)rowView.findViewById(R.id.innValueTextView)).setText(inn);
            ((TextView)rowView.findViewById(R.id.kppValueTextView)).setText(kpp);
            ((TextView)rowView.findViewById(R.id.corrNumberValueTextView)).setText(corrNumber);
            ((TextView)rowView.findViewById(R.id.bikValueTextView)).setText(bik);
            // TODO temp code ended

            (rowView.findViewById(R.id.shareCredsImageView)).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    mMenuLinearLayout.setVisibility(View.VISIBLE);
                    mShadowView.setVisibility(View.VISIBLE);
                }
            });

            final RelativeLayout credsLayout = ((RelativeLayout)rowView.findViewById(R.id.accountDetailsRelativeLayout));
            (rowView.findViewById(R.id.credsTextView)).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (credsLayout.getVisibility() == View.GONE) {
                        credsLayout.setVisibility(View.VISIBLE);
                    } else {
                        credsLayout.setVisibility(View.GONE);
                    }
                }
            });

            return rowView;
        }

        @Override
        public int getCount() {
            if (mAccountsList != null) {
                return mAccountsList.size();
            }
            return 0;
        }
    }
	
	class AccountDataHandler implements JSONResponseListener {

		@Override
		public void handleResponse(int result, JSONObject response, String error) {
			if (response != null) {
				Log.d("###", response.toString());
				try {
					int status = response.getInt("status");
					if (status == 200) {
                        mAccountsList.clear();
						JSONArray resultArray = response.getJSONArray("result");
						Double balance = 0.0;
						for (int index = 0; index < resultArray.length(); index++) {
							JSONObject resultObject = resultArray.getJSONObject(index);
							UserAccount account = new UserAccount();
                            if (!resultObject.isNull("currency")) {
                                String currency = resultObject.getString("currency");
                                account.setCurrency(currency);
                                if (!resultObject.isNull("actual_balance")) {
                                    if (currency.equals("RUR")) {
                                        balance += resultObject.getDouble("actual_balance");
                                        account.setActualBalance(resultObject.getDouble("actual_balance"));
                                    }
                                }
                            }
                            if (!resultObject.isNull("id")) {
                                account.setId(resultObject.getString("id"));
                            }
                            if (!resultObject.isNull("name")) {
                                account.setName(resultObject.getString("name"));
                            }
                            if (!resultObject.isNull("balance")) {
                                account.setBalance(resultObject.getString("balance"));
                            }
                            if (!resultObject.isNull("number")) {
                                account.setNumber(resultObject.getString("number"));
                            }
                            if (!resultObject.isNull("is_card")) {
                                account.setCard(resultObject.getBoolean("is_card"));
                            }
                            if (!resultObject.isNull("info")) {
                                account.setInfo(resultObject.getString("info"));
                            }
                            mAccountsList.add(account);
						}
						mBalance = doubleCurrencyToString(balance, "RUR");
                        mAccountsAdapter.notifyDataSetChanged();
					} else if (result == 403) {
                        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
						Toast.makeText(DashboardActivity.this, "error: " + response.getString("message") , Toast.LENGTH_LONG).show();						
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}

            AsyncJSONLoader orgLoader = new AsyncJSONLoader(DashboardActivity.this);
            orgLoader.registryListener(new OrganizationHandler());
            Bundle orgParams = new Bundle();
            orgParams.putString("endpoint", "/api/organization");
            orgParams.putString("requestType", "GET");
            Bundle orgHeaderParams = new Bundle();
            orgHeaderParams.putString("Authorization", mCrmToken);
            orgLoader.execute(orgParams, orgHeaderParams, null);
		}		
	}
	
	class OrganizationHandler implements JSONResponseListener {

		@Override
		public void handleResponse(int result, JSONObject response, String error) {
			if (response != null) {
				Log.d("###", response.toString());
				try {
					int status = response.getInt("status");
					if (status == 200) {
						JSONObject resultObject = response.getJSONObject("result");
                        if (!resultObject.isNull("cutname")) {
                            mCompanyName = resultObject.getString("cutname");
                        }
						if (!resultObject.isNull("account_creds")) {
							SharedPreferenceManager.getInstance(DashboardActivity.this).setCreds(resultObject.getString("account_creds"));
						}

                        // TODO temp code
                        if (!resultObject.isNull("inn")) {
                            inn = resultObject.getString("inn");
                        }
                        if (!resultObject.isNull("kpp")) {
                            kpp = resultObject.getString("kpp");
                        }
                        if (!resultObject.isNull("bank_name")) {
                            bankName = resultObject.getString("bank_name");
                        }
                        if (!resultObject.isNull("corr_number")) {
                            corrNumber = resultObject.getString("corr_number");
                        }
                        if (!resultObject.isNull("bank_bik")) {
                            bik = resultObject.getString("bank_bik");
                        }
                        // TODO temp code ended

					} else if (result == 403) {
                        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
						Toast.makeText(DashboardActivity.this, "error: " + response.getString("message") , Toast.LENGTH_LONG).show();						
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
			mBalanceTextView.setText(mBalance);
            mBalanceTextView.setTextSize(BALANCE_TEXT_SIZE * getResources().getDisplayMetrics().density);
			mNameTextView.setText(mCompanyName);
			mNameCredsTextView.setText(mCompanyName);
			isDataReceived = true;
		}		
	}
	
	class CommonCorrHandler implements JSONResponseListener {

		@Override
		public void handleResponse(int result, JSONObject response, String error) {
			if (response != null) {
				Log.d("###", response.toString());
				try {
					int status = response.getInt("status");
					if (status == 200) {
						if (!response.isNull("result")) {
							JSONArray correspondentsArray = response.getJSONArray("result");
							for (int index = 0; index < correspondentsArray.length(); index++) {
								JSONObject corrObject = correspondentsArray.getJSONObject(index);
								mCorrespondersList.add(Correspondent.createFromJSON(corrObject));
							}
							mCorrespondersAdapter.notifyDataSetChanged();							
						} else {
							Toast.makeText(DashboardActivity.this, "correspondents list is empty", Toast.LENGTH_LONG).show();
						}
					} else if (result == 403) {
                        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
						Toast.makeText(DashboardActivity.this, "error: " + response.getString("message") , Toast.LENGTH_LONG).show();						
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		}		
	}
}
