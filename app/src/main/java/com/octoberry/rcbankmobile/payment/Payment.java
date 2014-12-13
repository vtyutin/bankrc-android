package com.octoberry.rcbankmobile.payment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Parcel;
import android.os.Parcelable;

public class Payment implements Parcelable {
	public static final String PAYMENT = "payment";
	
	private String accountNumber = null;
	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getCorrName() {
		return corrName;
	}

	public void setCorrName(String corrName) {
		this.corrName = corrName;
	}

	public String getCorrAccount() {
		return corrAccount;
	}

	public void setCorrAccount(String corrAccount) {
		this.corrAccount = corrAccount;
	}

	public String getCorrInn() {
		return corrInn;
	}

	public void setCorrInn(String corrInn) {
		this.corrInn = corrInn;
	}

	public String getCorrKpp() {
		return corrKpp;
	}

	public void setCorrKpp(String corrKpp) {
		this.corrKpp = corrKpp;
	}

	public String getCorrBik() {
		return corrBik;
	}

	public void setCorrBik(String corrBik) {
		this.corrBik = corrBik;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getNds() {
		return nds;
	}

	public void setNds(int nds) {
		this.nds = nds;
	}

	public boolean isNdsIncluded() {
		return ndsIncluded;
	}

	public void setNdsIncluded(boolean ndsIncluded) {
		this.ndsIncluded = ndsIncluded;
	}

	private long amountFirst = 0;
	public long getAmountFirst() {
		return amountFirst;
	}

	public void setAmountFirst(long amountFirst) {
		this.amountFirst = amountFirst;
	}

	public long getAmountLast() {
		return amountLast;
	}

	public void setAmountLast(long amountLast) {
		this.amountLast = amountLast;
	}

	private long amountLast = 0;
	private String corrName = null;
	private String corrAccount = null;
	private String corrInn = null;
	private String corrKpp = null;
	private String corrBik = null;
	private String description = null;
	private int nds = 0;
	private boolean ndsIncluded = false;
		
	public static void clearPreference(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PAYMENT, 0);
		Editor editor = pref.edit();
		editor.remove("account_number");
		editor.remove("amount_first");
		editor.remove("amount_last");
		editor.remove("corr_name");
		editor.remove("corr_account");
		editor.remove("corr_inn");
		editor.remove("corr_kpp");
		editor.remove("corr_bik");
		editor.remove("description");
		editor.remove("nds");
		editor.remove("nds_included");
		editor.commit();
	}
	
	public void addToPreference(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PAYMENT, 0);
		Editor editor = pref.edit();
		editor.putString("account_number", accountNumber);
		editor.putLong("amount_first", amountFirst);
		editor.putLong("amount_last", amountLast);
		editor.putString("corr_name", corrName);
		editor.putString("corr_account", corrAccount);
		editor.putString("corr_inn", corrInn);
		editor.putString("corr_kpp", corrKpp);
		editor.putString("corr_bik", corrBik);
		editor.putString("description", description);
		editor.putInt("nds", nds);
		editor.putBoolean("nds_included", ndsIncluded);
		editor.commit();
	}
	
	public static Payment createFromPreference(Context context) {
		Payment payment = new Payment();
		SharedPreferences pref = context.getSharedPreferences(PAYMENT, 0);
		payment.setAccountNumber(pref.getString("account_number", null));
		payment.setAmountFirst(pref.getLong("amount_first", 0));
		payment.setAmountLast(pref.getLong("amount_last", 0));
		payment.setCorrName(pref.getString("corr_name", null));
		payment.setCorrAccount(pref.getString("corr_account", null));
		payment.setCorrInn(pref.getString("corr_inn", null));
		payment.setCorrKpp(pref.getString("corr_kpp", null));
		payment.setCorrBik(pref.getString("corr_bik", null));
		payment.setDescription(pref.getString("description", null));
		payment.setNds(pref.getInt("nds", 0));
		payment.setNdsIncluded(pref.getBoolean("nds_included", false));
		return payment;
	}
	
	public Payment() {
		
	}
	
	public Payment(Parcel parcel) {
		accountNumber = parcel.readString();
		amountFirst = parcel.readLong();
		amountLast = parcel.readLong();
		corrName = parcel.readString();
		corrAccount = parcel.readString();
		corrInn = parcel.readString();
		corrKpp = parcel.readString();
		corrBik = parcel.readString();
		description = parcel.readString();
		nds = parcel.readInt();
		ndsIncluded = parcel.readInt() == 1 ? true : false;
	}
		
	@Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    	dest.writeString(accountNumber);
    	dest.writeLong(amountFirst);
    	dest.writeLong(amountLast);
    	dest.writeString(corrName);
    	dest.writeString(corrAccount);
    	dest.writeString(corrInn);
    	dest.writeString(corrKpp);
    	dest.writeString(corrBik);
    	dest.writeString(description);
    	dest.writeInt(nds);
    	dest.writeInt(ndsIncluded ? 1 : 0);
    }
    
    public static final Parcelable.Creator<Payment> CREATOR = new Parcelable.Creator<Payment>() {
        public Payment createFromParcel(Parcel in) {
            return new Payment(in); 
        }

        public Payment[] newArray(int size) {
            return new Payment[size];
        }
    };
}
