package com.example.jddata;

import android.app.ActionBar;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.jddata.service.AccService;
import com.example.jddata.shelldroid.EnvManager;
import com.example.jddata.shelldroid.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainApplication extends Application {

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        EnvManager.envs = EnvManager.scanEnvs();
    }

    public static Context getContext() {
        return sContext;
    }

    public static void startMainJD() {
        startMainJD(true);
    }

    public static void startMainJD(boolean restart) {
        if (restart) {
            EnvManager.doRoot("am force-stop " + AccService.PACKAGE_NAME);
        }

        try {
            Intent startIntent = new Intent();
            startIntent.setAction(Intent.ACTION_VIEW);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startIntent.setClassName("com.jingdong.app.mall", "com.jingdong.app.mall.main.MainActivity");

            MainApplication.getContext().startActivity(startIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
