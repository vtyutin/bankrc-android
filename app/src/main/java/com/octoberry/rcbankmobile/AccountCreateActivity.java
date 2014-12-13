package com.octoberry.rcbankmobile;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import com.octoberry.rcbankmobile.db.DataBaseManager;
import com.octoberry.rcbankmobile.net.AsyncJSONLoader;
import com.octoberry.rcbankmobile.net.JSONResponseListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class AccountCreateActivity extends Activity {
	private ProgressBar mProgressBar;
	private EditText mOgrnEditText;
	private TextView mCheckCompanyTextView;
	private TextView mConfirmOgrnTextView;
	private TextView mCompanyNameTextView;
	private RelativeLayout mOrganizationRelativeLayout;
	private AccountCreateHandler mAccountCreateHandler = new AccountCreateHandler();
	private Timer mTimer = null;
	private String mOgrn;
	private String mToken;
	private boolean mGetOrganizationName = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_account_create);

		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		mOgrnEditText = (EditText) findViewById(R.id.editTextOgrn);
		mCheckCompanyTextView = (TextView) findViewById(R.id.checkCompanyTextView);
		mConfirmOgrnTextView = (TextView) findViewById(R.id.confirmCompanyTextView);
		mCompanyNameTextView = (TextView) findViewById(R.id.companyNameTextView);
		mOrganizationRelativeLayout = (RelativeLayout) findViewById(R.id.ogrnDetailsLayout);
		
		mToken = DataBaseManager.getInstance(getApplicationContext()).getActiveToken();
		
		mConfirmOgrnTextView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				mOgrn = mOgrnEditText.getText().toString();
				mProgressBar.animate();
				mProgressBar.setVisibility(View.VISIBLE);
									
				Log.d("###", "Token sent: " + mToken);
	
				mGetOrganizationName = false;
				AsyncJSONLoader loader = new AsyncJSONLoader(AccountCreateActivity.this);		
				loader.registryListener(mAccountCreateHandler);
				Bundle params = new Bundle();
				params.putString("endpoint", "/api/organization/confirm");
				params.putString("requestType", "POST");
				Bundle headerParams = new Bundle();
				headerParams.putString("Authorization", mToken);
				loader.execute(params, headerParams, null);
				
				mOgrnEditText.setEnabled(false);
			}
		});
		
		mOgrnEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (mTimer != null) {
					mTimer.cancel();
					mTimer.purge();
				}
				
				mOrganizationRelativeLayout.setVisibility(View.GONE);
				mCheckCompanyTextView.setVisibility(View.GONE);
				mConfirmOgrnTextView.setVisibility(View.GONE);
				
				if (s.length() >= 13) {
					mOgrn = s.toString();
					mTimer = new Timer();
					mTimer.schedule(new TimerTask() {		
						@Override
						public void run() {
							runOnUiThread(new Runnable() {
							    public void run() {
							    	mOgrnEditText.setEnabled(false);
									mProgressBar.setVisibility(View.VISIBLE);
							    }
							});
							mGetOrganizationName = true;
							Log.d("###", "check ogrn: " + "/api/organization/check/" + mOgrn);
							AsyncJSONLoader loader = new AsyncJSONLoader(AccountCreateActivity.this);		
							loader.registryListener(mAccountCreateHandler);
							Bundle params = new Bundle();
							params.putString("endpoint", "/api/organization/check/" + mOgrn);
							params.putString("requestType", "GET");
							Bundle headerParams = new Bundle();
							headerParams.putString("Authorization", mToken);
							loader.execute(params, headerParams, null);
						}
					}, 1000);
				}
			}			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});
	}
	
	class AccountCreateHandler implements JSONResponseListener {
		@Override
		public void handleResponse(int result, JSONObject response, String error) {
			mProgressBar.setVisibility(View.GONE);
			mOgrnEditText.setEnabled(true);
			if (response != null) {
				Log.d("###", response.toString());
				try {
					int resultCode = response.getInt("status");
					if (resultCode == 200) {						
						if (mGetOrganizationName) {
							JSONObject resultObject = response.getJSONObject("result");
							String name = resultObject.getString("name");
							Log.d("###", "name: " + name);
							mCompanyNameTextView.setText(name);
							mOrganizationRelativeLayout.setVisibility(View.VISIBLE);	
							mCheckCompanyTextView.setVisibility(View.VISIBLE);
							mConfirmOgrnTextView.setVisibility(View.VISIBLE);
						} else {
							DataBaseManager.getInstance(getApplicationContext()).setOgrn(mOgrn);
							Intent intent = new Intent(getApplicationContext(), RegistryActivity.class);
							startActivity(intent);
							finish();
						}
					} else {
						Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_LONG).show();
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}			
		}		
	}
}
