package com.example.jddata.excel;

/**
 * 品牌秒杀
 */
public class BrandSheet extends BaseSheet {
    public BrandSheet(String sheetName) {
        super(sheetName);
    }

    public void addTitleRow() {
        writeToSheetAppend("标题", "副标题");
    }
}
