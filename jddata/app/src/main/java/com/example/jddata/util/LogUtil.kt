package com.example.jddata.util

import android.os.Environment
import android.text.TextUtils
import android.util.Log
import com.example.jddata.BusHandler
import com.example.jddata.Entity.MessageDef
import com.example.jddata.Entity.RowData
import com.example.jddata.Entity.parser
import com.example.jddata.GlobalInfo
import com.example.jddata.MainApplication
import com.example.jddata.shelldroid.EnvManager
import com.example.jddata.storage.database
import com.example.jddata.storage.toVarargArray
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.transaction
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class LogUtil {
    companion object {
        @JvmField val EXCEL_FILE_FOLDER = Environment.getExternalStorageDirectory().toString() + "/Pictures/jdFetch/"
        @JvmField var log = StringBuilder("")
        @JvmField var rowDatas = ArrayList<RowData>()

        /**
         * 总体的log
         */
        @JvmStatic fun writeAllLog(content: String) {
            val writeLog = content + "\n"
            BusHandler.instance.singleThreadExecutor.execute {
                FileUtils.writeToFile(LogUtil.EXCEL_FILE_FOLDER, "allLog.txt", writeLog, true)
            }
        }

        @JvmStatic fun wroteFailLog(content: String) {
            val writeLog = content + "\n"
            BusHandler.instance.singleThreadExecutor.execute {
                FileUtils.writeToFile(getFolder(), "failLog.txt", writeLog, true)
            }
        }

        @JvmStatic fun writeLog(content: String) {
            Log.w("zfr", content);
            log.append(ExecUtils.getCurrentTimeString() + " : " + content + "\n")
        }

        @JvmStatic fun writeDataLog(row: RowData) {
            rowDatas.add(row)
        }

        @JvmStatic fun taskEnd() {
            if (!GlobalInfo.sIsTest) {
                val content = "------ singleActionType : " + GlobalInfo.singleType + " taskEnd"
                writeAllLog(ExecUtils.getCurrentTimeString() + " : " + content + "\n")
            } else {
                BusHandler.instance.removeMessages(MessageDef.MSG_TIME_OUT)
            }
        }

        @JvmStatic fun flushLog() {
            val flushLog = log.toString() + "\n"
            writeAllLog(flushLog)
            log = StringBuilder("")

            MainApplication.getContext().database.use {
                transaction {
                    for (row in rowDatas) {
                        insert(GlobalInfo.TABLE_NAME,
                                *row.map.toVarargArray())
                        FileUtils.writeToFile(getFolder(), "sslog.txt", row.toString() + "\n", true)
                    }
                    rowDatas.clear()
                }
            }

            BusHandler.instance.singleThreadExecutor.execute(Runnable {
                FileUtils.writeToFile(getFolder(), "log.txt", flushLog, true)
                getDatabaseDatas();
            })
        }

        @JvmStatic fun getDatabaseDatas() {
            val sb = StringBuilder()
            MainApplication.getContext().database.use {
                val builder = select(GlobalInfo.TABLE_NAME)
                val rows = builder.parseList(parser)
                for (row in rows) {
                    sb.append(row.invoke().toString() + "\n")
                }
            }
            FileUtils.writeToFile(getFolder(), "sslog.cvs", sb.toString(), false)
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