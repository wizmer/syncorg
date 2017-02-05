package com.coste.syncorg.gui.wizard.wizards;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.coste.google_drive.Base;
import com.coste.google_drive.FolderPicker;
import com.coste.syncorg.R;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;

import static com.coste.google_drive.FolderPicker.FOLDER_PATH;


public class GoogleDriveWizard extends AppCompatActivity {
	final private int PICKFILE_RESULT_CODE = 1;
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
				Intent intent = new Intent(GoogleDriveWizard.this, FolderPicker.class);
				startActivityForResult(intent, PICKFILE_RESULT_CODE);
			}
		});
//
//		orgFolder = ((TextView) findViewById(R.id.org_folder));
//
//		SharedPreferences appSettings = PreferenceManager
//				.getDefaultSharedPreferences(this);
//		syncFolder = appSettings.getString("syncFolder", "");
//		if(!syncFolder.equals("")) {
//			String details = getResources().getString(R.string.folder) + " " +
//					syncFolder;
//			orgFolder.setText(details);
//		}
//		Button okButton = (Button) findViewById(R.id.ok);
//		okButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				checkPreviousSynchronizer(NoSyncWizard.this);
//
//			}
//		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
			case PICKFILE_RESULT_CODE:
				if (resultCode == RESULT_OK) {
//					DriveId driveId = (DriveId) data.getParcelableExtra(
//							OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
//					Toast.makeText(this, "drive : " + driveId, Toast.LENGTH_LONG).show();

				}
				break;
			default:
				super.onActivityResult(requestCode, resultCode, data);
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
