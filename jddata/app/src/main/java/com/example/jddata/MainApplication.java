package com.example.jddata;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import com.example.jddata.service.AccService;
import com.example.jddata.shelldroid.EnvManager;
import com.example.jddata.util.FileUtils;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainApplication extends Application {

    public static Context sContext;
    // 线程池处理耗时任务
    public static Executor sExecutor;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        sExecutor = Executors.newFixedThreadPool(2);
        CrashHandler.getInstance().init(this);
        sExecutor.execute(new Runnable() {
            @Override
            public void run() {
                EnvManager.envs = EnvManager.scanEnvs();
                Session.initTemplates();
            }
        });
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

            MainApplication.sContext.startActivity(startIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
