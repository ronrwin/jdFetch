package com.example.jddata.util;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.example.jddata.BusHandler;
import com.example.jddata.GlobalInfo;
import com.example.jddata.shelldroid.EnvManager;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExcelUtil {

    public static String getEnvExcelFile(String fileName) {
        String folder = LogUtil.getFolder();
        return folder + File.separator + fileName  + ".xls";
    }

    public static Workbook initWorkbook(String fileName) {
        Workbook mExcelWorkbook = new HSSFWorkbook();
        writeFile(mExcelWorkbook, fileName);
        return mExcelWorkbook;
    }

    public static void writeFile(final Workbook workbook, final String fileName) {
        BusHandler.getInstance().singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                FileUtils.writeExcelFile(workbook, fileName);
            }
        });
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
