package com.example.jddata.xpose;

import android.os.Environment;
import android.telephony.CellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;

import com.example.jddata.MainApplication;
import com.example.jddata.shelldroid.Env;
import com.example.jddata.shelldroid.EnvManager;
import com.example.jddata.util.FileUtils;

import java.io.File;

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
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        log("initZygote with module path: " + startupParam.modulePath);
    }

    public void log(String text) {
//        XposedBridge.log(text);
        Log.w("zfr_hook", text);
    }

    public boolean checkEnv(String pkg) {
        String filepath = "/data/data/"+pkg+"/.ENV";
        return new File(filepath).exists();
    }

    public Env getEnvFromConfigFile(String app) {
        String filepath = "/data/data/"+app+"/.ENV";
        return EnvManager.readEnv(filepath);
    }

    public void hookBuildProperty(Env env) {
        Class cls = XposedHelpers.findClass("android.os.Build", ClassLoader.getSystemClassLoader());
        if (TextUtils.isEmpty(env.buildBoard)) {
            log("Build property hook: Board " + env.buildBoard);
            XposedHelpers.setStaticObjectField(cls, "BOARD", env.buildBoard);
        }
        if (TextUtils.isEmpty(env.buildManufacturer)) {
            log("Build property hook: MANUFACTURER " + env.buildManufacturer);
            XposedHelpers.setStaticObjectField(cls, "MANUFACTURER", env.buildManufacturer);
        }
        if (TextUtils.isEmpty(env.buildSerial)) {
            log("Build property hook: SERIAL " + env.buildSerial);
            XposedHelpers.setStaticObjectField(cls, "SERIAL", env.buildSerial);
        }
        if (TextUtils.isEmpty(env.buildModel)) {
            log("Build property hook: MODEL " + env.buildModel);
            XposedHelpers.setStaticObjectField(cls, "MODEL", env.buildModel);
        }
        if (TextUtils.isEmpty(env.buildBrand)) {
            log("Build property hook: BRAND " + env.buildBrand);
            XposedHelpers.setStaticObjectField(cls, "BRAND", env.buildBrand);
        }
    }

    public void locationHook(Env env, ClassLoader classLoader) {
        findAndHookMethod("android.telephony.TelephonyManager", classLoader, "getCellLocation", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                log("Fake empty cell location for " + pkgName);
                log("real result: " + param.getResult());
                CellLocation location = new GsmCellLocation();
                param.setResult(null);
            }
        });
        findAndHookMethod("android.telephony.TelephonyManager", classLoader, "getNeighboringCellInfo", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                log("Fake empty neighbor cell location for " + pkgName);
                log("real result: " + param.getResult());
                param.setResult(null);
            }
        });
        findAndHookMethod("android.net.wifi.WifiManager", classLoader, "getScanResults", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                log("Fake empty wifi scan results for " + pkgName);
                log("real result: " + param.getResult());
                param.setResult(null);
            }
        });
    }

    public void setupEnv(final Env env, ClassLoader classLoader) {
        findAndHookMethod("android.telephony.TelephonyManager", classLoader, "getDeviceId", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                log("Fake deviceid " + env.deviceId + " for " + pkgName);
                param.setResult(env.deviceId);
            }
        });

        hookBuildProperty(env);
        if (env.location != null) {
            locationHook(env, classLoader);
            GPShook.HookAndChange(classLoader, env.location.latitude, env.location.longitude);
        }

//        String locationStr = new String(FileUtils.readBytes(Environment.getExternalStorageDirectory() + "/location"));
//        if (!TextUtils.isEmpty(locationStr)) {
//            log("locationStr : " + locationStr);
//            String[] loc = locationStr.split(",");
//            GPShook.HookAndChange(classLoader, Double.parseDouble(loc[0]), Double.parseDouble(loc[1]));
//        }
        GPShook.HookAndChange(classLoader, 208.95, 34.27);
    }
}
