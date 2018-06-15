package com.example.jddata.shelldroid;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.example.jddata.MainApplication;
import com.example.jddata.R;

import java.util.ArrayList;
import java.util.List;

public class AndroidUtils {
    public static PackageManager pm() {
        return MainApplication.getContext().getPackageManager();
    }

    public static Drawable getIcon(String pkgName) {
        try {
            return pm().getApplicationIcon(pkgName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return MainApplication.getContext().getDrawable(R.drawable.ic_launcher_background);
    }

    public static String getDataDir(String pkgName) {
        try {
            ApplicationInfo info = pm().getApplicationInfo(pkgName, 0);
            if (info != null) {
                return info.dataDir;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<AppInfo> getInstalledAppInfo() {
        ArrayList<AppInfo> infos = new ArrayList<>();
        List<ApplicationInfo> pkgs = pm().getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo pkg : pkgs) {
            if ((pkg.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) == 0) {
                String appName = pm().getApplicationLabel(pkg).toString();
                infos.add(new AppInfo(appName, pkg.packageName, getIcon(pkg.packageName)));
            }
        }
        return infos;
    }
}
