package com.octoberry.rcbankmobile.chat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotLoggedInException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.json.JSONObject;

import com.octoberry.rcbankmobile.R;
import com.octoberry.rcbankmobile.db.DataBaseManager;
import com.octoberry.rcbankmobile.gcm.PlayServicesHelper;
import com.octoberry.rcbankmobile.net.AsyncJSONLoader;
import com.octoberry.rcbankmobile.net.JSONResponseListener;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.internal.module.custom.request.QBCustomObjectRequestBuilder;
import com.quickblox.internal.module.custom.request.QBCustomObjectUpdateBuilder;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.auth.model.QBSession;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.QBGroupChat;
import com.quickblox.module.chat.exception.QBChatException;
import com.quickblox.module.chat.listeners.QBMessageListener;
import com.quickblox.module.chat.model.QBChatHistoryMessage;
import com.quickblox.module.chat.model.QBChatMessage;
import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.chat.model.QBDialogType;
import com.quickblox.module.users.model.QBUser;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

public class ChatService extends Service implements QBMessageListener {
	
	private final static String TAG = ChatService.class.getSimpleName();
	
	public final static String BROADCAST_ACTION = "com.octoberry.rcbank.chataction";
	
	public final static String MSG_ID = "msg_id";
	public final static int INIT_DONE = 100;
	public final static int INCOMMING_MESSAGE = 200;
	
	public final static String MSG_MESSAGE_ID = "message_id";
	public final static String MSG_DIALOG_ID = "dialog_id";
	public final static String MSG_DATA = "message";
	public final static String MSG_SENDER = "sender";
	public final static String MSG_RECIPIENT = "recipient";
	public final static String MSG_DATE_SENT = "date_sent";
	
	private ChatTokenHandler mChatTokenHandler = new ChatTokenHandler();
	
	private QBUser mUser;
	private QBDialog mDialog = null;	
	private QBGroupChat mChat;
	
	boolean initDone = false;
	
	private PlayServicesHelper playServicesHelper;
	
	private ArrayList<ChatMessage> history = new ArrayList<ChatMessage>();

	@Override
	public IBinder onBind(Intent arg0) {
		Log.d("###", "service bind");
		if (initDone) {
			getChat();
		}
		return new ChatBinder();
	}
	
	class ChatBinder extends Binder {
	    ChatService getService() {
	    	return ChatService.this;
	    }
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("###", "service start");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCreate() {
		playServicesHelper = new PlayServicesHelper(this);
		Log.d("###", "token: " + DataBaseManager.getInstance(this).getActiveToken());
        Log.d("###", "phone_number: " + DataBaseManager.getInstance(this).getPhoneNumber());
		AsyncJSONLoader loader = new AsyncJSONLoader(this);
		loader.registryListener(mChatTokenHandler);
		Bundle params = new Bundle();
		params.putString("requestType", "GET");
		params.putString("endpoint", "/api/user");
		Bundle headerParams = new Bundle();
		headerParams.putString("Authorization", DataBaseManager.getInstance(this).getActiveToken());
        headerParams.putString("phone_number", DataBaseManager.getInstance(this).getPhoneNumber());
		loader.execute(params, headerParams, null);

		super.onCreate();
	}
	
	void initChatSession() {
		QBAuth.createSession(mUser, new QBEntityCallbackImpl<QBSession>() {
			@Override
			public void onSuccess(QBSession session, Bundle args) {
				super.onSuccess(session, args);
				Log.i("###", "session created, token = " + session.getToken());		
				playServicesHelper.registerInBackground();
				
				loginChat();				
			}
			@Override
			public void onError(List<String> errors) {
				handleErrors(errors);
			}
		});
	}

	void loginChat() {
		Log.d("###", "login chat");
				
		if (!QBChatService.isInitialized()) {
			QBChatService.init(this);
		}		
		
		if (QBChatService.getInstance().isLoggedIn() == false) {
			QBChatService.getInstance().login(mUser, new QBEntityCallbackImpl<Void>() {
						@Override
						public void onSuccess() {							
							getDialog();
						}
						@Override
						public void onError(List<String> errors) {
							handleErrors(errors);
							super.onError(errors);
						}
					});
		} else {
			getDialog();	        
		}		
	}
	
