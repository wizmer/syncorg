package com.coste.syncorg.orgdata;

import android.app.Application;
import android.content.Context;
import android.preference.PreferenceManager;

import com.coste.syncorg.services.SyncService;
import com.coste.syncorg.synchronizers.NullSynchronizer;
import com.coste.syncorg.synchronizers.SDCardSynchronizer;
import com.coste.syncorg.synchronizers.SSHSynchronizer;
import com.coste.syncorg.synchronizers.Synchronizer;

public class SyncOrgApplication extends Application {
    
	private static SyncOrgApplication instance;

    public static Context getContext() {
        return instance;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
    	instance = this;

        OrgDatabase.startDB(this);
        startSynchronizer();
        OrgFileParser.startParser(this);
        SyncService.startAlarm(this);
    }

    public void startSynchronizer() {
        String syncSource =
                PreferenceManager
                        .getDefaultSharedPreferences(getApplicationContext())
                        .getString("syncSource", "");
        Context c = getApplicationContext();


        if (syncSource.equals("sdcard"))
            Synchronizer.setInstance(new SDCardSynchronizer(c));
        else if (syncSource.equals("scp"))
            Synchronizer.setInstance(new SSHSynchronizer(c));
        else
            Synchronizer.setInstance(new NullSynchronizer(c));
    }
}
