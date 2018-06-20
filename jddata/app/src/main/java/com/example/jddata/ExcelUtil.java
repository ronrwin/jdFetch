package com.example.jddata;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ExcelUtil {
    /**
     * 获取指定的excel文件
     */
    public static Workbook getWorkbook(String filename) throws IOException {
        if (null != filename) {
            String fileType = filename.substring(filename.lastIndexOf("."),
                    filename.length());
            FileInputStream fileStream = new FileInputStream(new File(filename));
            if (".xls".equals(fileType.trim().toLowerCase())) {
                Workbook mExcelWorkbook = new HSSFWorkbook(fileStream);// 创建 Excel 2003 工作簿对象
                return mExcelWorkbook;
            }
        }
        return null;
    }
}
