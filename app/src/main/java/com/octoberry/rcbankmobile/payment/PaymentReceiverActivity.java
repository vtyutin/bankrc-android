package com.octoberry.rcbankmobile.payment;

import java.util.Locale;

import org.json.JSONObject;

import com.octoberry.rcbankmobile.R;
import com.octoberry.rcbankmobile.db.DataBaseManager;
import com.octoberry.rcbankmobile.db.SharedPreferenceManager;
import com.octoberry.rcbankmobile.net.AsyncJSONLoader;
import com.octoberry.rcbankmobile.net.JSONResponseListener;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PaymentReceiverActivity extends Activity implements JSONResponseListener {
	private TextView mDataTextView;
	private ImageView mCloseImageView;
	private ImageView mForwardImageView;
	private ListView mReceiverListView;
	
	private boolean isCopyDataAvailable = false;
	
	private Payment mPayment;
	private ReceiverListAdapter mAdapter;
    private String mAccountNumber;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_payment_receiver);
		
		mCloseImageView = (ImageView)findViewById(R.id.closeImageView);
		mForwardImageView = (ImageView)findViewById(R.id.forwardImageView);
		mReceiverListView = (ListView)findViewById(R.id.receiverDataListView);
		
		mPayment = Payment.createFromPreference(this);

		mAdapter = new ReceiverListAdapter(this);
		mAdapter.setPayment(mPayment);
		mReceiverListView.setAdapter(mAdapter);

        SharedPreferenceManager manager = SharedPreferenceManager.getInstance(this);
        mAccountNumber = manager.getAccountNumber();
		
		mDataTextView = (TextView)findViewById(R.id.dataTextView);
		mDataTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isCopyDataAvailable) {
					return;
				}
				ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);			
				
				mDataTextView.setBackgroundResource(R.drawable.frame_gray);
				mDataTextView.setTextColor(Color.rgb(109, 109, 114));
				mDataTextView.setText(R.string.NO_VALID_DATA);
				isCopyDataAvailable = false;
				
				mAdapter.notifyDataSetChanged();
				
				clipboard.setPrimaryClip(ClipData.newPlainText("", ""));
			}
		});
		
		mCloseImageView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		mForwardImageView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Payment.clearPreference(PaymentReceiverActivity.this);
				mPayment.addToPreference(PaymentReceiverActivity.this);
				
				Intent intent = new Intent(PaymentReceiverActivity.this, PaymentDescriptionActivity.class);
				startActivity(intent);
			}
		});
		
		checkForward();
	}
	
	private void checkForward() {
        boolean isPhysic = false;
        if ((mAccountNumber != null) && (mAccountNumber.startsWith("40817"))) {
            isPhysic = true;
        }
		if (((!isPhysic) && ((mPayment.getCorrInn() == null) || (mPayment.getCorrInn().length() == 0))) ||
				(mPayment.getCorrBik() == null) || (mPayment.getCorrBik().length() == 0) ||
				(mPayment.getCorrKpp() == null) || (mPayment.getCorrKpp().length() == 0) ||
				(mPayment.getCorrName() == null) || (mPayment.getCorrName().length() == 0) ||
				(mPayment.getCorrAccount() == null) || (mPayment.getCorrAccount().length() == 0)) {
			mForwardImageView.setVisibility(View.INVISIBLE);
			return;
		}
		mForwardImageView.setVisibility(View.VISIBLE);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d("###", "on Resume");
		
		mDataTextView.setBackgroundResource(R.drawable.frame_gray);
		mDataTextView.setTextColor(Color.rgb(109, 109, 114));
		mDataTextView.setText(R.string.NO_VALID_DATA);
		isCopyDataAvailable = false;

		ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		if ((clipboard.hasPrimaryClip()) && 
				(clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) ||
						clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML))) {				
			ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
			String pasteData = item.getText().toString().toUpperCase(Locale.getDefault());
			
			AsyncJSONLoader paymentLoader = new AsyncJSONLoader(this);
			paymentLoader.registryListener(this);
			Bundle headerParams = new Bundle();
			headerParams.putString("Authorization", DataBaseManager.getInstance(this).getActiveToken());
			Bundle params = new Bundle();
			params.putString("requestType", "POST");
			params.putString("endpoint", "/api/bank/payment/parse");
			Bundle bodyParams = new Bundle();
			bodyParams.putString("text", pasteData);
			paymentLoader.execute(params, headerParams, bodyParams);
		}
	}
	
	private class ReceiverListAdapter extends ArrayAdapter<Object> {
		private final static int RECEIVER_LIST_COUNT = 5;
		
		private final Context context;
		private Payment payment;
		
		public ReceiverListAdapter(Context context) {
			super(context, R.layout.receiver_list_row);
			this.context = context;
		}
		public void setPayment(Payment payment) {
			this.payment = payment;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View rowView;
			if (convertView == null) {
				rowView = inflater.inflate(R.layout.receiver_list_row, parent, false);
			} else {
				rowView = convertView;
			}
			TextView nameTextView = (TextView) rowView.findViewById(R.id.nameTextView);
			EditText valueEditText = (EditText) rowView.findViewById(R.id.valueEditText);
			switch (position) {
				case 0:
					nameTextView.setText(R.string.RECEIVER);
					valueEditText.setText(payment.getCorrName());
					break;
				case 1:
					nameTextView.setText(R.string.INN);
					valueEditText.setText(payment.getCorrInn());
					break;
				case 2:
					nameTextView.setText(R.string.KPP);
					valueEditText.setText(payment.getCorrKpp());
					break;
				case 3:
					nameTextView.setText(R.string.BIK);
					valueEditText.setText(payment.getCorrBik());
					break;
				case 4:
					nameTextView.setText(R.string.ACCOUNT_NUMBER);
					valueEditText.setText(payment.getCorrAccount());
					break;
				default:
					break;
			}
			final int positionNumber = position;
			valueEditText.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					switch (positionNumber) {
						case 0:
							mPayment.setCorrName(s.toString());
							break;
						case 1:
							mPayment.setCorrInn(s.toString());
							break;
						case 2:
							mPayment.setCorrKpp(s.toString());
							break;
						case 3:
							mPayment.setCorrBik(s.toString());
							break;
						case 4:
							mPayment.setCorrAccount(s.toString());
							break;							
						default:
							break;
					}
					checkForward();
				}
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
				@Override
				public void afterTextChanged(Editable s) {}
			});
			return rowView;
		}
		
		@Override
		public int getCount() {
			return RECEIVER_LIST_COUNT;
		}
	}

	@Override
	public void handleResponse(int result, JSONObject response, String error) {
		if (response != null) {
			Log.d("###", response.toString());
			try {
				int resultCode = response.getInt("status");

				if (resultCode == 200) {
					JSONObject resultObject = response.getJSONObject("result");
					Log.d("###", "payment parsed: " + resultObject);
					if (!resultObject.isNull("corr_name")) {
						mPayment.setCorrName(resultObject.getString("corr_name"));
						isCopyDataAvailable = true;
					}
					if (!resultObject.isNull("corr_inn")) {
						mPayment.setCorrInn(resultObject.getString("corr_inn"));
						isCopyDataAvailable = true;
					}
					if (!resultObject.isNull("corr_kpp")) {
						mPayment.setCorrKpp(resultObject.getString("corr_kpp"));
						isCopyDataAvailable = true;
					}
					if (!resultObject.isNull("corr_bank_bik")) {
						mPayment.setCorrBik(resultObject.getString("corr_bank_bik"));
						isCopyDataAvailable = true;
					}
					if (!resultObject.isNull("corr_account")) {
						mPayment.setCorrAccount(resultObject.getString("corr_account"));
						isCopyDataAvailable = true;
					}
					if (isCopyDataAvailable) {
						mDataTextView.setBackgroundResource(R.drawable.frame_black);
						mDataTextView.setTextColor(Color.BLACK);
						mDataTextView.setText(R.string.PASTE);
					}
				} else {
					Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_LONG).show();
				}
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
	}
}
