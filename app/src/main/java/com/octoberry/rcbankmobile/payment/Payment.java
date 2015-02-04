package com.octoberry.rcbankmobile.payment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.ArrayList;

public class Payment {
	public static final String PAYMENT = "payment";

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getCorrName() {
        return corrName;
    }

    public void setCorrName(String corrName) {
        this.corrName = corrName;
    }

    public String getCorrBankBik() {
        return corrBankBik;
    }

    public void setCorrBankBik(String corrBankBik) {
        this.corrBankBik = corrBankBik;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCorrBankName() {
        return corrBankName;
    }

    public void setCorrBankName(String corrBankName) {
        this.corrBankName = corrBankName;
    }

    public String getOperType() {
        return operType;
    }

    public void setOperType(String operType) {
        this.operType = operType;
    }

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public String getBankPlace() {
        return bankPlace;
    }

    public void setBankPlace(String bankPlace) {
        this.bankPlace = bankPlace;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBankCorrAccount() {
        return bankCorrAccount;
    }

    public void setBankCorrAccount(String bankCorrAccount) {
        this.bankCorrAccount = bankCorrAccount;
    }

    public Boolean getSigned() {
        return signed;
    }

    public void setSigned(Boolean signed) {
        this.signed = signed;
    }

    public String getAmountWords() {
        return amountWords;
    }

    public void setAmountWords(String amountWords) {
        this.amountWords = amountWords;
    }

    public String getRegisterStamp() {
        return registerStamp;
    }

    public void setRegisterStamp(String registerStamp) {
        this.registerStamp = registerStamp;
    }

    public String getCorrInn() {
        return corrInn;
    }

    public void setCorrInn(String corrInn) {
        this.corrInn = corrInn;
    }

    public String getCorrCutName() {
        return corrCutName;
    }

    public void setCorrCutName(String corrCutName) {
        this.corrCutName = corrCutName;
    }

    public String getBankBik() {
        return bankBik;
    }

    public void setBankBik(String bankBik) {
        this.bankBik = bankBik;
    }

    public String getCorrAccountNumber() {
        return corrAccountNumber;
    }

    public void setCorrAccountNumber(String corrAccountNumber) {
        this.corrAccountNumber = corrAccountNumber;
    }

    public String getUrgentType() {
        return urgentType;
    }

    public void setUrgentType(String urgentType) {
        this.urgentType = urgentType;
    }

    public String getCorrBankPlace() {
        return corrBankPlace;
    }

    public void setCorrBankPlace(String corrBankPlace) {
        this.corrBankPlace = corrBankPlace;
    }

    public String getCorrKpp() {
        return corrKpp;
    }

    public void setCorrKpp(String corrKpp) {
        this.corrKpp = corrKpp;
    }

    public String getKpp() {
        return kpp;
    }

    public void setKpp(String kpp) {
        this.kpp = kpp;
    }

    public String getCorrBankCorrAccount() {
        return corrBankCorrAccount;
    }

    public void setCorrBankCorrAccount(String corrBankCorrAccount) {
        this.corrBankCorrAccount = corrBankCorrAccount;
    }

    public String getDeclineStamp() {
        return declineStamp;
    }

    public void setDeclineStamp(String declineStamp) {
        this.declineStamp = declineStamp;
    }

    public String getSendTypeCaption() {
        return sendTypeCaption;
    }

    public void setSendTypeCaption(String sendTypeCaption) {
        this.sendTypeCaption = sendTypeCaption;
    }

    public String getNdsText() {
        return ndsText;
    }

    public void setNdsText(String ndsText) {
        this.ndsText = ndsText;
    }

    public Integer getNds() {
        return nds;
    }

    public void setNds(Integer nds) {
        this.nds = nds;
    }

    private String number = null;
    private String date = null;
    private String name = null;
    private String description = null;
    private String bankName = null;
    private String corrName = null;
    private String corrBankBik = null;
    private Long id = 0l;
    private Double amount = 0.0;
    private String corrBankName = null;
    private String operType = null;
    private String inn = null;
    private String bankPlace = null;
    private String accountNumber = null;
    private String accountId = null;
    private String status = null;
    private String bankCorrAccount = null;
    private Boolean signed = false;
    private String amountWords = null;
    private String registerStamp = null;
    private String corrInn = null;
    private String corrCutName = null;
    private String bankBik = null;
    private String corrAccountNumber = null;
    private String urgentType = null;
    private String corrBankPlace = null;
    private String corrKpp = null;
    private String kpp = null;
    private String corrBankCorrAccount = null;
    private String declineStamp = null;
    private String sendTypeCaption = null;
    private String ndsText = null;
    private int nds = 0;

    public static void clearPreference(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PAYMENT, 0);
		Editor editor = pref.edit();
        editor.clear();
		editor.commit();
	}
	
	public void addToPreference(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PAYMENT, 0);
		Editor editor = pref.edit();
		editor.putString("number", number);
        editor.putString("date", date);
		editor.putString("name", name);
		editor.putString("description", description);
		editor.putString("bankName", bankName);
		editor.putString("corrName", corrName);
		editor.putString("corrBankBik", corrBankBik);
		editor.putLong("id", id);
        if (amount < 0) {
            editor.putLong("amountFirst", amount.longValue());
            editor.putLong("amountLast", (amount.longValue() * 100) - (long)(amount * 100));
        } else {
            editor.putLong("amountFirst", amount.longValue());
            editor.putLong("amountLast", (long)(amount * 100) - (amount.longValue() * 100));
        }
		editor.putString("corrBankName", corrBankName);
		editor.putString("operType", operType);
		editor.putString("inn", inn);
        editor.putString("bankPlace", bankPlace);
        editor.putString("accountNumber", accountNumber);
        editor.putString("accountId", accountId);
        editor.putString("status", status);
        editor.putString("bankCorrAccount", bankCorrAccount);
        editor.putBoolean("signed", signed);
        editor.putString("amountWords", amountWords);
        editor.putString("registerStamp", registerStamp);
        editor.putString("corrInn", corrInn);
        editor.putString("corrCutName", corrCutName);
        editor.putString("bankBik",bankBik );
        editor.putString("corrAccountNumber", corrAccountNumber);
        editor.putString("urgentType", urgentType);
        editor.putString("corrBankPlace", corrBankPlace);
        editor.putString("corrKpp", corrKpp);
        editor.putString("kpp", kpp);
        editor.putString("corrBankCorrAccount", corrBankCorrAccount);
        editor.putString("declineStamp", declineStamp);
        editor.putString("sendTypeCaption", sendTypeCaption);
        editor.putString("ndsText", ndsText);
        editor.putInt("nds", nds);
		editor.commit();
	}
	
