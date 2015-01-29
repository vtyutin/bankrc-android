package com.octoberry.rcbankmobile;

import java.util.regex.Pattern;

import org.json.JSONObject;

import com.octoberry.rcbankmobile.db.DataBaseManager;
import com.octoberry.rcbankmobile.net.AsyncJSONLoader;
import com.octoberry.rcbankmobile.net.JSONResponseListener;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SnilsActivity extends Activity {
	private TextView mBackTextView;
	private EditText mSnilsEditText;
	private TextView mSnilsTextView;
	private ImageView mCheckImageView;
	private LinearLayout mNextLayout;
	private ProgressBar mProgressBar;
	
	private SnilsHandler mSnilsHandler = new SnilsHandler();
	private String mToken;
	
	private SmsEventReceiver smsReceiver = new SmsEventReceiver();
    private static final int MSG_SMS_DATA = 0;
    public static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private boolean mIsActivateMode = false;
    
    static final Pattern CODE_PATTERN = Pattern.compile("([0-9]{0,3})|([0-9]{3}-)+|([0-9]{3}-[0-9]{0,3})+");
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_snils);
		
		mToken = DataBaseManager.getInstance(this).getBankToken();
		
		mBackTextView = (TextView) findViewById(R.id.backTextView);
		mBackTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = SnilsActivity.this.getIntent();
				intent.putExtra("completed", false);
				SnilsActivity.this.setResult(RESULT_OK, intent);
				finish();
			}
		});
		
		mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
		mNextLayout = (LinearLayout)findViewById(R.id.nextLayout);
		mSnilsTextView = (TextView)findViewById(R.id.snilsTextView);
		mCheckImageView = (ImageView)findViewById(R.id.checkImageView);
		mSnilsEditText = (EditText)findViewById(R.id.snilsEditText);
		mSnilsEditText.addTextChangedListener(new TextWatcher() {			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.toString().length() > 0) {
					mSnilsTextView.setVisibility(View.VISIBLE);
					if (!mIsActivateMode) {	
						if (keepNumbersOnly(s).length() >= 10) {
							mCheckImageView.setVisibility(View.VISIBLE);
							mNextLayout.setVisibility(View.VISIBLE);
						} else {
							mCheckImageView.setVisibility(View.GONE);
							mNextLayout.setVisibility(View.GONE);
						}
					} else {
						mCheckImageView.setVisibility(View.VISIBLE);
						mNextLayout.setVisibility(View.VISIBLE);
					}
				} else {
					mSnilsTextView.setVisibility(View.GONE);
					mNextLayout.setVisibility(View.GONE);
				}
			}			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}			
			@Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && !CODE_PATTERN.matcher(s).matches() && (mIsActivateMode == false)) {
                    String input = s.toString();
                    String numbersOnly = keepNumbersOnly(input);
                    String code = formatNumbersAsCode(numbersOnly);

                    mSnilsEditText.removeTextChangedListener(this);
                    mSnilsEditText.setText(code);
                    // You could also remember the previous position of the cursor
                    mSnilsEditText.setSelection(code.length());
                    mSnilsEditText.addTextChangedListener(this);
                }
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
		
		mNextLayout.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				mProgressBar.setVisibility(View.VISIBLE);
				AsyncJSONLoader loader = new AsyncJSONLoader(SnilsActivity.this);		
				loader.registryListener(mSnilsHandler);
				Bundle params = new Bundle();
				Bundle bodyParams = new Bundle();
				Bundle headerParams = new Bundle();
				params.putString("requestType", "POST");
				headerParams.putString("Authorization", mToken);
				if (!mIsActivateMode) {										
					params.putString("endpoint", "/api/signature");										
					bodyParams.putString("snils", keepNumbersOnly(mSnilsEditText.getText().toString()));		
					Log.d("###", "snils: " + bodyParams.getString("snils"));
					mNextLayout.setVisibility(View.GONE);
					mSnilsEditText.setText("");
					mCheckImageView.setVisibility(View.GONE);					
				} else {
					params.putString("endpoint", "/api/signature/confirm");										
					bodyParams.putString("code", mSnilsEditText.getText().toString());		
					mNextLayout.setVisibility(View.GONE);
				}
				loader.execute(params, headerParams, bodyParams);
			}
		});
	}
	
	private String keepNumbersOnly(CharSequence s) {
        return s.toString().replaceAll("[^0-9]", ""); // Should of course be more robust
    }
	
	@Override
	protected void onStart() {
		super.onStart();
		
		IntentFilter filter = new IntentFilter(SMS_RECEIVED_ACTION);
        registerReceiver(smsReceiver, filter);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(smsReceiver);
	}
	
	class SnilsHandler implements JSONResponseListener {

		@Override
		public void handleResponse(int result, JSONObject response, String error) {
			mProgressBar.setVisibility(View.GONE);
			if (response != null) {
				Log.d("###", response.toString());
				try {
					int status = response.getInt("status");
					if ((status == 200) && (response.getBoolean("result"))) {
						if (!mIsActivateMode) {
							mSnilsEditText.setHint(R.string.SMS_CODE);
							mSnilsTextView.setText(R.string.SMS_CODE);
							mIsActivateMode = true;
						} else {
							Intent intent = SnilsActivity.this.getIntent();
							intent.putExtra("completed", true);
							SnilsActivity.this.setResult(RESULT_OK, intent);
							finish();
						}
					} else {
						Toast.makeText(SnilsActivity.this, "error: " + response.getString("message") , Toast.LENGTH_LONG).show();
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}			
		}		
	}
	
	private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SMS_DATA:
                	String smsBody = msg.getData().getString("body");
                	String smsAddress = msg.getData().getString("address");
                    Log.d("###", "SMS Body: " + smsBody);
                    Log.d("###", "SMS Address: " + smsAddress);
                    if (smsAddress.equals("Modulbank")) {
                    	mSnilsEditText.setText(msg.getData().getString("body"));
                    	mCheckImageView.setVisibility(View.VISIBLE);
                    	mNextLayout.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        }
    };
	
	private class SmsEventReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        	if(intent.getAction().equals(SMS_RECEIVED_ACTION)) {
    			Log.d("###", "handle sms");
                Bundle bundle = intent.getExtras();
                Log.d("###", "bundle: " + bundle);
                if (bundle != null){
                    //---retrieve the SMS message received---
                    try{
                        Object[] pdus = (Object[]) bundle.get("pdus");
                        Log.d("###", "pdus size: " + pdus.length);                        
                        for(int i = 0; i < pdus.length; i++) {
                        	SmsMessage message = SmsMessage.createFromPdu((byte[])pdus[i]);                       	
                        	Message m = Message.obtain(handler, MSG_SMS_DATA, 0, 0);
            	            Bundle bundleMessage = new Bundle();
            	            bundleMessage.putString("body", message.getMessageBody().toString());
            	            bundleMessage.putString("address", message.getOriginatingAddress());
            	            m.setData(bundleMessage);
            	            handler.sendMessage(m);
                        }
                    }catch(Exception e){
                    	Log.d("###", "sms retrieving error");
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
