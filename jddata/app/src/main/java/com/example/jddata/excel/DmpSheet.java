package com.example.jddata.excel;

/**
 * Dmp广告
 */
public class DmpSheet extends BaseSheet {
    public DmpSheet() {
        super("dmp广告");
    }

    public DmpSheet(String sheetName) {
        super(sheetName);
    }

    @Override
    protected void initFirstRow() {
        writeToSheet(0, "标题");
    }
}
