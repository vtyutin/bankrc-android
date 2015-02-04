package com.octoberry.rcbankmobile;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.flurry.android.FlurryAgent;
import com.octoberry.rcbankmobile.chat.ChatActivity;
import com.octoberry.rcbankmobile.db.DataBaseManager;
import com.octoberry.rcbankmobile.db.SharedPreferenceManager;
import com.octoberry.rcbankmobile.net.AsyncJSONLoader;
import com.octoberry.rcbankmobile.net.JSONResponseListener;

import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;

public class SetPincodeActivity extends Activity {
	private TextView mMeetingMessageTextView;
	private TextView mMeetingDetailsTextView;
	private TextView mPin1TextView;
	private TextView mPin2TextView;
	private TextView mPin3TextView;
	private TextView mPin4TextView;
	private TextView mCreatePinTextView;
	private EditText mPinEditText;
	private LinearLayout mPinLinearLayout;
	private ProgressBar mProgressBar;
	private ImageView mChatImageView;
	
	private StatusHandler mStatusHandler = new StatusHandler();
	private PasswordHandler mPasswordHandler = new PasswordHandler();
	private static final int SECUND = 1000;
	private static final int CHECK_ACCOUNT_STATUS_PERIOD = 30 * SECUND;
	

	
	private String mPinCode = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_pincode);
		
		mMeetingMessageTextView = (TextView)findViewById(R.id.meetingMessageTextView);
		mMeetingDetailsTextView = (TextView)findViewById(R.id.meetingDetailsTextView);
		mPin1TextView = (TextView)findViewById(R.id.pin1TextView);
		mPin2TextView = (TextView)findViewById(R.id.pin2TextView);
		mPin3TextView = (TextView)findViewById(R.id.pin3TextView);
		mPin4TextView = (TextView)findViewById(R.id.pin4TextView);
		mCreatePinTextView = (TextView)findViewById(R.id.createPINTextView);
		mPinLinearLayout = (LinearLayout)findViewById(R.id.pinLinearLayout);
		mPinEditText = (EditText)findViewById(R.id.pinEditText);
		mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
		mChatImageView = (ImageView)findViewById(R.id.chatImageView);
		
		mPinEditText.addTextChangedListener(new TextWatcher() {			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				mPinCode = s.toString();
				mPin1TextView.setBackgroundResource(R.drawable.pin_input_empty);
				mPin1TextView.setText(null);
				mPin2TextView.setBackgroundResource(R.drawable.pin_input_empty);
				mPin2TextView.setText(null);
				mPin3TextView.setBackgroundResource(R.drawable.pin_input_empty);
				mPin3TextView.setText(null);
				mPin4TextView.setBackgroundResource(R.drawable.pin_input_empty);
				mPin4TextView.setText(null);
				mCreatePinTextView.setBackgroundColor(Color.parseColor("#CCCCCC"));
				mCreatePinTextView.setTextColor(Color.parseColor("#333333"));
				mCreatePinTextView.setEnabled(false);
				switch (mPinCode.length()) {
					case 1:
						mPin1TextView.setBackgroundResource(R.drawable.pin_input_empty);
						mPin1TextView.setText(mPinCode);
						break;
					case 2:
						mPin1TextView.setBackgroundResource(R.drawable.pin_input_fill);
						mPin2TextView.setText(mPinCode.substring(1));
						break;
					case 3:
						mPin1TextView.setBackgroundResource(R.drawable.pin_input_fill);
						mPin2TextView.setBackgroundResource(R.drawable.pin_input_fill);
						mPin3TextView.setBackgroundResource(R.drawable.pin_input_empty);
						mPin3TextView.setText(mPinCode.substring(2));
						break;
					case 4:
						mPin1TextView.setBackgroundResource(R.drawable.pin_input_fill);
						mPin2TextView.setBackgroundResource(R.drawable.pin_input_fill);
						mPin3TextView.setBackgroundResource(R.drawable.pin_input_fill);
						mPin4TextView.setBackgroundResource(R.drawable.pin_input_empty);
						mPin4TextView.setText(mPinCode.substring(3));
						mCreatePinTextView.setBackgroundColor(Color.BLACK);
						mCreatePinTextView.setTextColor(Color.WHITE);
						mCreatePinTextView.setEnabled(true);
						break;	
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		
		mCreatePinTextView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Log.d("###", "pin: " + mPinCode.length());
				if (mPinCode.length() == 4) {
					mPin4TextView.setBackgroundResource(R.drawable.pin_input_fill);
					mProgressBar.setVisibility(View.VISIBLE);
					
					AsyncJSONLoader loader = new AsyncJSONLoader(SetPincodeActivity.this);
					loader.registryListener(mPasswordHandler);
					Bundle params = new Bundle();
					params.putString("endpoint", "/api/bank/password/set");
					params.putString("requestType", "POST");
					Bundle headerParams = new Bundle();
					headerParams.putString("Authorization", DataBaseManager.getInstance(SetPincodeActivity.this).getCrmToken());
					Bundle bodyParams = new Bundle();
					bodyParams.putString("password", mPinCode);
					loader.execute(params, headerParams, bodyParams);
				}
			}
		});
		
		mChatImageView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SetPincodeActivity.this, ChatActivity.class);
				startActivity(intent);
			}
		});

        final SharedPreferenceManager manager = SharedPreferenceManager.getInstance(this);
        if (!manager.isDocumentsUploaded()) {
            AsyncJSONLoader loader = new AsyncJSONLoader(SetPincodeActivity.this);
            loader.registryListener(new JSONResponseListener() {
                @Override
                public void handleResponse(int result, JSONObject response, String error) {
                    if (response != null) {
                        Log.d("###", response.toString());
                        try {
                            int resultCode = response.getInt("status");
                            if (resultCode == 200) {
                                manager.setDocumentsUploaded(true);
                            } else if (resultCode == 403) {
                                Intent intent = new Intent(SetPincodeActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // log flurry event
                                Map<String, String> articleParams = new HashMap<String, String>();
                                articleParams.put("message", response.getString("message"));
                                FlurryAgent.logEvent("documents uploaded set failed", articleParams);
                                Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception exc) {

                        }
                    }
                }
            });
            Bundle params = new Bundle();
            params.putString("endpoint", "/api/user");
            params.putString("requestType", "POST");
            Bundle headerParams = new Bundle();
            headerParams.putString("Authorization", DataBaseManager.getInstance(SetPincodeActivity.this).getCrmToken());
            Bundle bodyParams = new Bundle();
            bodyParams.putBoolean("is_documents_attached", true);
            loader.execute(params, headerParams, bodyParams);
        }
	}

    @Override
    protected void onResume() {
        super.onResume();
        checkAccountStatus();
    }

    private void checkAccountStatus() {
		AsyncJSONLoader loader = new AsyncJSONLoader(this);
		loader.registryListener(mStatusHandler);
		Bundle params = new Bundle();
		params.putString("endpoint", "/api/user");
		params.putString("requestType", "GET");
		Bundle headerParams = new Bundle();
		headerParams.putString("Authorization", DataBaseManager.getInstance(this).getCrmToken());
		loader.execute(params, headerParams, null);
	}
	
	private String formatTime(Calendar calendar) {
		String result = "";
		if (calendar.get(Calendar.HOUR_OF_DAY) < 10) {
			result += "0" + calendar.get(Calendar.HOUR_OF_DAY);
		} else {
			result += calendar.get(Calendar.HOUR_OF_DAY);
		}
		result += ":";
		if (calendar.get(Calendar.MINUTE) < 10) {
			result += "0" + calendar.get(Calendar.MINUTE);
		} else {
			result += calendar.get(Calendar.MINUTE);
		}
		return result;
	}

	class StatusHandler implements JSONResponseListener {

		@Override
		public void handleResponse(int result, JSONObject response, String error) {
			boolean needToRequest = true;
			if (response != null) {
				Log.d("###", response.toString());
				try {
					int resultCode = response.getInt("status");
					
					if (resultCode == 200) {
						JSONObject resultObject = response.getJSONObject("result");
						JSONObject organization = resultObject.getJSONObject("organization");
						if (!organization.isNull("meeting")) {
							JSONObject meeting = organization.getJSONObject("meeting");
                            String startDate = "";
                            String meetingTime = "";
                            String location = "";

                            if ((!meeting.isNull("date_start")) && (!meeting.isNull("time_start"))) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTimeInMillis(meeting.getLong("time_start"));

                                startDate = meeting.getString("date_start");
                                meetingTime = formatTime(calendar);
                            }
                            if (!meeting.isNull("location")) {
                                location = meeting.getString("location");
                            }
							String message = String.format(getResources().getString(R.string.MEETING_DATA_FORMAT), startDate, meetingTime, location);
							
							mMeetingDetailsTextView.setText(Html.fromHtml(message));
							mMeetingDetailsTextView.setVisibility(View.VISIBLE);
						}
						
						if (DataBaseManager.ACCOUNT_STATUS_CREATED.equals(organization.get("status"))) {
							mMeetingDetailsTextView.setVisibility(View.GONE);
							mMeetingMessageTextView.setVisibility(View.GONE);
							mPinLinearLayout.setVisibility(View.VISIBLE);
							needToRequest = false;
							
							mPinEditText.setVisibility(View.VISIBLE);
							mPinEditText.setFocusableInTouchMode(true);
							mPinEditText.requestFocus();
							final InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
							inputMethodManager.showSoftInput(mPinEditText, InputMethodManager.SHOW_IMPLICIT);
						}
					} else {
						Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_LONG).show();
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}			
			if (needToRequest) {
				try {
					Thread.sleep(CHECK_ACCOUNT_STATUS_PERIOD);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				checkAccountStatus();
			}
		}		
	}
	
	class PasswordHandler implements JSONResponseListener {

		@Override
		public void handleResponse(int result, JSONObject response, String error) {
			mProgressBar.setVisibility(View.GONE);
			if (response != null) {
				Log.d("###", response.toString());
				try {
					int resultCode = response.getInt("status");					
					if (resultCode == 200) {
                        // log flurry event
                        Map<String, String> articleParams = new HashMap<String, String>();
                        articleParams.put("phone", DataBaseManager.getInstance(SetPincodeActivity.this).getPhoneNumber());
                        FlurryAgent.logEvent("password successfully created", articleParams);

						Intent intent = new Intent(SetPincodeActivity.this, DashboardActivity.class);
						startActivity(intent);
						finish();
					} else {
                        // log flurry event
                        Map<String, String> articleParams = new HashMap<String, String>();
                        articleParams.put("message", response.getString("message"));
                        FlurryAgent.logEvent("password set failed", articleParams);
						Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_LONG).show();
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}			
		}		
	}
}
