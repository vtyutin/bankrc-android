package com.octoberry.rcbankmobile.chat;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.octoberry.rcbankmobile.GeneralUtils;
import com.octoberry.rcbankmobile.R;
import com.quickblox.module.chat.model.QBAttachment;
import com.quickblox.module.chat.model.QBChatHistoryMessage;
import com.quickblox.module.chat.model.QBMessage;
import com.quickblox.module.users.model.QBUser;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatAdapter extends ResourceCursorAdapter {

    private final String TAG = getClass().getName();

    private static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";
    private ChatActivity mChatActivity;
    private QBUser user;
    int mImagePreviewWidth;
    int mImagePreviewHeight;

    public ChatAdapter(ChatActivity chatActivity, int layout, Cursor c, int flags) {
        super(chatActivity, layout, c, flags);
        this.mChatActivity = chatActivity;

        Display display = chatActivity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        if (size.x < size.y) {
            mImagePreviewHeight = size.x / 3;
            mImagePreviewWidth = size.x / 3;
        } else {
            mImagePreviewHeight = size.y / 3;
            mImagePreviewWidth = size.y / 3;
        }
    }
    
    void setUser(QBUser user) {
    	this.user = user;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder;

        holder = (ViewHolder) view.getTag();
        if (holder == null) {
            holder = createViewHolder(view);
            view.setTag(holder);
        }
        if (holder.loadTask != null) {
            holder.loadTask.cancel(true);
            holder.progressBar.setVisibility(View.GONE);
        }

        setAlignment(holder, cursor.getString(cursor.getColumnIndex(ChatDBManager.Columns.SENDER_ID)).equals("" + user.getId()));
        holder.txtMessage.setText(cursor.getString(cursor.getColumnIndex(ChatDBManager.Columns.MESSAGE)));
        holder.txtInfo.setText(getTimeText(cursor.getString(cursor.getColumnIndex(ChatDBManager.Columns.DATE_SENT))));

        if (cursor.getString(cursor.getColumnIndex(ChatDBManager.Columns.ATTACHMENT_ID)) != null) {
            if ((holder.loadTask != null) && (!holder.loadTask.isCanceled) && (holder.loadTask.getAttachment() != null)) {
                if (holder.loadTask.getAttachment().getId().equals(cursor.getString(cursor.getColumnIndex(ChatDBManager.Columns.ATTACHMENT_ID)))) {
                    return;
                }
            }

            LoadAttachmentAsyncTask loadTask = new LoadAttachmentAsyncTask();
            loadTask.setHolder(holder);
            holder.loadTask = loadTask;
            QBAttachment attachment = new QBAttachment(cursor.getString(cursor.getColumnIndex(ChatDBManager.Columns.ATTACHMENT_TYPE)));
            attachment.setId(cursor.getString(cursor.getColumnIndex(ChatDBManager.Columns.ATTACHMENT_ID)));
            attachment.setUrl(cursor.getString(cursor.getColumnIndex(ChatDBManager.Columns.ATTACHMENT_URL)));
            loadTask.execute(attachment);
            holder.attachmentLayout.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.attachmentImageView.setImageBitmap(null);
        } else {
            holder.loadTask = null;
            holder.attachmentLayout.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.GONE);
        }
    }

    class LoadAttachmentAsyncTask extends AsyncTask<QBAttachment, Void, Bitmap> {
        private ViewHolder holder;
        private boolean isCanceled = false;
        private String pathToPreview;
        private QBAttachment attachment;
        public void setHolder(ViewHolder holder) {
            this.holder = holder;
        }

        public QBAttachment getAttachment() {
            return attachment;
        }

        @Override
        protected void onCancelled() {
            isCanceled = true;
            super.onCancelled();
        }

        @Override
        protected Bitmap doInBackground(QBAttachment... qbAttachments) {
            attachment = qbAttachments[0];
            if (attachment.getUrl() == null) {
                return null;
            }
            File storedFile = new File(Environment.getExternalStorageDirectory() + "/octoberry/attachments", String.format("attachment_%s.JPG", attachment.getId()));
            if (storedFile.exists()) {
                GeneralUtils utils = new GeneralUtils();
                String thumbnail = utils.decodeImageFromFile(storedFile.getAbsolutePath(), attachment.getId(), mImagePreviewWidth, mImagePreviewHeight);
                Bitmap bitmap = BitmapFactory.decodeFile(thumbnail);

                File file = new File(Environment.getExternalStorageDirectory() + "/octoberry/attachments", String.format("attachment_%s.JPG", attachment.getId()));
                pathToPreview = file.getAbsolutePath();

                if (isCanceled) {
                    return null;
                }

                return bitmap;
            }

            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(attachment.getUrl());
            HttpResponse response = null;
            try {
                response = httpclient.execute(httpget);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (response == null) {
                Log.e(TAG, "can't download attachment");
                return null;
            }

            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode == 200) {
                HttpEntity fileEntity = response.getEntity();
                InputStream is = null;
                try {
                    is = fileEntity.getContent();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "can't get content");
                    return null;
                }

                File dir = new File(Environment.getExternalStorageDirectory() + "/octoberry/attachments");
                if (!dir.exists()) {
                    dir.mkdir();
                }

                File file = new File(Environment.getExternalStorageDirectory() + "/octoberry/attachments", String.format("attachment_%s.JPG", attachment.getId()));
                pathToPreview = file.getAbsolutePath();
                FileOutputStream fos = null;
                try {
                    file.createNewFile();
                    fos = new FileOutputStream(file);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "can't create file: " + file.getAbsolutePath());
                    return null;
                }
                int readByte;
                byte[] buffer = new byte[10240];
                try {
                    while ((readByte = is.read(buffer)) > 0) {
                        fos.write(buffer, 0, readByte);
                    }
                    is.close();
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "can't read stream");
                    return null;
                }

                if (isCanceled) {
                    return null;
                }

                GeneralUtils utils = new GeneralUtils();
                String thumbnail = utils.decodeImageFromFile(file.getAbsolutePath(), attachment.getId(), mImagePreviewWidth, mImagePreviewHeight);
                Bitmap bitmap = BitmapFactory.decodeFile(thumbnail);
                return bitmap;
            } else {
                Log.e(TAG, "Can't load attachment file. result: " + responseCode);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap image) {
            holder.progressBar.setVisibility(View.GONE);
            if (isCanceled) {
                return;
            }
            if (image != null) {
                holder.attachmentImageView.setImageBitmap(image);
                holder.attachmentImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mChatActivity.startImagePreview(pathToPreview);
                    }
                });
            }
        }
    }

    private void setAlignment(ViewHolder holder, boolean isIncoming) {
    	if (isIncoming) {
            holder.contentWithBG.setBackgroundResource(R.drawable.incoming_message_bg);

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.contentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            holder.content.setLayoutParams(lp);
            layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            layoutParams.bottomMargin = 50;
            holder.txtMessage.setLayoutParams(layoutParams);

            layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.txtInfo.setLayoutParams(layoutParams);
        } else {
            holder.contentWithBG.setBackgroundResource(R.drawable.outgoing_message_bg);

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.contentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            holder.content.setLayoutParams(lp);
            layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            layoutParams.bottomMargin = 50;
            holder.txtMessage.setLayoutParams(layoutParams);

            layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.txtInfo.setLayoutParams(layoutParams);
        }
    }

    private ViewHolder createViewHolder(View v) {
    	ViewHolder holder = new ViewHolder();
        holder.txtMessage = (TextView) v.findViewById(R.id.txtMessage);
        holder.txtMessage.setTextColor(Color.WHITE);
        holder.content = (LinearLayout) v.findViewById(R.id.content);
        holder.contentWithBG = (LinearLayout) v.findViewById(R.id.contentWithBackground);
        holder.txtInfo = (TextView) v.findViewById(R.id.txtInfo);
        holder.attachmentLayout = (RelativeLayout) v.findViewById(R.id.attachmentRelativeLayout);
        holder.progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        holder.attachmentImageView = (ImageView) v.findViewById(R.id.attachmentImageView);
        return holder;
    }

    private String getTimeText(String dateSent) {
    	if (dateSent != null){
            try {
                return DateFormat.format(DATE_FORMAT, Long.parseLong(dateSent)).toString();
            } catch (NumberFormatException nfe) {
            }
        }
        return DateFormat.format(DATE_FORMAT, new Date().getTime()).toString();
    }

    private static class ViewHolder {
        public LoadAttachmentAsyncTask loadTask = null;
        public TextView txtMessage;
        public TextView txtInfo;
        public LinearLayout content;
        public LinearLayout contentWithBG;
        public RelativeLayout attachmentLayout;
        public ProgressBar progressBar;
        public ImageView attachmentImageView;
    }
}
