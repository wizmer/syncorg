package com.coste.syncorg.synchronizers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.coste.syncorg.orgdata.OrgFile;

import java.io.BufferedReader;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class NullSynchronizer extends Synchronizer {
    String syncFolder;

    public NullSynchronizer(Context context) {
        super(context);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        syncFolder = preferences.getString("syncFolder", "null");
        File dir = new File(getAbsoluteFilesDir());
        if(!dir.exists()){
            dir.mkdir();
        }
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