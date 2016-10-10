package com.coste.syncorg.synchronizers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.File;

public class NullSynchronizer extends Synchronizer {
    String syncFolder;

    public NullSynchronizer(Context context) {
        super(context);
        syncFolder = PreferenceManager.getDefaultSharedPreferences(context).getString("syncFolder", "");
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
        for(File f: files){
            result.changedFiles.add(f.getName());
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