	public static Payment createFromPreference(Context context) {
		Payment payment = new Payment();
		SharedPreferences pref = context.getSharedPreferences(PAYMENT, 0);
		payment.setNumber(pref.getString("number", null));
        payment.setDate(pref.getString("date", null));
		payment.setName(pref.getString("name", null));
		payment.setDescription(pref.getString("description", null));
		payment.setBankName(pref.getString("bankName", null));
		payment.setCorrName(pref.getString("corrName", null));
		payment.setCorrBankBik(pref.getString("corrBankBik", null));
		payment.setId(pref.getLong("id", 0));
        payment.setAmount(new Double("" + pref.getLong("amountFirst", 0) + "." + pref.getLong("amountLast", 0)));
        payment.setCorrBankName(pref.getString("corrBankName", null));
		payment.setOperType(pref.getString("operType", null));
		payment.setInn(pref.getString("inn", null));
		payment.setBankPlace(pref.getString("bankPlace", null));
        payment.setCorrAccountNumber(pref.getString("accountNumber", null));
        payment.setAccountId(pref.getString("accountId", null));
        payment.setStatus(pref.getString("status", null));
        payment.setBankCorrAccount(pref.getString("bankCorrAccount", null));
        payment.setSigned(pref.getBoolean("signed", false));
        payment.setAmountWords(pref.getString("amountWords", null));
        payment.setRegisterStamp(pref.getString("registerStamp", null));
        payment.setCorrInn(pref.getString("corrInn", null));
        payment.setCorrCutName(pref.getString("corrCutName", null));
        payment.setBankBik(pref.getString("bankBik", null));
        payment.setAccountNumber(pref.getString("corrAccountNumber", null));
        payment.setUrgentType(pref.getString("urgentType", null));
        payment.setCorrBankPlace(pref.getString("corrBankPlace", null));
        payment.setCorrKpp(pref.getString("corrKpp", null));
        payment.setKpp(pref.getString("kpp", null));
        payment.setCorrBankCorrAccount(pref.getString("corrBankCorrAccount", null));
        payment.setDeclineStamp(pref.getString("declineStamp", null));
        payment.setSendTypeCaption(pref.getString("sendTypeCaption", null));
        payment.setNdsText(pref.getString("ndsText", null));
        payment.setNds(pref.getInt("nds", 0));

		return payment;
	}

    public ArrayList<String> getValues() {
        ArrayList<String> result = new ArrayList<String>();
        result.add("" + amount.doubleValue());
        result.add(date);
        result.add(description);
        result.add(bankName);
        result.add(corrName);
        result.add(corrBankBik);
        result.add("" + id);
        result.add(corrBankName);
        result.add(name);
        result.add(operType);
        result.add(inn);
        result.add(bankPlace);
        result.add(accountNumber);
        result.add(accountId);
        result.add(status);
        result.add(bankCorrAccount);
        result.add("" + signed);
        result.add(amountWords);
        result.add(number);
        result.add(registerStamp);
        result.add(corrInn);
        result.add(corrCutName);
        result.add(bankBik);
        result.add(corrAccountNumber);
        result.add(urgentType);
        result.add(corrBankPlace);
        result.add(corrKpp);
        result.add(kpp);
        result.add(corrBankCorrAccount);
        result.add(declineStamp);
        result.add(sendTypeCaption);
        result.add(ndsText);
        return result;
    }

    public static ArrayList<String> getHeaders() {
        ArrayList<String> result = new ArrayList<String>();
        result.add("amount");
        result.add("date");
        result.add("description");
        result.add("bank_name");
        result.add("corr_name");
        result.add("corr_bank_bik");
        result.add("id");
        result.add("corr_bank_name");
        result.add("name");
        result.add("opertype");
        result.add("inn");
        result.add("bank_place");
        result.add("account_number");
        result.add("account_id");
        result.add("status");
        result.add("bank_corr_account");
        result.add("signed");
        result.add("amount_words");
        result.add("number");
        result.add("register_stamp");
        result.add("corr_inn");
        result.add("corr_cutname");
        result.add("bank_bik");
        result.add("corr_account_number");
        result.add("urgenttype");
        result.add("corr_bank_place");
        result.add("corr_kpp");
        result.add("kpp");
        result.add("corr_bank_corr_account");
        result.add("decline_stamp");
        result.add("sendtype_caption");
        result.add("nds_text");
        return result;
    }
	
	public Payment() {
		
	}
}
