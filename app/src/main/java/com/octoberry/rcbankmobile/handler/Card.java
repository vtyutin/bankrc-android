package com.octoberry.rcbankmobile.handler;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class Card implements Parcelable {
	private static final int DATA_SIZE = 10;
	
	public static final int TYPE_INVALID 		= -1;
	public static final int TYPE_TEXT 			= 0;
	public static final int TYPE_SIGNATURE 		= 1;
	public static final int TYPE_SEPARATOR 		= 2;
	public static final int TYPE_CHECK_LIST 	= 3;
	public static final int TYPE_CREDS 			= 4;
	public static final int TYPE_EVENT 			= 5;
	public static final int TYPE_BUTTON 		= 6;
	public static final int TYPE_PASSPORTS 		= 7;
	private int type = TYPE_INVALID;
	private String description = null;
	private String title = null;
	private String creds = null;
	private String action = null;
	private String address = null;
	private String calendarTitle = null;
	private boolean completed = false;
	private Date startDate = null;
	private Date endDate = null;
	private Document[] documents = null;

	public Card(int type) {
		this.type = type;
	}		
	
	public Card(Parcel parcel) {
		String[] data = new String[DATA_SIZE];
		parcel.readStringArray(data);
        this.type = Integer.parseInt(data[0]);
        this.description = data[1];
        this.title = data[2];
        this.creds = data[3];
        this.action = data[4];
        this.address = data[5];
        this.calendarTitle = data[6];
        this.completed = Boolean.getBoolean(data[7]);
        if (data[8] != null) {
        	long date = Long.parseLong(data[8]);
        	this.startDate = new Date(date);
        }
        if (data[9] != null) {
        	long date = Long.parseLong(data[9]);
        	this.endDate = new Date(date);
        }
        int documentsSize = parcel.readInt();
        if (documentsSize > 0) {
        	documents = new Document[documentsSize];
        	parcel.readTypedArray(documents, Document.CREATOR);
        }                
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		switch (type) {
		case TYPE_BUTTON:
			builder.append("type: button ");
			break;
		case TYPE_CHECK_LIST:
			builder.append("type: check_list ");
			break;
		case TYPE_CREDS:
			builder.append("type: creds ");
			break;
		case TYPE_EVENT:
			builder.append("type: event ");
			break;
		case TYPE_SEPARATOR:
			builder.append("type: separator ");
			break;
		case TYPE_SIGNATURE:
			builder.append("type: signature ");
			break;
		case TYPE_TEXT:
			builder.append("type: text ");
			break;
		case TYPE_PASSPORTS:
			builder.append("type: passports ");
			break;	
		default:
			builder.append("type: unknown ");
			break;
		}
		
		if (title != null) {
			builder.append("title: " + title + " ");
		}
		if (description != null) {
			builder.append("description: " + description + " ");
		}
		if (action != null) {
			builder.append("action: " + action + " ");
		}
		if (creds != null) {
			builder.append("creds: " + creds + " ");
		}		
		if (address != null) {
			builder.append("address: " + address + " ");
		}
		if (calendarTitle != null) {
			builder.append("calendarTitle: " + calendarTitle + " ");
		}		
		builder.append("completed: " + completed + " ");
		
		if (startDate != null) {
			builder.append("startDate: " + startDate + " ");
		}
		
		if (endDate != null) {
			builder.append("endDate: " + endDate + " ");
		}
		
		return builder.toString();
	}
	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCalendarTitle() {
		return calendarTitle;
	}

	public void setCalendarTitle(String calendarTitle) {
		this.calendarTitle = calendarTitle;
	}
	
	public int getType() {
		return type;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getCreds() {
		return creds;
	}
	public void setCreds(String creds) {
		this.creds = creds;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public boolean isCompleted() {
		return completed;
	}
	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public Document[] getDocuments() {
		return documents;
	}
	public void setDocuments(Document[] documents) {
		this.documents = documents;
	}

	@Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {    	
    	String[] data = new String[DATA_SIZE];
    	data[0] = "" + type;
    	data[1] = description;
    	data[2] = title;
    	data[3] = creds;
    	data[4] = action;
    	data[5] = address;
    	data[6] = calendarTitle;
    	data[7] = ((Boolean)completed).toString();
    	if (startDate != null) {
    		data[8] = "" + startDate.getTime();
     	} else {
     		data[8] = null;
     	}
    	if (endDate != null) {
    		data[9] = "" + endDate.getTime();
     	} else {
     		data[9] = null;
     	}
    	dest.writeStringArray(data);
    	if (documents != null) {
    		dest.writeInt(documents.length);
    		dest.writeTypedArray(documents, 0);
    	} else {
    		dest.writeInt(0);
    	}
    }
    
    public static final Parcelable.Creator<Card> CREATOR = new Parcelable.Creator<Card>() {
        public Card createFromParcel(Parcel in) {
            return new Card(in); 
        }

        public Card[] newArray(int size) {
            return new Card[size];
        }
    };
}
