package com.example.jddata.util

import com.example.jddata.BusHandler
import com.example.jddata.shelldroid.Env
import java.text.SimpleDateFormat

open class BaseLogFile(fileName: String) {
    var mTxtFileName: String? = null
    var mEnv: Env? = null

    init {
        mTxtFileName = ExecUtils.getCurrentTimeString(SimpleDateFormat("HH时mm分ss秒")) + "_" + fileName + ".txt"
    }

    fun setEnv(env: Env) {
        mEnv = env
    }

    fun writeToFileAppend(vararg datas: String?) {
        var sb = StringBuilder()
        for (i in datas.indices) {
            val data = datas[i]
            sb.append("$data  |  ")
        }
        LogUtil.logCache("debug", "txt : " + sb.toString())

        // 记录本次动作的日志
        BusHandler.instance.singleThreadExecutor.execute {
            if (mEnv != null && mTxtFileName != null) {
                // fixme: 测试过程中产生大量文件，是否需要记录？
//                FileUtils.writeToFile(LogUtil.getDeviceFolder(mEnv!!), mTxtFileName!!, sb.toString() + "\n", true)
            }
        }

    }


}
