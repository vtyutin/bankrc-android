package com.octoberry.nonamebank.handler;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.octoberry.nonamebank.SetPincodeActivity;
import com.octoberry.nonamebank.AccountStatusActivity;
import com.octoberry.nonamebank.EnterPhoneActivity;
import com.octoberry.nonamebank.LoginActivity;
import com.octoberry.nonamebank.PrepareDocumentsActivity;
import com.octoberry.nonamebank.db.DataBaseManager;
import com.octoberry.nonamebank.net.AsyncJSONLoader;
import com.octoberry.nonamebank.net.JSONResponseListener;

public class AccountStatusHandler implements JSONResponseListener {
	private static final String TAG = AccountStatusHandler.class.getName();
	
	Activity mParentActivity;
	View mProgressElement;
	Context mContext;
	Class<?> mDefaultActivityClass;
	private static int documentIndex = 0;

	public AccountStatusHandler(Activity activity, Context context, View progressElement, Class<?> activityClass) {
		mProgressElement = progressElement;
		mContext = context;
		mParentActivity = activity;
		mDefaultActivityClass = activityClass;
	}
	
	class NextStepHandler implements JSONResponseListener {
		@Override
		public void handleResponse(int result, JSONObject response, String error) {
			if (response != null) {
				Log.d("###", response.toString());
				try {
					int resultCode = response.getInt("status");

					if (resultCode == 200) {
						String token = DataBaseManager.getInstance(mContext).getCrmToken();
						Log.d("###", "checkAccountStatus: token: " + token);
						AsyncJSONLoader loader = new AsyncJSONLoader(mContext);
						loader.registryListener(AccountStatusHandler.this);
						Bundle params = new Bundle();
						params.putString("requestType", "GET");
						params.putString("endpoint", "/api/user");
						Bundle headerParams = new Bundle();
						headerParams.putString("Authorization", token);
						loader.execute(params, headerParams, null);
					} else {
						Toast.makeText(mContext,
								response.getString("message"),
								Toast.LENGTH_LONG).show();
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		}
	}
	
	public void nextStep() {
		String token = DataBaseManager.getInstance(mContext).getBankToken();
		if (token != null) {
			AsyncJSONLoader loader = new AsyncJSONLoader(mContext);
			loader.registryListener(new NextStepHandler());
			Bundle params = new Bundle();
			params.putString("requestType", "POST");
			params.putString("endpoint", "/api/organization/next_step");
			Bundle headerParams = new Bundle();
			headerParams.putString("Authorization", token);
			loader.execute(params, headerParams, null);
		}
	}
	
	public void checkAccountStatus() {
		String token = DataBaseManager.getInstance(mContext).getCrmToken();
		Log.d("###", "checkAccountStatus: token: " + token);
		if (token != null) {
			AsyncJSONLoader loader = new AsyncJSONLoader(mContext);
			loader.registryListener(this);
			Bundle params = new Bundle();
			params.putString("requestType", "GET");
			params.putString("endpoint", "/api/user");
			Bundle headerParams = new Bundle();
			headerParams.putString("Authorization", token);
			loader.execute(params, headerParams, null);
		}
	}
	
	@Override
	public void handleResponse(int result, JSONObject response, String error) {
		if (mProgressElement != null) {
			if (mProgressElement instanceof ProgressBar) {
				((ProgressBar)mProgressElement).setVisibility(View.GONE);
			} else if (mProgressElement instanceof SwipeRefreshLayout) {
				((SwipeRefreshLayout)mProgressElement).setRefreshing(false);
			}
		}
		if (response != null) {
			Log.d("###", response.toString());
			try {
				int resultCode = response.getInt("status");

				if (resultCode == 200) {
					if (!response.isNull("result")) {
						JSONObject organization = response.getJSONObject("result").getJSONObject("organization");
	
						try {
							String accountDetails = organization.getString("account_creds");
							DataBaseManager.getInstance(mContext).setAccountDetails(accountDetails);
						} catch (Exception exc) {}
						
						String status = organization.getString("status");
						Log.d("###", "AccountStatusHandler: status: " + status);
						Intent intent = null;
						if (status.equals(DataBaseManager.ACCOUNT_STATUS_NEW) ||
								status.equals(DataBaseManager.ACCOUNT_STATUS_CONFIRMED)) {
							intent = new Intent(mContext, PrepareDocumentsActivity.class);
						} else if (status.equals(DataBaseManager.ACCOUNT_STATUS_DOCUMENTS) ||
								status.equals(DataBaseManager.ACCOUNT_STATUS_MEETING) ||
								status.equals(DataBaseManager.ACCOUNT_STATUS_DENIED) ||
								status.equals(DataBaseManager.ACCOUNT_STATUS_ERROR)) {
							intent = new Intent(mContext, AccountStatusActivity.class);
						} else if (status.equals(DataBaseManager.ACCOUNT_STATUS_CREATED)) {
							if (DataBaseManager.getInstance(mContext).getCrmToken() == null) {
								intent = new Intent(mContext, SetPincodeActivity.class);
							} else {
								intent = new Intent(mContext, LoginActivity.class);
							}
						} else {
							Log.e(this.getClass().getName(), "invalid account status: " + status);
							intent = new Intent(mContext, EnterPhoneActivity.class);
						} 
						
						if (!organization.isNull("cards")) {
							ArrayList<Card> cards = parseCards(organization.getJSONArray("cards"));
							intent.putParcelableArrayListExtra("cards", cards);
						}
						mParentActivity.finish();
						mParentActivity.startActivity(intent);
					} else {
						Log.i(AccountStatusHandler.class.getName(), "account is not created yet");
						if (mDefaultActivityClass != null) {
							Intent intent = new Intent(mContext, mDefaultActivityClass);
							mParentActivity.startActivity(intent);
							mParentActivity.finish();
						}
					}
				} else {
					Toast.makeText(mContext, response.getString("message"), Toast.LENGTH_LONG).show();
				}
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}		
	}	
	
	public static ArrayList<Card> parseCards(JSONArray cards) {
		ArrayList<Card> result = new ArrayList<Card>();
		for (int index = 0; index < cards.length(); index++) {
			try {
				JSONObject cardObject = cards.getJSONObject(index);
				Card card = new Card(parseType(cardObject.getString("type")));
				if (!cardObject.isNull("description")) {
					card.setDescription(cardObject.getString("description"));
				}
				if (!cardObject.isNull("title")) {
					card.setTitle(cardObject.getString("title"));
				}
				if (!cardObject.isNull("creds")) {
					card.setCreds(cardObject.getString("creds"));
				}
				if (!cardObject.isNull("action")) {
					card.setAction(cardObject.getString("action"));
				}
				if (!cardObject.isNull("address")) {
					card.setAddress(cardObject.getString("address"));
				}
				if (!cardObject.isNull("calendar_title")) {
					card.setCalendarTitle(cardObject.getString("calendar_title"));
				}
				if (!cardObject.isNull("completed")) {
					card.setCompleted(cardObject.getBoolean("completed"));
				}
				if (!cardObject.isNull("date_start")) {
					card.setStartDate(new Date(cardObject.getLong("date_start") * 1000));
				}
				if (!cardObject.isNull("date_end")) {
					card.setEndDate(new Date(cardObject.getLong("date_end") * 1000));
				}				
				if (!cardObject.isNull("documents")) {
					card.setDocuments(parseDocuments(cardObject.getJSONArray("documents")));
				}				
				result.add(card);
			} catch (JSONException e) {
				Log.e(TAG, "error reading card for index: " + index);
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public static int parseType(String type) {
		if (type.equals("text")) {
			return Card.TYPE_TEXT;
		} else if (type.equals("separator")) {
			return Card.TYPE_SEPARATOR;
		} else if (type.equals("creds")) {
			return Card.TYPE_CREDS;
		} else if (type.equals("event")) {
			return Card.TYPE_EVENT;
		} else if (type.equals("signature")) {
			return Card.TYPE_SIGNATURE;
		} else if (type.equals("checklist")) {
			return Card.TYPE_CHECK_LIST;
		} else if (type.equals("button")) {
			return Card.TYPE_BUTTON;
		} else if (type.equals("passports")) {
			return Card.TYPE_PASSPORTS;
		}
		
		Log.e(TAG, "invalid type: " + type);
		return Card.TYPE_INVALID;
	}
	
	public static Document[] parseDocuments(JSONArray docs) {
		Document[] documents = new Document[docs.length()];
		for (int i = 0; i < docs.length(); i++) {
			try {
				JSONObject obj = docs.getJSONObject(i);
				documents[i] = new Document(obj.getString("title"));
				if (!obj.isNull("uploadable")) {
					documents[i].setUploadable(obj.getBoolean("uploadable"));
				}
				if (!obj.isNull("id")) {
					documents[i].setId(obj.getString("id"));
				} else {
					documents[i].setId("" + documentIndex);
					documentIndex++;
				}
			} catch (JSONException e) {
				Log.e(TAG, "can't get array element for index: " + i);
			}
		}
		return documents;
	}
}
