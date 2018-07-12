package com.example.jddata.util

import android.os.Environment
import android.text.TextUtils
import android.util.Log
import com.example.jddata.BusHandler
import com.example.jddata.Entity.MessageDef
import com.example.jddata.GlobalInfo
import com.example.jddata.shelldroid.EnvManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class LogUtil {
    companion object {
        @JvmField val EXCEL_FILE_FOLDER = Environment.getExternalStorageDirectory().toString() + "/Pictures/jdFetch/"
        @JvmField var log = StringBuilder("")
        @JvmField var flushLog = ""

        /**
         * 总体的log
         */
        @JvmStatic fun writeAllLog(content: String) {
            val writeLog = content + "\n"
            BusHandler.instance.singleThreadExecutor.execute {
                FileUtils.writeToFile(LogUtil.EXCEL_FILE_FOLDER, "allLog.txt", writeLog, true)
            }
        }

        @JvmStatic fun writeLog(content: String) {
            Log.w("zfr", content);
            log.append(ExecUtils.getCurrentTimeString(SimpleDateFormat("MM-dd HH:mm:ss:SSS")) + " : " + content + "\n")
        }

        @JvmStatic fun taskEnd() {
            if (!GlobalInfo.sIsTest) {
                val content = "------ singleActionType : " + GlobalInfo.singleType + " taskEnd"
                writeAllLog(ExecUtils.getCurrentTimeString(SimpleDateFormat("MM-dd HH:mm:ss:SSS")) + " : " + content + "\n")
            } else {
                BusHandler.instance.removeMessages(MessageDef.MSG_TIME_OUT)
            }
        }

        @JvmStatic fun flushLog() {
            flushLog = log.toString() + "\n"
            writeAllLog(flushLog)
            log = StringBuilder("")
            BusHandler.instance.singleThreadExecutor.execute(Runnable {
                FileUtils.writeToFile(getFolder(), "log.txt", flushLog, true)
            })
        }

        @JvmStatic fun getFolder(): String {
            val time = System.currentTimeMillis()//long now = android.os.SystemClock.uptimeMillis();
            val format = SimpleDateFormat("yyyy_MM_dd")
            val d1 = Date(time)
            val t1 = format.format(d1)
            var folder = EXCEL_FILE_FOLDER + "source"

            if (!TextUtils.isEmpty(GlobalInfo.sTargetEnvName)) {
                folder = EXCEL_FILE_FOLDER + t1 + File.separator + GlobalInfo.sTargetEnvName
            } else {
                if (EnvManager.sCurrentEnv != null) {
                    folder = EXCEL_FILE_FOLDER + t1 + File.separator + EnvManager.sCurrentEnv.envName
                }
            }

            val folderFile = File(folder)
            if (!folderFile.exists()) {
                folderFile.mkdirs()
            }
            return folder
        }
    }
}