package com.coste.syncorg.gui.wizard.wizards;

import android.content.Context;

import com.coste.syncorg.gui.wizard.DirectoryBrowser;
import com.coste.syncorg.synchronizers.UbuntuOneSynchronizer;

import java.io.File;

public class UbuntuOneDirectoryBrowser extends DirectoryBrowser<String> {
	private UbuntuOneSynchronizer synchronizer;

	public UbuntuOneDirectoryBrowser(Context context, UbuntuOneSynchronizer synchronizer) {
		super(context);
		this.synchronizer = synchronizer;

		browseTo(File.separator);
	}

	private static String getParentPath(String path) {
		if (path.charAt(path.length() - 1) == File.separatorChar) {
			path = path.substring(0, path.length() - 1);
		}
		int ind = path.lastIndexOf(File.separatorChar);
		return path.substring(0, ind + 1);
	}

	@Override
	public boolean isCurrentDirectoryRoot() {
		return currentDirectory.equals(File.separator);
	}

	@Override
	public void browseTo(int position) {
		browseTo(getDir(position));
	}

	@Override
	public void browseTo(String directory) {
		currentDirectory = directory;
		directoryNames.clear();
		directoryListing.clear();
		if (!isCurrentDirectoryRoot()) {
			directoryNames.add(upOneLevel);
			directoryListing.add(getParentPath(currentDirectory));
//			Log.d("SyncOrg", "Current directory: " + currentDirectory);
//			Log.d("SyncOrg", "Parent path: "	+ getParentPath(currentDirectory));
		}
		for (String item : synchronizer.getDirectoryList(directory)) {
			directoryNames.add(item);
			directoryListing.add(item);
		}
	}
}
