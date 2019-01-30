package com.example.jddata.util

import android.os.Environment
import android.util.Log
import com.example.jddata.BusHandler
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
        @JvmField val TAG = "jdFetch"

        // 二级目录：日期
        @JvmStatic fun getDateFolder(): String {
            return "${EXCEL_FILE_FOLDER}/${ExecUtils.today()}"
        }

        // 三级目录：设备
        @JvmStatic fun getDeviceFolder(): String {
            val folder = "${getDateFolder()}/${EnvManager.sCurrentEnv.envName}"

            val folderFile = File(folder)
            if (!folderFile.exists()) {
                folderFile.mkdirs()
            }
            return folder
        }

        /**
         * 记录执行结果
         */
        @JvmStatic fun writeResultLog(content: String) {
            val resultContent = ExecUtils.getCurrentTimeString() + " : " + content + "\n"
            BusHandler.instance.singleThreadExecutor.execute {
                var filename = "resultLog.txt"
                FileUtils.writeToFile("${getDateFolder()}", filename, resultContent, true)
            }
        }

        // 行为日志
        @JvmStatic fun logCache(content: String) {
            Log.w(TAG, content);
            log.append(ExecUtils.getCurrentTimeString() + " : " + content + "\n")
        }

        // 数据库行数据缓存
        @JvmStatic fun dataCache(row: RowData) {
            rowDatas.add(row)
        }

        /**
         * 记录动作执行
         */
        @JvmStatic fun writeMove(action: Action) {
            val extra = action.map?.get(GlobalInfo.EXTRA)
            if (extra != null) {
                BusHandler.instance.singleThreadExecutor.execute {
                    val deviceCreateTime = EnvManager.sCurrentEnv.createTime!!
                    val imei = EnvManager.sCurrentEnv.imei!!
                    val deviceId = "${EnvManager.sCurrentEnv.envName}"

                    var moveColumn = ""


                    if (extra is String) {
                        moveColumn = "${moveColumn},${extra}"
                    }

                    val content = "${deviceId},${imei},${deviceCreateTime},${action.createTime},${EnvManager.sCurrentEnv.location?.name},${moveColumn}"

                    FileUtils.writeToFile(EXCEL_FILE_FOLDER, "动作序列表.csv", content + "\n", true, "gb2312")
                }
            }
        }

        @JvmStatic fun flushLog() {
            flushLog(true)
        }

        // 把数据库行数据缓存，写到数据库中
        // 把本次动作日志，写到设备的日志记录中
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

            // 输出这次动作的日志到设备目录log.txt中
            BusHandler.instance.singleThreadExecutor.execute{
                FileUtils.writeToFile(getDeviceFolder(), "log.txt", flushLog, true)
            }
        }

    }
}