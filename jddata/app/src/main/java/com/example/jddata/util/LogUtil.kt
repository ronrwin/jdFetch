package com.example.jddata.util

import android.os.Environment
import android.text.TextUtils
import android.util.Log
import com.example.jddata.BusHandler
import com.example.jddata.GlobalInfo
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class LogUtil {
    companion object {
        @JvmField val EXCEL_FILE_FOLDER = Environment.getExternalStorageDirectory().toString() + "/Pictures/"
        @JvmField var log = StringBuilder("")
        @JvmField var flushLog = ""

        @JvmStatic fun writeLog(content: String) {
            Log.w("zfr", content);
            log.append(content + "\n")
        }

        @JvmStatic fun flushLog() {
            flushLog = log.toString()
            log = StringBuilder("")
            BusHandler.getInstance().singleThreadExecutor.execute(Runnable {
                FileUtils.writeToFile(getFolder(), "log.txt", flushLog, true)
            })
        }

        @JvmStatic fun getFolder(): String {
            val time = System.currentTimeMillis()//long now = android.os.SystemClock.uptimeMillis();
            val format = SimpleDateFormat("yyyy_MM_dd")
            val d1 = Date(time)
            val t1 = format.format(d1)

            if (!TextUtils.isEmpty(GlobalInfo.sTargetEnvName)) {
                val folder = EXCEL_FILE_FOLDER + t1 + File.separator + GlobalInfo.sTargetEnvName
                val folderFile = File(folder)
                if (!folderFile.exists()) {
                    folderFile.mkdirs()
                }
                return folder
            }

            val folder = EXCEL_FILE_FOLDER + "source"
            val folderFile = File(folder)
            if (!folderFile.exists()) {
                folderFile.mkdirs()
            }
            return folder
        }
    }
}