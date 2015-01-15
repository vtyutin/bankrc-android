package com.octoberry.rcbankmobile;

import com.deploygate.sdk.DeployGate;
import com.flurry.android.FlurryAgent;
import com.octoberry.rcbankmobile.chat.ChatService;
import com.octoberry.rcbankmobile.db.DataBaseManager;
import com.quickblox.core.QBSettings;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class App extends Application {
	public static final String PROJECT_NUMBER = "764899792444";
		
	private static App instance;

    public static App getInstance() {
        return instance;
    }

    private Handler mUploadHandler;
    
	@Override
	public void onCreate() {
		super.onCreate();
		DeployGate.install(this, null, true);

        FlurryAgent.setLogEnabled(false);
        FlurryAgent.init(this, getResources().getString(R.string.flurry_app_key));

		initApplication();
	}
	
	private void initApplication() {
        instance = this;
        clearSharedPrefences();
        QBSettings.getInstance().fastConfigInit("14696", "2vghAtCu5uMpCHq", "BGnMkR3ZX4dq9hq");
    }
	
	private void clearSharedPrefences() {
		SharedPreferences pref = getSharedPreferences("upload", 0);
		Editor edit = pref.edit();
		edit.remove("path_to_file");
		edit.remove("in_progress");
		edit.remove("upload_id");
		edit.commit();
	}
	
	/**
     * @return Application's version code from the {@code PackageManager}.
     */
    public int getAppVersion() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
}