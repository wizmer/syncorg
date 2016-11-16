package com.coste.syncorg.gui.wizard.wizards;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.coste.syncorg.OrgNodeListActivity;
import com.coste.syncorg.R;
import com.coste.syncorg.directory_chooser.DirectoryChooserActivity;
import com.coste.syncorg.directory_chooser.FolderPickerActivity;
import com.coste.syncorg.orgdata.SyncOrgApplication;


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
		Button okButton = (Button) findViewById(R.id.ok);
		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveSettings();
				((SyncOrgApplication) getApplication()).startSynchronizer();
				Intent intent = new Intent(NoSyncWizard.this, OrgNodeListActivity.class);
				startActivity(intent);
			}
		});
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

		editor.putString("syncSource", "null");
		editor.putString("syncFolder", syncFolder);
		editor.apply();
	}
}
