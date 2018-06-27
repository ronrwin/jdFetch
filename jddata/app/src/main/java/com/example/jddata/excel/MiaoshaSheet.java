package com.example.jddata.excel;


/**
 * 秒杀
 */
public class MiaoshaSheet extends BaseSheet {
    public MiaoshaSheet(String sheetName) {
        super(sheetName);
    }

    public void initFirstRow() {
        writeToSheet(0, "标题", "秒杀价", "京东价");
    }
}
