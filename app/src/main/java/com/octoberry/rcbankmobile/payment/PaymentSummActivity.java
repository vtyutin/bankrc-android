package com.octoberry.rcbankmobile.payment;

import java.util.regex.Pattern;

import com.octoberry.rcbankmobile.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class PaymentSummActivity extends Activity {
	ImageView mCheckImageView;
	TextView mNoNdsTextView;
	TextView mTenTextView;
	TextView mEighteenTextView;
	EditText mSummEditText;
	TextView mCommisionTextView;
	TextView mTotalTextView;
	ImageView mCloseImageView;
	ImageView mForwardImageView;
	
	private boolean mIsNdsIncluded = true;
	private int mNdsSelection = 1;
	private Double inputDouble = 0.0;
	private Double ndsDouble = 0.0;
	private Double totalDouble = 0.0;
	
	private static final Double NDS_10 = 0.1;
	private static final Double NDS_18 = 0.18;
	
	private static final int NDS_10_P = 10;
	private static final int NDS_18_P = 18;
	
	Payment mPayment;
	
	static final Pattern CODE_PATTERN = Pattern.compile("([0-9]{0,3})|([0-9]{3}-)+|([0-9]{3}-[0-9]{0,3})+");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_payment_summ);
		
		mNoNdsTextView = (TextView)findViewById(R.id.noNdsTextView);
		mTenTextView = (TextView)findViewById(R.id.tenPercentageTextView);
		mEighteenTextView = (TextView)findViewById(R.id.eighteenPercentageTextView);
		mCommisionTextView = (TextView)findViewById(R.id.commisionTextView);
		mTotalTextView = (TextView)findViewById(R.id.totalTextView);
		mCloseImageView = (ImageView)findViewById(R.id.closeImageView);
		mForwardImageView = (ImageView)findViewById(R.id.forwardImageView);
		mSummEditText = (EditText)findViewById(R.id.summEditText);
		
		mCheckImageView = (ImageView)findViewById(R.id.ndsCheckImageView);
		mCheckImageView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				mIsNdsIncluded = !mIsNdsIncluded;
				if (mIsNdsIncluded) {
					mCheckImageView.setImageResource(R.drawable.checkbox_white_checked);
					setNdsSelection(mNdsSelection);
				} else {
					mCheckImageView.setImageResource(R.drawable.checkbox_white_empty);
					clearNdsSelection();
				}
				calculateValues(mSummEditText.getText());
			}
		});
		mNoNdsTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mIsNdsIncluded) {
					setNdsSelection(0);
				}
			}
		});
		mTenTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mIsNdsIncluded) {
					setNdsSelection(1);
				}
			}
		});
		mEighteenTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mIsNdsIncluded) {
					setNdsSelection(2);
				}
			}
		});
		mSummEditText.addTextChangedListener(new TextWatcher() {			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.toString().length() > 0) {
					
					if(!s.toString().matches("^\\-(\\d{1,3}(\\,\\d{3})*|(\\d+))(\\.\\d{2})?-"))
	                {
						mForwardImageView.setVisibility(View.VISIBLE);
						
                        mSummEditText.removeTextChangedListener(this);
                        calculateValues(s);
                        
                        mSummEditText.setSelection(mSummEditText.getText().length() - 2);
                        mSummEditText.addTextChangedListener(this);	                        
	                }
				} else {
					mForwardImageView.setVisibility(View.GONE);
				}
			}			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}			
			@Override
            public void afterTextChanged(Editable s) {}			
		});
		
		mPayment = Payment.createFromPreference(PaymentSummActivity.this);
		if ((mPayment.getAmountFirst() > 0) || (mPayment.getAmountLast() > 0)) {
			mSummEditText.setText("" + mPayment.getAmountFirst() + "." + mPayment.getAmountLast());
		}
		
		mCloseImageView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				mPayment.setAmountFirst(0);
				mPayment.setAmountLast(0);
				mPayment.setNds(0);
				mPayment.setNdsIncluded(false);
				mPayment.addToPreference(PaymentSummActivity.this);
				finish();
			}
		});
		
		mForwardImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				long first = totalDouble.longValue();
				long last = Double.valueOf(totalDouble * 100).longValue() - (first * 100);
				mPayment.setAmountFirst(first);
				mPayment.setAmountLast(last);
				if (mNdsSelection == 1) {
					mPayment.setNds(NDS_10_P);
				} else if (mNdsSelection == 2) {
					mPayment.setNds(NDS_18_P);
				} else {
					mPayment.setNds(0);
				}
				mPayment.setNdsIncluded(mIsNdsIncluded);
				mPayment.addToPreference(PaymentSummActivity.this);
				
				Intent intent = new Intent(PaymentSummActivity.this, PaymentAccountActivity.class);
				startActivity(intent);
			}
		});
	}
	
	private void calculateValues(CharSequence s) {
		String userInput = "" + s.toString().replaceAll("[^\\d]", "");
		if (userInput.length() == 0) {
			return;
		}
		inputDouble = Double.parseDouble(userInput) / 100;
		if (mIsNdsIncluded) {
			switch (mNdsSelection) {
				case 0:
					ndsDouble = 0.0;
					totalDouble = inputDouble;
					break;
				case 1:				
					ndsDouble = inputDouble * NDS_10;
					totalDouble = inputDouble + ndsDouble;
					break;
				case 2:
					ndsDouble = inputDouble * NDS_18;
					totalDouble = inputDouble + ndsDouble;
					break;	
				default:
					ndsDouble = 0.0;
					totalDouble = inputDouble;
					break;
			}
		} else {
			ndsDouble = 0.0;
			totalDouble = inputDouble;
		}
		mSummEditText.setText(doubleCurrencyToString(inputDouble));
		mCommisionTextView.setText(doubleCurrencyToString(ndsDouble));
		mTotalTextView.setText(doubleCurrencyToString(totalDouble));
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
		
		return String.format("%s.%s �", fisrtStr, lastStr);		
	}
	
	private void clearNdsSelection() {
		mNoNdsTextView.setBackgroundResource(R.drawable.frame_white);
		mNoNdsTextView.setTextColor(Color.WHITE);
		mTenTextView.setBackgroundResource(R.drawable.frame_white);
		mTenTextView.setTextColor(Color.WHITE);
		mEighteenTextView.setBackgroundResource(R.drawable.frame_white);
		mEighteenTextView.setTextColor(Color.WHITE);
	}
	
	private void setNdsSelection(int selection) {
		mNdsSelection = selection;
		switch (selection) {
		case 0:
			mNoNdsTextView.setBackgroundColor(Color.WHITE);
			mTenTextView.setBackgroundResource(R.drawable.frame_white);
			mEighteenTextView.setBackgroundResource(R.drawable.frame_white);
			mNoNdsTextView.setTextColor(Color.BLACK);
			mTenTextView.setTextColor(Color.WHITE);
			mEighteenTextView.setTextColor(Color.WHITE);
			break;
		case 1:
			mNoNdsTextView.setBackgroundResource(R.drawable.frame_white);
			mTenTextView.setBackgroundColor(Color.WHITE);
			mEighteenTextView.setBackgroundResource(R.drawable.frame_white);
			mNoNdsTextView.setTextColor(Color.WHITE);
			mTenTextView.setTextColor(Color.BLACK);
			mEighteenTextView.setTextColor(Color.WHITE);
			break;
		case 2:
			mNoNdsTextView.setBackgroundResource(R.drawable.frame_white);
			mTenTextView.setBackgroundResource(R.drawable.frame_white);
			mEighteenTextView.setBackgroundColor(Color.WHITE);
			mNoNdsTextView.setTextColor(Color.WHITE);
			mTenTextView.setTextColor(Color.WHITE);
			mEighteenTextView.setTextColor(Color.BLACK);
			break;
		default:
			break;
		}
		calculateValues(mSummEditText.getText());
	}

}
