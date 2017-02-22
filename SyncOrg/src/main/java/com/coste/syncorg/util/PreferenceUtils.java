package com.coste.syncorg.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.coste.syncorg.orgdata.SyncOrgApplication;

import java.util.ArrayList;
import java.util.HashSet;

public class PreferenceUtils {
    private static final int DEFAULT_FONTSIZE = 14;


    public static HashSet<String> getExcludedTags() {
        Context context = SyncOrgApplication.getContext();
        String tags = PreferenceManager.getDefaultSharedPreferences(context).getString(
                "excludeTagsInheritance", null);

        if (tags == null)
            return null;

        HashSet<String> tagsSet = new HashSet<String>();
        for (String tag : tags.split(":")) {
            if (TextUtils.isEmpty(tag) == false)
                tagsSet.add(tag);
        }

        return tagsSet;
    }

    public static int getFontSize() {
        Context context = SyncOrgApplication.getContext();
        try {
            int fontSize = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(
                    context).getString("fontSize", "14"));

            if (fontSize > 2)
                return fontSize;
        } catch (NumberFormatException e) {
        }

        return DEFAULT_FONTSIZE;
    }

    public static int getLevelOfRecursion() {
        Context context = SyncOrgApplication.getContext();
        return Integer.parseInt(PreferenceManager
                .getDefaultSharedPreferences(context).getString(
                        "viewRecursionMax", "0"));
    }

    public static String getThemeName() {
        Context context = SyncOrgApplication.getContext();
        SharedPreferences appSettings =
                PreferenceManager.getDefaultSharedPreferences(context);
        return appSettings.getString("theme", "Dark");
    }

    public static boolean useAdvancedCapturing() {
        Context context = SyncOrgApplication.getContext();
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("captureAdvanced", true);
    }

    public static boolean isSyncConfigured() {
        Context context = SyncOrgApplication.getContext();
        String syncSource = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("syncSource", "");

        return !TextUtils.isEmpty(syncSource);
    }

    public static boolean isUpgradedVersion() {
        Context context = SyncOrgApplication.getContext();
        SharedPreferences appSettings =
                PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = appSettings.edit();
        int versionCode = appSettings.getInt("appVersion", -1);
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            int newVersion = pInfo.versionCode;
            if (versionCode != -1 && versionCode != newVersion) {
                editor.putInt("appVersion", newVersion);
                editor.commit();
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public static ArrayList<String> getSelectedTodos() {
        Context context = SyncOrgApplication.getContext();
        SharedPreferences appSettings =
                PreferenceManager.getDefaultSharedPreferences(context);

        ArrayList<String> todos = new ArrayList<String>();

        String todoString = appSettings.getString("selectedTodos", "").trim();
        if (TextUtils.isEmpty(todoString))
            return todos;

        for (String todo : todoString.split(" ")) {
            if (TextUtils.isEmpty(todo))
                continue;
            else
                todos.add(todo);
        }

        return todos;
    }
}
