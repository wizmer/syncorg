package com.coste.syncorg.gui.wizard.wizards;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.coste.syncorg.OrgNodeListActivity;
import com.coste.syncorg.R;
import com.coste.syncorg.directory_chooser.DirectoryChooserActivity;
import com.coste.syncorg.directory_chooser.FolderPickerActivity;
import com.coste.syncorg.orgdata.OrgFile;
import com.coste.syncorg.orgdata.SyncOrgApplication;
import com.coste.syncorg.synchronizers.Synchronizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class NoSyncWizard extends AppCompatActivity {
	final private int PICKFILE_RESULT_CODE = 1;
	static public String FOLDER_PATH;
	String syncFolder = null;
	TextView orgFolder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wizard_no_sync);

		Button folder = (Button) findViewById(R.id.select_folder);
		folder.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(NoSyncWizard.this, FolderPickerActivity.class);
				startActivityForResult(intent, PICKFILE_RESULT_CODE);
			}
		});

		orgFolder = ((TextView) findViewById(R.id.org_folder));

		SharedPreferences appSettings = PreferenceManager
				.getDefaultSharedPreferences(this);
		syncFolder = appSettings.getString("syncFolder", "");
		if(!syncFolder.equals("")) {
			String details = getResources().getString(R.string.org_folder) + " " +
					syncFolder;
			orgFolder.setText(details);
		}
		Button okButton = (Button) findViewById(R.id.ok);
		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				checkPreviousSynchronizer(NoSyncWizard.this);

			}
		});
	}



	public static void copyDirectoryOneLocationToAnotherLocation(File sourceLocation, File targetLocation)
			throws IOException {

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (int i = 0; i < sourceLocation.listFiles().length; i++) {
				copyDirectoryOneLocationToAnotherLocation(new File(sourceLocation, children[i]),
						new File(targetLocation, children[i]));
			}
		} else {
			InputStream in = new FileInputStream(sourceLocation);
			OutputStream out = new FileOutputStream(targetLocation);

			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}

	void checkPreviousSynchronizer(Context context){
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String syncSource = sharedPreferences.getString("syncSource", "null");
		final String currentSyncFolder = Synchronizer.getInstance().getAbsoluteFilesDir();
		if(syncSource.equals("null") || syncSource.equals("nullSync")){
			File[] currentNodes = new File(currentSyncFolder).listFiles();
			if(currentNodes != null && currentNodes.length > 0) {
				AlertDialog.Builder alert = new AlertDialog.Builder(NoSyncWizard.this);
				alert.setCancelable(false);
				alert.setTitle(R.string.new_file);
				alert.setMessage(R.string.copy_old_sync_folder);
				alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						try {
							copyDirectoryOneLocationToAnotherLocation(new File(currentSyncFolder), new File(syncFolder));
							proceed();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});

				alert.setNegativeButton(R.string.no_start_from_scratch, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						proceed();
					}
				});

				alert.show();
			}else{
				proceed();
			}
		}
	}

	void proceed(){
		saveSettings();
		((SyncOrgApplication) getApplication()).startSynchronizer();
		Intent intent = new Intent(NoSyncWizard.this, OrgNodeListActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		switch(requestCode){
			case PICKFILE_RESULT_CODE:
				if(resultCode==RESULT_OK){
					syncFolder = data.getExtras().getString(FolderPickerActivity.EXTRA_RESULT_DIRECTORY);
					String details = getResources().getString(R.string.org_folder) + " " +
							syncFolder;
					orgFolder.setText(details);
				}
				break;
		}
	}

	private void loadSettings() {

	}

	public void saveSettings() {
		SharedPreferences appSettings = PreferenceManager
				.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = appSettings.edit();

		editor.putString("syncSource", "nullSync");
		editor.putString("syncFolder", syncFolder);
		editor.apply();
	}
}
