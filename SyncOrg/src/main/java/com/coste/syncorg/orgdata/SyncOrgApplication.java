package com.coste.syncorg.orgdata;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.coste.syncorg.BuildConfig;
import com.coste.syncorg.services.SyncService;
import com.coste.syncorg.synchronizers.NullSynchronizer;
import com.coste.syncorg.synchronizers.SDCardSynchronizer;
import com.coste.syncorg.synchronizers.SSHSynchronizer;
import com.coste.syncorg.synchronizers.Synchronizer;

import static android.R.attr.version;

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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String syncSource = sharedPreferences.getString("syncSource", "");
        int prevousVersion = sharedPreferences.getInt("VERSION_CODE", -1);

        if(prevousVersion < 4 && BuildConfig.VERSION_CODE >= 4){
            // TODO: update version number
            Log.v("version", "New version");
            Log.v("version", "sync "+syncSource);
            if(!syncSource.equals("sdcard") && !syncSource.equals("scp")){
                Log.v("version", "Need to do stuff");

            }


        }

        Context c = getApplicationContext();

        if (syncSource.equals("sdcard"))
            Synchronizer.setInstance(new SDCardSynchronizer(c));
        else if (syncSource.equals("scp"))
            Synchronizer.setInstance(new SSHSynchronizer(c));
        else
            Synchronizer.setInstance(new NullSynchronizer(c));
    }
}
