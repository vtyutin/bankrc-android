package com.octoberry.rcbankmobile;

import org.json.JSONObject;

import com.octoberry.rcbankmobile.db.DataBaseManager;
import com.octoberry.rcbankmobile.net.AsyncJSONLoader;
import com.octoberry.rcbankmobile.net.JSONResponseListener;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {
	private EditText mPinEditText;
	private EditText mPhoneEditText;
	private ImageView mPhoneSeparatorImageView;
	private TextView mOpenAccountTextView;
	private TextView mEnterTextView;
	private ProgressBar mProgressBar;
	
	private String mPhoneNumber;
	private String mPassword;
	private String mToken;
	
	private LoginHandler mLoginHandler = new LoginHandler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		mPinEditText = (EditText)findViewById(R.id.pinEditText);
		mPhoneEditText = (EditText)findViewById(R.id.phoneEditText);
		mProgressBar = (ProgressBar)findViewById(R.id.progressBar);		
		mOpenAccountTextView = (TextView)findViewById(R.id.openAccountTextView);
		mEnterTextView = (TextView)findViewById(R.id.enterTextView);
		mPhoneSeparatorImageView = (ImageView)findViewById(R.id.phoneSeparator);
		
		mPhoneNumber = DataBaseManager.getInstance(getApplicationContext()).getPhoneNumber();
		mToken = DataBaseManager.getInstance(getApplicationContext()).getActiveToken();
		Log.d("###", "mPhoneNumber: " + mPhoneNumber);
		if (mPhoneNumber == null) {
			mPhoneEditText.setVisibility(View.VISIBLE);
			mPhoneSeparatorImageView.setVisibility(View.VISIBLE);
		} else {
			mPhoneEditText.setVisibility(View.GONE);
			mPhoneSeparatorImageView.setVisibility(View.GONE);
		}
		
		mOpenAccountTextView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getBaseContext(), AccountCreateActivity.class);
				startActivity(intent);
				finish();
			}
		});
		
		mPhoneEditText.addTextChangedListener(new TextWatcher() {			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				mPhoneNumber = mPhoneEditText.getText().toString().replaceAll("[^0-9]", "");
				String numbersOnly = mPhoneNumber;
                numbersOnly = numbersOnly.replaceAll("[^0-9]", "");
                String phone = formatNumbersAsPhone(numbersOnly);

                mPhoneEditText.removeTextChangedListener(this);
                mPhoneEditText.setText(phone);
                // You could also remember the previous position of the cursor
                mPhoneEditText.setSelection(phone.length());
                mPhoneEditText.addTextChangedListener(this);
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {}
			
			@Override
			public void afterTextChanged(Editable arg0) {}
			
			private String formatNumbersAsPhone(String s) {
				Log.d("###", "formatNumbersAsPhone: before: " + s);
				String formatedString = s;
                if ((s.length() > 8) && (s.length() <= 10)) {
                	formatedString = String.format("%s %s-%s-%s", s.substring(0, 3), s.substring(3, 6),
                			s.substring(6, 8), s.substring(8, s.length()));
                } else if (s.length() == 8) {
                	formatedString = String.format("%s %s-%s", s.substring(0, 3), s.substring(3, 6),
                			s.substring(6, 8));
                } else if ((s.length() > 5) && (s.length() < 8)) {
                	formatedString = String.format("%s-%s-%s", s.substring(0, 3), s.substring(3, 5),
                			s.substring(5, s.length()));
                }				
                Log.d("###", "formatNumbersAsPhone: " + formatedString);
                return formatedString;                
            }
		});
		
		mPinEditText.addTextChangedListener(new TextWatcher() {			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				mPassword = mPinEditText.getText().toString();
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			
			@Override
			public void afterTextChanged(Editable arg0) {}
		});
		
		mEnterTextView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				if ((mPassword == null) || (mPassword.length() == 0)) {
					Toast.makeText(getApplicationContext(), R.string.ENTER_PASSWORD, Toast.LENGTH_LONG).show();
					return;
				}	
				if ((mPhoneNumber == null)) {
					Toast.makeText(getApplicationContext(), R.string.ENTER_PHONE, Toast.LENGTH_LONG).show();
					return;
				}	
				
				mProgressBar.setVisibility(View.VISIBLE);
				
				AsyncJSONLoader loader = new AsyncJSONLoader(LoginActivity.this);
				loader.registryListener(mLoginHandler);
				Bundle params = new Bundle();
				params.putString("requestType", "POST");
				Bundle bodyParams = new Bundle();
				Bundle headerParams = new Bundle();
				headerParams.putString("Authorization", mToken);
				headerParams.putString("Content-Type", "application/json");
				params.putString("endpoint", "/api/user/login");
				bodyParams.putString("phone_number", mPhoneNumber);	
				bodyParams.putString("password", mPassword);
				loader.execute(params, headerParams, bodyParams);
			}
		});
	}

	class LoginHandler implements JSONResponseListener {

		@Override
		public void handleResponse(int result, JSONObject response, String error) {
			mProgressBar.setVisibility(View.GONE);
			if (response != null) {
				Log.d("###", "login response: " + response.toString());		
				try {
					int resultCode = response.getInt("status");
					if (resultCode == 200) {								
						JSONObject resultObject = response.getJSONObject("result");
						if (!resultObject.isNull("bank_token")) {
							DataBaseManager.getInstance(getApplicationContext()).setActiveToken(resultObject.getString("bank_token"));
							DataBaseManager.getInstance(getApplicationContext()).setCurrentToken(resultObject.getString("crm_token"));
							DataBaseManager.getInstance(getApplicationContext()).setPhoneNumber(mPhoneNumber);
							Intent intent = new Intent(getBaseContext(), DashboardActivity.class);
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
