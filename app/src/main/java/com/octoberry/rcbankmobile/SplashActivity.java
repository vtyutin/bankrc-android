package com.octoberry.rcbankmobile;

import org.json.JSONException;
import org.json.JSONObject;

import com.octoberry.rcbankmobile.db.DataBaseManager;
import com.octoberry.rcbankmobile.db.SharedPreferenceManager;
import com.octoberry.rcbankmobile.net.AsyncJSONLoader;
import com.octoberry.rcbankmobile.net.JSONResponseListener;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SplashActivity extends Activity {
	TextView mAlreadyClientTextView;
	TextView mOpenAccountTextView;
	ImageView mLogoImageView;
	
	float mTextTopPosition = -1;
	float mLogoTopPosition = -1;
	int mLogoHeight = -1;
	
	private final static int ANIMATION_START_DELAY = 1000;
	private final static int LOGO_ANIMATION_DURATION = 1000;
	private final static int BUTTONS_ANIMATION_DURATION = 1000;
	
	private ObjectAnimator mLogoAnimator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		
		mAlreadyClientTextView = (TextView)findViewById(R.id.alreadyClientTextView);
		mOpenAccountTextView = (TextView)findViewById(R.id.openAccountTextView);
		mLogoImageView = (ImageView)findViewById(R.id.logoImageView);
		
		mLogoAnimator = new ObjectAnimator();
		mLogoAnimator.setTarget(mLogoImageView);
		mLogoAnimator.setProperty(View.TRANSLATION_Y);		
		mLogoAnimator.setDuration(LOGO_ANIMATION_DURATION);
		mLogoAnimator.setStartDelay(ANIMATION_START_DELAY);
		mLogoAnimator.addListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {}				
			@Override
			public void onAnimationRepeat(Animator animation) {}				
			@Override
			public void onAnimationEnd(Animator animation) {
				ObjectAnimator button1Animator = ObjectAnimator.ofFloat(mOpenAccountTextView, "alpha", 0.0f, 1.0f);
				ObjectAnimator button2Animator = ObjectAnimator.ofFloat(mAlreadyClientTextView, "alpha", 0.0f, 1.0f);
				button1Animator.setDuration(BUTTONS_ANIMATION_DURATION);
				button2Animator.setDuration(BUTTONS_ANIMATION_DURATION);
				button1Animator.start();
				button2Animator.start();
			}
			@Override
			public void onAnimationCancel(Animator animation) {}
		});
		
		if (DataBaseManager.getInstance(this).getBankToken() == null) {
			mLogoImageView.addOnLayoutChangeListener(new OnLayoutChangeListener() {			
				@Override
				public void onLayoutChange(View v, int left, int top, int right,
						int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
					mLogoHeight = v.getHeight();
					mLogoTopPosition = top;
					mLogoImageView.removeOnLayoutChangeListener(this);
					if (mTextTopPosition != -1) {
						mLogoAnimator.setFloatValues(0, -(mTextTopPosition / 2) - (mLogoHeight));
						if (DataBaseManager.getInstance(SplashActivity.this).getBankToken() != null) {
							mLogoAnimator.start();
						}
					}
				}
			});
			
			mOpenAccountTextView.addOnLayoutChangeListener(new OnLayoutChangeListener() {			
				@Override
				public void onLayoutChange(View v, int left, int top, int right,
						int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
					mTextTopPosition = top;
					mOpenAccountTextView.removeOnLayoutChangeListener(this);
					if (mLogoTopPosition != -1) {
						mLogoAnimator.setFloatValues(0, -(mTextTopPosition / 2) - (mLogoHeight));
						if (DataBaseManager.getInstance(SplashActivity.this).getBankToken() != null) {
							mLogoAnimator.start();
						}
					}
				}
			});
		} else {
			Intent intent = new Intent(getBaseContext(), LoginActivity.class);
			startActivity(intent);
			finish();
		}
		
		mAlreadyClientTextView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				//Intent intent = new Intent(getBaseContext(), DashboardActivity.class);
				Intent intent = new Intent(getBaseContext(), LoginActivity.class);
				startActivity(intent);
				finish();
			}
		});
		
		mOpenAccountTextView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getBaseContext(), CheckOgrnActivity.class);
                Log.d("###", "stored account status: " + DataBaseManager.getInstance(SplashActivity.this).getAccountStatus());
				if (DataBaseManager.getInstance(SplashActivity.this).getAccountStatus() == null) {
					if (DataBaseManager.getInstance(SplashActivity.this).getOgrn() != null) {
						intent = new Intent(getBaseContext(), EnterPhoneActivity.class);
					}
				} else if (SharedPreferenceManager.getInstance(SplashActivity.this).isDocumentsUploaded()) {
                    intent = new Intent(getBaseContext(), SetPincodeActivity.class);
                } else {
					if (DataBaseManager.getInstance(SplashActivity.this).getAccountStatus().equals(DataBaseManager.ACCOUNT_STATUS_DOCUMENTS)) {
						intent = new Intent(getBaseContext(), WaitDocumentsActivity.class);
					} else if (DataBaseManager.getInstance(SplashActivity.this).getAccountStatus().equals(DataBaseManager.ACCOUNT_STATUS_PREPARED)) {
						intent = new Intent(getBaseContext(), SetPincodeActivity.class);
					} else if (DataBaseManager.getInstance(SplashActivity.this).getAccountStatus().equals(DataBaseManager.ACCOUNT_STATUS_CONFIRMED)) {
                        intent = new Intent(getBaseContext(), WaitDocumentsActivity.class);
                        //intent = new Intent(getBaseContext(), SetPincodeActivity.class);
                    } else if (DataBaseManager.getInstance(SplashActivity.this).getAccountStatus().equals(DataBaseManager.ACCOUNT_STATUS_MEETING)) {
                        intent = new Intent(getBaseContext(), PrepareDocumentsActivity.class);
                    } else if (DataBaseManager.getInstance(SplashActivity.this).getAccountStatus().equals(DataBaseManager.ACCOUNT_STATUS_CREATED)) {
                        intent = new Intent(getBaseContext(), SetPincodeActivity.class);
                    }
				}
				SharedPreferenceManager.getInstance(SplashActivity.this).clearAllPreferences();
				startActivity(intent);
				finish();
			}
		});
				
		if (DataBaseManager.getInstance(this).getCrmToken() == null) {
			AsyncJSONLoader loader = new AsyncJSONLoader(this);
			loader.registryListener(new RegistryHandler());
			Bundle params = new Bundle();
			params.putString("endpoint", "/api/user/register");
			params.putString("requestType", "POST");
			loader.execute(params, null, null);		
		} else {
            if (DataBaseManager.getInstance(this).getBankToken() == null) {
                checkAccountStatus();
            }
        }
	}

    private void checkAccountStatus() {
        AsyncJSONLoader loader = new AsyncJSONLoader(this);
        loader.registryListener(new AccountStatusHandler());
        Bundle params = new Bundle();
        params.putString("endpoint", "/api/user");
        params.putString("requestType", "GET");
        Bundle headerParams = new Bundle();
        headerParams.putString("Authorization", DataBaseManager.getInstance(this).getCrmToken());
        loader.execute(params, headerParams, null);
    }
	
	class RegistryHandler implements JSONResponseListener {
		@Override
		public void handleResponse(int result, JSONObject response, String error) {
			if (response != null) {
				Log.d("###", response.toString());
				try {
					int resultCode = response.getInt("status");					
					if (resultCode == 200) {
						JSONObject resultObject = response.getJSONObject("result");
						DataBaseManager.getInstance(SplashActivity.this).setCrmToken(resultObject.getString("crm_token"));
						mLogoAnimator.start();
					}
				} catch (Exception exc) {
					exc.printStackTrace();
					if (!response.isNull("message")) {
						try {
							Toast.makeText(SplashActivity.this, response.getString("message"), Toast.LENGTH_LONG).show();
						} catch (JSONException e) {}
					} else {
						Toast.makeText(SplashActivity.this, "registry failed", Toast.LENGTH_LONG).show();
					}
				}
			}
		}
	}

    class AccountStatusHandler implements JSONResponseListener {
        @Override
        public void handleResponse(int result, JSONObject response, String error) {
            if (response != null) {
                Log.d("###", response.toString());
                try {
                    int resultCode = response.getInt("status");
                    if (resultCode == 200) {
                        JSONObject resultObject = response.getJSONObject("result");
                        if (!resultObject.isNull("organization")) {
                            JSONObject organization = resultObject.getJSONObject("organization");
                            Log.d("###", "status: " + organization.getString("status"));
                            DataBaseManager.getInstance(SplashActivity.this).setAccountStatus(organization.getString("status"));
                        }
                    }
                } catch (Exception exc) {
                    exc.printStackTrace();
                    if (!response.isNull("message")) {
                        try {
                            Toast.makeText(SplashActivity.this, response.getString("message"), Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {}
                    } else {
                        Toast.makeText(SplashActivity.this, "getting status failed", Toast.LENGTH_LONG).show();
                    }
                }
            }
            mLogoAnimator.start();
        }
    }
}
