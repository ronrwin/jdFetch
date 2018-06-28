package com.example.jddata.util;

import android.os.Environment;

import com.example.jddata.util.ExecUtils;

public class ScreenUtils {
    public static void scrrenShot() {
        String command = "screencap -p " + Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/" + System.currentTimeMillis() + ".png";
        ExecUtils.handleExecCommand(command);
    }
}
