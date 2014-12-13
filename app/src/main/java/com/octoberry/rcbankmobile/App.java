package com.octoberry.rcbankmobile;

import com.deploygate.sdk.DeployGate;
import com.octoberry.rcbankmobile.chat.ChatService;
import com.octoberry.rcbankmobile.db.DataBaseManager;
import com.quickblox.core.QBSettings;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class App extends Application {
	public static final String PROJECT_NUMBER = "764899792444";
		
	private static App instance;

    public static App getInstance() {
        return instance;
    }
    
	@Override
	public void onCreate() {
		super.onCreate();
		DeployGate.install(this, null, true);

		initApplication();
	}
	
	private void initApplication() {
        instance = this;
        clearSharedPrefences();
        QBSettings.getInstance().fastConfigInit("14696", "2vghAtCu5uMpCHq", "BGnMkR3ZX4dq9hq");
        if (DataBaseManager.getInstance(this).getPhoneNumber() != null) {
        	startService(new Intent(this, ChatService.class));
        }
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