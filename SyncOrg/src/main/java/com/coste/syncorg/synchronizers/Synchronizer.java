package com.coste.syncorg.synchronizers;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.coste.syncorg.R;
import com.coste.syncorg.gui.SynchronizerNotification;
import com.coste.syncorg.orgdata.OrgFile;
import com.coste.syncorg.orgdata.OrgFileParser;
import com.coste.syncorg.util.OrgUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.security.cert.CertificateException;
import java.util.HashSet;

import static com.coste.syncorg.settings.SettingsActivity.KEY_SYNC_SOURCE;

/**
 * The base class of all the synchronizers.
 * The singleton instance of the class can be retreived using getInstance()
 * This class implements many of the operations that need to be done on
 * synching. Instead of using it directly, create a {@link SyncManager}.
 * <p/>
 * When implementing a new synchronizer, the methods {@link #isConfigured()},
 * {@link #putRemoteFile(String, String)} and {@link #getRemoteFile(String)} are
 * needed.
 */
public abstract class Synchronizer extends AsyncTask<Void, Void, SyncResult> {
    public static final String SYNC_UPDATE = "com.coste.syncorg.Synchronizer.action.SYNC_UPDATE";
    public static final String SYNC_DONE = "sync_done";
    public static final String SYNC_START = "sync_start";
    public static final String SYNC_PROGRESS_UPDATE = "progress_update";
    public static final String SYNC_SHOW_TOAST = "showToast";

    // Synchronizer types
    final public static String GOOGLE_DRIVE = "google_drive";
    final public static String SD_CARD = "sdcard";
    final public static String SSH = "scp";
    final public static String EXTERNAL = "nullSync";
    final public static String WEBDAV = "webdav";

    static private boolean syncRunning = false;
    private static boolean syncEnabled = true;
    protected Context context;
    private ContentResolver resolver;
    private SynchronizerNotification notify;

    protected Synchronizer(Context context) {
        this.context = context;
        this.resolver = context.getContentResolver();

        this.notify = new SynchronizerNotification(context);
    }

    public static void setSyncEnabled(boolean syncEnabled) {
        Synchronizer.syncEnabled = syncEnabled;
    }

    /**
     * Return an instance of the synchronizer according to the user preferences
     *
     * @param context
     * @return
     */
    public static Synchronizer getSynchronizer(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        String syncSource = preferences.getString(KEY_SYNC_SOURCE, "");

        if (syncSource.equals(SD_CARD))
            return new SDCardSynchronizer(context);
        else if (syncSource.equals(SSH))
            return new SSHSynchronizer(context);
        else if (syncSource.equals(GOOGLE_DRIVE))
            return new GoogleDriveSynchronizer(context);
        else
            return new ExternalSynchronizer(context);
    }

    /**
     * Instanciate a synchronizer and start a synchronization if not already running
     *
     * @param context
     */
    public static void runSynchronize(final Context context) {
        if (!syncEnabled || syncRunning) return;

        syncRunning = true;
        Synchronizer syncer = getSynchronizer(context);
        if (syncer == null) {
            syncRunning = false;
            return;
        }

        if (!syncer.isConfigured()) {
            syncer.notify.errorNotification("Sync not configured");
            return;
        }

        try {
            syncer.throwIfNotConnectable();
            syncer.execute();
        } catch (Throwable e) {
            syncer.notify.errorNotification(e.getMessage());
            syncRunning = false;
        }
    }

    public static void addFile(Context context, String filename) {
        Synchronizer syncer = getSynchronizer(context);
        if (syncer == null) return;
        syncer._addFile(filename);
    }

    /**
     * Return true if the user has to enter its credentials when the app starts
     * eg. SSHSynchonizer by password returns yes
     *
     * @return
     */
    public boolean isCredentialsRequired() {
        return false;
    }


    /**
     * @return List of files that where changed.
     */
    @Override
    protected void onPostExecute(SyncResult pulledFiles) {
        if(pulledFiles==null){
            syncRunning = false;
            return;
        }

        try {
            for (String filename : pulledFiles.deletedFiles) {
                OrgFile orgFile = new OrgFile(filename, resolver);
                orgFile.removeFile(context, true);
            }

            HashSet<String> modifiedFiles = pulledFiles.newFiles;
            modifiedFiles.addAll(pulledFiles.changedFiles);
            for (String filename : modifiedFiles) {
                OrgFile orgFile = new OrgFile(filename, getRelativeFilePath(filename));
                FileReader fileReader = new FileReader(filename);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                OrgFileParser.parseFile(orgFile, bufferedReader, context);
                orgFile.updateTimeModified(context);
            }

            announceSyncDone();
        } catch (Exception e) {
            showErrorNotification(e);
            e.printStackTrace();
        } finally {
            syncRunning = false;
        }
    }

    /**
     * Return the relative path of the file starting at the root of the synced folder
     * If the file is not in this folder, it will return the absolute path instead
     *
     * @param filename
     * @return
     */
    private String getRelativeFilePath(String filename) {
        String filesDir = getAbsoluteFilesDir() + "/";
        if (filename.substring(0, filesDir.length()).equals(filesDir)) {
            return filename.substring(filesDir.length());
        }
        return filename;
    }

    private void announceStartSync() {
        notify.setupNotification();
        OrgUtils.announceSyncStart(context);
    }

    private void announceProgressUpdate(int progress, String message) {
        if (message != null && !TextUtils.isEmpty(message))
            notify.updateNotification(progress, message);
        else
            notify.updateNotification(progress);
        OrgUtils.announceSyncUpdateProgress(progress, context);
    }

    private void announceProgressDownload(String filename, int fileIndex, int totalFiles) {
        int progress = 0;
        if (totalFiles > 0)
            progress = (100 / totalFiles) * fileIndex;
        String message = context.getString(R.string.downloading) + " " + filename;
        announceProgressUpdate(progress, message);
    }

    private void showErrorNotification(Exception exception) {
        notify.finalizeNotification();

        String errorMessage = "";
        if (CertificateException.class.isInstance(exception)) {
            errorMessage = "Certificate Error occured during sync: "
                    + exception.getLocalizedMessage();
        } else {
            errorMessage = "Error: " + exception.getLocalizedMessage();
        }

        notify.errorNotification(errorMessage);
    }

    private void announceSyncDone() {
        announceProgressUpdate(100, "Done synchronizing");
        notify.finalizeNotification();
        OrgUtils.announceSyncDone(context);
    }

    public abstract String getAbsoluteFilesDir();

    /**
     * Delete all files from the synchronized repository
     * except repository configuration files
     *
     * @param context
     */
    public void clearRepository(Context context) {
        File dir = new File(getAbsoluteFilesDir());
        for (File file : dir.listFiles()) {
            file.delete();
        }
    }

    /**
     * Called before running the synchronizer to ensure that it's configuration
     * is in a valid state.
     */
    boolean isConfigured(){
        return true;
    };

    /**
     * Called before running the synchronizer to ensure it can connect.
     */
    abstract public boolean throwIfNotConnectable() throws Exception;

    /**
     * Synchronize a new file
     *
     * @param filename
     */
    abstract public void _addFile(String filename);


}
