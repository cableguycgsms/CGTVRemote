package com.cableguy.remotesdk.utils;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

public class DeviceUtils {
    private static final String TAG = "DeviceUtils";

    public static String getDeviceId(Context context) {
        @SuppressLint("HardwareIds") String androidId = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        return androidId;
    }

    public static void launchAppActivity(String packageActivity, Context context) {
        if (context == null || packageActivity == null) return;

        String packageName;
        String activityName = null;

        if (packageActivity.contains("/")) {
            String[] parts = packageActivity.split("/");
            if (parts.length == 2) {
                packageName = parts[0];
                activityName = parts[1];
                if (activityName.startsWith(".")) {
                    activityName = packageName + activityName;
                }
            } else {
                Log.e("DeviceUtils", "Invalid format: " + packageActivity);
                return;
            }
        } else {
            packageName = packageActivity;
        }

        if (!isAppInstalled(context, packageName)) {
            openPlayStore(context, packageName);
            return;
        }

        // Try launching via getLaunchIntentForPackage
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launchIntent);
            return;
        }

        // Try explicit activity if provided
        if (activityName != null) {
            try {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(packageName, activityName));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return;
            } catch (Exception e) {
                Log.e("DeviceUtils", "Explicit activity launch failed", e);
            }
        }

        // Fallback to Play Store
        openPlayStore(context, packageName);
    }

    private static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private static void openPlayStore(Context context, String packageName) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + packageName))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + packageName))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }
}
