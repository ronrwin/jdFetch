package com.example.jddata.util

import android.os.Environment
import android.text.TextUtils
import android.util.Log
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
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
        @JvmField val EXCEL_FILE_FOLDER = Environment.getExternalStorageDirectory().toString() + "/Pictures/jdFetch/"
        @JvmField var log = StringBuilder("")
        @JvmField var rowDatas = ArrayList<RowData>()

        @JvmStatic fun getExternalFolder(): String {
            return EXCEL_FILE_FOLDER + GlobalInfo.moveId + File.separator
        }

        /**
         * 总体的log
         */
        @JvmStatic fun writeAllLog(content: String) {
            val writeLog = content + "\n"
            BusHandler.instance.singleThreadExecutor.execute {
                FileUtils.writeToFile(getExternalFolder(), "allLog.txt", writeLog, true)
            }
        }

        @JvmStatic fun writeResultLog(content: String) {
            val resultContent = ExecUtils.getCurrentTimeString() + " : " + content + "\n"
            BusHandler.instance.singleThreadExecutor.execute {
                FileUtils.writeToFile(getDateFolder(), "resultLog.txt", resultContent, true)
            }
        }

        @JvmStatic fun writeLog(content: String) {
            Log.w("jdFetch", content);
            log.append(ExecUtils.getCurrentTimeString() + " : " + content + "\n")
        }

        @JvmStatic fun writeDataLog(row: RowData) {
            rowDatas.add(row)
        }

        @JvmStatic fun writeOutputTxt(filename: String, content: String) {
            val resultContent = ExecUtils.getCurrentTimeString() + " : " + content + "\n"
            BusHandler.instance.singleThreadExecutor.execute {
                FileUtils.writeToFile(LogUtil.getMobileFolder(), filename, resultContent, true)
            }
        }

        @JvmStatic fun writeMoveTime(action: Action) {
            BusHandler.instance.singleThreadExecutor.execute {
                val wifi = SharedPreferenceHelper.getInstance().getValue(RowData.WIFI_LOCATION)
                var mobile = ""
                var deviceCreateTime = ""
                if (EnvManager.sCurrentEnv != null) {
                    mobile = EnvManager.sCurrentEnv.envName!!
                    deviceCreateTime = EnvManager.sCurrentEnv.createTime!!
                } else {
                    mobile = "0"
                }

                val gpsLocation = GlobalInfo.sSelectLocation.name!!
                val ipLocation = if (!TextUtils.isEmpty(wifi)) wifi else GlobalInfo.sSelectLocation.name

                val deviceId = "${GlobalInfo.getLocationId(gpsLocation)}${GlobalInfo.getIPLocationId(ipLocation!!)}${String.format("%02d", GlobalInfo.moveId!!.toInt())}${String.format("%02d", mobile!!.toInt())}"

                var moveColumn = ""
                when (GlobalInfo.moveId) {
                    "1" -> moveColumn = "点击搜索,搜索洗发水"
                    "2" -> moveColumn = "点击搜索,搜索洗发水,点击海飞丝"
                    "3" -> moveColumn = "点击搜索,搜索洗发水,点击海飞丝,加购"
                    "4" -> moveColumn = "点击搜索,搜索海飞丝,点击海飞丝"
                    "5" -> moveColumn = "点击DMP广告页什么都不做"
                    "6" -> moveColumn = "点击DMP广告页,点击广告也某一商品"
                    "7" -> moveColumn = "点击DMP广告页,点击广告也某一商品,加购"
                    "8" -> moveColumn = "点击京东秒杀,点击秒杀某一产品"
                    "9" -> moveColumn = "点击京东秒杀,点击秒杀某一产品,加购"
                }

                val content = "${deviceId},${deviceCreateTime},${action.createTime},${gpsLocation},${ipLocation},${moveColumn}"

                FileUtils.writeToFile(EXCEL_FILE_FOLDER, "动作时间.csv", content + "\n", true, "gb2312")
            }
        }

        @JvmStatic fun taskEnd() {
            if (!GlobalInfo.sIsTest) {
                var content = ""
                val date = ExecUtils.getCurrentTimeString(SimpleDateFormat("MM-dd"))
                if (GlobalInfo.sOneKeyRun) {
                    GlobalInfo.sOneKeyRun = false
                    content = "------ sOneKeyRun : taskEnd"
                    writeAllLog(ExecUtils.getCurrentTimeString() + " : " + content + "\n")
                    LogUtil.writeResultLog(content)
                    StorageUtil.outputDatabaseDatas(date)
                } else {
                    if (GlobalInfo.sAutoFetch) {
                        BusHandler.instance.oneKeyRun()
                    } else {
                        content = "------ singleActionType : " + GlobalInfo.singleActionType + " taskEnd"
                        writeAllLog(ExecUtils.getCurrentTimeString() + " : " + content + "\n")
                        LogUtil.writeResultLog(content)
                        // 单任务序列跑完。
                        StorageUtil.outputDatabaseDatas(date)
                    }
                }
            } else {
                BusHandler.instance.removeMessages(MessageDef.MSG_TIME_OUT)
            }
        }

        @JvmStatic fun flushLog() {
            flushLog(true)
        }

        @JvmStatic fun flushLog(writeDatabase: Boolean) {
            val flushLog = log.toString() + "\n"
            writeAllLog(flushLog)
            log = StringBuilder("")

            if (writeDatabase) {
                BusHandler.instance.singleThreadExecutor.execute(Runnable {
                    MainApplication.getContext().database.use {
                        transaction {
                            for (row in rowDatas) {
                                insert(GlobalInfo.TABLE_NAME,
                                        *row.map.toVarargArray())
                            }
                            rowDatas.clear()
                        }
                    }
                })
            } else {
                rowDatas.clear()
            }

            BusHandler.instance.singleThreadExecutor.execute(Runnable {
                FileUtils.writeToFile(getMobileFolder(), "log.txt", flushLog, true)
            })
        }

        @JvmStatic fun getDateFolder(): String {
            val time = System.currentTimeMillis()//long now = android.os.SystemClock.uptimeMillis();
            val format = SimpleDateFormat("yyyy_MM_dd")
            val d1 = Date(time)
            val t1 = format.format(d1)
            var folder = getExternalFolder() + File.separator + t1
            return folder
        }

        @JvmStatic fun getMobileFolder(): String {
            val time = System.currentTimeMillis()//long now = android.os.SystemClock.uptimeMillis();
            val format = SimpleDateFormat("yyyy_MM_dd")
            val d1 = Date(time)
            val t1 = format.format(d1)
            var folder = getExternalFolder() + t1 + File.separator + "source"

            if (!TextUtils.isEmpty(GlobalInfo.sTargetEnvName)) {
                folder = getExternalFolder() + t1 + File.separator + GlobalInfo.sTargetEnvName
            } else {
                if (EnvManager.sCurrentEnv != null) {
                    folder = getExternalFolder() + t1 + File.separator + EnvManager.sCurrentEnv.envName
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