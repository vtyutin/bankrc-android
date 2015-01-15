package com.octoberry.rcbankmobile.payment;

import com.octoberry.rcbankmobile.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
	
	private Payment mPayment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_payment_confirm);
		
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
		mTargetIdTextView = (TextView)findViewById(R.id.targetIdTextView);
		mCloseImageView = (ImageView)findViewById(R.id.closeImageView);
		mConfirmImageView = (ImageView)findViewById(R.id.confirmImageView);
		
		mTargetTextView.setText(mPayment.getCorrName());
		SpannableString spanString = new SpannableString(mPayment.getCorrInn());
		spanString.setSpan(new UnderlineSpan(), 0, spanString.length(), 0);
		mTargetIdTextView.setText(spanString);

		mPayTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mConfirmLayout.setVisibility(View.VISIBLE);
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
				Intent intent = new Intent(PaymentConfirmActivity.this, PaymentResultActivity.class);
				startActivity(intent);
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
					valueTextView.setText(payment.getCorrBik());
					break;
				case 4:
					nameTextView.setText(R.string.ACCOUNT_NUMBER);
					valueTextView.setText(payment.getCorrAccount());
					break;
				case 5:
					nameTextView.setText(R.string.PAYMENT_SUMM);
					Double summ = Long.valueOf(payment.getAmountFirst()).doubleValue();
					summ = summ + (Long.valueOf(payment.getAmountLast()).doubleValue() / 100);
					valueTextView.setText(doubleCurrencyToString(summ));
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
					if (payment.isNdsIncluded()) {
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

}
