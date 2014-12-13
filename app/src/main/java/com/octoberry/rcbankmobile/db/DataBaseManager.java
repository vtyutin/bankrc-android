package com.octoberry.rcbankmobile.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

public class DataBaseManager {
	public static String ACCOUNT_STATUS_NEW = "new";
	public static String ACCOUNT_STATUS_CONFIRMED = "confirmed";
	public static String ACCOUNT_STATUS_DOCUMENTS = "documents";
	public static String ACCOUNT_STATUS_MEETING = "meeting";
	public static String ACCOUNT_STATUS_CREATED = "created";
	public static String ACCOUNT_STATUS_DENIED = "denied";
	public static String ACCOUNT_STATUS_ERROR = "error";
	public static String ACCOUNT_STATUS_PREPARED = "prepared";
	
	private static final int DB_VERSION = 1;
	private static final String TABLE_SETTINGS = "settings";
	private static DataBaseManager instance = null;
	
	public static class Columns {
		public static final String ID = "_id";
        public static final String ACTIVE_TOKEN = "active_token";
        public static final String PHONE_NUMBER = "phone_number";
        public static final String MEET_DATE = "meet_date";
        public static final String MEET_TIME = "meet_time";
        public static final String ACCOUNT_DETAILS = "account_details";
        public static final String CURENT_TOKEN = "current_token";
        public static final String CHAT_TOKEN = "chat_token";
        public static final String CHAT_ID = "chat_id";
        public static final String CHAT_PASSWORD = "chat_password";
        public static final String CHAT_LOGIN = "chat_login";
        public static final String CHAT_USER_ID = "chat_user_id";
        public static final String OGRN = "ogrn";
        public static final String ACCOUNT_STATUS = "status";
    }
	
	private DatabaseHelper mDatabaseHelper;
	
	private DataBaseManager(Context context) {
		mDatabaseHelper = new DatabaseHelper(context);		
	}
	
	public static DataBaseManager getInstance(Context context) {
		if (instance == null) {
			instance = new DataBaseManager(context);
		}
		return instance;
	}
	
	// API methods
	public synchronized String getAccountStatus() {		
        return getStringParameter(TABLE_SETTINGS, Columns.ACCOUNT_STATUS, null);
	}
	
	public synchronized void setAccountStatus(String status) {
		updateStringParameter(TABLE_SETTINGS, Columns.ACCOUNT_STATUS, status, null);
	}
	
	public synchronized String getPhoneNumber() {		
        return getStringParameter(TABLE_SETTINGS, Columns.PHONE_NUMBER, null);
	}
	
	public synchronized void setPhoneNumber(String phoneNumber) {
		updateStringParameter(TABLE_SETTINGS, Columns.PHONE_NUMBER, phoneNumber, null);
	}
	
	public synchronized String getOgrn() {		
        return getStringParameter(TABLE_SETTINGS, Columns.OGRN, null);
	}
	
	public synchronized void setOgrn(String ogrn) {
		updateStringParameter(TABLE_SETTINGS, Columns.OGRN, ogrn, null);
	}
	
	public synchronized String getActiveToken() {		
        return getStringParameter(TABLE_SETTINGS, Columns.ACTIVE_TOKEN, null);
	}
	
	public synchronized void setCurrentToken(String token) {
		updateStringParameter(TABLE_SETTINGS, Columns.CURENT_TOKEN, token, null);
	}
	
	public synchronized String getCurrentToken() {		
        return getStringParameter(TABLE_SETTINGS, Columns.CURENT_TOKEN, null);
	}
	
	public synchronized void setChatToken(String token) {
		updateStringParameter(TABLE_SETTINGS, Columns.CHAT_TOKEN, token, null);
	}
	
	public synchronized String getChatToken() {		
        return getStringParameter(TABLE_SETTINGS, Columns.CHAT_TOKEN, null);
	}
	
	public synchronized void setChatPassword(String password) {
		updateStringParameter(TABLE_SETTINGS, Columns.CHAT_PASSWORD, password, null);
	}
	
	public synchronized String getChatPassword() {		
        return getStringParameter(TABLE_SETTINGS, Columns.CHAT_PASSWORD, null);
	}
	
	public synchronized void setChatLogin(String login) {
		updateStringParameter(TABLE_SETTINGS, Columns.CHAT_LOGIN, login, null);
	}
	
	public synchronized String getChatLogin() {		
        return getStringParameter(TABLE_SETTINGS, Columns.CHAT_LOGIN, null);
	}
	
	public synchronized void setChatUserId(String userId) {
		updateStringParameter(TABLE_SETTINGS, Columns.CHAT_USER_ID, userId, null);
	}
	
