package com.matburt.mobileorg.Services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.matburt.mobileorg.Parsing.MobileOrgApplication;
import com.matburt.mobileorg.Synchronizers.DropboxSynchronizer;
import com.matburt.mobileorg.Synchronizers.SDCardSynchronizer;
import com.matburt.mobileorg.Synchronizers.SSHSynchronizer;
import com.matburt.mobileorg.Synchronizers.Synchronizer;
import com.matburt.mobileorg.Synchronizers.WebDAVSynchronizer;
import com.matburt.mobileorg.Synchronizers.NullSynchronizer;

public class SyncService extends Service implements
		SharedPreferences.OnSharedPreferenceChangeListener {
	private static final String START_ALARM = "START_ALARM";

	private SharedPreferences appSettings;
	private MobileOrgApplication appInst;

	private AlarmManager alarmManager;
	private PendingIntent alarmIntent;
	private boolean alarmScheduled = false;

	private boolean syncRunning;

	@Override
	public void onCreate() {
		super.onCreate();
		this.appSettings = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		this.appSettings.registerOnSharedPreferenceChangeListener(this);
		this.appInst = (MobileOrgApplication) this.getApplication();
		this.alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	}

	@Override
	public void onDestroy() {
		unsetAlarm();
	}

	public static void startAlarm(Context context) {
		Intent intent = new Intent(context, SyncService.class);
		intent.putExtra("action", SyncService.START_ALARM);
		context.startService(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String action = intent.getStringExtra("action");
		if (action != null && action.equals(START_ALARM))
			setAlarm();
		else if(!this.syncRunning) {
			this.syncRunning = true;
			runSynchronizer();
		}
		return 0;
	}

    public Synchronizer getSynchronizer() {
        Synchronizer synchronizer = null;
		String syncSource = appSettings.getString("syncSource", "");
		if (syncSource.equals("webdav"))
			synchronizer = new WebDAVSynchronizer(getApplicationContext(), this.appInst);
		else if (syncSource.equals("sdcard"))
			synchronizer = new SDCardSynchronizer(getApplicationContext(), this.appInst);
		else if (syncSource.equals("dropbox"))
			synchronizer = new DropboxSynchronizer(getApplicationContext(), this.appInst);
		else if (syncSource.equals("scp"))
			synchronizer = new SSHSynchronizer(getApplicationContext(), this.appInst);
        else if (syncSource.equals("null"))
            synchronizer = new NullSynchronizer(getApplicationContext(), this.appInst);
		else
			synchronizer = null;
        return synchronizer;
    }

	private void runSynchronizer() {
		unsetAlarm();
		final Synchronizer synchronizer = this.getSynchronizer();

		Thread syncThread = new Thread() {
			public void run() {
				synchronizer.sync();
				synchronizer.close();
				syncRunning = false;
				setAlarm();
			}
		};

		syncThread.start();
	}


	private void setAlarm() {
		boolean doAutoSync = this.appSettings.getBoolean("doAutoSync", false);
		if (!this.alarmScheduled && doAutoSync) {

			int interval = Integer.parseInt(
					this.appSettings.getString("autoSyncInterval", "1800000"),
					10);

			this.alarmIntent = PendingIntent.getService(appInst, 0, new Intent(
					this, SyncService.class), 0);
			alarmManager.setRepeating(AlarmManager.RTC,
					System.currentTimeMillis() + interval, interval,
					alarmIntent);

			this.alarmScheduled = true;
		}
	}

	private void unsetAlarm() {
		if (this.alarmScheduled) {
			this.alarmManager.cancel(this.alarmIntent);
			this.alarmScheduled = false;
		}
	}

	private void resetAlarm() {
		unsetAlarm();
		setAlarm();
	}


	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals("doAutoSync")) {
			if (sharedPreferences.getBoolean("doAutoSync", false)
					&& !this.alarmScheduled)
				setAlarm();
			else if (!sharedPreferences.getBoolean("doAutoSync", false)
					&& this.alarmScheduled)
				unsetAlarm();
		} else if (key.equals("autoSyncInterval"))
			resetAlarm();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}