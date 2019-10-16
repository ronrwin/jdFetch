package com.example.jddata;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.example.jddata.action.Action;
import com.example.jddata.service.AccService;
import com.example.jddata.shelldroid.Env;
import com.example.jddata.shelldroid.EnvManager;
import com.example.jddata.util.FileUtils;
import com.example.jddata.util.LogUtil;
import com.example.jddata.util.OpenAccessibilitySettingHelper;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainApplication extends Application {

    public static Context sContext;
    // 线程池处理耗时任务
    public static Executor sExecutor;

    public static SparseArray<String> sCommandMap;

    public static ConcurrentLinkedDeque<Action> sActionQueue = new ConcurrentLinkedDeque<>();

    public static long sAllTaskCost = 0L;

    public static int sDay = 0;

    public static Handler sMainHandler;

    public static String sCurrentScene = "";

    public static String sCurrentSkuFile = "";

    public static HandlerThread jdKillCheckThread = new HandlerThread("jd_kill_check_thread");
    public static JdKillCheckHandler jdKillCheckHandler;

    public static HashMap<String, String> day9Map = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        sMainHandler = new Handler(Looper.getMainLooper());
        sExecutor = Executors.newFixedThreadPool(2);
        CrashHandler.getInstance().init(this);
        sExecutor.execute(new Runnable() {
            @Override
            public void run() {
                EnvManager.envs = EnvManager.scanEnvs();
//                changeDay9();
//                GlobalInfo.generateClient();
//                Session.initTemplates();
                madeCommandMap();
            }
        });
    }

    public static void changeDay9() {
        byte[] bytes = FileUtils.readBytes(LogUtil.EXTERNAL_FILE_FOLDER + "/new_day9.txt");
        if (bytes != null) {
            String text = new String(bytes);
            String[] lines = text.replace("\r", "").split("\n");

            for (String line : lines) {
                String[] pair = line.split(",");
                day9Map.put(pair[0], pair[1]);
            }
        } else {
            sMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(sContext, "new_day9.txt not found", Toast.LENGTH_LONG).show();
                    System.exit(0);
                }
            });
        }

        for (Env env : EnvManager.envs) {
            final String id = env.getId().split("_")[0];
            if (day9Map.containsKey(id)) {
                env.setDay9(day9Map.get(id));
            } else {
//                sMainHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(sContext, "动作变更错误：" + id, Toast.LENGTH_LONG).show();
//                        System.exit(0);
//                    }
//                });
            }
        }
    }

    public static void startJDKillThread() {
        if (jdKillCheckHandler == null) {
            jdKillCheckThread.start();
            jdKillCheckHandler = new JdKillCheckHandler(jdKillCheckThread.getLooper());
            jdKillCheckHandler.sendEmptyMessageDelayed(0, 5000);
        }
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
}
