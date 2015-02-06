package com.octoberry.rcbankmobile.handler;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ruinvtyu on 29.01.2015.
 */
public class UserAccount implements Parcelable {
    private String id = null;
    private String name = null;
    private String balance = null;
    private String number = null;
    private Double actualBalance = 0.0;
    private boolean isCard = false;
    private String info = null;
    private String currency = null;

    public String getId() {
        return id;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public void setId(String id) {
        this.id = id;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Double getActualBalance() {
        return actualBalance;
    }

    public void setActualBalance(Double actualBalance) {
        this.actualBalance = actualBalance;
    }

    public boolean isCard() {
        return isCard;
    }

    public void setCard(boolean isCard) {
        this.isCard = isCard;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle data = new Bundle();
        data.putString("id", id);
        data.putString("name", name);
        data.putString("balance", balance);
        data.putString("number", number);
        data.putDouble("actual_balance", actualBalance);
        data.putBoolean("is_card", isCard);
        data.putString("info", info);
        data.putString("currency", currency);
        dest.writeBundle(data);
    }

    public UserAccount() {
    }

    public UserAccount(Parcel src) {
        Bundle data = src.readBundle();
        id = data.getString("id", null);
        name = data.getString("name", null);
        balance = data.getString("balance", null);
        number = data.getString("number", null);
        actualBalance = data.getDouble("actual_balance", 0.0);
        isCard = data.getBoolean("is_card", false);
        info = data.getString("info", null);
        currency = data.getString("currency", null);
    }

    public static final Parcelable.Creator<UserAccount> CREATOR = new Parcelable.Creator<UserAccount>() {
        public UserAccount createFromParcel(Parcel in) {
            return new UserAccount(in);
        }

        public UserAccount[] newArray(int size) {
            return new UserAccount[size];
        }
    };
}
