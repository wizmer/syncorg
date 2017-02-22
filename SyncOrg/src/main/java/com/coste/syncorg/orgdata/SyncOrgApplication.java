package com.coste.syncorg.orgdata;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SyncOrgApplication extends Application {

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
