package com.example.jddata.excel;

import com.example.jddata.util.ExcelUtil;
import com.example.jddata.util.FileUtils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class BaseSheet {
    protected String mSheetName;
    protected Workbook mExcelWorkbook;
    protected String mFileName;
    protected Sheet mSheet;

    public BaseSheet(String sheetName) {
        mSheetName = sheetName;
        mFileName = ExcelUtil.getEnvExcelFile(sheetName);
        ExcelUtil.deleteFile(mFileName);
        mExcelWorkbook = ExcelUtil.getWorkbook(mFileName);
        mSheet = mExcelWorkbook.getSheet(mSheetName);

        if (mSheet != null) {
            int max = mSheet.getLastRowNum();
            for(int i = 0; i < max; i++)
            {
                Row row = mSheet.getRow(i);
                if (row != null) {
                    mSheet.removeRow(row);
                }
            }
        } else {
            //创建execl中的一个表
            mSheet = mExcelWorkbook.createSheet();
            int sheetCount = mExcelWorkbook.getNumberOfSheets();
            mExcelWorkbook.setSheetName(sheetCount - 1, mSheetName);
        }
        initFirstRow();
        FileUtils.writeExcelFile(mExcelWorkbook, mFileName);
    }

    protected void initFirstRow() {

    }

    public void writeToSheet(int rowIndex, String... datas) {
        Sheet sheet = mExcelWorkbook.getSheet(mSheetName);
        Row row = sheet.createRow(rowIndex);

        for (int i = 0; i < datas.length; i++) {
            String data = datas[i];
            Cell cell = row.createCell(i);
            cell.setCellValue(data);
        }

        FileUtils.writeExcelFile(mExcelWorkbook, mFileName);
    }

    public void writeToSheetAppend(String... datas) {
        Sheet sheet = mExcelWorkbook.getSheet(mSheetName);
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);

        for (int i = 0; i < datas.length; i++) {
            String data = datas[i];
            Cell cell = row.createCell(i);
            cell.setCellValue(data);
        }

        FileUtils.writeExcelFile(mExcelWorkbook, mFileName);
    }
}
