package com.example.jddata;

import android.os.Environment;

public class ScreenUtils {
    public static void scrrenShot() {
        String command = "screencap -p " + Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/" + System.currentTimeMillis() + ".png";
        ExecUtils.handleExecCommand(command);
    }
}