	void getDialog() {
		Log.d("###", "get Dialog");
		if (mDialog == null) {
			Runnable r = new Runnable() {				
				@Override
				public void run() {
					try {
						ArrayList<QBDialog> dialogs = QBChatService.getChatDialogs(QBDialogType.GROUP, new QBCustomObjectRequestBuilder(), new Bundle());
						Log.d("###", "dialog size: " + dialogs.size());
						if (dialogs.size() == 0) {
							QBDialog dialog = new QBDialog();
							dialog.setName("chat");
							ArrayList<Integer> occupants = new ArrayList<Integer>();												
							Log.d("###", "opponent id: " + Integer.valueOf(getResources().getString(R.string.chat_opponent_id)).intValue());
							Log.d("###", "user id: " + mUser.getId());
							occupants.add(mUser.getId());
							occupants.add(Integer.valueOf(getResources().getString(R.string.chat_opponent_id)).intValue());							
							dialog.setOccupantsIds(occupants);
							dialog.setType(QBDialogType.GROUP);
							Log.d("###", "create dialog");
							try {
								mDialog = QBChatService.getInstance().getGroupChatManager().createDialog(dialog);
								getChat();
							} catch (QBResponseException qre) {
								List<String> errors = new ArrayList<String>();
								errors.add("can't create dialog");
								errors.add(qre.getMessage());
								handleErrors(errors);
							}																
						} else {
							mDialog = dialogs.get(0);
							ArrayList<Integer> occupants = new ArrayList<Integer>();												
							Log.d("###", "opponent id: " + Integer.valueOf(getResources().getString(R.string.chat_opponent_id)).intValue());
							Log.d("###", "user id: " + mUser.getId());
							occupants.add(mUser.getId());
							occupants.add(Integer.valueOf(getResources().getString(R.string.chat_opponent_id)).intValue());							
							mDialog.setOccupantsIds(occupants);
							QBCustomObjectUpdateBuilder builder = new QBCustomObjectUpdateBuilder();
							builder.push("occupants_ids", occupants);
							QBChatService.getInstance().getGroupChatManager().updateDialog(mDialog, builder);
							getChat();
						}
					} catch (QBResponseException e) {
						List<String> errors = new ArrayList<String>();
						errors.add("can't get dialogs");
						errors.add(e.getMessage());
						handleErrors(errors);
					}
				}
			};
			Thread t = new Thread(r);
			t.start();
		} else {
			getChat();
		}
	}
	
	void getChat() {		
		Log.d("###", "get chat: " + mChat);		
		if (mChat == null) {
			Log.d("###", "create group chat: " + mDialog.getRoomJid());
			mChat = QBChatService.getInstance().getGroupChatManager().createGroupChat(mDialog.getRoomJid());
			Log.d("###", "chat created");
		}
		
		DiscussionHistory history = new DiscussionHistory();
        history.setMaxStanzas(0);
        Log.d("###", "join chat: " + mChat);
        mChat.join(history, new QBEntityCallbackImpl() {
            @Override
            public void onSuccess() {
            	Log.w("Chat", "Join successful");
                mChat.addMessageListener(ChatService.this);
        		loadChatHistory();
            }
            @Override
            public void onError(List list) {
                Log.e("Could not join chat, errors:", Arrays.toString(list.toArray()));
                handleErrors(list);
            }
        });
	}
	
	private void loadChatHistory() {
		QBCustomObjectRequestBuilder customObjectRequestBuilder = new QBCustomObjectRequestBuilder();
		customObjectRequestBuilder.setPagesLimit(100);
		Log.d("###", "loadChatHistory");
		new DialogMessageLoader().execute(mDialog);
	}

	private class DialogMessageLoader extends AsyncTask<QBDialog, Void, Void> {
		@Override
		protected Void doInBackground(QBDialog... params) {
			QBCustomObjectRequestBuilder customObjectRequestBuilder = new QBCustomObjectRequestBuilder();
			customObjectRequestBuilder.setPagesLimit(100);
			try {
				ArrayList<QBChatHistoryMessage> messages = QBChatService.getDialogMessages(params[0], customObjectRequestBuilder, new Bundle());
				Log.d("###", "loadChatHistory success");
				history.clear();
				for (QBChatHistoryMessage message: messages) {
					history.add(new ChatMessage(message.getId(), message.getDialogId(), message.getBody(),
							message.getSenderId(), message.getRecipientId(), 
							DateFormat.format("yyyy-MM-dd hh:mm:ss", message.getDateSent()).toString()));
				}
				
				try {
					QBChatService.getInstance().startAutoSendPresence(60);
				} catch (NotLoggedInException e) {
					List<String> errors = new ArrayList<String>();
					errors.add("can't start auto presense");
					errors.add(e.getMessage());
					handleErrors(errors);
				}
				
				initDone = true;
				Intent intent = new Intent(BROADCAST_ACTION);
			    intent.putExtra(MSG_ID, INIT_DONE);
			    sendBroadcast(intent);
			} catch (QBResponseException e) {
				List<String> errors = new ArrayList<String>();
				errors.add("can't get dialog messages");
				errors.add(e.getMessage());
				handleErrors(errors);
			}
			return null;
		}
	}
	
