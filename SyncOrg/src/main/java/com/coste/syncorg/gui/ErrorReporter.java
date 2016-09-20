package com.coste.syncorg.gui;

import android.app.AlertDialog;
import android.content.Context;

import com.coste.syncorg.R;

public class ErrorReporter
{
	private static final String LT = "SyncOrg";
	
	public static void displayError(Context context, String message) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(context); 
        dialog.setTitle(R.string.error_dialog_title);
        dialog.setMessage(message);
        dialog.setNeutralButton("Ok", null);
        dialog.create().show();
	}

	public static void displayError(Context context, Exception e) {
//		Log.e(LT, e.toString());
		ErrorReporter.displayError(context, e.getMessage());
	}
}
