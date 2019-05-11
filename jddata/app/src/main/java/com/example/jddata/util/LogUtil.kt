package com.example.jddata.util

import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.jddata.BusHandler
import com.example.jddata.Entity.RowData
import com.example.jddata.GlobalInfo
import com.example.jddata.MainApplication
import com.example.jddata.action.Action
import com.example.jddata.shelldroid.Env
import com.example.jddata.shelldroid.EnvManager
import com.example.jddata.storage.database
import com.example.jddata.storage.toVarargArray
import org.jetbrains.anko.db.*
import java.io.*


class LogUtil {
    companion object {
        @JvmField val EXTERNAL_FILE_FOLDER = Environment.getExternalStorageDirectory().toString() + "/Pictures/jdFetch"
        @JvmField var log = StringBuilder("")
        @JvmField var rowDatas = ArrayList<RowData>()
        @JvmField val TAG = "jdFetch"

        // 二级目录：日期
        @JvmStatic fun getDateFolder(): String {
            val folder = "${EXTERNAL_FILE_FOLDER}/${ExecUtils.today()}"
            val folderFile = File(folder)
            if (!folderFile.exists()) {
                folderFile.mkdirs()
            }
            return folder
        }

        // 三级目录：设备
        @JvmStatic fun getDeviceFolder(env: Env): String {
            val folder = "${getDateFolder()}/${env.id}"

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
                FileUtils.writeToFile("${EXTERNAL_FILE_FOLDER}", filename, resultContent, true)
            }
        }

        /**
         * 记录执行结果
         */
        @JvmStatic fun writeFailLog(content: String) {
            val resultContent = ExecUtils.getCurrentTimeString() + " : " + content + "\n"
            BusHandler.instance.singleThreadExecutor.execute {
                var filename = "failLog.txt"
                FileUtils.writeToFile("${EXTERNAL_FILE_FOLDER}", filename, resultContent, true)
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

                    FileUtils.writeToFile(EXTERNAL_FILE_FOLDER, "动作序列表.csv", content + "\n", true, "gb2312")
                }
            }
        }

        @JvmStatic fun flushLog(env: Env, writeDatabase: Boolean) {
            Companion.flushLog(env, writeDatabase, false)
        }

        // 把数据库行数据缓存，写到数据库中
        // 把本次动作日志，写到设备的日志记录中
        @JvmStatic fun flushLog(env: Env, writeDatabase: Boolean, isFail: Boolean) {
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
                // fixme: 测试过程中产生大量文件，是否需要记录？
//                FileUtils.writeToFile(getDeviceFolder(env), "log.txt", flushLog, true)
            }

            if (isFail) {
                writeFailLog(flushLog)
            }
        }

        /**
         * 保存对象
         *
         * @param ser 要保存的序列化对象
         * @param file 保存在本地的文件名
         * @throws IOException
         */
        @JvmStatic fun saveObject(context: Context, ser: Serializable,
                       file: String): Boolean {
            var fos: FileOutputStream? = null
            var oos: ObjectOutputStream? = null
            try {
                fos = FileOutputStream(file)
                oos = ObjectOutputStream(fos)
                oos.writeObject(ser)
                oos.flush()
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            } finally {
                try {
                    oos?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                try {
                    fos?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        /**
         * 读取对象
         *
         * @param file 保存在本地的文件名
         * @return
         * @throws IOException
         */
        @JvmStatic fun readObject(context: Context, file: String): Serializable? {
            var fis: FileInputStream? = null
            var ois: ObjectInputStream? = null
            try {
                fis = FileInputStream(file)
                ois = ObjectInputStream(fis)
                return (ois.readObject()) as Serializable
            } catch (e: FileNotFoundException) {
            } catch (e: Exception) {
                e.printStackTrace()
                // 反序列化失败 - 删除缓存文件
                if (e is InvalidClassException) {
                    val data = context.getFileStreamPath(file)
                    data.delete()
                }
            } finally {
                try {
                    ois!!.close()
                } catch (e: Exception) {
                }

                try {
                    fis!!.close()
                } catch (e: Exception) {
                }

            }
            return null
        }

        @JvmStatic fun saveActions() {
            if (MainApplication.sActionQueue.size > 0) {
                MainApplication.sExecutor.execute {
                    if (EnvManager.envs.size > 0) {
                        var biggest = 0
                        for (env in EnvManager.envs) {
                            val id = env.id
                            if (id!!.contains("_")) {
                                val ids = id.split("_")
                                val num = ids[0].toInt()
                                if (num > biggest) {
                                    biggest = num
                                }
                            }
                        }
                        saveObject(MainApplication.sContext, MainApplication.sActionQueue,
                                LogUtil.EXTERNAL_FILE_FOLDER + "/notEndActions_${biggest}.txt")
                    }
                }
            }
        }
    }
}