package com.octoberry.rcbankmobile;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.octoberry.rcbankmobile.chat.ChatActivity;
import com.octoberry.rcbankmobile.db.DataBaseManager;
import com.octoberry.rcbankmobile.db.SharedPreferenceManager;
import com.octoberry.rcbankmobile.net.AsyncFileLoader;
import com.octoberry.rcbankmobile.net.AsyncJSONLoader;
import com.octoberry.rcbankmobile.net.FileResponseListener;
import com.octoberry.rcbankmobile.net.JSONResponseListener;
import com.octoberry.rcbankmobile.payment.PaymentMenuActivity;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DashboardActivity extends Activity {
	private final static String TAG = DashboardActivity.class.getName();
	
	private ImageView mAccountsImageView;
	private ImageView mChatImageView;
	private ImageView mActionImageView;
	private TextView mNameTextView;
	private TextView mBalanceTextView;
	private RelativeLayout mAccountDetailsLayout;
	private RelativeLayout mCredsDetailsLayout;
	private RelativeLayout mTopRelativeLayout;
	private ImageView mCredsCloseImageView;
	private ImageView mHandleImageView;
	private TextView mInnTextView;
	private TextView mKppTextView;
	private TextView mAccountTextView;
	private TextView mBankNameTextView;
	private TextView mCorrTextView;
	private TextView mBikTextView;
	private TextView mCredsNameTextView;
	private LinearLayout mMenuLinearLayout;
	private TextView mSmsTextView;
	private TextView mEmailTextView;
	private TextView mCancelTextView;
	private View mShadowView;
	private ImageView mSendCredsImageView;
	private RelativeLayout mInitViewLayout;
	private TextView mFavouritePartnersTextView;
	private ListView mCorrespondersListView;
	
	private int mAccountViewHeight = 0;
	private int mTopViewHeight = 0;
	private int mHandleImageViewHeight = 0;
	private int mTopInitPosition = 0;
	private Rect mHandleButtonRect;
	
	private GestureDetectorCompat mDetector;
	private SimpleGestureListener mSimpleGestureListener = new SimpleGestureListener();
	
	private ArrayList<Correspondent> mCorrespondersList = new ArrayList<Correspondent>();
	private CorrespondersListAdapter mCorrespondersAdapter;
	
	private String mToken;
	private String mBankToken;
	private String mCompanyName;
	private String mBalance;
	boolean isDataReceived = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dashboard);
		
		mAccountsImageView = (ImageView)findViewById(R.id.accountsImageView);
		mChatImageView = (ImageView)findViewById(R.id.chatImageView);
		mActionImageView = (ImageView)findViewById(R.id.actionImageView);
		mNameTextView = (TextView)findViewById(R.id.nameTextView);
		mBalanceTextView = (TextView)findViewById(R.id.balanceTextView);
		mCredsCloseImageView = (ImageView)findViewById(R.id.closeImageView);
		mHandleImageView = (ImageView)findViewById(R.id.handleImageView);
		mInnTextView = (TextView)findViewById(R.id.innValueTextView);
		mKppTextView = (TextView)findViewById(R.id.kppValueTextView);
		mAccountTextView = (TextView)findViewById(R.id.accountValueTextView);
		mBankNameTextView = (TextView)findViewById(R.id.bankNameValueTextView);
		mCorrTextView = (TextView)findViewById(R.id.corrValueTextView);
		mBikTextView = (TextView)findViewById(R.id.bikValueTextView);
		mCredsNameTextView = (TextView)findViewById(R.id.nameCredsTextView);
		mMenuLinearLayout = (LinearLayout) findViewById(R.id.menuLinearLayout);
		mSmsTextView = (TextView) findViewById(R.id.sendSmsTextView);
		mEmailTextView = (TextView) findViewById(R.id.sendEmailTextView);
		mCancelTextView = (TextView) findViewById(R.id.cancelTextView);
		mShadowView = findViewById(R.id.shadowView);
		mSendCredsImageView = (ImageView)findViewById(R.id.uploadCredsImageView);
		mFavouritePartnersTextView = (TextView) findViewById(R.id.favouritePartnersTextView);
		mCorrespondersListView = (ListView) findViewById(R.id.activeListView);
		
		mAccountDetailsLayout = (RelativeLayout)findViewById(R.id.accountDetailsLayout);
		mCredsDetailsLayout = (RelativeLayout)findViewById(R.id.credsDetailsLayout);
		mTopRelativeLayout = (RelativeLayout)findViewById(R.id.topRelativeLayout);
		mInitViewLayout = (RelativeLayout)findViewById(R.id.initMessageLayout);
		
		mCorrespondersAdapter = new CorrespondersListAdapter(this);
		mCorrespondersListView.setAdapter(mCorrespondersAdapter);
		
		mToken = DataBaseManager.getInstance(this).getCurrentToken();
		mBankToken = DataBaseManager.getInstance(this).getActiveToken();
		
		mDetector = new GestureDetectorCompat(this, mSimpleGestureListener);
		
		mTopRelativeLayout.addOnLayoutChangeListener(new OnLayoutChangeListener() {			
			@Override
			public void onLayoutChange(View v, int left, int top, int right,
					int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				mTopViewHeight = bottom - top;
				if (isDataReceived) {
					mTopRelativeLayout.removeOnLayoutChangeListener(this);					
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
				
				File f = new File(android.os.Environment.getExternalStorageDirectory(), "credentials.pdf");
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
		
		mSendCredsImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mMenuLinearLayout.setVisibility(View.VISIBLE);
				mShadowView.setVisibility(View.VISIBLE);
			}
		});
		
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
		File f = new File(android.os.Environment.getExternalStorageDirectory(), "credentials.pdf");
		if (f.exists() == false) {			
			AsyncFileLoader credsLoader = new AsyncFileLoader(this, f.getAbsolutePath());
			credsLoader.registryListener(new GetCredentialsHandler());
			Bundle credsParams = new Bundle();
			credsParams.putString("requestType", "GET");
			credsParams.putString("endpoint", "/api/organization/creds");
			Bundle credsHeaderParams = new Bundle();
			credsHeaderParams.putString("Authorization", mToken);
			credsHeaderParams.putString("Accept", "application/pdf");
			credsLoader.execute(credsParams, credsHeaderParams, null);
		}
	}
	
	class GetCredentialsHandler implements FileResponseListener {
		@Override
		public void handleResponse(int result, String path) {
			if (result == 200) {
				Log.d(TAG, "Credentials file downloaded: " + path);
				mSendCredsImageView.setVisibility(View.VISIBLE);
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
    	findViewById(R.id.rootLayout).requestLayout();
    	mTopRelativeLayout.requestLayout();
	}
	
	private void expandCredsView() {
		int paddingInitValue = mTopRelativeLayout.getPaddingTop();
    	int paddingTargetValue = -mTopInitPosition;		
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
	
	private class Correspondent {
		private String name = null;
		private String shortName = null;
		private String date = null;
		private String description = null;
		
		public String getName() {
			return name;
		}
		
		public String getLetter() {
			if ((shortName != null) && (shortName.length() > 0)) {
				return shortName.substring(0, 1);
			}
			return null;
		}
		
		public String getDetails() {
			if (date != null) {
				return date + ", " + getResources().getString(R.string.LAST_PAYMENT);
			}
			return null;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		public void setShortName(String name) {
			this.shortName = name;
		}
		public void setDate(String date) {
			this.date = date;
		}
		public void setDescription(String description) {
			this.description = description;
		}
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
			final int pos = position;
			if (rowView == null) {
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(R.layout.corresponders_list_row, null);				
			}
			TextView letterTextView = (TextView)rowView.findViewById(R.id.letterTextView);
			letterTextView.setText(mCorrespondersList.get(position).getLetter());
			TextView nameTextView = (TextView)rowView.findViewById(R.id.nameTextView);
			nameTextView.setText(mCorrespondersList.get(position).getName());
			TextView detailsTextView = (TextView)rowView.findViewById(R.id.detailsTextView);
			detailsTextView.setText(mCorrespondersList.get(position).getDetails());
			
			rowView.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {								
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
	
	class AccountDataHandler implements JSONResponseListener {

		@Override
		public void handleResponse(int result, JSONObject response, String error) {
			AsyncJSONLoader orgLoader = new AsyncJSONLoader(DashboardActivity.this);
			orgLoader.registryListener(new OrganizationHandler());
			Bundle orgParams = new Bundle();
			orgParams.putString("endpoint", "/api/organization");
			orgParams.putString("requestType", "GET");
			Bundle orgHeaderParams = new Bundle();
			orgHeaderParams.putString("Authorization", mToken);
			orgLoader.execute(orgParams, orgHeaderParams, null);
			
			if (response != null) {
				Log.d("###", response.toString());
				try {
					int status = response.getInt("status");
					if (status == 200) {
						JSONArray resultArray = response.getJSONArray("result");
						Double balance = 0.0;
						String info = "";
						for (int index = 0; index < resultArray.length(); index++) {
							JSONObject resultObject = resultArray.getJSONObject(index);
							String currency = resultObject.getString("currency");
							if (currency.equals("RUR")) {
								balance += resultObject.getDouble("balance");
								info = resultObject.getString("info");
							}
						}
						mBalance = doubleCurrencyToString(balance, "RUR");
						mCompanyName = info;						
					} else {
						Toast.makeText(DashboardActivity.this, "error: " + response.getString("message") , Toast.LENGTH_LONG).show();						
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
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
						if (!resultObject.isNull("inn")) {
							mInnTextView.setText(resultObject.getString("inn"));
						}
						if (!resultObject.isNull("kpp")) {
							mKppTextView.setText(resultObject.getString("kpp"));
						}
						if (!resultObject.isNull("bank_name")) {
							mBankNameTextView.setText(resultObject.getString("bank_name"));
						}
						if (!resultObject.isNull("account_number")) {
							mAccountTextView.setText(resultObject.getString("account_number"));
						}
						if (!resultObject.isNull("corr_number")) {
							mCorrTextView.setText(resultObject.getString("corr_number"));
						}
						if (!resultObject.isNull("bank_bik")) {
							mBikTextView.setText(resultObject.getString("bank_bik"));
						}	
						if (!resultObject.isNull("account_creds")) {
							SharedPreferenceManager.getInstance(DashboardActivity.this).setCreds(resultObject.getString("account_creds"));
						}
					} else {
						Toast.makeText(DashboardActivity.this, "error: " + response.getString("message") , Toast.LENGTH_LONG).show();						
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
			mBalanceTextView.setText(mBalance);
			mNameTextView.setText(mCompanyName);
			mCredsNameTextView.setText(mCompanyName);
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
								Correspondent corr = new Correspondent();
								if (!corrObject.isNull("corr_name")) {
									corr.setName(corrObject.getString("corr_name"));
								}
								if (!corrObject.isNull("corr_cutname")) {
									corr.setShortName(corrObject.getString("corr_cutname"));
								}
								if (!corrObject.isNull("date")) {
									corr.setDate(corrObject.getString("date"));
								}
								if (!corrObject.isNull("description")) {
									corr.setDescription(corrObject.getString("description"));
								}
								mCorrespondersList.add(corr);
							}
							mCorrespondersAdapter.notifyDataSetChanged();							
						} else {
							Toast.makeText(DashboardActivity.this, "correspondents list is empty", Toast.LENGTH_LONG).show();
						}
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
