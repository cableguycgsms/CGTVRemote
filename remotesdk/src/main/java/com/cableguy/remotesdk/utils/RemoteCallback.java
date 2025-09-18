package com.cableguy.remotesdk.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class RemoteCallback {

    private static final String PREF_NAME = "REMOTE_DATA";
    private static final String KEY_SOCKET_URL = "remoteSocketURL";
    private static final String KEY_REMOTE_URL = "remoteURL";

    private static String remoteSocketURL = "";
    private static String remoteURL = "";
    private static Context appContext; // global reference

    // Call this once from Application.onCreate()
    public static void init(Context context) {
        appContext = context.getApplicationContext();
        SharedPreferences prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        remoteSocketURL = prefs.getString(KEY_SOCKET_URL, "");
        remoteURL = prefs.getString(KEY_REMOTE_URL, "");
    }

    public static String getRemoteSocketURL() {
        return remoteSocketURL;
    }

    public static void setRemoteSocketURL(String redirectUrl) {
        remoteSocketURL = redirectUrl;
        if (appContext != null) {
            appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(KEY_SOCKET_URL, redirectUrl)
                    .apply();
        }
    }

    public static String getRemoteURL() {
        return remoteURL;
    }

    public static void setRemoteURL(String redirectUrl) {
        remoteURL = redirectUrl;
        if (appContext != null) {
            appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(KEY_REMOTE_URL, redirectUrl)
                    .apply();
        }
    }
}
