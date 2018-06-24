package com.example.jddata.excel;

import com.example.jddata.FileUtils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

/**
 * 发现好货
 */
public class WorthBuySheet extends BaseSheet {
    public WorthBuySheet(String sheetName) {
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
        cell1.setCellValue("描述");

        //创建标题栏第2个标题
        Cell cell2 = titleRow1.createCell(2);
        cell2.setCellValue("收藏数");

        FileUtils.writeExcelFile(mExcelWorkbook, mFileName);
    }
}
