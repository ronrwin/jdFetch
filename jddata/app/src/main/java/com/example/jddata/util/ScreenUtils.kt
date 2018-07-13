package com.example.jddata.util

import android.os.Environment

import com.example.jddata.util.ExecUtils

class ScreenUtils {
    companion object {
        @JvmStatic fun scrrenShot() {
            val command = "screencap -p " + Environment.getExternalStorageDirectory().absolutePath + "/Pictures/" + System.currentTimeMillis() + ".png"
            ExecUtils.handleExecCommand(command)
        }
    }
}
