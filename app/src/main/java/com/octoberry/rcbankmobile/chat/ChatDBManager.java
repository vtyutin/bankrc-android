package com.octoberry.rcbankmobile.chat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.octoberry.rcbankmobile.db.DataBaseManager;
import com.quickblox.module.chat.model.QBAttachment;

/**
 * Created by ruinvtyu on 27.01.2015.
 */
public class ChatDBManager {
    private static final int DB_VERSION = 1;
    private static final String TABLE_CHAT = "chat";
    private static ChatDBManager instance = null;

    public static class Columns {
        public static final String ID = "_id";
        public static final String MESSAGE_ID = "message_id";
        public static final String SENDER_ID = "sender_id";
        public static final String RECIPIENT_ID = "recipient_id";
        public static final String DATE_SENT = "date_sent";
        public static final String DIALOG_ID = "dialog_id";
        public static final String MESSAGE = "message";
        public static final String ATTACHMENT_TYPE = "attachment_type";
        public static final String ATTACHMENT_ID = "attachment_id";
        public static final String ATTACHMENT_URL = "attachment_url";
        public static final String STATUS = "status";
    }

    private DatabaseHelper mDatabaseHelper;

    private ChatDBManager(Context context) {
        mDatabaseHelper = new DatabaseHelper(context);
    }

    public static ChatDBManager getInstance(Context context) {
        if (instance == null) {
            instance = new ChatDBManager(context);
        }
        return instance;
    }

    private class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, "chat_db", null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createAllTables(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d("###", "Update DB");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT);
            // TODO implement db updating
            onCreate(db);
        }
    }

    private void createAllTables(SQLiteDatabase db) {
        db.execSQL("Create table " + TABLE_CHAT
                + "( " + Columns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "  " + Columns.MESSAGE_ID + " VARCHAR(125), "
                + "  " + Columns.SENDER_ID + " VARCHAR(15), "
                + "  " + Columns.RECIPIENT_ID + " VARCHAR(15), "
                + "  " + Columns.DIALOG_ID + " VARCHAR(15), "
                + "  " + Columns.DATE_SENT + " VARCHAR(15), "
                + "  " + Columns.MESSAGE + " VARCHAR(512), "
                + "  " + Columns.STATUS + " VARCHAR(10), "
                + "  " + Columns.ATTACHMENT_TYPE + " VARCHAR(10), "
                + "  " + Columns.ATTACHMENT_ID + " VARCHAR(15), "
                + "  " + Columns.ATTACHMENT_URL + " VARCHAR(250));");
    }

    public Cursor getAllMessages() {
        String result = null;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        qb.setTables(TABLE_CHAT);
        try {
            Cursor c = qb.query(db, null, null, null, null, null, Columns.DATE_SENT + " ASC");
            return c;
        } catch (Exception exc) {
            Log.e(this.getClass().getName(), "getAllMessages: error: " + exc.getMessage());
        }
        return null;
    }

    public boolean addMessage(ChatMessage message, boolean forceUpdate) {
        int idToUpdate = -1;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        qb.setTables(TABLE_CHAT);
        final String[] PROJECTION = { Columns.ID, Columns.MESSAGE_ID};
        final String SELECTION = Columns.MESSAGE_ID + "=?";
        final String[] SELECTION_ARGS = { message.getId() };
        // get record id first
        try {
            Cursor c = qb.query(db, PROJECTION, SELECTION, SELECTION_ARGS, null, null, null);
            if (c.moveToFirst()) {
                idToUpdate = c.getInt(c.getColumnIndex(Columns.ID));
            }
        } catch (Exception exc) {
            Log.e(this.getClass().getName(), "addMessage: get record id error: " + exc.getMessage());
        }

        ContentValues values = new ContentValues();
        values.put(Columns.MESSAGE_ID, message.getId());
        values.put(Columns.DIALOG_ID, message.getDialogId());
        values.put(Columns.SENDER_ID, message.getSenderId());
        values.put(Columns.RECIPIENT_ID, message.getRecipientId());
        values.put(Columns.DATE_SENT, message.getDateSent());
        values.put(Columns.MESSAGE, message.getBody());
        if ((message.getAttachments() != null) && (message.getAttachments().size() > 0)) {
            QBAttachment attachment = message.getAttachments().iterator().next();
            values.put(Columns.ATTACHMENT_ID, attachment.getId());
            values.put(Columns.ATTACHMENT_TYPE, attachment.getType());
            values.put(Columns.ATTACHMENT_URL, attachment.getUrl());
        } else {
            values.putNull(Columns.ATTACHMENT_ID);
            values.putNull(Columns.ATTACHMENT_TYPE);
            values.putNull(Columns.ATTACHMENT_URL);
        }
        if (idToUpdate != -1) {
            if (!forceUpdate) {
                // message is already exist
                return true;
            } else {
                // message exists, need to update
                return db.update(TABLE_CHAT, values, Columns.ID + "=" + idToUpdate, null) > -1 ? true : false;
            }
        } else {
            return db.insert(TABLE_CHAT, null, values) > -1 ? true : false;
        }
    }
}