	public synchronized String getChatUserId() {		
        return getStringParameter(TABLE_SETTINGS, Columns.CHAT_USER_ID, null);
	}
	
	public synchronized void setChatId(String id) {
		updateStringParameter(TABLE_SETTINGS, Columns.CHAT_ID, id, null);
	}
	
	public synchronized String getChatId() {		
        return getStringParameter(TABLE_SETTINGS, Columns.CHAT_ID, null);
	}
	
	public synchronized void setActiveToken(String token) {
		updateStringParameter(TABLE_SETTINGS, Columns.ACTIVE_TOKEN, token, null);
	}
	
	public synchronized String getMeetDate() {		
        return getStringParameter(TABLE_SETTINGS, Columns.MEET_DATE, null);
	}
	
	public synchronized void setMeetDate(String date) {
		updateStringParameter(TABLE_SETTINGS, Columns.MEET_DATE, date, null);
	}
	
	public synchronized String getMeetTime() {		
        return getStringParameter(TABLE_SETTINGS, Columns.MEET_TIME, null);
	}
	
	public synchronized void setMeetTime(String time) {
		updateStringParameter(TABLE_SETTINGS, Columns.MEET_TIME, time, null);
	}
	
	public synchronized String getAccountDetails() {		
        return getStringParameter(TABLE_SETTINGS, Columns.ACCOUNT_DETAILS, null);
	}
	
	public synchronized void setAccountDetails(String details) {
		updateStringParameter(TABLE_SETTINGS, Columns.ACCOUNT_DETAILS, details, null);
	}
	
	// get and update one string column in appropriate table
	private String getStringParameter(String tableName, String columnName, String id) {
		String result = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();        
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        String[] projectionIn = new String[]{ columnName };
        qb.setTables(tableName);
        try {
        	Cursor c;
        	if (id == null) {
        		c = qb.query(db, projectionIn, null, null, null, null, null);
        	} else {
        		c = qb.query(db, projectionIn, Columns.ID + "=" + id, null, null, null, null);
        	}
	        if (c.moveToPosition(0)) {
	        	result = c.getString(c.getColumnIndex(columnName));
	        }
        } catch (Exception exc) {
    		Log.d(this.getClass().getName(), "getStringParameter: no parameter: " + columnName + " in table: " + tableName);
    	}
        return result;
	}
	
	private void updateStringParameter(String tableName, String columnName, String value, String id) {
		int idToUpdate = -1;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();        
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        qb.setTables(tableName);
        if (id == null) {
	        try {
	        	Cursor c = qb.query(db, null, null, null, null, null, null);        	
		        if (c.moveToFirst()) {
		        	idToUpdate = c.getInt(c.getColumnIndex(Columns.ID));
		        }
	        } catch (Exception exc) {
	    		exc.printStackTrace();
	    	}
        } else {
        	try {
        		idToUpdate = Integer.parseInt(id);
        	} catch (Exception exc) {
	    		exc.printStackTrace();
	    	}
        }
        
        ContentValues values = new ContentValues();
    	values.put(columnName, value);        
        if (idToUpdate > 0) { 
    		db.update(tableName, values, Columns.ID + "=" + idToUpdate, null);
    	} else {
    		db.insert(tableName, null, values);
    	}
	}

    private class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, "module_db", null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			createAllTables(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d("###", "Update DB");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
			// TODO implement db updating
			onCreate(db);
		}
	}
    
    private void createAllTables(SQLiteDatabase db) {
    	db.execSQL("Create table " + TABLE_SETTINGS
				+ "( " + Columns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "  " + Columns.PHONE_NUMBER + " VARCHAR(15), "
				+ "  " + Columns.OGRN + " VARCHAR(15), "
				+ "  " + Columns.ACCOUNT_STATUS + " VARCHAR(15), "
				+ "  " + Columns.MEET_DATE + " VARCHAR(25), "
				+ "  " + Columns.MEET_TIME + " VARCHAR(15), "
				+ "  " + Columns.ACCOUNT_DETAILS + " VARCHAR(512), "
				+ "  " + Columns.CURENT_TOKEN + " VARCHAR(50), "
				+ "  " + Columns.CHAT_TOKEN + " VARCHAR(50), "
				+ "  " + Columns.CHAT_ID + " VARCHAR(50), "
				+ "  " + Columns.CHAT_PASSWORD + " VARCHAR(50), "
				+ "  " + Columns.CHAT_LOGIN + " VARCHAR(50), "
				+ "  " + Columns.CHAT_USER_ID + " VARCHAR(50), "
				+ "  " + Columns.ACTIVE_TOKEN + " VARCHAR(50));");
    }
}
