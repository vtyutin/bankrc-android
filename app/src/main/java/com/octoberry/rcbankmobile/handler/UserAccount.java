package com.octoberry.rcbankmobile.handler;

/**
 * Created by ruinvtyu on 29.01.2015.
 */
public class UserAccount {
    private String id = null;
    private String balance = null;
    private String number = null;
    private Double actualBalance = 0.0;
    private boolean isCard = false;
    private String info = null;
    private String currency = null;

    public String getId() {
        return id;
    }

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
}
