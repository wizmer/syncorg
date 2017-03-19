package com.coste.syncorg.gui.wizard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.coste.syncorg.R;
import com.coste.syncorg.gui.wizard.wizards.GoogleDriveWizard;
import com.coste.syncorg.gui.wizard.wizards.ExternalSyncWizard;
import com.coste.syncorg.gui.wizard.wizards.SDCardWizard;
import com.coste.syncorg.gui.wizard.wizards.SSHWizard;
import com.coste.syncorg.gui.wizard.wizards.WebDAVWizard;

import static com.coste.syncorg.settings.SettingsActivity.KEY_SYNC_SOURCE;
import static com.coste.syncorg.synchronizers.Synchronizer.EXTERNAL;
import static com.coste.syncorg.synchronizers.Synchronizer.GOOGLE_DRIVE;
import static com.coste.syncorg.synchronizers.Synchronizer.SD_CARD;
import static com.coste.syncorg.synchronizers.Synchronizer.SSH;
import static com.coste.syncorg.synchronizers.Synchronizer.WEBDAV;

public class WizardActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wizard_choose_synchronizer);

        final RadioGroup syncGroup = (RadioGroup) findViewById(R.id.sync_group);

        SharedPreferences srcPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        String syncSource = srcPrefs.getString(KEY_SYNC_SOURCE, EXTERNAL);
        int id = getResources().getIdentifier(syncSource, "id", getPackageName());
        RadioButton radioButton = (RadioButton) findViewById(id);
        if (radioButton != null) radioButton.setChecked(true);

        findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = syncGroup.getCheckedRadioButtonId();
                if (id < 0) return;

                // Saving selected synchronizer
                SharedPreferences appSettings = PreferenceManager
                        .getDefaultSharedPreferences(WizardActivity.this);
                SharedPreferences.Editor editor = appSettings.edit();
                String syncName = getResources().getResourceEntryName(id);
                editor.putString(KEY_SYNC_SOURCE, syncName);
                editor.apply();

                switch (syncName) {
                    case WEBDAV:
                        startActivity(new Intent(WizardActivity.this, WebDAVWizard.class));
                        break;
                    case GOOGLE_DRIVE:
                        startActivity(new Intent(WizardActivity.this, GoogleDriveWizard.class));
                        break;
                    case SD_CARD:
                        startActivity(new Intent(WizardActivity.this, SDCardWizard.class));
                        break;
                    case SSH:
                        startActivity(new Intent(WizardActivity.this, SSHWizard.class));
                        break;
                    default:
                        startActivity(new Intent(WizardActivity.this, ExternalSyncWizard.class));
                        break;
                }
            }
        });
    }
}

