package com.example.jddata.excel

import com.example.jddata.util.ExcelUtil
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook

open class BaseSheet(sheetName : String) {
    protected var mSheetName : String? = null
    protected var mExcelWorkbook : Workbook? = null
    protected var mFileName : String? = null
    protected var mSheet : Sheet? = null

    init {
        mSheetName = sheetName
        mFileName = ExcelUtil.getEnvExcelFile(sheetName)
        ExcelUtil.deleteFile(mFileName)
        mExcelWorkbook = ExcelUtil.getWorkbook(mFileName)
        mSheet = mExcelWorkbook!!.getSheet(mSheetName)
        if (mSheet != null) {
            val max = mSheet!!.getLastRowNum()
            for (i in 0..max) {
                val row = mSheet!!.getRow(i)
                if (row != null) {
                    mSheet!!.removeRow(row)
                }
            }
        } else {
            //创建execl中的一个表
            mSheet = mExcelWorkbook!!.createSheet()
            val sheetCount = mExcelWorkbook!!.getNumberOfSheets()
            mExcelWorkbook!!.setSheetName(sheetCount - 1, mSheetName)
        }
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

    fun writeToSheetAppend(vararg datas: String) {
        val sheet = mExcelWorkbook!!.getSheet(mSheetName)
        val row = sheet.createRow(sheet.lastRowNum + 1)

        for (i in datas.indices) {
            val data = datas[i]
            val cell = row.createCell(i)
            cell.setCellValue(data)
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
class BrandSheet : BaseSheet {
    constructor() : super("品牌秒杀") {}

    fun addTitleRow() {
        writeToSheetAppend("标题", "副标题")
    }
}
/**
 * Dmp广告
 */
class DmpSheet : BaseSheet {
    constructor() : super("dmp广告") {}
    override fun initFirstRow() {
        writeToSheet(0, "标题")
    }
}
/**
 * 排行榜
 */
class LeaderboardSheet : BaseSheet {
    constructor() : super("排行榜") {}
}
/**
 * 秒杀
 */
class MiaoshaSheet(sheetName: String) : BaseSheet(sheetName) {

    public override fun initFirstRow() {
        writeToSheet(0, "标题", "秒杀价", "京东价")
    }
}
/**
 * 会买专辑
 */
class NiceBuySheet : BaseSheet {
    constructor() : super("会买专辑") {}

    fun addTitleRow() {
        writeToSheetAppend("标题", "数量", "看过数", "收藏数")
    }
}
class SearchSheet(mSearchStr: String) : BaseSheet("搜索_$mSearchStr") {

    override fun initFirstRow() {
        writeToSheet(0, "标题", "价格", "评价", "好评率")
    }

}
/**
 * 品类秒杀
 */
class TypeSheet : BaseSheet {
    constructor() : super("品类秒杀") {}
}
/**
 * 发现好货
 */
class WorthBuySheet : BaseSheet {
    constructor() : super("发现好货") {}
    public override fun initFirstRow() {
        writeToSheet(0, "标题", "描述", "收藏数")
    }
}