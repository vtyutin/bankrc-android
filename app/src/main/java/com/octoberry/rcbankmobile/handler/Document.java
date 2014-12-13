package com.octoberry.rcbankmobile.handler;

import android.os.Parcel;
import android.os.Parcelable;

public class Document implements Parcelable {
	private String title = null;
	private boolean uploadable = false;
	private boolean completed = false;
	private boolean isFirstPageUploaded = false;
	private boolean isSecondPageUploaded = false;
	private String id = null;
	private String firstPageFilePath = null;
	private String secondPageFilePath = null;
	
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
		uploadable = parcel.readInt() == 1 ? true : false;
		id = parcel.readString();
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
    	dest.writeString(id);
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
