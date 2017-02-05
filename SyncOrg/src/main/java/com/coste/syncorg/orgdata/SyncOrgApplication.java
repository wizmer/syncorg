package com.coste.syncorg.orgdata;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.coste.syncorg.synchronizers.Synchronizer;

public class SyncOrgApplication extends Application {
    
	private static SyncOrgApplication instance;

    public static Context getContext() {
        return instance;
    }
    SharedPreferences sharedPreferences;
    
    @Override
    public void onCreate() {
        super.onCreate();
    	instance = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        OrgDatabase.startDB(this);
        OrgFileParser.startParser(this);
    }

}
