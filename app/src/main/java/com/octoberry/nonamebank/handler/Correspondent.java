package com.octoberry.nonamebank.handler;

import android.content.res.Resources;
import android.util.Log;

import com.octoberry.nonamebank.App;
import com.octoberry.nonamebank.R;

import org.json.JSONObject;

/**
 * Created by ruinvtyu on 30.01.2015.
 */
public class Correspondent {
    private final static String TAG = Correspondent.class.getName();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getBankBik() {
        return bankBik;
    }

    public void setBankBik(String bankBik) {
        this.bankBik = bankBik;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public String getCutName() {
        return cutName;
    }

    public void setCutName(String cutName) {
        this.cutName = cutName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getBankPlace() {
        return bankPlace;
    }

    public void setBankPlace(String bankPlace) {
        this.bankPlace = bankPlace;
    }

    public String getKpp() {
        return kpp;
    }

    public void setKpp(String kpp) {
        this.kpp = kpp;
    }

    public String getBankCorrAccount() {
        return bankCorrAccount;
    }

    public void setBankCorrAccount(String bankCorrAccount) {
        this.bankCorrAccount = bankCorrAccount;
    }

    private String name = null;
    private String date = null;
    private String bankBik = null;
    private String bankName = null;
    private String description = null;
    private String inn = null;
    private String cutName = null;
    private String accountNumber = null;
    private String bankPlace = null;
    private String kpp = null;
    private String bankCorrAccount = null;
    private int nds = 0;

    public int getNds() {
        return nds;
    }

    public void setNds(int nds) {
        this.nds = nds;
    }

    public String getLetter() {
        if ((cutName != null) && (cutName.length() > 0)) {
            return cutName.substring(0, 1);
        }
        return null;
    }

    public String getDetails(String message) {
        if (date != null) {
            return date + ", " + message;
        }
        return null;
    }

    public static Correspondent createFromJSON(JSONObject corrObject) {
        Correspondent result = new Correspondent();

        try {
            if (!corrObject.isNull("corr_name")) {
                result.setName(corrObject.getString("corr_name"));
            }
            if (!corrObject.isNull("corr_bank_bik")) {
                result.setBankBik(corrObject.getString("corr_bank_bik"));
            }
            if (!corrObject.isNull("corr_bank_name")) {
                result.setBankName(corrObject.getString("corr_bank_name"));
            }
            if (!corrObject.isNull("corr_inn")) {
                result.setInn(corrObject.getString("corr_inn"));
            }
            if (!corrObject.isNull("corr_cutname")) {
                result.setCutName(corrObject.getString("corr_cutname"));
            }
            if (!corrObject.isNull("corr_account_number")) {
                result.setAccountNumber(corrObject.getString("corr_account_number"));
            }
            if (!corrObject.isNull("corr_bank_place")) {
                result.setBankPlace(corrObject.getString("corr_bank_place"));
            }
            if (!corrObject.isNull("corr_kpp")) {
                result.setKpp(corrObject.getString("corr_kpp"));
            }
            if (!corrObject.isNull("corr_bank_corr_account")) {
                result.setBankCorrAccount(corrObject.getString("corr_bank_corr_account"));
            }
            if (!corrObject.isNull("date")) {
                result.setDate(corrObject.getString("date"));
            }
            if (!corrObject.isNull("description")) {
                result.setDescription(corrObject.getString("description"));
            }
            if (!corrObject.isNull("nds_text")) {
                String ndsText = corrObject.getString("nds_text");
                int ndsValue = 0;
                Resources resources = App.getInstance().getApplicationContext().getResources();
                if (ndsText.equals(resources.getString(R.string.NDS_10P))) {
                    ndsValue = 10;
                } else if (ndsText.equals(resources.getString(R.string.NDS_18P))) {
                    ndsValue = 18;
                }
                result.setNds(ndsValue);
            }
        } catch (Exception exc) {
            Log.e(TAG, "invalid JSON Object: " + exc.getMessage());
        }

        return result;
    }
}
