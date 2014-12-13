package com.octoberry.rcbankmobile;

import java.sql.DatabaseMetaData;

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
	private String mToken = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		
		mAlreadyClientTextView = (TextView)findViewById(R.id.alreadyClientTextView);
		mOpenAccountTextView = (TextView)findViewById(R.id.openAccountTextView);
		mLogoImageView = (ImageView)findViewById(R.id.logoImageView);
		
		mToken = DataBaseManager.getInstance(this).getActiveToken();
		
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
		
		if (DataBaseManager.getInstance(this).getCurrentToken() == null) {
			mLogoImageView.addOnLayoutChangeListener(new OnLayoutChangeListener() {			
				@Override
				public void onLayoutChange(View v, int left, int top, int right,
						int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
					mLogoHeight = v.getHeight();
					mLogoTopPosition = top;
					mLogoImageView.removeOnLayoutChangeListener(this);
					if (mTextTopPosition != -1) {
						mLogoAnimator.setFloatValues(0, -(mTextTopPosition / 2) - (mLogoHeight));
						if (mToken != null) {
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
						if (mToken != null) {
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
				Intent intent = new Intent(getBaseContext(), AccountCreateActivity.class);
				if (DataBaseManager.getInstance(SplashActivity.this).getAccountStatus() == null) {
					if (DataBaseManager.getInstance(SplashActivity.this).getOgrn() != null) {
						intent = new Intent(getBaseContext(), RegistryActivity.class);
					}
				} else {
					if (DataBaseManager.getInstance(SplashActivity.this).getAccountStatus().equals(DataBaseManager.ACCOUNT_STATUS_DOCUMENTS)) {
						intent = new Intent(getBaseContext(), AccountActivateActivity.class);
					} else if (DataBaseManager.getInstance(SplashActivity.this).getAccountStatus().equals(DataBaseManager.ACCOUNT_STATUS_PREPARED)) {
						intent = new Intent(getBaseContext(), AccountReadyActivity.class);
					}
				}
				SharedPreferenceManager.getInstance(SplashActivity.this).clearAllPreferences();
				startActivity(intent);
				finish();
			}
		});
				
		if (mToken == null) {
			AsyncJSONLoader loader = new AsyncJSONLoader(this);
			loader.registryListener(new RegistryHandler());
			Bundle params = new Bundle();
			params.putString("endpoint", "/api/user/register");
			params.putString("requestType", "POST");
			loader.execute(params, null, null);		
		}
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
						DataBaseManager.getInstance(SplashActivity.this).setActiveToken(resultObject.getString("crm_token"));
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
}
