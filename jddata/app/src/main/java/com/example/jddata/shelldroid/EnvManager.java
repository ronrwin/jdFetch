package com.example.jddata.shelldroid;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.example.jddata.util.ExecUtils;
import com.example.jddata.util.FileUtils;
import com.example.jddata.MainApplication;
import com.example.jddata.util.StringUtils;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

public class EnvManager {
    public static String TAG = "EnvManager";
    public static Env sCurrentEnv;
    public static ArrayList<Env> envs = new ArrayList<>();
    
    public static String envRepoPath() {
        return MainApplication.getContext().getFilesDir().toString() + "/ENV_REPO";
    }

    public static void clear() {
        String cmd = "rm -f -r " + envRepoPath();
        doRoot(cmd);
        envs = scanEnvs();
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
                    if (!env.getAppName().contains("active")) {
                        env.setAppName(env.getAppName() + "active");
                    }
                    activeEnv = env.getEnvName();
                    envs.add(0, env);
                } else {
                    if (!env.getEnvName().equals(activeEnv)) {
                        if (env.getActive()) {
                            env.setAppName(env.getAppName() + " used ");
                        }
                        env.setAppName(env.getAppName().replace("active", ""));
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
        String cmd = "am force-stop " + env.getPkgName();
        doRoot(cmd);
    }

    public static void startApp(Env env) {
        String cmd = "monkey -p " + env.getPkgName() + " -c android.intent.category.LAUNCHER 1";
        doRoot(cmd);
    }

    public static void updateAppLastRunning(Env env) {
        Log.d(TAG, "update last running: " + env);
        String filepath = getAppRepoDir(env) + "/.RUNNING";
        try {
            Env newEnv = env.clone();
            newEnv.setActive(true);
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
        return envRepo() + "/" + env.getPkgName();
    }

    public static String getEnvDir(Env env) {
        return getAppRepoDir(env) + "/" + env.getId();
    }

    public static String  getAppDir(Env env) {
        return "/data/data/" + env.getPkgName();
    }

    public static boolean envDirExist(Env env) {
        return new File(getEnvDir(env)).exists();
    }

    public static void envDirBuild(Env env) {
        if (TextUtils.isEmpty(env.getDeviceId())) {
            env.setDeviceId("865121" + StringUtils.getNumRandomString(9));
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
        if (envs.isEmpty()) {
            return false;
        }

        for (Env env : envs) {
            if (env.getEnvName().equals(name)) {
                active(env);
                return true;
            }
        }
        return false;
    }

    public static boolean activeByIndex(int index) {
        if (!envs.isEmpty() && index < envs.size()) {
            active(envs.get(index));
            return true;
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
                newEnv.setId("pre-shelldroid-data");
                switchEnv(env, newEnv);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (last != null && !last.getId().equals(env.getId())) {
                Log.d(TAG, "last env:\n" + env);
                if (!envDirExist(env)) {
                    envDirBuild(env);
                }
                switchEnv(env, last);
            } else {
                killApp(env);
            }
        }
        updateAppLastRunning(env);
        startApp(env);
        sCurrentEnv = env;
    }

    public static Env createJDApp(String pkgName, String envName) {
        ArrayList<AppInfo> data = AndroidUtils.getInstalledAppInfo();
        for (AppInfo appInfo : data) {
            if (appInfo.getPkgName().equals(pkgName)) {
                Env env = new Env();
                env.setId(java.util.UUID.randomUUID().toString());
                env.setEnvName(envName);
                env.setAppName(appInfo.getAppName());
                env.setPkgName(pkgName);
                env.setActive(false);
                env.setDeviceId("");
                env.setBuildModel("");
                env.setBuildManufacturer("");
                env.setBuildBrand("");
                env.setCreateTime(ExecUtils.getCurrentTimeString());

                return env;
            }
        }
        return null;
    }
}
