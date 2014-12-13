package com.octoberry.rcbankmobile.chat;

import java.util.Collection;

import com.quickblox.module.chat.model.QBAttachment;
import com.quickblox.module.chat.model.QBMessage;

public class ChatMessage extends QBMessage {
	private String message;
	private Integer senderId;
	private Integer recipientId;
	private String dateSent;
	private String dialogId;
	private String id;
	
	public ChatMessage(String id, String dialogId, String message, Integer senderId, Integer recipientId, String dateSent) {
		this.dateSent = dateSent;
		this.dialogId = dialogId;
		this.message = message;
		this.recipientId = recipientId;
		this.senderId = senderId;
		this.id = id;
	}
	
	@Override
	public Collection<QBAttachment> getAttachments() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getBody() {
		return message;
	}
	@Override
	public String getDialogId() {
		return dialogId;
	}
	@Override
	public String getId() {
		return id;
	}
	@Override
	public String getProperty(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Collection<String> getPropertyNames() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Integer getRecipientId() {
		return recipientId;
	}
	@Override
	public Integer getSenderId() {
		return senderId;
	}
	
	public String getDateSent() {
		return dateSent;
	}
}
