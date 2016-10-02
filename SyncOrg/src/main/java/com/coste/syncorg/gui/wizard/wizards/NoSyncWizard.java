package com.coste.syncorg.gui.wizard.wizards;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.coste.syncorg.R;
import com.coste.syncorg.synchronizers.JGitWrapper;

public class NoSyncWizard extends AppCompatActivity {
	final private int PICKFILE_RESULT_CODE = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wizard_no_sync);

		Button folder = (Button) findViewById(R.id.select_folder);
		folder.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent fileintent = new Intent(Intent.ACTION_GET_CONTENT);
				fileintent.setType(DocumentsContract.Document.MIME_TYPE_DIR);

				try {
					startActivityForResult(fileintent, PICKFILE_RESULT_CODE);
				} catch (ActivityNotFoundException e) {
					Log.e("tag", "No activity can handle picking a file. Showing alternatives.");
				}
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		switch(requestCode){
			case PICKFILE_RESULT_CODE:
				if(resultCode==RESULT_OK){
//
//					String FilePath = data.getData().getPath();
//					String FileName = data.getData().getLastPathSegment();
//					int lastPos = FilePath.length() - FileName.length();
//					String Folder = FilePath.substring(0, lastPos);
//
//					textFile.setText("Full Path: \n" + FilePath + "\n");
//					textFolder.setText("Folder: \n" + Folder + "\n");
//					textFileName.setText("File Name: \n" + FileName + "\n");
//
//					filename thisFile = new filename(FileName);
//					textFileName_WithoutExt.setText("Filename without Ext: " + thisFile.getFilename_Without_Ext());
//					textFileName_Ext.setText("Ext: " + thisFile.getExt());

				}
				break;

		}
	}

	private void loadSettings() {
		SharedPreferences appSettings = PreferenceManager
				.getDefaultSharedPreferences(this);

	}

	public void saveSettings() {
//
//		SharedPreferences.Editor editor = appSettings.edit();
//
//		editor.putString("syncSource", "scp");


	}
}
