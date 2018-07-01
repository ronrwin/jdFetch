package com.example.jddata.xpose;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;

import dalvik.system.DexFile;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class PackageHooker {

    private final XC_LoadPackage.LoadPackageParam loadPackageParam;

    public PackageHooker(XC_LoadPackage.LoadPackageParam param) {
        loadPackageParam = param;
        try {
            hook();
        } catch (IOException | ClassNotFoundException e) {
            XposedBridge.log(e);
        }
    }

    public void hook() throws IOException, ClassNotFoundException {
        DexFile dexFile = new DexFile(loadPackageParam.appInfo.sourceDir);
        Enumeration<String> classNames = dexFile.entries();
        while (classNames.hasMoreElements()) {
            String className = classNames.nextElement();

            if (isClassNameValid(className)) {
                Class clazz = Class.forName(className, false, loadPackageParam.classLoader);
                for (Method method : clazz.getDeclaredMethods()) {
                    if (!Modifier.isAbstract(method.getModifiers())) {
                        XposedBridge.log("clazz: " + clazz);
                        XposedBridge.hookMethod(method, new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                XposedBridge.log("HOOKED: " + param.method.getName());
                            }
                        });
                    }
                }
            }

        }
    }

    public boolean isClassNameValid(String className) {
        return className.startsWith(loadPackageParam.packageName)
                && !className.contains("$")
                && !className.contains("BuildConfig")
                && !className.equals(loadPackageParam.packageName + ".R");
    }
}
