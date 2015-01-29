package com.octoberry.rcbankmobile.payment;

import com.octoberry.rcbankmobile.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;

public class PaymentDescriptionActivity extends Activity {
	EditText mTargetEditText;
	ImageView mCloseImageView;
	ImageView mForwardImageView;
	
	Payment mPayment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_payment_description);
		
		mTargetEditText = (EditText)findViewById(R.id.targetEditText);
		mCloseImageView = (ImageView)findViewById(R.id.closeImageView);
		mForwardImageView = (ImageView)findViewById(R.id.forwardImageView);
		
		mPayment = Payment.createFromPreference(PaymentDescriptionActivity.this);
		
		mCloseImageView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				mPayment.setDescription(null);
				mPayment.addToPreference(PaymentDescriptionActivity.this);
				finish();
			}
		});
		
		mForwardImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mPayment.setDescription(mTargetEditText.getText().toString());
				mPayment.addToPreference(PaymentDescriptionActivity.this);
				
				Intent intent = new Intent(PaymentDescriptionActivity.this, PaymentSummActivity.class);
				startActivity(intent);
			}
		});
		
		mTargetEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length() > 3) {
					mForwardImageView.setVisibility(View.VISIBLE);
				} else {
					mForwardImageView.setVisibility(View.INVISIBLE);
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}			
			@Override
			public void afterTextChanged(Editable s) {}
		});

        if (mPayment != null) {
            mTargetEditText.setText(mPayment.getDescription());
        }
	}

}
