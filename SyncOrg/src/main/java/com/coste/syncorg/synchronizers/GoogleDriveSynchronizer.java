package com.coste.syncorg.synchronizers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.coste.syncorg.orgdata.OrgFile;
import com.coste.syncorg.services.PermissionManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import static com.coste.syncorg.settings.SettingsActivity.DRIVE_ID;

/**
 * A synchronizer that keeps track of the synchronization state of a standard folder
 * This is done by keeping track of the timestamp of last modification for each file in the folder.
 * Every time the synchronizer is called it will check whether timestamps kept in DB match those of
 * the files. It will then parse new files and externally modified files
 */
public class GoogleDriveSynchronizer extends Synchronizer implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    DriveFolder rootFolder;
    GoogleApiClient mGoogleApiClient;

    public GoogleDriveSynchronizer(Context context) {
        super(context);
    }


    @Override
    public String getAbsoluteFilesDir() {
        return "";
    }


    @Override
    protected SyncResult doInBackground(Void... voids) {
        return null;
    }

    /**
     * Google Drive API already have its own async mechanism so we don't do
     * anything in ours. onPreExecute and doInBackground are intentionally left empty
     * and the real process start in onPostExecute. We just need to trigger the connection
     * process. Our async process will be over but the synchronization will still occur as
     * super.onPostExecute will be called as a callback when the API find the data.
     *
     * @param result
     */
    @Override
    public void onPreExecute() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER) // required for App Folder sample
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    @Override
    public void onPostExecute(SyncResult result) {
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        rootFolder = DriveId.decodeFromString(preferences.getString(DRIVE_ID, "null"))
                .asDriveFolder();
        rootFolder.listChildren(mGoogleApiClient).setResultCallback(childrenRetrievedCallback);
    }


    ResultCallback<DriveApi.MetadataBufferResult> childrenRetrievedCallback = new
            ResultCallback<DriveApi.MetadataBufferResult>() {
                @Override
                public void onResult(DriveApi.MetadataBufferResult result) {
                    if (!result.getStatus().isSuccess()) {
                        Toast.makeText(context, "Problem while retrieving files", Toast.LENGTH_LONG).show();
                        return;
                    }

                    MetadataBuffer metadata = result.getMetadataBuffer();
                    String total = "";
                    for(Metadata data: metadata){
                        total += data.getTitle() + "\n";
                    }
                    Toast.makeText(context, "Success with files: "+total, Toast.LENGTH_LONG).show();
                    metadata.release();
                    GoogleDriveSynchronizer.super.onPostExecute(null);
                }
            };


//    @Override
//    public void onConnected(Bundle connectionHint) {
//        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
//                .setTitle("New folder").build();
//        Drive.DriveApi.getRootFolder(mGoogleApiClient).createFolder(
//                mGoogleApiClient, changeSet).setResultCallback(folderCreatedCallback);
//    }
//
//    ResultCallback<DriveFolder.DriveFolderResult> folderCreatedCallback = new
//            ResultCallback<DriveFolder.DriveFolderResult>() {
//                @Override
//                public void onResult(DriveFolder.DriveFolderResult result) {
//                    if (!result.getStatus().isSuccess()) {
////                        showMessage("Error while trying to create the folder");
//                        return;
//                    }
////                    showMessage("Created a folder: " + result.getDriveFolder().getDriveId());
//                }
//            };

    @Override
    public void _addFile(String filename) {

    }

    @Override
    public boolean throwIfNotConnectable() {
        return true;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}