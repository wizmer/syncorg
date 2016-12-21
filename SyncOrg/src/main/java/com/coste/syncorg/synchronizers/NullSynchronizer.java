package com.coste.syncorg.synchronizers;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.coste.syncorg.orgdata.OrgFile;
import com.coste.syncorg.services.PermissionManagerActivity;
import com.coste.syncorg.services.SyncService;

import java.io.BufferedReader;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class NullSynchronizer extends Synchronizer {
    String syncFolder;

    public NullSynchronizer(Context context) {
        super(context);
        SyncService syncServiceContext = (SyncService) context;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        syncFolder = preferences.getString("syncFolder", "null");
        File dir = new File(getAbsoluteFilesDir());
        if(!dir.exists()){
            if(Build.VERSION.SDK_INT >= 23){
                int hasWritePermission = context.checkSelfPermission(Manifest.permission.WRITE_CONTACTS);
                if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(context, PermissionManagerActivity.class);
                    context.startActivity(intent);
                    return;
                }
            }
            createSyncFolder();
        }
    }

    public void createSyncFolder(){
        File dir = new File(getAbsoluteFilesDir());
        dir.mkdir();
    }

    @Override
    public String getAbsoluteFilesDir() {
        return syncFolder;
    }

    public boolean isConfigured() {
        return true;
    }

    public void putRemoteFile(String filename, String contents) {
    }

    public BufferedReader getRemoteFile(String filename) {
        return null;
    }

    @Override
    public SyncResult synchronize() {
        SyncResult result = new SyncResult();
        File file = new File(getAbsoluteFilesDir());
        File[] files = file.listFiles();
        HashMap<String, Long> times_modified = OrgFile.getLastModifiedTimes(context);

        result.deletedFiles = times_modified.keySet();

        if(files == null) return result;

        for(File f: files){
            // Skip hidden files
            if(f.getName().startsWith(".")) continue;
            result.deletedFiles.remove(f.getName());
            Long timeInDB = times_modified.get(f.getName());
            if(timeInDB == null || f.lastModified() != timeInDB){
                result.changedFiles.add(f.getName());
            }
        }

        return result;
    }


    @Override
	public void postSynchronize() {
    }

    @Override
    public void addFile(String filename) {

    }

    @Override
    public boolean isConnectable() {
		return true;
	}
}