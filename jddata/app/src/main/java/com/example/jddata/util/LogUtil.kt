package com.example.jddata.util

import android.content.Context
import android.os.Environment
import android.text.TextUtils
import android.text.format.DateUtils
import android.util.Log
import android.widget.Toast
import com.example.jddata.BusHandler
import com.example.jddata.Entity.Route
import com.example.jddata.Entity.RowData
import com.example.jddata.Entity.SaveEntity
import com.example.jddata.GlobalInfo
import com.example.jddata.MainApplication
import com.example.jddata.action.Action
import com.example.jddata.action.Factory
import com.example.jddata.shelldroid.Env
import com.example.jddata.shelldroid.EnvManager
import com.example.jddata.storage.database
import com.example.jddata.storage.toVarargArray
import org.jetbrains.anko.db.*
import java.io.*
import java.util.AbstractCollection
import java.util.concurrent.ConcurrentLinkedDeque


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
                var filename = "resultLog_${getEnvRange()}.txt"
                FileUtils.writeToFile(getDateFolder(), filename, resultContent, true)
            }
        }

        /**
         * 记录执行结果
         */
        @JvmStatic fun writeFailLog(content: String) {
            val resultContent = ExecUtils.getCurrentTimeString() + " : " + content + "\n"
            BusHandler.instance.singleThreadExecutor.execute {
                var filename = "failLog_${getEnvRange()}.txt"
                FileUtils.writeToFile(getDateFolder(), filename, resultContent, true)
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
            if (row.map.containsKey(RowData.SKU) && !TextUtils.isEmpty(row.sku)) {
                row.skuUrl = "https://item.jd.com/${row.sku}.html"
            }
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
                    val deviceId = "${action.env!!.id}"

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
                val dest = File(file)
                dest.getParentFile().mkdirs()

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

        @JvmStatic fun getEnvRange(): String {
            var out = ""
            var biggest = 0
            var minist = 900
            for (env in EnvManager.envs) {
                val id = env.id
                if (id!!.contains("_")) {
                    val ids = id.split("_")
                    val num = ids[0].toInt()
                    if (num > biggest) {
                        biggest = num
                    }
                    if (num < minist) {
                        minist = num
                    }
                }
            }
            out = "${minist}_${biggest}"
            return out
        }

        @JvmStatic fun getSerilize(): ArrayList<SaveEntity>? {
            if (EnvManager.envs.size > 0) {
                var lasrEnv = EnvManager.envs[0]
                try {
                    val filename = "/${ExecUtils.today()}_notEndActions_${LogUtil.getEnvRange()}.txt"
                    Log.d("zfr", "read from : $filename")

                    val o = LogUtil.readObject(MainApplication.sContext, EXTERNAL_FILE_FOLDER + filename)
                    if (o != null) {
                        val entitys = o as ArrayList<SaveEntity>
                        return entitys
                    } else {
                        MainApplication.sMainHandler.post {
                            Toast.makeText(MainApplication.sContext, "NO not Run Action", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    MainApplication.sMainHandler.post {
                        Toast.makeText(MainApplication.sContext, "exception", Toast.LENGTH_LONG).show()
                    }
                }
            }
            return null
        }

        @JvmStatic fun restoreActions() {
            MainApplication.sExecutor.execute {
                if (EnvManager.envs.size > 0) {
                    var lasrEnv = EnvManager.envs[0]
                    val entitys = getSerilize()
                    if (entitys != null) {
                        MainApplication.sMainHandler.post {
                            // 有收集任务的时候才开启京东秒杀获取线程。
                            var hasFetch = false
                            MainApplication.sActionQueue.clear()
                            for (en in entitys) {
                                if (en.actionType.startsWith("fetch")) {
                                    hasFetch = true
                                }
                                if (!lasrEnv.id.equals(en.id)) {
                                    lasrEnv = EnvManager.findEnvById(en.id)
                                }
                                if (lasrEnv != null) {
                                    if (en.route != null) {
                                        val action = Factory.createTemplateAction(lasrEnv, en.route!!)
                                        LogUtil.logCache(">>>>  env: ${lasrEnv.envName}, createAction : ${action!!.mActionType}")
                                        MainApplication.sActionQueue.add(action)
                                    } else {
                                        val action = Factory.createAction(lasrEnv, en.actionType)
                                        LogUtil.logCache(">>>>  env: ${lasrEnv.envName}, createAction : ${action!!.mActionType}")
                                        MainApplication.sActionQueue.add(action)
                                    }
                                }
                            }
                            BusHandler.instance.startPollAction()

                            if (hasFetch) {
                                MainApplication.startJDKillThread()
                            }
                        }
                    }
                }
            }
        }

        @JvmStatic fun saveActions(collection: AbstractCollection<Action>) {
            val entitys = ArrayList<SaveEntity>()
            for (action in collection) {
                val route = action.getState(GlobalInfo.ROUTE) as Route?
                entitys.add(SaveEntity(action.env!!.id!!, action.mActionType!!, route, GlobalInfo.sIsOrigin))
            }

            MainApplication.sExecutor.execute {
                if (EnvManager.envs.size > 0) {
                    val filename = "/${ExecUtils.today()}_notEndActions_${LogUtil.getEnvRange()}.bak.txt"

                    val result = saveObject(MainApplication.sContext, entitys,
                            EXTERNAL_FILE_FOLDER + filename)
                    if (result) {
                        val file = File(EXTERNAL_FILE_FOLDER + filename)
                        val newName = "/${ExecUtils.today()}_notEndActions_${LogUtil.getEnvRange()}.txt"
                        val doneName = "/done_${LogUtil.getEnvRange()}.txt"
                        if (file.exists()) {
                            if (entitys.size > 0) {
                                val vv = file.renameTo(File(EXTERNAL_FILE_FOLDER + newName))
                            } else {
                                val res = file.renameTo(File(getDateFolder() + doneName))
                                if (res) {
                                    val notEnd = File(EXTERNAL_FILE_FOLDER + newName)
                                    if (notEnd.exists()) {
                                        notEnd.delete()
                                    }
                                }
                            }
                        }
                    } else {
                        val file = File(EXTERNAL_FILE_FOLDER + filename)
                        if (file.exists()) {
                            file.delete()
                        }
                    }
                }
            }
        }
    }
}