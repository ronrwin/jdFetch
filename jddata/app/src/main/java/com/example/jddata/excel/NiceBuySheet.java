package com.example.jddata.excel;

import com.example.jddata.FileUtils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

/**
 * 会买专辑
 */
public class NiceBuySheet extends BaseSheet {
    public NiceBuySheet(String sheetName) {
        super(sheetName);
    }

    public void addTitleRow() {
        writeToSheetAppend("标题", "数量", "看过数", "收藏数");
    }
}
