package com.example.jddata;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;

import com.example.jddata.service.AccService;
import com.example.jddata.shelldroid.EnvManager;
import com.example.jddata.util.FileUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainApplication extends Application {

    public static Context sContext;
    // 线程池处理耗时任务
    public static Executor sExecutor;

    public static SparseArray<String> sCommandMap;

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
                madeCommandMap();
            }
        });
    }

    private void madeCommandMap() {
        try {
            Class clz = Class.forName("com.example.jddata.service.ServiceCommand");
            Field[] fields = clz.getDeclaredFields();
            sCommandMap = new SparseArray<>(fields.length);
            for (Field field : fields) {
                if (field.get(clz) instanceof Integer) {
                    sCommandMap.put((Integer) field.get(clz), field.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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


    // 构造脚本
    private void test() {
        StringBuilder sb = new StringBuilder();
        for (int i = 133; i < 401; i++) {
            sb.append("  {\n");
            sb.append("    \"templateId\": " + i + ",\n");
            sb.append("    \"actions\": [\n");
            sb.append("      {\n");
            sb.append("         \"action\": \"home_search_select\"\n");
            sb.append("      },\n");
            sb.append("      {\n");
            sb.append("         \"action\": \"close\"\n");
            sb.append("      }\n");
            sb.append("    ]\n");
            sb.append("  },\n");
        }
        FileUtils.writeToFile(Environment.getExternalStorageDirectory().getAbsolutePath(), "test.txt", sb.toString());
    }
}
