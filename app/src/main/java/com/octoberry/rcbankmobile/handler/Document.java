package com.octoberry.rcbankmobile.handler;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class Document implements Parcelable {
    public final static String TYPE_PASSPORT = "passport";
    public final static String TYPE_LEGAL = "legal";

	private String title = null;
	private boolean uploadable = false;
	private boolean completed = false;
	private boolean isFirstPageUploaded = false;
	private boolean isSecondPageUploaded = false;
	private String id = null;
	private String firstPageFilePath = null;
	private String secondPageFilePath = null;

    public String getFirstPageTitle() {
        return firstPageTitle;
    }

    public void setFirstPageTitle(String firstPageTitle) {
        this.firstPageTitle = firstPageTitle;
    }

    public String getSecondPageTitle() {
        return secondPageTitle;
    }

    public void setSecondPageTitle(String secondPageTitle) {
        this.secondPageTitle = secondPageTitle;
    }

    public String getFirstPageId() {
        return firstPageId;
    }

    public void setFirstPageId(String firstPageId) {
        this.firstPageId = firstPageId;
    }

    public String getSecondPageId() {
        return secondPageId;
    }

    public void setSecondPageId(String secondPageId) {
        this.secondPageId = secondPageId;
    }

    private String firstPageTitle = null;
    private String secondPageTitle = null;
    private String firstPageId = null;
    private String secondPageId = null;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private String type = null;
	
	public String getSecondPageFilePath() {
		return secondPageFilePath;
	}

	public void setSecondPageFilePath(String secondPageFilePath) {
		this.secondPageFilePath = secondPageFilePath;
	}

	public String getFirstPageFilePath() {
		return firstPageFilePath;
	}

	public void setFirstPageFilePath(String firstPageFilePath) {
		this.firstPageFilePath = firstPageFilePath;
	}
	
	public boolean isFirstPageUploaded() {
		return isFirstPageUploaded;
	}

	public void setFirstPageUploaded(boolean isFirstPageUploaded) {
		this.isFirstPageUploaded = isFirstPageUploaded;
	}

	public boolean isSecondPageUploaded() {
		return isSecondPageUploaded;
	}

	public void setSecondPageUploaded(boolean isSecondPageUploaded) {
		this.isSecondPageUploaded = isSecondPageUploaded;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public Document(String title) {
		this.title = title;
	}		
	
	public Document(Parcel parcel) {
		title = parcel.readString();
		uploadable = (parcel.readInt() == 1);
		Bundle data = parcel.readBundle();
        id = data.getString("id", null);
        type = data.getString("type", null);
        firstPageTitle = data.getString("firstPageTitle", null);
        firstPageId = data.getString("firstPageId", null);
        secondPageTitle = data.getString("secondPageTitle", null);
        secondPageId = data.getString("secondPageId", null);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (title != null) {
			builder.append("title: " + title + " ");
		}
		builder.append("uploadable: " + uploadable + " ");
		return builder.toString();
	}	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public boolean isUploadable() {
		return uploadable;
	}
	public void setUploadable(boolean uploadable) {
		this.uploadable = uploadable;
	}
	
	@Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
    	dest.writeInt(uploadable ? 1 : 0);
        Bundle data = new Bundle();
        data.putString("id", id);
        data.putString("type", type);
        data.putString("firstPageTitle", firstPageTitle);
        data.putString("firstPageId", firstPageId);
        data.putString("secondPageTitle", secondPageTitle);
        data.putString("secondPageId", secondPageId);
        dest.writeBundle(data);
    }
    
    public static final Parcelable.Creator<Document> CREATOR = new Parcelable.Creator<Document>() {
        public Document createFromParcel(Parcel in) {
            return new Document(in); 
        }

        public Document[] newArray(int size) {
            return new Document[size];
        }
    };
}