	private void handleErrors(List<String> errors) {
		String message = String.format("[ERROR] Request has been completed with errors: %s", errors);
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
		Log.e("###", message);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                onCreate();
            }
        }, 300000);
        //onCreate();
	}
	
	class ChatTokenHandler implements JSONResponseListener {
		@Override
		public void handleResponse(int result, JSONObject response, String error) {
			if (response != null) {
				Log.d("###", "chat message response: " + response.toString());							
				try {
					int resultCode = response.getInt("status");
					if (resultCode == 200) {
						JSONObject resultObject = response.getJSONObject("result");
						if (!resultObject.isNull("qb_user")) {
							JSONObject qbUser = resultObject.getJSONObject("qb_user");
							if (!qbUser.isNull("login") && !qbUser.isNull("password") && !qbUser.isNull("user_id")
									&& !qbUser.isNull("chat_id")) {
								DataBaseManager.getInstance(getApplicationContext()).setChatUserId(qbUser.getString("user_id"));
								DataBaseManager.getInstance(getApplicationContext()).setChatToken(qbUser.getString("login"));
								DataBaseManager.getInstance(getApplicationContext()).setChatPassword(qbUser.getString("password"));
								DataBaseManager.getInstance(getApplicationContext()).setChatId(qbUser.getString("chat_id"));
								mUser = new QBUser(qbUser.getString("login"), qbUser.getString("password"));
								mUser.setId(Integer.parseInt(qbUser.getString("user_id")));
								initChatSession();
							} else {
                                Log.w(TAG, "invalid chat data");
								Toast.makeText(getApplicationContext(), "invalid data", Toast.LENGTH_LONG).show();
							}
						} else {
                            Log.w(TAG, "no chat user created");
							Toast.makeText(getApplicationContext(), "No chat user created", Toast.LENGTH_LONG).show();
						}
					} else {
                        Log.e(TAG, "error getting user data: " + response.getString("message"));
						Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_LONG).show();
					}
				} catch (Exception exc) {
					List<String> errors = new ArrayList<String>();
					errors.add("can't parse response");
					errors.add(exc.getMessage());
					handleErrors(errors);
				}
			}
		}
	}
	
	public boolean sendMessage(String messageText) {
		QBChatMessage chatMessage = new QBChatMessage();
		DefaultPacketExtension extension = new DefaultPacketExtension("client", "jabber:client");
		extension.setValue("dialog_id", mDialog.getDialogId());
		Date date = new Date();
		extension.setValue("date_sent", "" + date.getTime());
		chatMessage.setBody(messageText);
		chatMessage.setProperty("save_to_history", "1");
		try {
			//sendMessage(chatMessage);
			mChat.sendMessage(chatMessage);
			return true;
		} catch (XMPPException e) {
			Log.e(TAG, "failed to send a message", e);
		} catch (SmackException sme) {
			Log.e(TAG, "failed to send a message", sme);
		}
		return false;
	}

	@Override
	public void processError(QBChat arg0, QBChatException arg1, QBChatMessage arg2) {
		Log.d("###", "processError: " + arg0);
		
	}

	@Override
	public void processMessage(QBChat chat, QBChatMessage message) {
		ChatMessage msg = new ChatMessage(message.getId(), message.getDialogId(), message.getBody(),
				message.getSenderId(), message.getRecipientId(), 
				DateFormat.format("yyyy-MM-dd hh:mm:ss", new Date().getTime()).toString());
		
		history.add(msg);
		
		Intent intent = new Intent(BROADCAST_ACTION);
	    intent.putExtra(MSG_ID, INCOMMING_MESSAGE);
	    intent.putExtra(MSG_MESSAGE_ID, msg.getId());
	    intent.putExtra(MSG_DATA, msg.getBody());
	    intent.putExtra(MSG_DIALOG_ID, msg.getDialogId());
	    intent.putExtra(MSG_SENDER, msg.getSenderId());
	    intent.putExtra(MSG_RECIPIENT, msg.getRecipientId());
	    intent.putExtra(MSG_DATE_SENT, msg.getDateSent());
	    
	    sendBroadcast(intent);
	}

	@Override
	public void processMessageDelivered(QBChat chat, String message) {	
	}
	
	public ArrayList<ChatMessage> getChatHistory() {
		if (initDone) {
			return history;
		}
		return null;
	} 
	
	public QBUser getUser() {
		return mUser;
	} 
}
