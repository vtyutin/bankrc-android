package com.octoberry.rcbankmobile.chat;

import java.util.ArrayList;

import com.octoberry.rcbankmobile.R;
import com.quickblox.module.chat.model.QBMessage;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

public class ChatActivity extends Activity {
	private ListView mMessagesContainer;
	private EditText mMessageEditText;
	private ImageView mStatusImageView;
	private ImageView mCloseImageView;
	private ImageView mSendImageView;
	private ProgressBar mProgressBar;

	private ChatAdapter adapter;
	
	private static final String TAG = ChatActivity.class.getSimpleName();	
	
	private static ChatActivity instance = null;
	
	boolean bound = false;
	ServiceConnection sConn;
	ChatService mChatService;
	BroadcastReceiver mReceiver; 
	
	public static ChatActivity getInstance() {
		return instance;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		instance = this;
		
		setContentView(R.layout.activity_chat);

		mMessagesContainer = (ListView) findViewById(R.id.messagesContainer);
		mMessagesContainer.setAdapter(new ChatAdapter(this));
		mMessagesContainer.setAdapter(adapter);
		mMessageEditText = (EditText) findViewById(R.id.messageEdit);
		mStatusImageView = (ImageView) findViewById(R.id.statusImageView);
		mCloseImageView = (ImageView) findViewById(R.id.closeImageView);
		mSendImageView = (ImageView) findViewById(R.id.chatSendImageView);
		mSendImageView.setEnabled(false);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

		adapter = new ChatAdapter(ChatActivity.this);		
		mMessagesContainer.setAdapter(adapter);
		
		mProgressBar.setVisibility(View.VISIBLE);
		mSendImageView.setEnabled(false);
		mStatusImageView.setImageResource(R.drawable.mes_offline);
		
		mSendImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String messageText = mMessageEditText.getText().toString();
				if (messageText.length() == 0) {
					return;
				}
				boolean result = mChatService.sendMessage(messageText);
				mMessageEditText.setText("");
			}
		});	
		
		mCloseImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});		
		
		sConn = new ServiceConnection() {
			public void onServiceConnected(ComponentName name, IBinder binder) {
				Log.d(TAG, "MainActivity onServiceConnected");
				mChatService = ((ChatService.ChatBinder) binder).getService();
				adapter.setUser(mChatService.getUser());
				ArrayList<ChatMessage> history = mChatService.getChatHistory();
				
				if (history != null) {
					adapter.clean();
					for (ChatMessage message : history) {
						adapter.add(message);
					}
					adapter.notifyDataSetChanged();
					
					mMessagesContainer.setSelection(adapter.getCount() - 1);
					
					mProgressBar.setVisibility(View.GONE);
					mSendImageView.setEnabled(true);
					mStatusImageView.setImageResource(R.drawable.mes_online);
					
				}
				bound = true;
			}

			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "MainActivity onServiceDisconnected");
				bound = false;
			}
		};
		
		mReceiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				if (mChatService == null) {
					return;
				}
				switch (intent.getIntExtra(ChatService.MSG_ID, 0)) {
				case ChatService.INIT_DONE:
					ArrayList<ChatMessage> history = mChatService.getChatHistory();
					if (history != null) {
						adapter.clean();
						for (ChatMessage message : history) {
							adapter.add(message);
						}
						adapter.notifyDataSetChanged();
						
						mMessagesContainer.setSelection(adapter.getCount() - 1);
						
						mProgressBar.setVisibility(View.GONE);
						mSendImageView.setEnabled(true);
						mStatusImageView.setImageResource(R.drawable.mes_online);
						
					}
					break;
				case ChatService.INCOMMING_MESSAGE:
					String id = intent.getStringExtra(ChatService.MSG_MESSAGE_ID);
					String dialogId = intent.getStringExtra(ChatService.MSG_DIALOG_ID);
					String msg = intent.getStringExtra(ChatService.MSG_DATA);
					int senderId = intent.getIntExtra(ChatService.MSG_SENDER, 0);
					int recipientId = intent.getIntExtra(ChatService.MSG_RECIPIENT, 0);
					String dateSent = intent.getStringExtra(ChatService.MSG_DATE_SENT);
					ChatMessage message = new ChatMessage(id, dialogId, msg, senderId, recipientId, dateSent);
					showMessage(message);
					break;
				default:
					Log.w(TAG,
							"invalid message id got in receiver: "
									+ intent.getIntExtra(ChatService.MSG_ID, 0));
					break;
				}
			}
		};
	}
	
	@Override
    protected void onResume() {
		IntentFilter intFilt = new IntentFilter(ChatService.BROADCAST_ACTION);
	    registerReceiver(mReceiver, intFilt);
	    
	    mProgressBar.setVisibility(View.VISIBLE);
		mSendImageView.setEnabled(false);
		mStatusImageView.setImageResource(R.drawable.mes_offline);
		
		Intent intent = new Intent(this, ChatService.class);
		bindService(intent, sConn, BIND_AUTO_CREATE);
        super.onResume();
    }
	
	@Override
	protected void onPause() {
		Log.d("###", "onPause");
		
		if (bound) unbindService(sConn);
	    bound = false;
	    unregisterReceiver(mReceiver);
		super.onPause();
	}
		
	public void showMessage(QBMessage message) {
		adapter.add(message);
		adapter.notifyDataSetChanged();
		mMessagesContainer.setSelection(mMessagesContainer.getCount() - 1);
	}
	
}
