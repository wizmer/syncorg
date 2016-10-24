package com.coste.syncorg.gui.wizard.wizards;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.coste.syncorg.R;
import com.coste.syncorg.synchronizers.JGitWrapper;

public class SSHWizard extends AppCompatActivity {
	private EditText sshUser;
	private EditText sshPass;
	private EditText sshPath;
	private EditText sshHost;
	private EditText sshPort;

	private TextView sshPubFileActual;
	Switch auth_selector;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wizard_ssh);


		final TextView sshPasswordTitle = (TextView) findViewById(R.id.wizard_ssh_password_text);
		final TextView sshPubkeyTitle = (TextView) findViewById(R.id.wizard_ssh_pubkey_prompt);

		sshUser = (EditText) findViewById(R.id.wizard_ssh_username);
		sshPass = (EditText) findViewById(R.id.wizard_ssh_password);
		sshPath = (EditText) findViewById(R.id.wizard_ssh_path);
		sshHost = (EditText) findViewById(R.id.wizard_ssh_host);
		sshPort = (EditText) findViewById(R.id.wizard_ssh_port);
		sshPubFileActual = (TextView) findViewById(R.id.wizard_ssh_pub_file_actual);
		final Button sshPubFileSelect = (Button) findViewById(R.id.wizard_ssh_choose_pub_file);
		sshPubFileSelect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String pubKey = com.coste.syncorg.synchronizers.SshSessionFactory.generateKeyPair(SSHWizard.this);
				if(pubKey.equals("")) return;
				ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				ClipData clip = ClipData.newPlainText("label", pubKey);
				clipboard.setPrimaryClip(clip);
				Toast.makeText(SSHWizard.this, R.string.pubkey_copied, Toast.LENGTH_LONG).show();
			}
		});

		auth_selector = (Switch) findViewById(R.id.auth_selector);
		auth_selector.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				if(b){
					sshPass.setVisibility(View.GONE);
					sshPasswordTitle.setVisibility(View.GONE);
					sshPubFileSelect.setVisibility(View.VISIBLE);
					sshPubkeyTitle.setVisibility(View.VISIBLE);
					sshPubFileActual.setVisibility(View.VISIBLE);
				} else {
					sshPass.setVisibility(View.VISIBLE);
					sshPasswordTitle.setVisibility(View.VISIBLE);
					sshPubFileSelect.setVisibility(View.GONE);
					sshPubkeyTitle.setVisibility(View.GONE);
					sshPubFileActual.setVisibility(View.GONE);
				}
			}
		});

		loadSettings();

		Button done = (Button) findViewById(R.id.done);
		done.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveSettings();
			}
		});

		Button help = (Button) findViewById(R.id.help);
		help.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// DialogFragment.show() will take care of adding the fragment
				// in a transaction.  We also want to remove any currently showing
				// dialog, so make our own transaction and take care of that here.
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

				// Create and show the dialog.
				DialogFragment newFragment = MyDialogFragment.newInstance();
				newFragment.show(ft, "dialog");
			}
		});


	}

	private void loadSettings() {
		SharedPreferences appSettings = PreferenceManager
				.getDefaultSharedPreferences(this);
		sshPath.setText(appSettings.getString("scpPath", ""));
		sshUser.setText(appSettings.getString("scpUser", ""));
		sshPass.setText(appSettings.getString("scpPass", ""));
		sshHost.setText(appSettings.getString("scpHost", ""));
		sshPort.setText(appSettings.getString("scpPort", ""));
		auth_selector.setChecked(appSettings.getBoolean("usePassword", true));
		auth_selector.performClick();
		auth_selector.performClick();
		sshPubFileActual.setText(appSettings.getString("scpPubFile", ""));
	}

	public void saveSettings() {
		final String pathActual = sshPath.getText().toString();
		final String userActual = sshUser.getText().toString();
		final String hostActual = sshHost.getText().toString();

		String portActual = sshPort.getText().toString();
		if(portActual.equals("")) portActual = "22";

		SharedPreferences appSettings = PreferenceManager
				.getDefaultSharedPreferences(this);


		SharedPreferences.Editor editor = appSettings.edit();

		editor.putString("syncSource", "scp");

		editor.putString("scpPath", pathActual);
		editor.putString("scpUser", userActual);
		editor.putString("scpHost", hostActual);
		editor.putString("scpPort", portActual);
		editor.putBoolean("usePassword", auth_selector.isChecked());
		editor.putString("scpPubFile", sshPubFileActual.getText().toString());
		editor.putString("scpPass", sshPass.getText().toString());
		editor.apply();

		JGitWrapper.CloneGitRepoTask task = new JGitWrapper.CloneGitRepoTask(this);
		task.execute(pathActual, sshPass.getText().toString(), userActual, hostActual, portActual);
	}

	public static class MyDialogFragment extends DialogFragment {

		/**
		 * Create a new instance of MyDialogFragment, providing "num"
		 * as an argument.
		 */
		static MyDialogFragment newInstance() {
			return new MyDialogFragment();
		}


		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			View v = inflater.inflate(R.layout.wizard_help, container, false);
			return v;
		}
	}
}
