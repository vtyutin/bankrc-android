package com.octoberry.nonamebank.payment;

import com.octoberry.nonamebank.DashboardActivity;
import com.octoberry.nonamebank.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


public class PaymentResultActivity extends Activity {
	TextView mTitleTextView;
	ImageView mCloseImageView;
	TextView mResultTextView;
	ImageView mResultImageView;
	TextView mResultMessageTextView;
	TextView mCancelTextView;
	TextView mTellTextView;
	ProgressBar mProgress;
	ImageView mSeparatorImageView;
	
	private String mPaymentId = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_payment_result);
		
		mTitleTextView = (TextView)findViewById(R.id.titleTextView);
		mCloseImageView = (ImageView)findViewById(R.id.closeImageView);
		mResultTextView = (TextView)findViewById(R.id.resultTextView);
		mResultImageView = (ImageView)findViewById(R.id.resultImageView);
		mResultMessageTextView = (TextView)findViewById(R.id.resultMessageTextView);
		mCancelTextView = (TextView)findViewById(R.id.cancelTextView);
		mTellTextView = (TextView)findViewById(R.id.tellTextView);
		mSeparatorImageView = (ImageView)findViewById(R.id.subseparator);
		mProgress = (ProgressBar)findViewById(R.id.progressBar);
		
		mCloseImageView.setVisibility(View.INVISIBLE);
		mResultTextView.setVisibility(View.INVISIBLE);
		mResultImageView.setVisibility(View.INVISIBLE);
		mResultMessageTextView.setVisibility(View.INVISIBLE);
		mCancelTextView.setVisibility(View.INVISIBLE);
		mTellTextView.setVisibility(View.INVISIBLE);
		mSeparatorImageView.setVisibility(View.INVISIBLE);
		mProgress.setVisibility(View.GONE);
		
		mCloseImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(PaymentResultActivity.this, DashboardActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});

        if (getIntent().getBooleanExtra("success", false)) {
            mTitleTextView.setText(R.string.PAYMENT_RESULT_SUCCESS);
            mCloseImageView.setVisibility(View.VISIBLE);
            mResultImageView.setImageResource(R.drawable.final_ok);
            mResultTextView.setText(R.string.PAYMENT_SUCCESS);
            mResultImageView.setVisibility(View.VISIBLE);
            mResultTextView.setVisibility(View.VISIBLE);
            mResultMessageTextView.setVisibility(View.INVISIBLE);
            mSeparatorImageView.setVisibility(View.VISIBLE);
            mCancelTextView.setVisibility(View.VISIBLE);
            mTellTextView.setVisibility(View.VISIBLE);
        } else {
            mTitleTextView.setText(R.string.PAYMENT_RESULT_FAIL);
            mCloseImageView.setVisibility(View.VISIBLE);
            mResultImageView.setImageResource(R.drawable.final_fail);
            mResultTextView.setText(R.string.PAYMENT_FAIL);
            mResultImageView.setVisibility(View.VISIBLE);
            mResultTextView.setVisibility(View.VISIBLE);
            mResultMessageTextView.setVisibility(View.VISIBLE);
            mSeparatorImageView.setVisibility(View.INVISIBLE);
            mCancelTextView.setVisibility(View.VISIBLE);
            mTellTextView.setVisibility(View.INVISIBLE);
        }
		/*
		Payment payment = Payment.createFromPreference(this);
		String token = DataBaseManager.getInstance(this).getBankToken();
		
		AsyncJSONLoader paymentLoader = new AsyncJSONLoader(this);
		paymentLoader.registryListener(this);
		Bundle params = new Bundle();
		params.putString("requestType", "POST");
		params.putString("endpoint", "/api/bank/payment");
		Bundle headerParams = new Bundle();
		headerParams.putString("Authorization", token);
		Bundle bodyParams = new Bundle();
		bodyParams.putString("account_number", payment.getAccountNumber());
		bodyParams.putString("amount", "" + payment.getAmount());
		bodyParams.putString("corr_name", payment.getCorrName());
		bodyParams.putString("corr_account_number", payment.getCorrAccountNumber());
		bodyParams.putString("corr_inn", payment.getCorrInn());
		bodyParams.putString("corr_kpp", payment.getCorrKpp());
		bodyParams.putString("corr_bank_bik", payment.getCorrBankBik());
		bodyParams.putString("description", payment.getDescription());
		bodyParams.putString("nds_value", "" + payment.getNds());
		bodyParams.putString("nds_include", "" + (payment.getNds() > 0 ? 1 : 0));
		paymentLoader.execute(params, headerParams, bodyParams);
		*/
	}
/*
	@Override
	public void handleResponse(int result, JSONObject response, String error) {
		mProgress.setVisibility(View.GONE);
		if (response != null) {
			Log.d("###", response.toString());
			try {
				int resultCode = response.getInt("status");

				if (resultCode == 200) {
					JSONObject resultObject = response.getJSONObject("result");
					mPaymentId = resultObject.getString("id");
					Log.d("###", "payment created with id: " + mPaymentId);
					
					mTitleTextView.setText(R.string.PAYMENT_RESULT_SUCCESS);
					mCloseImageView.setVisibility(View.VISIBLE);
					mResultImageView.setImageResource(R.drawable.final_ok);
					mResultTextView.setText(R.string.PAYMENT_SUCCESS);
					mResultImageView.setVisibility(View.VISIBLE);
					mResultTextView.setVisibility(View.VISIBLE);
					mResultMessageTextView.setVisibility(View.INVISIBLE);
					mSeparatorImageView.setVisibility(View.VISIBLE);
					mCancelTextView.setVisibility(View.VISIBLE);
					mTellTextView.setVisibility(View.VISIBLE);

                    // log flurry event
                    Map<String, String> articleParams = new HashMap<String, String>();
                    articleParams.put("payment id", mPaymentId);
                    FlurryAgent.logEvent("payment successful", articleParams);

                    return;
				} else {
					Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_LONG).show();
					
					mTitleTextView.setText(R.string.PAYMENT_RESULT_FAIL);
					mCloseImageView.setVisibility(View.VISIBLE);
					mResultImageView.setImageResource(R.drawable.final_fail);
					mResultTextView.setText(R.string.PAYMENT_FAIL);
					mResultImageView.setVisibility(View.VISIBLE);
					mResultTextView.setVisibility(View.VISIBLE);
					mResultMessageTextView.setVisibility(View.VISIBLE);
					mSeparatorImageView.setVisibility(View.INVISIBLE);
					mCancelTextView.setVisibility(View.VISIBLE);
					mTellTextView.setVisibility(View.INVISIBLE);

                    // log flurry event
                    Map<String, String> articleParams = new HashMap<String, String>();
                    articleParams.put("message", response.getString("message"));
                    FlurryAgent.logEvent("payment failed", articleParams);
				}
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
        // log flurry event
        Map<String, String> articleParams = new HashMap<String, String>();
        articleParams.put("ogrn", DataBaseManager.getInstance(PaymentResultActivity.this).getOgrn());
        FlurryAgent.logEvent("payment failed", articleParams);
	}
	*/
}
