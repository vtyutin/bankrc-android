package com.octoberry.rcbankmobile.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class SharedPreferenceManager {
	private static SharedPreferenceManager instance;
	private static SharedPreferences mPreferences;
	
	private SharedPreferenceManager() {		
	}
	
	public static SharedPreferenceManager getInstance(Context context) {		
		if (instance == null) {
			instance = new SharedPreferenceManager();
		}
		mPreferences = context.getSharedPreferences("prefs", 0);
		return instance;
	}
	
	public void clearAllPreferences() {
		Editor editor = mPreferences.edit();
		editor.clear();
		editor.commit();
	}
	
	public void setUploadDocId(String docId) {
		Editor editor = mPreferences.edit();
		editor.putString("upload_doc_id", docId);
		editor.commit();
	}
	
	public String getUploadDocId() {
		return mPreferences.getString("upload_doc_id", null);
	}
	
	public void clearUploadDocId() {
		Editor editor = mPreferences.edit();
		editor.remove("upload_doc_id");
		editor.commit();
	}
	
	public void setUploadDocPage(int pageIndex) {
		Editor editor = mPreferences.edit();
		editor.putInt("upload_doc_page", pageIndex);
		editor.commit();
	}
	
	public int getUploadDocPage() {
		return mPreferences.getInt("upload_doc_page", -1);
	}
	
	public void clearUploadDocPage() {
		Editor editor = mPreferences.edit();
		editor.remove("upload_doc_page");
		editor.commit();
	}
	
	public void setUploadDocPath(String path) {
		Editor editor = mPreferences.edit();
		editor.putString("upload_doc_path", path);
		editor.commit();
	}
	
	public String getUploadDocPath() {
		return mPreferences.getString("upload_doc_path", null);
	}
	
	public void clearUploadDocPath() {
		Editor editor = mPreferences.edit();
		editor.remove("upload_doc_path");
		editor.commit();
	}
	
	public void setCreds(String creds) {
		Editor editor = mPreferences.edit();
		editor.putString("creds", creds);
		editor.commit();
	}
	
	public String getCreds() {
		return mPreferences.getString("creds", null);
	}
	
	public void setSignatureCreated(boolean isCreated) {
		Editor editor = mPreferences.edit();
		editor.putBoolean("is_signature_created", true);
		editor.commit();
	}
	
	public boolean isSignatureCreated() {
		return mPreferences.getBoolean("is_signature_created", false);
	}
	
	public void setCredsTitle(String credsTitle) {
		Editor editor = mPreferences.edit();
		editor.putString("creds_title", credsTitle);
		editor.commit();
	}
	
	public String getCredsTitle() {
		return mPreferences.getString("creds_title", null);
	}
	
	public void setAccountNumber(String accountNumber) {
		String formatedNumber;
		if (accountNumber.length() >= 20) {
			formatedNumber = String.format("%s %s\n%s %s", accountNumber.substring(0, 5), accountNumber.substring(5, 10),
					accountNumber.substring(10, 15), accountNumber.substring(15));
		} else if (accountNumber.length() >= 15) {
			formatedNumber = String.format("%s %s\n%s", accountNumber.substring(0, 5), accountNumber.substring(5, 10),
					accountNumber.substring(10));
		} else if (accountNumber.length() >= 10) {
			formatedNumber = String.format("%s %s\n%s", accountNumber.substring(0, 5), accountNumber.substring(5, 10),
					accountNumber.substring(10));
		} else if (accountNumber.length() >= 5) {
			formatedNumber = String.format("%s %s", accountNumber.substring(0, 5), accountNumber.substring(5));
		} else {
			formatedNumber = accountNumber;
		}
		Editor editor = mPreferences.edit();
		editor.putString("account_number", formatedNumber);
		editor.commit();
	}
	
	public String getAccountNumber() {
		return mPreferences.getString("account_number", null);
	}
}
