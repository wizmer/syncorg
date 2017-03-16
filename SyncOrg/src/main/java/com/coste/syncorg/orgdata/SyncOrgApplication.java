package com.coste.syncorg.orgdata;

import android.support.multidex.MultiDexApplication;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SyncOrgApplication extends MultiDexApplication {

    private static SyncOrgApplication instance;
    SharedPreferences sharedPreferences;

    public static Context getContext() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        OrgDatabase.startDB(this);
        OrgFileParser.startParser(this);
    }

}
