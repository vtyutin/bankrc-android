package com.octoberry.nonamebank.payment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.octoberry.nonamebank.LoginActivity;
import com.octoberry.nonamebank.R;
import com.octoberry.nonamebank.db.DataBaseManager;
import com.octoberry.nonamebank.net.AsyncFileLoader;
import com.octoberry.nonamebank.net.FileResponseListener;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PaymentDetailsActivity extends Activity {
    private final static String TAG = PaymentDetailsActivity.class.getName();

    ImageView mCloseImageView;
    TextView mTitleTextView;
    ImageView mShareImageView;
    ImageView mRepeatImageView;
    TextView mAmountTextView;
    TextView mReceiveDateTextView;
    TextView mPaidDateTextView;
    TextView mTargetValueTextView;
    TextView mFromValueTextView;
    TextView mBankValueTextView;
    ProgressBar mProgressBar;

    Payment mPayment;
    String mToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_details);

        mCloseImageView = (ImageView)findViewById(R.id.closeImageView);
        mTitleTextView = (TextView)findViewById(R.id.titleTextView);
        mShareImageView = (ImageView)findViewById(R.id.shareImageView);
        mRepeatImageView = (ImageView)findViewById(R.id.repeatImageView);
        mAmountTextView = (TextView)findViewById(R.id.amountTextView);
        mReceiveDateTextView = (TextView)findViewById(R.id.receiveDateTextView);
        mPaidDateTextView = (TextView)findViewById(R.id.paidDateTextView);
        mTargetValueTextView = (TextView)findViewById(R.id.targetValueTextView);
        mFromValueTextView = (TextView)findViewById(R.id.fromValueTextView);
        mBankValueTextView = (TextView)findViewById(R.id.bankValueTextView);
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);

        mPayment = Payment.createFromPreference(this);
        mToken = DataBaseManager.getInstance(getApplicationContext()).getBankToken();

        mTitleTextView.setText("#" + mPayment.getNumber());
        mAmountTextView.setText("" + mPayment.getAmount().doubleValue());
        mReceiveDateTextView.setText(mPayment.getDate());
        mPaidDateTextView.setText(mPayment.getDate());
        mTargetValueTextView.setText(mPayment.getDescription());
        mFromValueTextView.setText(mPayment.getName());
        mBankValueTextView.setText(mPayment.getBankName());

        mCloseImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mShareImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(View.VISIBLE);
                File f = new File(android.os.Environment.getExternalStorageDirectory() + "/octoberry", String.format("%s.pdf", mPayment.getId()));
                AsyncFileLoader loader = new AsyncFileLoader(PaymentDetailsActivity.this, f.getAbsolutePath());
                loader.registryListener(new PaymentPdfLoadListener(mPayment, mProgressBar));
                Bundle params = new Bundle();
                params.putString("requestType", "GET");
                params.putString("endpoint", String.format("/api/bank/payment/%s.pdf", mPayment.getId()));
                Bundle headerParams = new Bundle();
                headerParams.putString("Authorization", mToken);
                headerParams.putString("Accept", "application/pdf");
                loader.execute(params, headerParams, null);
            }
        });

        mRepeatImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PaymentDetailsActivity.this, PaymentReceiverActivity.class);
                startActivity(intent);
            }
        });
    }

    private class PaymentPdfLoadListener implements FileResponseListener {
        private Payment payment;
        private ProgressBar progressBar;
        public PaymentPdfLoadListener(Payment payment, ProgressBar progressBar) {
            this.payment = payment;
            this.progressBar = progressBar;
        }
        @Override
        public void handleResponse(int result, String pathToFile) {
            progressBar.setVisibility(View.GONE);
            if (result == 200) {
                Log.d(TAG, "Credentials file downloaded: " + pathToFile);

                // log flurry event
                Map<String, String> articleParams = new HashMap<String, String>();
                articleParams.put("path", pathToFile);
                FlurryAgent.logEvent("payment pdf downloaded", articleParams);

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_SUBJECT, String.format(getResources().getString(R.string.SHARE_PAYMENT_TITLE), payment.getName()));
                intent.putExtra(Intent.EXTRA_TEXT, String.format(getResources().getString(R.string.SHARE_PAYMENT_BODY), payment.getName(), payment.getStatus()));

                File f = new File(pathToFile);
                if (f.exists()) {
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + f.getAbsolutePath()));
                }
                try {
                    startActivity(Intent.createChooser(intent, getResources().getString(R.string.SHARE_PAYMENT_REQUEST)));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(PaymentDetailsActivity.this, "There are no email clients installed.", Toast.LENGTH_LONG).show();
                }
            } else if (result == 403) {
                Intent intent = new Intent(PaymentDetailsActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(PaymentDetailsActivity.this, "failed to load payment pdf", Toast.LENGTH_LONG).show();
                Log.e(TAG, "can't load credentials file. response code: " + result);

                // log flurry event
                Map<String, String> articleParams = new HashMap<String, String>();
                articleParams.put("result", "" + result);
                FlurryAgent.logEvent("payment pdf download failed", articleParams);
            }
        }
    }
}
