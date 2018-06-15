package com.example.jddata.shelldroid;

import android.graphics.drawable.Drawable;

public class AppInfo {
    String appName;
    String pkgName;
    Drawable icon;

    public AppInfo(String appName, String pkgName, Drawable icon) {
        this.appName = appName;
        this.pkgName = pkgName;
        this.icon = icon;
    }
}
