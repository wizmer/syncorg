package com.coste.syncorg.gui;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.coste.syncorg.MainActivity;
import com.coste.syncorg.R;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SynchronizerNotification {
    private NotificationManager notificationManager;
    private Notification notification;
    private int notifyRef = 1;
    private Context context;


    public SynchronizerNotification(Context context) {
        this.context = context;
    }

    public NotificationManager getNotificationManager(Context context){
        if(this.notificationManager!=null) return this.notificationManager;
        this.notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        return this.notificationManager;
    }

    /**
     * Set the notification to be displayed in case of error.
     * Also prepare the intent to send if the user clicks on the notification"
     */
    public void errorNotification(String errorMsg) {
        Intent notifyIntent = new Intent(context, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notifyIntent.putExtra("ERROR_MESSAGE", errorMsg);
        notifyIntent.setAction("com.coste.syncorg.SYNC_FAILED");

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notifyIntent, 0);
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentIntent(contentIntent);
        builder.setSmallIcon(R.drawable.icon);
        builder.setContentTitle(context.getString(R.string.sync_failed));
        builder.setContentText(errorMsg);
        notification = builder.getNotification();
        notification.flags = Notification.FLAG_AUTO_CANCEL;

        getNotificationManager(context).notify(notifyRef, notification);
    }

    public void setupNotification() {
        Intent notifyIntent = new Intent(context, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                notifyIntent, 0);

        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentIntent(contentIntent);
        builder.setSmallIcon(R.drawable.icon);
        builder.setOngoing(true);
        builder.setContentTitle(context.getString(R.string.sync_synchronizing_changes));
        builder.setProgress(100, 0, true);
        notification = builder.getNotification();

        getNotificationManager(context).notify(notifyRef, notification);
    }

    public void updateNotification(String message) {
        if (notification == null)
            return;

        if (message != null) {
            getNotificationManager(context).notify(notifyRef, notification);
        }
    }

    public void updateNotification(int progress) {
        updateNotification(progress, null);
    }

    public void updateNotification(int progress, String message) {
        if (notification == null)
            return;

//		FIXME:
//		This was commented because contentView is null on Android Nougat
//	    But we must find a way to make it work
//		notification.contentView.setProgressBar(android.R.id.progress, 100,
//				progress, false);

        getNotificationManager(context).notify(notifyRef, notification);
    }

    public void finalizeNotification() {
        getNotificationManager(context).cancel(notifyRef);
    }

}
