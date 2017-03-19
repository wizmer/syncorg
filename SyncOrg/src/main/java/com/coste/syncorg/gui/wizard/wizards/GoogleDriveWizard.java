package com.coste.syncorg.gui.wizard.wizards;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.coste.google_drive.FolderPicker;
import com.coste.syncorg.MainActivity;
import com.coste.syncorg.R;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;

import static com.coste.syncorg.settings.SettingsActivity.DRIVE_ID;
import static com.coste.syncorg.settings.SettingsActivity.KEY_SYNC_SOURCE;
import static com.coste.syncorg.synchronizers.Synchronizer.GOOGLE_DRIVE;


public class GoogleDriveWizard extends AppCompatActivity {
    final private int PICKFILE_RESULT_CODE = 1;
    String driveId;
    Button folderButton;
    Button okButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wizard_google_drive);

        loadSettings();

        folderButton = (Button) findViewById(R.id.select_google_drive_folder);
        okButton = (Button) findViewById(R.id.ok);

        folderButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GoogleDriveWizard.this, FolderPicker.class);
                startActivityForResult(intent, PICKFILE_RESULT_CODE);
//              driveId.asDriveFolder().createFolder()
//              listChildren(getGoogleApiClient()).setResultCallback(childrenRetrievedCallback);
            }
        });
        if(driveId.equals("")){
            folderButton.setText(driveId);
        }else{
            folderButton.setText(R.string.no_folder_selected);
        }


        okButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
                startActivity(new Intent(GoogleDriveWizard.this, MainActivity.class));
            }
        });
        refresh();
    }

    /**
     * Refresh button labels
     */
    private void refresh(){
        okButton.setClickable(driveId!=null);
        folderButton.setText(driveId);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICKFILE_RESULT_CODE:
                if (resultCode == RESULT_OK) {
                    DriveId driveId = data.getParcelableExtra(
							OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);

                    if(driveId!=null){
                        this.driveId = driveId.encodeToString();
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
        refresh();
    }


    private void loadSettings() {
        SharedPreferences appSettings = PreferenceManager
                .getDefaultSharedPreferences(this);
        driveId = appSettings.getString(DRIVE_ID, "");
    }

    public void saveSettings() {
        SharedPreferences appSettings = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = appSettings.edit();

        editor.putString(KEY_SYNC_SOURCE, GOOGLE_DRIVE);
        editor.putString(DRIVE_ID, driveId);
        editor.apply();
    }
}
