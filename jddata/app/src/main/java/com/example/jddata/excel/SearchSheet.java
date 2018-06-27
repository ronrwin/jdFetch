package com.example.jddata.excel;


public class SearchSheet extends BaseSheet {

    public SearchSheet(String mSearchStr) {
        super("search " + mSearchStr);
    }

    @Override
    protected void initFirstRow() {
        writeToSheet(0, "标题", "价格", "评价", "好评率");
    }

}
