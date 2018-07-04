package com.example.jddata.shelldroid;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.example.jddata.util.FileUtils;
import com.example.jddata.MainApplication;
import com.example.jddata.util.StringUtils;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

public class EnvManager {
    public static String TAG = "EnvManager";
    public static Env sCurrentEnv;
    
    public static String envRepoPath() {
        return MainApplication.getContext().getFilesDir().toString() + "/ENV_REPO";
    }

    public static void clear() {
         File file = new File(envRepoPath());
         if (file.exists()) {
             file.delete();
         }
    }

    public static String envRepo() {
        File repo = new File(envRepoPath());
        if (!repo.exists()) {
            repo.mkdirs();
        }
        return envRepoPath();
    }

    public static Env readEnv(String filepath) {
        byte[] bytes = FileUtils.readBytes(filepath);
        if (bytes != null) {
            try {
                String json = new String(bytes, "UTF-8");
                return JSON.parseObject(json, Env.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void saveEnv(Env env, String filepath) {
        Log.d(TAG, "Save env: "+env + " to " + filepath);
        try {
            PrintWriter out = new PrintWriter(filepath, "utf8");
            String jsonString = JSON.toJSONString(env);
            out.print(jsonString);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    public static ArrayList<Env> scanEnvs() {
        File root = new File(envRepo());
        ArrayList<String> appCurrentEnv = new ArrayList<>();
        ArrayList<String> appEnvs = new ArrayList<>();
        ArrayList<String> allEnvs = new ArrayList<>();
        if (root.exists()) {
            File[] pkgFiles = root.listFiles();
            for (File pkgfile : pkgFiles) {
                if (pkgfile.isDirectory()) {
                    appCurrentEnv.add(pkgfile.getAbsolutePath() + "/.RUNNING");
                    File[] appFiles = pkgfile.listFiles();
                    for (File appfile : appFiles) {
                        if (appfile.isDirectory()) {
                            appEnvs.add(appfile.getAbsolutePath() + "/.ENV");
                        }
                    }
                }
            }
        }
        allEnvs.addAll(appCurrentEnv);
        allEnvs.addAll(appEnvs);

        ArrayList<Env> envs = new ArrayList<>();
        String activeEnv = "";
        for (String envPath : allEnvs) {
            Log.d(TAG, "Find Env: " + envPath);
            Env env = readEnv(envPath);
            if (env != null) {
                if (envPath.contains("RUNNING")) {
                    if (!env.appName.contains("active")) {
                        env.appName = env.appName + "active";
                    }
                    activeEnv = env.envName;
                    envs.add(0, env);
                } else {
                    if (!env.envName.equals(activeEnv)) {
                        if (env.active) {
                            env.appName = env.appName + " used ";
                        }
                        env.appName = env.appName.replace("active", "");
                        envs.add(env);
                    }
                }
            }
        }
        return envs;
    }

    public static void ensureSelinuxPermissive() {
        Log.d(TAG, "ensureSelinuxPermissive");
        try {
            Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", "setenforce permissive"});
            proc.waitFor();
        } catch (Exception e) {
            Log.e(TAG, "Fail to disable selinux");
            e.printStackTrace();
        }
    }

    public static boolean doRoot(String cmd) {
        Log.d(TAG, "doRoot: " + cmd);
        try {
            Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
            proc.waitFor();
        } catch (Exception e) {
            Log.e(TAG, "Fail to run cmd: " + cmd);
            e.printStackTrace();
        }
        return true;
    }

    public static void killApp(Env env) {
        String cmd = "am force-stop " + env.pkgName;
        doRoot(cmd);
    }

    public static void startApp(Env env) {
        String cmd = "monkey -p " + env.pkgName + " -c android.intent.category.LAUNCHER 1";
        doRoot(cmd);
    }

    public static void updateAppLastRunning(Env env) {
        Log.d(TAG, "update last running: " + env);
        String filepath = getAppRepoDir(env) + "/.RUNNING";
        try {
            Env newEnv = env.clone();
            newEnv.active = true;
            saveEnv(newEnv, filepath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Env appLastRunning(Env env) {
        String filepath = getAppRepoDir(env) + "/.RUNNING";
        return readEnv(filepath);
    }

    public static String getAppRepoDir(Env env) {
        return envRepo() + "/" + env.pkgName;
    }

    public static String getEnvDir(Env env) {
        return getAppRepoDir(env) + "/" + env.id;
    }

    public static String  getAppDir(Env env) {
        return "/data/data/" + env.pkgName;
    }

    public static boolean envDirExist(Env env) {
        return new File(getEnvDir(env)).exists();
    }

    public static void envDirBuild(Env env) {
        if (TextUtils.isEmpty(env.deviceId)) {
            env.deviceId = "8651210" + StringUtils.getNumRandomString(8);
        }

        doRoot("mkdir -p " + getEnvDir(env));
        doRoot(String.format("cp -a %s/lib %s", getAppDir(env), getEnvDir(env)));
        doRoot(String.format("chmod -R 777 %s", getAppRepoDir(env)));
        String envFile = getEnvDir(env) + "/.ENV";
        saveEnv(env, envFile);
        doRoot(String.format("chmod 777 %s", envFile));
    }

    public static void switchEnv(Env env, Env lastEnv) {
        killApp(env);
        doRoot(String.format("mv %s %s", getAppDir(env), getEnvDir(lastEnv)));
        doRoot(String.format("mv %s %s", getEnvDir(env), getAppDir(env)));
    }

    public static void delete(Env env) {
        doRoot(String.format("rm -fr %s", getEnvDir(env)));
    }

    public static boolean activeByName(String name) {
        ArrayList<Env> envs = scanEnvs();
        for (Env env : envs) {
            if (env.envName.equals(name)) {
                active(env);
                return true;
            }
        }
        return false;
    }

    public static void active(Env env) {
        Log.d(TAG, "active env:\n" + env);
        Env last = appLastRunning(env);
        if (last == null) {
            if (!envDirExist(env)) {
                envDirBuild(env);
            }
            try {
                Env newEnv = env.clone();
                newEnv.id = "pre-shelldroid-data";
                switchEnv(env, newEnv);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (last != null && !last.id.equals(env.id)) {
                Log.d(TAG, "last env:\n" + env);
                if (!envDirExist(env)) {
                    envDirBuild(env);
                }
                switchEnv(env, last);
            }
        }
        updateAppLastRunning(env);
        startApp(env);
        sCurrentEnv = env;
    }

    public static Env createJDApp(String pkgName, String envName) {
        ArrayList<AppInfo> data = AndroidUtils.getInstalledAppInfo();
        for (AppInfo appInfo : data) {
            if (appInfo.pkgName.equals(pkgName)) {
                Env env = new Env();
                env.id = java.util.UUID.randomUUID().toString();
                env.envName = envName;
                env.appName = appInfo.appName;
                env.pkgName = pkgName;
                env.active = false;
                env.deviceId = "";
                env.buildModel = "";
                env.buildManufacturer = "";
                env.buildBrand = "";

                return env;
            }
        }
        return null;
    }
}
