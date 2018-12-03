package com.example.jddata.xpose;

import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;

import com.example.jddata.GlobalInfo;
import com.example.jddata.MainApplication;
import com.example.jddata.shelldroid.Env;
import com.example.jddata.shelldroid.EnvManager;
import com.example.jddata.util.FileUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class Module extends XC_MethodHook implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    public String pkgName;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        pkgName = lpparam.packageName;
        if (checkEnv(pkgName)) {
            Env env = getEnvFromConfigFile(pkgName);
            if (env != null) {
                log("setup env:" + env);
                setupEnv(env, lpparam.classLoader);
            } else {
                log(".ENV file damaged! " + pkgName);
            }
        }

        byte[] bytes = FileUtils.readBytes(Environment.getExternalStorageDirectory() + File.separator + "location");
        if (bytes != null) {
            String locationStr = new String(bytes);
            if (!TextUtils.isEmpty(locationStr)) {
                log(locationStr);
                String[] loc = locationStr.split(",");
                hook(lpparam.classLoader, Double.parseDouble(loc[2]), Double.parseDouble(loc[1]));
            }
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        log("initZygote with module path: " + startupParam.modulePath);
    }

    public void log(String text) {
        XposedBridge.log(text);
    }

    public boolean checkEnv(String pkg) {
        String filepath = "/data/data/"+pkg+"/.ENV";
        return new File(filepath).exists();
    }

    public Env getEnvFromConfigFile(String app) {
        String filepath = "/data/data/"+app+"/.ENV";
        return EnvManager.readEnv(filepath);
    }

//    public void hookBuildProperty(Env env) {
//        Class cls = XposedHelpers.findClass("android.os.Build", ClassLoader.getSystemClassLoader());
//        if (TextUtils.isEmpty(env.getBuildBoard())) {
////            log("Build property hook: Board " + env.getBuildBoard());
//            XposedHelpers.setStaticObjectField(cls, "BOARD", env.getBuildBoard());
//        }
//        if (TextUtils.isEmpty(env.getBuildManufacturer())) {
////            log("Build property hook: MANUFACTURER " + env.getBuildManufacturer());
//            XposedHelpers.setStaticObjectField(cls, "MANUFACTURER", env.getBuildManufacturer());
//        }
//        if (TextUtils.isEmpty(env.getBuildSerial())) {
////            log("Build property hook: SERIAL " + env.getBuildSerial());
//            XposedHelpers.setStaticObjectField(cls, "SERIAL", env.getBuildSerial());
//        }
//        if (TextUtils.isEmpty(env.getBuildModel())) {
////            log("Build property hook: MODEL " + env.getBuildModel());
//            XposedHelpers.setStaticObjectField(cls, "MODEL", env.getBuildModel());
//        }
//        if (TextUtils.isEmpty(env.getBuildBrand())) {
////            log("Build property hook: BRAND " + env.getBuildBrand());
//            XposedHelpers.setStaticObjectField(cls, "BRAND", env.getBuildBrand());
//        }
//    }


    public void setupEnv(final Env env, ClassLoader classLoader) {
        findAndHookMethod("android.telephony.TelephonyManager", classLoader, "getDeviceId", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                log("Fake deviceid " + env.getDeviceId() + " for " + pkgName);
                param.setResult(env.getDeviceId());
            }
        });

