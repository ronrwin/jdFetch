package com.example.jddata;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class BuyGoodsSheet {
    public static Workbook initWorkbook(String fileName, String sheetName) {
        Workbook mExcelWorkbook = new HSSFWorkbook();
        //标题栏单元格特征
//        CellStyle titleStyle = createTitleCellStyle();

        //创建execl中的一个表
        Sheet sheet = mExcelWorkbook.createSheet();
        mExcelWorkbook.setSheetName(0, sheetName);
        //创建标题栏1
        Row titleRow1 = sheet.createRow(0);
        // 设置标题栏高度
//        titleRow1.setHeightInPoints(60);
//        titleRow1.setRowStyle(titleStyle);

        //创建标题栏第1个标题
        Cell cell0 = titleRow1.createCell(0);
        cell0.setCellValue("标题");

        //创建标题栏第2个标题
        Cell cell1 = titleRow1.createCell(1);
        cell1.setCellValue("价格");

        FileUtils.writeExcelFile(mExcelWorkbook, fileName);
        return mExcelWorkbook;
    }

    public static void writeToSheet(Workbook workbook, String fileName, String sheetname, String title, String price) {
        Sheet sheet = workbook.getSheet(sheetname);
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);

        Cell cell0 = row.createCell(0);
        cell0.setCellValue(title);

        //创建标题栏第2个标题
        Cell cell1 = row.createCell(1);
        cell1.setCellValue(price);


        FileUtils.writeExcelFile(workbook, fileName);
    }
}
