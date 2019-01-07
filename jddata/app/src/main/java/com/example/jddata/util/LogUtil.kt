package com.example.jddata.util

import android.os.Environment
import android.text.TextUtils
import android.util.Log
import com.example.jddata.BusHandler
import com.example.jddata.Entity.MessageDef
import com.example.jddata.Entity.RowData
import com.example.jddata.GlobalInfo
import com.example.jddata.MainApplication
import com.example.jddata.action.Action
import com.example.jddata.shelldroid.EnvManager
import com.example.jddata.storage.database
import com.example.jddata.storage.toVarargArray
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.transaction
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class LogUtil {
    companion object {
        @JvmField val EXCEL_FILE_FOLDER = Environment.getExternalStorageDirectory().toString() + "/Pictures/jdFetch"
        @JvmField var log = StringBuilder("")
        @JvmField var rowDatas = ArrayList<RowData>()

        @JvmStatic fun getExternalFolder(): String {
            return "${EXCEL_FILE_FOLDER}/账号/"
        }

        /**
         * 记录执行结果
         */
        @JvmStatic fun writeResultLog(content: String) {
            val resultContent = ExecUtils.getCurrentTimeString() + " : " + content + "\n"
            BusHandler.instance.singleThreadExecutor.execute {
                val date = ExecUtils.getCurrentTimeString(SimpleDateFormat("MM-dd"))
                var filename = "resultLog_日期${date}.txt"
                FileUtils.writeToFile(EXCEL_FILE_FOLDER, filename, resultContent, true)
            }
        }

        @JvmStatic fun logCache(content: String) {
            Log.w("jdFetch", content);
            log.append(ExecUtils.getCurrentTimeString() + " : " + content + "\n")
        }

        @JvmStatic fun dataCache(row: RowData) {
            rowDatas.add(row)
        }

        @JvmStatic fun writeOutputTxt(filename: String, content: String) {
            val resultContent = ExecUtils.getCurrentTimeString() + " : " + content + "\n"
            BusHandler.instance.singleThreadExecutor.execute {
                FileUtils.writeToFile(LogUtil.getMobileFolder(), filename, resultContent, true)
            }
        }

        /**
         * 记录动作执行
         */
        @JvmStatic fun writeMove(action: Action) {
            BusHandler.instance.singleThreadExecutor.execute {
                val deviceCreateTime = EnvManager.sCurrentEnv.createTime!!
                val imei = EnvManager.sCurrentEnv.deviceId!!
                val deviceId = "${EnvManager.sCurrentEnv.envName}"

                var moveColumn = ""

                val extra = action.map?.get(GlobalInfo.EXTRA)
                if (extra is String) {
                    moveColumn = "${moveColumn},${extra}"
                }

                val content = "${deviceId},${imei},${deviceCreateTime},${action.createTime},${EnvManager.sCurrentEnv.location?.name},${moveColumn}"

                FileUtils.writeToFile(EXCEL_FILE_FOLDER, "动作序列表.csv", content + "\n", true, "gb2312")
            }
        }

        @JvmStatic fun flushLog() {
            flushLog(true)
        }

        @JvmStatic fun flushLog(writeDatabase: Boolean) {
            val flushLog = log.toString() + "\n"
            log = StringBuilder("")

            if (writeDatabase) {
                BusHandler.instance.singleThreadExecutor.execute {
                    MainApplication.sContext.database.use {
                        transaction {
                            for (row in rowDatas) {
                                insert(GlobalInfo.TABLE_NAME,
                                        *row.map.toVarargArray())
                            }
                            rowDatas.clear()
                        }
                    }
                }
            } else {
                rowDatas.clear()
            }

            BusHandler.instance.singleThreadExecutor.execute{
                FileUtils.writeToFile(getMobileFolder(), "log.txt", flushLog, true)
            }
        }

        @JvmStatic fun getMobileFolder(): String {
            val time = System.currentTimeMillis()//long now = android.os.SystemClock.uptimeMillis();
            val format = SimpleDateFormat("yyyy_MM_dd")
            val d1 = Date(time)
            val t1 = format.format(d1)
            var folder = getExternalFolder() + t1 + File.separator + "source"
            folder = getExternalFolder() + t1 + File.separator + EnvManager.sCurrentEnv.envName + "号账号"

            val folderFile = File(folder)
            if (!folderFile.exists()) {
                folderFile.mkdirs()
            }
            return folder
        }
    }
}