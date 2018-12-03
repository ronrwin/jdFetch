package com.example.jddata.excel

import com.example.jddata.util.ExecUtils
import com.example.jddata.util.LogUtil
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import java.text.SimpleDateFormat

open class BaseWorkBook(fileName: String, sheetName : String, append: Boolean) {
    var mTxtFileName: String? = null

    constructor(sheetName: String) : this(sheetName, sheetName,true)

    init {
        mTxtFileName = ExecUtils.getCurrentTimeString(SimpleDateFormat("HH时mm分ss秒")) + "_" + fileName + ".txt"
    }

    fun writeToSheetAppendWithTime(vararg datas: String?) {
        var sb = StringBuilder()
        val time = ExecUtils.getCurrentTimeString()
        sb.append("$time  |  ")
        for (i in datas.indices) {
            val data = datas[i]
            sb.append("$data  |  ")
        }
        LogUtil.writeLog("txt : " + sb.toString())
        LogUtil.writeOutputTxt(mTxtFileName!!, sb.toString())
    }

    fun writeToSheetAppend(vararg datas: String?) {
        val sb = StringBuilder()
        for (i in datas.indices) {
            val data = datas[i]
            sb.append("$data  |  ")
        }
        LogUtil.writeLog("txt : " + sb.toString())
        LogUtil.writeOutputTxt(mTxtFileName!!, sb.toString())
    }
}
