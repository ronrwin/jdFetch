package com.example.jddata;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.example.jddata.service.AccService;
import com.example.jddata.shelldroid.EnvManager;
import com.example.jddata.shelldroid.Location;

import java.util.List;

public class MainApplication extends Application {

    private static Context sContext;

    public static boolean sIsTest = true;
    public static String sTargetEnvName;
    public static String sSearchText;

    public static Location sSelectLocation;
    public static Location[] sLocations = new Location[] {
            new Location("广州", 113.23, 23.16),
            new Location("上海", 121.48, 31.22),
            new Location("昆明", 102.73, 25.04),
            new Location("呼和浩特", 111.65, 40.82),
            new Location("北京", 116.46, 39.92),
            new Location("成都", 104.06, 30.67),
            new Location("长春", 125.35, 43.88),
            new Location("合肥", 117.27, 31.86),
            new Location("济南", 117, 36.65),
            new Location("太原", 112.53, 37.87),
            new Location("南宁", 108.33, 22.84),
            new Location("乌鲁木齐", 87.68, 43.77),
            new Location("南京", 118.78, 32.04),
            new Location("南昌", 115.89, 28.68),
            new Location("石家庄", 114.48, 38.03),
            new Location("郑州", 113.65, 34.76),
            new Location("杭州", 120.19, 30.26),
            new Location("海口", 110.35, 20.02),
            new Location("武汉", 114.31, 30.52),
            new Location("长沙", 113, 28.21),
            new Location("兰州", 103.73, 36.03),
            new Location("福州", 119.3, 26.08),
            new Location("拉萨", 91.11, 29.97),
            new Location("贵阳", 106.71, 26.57),
            new Location("沈阳", 123.38, 41.8),
            new Location("重庆", 106.54, 29.59),
            new Location("西安", 108.95, 34.27),
            new Location("哈尔滨", 126.63, 45.75),
            new Location("香港", 114.1, 22.2),
            new Location("澳门", 113.33, 22.13)
    };

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
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
