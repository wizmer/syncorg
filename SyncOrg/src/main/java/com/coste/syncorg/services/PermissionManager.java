package com.coste.syncorg.services;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;


public class PermissionManager {
    static public boolean permissionGranted(Context context) {
        if (Build.VERSION.SDK_INT >= 23) {
            int hasWritePermission = context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(context, PermissionManagerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return false;
            }
        }

        return true;
    }

}
