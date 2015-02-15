package com.octoberry.nonamebank.payment;

import org.json.JSONArray;
import org.json.JSONObject;

import com.octoberry.nonamebank.R;
import com.octoberry.nonamebank.db.DataBaseManager;
import com.octoberry.nonamebank.net.AsyncJSONLoader;
import com.octoberry.nonamebank.net.JSONResponseListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class PaymentAccountActivity extends Activity {
	private ProgressBar mProgress;
	private ListView mAccountListView;
	private ImageView mCloseImageView;
	
	private AccountListHandler mAccountListHandler = new AccountListHandler();
	private String mToken;
	private AccountListAdapter mAccountListAdapter;
	private AccountListValue[] mAccountListValues;
	
	private Payment mPayment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_payment_account);
		
		mProgress = (ProgressBar)findViewById(R.id.progressBar);
		mAccountListView = (ListView)findViewById(R.id.accountListView);
		mCloseImageView = (ImageView)findViewById(R.id.closeImageView);
		
		mPayment = Payment.createFromPreference(this);
		
		mCloseImageView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				mPayment.setAccountNumber(null);
				mPayment.addToPreference(PaymentAccountActivity.this);
				finish();
			}
		});
		
		mToken = DataBaseManager.getInstance(this).getBankToken();
		
		mProgress.setVisibility(View.VISIBLE);
		AsyncJSONLoader paymentLoader = new AsyncJSONLoader(this);
		paymentLoader.registryListener(mAccountListHandler);
		Bundle params = new Bundle();
		params.putString("requestType", "GET");
		params.putString("endpoint", "/api/bank/account");
		Bundle headerParams = new Bundle();
		headerParams.putString("Authorization", mToken);
		paymentLoader.execute(params, headerParams, null);
	}
	
	class AccountListHandler implements JSONResponseListener {
		@Override
		public void handleResponse(int result, JSONObject response, String error) {
			mProgress.setVisibility(View.GONE);
			if (response != null) {
				Log.d("###", response.toString());
				try {
					int resultCode = response.getInt("status");

					if (resultCode == 200) {
						JSONArray accountListArray = response.getJSONArray("result");

						mAccountListValues = new AccountListValue[accountListArray.length()];
						for (int i = 0; i < accountListArray.length(); i++) {
							JSONObject item = accountListArray.getJSONObject(i);
							String info = item.getString("info");
							String currency = item.getString("currency");
							String balance = item.getString("balance");
							String number = item.getString("number");
							AccountListValue listItem = new AccountListValue(info, currency, balance, number);
							mAccountListValues[i] = listItem;
						}
						if (mAccountListAdapter == null) {
							mAccountListAdapter = new AccountListAdapter(getApplicationContext());							
						}
						mAccountListAdapter.setAccountValues(mAccountListValues);
						mAccountListView.setAdapter(mAccountListAdapter);
					} else {
						Toast.makeText(getApplicationContext(),
								response.getString("message"),
								Toast.LENGTH_LONG).show();
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}

		}
	}
	private class AccountListValue {
		private String info;
		public String getInfo() {
			return info;
		}

		public String getCurrency() {
			return currency;
		}

		public String getBalance() {
			return balance;
		}

		public String getNumber() {
			return number;
		}

		private String currency;
		private String balance;
		private String number;

		public AccountListValue(String info, String currency, String balance, String number) {
			this.info = info;
			this.currency = currency;
			this.balance = balance;
			this.number = number;
		}
	}

	private class AccountListAdapter extends ArrayAdapter<Object> {
		private final Context context;
		private AccountListValue[] accountValues;
		
		public AccountListAdapter(Context context) {
			super(context, R.layout.account_list_row);
			this.context = context;
		}
		public void setAccountValues(AccountListValue[] accountValues) {
			this.accountValues = accountValues;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View rowView;
			if (convertView == null) {
				rowView = inflater.inflate(R.layout.account_list_row, parent, false);
			} else {
				rowView = convertView;
			}
			TextView infoTextView = (TextView) rowView.findViewById(R.id.infoTextView);
			TextView balanceTextView = (TextView) rowView.findViewById(R.id.balanceTextView);
			TextView numberTextView = (TextView) rowView.findViewById(R.id.numberTextView);
			
			infoTextView.setText(accountValues[position].getInfo());
			String balance = accountValues[position].getBalance();
			balanceTextView.setText(formatBalance(balance) + " " + accountValues[position].getCurrency());
			numberTextView.setText(accountValues[position].getNumber());
            final int pos = position;
			rowView.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					TextView numberTextView = (TextView) v.findViewById(R.id.numberTextView);
					mPayment.setAccountNumber(accountValues[pos].getNumber());
					mPayment.addToPreference(PaymentAccountActivity.this);
					
					Intent intent = new Intent(PaymentAccountActivity.this, PaymentConfirmActivity.class);
					startActivity(intent);
				}
			});
			
			return rowView;
		}
		
		private String formatBalance(String balance) {
			Double inputDouble = Double.parseDouble(balance);
			return doubleCurrencyToString(inputDouble);
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
			if (accountValues != null) {
				return accountValues.length;
			}
			return 0;
		}
	}
}
