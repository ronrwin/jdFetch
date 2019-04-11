package com.example.jddata.util

import android.os.Environment
import android.util.Log
import com.example.jddata.BusHandler
import com.example.jddata.Entity.RowData
import com.example.jddata.GlobalInfo
import com.example.jddata.MainApplication
import com.example.jddata.action.Action
import com.example.jddata.shelldroid.Env
import com.example.jddata.storage.database
import com.example.jddata.storage.toVarargArray
import org.jetbrains.anko.db.*
import java.io.File

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
        @JvmStatic fun getDeviceFolder(env: Env): String {
            val folder = "${getDateFolder()}/${env.envName}"

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
            Log.v(TAG, content)
            log.append(ExecUtils.getCurrentTimeString() + " : " + content + "\n")
        }

        // 行为日志
        @JvmStatic fun logCache(level: String, content: String) {
            when (level) {
                "debug" -> {
                    Log.d(TAG, content)
                }
                "warn" -> {
                    Log.w(TAG, content)
                }
                "info" -> {
                    Log.i(TAG, content)
                }
                "error" -> {
                    Log.e(TAG, content)
                }
                "verbose" -> {
                    Log.v(TAG, content)
                }
            }
            log.append(ExecUtils.getCurrentTimeString() + " : " + content + "\n")
        }

        // 数据库行数据缓存
        @JvmStatic fun dataCache(row: RowData) {
            Log.d("zfr", row.map.toString())
            rowDatas.add(row)
        }

        /**
         * 记录动作执行
         */
        @JvmStatic fun writeMove(action: Action) {
            val extra = action.map?.get(GlobalInfo.EXTRA)
            if (extra != null) {
                BusHandler.instance.singleThreadExecutor.execute {
                    val deviceCreateTime = action.env!!.createTime!!
                    val imei = action.env!!.imei!!
                    val deviceId = "${action.env!!.envName}"

                    var moveColumn = ""

                    if (extra is String) {
                        moveColumn = "${moveColumn},${extra}"
                    }

                    val content = "${deviceId},${imei},${deviceCreateTime},${action.createTime},${action.env!!.locationName},${moveColumn}"

                    FileUtils.writeToFile(EXCEL_FILE_FOLDER, "动作序列表.csv", content + "\n", true, "gb2312")
                }
            }
        }

        // 把数据库行数据缓存，写到数据库中
        // 把本次动作日志，写到设备的日志记录中
        @JvmStatic fun flushLog(env: Env, writeDatabase: Boolean) {
            val flushLog = log.toString() + "\n"
            log = StringBuilder("")

            if (writeDatabase) {
                // todo: 数据
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
                FileUtils.writeToFile(getDeviceFolder(env), "log.txt", flushLog, true)
            }
        }


    }
}