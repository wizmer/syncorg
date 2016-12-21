package com.coste.syncorg.orgdata;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.coste.syncorg.BuildConfig;
import com.coste.syncorg.R;
import com.coste.syncorg.gui.wizard.wizards.NoSyncWizard;
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
    SharedPreferences sharedPreferences;
    
    @Override
    public void onCreate() {
        super.onCreate();
    	instance = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        OrgDatabase.startDB(this);
        OrgFileParser.startParser(this);
        SyncService.startAlarm(this);
    }

}
