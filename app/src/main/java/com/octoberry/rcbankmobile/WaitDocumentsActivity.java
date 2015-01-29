package com.octoberry.rcbankmobile;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.octoberry.rcbankmobile.chat.ChatActivity;
import com.octoberry.rcbankmobile.db.DataBaseManager;
import com.octoberry.rcbankmobile.db.SharedPreferenceManager;
import com.octoberry.rcbankmobile.handler.Document;
import com.octoberry.rcbankmobile.net.AsyncJSONLoader;
import com.octoberry.rcbankmobile.net.JSONResponseListener;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class WaitDocumentsActivity extends Activity {
	private TextView mPrepareDocumentsTextView;
	private ImageView mChatImageView;
	
	private ArrayList<Document> mDocumentsCheckList = null;
	private ArrayList<Document> mFounderCheckList = null;
	
	private StatusHandler mStatusHandler = new StatusHandler();
	private static final int SECUND = 1000;
	private static final int CHECK_ACCOUNT_STATUS_PERIOD = 20 * SECUND;
	
	boolean isActivityStoped = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wait_documents);
		
		mPrepareDocumentsTextView = (TextView)findViewById(R.id.prepareDocumentsTextView);
		mChatImageView = (ImageView)findViewById(R.id.chatImageView);
		
		mPrepareDocumentsTextView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				DataBaseManager.getInstance(WaitDocumentsActivity.this).setAccountStatus(DataBaseManager.ACCOUNT_STATUS_DOCUMENTS);
				Intent intent = new Intent(getBaseContext(), PrepareDocumentsActivity.class);
				intent.putParcelableArrayListExtra("check_list_documents", mDocumentsCheckList);
				intent.putParcelableArrayListExtra("founder_check_list", mFounderCheckList);
				startActivity(intent);
				finish();
			}
		});
		
		mChatImageView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(WaitDocumentsActivity.this, ChatActivity.class);
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		isActivityStoped = false;
		checkAccountStatus();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		isActivityStoped = true;
	}
	
	private void checkAccountStatus() {
		AsyncJSONLoader loader = new AsyncJSONLoader(this);
		loader.registryListener(mStatusHandler);
		Bundle params = new Bundle();
		params.putString("endpoint", "/api/user");
		params.putString("requestType", "GET");
		Bundle headerParams = new Bundle();
		headerParams.putString("Authorization", DataBaseManager.getInstance(this).getCrmToken());
		loader.execute(params, headerParams, null);
	}

	class StatusHandler implements JSONResponseListener {

		@Override
		public void handleResponse(int result, JSONObject response, String error) {
			boolean needToRequest = true;
			if (response != null) {
				Log.d("###", response.toString());
				try {
					int resultCode = response.getInt("status");
					
					if (resultCode == 200) {
						JSONObject resultObject = response.getJSONObject("result");
						JSONObject organization = resultObject.getJSONObject("organization");
                        Log.d("###", "status: " + organization.getString("status"));
						if (DataBaseManager.ACCOUNT_STATUS_DOCUMENTS.equals(organization.getString("status"))) {
							needToRequest = false;
							mPrepareDocumentsTextView.setEnabled(true);
							mPrepareDocumentsTextView.setBackgroundColor(Color.BLACK);
							mPrepareDocumentsTextView.setTextColor(Color.WHITE);
							if (!organization.isNull("account_number")) {
								SharedPreferenceManager.getInstance(WaitDocumentsActivity.this).setAccountNumber(organization.getString("account_number"));
							}
							if (!organization.isNull("account_creds")) {
								SharedPreferenceManager.getInstance(WaitDocumentsActivity.this).setCreds(organization.getString("account_creds"));
							}
							if (!resultObject.isNull("dbo_user")) {
								JSONObject dboUser = resultObject.getJSONObject("dbo_user");
								if (!dboUser.isNull("is_signature_created")) {
									SharedPreferenceManager.getInstance(WaitDocumentsActivity.this).setSignatureCreated(dboUser.getBoolean("is_signature_created"));
								}
							}
							
							if (!organization.isNull("meeting_checklist")) {
								mDocumentsCheckList = new ArrayList<Document>();
								JSONArray jsonArray = organization.getJSONArray("meeting_checklist");
								for (int i = 0; i < jsonArray.length(); i++) {
									Document doc = new Document(jsonArray.getJSONObject(i).getString("title"));
									mDocumentsCheckList.add(doc);
								}								
							}
                            Log.d("###", "check uploads");
							if (!organization.isNull("uploads_checklist")) {
								mFounderCheckList = new ArrayList<Document>();
								JSONArray jsonArray = organization.getJSONArray("uploads_checklist");
                                Log.d("###", "documents length: " + jsonArray.length());
								for (int i = 0; i < jsonArray.length(); i++) {
                                    Log.d("###", "create document[" + i + "] with title: " + jsonArray.getJSONObject(i).getString("title"));
  									Document doc = new Document(jsonArray.getJSONObject(i).getString("title"));
                                    doc.setType(jsonArray.getJSONObject(i).getString("type"));
                                    JSONArray docArray = jsonArray.getJSONObject(i).getJSONArray("docs");
                                    if (docArray.length() > 0) {
                                        doc.setFirstPageId(docArray.getJSONObject(0).getString("id"));
                                        doc.setFirstPageTitle(docArray.getJSONObject(0).getString("title"));
                                    }
                                    if (docArray.length() > 1) {
                                        doc.setSecondPageId(docArray.getJSONObject(1).getString("id"));
                                        doc.setSecondPageTitle(docArray.getJSONObject(1).getString("title"));
                                    }
									mFounderCheckList.add(doc);
								}								
							}
						}
					} else {
						Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_LONG).show();
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
			if (needToRequest && (!isActivityStoped)) {
				try {
					Thread.sleep(CHECK_ACCOUNT_STATUS_PERIOD);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				checkAccountStatus();
			}
		}		
	}
}
