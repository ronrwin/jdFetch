package com.example.jddata.excel;

import android.text.TextUtils;

import com.example.jddata.ExcelUtil;
import com.example.jddata.FileUtils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * 购物车
 */
public class RecommendSheet extends BaseSheet {
    public RecommendSheet(String sheetName) {
        super(sheetName);
    }

    public void initFirstRow() {
        //创建标题栏1
        Row titleRow1 = mSheet.createRow(0);

        //创建标题栏第1个标题
        Cell cell0 = titleRow1.createCell(0);
        cell0.setCellValue("标题");

        //创建标题栏第2个标题
        Cell cell1 = titleRow1.createCell(1);
        cell1.setCellValue("价格");

        FileUtils.writeExcelFile(mExcelWorkbook, mFileName);
    }
}
