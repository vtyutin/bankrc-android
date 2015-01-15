package com.octoberry.rcbankmobile.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class SharedPreferenceManager {
	private static SharedPreferenceManager instance;
	private static SharedPreferences mPreferences;
    private static SharedPreferences mUploadDataPreferences;
    private static SharedPreferences mCheckDataPreferences;
	
	private SharedPreferenceManager() {		
	}
	
	public static SharedPreferenceManager getInstance(Context context) {		
		if (instance == null) {
			instance = new SharedPreferenceManager();
		}
		mPreferences = context.getSharedPreferences("prefs", 0);
        mUploadDataPreferences = context.getSharedPreferences("upload_data_prefs", 0);
        mCheckDataPreferences = context.getSharedPreferences("check_data_prefs", 0);
		return instance;
	}

    public void setUploadedDocPath(String docId, String docPath) {
        Editor editor = mUploadDataPreferences.edit();
        editor.putString(docId, docPath);
        editor.commit();
    }

    public String getUploadedDocPath(String docId) {
        return mUploadDataPreferences.getString(docId, null);
    }

    public void clearUploadedDocPath(String docId) {
        Editor editor = mUploadDataPreferences.edit();
        editor.remove(docId);
        editor.commit();
    }

    public void setCheckDocStatus(String docId, boolean isPrepared) {
        Editor editor = mCheckDataPreferences.edit();
        editor.putBoolean(docId, isPrepared);
        editor.commit();
    }

    public boolean getCheckDocStatus(String docId) {
        return mCheckDataPreferences.getBoolean(docId, false);
    }

    public void clearCheckDocStatus() {
        Editor editor = mCheckDataPreferences.edit();
        editor.clear();
        editor.commit();
    }

    public void clearUploadData() {
        Editor editor = mPreferences.edit();
        editor.remove("upload_doc_id");
        editor.remove("upload_doc_type");
        editor.remove("upload_doc_title");
        editor.remove("upload_doc_page");
        editor.remove("upload_doc_path");
        editor.commit();
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

    public void setUploadDocTitle(String docTitle) {
        Editor editor = mPreferences.edit();
        editor.putString("upload_doc_title", docTitle);
        editor.commit();
    }

    public String getUploadDocTitle() {
        return mPreferences.getString("upload_doc_title", null);
    }

    public void clearUploadDocTitle() {
        Editor editor = mPreferences.edit();
        editor.remove("upload_doc_title");
        editor.commit();
    }

    public void setUploadDocType(String type) {
        Editor editor = mPreferences.edit();
        editor.putString("upload_doc_type", type);
        editor.commit();
    }

    public String getUploadDocType() {
        return mPreferences.getString("upload_doc_type", null);
    }

    public void clearUploadDocType() {
        Editor editor = mPreferences.edit();
        editor.remove("upload_doc_type");
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

    public void setDashboardInfoViewed() {
        Editor editor = mPreferences.edit();
        editor.putBoolean("dashboard_info", true);
        editor.commit();
    }

    public boolean isDashboardInfoViwed() { return mPreferences.getBoolean("dashboard_info", false); }
}
