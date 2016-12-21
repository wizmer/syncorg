package com.coste.syncorg.services;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.coste.syncorg.R;
import com.coste.syncorg.synchronizers.NullSynchronizer;


@TargetApi(23)
public class PermissionManagerActivity extends AppCompatActivity {
    final public static int MAKE_NULL_SYNC_DIR_PERMISSION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_manager);

        requestPermissions(new String[] {Manifest.permission.WRITE_CONTACTS},
                MAKE_NULL_SYNC_DIR_PERMISSION);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MAKE_NULL_SYNC_DIR_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    ((NullSynchronizer)NullSynchronizer.getInstance()).createSyncFolder();
                } else {
                    // Permission Denied
                    Toast.makeText(this, "Permission write to external storage: Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
