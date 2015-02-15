package com.octoberry.nonamebank.chat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.json.JSONObject;

import com.octoberry.nonamebank.R;
import com.octoberry.nonamebank.db.DataBaseManager;
import com.octoberry.nonamebank.gcm.PlayServicesHelper;
import com.octoberry.nonamebank.net.AsyncJSONLoader;
import com.octoberry.nonamebank.net.JSONResponseListener;
import com.quickblox.core.QBEntityCallback;
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
import com.quickblox.module.chat.model.QBAttachment;
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
    public final static String MSG_ATTACHMENT_ID = "attachment_id";
    public final static String MSG_ATTACHMENT_URL = "attachment_url";
    public final static String MSG_ATTACHMENT_TYPE = "attachment_type";
	
	private ChatTokenHandler mChatTokenHandler = new ChatTokenHandler();
	
	private QBUser mUser;
	private QBDialog mDialog = null;	
	private QBGroupChat mChat;

    private static final int INVALID_REQUEST = -1;
    private static final int LOGIN_CHAT = 0;
    private static final int JOIN_CHAT = 1;
    private int mRequestType = INVALID_REQUEST;

    private static final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
	
	boolean initDone = false;
    boolean initializationInprogress = false;
	
	private PlayServicesHelper playServicesHelper;
    private ChatCallBackListener mChatCallbakListener = new ChatCallBackListener();

    private ChatDBManager dbManager = null;

	@Override
	public IBinder onBind(Intent arg0) {
		Log.d("###", "service bind");
		if (!initDone) {
            if (!initializationInprogress) {
                startChat();
            }
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
        if (initDone) {
            getChat();
        } else {
            if (!initializationInprogress) {
                startChat();
            }
        }
		return super.onStartCommand(intent, flags, startId);
	}

    @Override
    public void onDestroy() {
        worker.shutdown();
        super.onDestroy();
    }

    @Override
	public void onCreate() {
        dbManager = ChatDBManager.getInstance(getApplicationContext());
		playServicesHelper = new PlayServicesHelper(this);
		super.onCreate();
	}

    private void startChat() {
            Log.d("###", "token: " + DataBaseManager.getInstance(this).getCrmToken());
            Log.d("###", "phone_number: " + DataBaseManager.getInstance(this).getPhoneNumber());
            AsyncJSONLoader loader = new AsyncJSONLoader(this);
            loader.registryListener(mChatTokenHandler);
            Bundle params = new Bundle();
            params.putString("requestType", "GET");
            params.putString("endpoint", "/api/user");
            Bundle headerParams = new Bundle();
            headerParams.putString("Authorization", DataBaseManager.getInstance(this).getCrmToken());
            headerParams.putString("phone_number", DataBaseManager.getInstance(this).getPhoneNumber());
            loader.execute(params, headerParams, null);
        //}
        initializationInprogress = true;
    }

    class ChatCallBackListener implements QBEntityCallback {

        @Override
        public void onSuccess(Object o, Bundle bundle) {
            if (o instanceof QBSession) {
                QBSession session = (QBSession)o;
                Log.i("###", "session created, token = " + session.getToken());
                playServicesHelper.registerInBackground();
                loginChat();
            }
        }

        @Override
        public void onSuccess() {
            switch (mRequestType) {
                case LOGIN_CHAT:
                    getDialog();
                    break;
                case JOIN_CHAT:
                    mChat.addMessageListener(ChatService.this);
                    loadChatHistory();
                    break;
            }
            mRequestType = INVALID_REQUEST;
        }

        @Override
        public void onError(List list) {
            handleErrors(list);
        }
    }
	
	void initChatSession() {
		QBAuth.createSession(mUser, mChatCallbakListener);
	}

	void loginChat() {
		Log.d("###", "login chat");
				
		if (!QBChatService.isInitialized()) {
			QBChatService.init(this);
		}		
		
		if (!QBChatService.getInstance().isLoggedIn()) {
            mRequestType = LOGIN_CHAT;
            QBChatService.getInstance().login(mUser, mChatCallbakListener);
		} else {
			getDialog();	        
		}		
	}
	
	void getDialog() {
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
							occupants.add(mUser.getId());
							occupants.add(Integer.valueOf(getResources().getString(R.string.chat_opponent_id)));
							dialog.setOccupantsIds(occupants);
							dialog.setType(QBDialogType.GROUP);
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
							occupants.add(mUser.getId());
							occupants.add(Integer.valueOf(getResources().getString(R.string.chat_opponent_id)));
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
		if (mChat == null) {
			mChat = QBChatService.getInstance().getGroupChatManager().createGroupChat(mDialog.getRoomJid());
		}

        mRequestType = JOIN_CHAT;
        mChat.join(null, mChatCallbakListener);
	}
	
	private void loadChatHistory() {
        Log.d("###", "loadChatHistory");
		new DialogMessageLoader().execute();
	}

	private class DialogMessageLoader extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			QBCustomObjectRequestBuilder customObjectRequestBuilder = new QBCustomObjectRequestBuilder();
			try {

				ArrayList<QBChatHistoryMessage> messages = QBChatService.getDialogMessages(mDialog, customObjectRequestBuilder, new Bundle());
				Log.d("###", "loadChatHistory success");

				for (QBChatHistoryMessage message: messages) {
                    Integer senderId = mUser.getId();
                    try {
                        senderId = message.getSenderId();
                    } catch (Exception exc) {
                        Log.e(TAG, "invalid sender id: " + exc.getMessage());
                    }
                    ChatMessage chatMwssage = new ChatMessage(message.getId(), message.getDialogId(), message.getBody(),
                            senderId, message.getRecipientId(),
                            "" + message.getDateSent());
					if ((message.getAttachments() != null) && (message.getAttachments().size() > 0)) {
                        QBAttachment attachment = message.getAttachments().iterator().next();
                        chatMwssage.addAttachment(attachment);
                    }
                    dbManager.addMessage(chatMwssage, false);
                    processMessageInternal(chatMwssage);
				}

                Runnable task = new Runnable() {
                    public void run() {
                        if ((mChat != null) && (mChat.isJoined())) {
                            try {
                                Log.d("###", "send presence");
                                mChat.sendPresence(null);
                                worker.schedule(this, 60, TimeUnit.SECONDS);
                            } catch (XMPPException e) {
                                e.printStackTrace();
                            } catch (SmackException.NotConnectedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                worker.schedule(task, 60, TimeUnit.SECONDS);
				initDone = true;
                initializationInprogress = false;
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
	
	private void handleErrors(List errors) {
        initializationInprogress = false;
        String message = String.format("[ERROR] Request has been completed with errors: %s", errors);
		Log.e("###", message);
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
                                initializationInprogress = true;
                                return;
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
            initializationInprogress = false;
		}
	}
	
	public boolean sendMessage(String messageText, String attachmentId, String url) {
		boolean isTextMessage = true;
		QBChatMessage message = new QBChatMessage();
        if (messageText != null) {
            message.setBody(messageText);
        }
        message.setProperty("save_to_history", "1");
        message.setProperty("dialog_id", mDialog.getDialogId());
        if (attachmentId != null) {
            isTextMessage = false;
            QBAttachment attachment = new QBAttachment("photo");
            attachment.setId(attachmentId);
            attachment.setUrl(url);
            message.addAttachment(attachment);
        }
		try {
			//sendMessage(chatMessage);
            if (isTextMessage) {
                Log.d("###", "send text message: " + messageText);
                mChat.sendMessage(messageText);
            } else {
                mChat.sendMessage(message);
            }
			return true;
		} catch (XMPPException e) {
			Log.e(TAG, "failed to send a message", e);
		} catch (SmackException sme) {
			Log.e(TAG, "failed to send a message", sme);
		} catch (Exception exc) {
            Log.e(TAG, "failed to send a message", exc);
        }
		return false;
	}

	@Override
	public void processError(QBChat arg0, QBChatException arg1, QBChatMessage arg2) {
		Log.d("###", "processError: " + arg0);
	}

    @Override
    public void processMessage(QBChat chat, QBChatMessage message) {
        Integer senderId = mUser.getId();
        try {
            senderId = message.getSenderId();
        } catch (Exception exc) {
            Log.e(TAG, "invalid sender id: " + exc.getMessage());
        }
        ChatMessage msg = new ChatMessage(message.getId(), message.getDialogId(), message.getBody(),
                senderId, message.getRecipientId(),
                "" + new Date().getTime());

        if (message.getAttachments() != null) {
            if (message.getAttachments().iterator().hasNext()) {
                QBAttachment attachment = message.getAttachments().iterator().next();
                msg.addAttachment(attachment);
            }
        }
        processMessageInternal(msg);
    }

	public void processMessageInternal(ChatMessage msg) {
        dbManager.addMessage(msg, false);

		Intent intent = new Intent(BROADCAST_ACTION);
	    intent.putExtra(MSG_ID, INCOMMING_MESSAGE);
	    intent.putExtra(MSG_MESSAGE_ID, msg.getId());
	    intent.putExtra(MSG_DATA, msg.getBody());
	    intent.putExtra(MSG_DIALOG_ID, msg.getDialogId());
	    intent.putExtra(MSG_SENDER, msg.getSenderId());
	    intent.putExtra(MSG_RECIPIENT, msg.getRecipientId());
	    intent.putExtra(MSG_DATE_SENT, msg.getDateSent());
	    if (msg.getAttachments().size() > 0) {
            QBAttachment attachment = msg.getAttachments().iterator().next();
            intent.putExtra(MSG_ATTACHMENT_TYPE, attachment.getType());
            intent.putExtra(MSG_ATTACHMENT_ID, attachment.getId());
            intent.putExtra(MSG_ATTACHMENT_URL, attachment.getUrl());
        }
	    sendBroadcast(intent);
	}

	@Override
	public void processMessageDelivered(QBChat chat, String message) {
        Log.d("###", "processMessageDelivered: " + message);
	}
	
	public QBUser getUser() {
		return mUser;
	} 
}
