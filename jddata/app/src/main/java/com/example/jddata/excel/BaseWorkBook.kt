package com.example.jddata.excel

import com.example.jddata.GlobalInfo
import com.example.jddata.util.ExcelUtil
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.LogUtil
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import java.io.File
import java.text.SimpleDateFormat

open class BaseWorkBook(fileName: String, sheetName : String, append: Boolean) {
    var mExcelWorkbook : Workbook? = null
    var mFilePath : String? = null
    protected var mCurrentSheet : Sheet? = null
    val sheetWidth = 25
    var mTxtFileName: String? = null

    constructor(sheetName: String) : this(sheetName, sheetName,true)

    init {
        var isAppend = append
        mTxtFileName = ExecUtils.getCurrentTimeString(SimpleDateFormat("HH时mm分ss秒")) + "_" + fileName + ".txt"
        mFilePath = ExcelUtil.getEnvExcelFile(ExecUtils.getCurrentTimeString(SimpleDateFormat("HH时mm分ss秒")) + "_" + fileName)

        if (GlobalInfo.outputAsExcel) {
            var file = File(mFilePath)
            if (!file.exists()) {
                isAppend = false
            }
            if (!isAppend) {
                ExcelUtil.deleteFile(mFilePath)
            }
            mExcelWorkbook = ExcelUtil.getWorkbook(mFilePath)
            createSheet(sheetName)
        }
    }

    fun createSheet(sheetName: String) {
        mCurrentSheet = mExcelWorkbook!!.getSheet(sheetName)
        if (mCurrentSheet == null) {
            //创建execl中的一个表
            mCurrentSheet = mExcelWorkbook!!.createSheet()
            val sheetCount = mExcelWorkbook!!.getNumberOfSheets()
            mExcelWorkbook!!.setSheetName(sheetCount - 1, sheetName)

            for(i in 0..6) {
                mCurrentSheet!!.setColumnWidth(i, sheetWidth * 256)
            }
        }

        ExcelUtil.writeFile(mExcelWorkbook, mFilePath)
    }

    fun writeToSheetAppendWithTime(vararg datas: String?) {
        var sb = StringBuilder()
        val time = ExecUtils.getCurrentTimeString()
        if (GlobalInfo.outputAsExcel) {
            val row = mCurrentSheet?.createRow(mCurrentSheet!!.lastRowNum + 1)

            row?.createCell(0)?.setCellValue(GlobalInfo.sTargetEnvName + "号账号")
            row?.createCell(1)?.setCellValue(GlobalInfo.sSelectLocation?.name)

            row?.createCell(2)?.setCellValue(time)
            sb.append("$time  |  ")

            for (i in datas.indices) {
                val data = datas[i]
                row?.createCell(i+3)?.setCellValue(data)
                sb.append("$data  |  ")
            }

            LogUtil.writeLog("excel : " + sb.toString())
            ExcelUtil.writeFile(mExcelWorkbook, mFilePath)
        } else {
            sb.append("$time  |  ")
            for (i in datas.indices) {
                val data = datas[i]
                sb.append("$data  |  ")
            }
            LogUtil.writeLog("txt : " + sb.toString())
            LogUtil.writeOutputTxt(mTxtFileName!!, sb.toString())
        }
    }

    fun writeToSheetAppend(vararg datas: String?) {
        var sb = StringBuilder()
        if (GlobalInfo.outputAsExcel) {
            val row = mCurrentSheet?.createRow(mCurrentSheet!!.lastRowNum + 1)

            row?.createCell(0)?.setCellValue(GlobalInfo.sTargetEnvName + "号账号")
            row?.createCell(1)?.setCellValue(GlobalInfo.sSelectLocation?.name)

            for (i in datas.indices) {
                val data = datas[i]
                row?.createCell(i + 2)?.setCellValue(data)
                sb.append("$data  |  ")
            }

            LogUtil.writeLog("excel : " + sb.toString())
            ExcelUtil.writeFile(mExcelWorkbook, mFilePath)
        } else {
            for (i in datas.indices) {
                val data = datas[i]
                sb.append("$data  |  ")
            }
            LogUtil.writeLog("txt : " + sb.toString())
            LogUtil.writeOutputTxt(mTxtFileName!!, sb.toString())
        }
    }
}

/**
 * 购物车
 */
class RecommendWorkBook(sheetName: String) : BaseWorkBook(sheetName)
/**
 * Dmp广告
 */
class DmpWorkBook : BaseWorkBook("dmp广告")
/**
 * 排行榜
 */
class LeaderboardWorkBook : BaseWorkBook("排行榜")
/**
 * 秒杀
 */
class MiaoshaWorkBook(sheetName: String) : BaseWorkBook(sheetName)
/**
 * 会买专辑
 */
class NiceBuyWorkBook : BaseWorkBook("会买专辑")
/**
 * 品类秒杀
 */
class TypeWorkBook : BaseWorkBook("品类秒杀")
/**
 * 发现好货
 */
class WorthBuyWorkBook : BaseWorkBook("发现好货")