package com.octoberry.nonamebank.payment;

import com.flurry.android.FlurryAgent;
import com.octoberry.nonamebank.R;
import com.octoberry.nonamebank.db.DataBaseManager;
import com.octoberry.nonamebank.net.AsyncJSONLoader;
import com.octoberry.nonamebank.net.JSONResponseListener;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PaymentConfirmActivity extends Activity {
	private ListView mCheckListView;
	private TextView mSaveTextView;
	private TextView mPayTextView;
	private RelativeLayout mConfirmLayout;
	private ImageView mCloseConfirmImageView;
	private TextView mTargetTextView;
	private TextView mTargetIdTextView;
	private ImageView mCloseImageView;
	private ImageView mConfirmImageView;
    private EditText mConfirmCodeEditText;
    private ProgressBar mProgress;
	
	private Payment mPayment;

    private SmsEventReceiver smsReceiver = new SmsEventReceiver();
    private static final int MSG_SMS_DATA = 0;
    public static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_payment_confirm);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

		mPayment = Payment.createFromPreference(this);
		
		mCheckListView = (ListView)findViewById(R.id.checkListView);
		CheckListAdapter adapter = new CheckListAdapter(this);
		adapter.setPayment(mPayment);
		mCheckListView.setAdapter(adapter);
		
		mPayTextView = (TextView)findViewById(R.id.payTextView);
		mSaveTextView = (TextView)findViewById(R.id.saveTextView);
		mConfirmLayout = (RelativeLayout)findViewById(R.id.confirmLayout);
		mCloseConfirmImageView = (ImageView)findViewById(R.id.closeConfirmImageView);
		mTargetTextView = (TextView)findViewById(R.id.targetTextView);
		mTargetIdTextView = (TextView)findViewById(R.id.smsCodeTextView);
		mCloseImageView = (ImageView)findViewById(R.id.closeImageView);
		mConfirmImageView = (ImageView)findViewById(R.id.confirmImageView);
        mConfirmCodeEditText = (EditText)findViewById(R.id.smsCodeEditText);
        mProgress = (ProgressBar)findViewById(R.id.progressBar);

        mConfirmLayout.setVisibility(View.GONE);

 		mTargetTextView.setText(mPayment.getCorrName());
		SpannableString spanString = new SpannableString("");
		spanString.setSpan(new UnderlineSpan(), 0, spanString.length(), 0);
		mTargetIdTextView.setText(spanString);

        mConfirmCodeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                mTargetIdTextView.setText(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mConfirmCodeEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mConfirmCodeEditText.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                }
                return false;
            }
        });

		mPayTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                String token = DataBaseManager.getInstance(PaymentConfirmActivity.this).getBankToken();

                AsyncJSONLoader paymentLoader = new AsyncJSONLoader(PaymentConfirmActivity.this);
                paymentLoader.registryListener(new JSONResponseListener() {
                    @Override
                    public void handleResponse(int result, JSONObject response, String error) {
                        mProgress.setVisibility(View.GONE);
                        if (response != null) {
                            Log.d("###", response.toString());
                            try {
                                int resultCode = response.getInt("status");

                                if (resultCode == 200) {
                                    JSONObject resultObject = response.getJSONObject("result");
                                    mPayment.setId(Long.parseLong(resultObject.getString("id")));
                                    boolean smsRequired = resultObject.getBoolean("sms_verification_required");
                                    if (smsRequired) {
                                        mConfirmLayout.setVisibility(View.VISIBLE);
                                        return;
                                    } else {
                                        // log flurry event
                                        Map<String, String> articleParams = new HashMap<String, String>();
                                        articleParams.put("payment id", mPayment.getId().toString());
                                        FlurryAgent.logEvent("payment successful", articleParams);

                                        Intent intent = new Intent(PaymentConfirmActivity.this, PaymentResultActivity.class);
                                        intent.putExtra("success", true);
                                        startActivity(intent);
                                        return;
                                    }
                                }
                            } catch (Exception exc) {
                                exc.printStackTrace();
                            }
                        }
                        // log flurry event
                        Map<String, String> articleParams = new HashMap<String, String>();
                        articleParams.put("ogrn", DataBaseManager.getInstance(PaymentConfirmActivity.this).getOgrn());
                        FlurryAgent.logEvent("payment failed", articleParams);

                        Intent intent = new Intent(PaymentConfirmActivity.this, PaymentResultActivity.class);
                        intent.putExtra("success", false);
                        startActivity(intent);
                    }
                });
                Bundle params = new Bundle();
                params.putString("requestType", "POST");
                params.putString("endpoint", "/api/bank/payment");
                Bundle headerParams = new Bundle();
                headerParams.putString("Authorization", token);
                Bundle bodyParams = new Bundle();
                bodyParams.putString("account_number", mPayment.getAccountNumber());
                bodyParams.putString("amount", "" + mPayment.getAmount());
                bodyParams.putString("corr_name", mPayment.getCorrName());
                bodyParams.putString("corr_account_number", mPayment.getCorrAccountNumber());
                bodyParams.putString("corr_inn", mPayment.getCorrInn());
                bodyParams.putString("corr_kpp", mPayment.getCorrKpp());
                bodyParams.putString("corr_bank_bik", mPayment.getCorrBankBik());
                bodyParams.putString("description", mPayment.getDescription());
                bodyParams.putString("nds_value", "" + mPayment.getNds());
                bodyParams.putString("nds_include", "" + (mPayment.getNds() > 0 ? 1 : 0));
                paymentLoader.execute(params, headerParams, bodyParams);
                mProgress.setVisibility(View.VISIBLE);
			}
		});
		
		mCloseConfirmImageView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				mConfirmLayout.setVisibility(View.GONE);
			}
		});
		
		mCloseImageView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		mConfirmImageView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
                String token = DataBaseManager.getInstance(PaymentConfirmActivity.this).getBankToken();

                AsyncJSONLoader paymentLoader = new AsyncJSONLoader(PaymentConfirmActivity.this);
                paymentLoader.registryListener(new JSONResponseListener() {
                    @Override
                    public void handleResponse(int result, JSONObject response, String error) {
                        mProgress.setVisibility(View.GONE);
                        if (response != null) {
                            Log.d("###", response.toString());
                            try {
                                int resultCode = response.getInt("status");

                                if (resultCode == 200) {
                                    // log flurry event
                                    Map<String, String> articleParams = new HashMap<String, String>();
                                    articleParams.put("payment id", mPayment.getId().toString());
                                    FlurryAgent.logEvent("payment successful", articleParams);

                                    Intent intent = new Intent(PaymentConfirmActivity.this, PaymentResultActivity.class);
                                    intent.putExtra("success", true);
                                    startActivity(intent);
                                } else if (resultCode == 400) {
                                    Animation shake = AnimationUtils.loadAnimation(PaymentConfirmActivity.this, R.anim.shake);
                                    mTargetIdTextView.startAnimation(shake);
                                    mConfirmCodeEditText.setText("");
                                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                    v.vibrate(500);
                                } else {
                                    // log flurry event
                                    Map<String, String> articleParams = new HashMap<String, String>();
                                    articleParams.put("ogrn", DataBaseManager.getInstance(PaymentConfirmActivity.this).getOgrn());
                                    FlurryAgent.logEvent("payment failed", articleParams);
                                }
                                return;
                            } catch (Exception exc) {
                                exc.printStackTrace();
                            }
                        }
                        // log flurry event
                        Map<String, String> articleParams = new HashMap<String, String>();
                        articleParams.put("ogrn", DataBaseManager.getInstance(PaymentConfirmActivity.this).getOgrn());
                        FlurryAgent.logEvent("payment failed", articleParams);

                        Intent intent = new Intent(PaymentConfirmActivity.this, PaymentResultActivity.class);
                        intent.putExtra("success", false);
                        startActivity(intent);
                        return;
                    }
                });
                Bundle params = new Bundle();
                params.putString("requestType", "POST");
                params.putString("endpoint", String.format("/api/bank/payment/%s/sign", mPayment.getId().toString()));
                Bundle headerParams = new Bundle();
                headerParams.putString("Authorization", token);
                Bundle bodyParams = new Bundle();
                bodyParams.putString("code", mConfirmCodeEditText.getText().toString());
                paymentLoader.execute(params, headerParams, bodyParams);
                mProgress.setVisibility(View.VISIBLE);
			}
		});
	}
	
	private class CheckListAdapter extends ArrayAdapter<Object> {
		private final static int CHECK_LIST_COUNT = 9;
		
		private final Context context;
		private Payment payment = null;
		
		public CheckListAdapter(Context context) {
			super(context, R.layout.confirm_payment_row);
			this.context = context;
		}
		public void setPayment(Payment payment) {
			this.payment = payment;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View rowView;
			if (convertView == null) {
				rowView = inflater.inflate(R.layout.confirm_payment_row, parent, false);
			} else {
				rowView = convertView;
			}
			TextView nameTextView = (TextView) rowView.findViewById(R.id.nameTextView);
			TextView valueTextView = (TextView) rowView.findViewById(R.id.valueTextView);
			switch (position) {
				case 0:
					nameTextView.setText(R.string.RECEIVER);
					valueTextView.setText(payment.getCorrName());
					break;
				case 1:
					nameTextView.setText(R.string.INN);
					valueTextView.setText(payment.getCorrInn());
					break;
				case 2:
					nameTextView.setText(R.string.KPP);
					valueTextView.setText(payment.getCorrKpp());
					break;
				case 3:
					nameTextView.setText(R.string.BIK);
					valueTextView.setText(payment.getCorrBankBik());
					break;
				case 4:
					nameTextView.setText(R.string.ACCOUNT_NUMBER);
					valueTextView.setText(payment.getCorrAccountNumber());
					break;
				case 5:
					nameTextView.setText(R.string.PAYMENT_SUMM);
					valueTextView.setText(doubleCurrencyToString(payment.getAmount()));
					break;
				case 6:
					nameTextView.setText(R.string.FROM_ACCOUNT);
					valueTextView.setText(payment.getAccountNumber());
					break;
				case 7:
					nameTextView.setText(R.string.PAYMENT_TARGET_TITLE);
					valueTextView.setText(payment.getDescription());
					break;
				case 8:
					nameTextView.setText(R.string.NDS);
					if (payment.getNds() > 0) {
						valueTextView.setText("" + payment.getNds() + "%");
					} else {
						valueTextView.setText(R.string.NO_NDS);
					}
					break;
				default:
					break;
			}
			
			return rowView;
		}
		
		private String doubleCurrencyToString(Double dValue) {
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
			
			return String.format("%s.%s", fisrtStr, lastStr);		
		}
		
		@Override
		public int getCount() {
			if (payment != null) {
				return CHECK_LIST_COUNT;
			}
			return 0;
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
                        mConfirmCodeEditText.setText(msg.getData().getString("body"));
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
