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

public class MainApplication extends Application {

    public static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        CrashHandler.getInstance().init(this);
        EnvManager.envs = EnvManager.scanEnvs();
    }

    public static void copyPic(String filename) {
        File path = new File(Environment.getExternalStorageDirectory() + "/Pictures/" + filename);
        if (path.exists()) {
            path.delete();
        }

        if (!path.exists()) {
            try {
                FileUtils.copyAssets(sContext, filename, path.getAbsolutePath());
                // 最后通知图库更新
                sContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
