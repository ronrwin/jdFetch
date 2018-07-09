package com.example.jddata.excel

import android.text.TextUtils
import com.example.jddata.GlobalInfo
import com.example.jddata.util.ExcelUtil
import com.example.jddata.util.ExecUtils
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import java.io.File

open class BaseSheet(sheetName : String) {
    protected var mSheetName : String? = null
    protected var mExcelWorkbook : Workbook? = null
    protected var mFileName : String? = null
    protected var mSheet : Sheet? = null
    val sheetWidth = 12

    init {
        mSheetName = sheetName
        mFileName = ExcelUtil.getEnvExcelFile(sheetName)
        ExcelUtil.deleteFile(mFileName)

        mExcelWorkbook = ExcelUtil.getWorkbook(mFileName)
        mSheet = mExcelWorkbook!!.getSheet(mSheetName)
        //创建execl中的一个表
        mSheet = mExcelWorkbook!!.createSheet()
        val sheetCount = mExcelWorkbook!!.getNumberOfSheets()
        mExcelWorkbook!!.setSheetName(sheetCount - 1, mSheetName)

        for(i in 0..6) {
            mSheet!!.setColumnWidth(i, sheetWidth * 256)
        }

        if (!TextUtils.isEmpty(GlobalInfo.sTargetEnvName)) {
            writeToSheetAppend(GlobalInfo.sTargetEnvName + "号账号")
        }

        writeToSheetAppend("")
        initFirstRow()
        ExcelUtil.writeFile(mExcelWorkbook, mFileName)
    }

    open protected fun initFirstRow() {}

    fun writeToSheet(rowIndex: Int, vararg datas: String) {
        val sheet = mExcelWorkbook!!.getSheet(mSheetName)
        val row = sheet.createRow(rowIndex)

        for (i in datas.indices) {
            val data = datas[i]
            val cell = row.createCell(i)
            cell.setCellValue(data)
        }

        ExcelUtil.writeFile(mExcelWorkbook, mFileName)
    }

    fun writeToSheetAppendWithTime(vararg datas: String?) {
        val row = mSheet?.createRow(mSheet!!.lastRowNum + 1)

        row?.createCell(0)?.setCellValue(ExecUtils.getCurrentTimeString())

        for (i in datas.indices) {
            val data = datas[i]
            row?.createCell(i+1)?.setCellValue(data)
        }

        ExcelUtil.writeFile(mExcelWorkbook, mFileName)
    }

    fun writeToSheetAppend(vararg datas: String?) {
        val row = mSheet?.createRow(mSheet!!.lastRowNum + 1)

        for (i in datas.indices) {
            val data = datas[i]
            row?.createCell(i)?.setCellValue(data)
        }

        ExcelUtil.writeFile(mExcelWorkbook, mFileName)
    }
}

/**
 * 购物车
 */
class RecommendSheet(sheetName: String) : BaseSheet(sheetName)
/**
 * 品牌秒杀
 */
class BrandSheet : BaseSheet("品牌秒杀") {
    fun addTitleRow() {
        writeToSheetAppend("标题", "副标题")
    }
}
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
class SearchSheet(mSearchStr: String) : BaseSheet("搜索_$mSearchStr")
/**
 * 品类秒杀
 */
class TypeSheet : BaseSheet("品类秒杀")
/**
 * 发现好货
 */
class WorthBuySheet : BaseSheet("发现好货")