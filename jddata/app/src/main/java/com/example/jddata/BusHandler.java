package com.example.jddata;

import android.os.Looper;
import android.os.Message;

import com.example.jddata.Entity.BrandEntity;
import com.example.jddata.Entity.NiceBuyEntity;
import com.example.jddata.Entity.TypeEntity;
import com.example.jddata.excel.BrandSheet;
import com.example.jddata.excel.NiceBuySheet;
import com.example.jddata.excel.TypeSheet;

import java.util.ArrayList;

public class BusHandler extends android.os.Handler {


    private BusHandler() {
        super(Looper.getMainLooper());
    }

    private static class Holder {
        private static BusHandler mInstance = new BusHandler();

        private Holder() {
        }
    }

    public static BusHandler getInstance() {
        return Holder.mInstance;
    }

    public ArrayList<NiceBuyEntity> mNiceBuyTitles = new ArrayList<>();
    public NiceBuySheet mNiceBuySheet;

    public ArrayList<BrandEntity> mBrandEntitys = new ArrayList<>();
    public BrandSheet mBrandSheet;

    public ArrayList<TypeEntity> mTypePrices = new ArrayList<>();
    public TypeSheet mTypeSheet;

    @Override
    public void handleMessage(Message msg) {
        int what = msg.what;
        switch (what) {
            case MessageDef.MSG_TIME_OUT:

                break;
        }
    }
}
