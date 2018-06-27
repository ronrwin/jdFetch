package com.example.jddata;

import android.os.Environment;

import com.example.jddata.shelldroid.EnvManager;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ExcelUtil {
    public static final String EXCEL_FILE_FOLDER = Environment.getExternalStorageDirectory() + "/Pictures/";

    public static String getEnvExcelFile(String sheetName) {
        if (EnvManager.sCurrentEnv != null) {
            String folder = EXCEL_FILE_FOLDER + EnvManager.sCurrentEnv.appName;
            File folderFile = new File(folder);
            if (!folderFile.exists()) {
                folderFile.mkdirs();
            }
            return EXCEL_FILE_FOLDER + EnvManager.sCurrentEnv.appName + "/data_" + sheetName + ".xls";
        }

        String folder = EXCEL_FILE_FOLDER + "source";
        File folderFile = new File(folder);
        if (!folderFile.exists()) {
            folderFile.mkdirs();
        }
        return EXCEL_FILE_FOLDER + "source/data_" + sheetName + ".xls";
    }

    public static Workbook initWorkbook(String fileName) {
        Workbook mExcelWorkbook = new HSSFWorkbook();
        FileUtils.writeExcelFile(mExcelWorkbook, fileName);
        return mExcelWorkbook;
    }

    /**
     * 获取指定的excel文件
     */
    public static Workbook getWorkbook(String filename) {
        if (null != filename) {
            File file = new File(filename);
            if (file.exists()) {
                try {
                    String fileType = filename.substring(filename.lastIndexOf("."),
                            filename.length());
                    FileInputStream fileStream = new FileInputStream(new File(filename));
                    if (".xls".equals(fileType.trim().toLowerCase())) {
                        Workbook mExcelWorkbook = new HSSFWorkbook(fileStream);// 创建 Excel 2003 工作簿对象
                        return mExcelWorkbook;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return initWorkbook(filename);
    }

    public static void deleteFile(String filename) {
        if (null != filename) {
            File file = new File(filename);
            if (file.exists()) {
                file.delete();
            }
        }
    }
}
