package com.example.jddata.util

import java.text.SimpleDateFormat

open class BaseLogFile(fileName: String, sheetName : String, append: Boolean) {
    var mTxtFileName: String? = null

    constructor(sheetName: String) : this(sheetName, sheetName,true)

    init {
        mTxtFileName = ExecUtils.getCurrentTimeString(SimpleDateFormat("HH时mm分ss秒")) + "_" + fileName + ".txt"
    }

    fun writeToFileAppendWithTime(vararg datas: String?) {
        var sb = StringBuilder()
        val time = ExecUtils.getCurrentTimeString()
        sb.append("$time  |  ")
        for (i in datas.indices) {
            val data = datas[i]
            sb.append("$data  |  ")
        }
        LogUtil.logCache("txt : " + sb.toString())
        LogUtil.writeOutputTxt(mTxtFileName!!, sb.toString())
    }
}