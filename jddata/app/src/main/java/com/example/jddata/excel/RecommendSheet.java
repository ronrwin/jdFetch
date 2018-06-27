package com.example.jddata.excel;


/**
 * 购物车
 */
public class RecommendSheet extends BaseSheet {
    public RecommendSheet(String sheetName) {
        super(sheetName);
    }

    public void initFirstRow() {
        writeToSheet(0, "标题", "价格");
    }
}