//        hookBuildProperty(env);
    }

    public void hook(ClassLoader classLoader, final double latitude, final double longtitude) {
        // 基站信息设置为Null
        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", classLoader,
                "getCellLocation", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(null);
                    }
                });

        // 把基站信息设置为NULL
        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", classLoader,
                "getNeighboringCellInfo", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(null);
                    }
                });

        XposedHelpers.findAndHookMethod(TelephonyManager.class, "getAllCellInfo", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(null);
            }
        });

        // ======================

        XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", classLoader, "getBSSID", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult("00-00-00-00-00-00-00-00");
            }
        });

        XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", classLoader, "getIpAddress", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(0);
            }
        });

        XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", classLoader, "getMacAddress", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult("00-00-00-00-00-00-00-00");
            }
        });

        // WIFI 信息集合
        XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", classLoader, "getSSID", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult("null");
            }
        });
        // ======================

        // WIFI 集合
        XposedHelpers.findAndHookMethod("android.net.wifi.WifiManager", classLoader, "getScanResults", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(null);
            }
        });

        XposedHelpers.findAndHookMethod("android.net.wifi.WifiManager", classLoader, "getWifiState", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(WifiManager.WIFI_STATE_DISABLED);
            }
        });

        XposedHelpers.findAndHookMethod("android.net.wifi.WifiManager", classLoader, "isWifiEnabled", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(false);
            }
        });

        // 纬度
        XposedHelpers.findAndHookMethod("android.location.Location", classLoader, "getLatitude", new XC_MethodHook() {

            @Override
            protected void beforeHookedMethod(MethodHookParam param)
                    throws Throwable {
                param.setResult(latitude);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                log("getLatitude" + param.getResult());
            }
        });

        // 经度
        XposedHelpers.findAndHookMethod("android.location.Location", classLoader, "getLongitude", new XC_MethodHook() {

            @Override
            protected void beforeHookedMethod(MethodHookParam param)
                    throws Throwable {
                param.setResult(longtitude);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                log("getLongitude" + param.getResult());
            }
        });


        XposedHelpers.findAndHookMethod(LocationManager.class, "getLastLocation", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Location l = new Location(LocationManager.GPS_PROVIDER);
                l.setLatitude(latitude);
                l.setLongitude(longtitude);
                l.setAccuracy(100f);
                l.setTime(0);
                param.setResult(l);
            }
        });

        XposedHelpers.findAndHookMethod(LocationManager.class, "getLastKnownLocation", String.class, new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Location l = new Location(LocationManager.GPS_PROVIDER);
                l.setLatitude(latitude);
                l.setLongitude(longtitude);
                l.setAccuracy(100f);
                l.setTime(0);
                param.setResult(l);
            }
        });

        XposedBridge.hookAllMethods(LocationManager.class, "getProviders", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<String> providers = (List<String>) param.getResult();
                if (providers != null) {
                    XposedBridge.log("getProviders list： " + providers.toString());
                }
                ArrayList<String> arrayList = new ArrayList<String>();
                arrayList.add("gps");
                param.setResult(arrayList);
            }
        });


        XposedBridge.hookAllMethods(LocationManager.class, "getAllProviders", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<String> providers = (List<String>) param.getResult();
                if (providers != null) {
                    XposedBridge.log("getProviders list： " + providers.toString());
                }
                ArrayList<String> arrayList = new ArrayList<String>();
                arrayList.add("gps");
                param.setResult(arrayList);
            }
        });

        XposedHelpers.findAndHookMethod(LocationManager.class, "getBestProvider", Criteria.class, Boolean.TYPE, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult("gps");
            }
        });

        XposedHelpers.findAndHookMethod(LocationManager.class, "addGpsStatusListener", GpsStatus.Listener.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (param.args[0] != null) {
                    XposedHelpers.callMethod(param.args[0], "onGpsStatusChanged", 1);
                    XposedHelpers.callMethod(param.args[0], "onGpsStatusChanged", 3);
                }
            }
        });

        XposedHelpers.findAndHookMethod(LocationManager.class, "addNmeaListener", GpsStatus.NmeaListener.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(false);
            }
        });

        XposedHelpers.findAndHookMethod("android.location.LocationManager", classLoader,
                "getGpsStatus", GpsStatus.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        GpsStatus gss = (GpsStatus) param.getResult();
                        if (gss == null)
                            return;

                        Class<?> clazz = GpsStatus.class;
                        Method m = null;
                        for (Method method : clazz.getDeclaredMethods()) {
                            if (method.getName().equals("setStatus")) {
                                if (method.getParameterTypes().length > 1) {
                                    m = method;
                                    break;
                                }
                            }
                        }
                        if (m == null)
                            return;

                        //access the private setStatus function of GpsStatus
                        m.setAccessible(true);

                        //make the apps belive GPS works fine now
                        int svCount = 5;
                        int[] prns = {1, 2, 3, 4, 5};
                        float[] snrs = {0, 0, 0, 0, 0};
                        float[] elevations = {0, 0, 0, 0, 0};
                        float[] azimuths = {0, 0, 0, 0, 0};
                        int ephemerisMask = 0x1f;
                        int almanacMask = 0x1f;

                        //5 satellites are fixed
                        int usedInFixMask = 0x1f;

                        XposedHelpers.callMethod(gss, "setStatus", svCount, prns, snrs, elevations, azimuths, ephemerisMask, almanacMask, usedInFixMask);
                        param.args[0] = gss;
                        param.setResult(gss);
                        try {
                            m.invoke(gss, svCount, prns, snrs, elevations, azimuths, ephemerisMask, almanacMask, usedInFixMask);
                            param.setResult(gss);
                        } catch (Exception e) {
                            XposedBridge.log(e);
                        }
                    }
                });

        XposedBridge.hookAllMethods(LocationManager.class, "requestLocationUpdates", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (param.args.length >= 4 && (param.args[2] instanceof LocationListener)) {
                    LocationListener ll = (LocationListener) param.args[2];

                    Class<?> clazz = LocationListener.class;
                    Method m = null;
                    for (Method method : clazz.getDeclaredMethods()) {
                        if (method.getName().equals("onLocationChanged") && !Modifier.isAbstract(method.getModifiers())) {
                            m = method;
                            break;
                        }
                    }
                    Location l = new Location(LocationManager.GPS_PROVIDER);
                    l.setLatitude(latitude);
                    l.setLongitude(longtitude);
                    l.setAccuracy(10.00f);
                    l.setTime(0);
                    XposedHelpers.callMethod(ll, "onLocationChanged", l);
                    try {
                        if (m != null) {
                            m.invoke(ll, l);
                        }
                    } catch (Exception e) {
                        XposedBridge.log(e);
                    }
                }
            }
        }) ;

    }
}
