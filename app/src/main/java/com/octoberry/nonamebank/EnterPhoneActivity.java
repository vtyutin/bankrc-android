package com.octoberry.nonamebank;

import org.json.JSONObject;

import com.flurry.android.FlurryAgent;
import com.octoberry.nonamebank.chat.ChatService;
import com.octoberry.nonamebank.db.DataBaseManager;
import com.octoberry.nonamebank.net.AsyncJSONLoader;
import com.octoberry.nonamebank.net.JSONResponseListener;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class EnterPhoneActivity extends Activity {
	private EditText mPhoneEditText;
	private TextView mOpenAccountTextView;
	private ProgressBar progressBar;
	private RegistryHandler registryHandler = new RegistryHandler();
	private String mPhoneNumber = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registry);

		mPhoneEditText = (EditText) findViewById(R.id.phoneNumberEditText);
		mPhoneEditText.requestFocus();
		
		mOpenAccountTextView = (TextView) findViewById(R.id.openAccountTextView);
		
		TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		mPhoneNumber = manager.getLine1Number();
		Log.d("###", "phone number: " + mPhoneNumber);
		if ((mPhoneNumber == null) || (mPhoneNumber.length() < 10)) {
			mPhoneEditText.setVisibility(View.VISIBLE);
		} else {
			DataBaseManager.getInstance(getApplicationContext()).setPhoneNumber(mPhoneNumber);
		}
		progressBar = (ProgressBar) findViewById(R.id.progressBar);

		mPhoneEditText.addTextChangedListener(new PhoneNumberFormattingTextWatcher () {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String notFormatedPhone = "";
				for (int i = 0; i < s.length(); i++) {
					char c = s.charAt(i);
					if (Character.isDigit(c)) {
						notFormatedPhone += c;
					}
				}
				mPhoneNumber = notFormatedPhone;
				if ((mPhoneNumber.length() == 10)) {
					mOpenAccountTextView.setVisibility(View.VISIBLE);
				} else {
					mOpenAccountTextView.setVisibility(View.GONE);
				}
			}
		});

		mOpenAccountTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if ((mPhoneNumber == null) || (mPhoneNumber.length() < 10)) {
					Toast.makeText(getApplicationContext(),
							R.string.ENTER_PHONE, Toast.LENGTH_LONG).show();
				} else {					
					progressBar.animate();
					progressBar.setVisibility(View.VISIBLE);

					AsyncJSONLoader loader = new AsyncJSONLoader(
							EnterPhoneActivity.this);
					loader.registryListener(registryHandler);
					Bundle params = new Bundle();
					params.putString("endpoint", "/api/user");
					params.putString("requestType", "POST");
					Bundle headerParams = new Bundle();
					headerParams.putString("Authorization", DataBaseManager.getInstance(EnterPhoneActivity.this).getCrmToken());
					Bundle bodyParams = new Bundle();
					bodyParams.putString("phone_number", mPhoneNumber);
					loader.execute(params, headerParams, bodyParams);
				}
			}
		});
	}

	class RegistryHandler implements JSONResponseListener {

		@Override
		public void handleResponse(int result, JSONObject response, String error) {
			if (response != null) {
				Log.d("###", response.toString());
				try {
					int resultCode = response.getInt("status");
					if (resultCode == 200) {
						DataBaseManager.getInstance(getApplicationContext()).setPhoneNumber(mPhoneNumber);
											
						startService(new Intent(EnterPhoneActivity.this, ChatService.class));

                        // log flurry event
                        Map<String, String> articleParams = new HashMap<String, String>();
                        articleParams.put("phone", mPhoneNumber);
                        FlurryAgent.logEvent("user registered", articleParams);

						Intent intent = new Intent(getBaseContext(), WaitDocumentsActivity.class);
						startActivity(intent);
						finish();				
					} else {
                        // log flurry event
                        Map<String, String> articleParams = new HashMap<String, String>();
                        articleParams.put("message", response.getString("message"));
                        FlurryAgent.logEvent("user registration failed", articleParams);
                    }
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		}
	}
}
