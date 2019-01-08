package com.example.jddata.util

import com.example.jddata.BusHandler
import java.text.SimpleDateFormat

open class BaseLogFile(fileName: String) {
    var mTxtFileName: String? = null

    init {
        mTxtFileName = ExecUtils.getCurrentTimeString(SimpleDateFormat("HH时mm分ss秒")) + "_" + fileName + ".txt"
    }

    fun writeToFileAppend(vararg datas: String?) {
        var sb = StringBuilder()
        for (i in datas.indices) {
            val data = datas[i]
            sb.append("$data  |  ")
        }
        LogUtil.logCache("txt : " + sb.toString())

        // 记录本次动作的日志
        BusHandler.instance.singleThreadExecutor.execute {
            FileUtils.writeToFile(LogUtil.getDeviceFolder(), mTxtFileName!!, sb.toString()+"\n", true)
        }

    }


}
