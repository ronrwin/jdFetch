package com.example.jddata.excel;


/**
 * 会买专辑
 */
public class NiceBuySheet extends BaseSheet {
    public NiceBuySheet() {
        super("会买专辑");
    }

    public NiceBuySheet(String sheetName) {
        super(sheetName);
    }

    public void addTitleRow() {
        writeToSheetAppend("标题", "数量", "看过数", "收藏数");
    }
}
