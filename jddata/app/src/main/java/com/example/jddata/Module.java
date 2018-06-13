package com.example.jddata;


import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import java.lang.reflect.Field;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Module extends XC_MethodHook implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        String packageName = lpparam.packageName;

        XposedBridge.log("load app: " + packageName);//显示加载的 app 名称

//        if (packageName.equals(PACKAGE_WHATSAPP) || packageName.equals(MY_PACKAGE_NAME)) {
//            DeviceInfoHook.initAllHooks(lpparam);
//        }

        Field filField = XposedHelpers.findFieldIfExists(View.class, "mTouchDelegate");
        if (filField != null) {
            XposedBridge.log(filField.toString());
        }

//        XposedHelpers.findAndHookMethod("android.view.View", lpparam.classLoader, "setTouchDelegate", new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                XposedBridge.log(param.toString());
//            }
//
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                super.beforeHookedMethod(param);
//            }
//        });
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
    }
}
