package com.example.jddata.excel;

/**
 * 发现好货
 */
public class WorthBuySheet extends BaseSheet {
    public WorthBuySheet(String sheetName) {
        super(sheetName);
    }

    public void initFirstRow() {
        writeToSheet(0, "标题", "描述", "收藏数");
    }
}
