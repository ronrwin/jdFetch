package com.example.jddata.excel

import android.text.TextUtils
import com.example.jddata.GlobalInfo
import com.example.jddata.util.ExcelUtil
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.LogUtil
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import java.io.File
import java.text.SimpleDateFormat

open class BaseSheet(fileName: String, var sheetName : String, append: Boolean) {
    protected var mExcelWorkbook : Workbook? = null
    protected var mFilePath : String? = null
    protected var mSheet : Sheet? = null
    val sheetWidth = 25

    constructor(sheetName: String) : this(sheetName, sheetName,false)

    init {
        var isAppend = append
        mFilePath = ExcelUtil.getEnvExcelFile(fileName + "_" + ExecUtils.getCurrentTimeString(SimpleDateFormat("HH_mm")))
        var file = File(mFilePath)
        if (!file.exists()) {
            isAppend = false
        }
        if (!isAppend) {
            ExcelUtil.deleteFile(mFilePath)
        }

        mExcelWorkbook = ExcelUtil.getWorkbook(mFilePath)
        mSheet = mExcelWorkbook!!.getSheet(sheetName)
        if (mSheet == null) {
            //创建execl中的一个表
            mSheet = mExcelWorkbook!!.createSheet()
            val sheetCount = mExcelWorkbook!!.getNumberOfSheets()
            mExcelWorkbook!!.setSheetName(sheetCount - 1, sheetName)

            for(i in 0..6) {
                mSheet!!.setColumnWidth(i, sheetWidth * 256)
            }
        }

        writeToSheetAppend("")
        if (!TextUtils.isEmpty(GlobalInfo.sTargetEnvName)) {
            writeToSheetAppend(GlobalInfo.sTargetEnvName + "号账号")
        }
        ExcelUtil.writeFile(mExcelWorkbook, mFilePath)
    }

    fun writeToSheet(rowIndex: Int, vararg datas: String) {
        val sheet = mExcelWorkbook!!.getSheet(sheetName)
        val row = sheet.createRow(rowIndex)

        for (i in datas.indices) {
            val data = datas[i]
            val cell = row.createCell(i)
            cell.setCellValue(data)
        }

        ExcelUtil.writeFile(mExcelWorkbook, mFilePath)
    }

    fun writeToSheetAppendWithTime(vararg datas: String?) {
        var sb = StringBuilder()
        val row = mSheet?.createRow(mSheet!!.lastRowNum + 1)

        val time = ExecUtils.getCurrentTimeString()
        row?.createCell(0)?.setCellValue(time)
        sb.append("$time  |  ")

        for (i in datas.indices) {
            val data = datas[i]
            row?.createCell(i+1)?.setCellValue(data)
            sb.append("$data  |  ")
        }

        LogUtil.writeLog(sb.toString())
        ExcelUtil.writeFile(mExcelWorkbook, mFilePath)
    }

    fun writeToSheetAppend(vararg datas: String?) {
        var sb = StringBuilder()
        val row = mSheet?.createRow(mSheet!!.lastRowNum + 1)

        for (i in datas.indices) {
            val data = datas[i]
            row?.createCell(i)?.setCellValue(data)
            sb.append("$data  |  ")
        }

        LogUtil.writeLog(sb.toString())
        ExcelUtil.writeFile(mExcelWorkbook, mFilePath)
    }
}

/**
 * 购物车
 */
class RecommendSheet(sheetName: String) : BaseSheet(sheetName)
/**
 * 品牌秒杀
 */
class BrandSheet : BaseSheet("品牌秒杀")
/**
 * Dmp广告
 */
class DmpSheet : BaseSheet("dmp广告")
/**
 * 排行榜
 */
class LeaderboardSheet : BaseSheet("排行榜")
/**
 * 秒杀
 */
class MiaoshaSheet(sheetName: String) : BaseSheet(sheetName)
/**
 * 会买专辑
 */
class NiceBuySheet : BaseSheet("会买专辑")
/**
 * 搜索
 */
class SearchSheet(mSearchStr: String) : BaseSheet("搜索_$mSearchStr")
/**
 * 品类秒杀
 */
class TypeSheet : BaseSheet("品类秒杀")
/**
 * 发现好货
 */
class WorthBuySheet : BaseSheet("发现好货")