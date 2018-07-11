package com.example.veeotech.postaltracking.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by VeeoTech on 2018/5/4.
 */

public class VersionUtil {

    public static String getVersionName(Context context) {
        PackageInfo info = null;
        PackageManager pm = context.getPackageManager();
        try {
            info = pm.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return info.versionName;

    }
    public static int getVersionCode(Context context) {
        PackageInfo info = null;
        PackageManager pm = context.getPackageManager();
        try {
            info = pm.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return info.versionCode;

    }
}
