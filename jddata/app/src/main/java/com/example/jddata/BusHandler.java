package com.example.jddata;

import android.accessibilityservice.AccessibilityService;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.example.jddata.Entity.BrandEntity;
import com.example.jddata.Entity.MessageDef;
import com.example.jddata.Entity.NiceBuyEntity;
import com.example.jddata.Entity.TypeEntity;
import com.example.jddata.excel.BrandSheet;
import com.example.jddata.excel.DmpSheet;
import com.example.jddata.excel.NiceBuySheet;
import com.example.jddata.excel.TypeSheet;
import com.example.jddata.action.BaseAction;
import com.example.jddata.action.Factory;
import com.example.jddata.shelldroid.EnvManager;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BusHandler extends android.os.Handler {

    public Executor singleThreadExecutor = Executors.newSingleThreadExecutor();
    public AccessibilityService mAccessibilityService;

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

    public BaseAction mCurrentAction;

    public ArrayList<NiceBuyEntity> mNiceBuyTitles = new ArrayList<>();
    public NiceBuySheet mNiceBuySheet;

    public ArrayList<TypeEntity> mTypePrices = new ArrayList<>();
    public TypeSheet mTypeSheet;

    public DmpSheet mDmpSheet;

    @Override
    public void handleMessage(Message msg) {
        int what = msg.what;
        switch (what) {
            case MessageDef.MSG_TIME_OUT:
            case MessageDef.FAIL:
                Log.w("zfr", "action fail");
                if (!GlobalInfo.sIsTest) {
                    BusHandler.getInstance().mCurrentAction = Factory.createAction(mActionType);
                    startCountTimeout();
                    EnvManager.activeByName(mTaskId++ + "");
                }
                break;
            case MessageDef.SUCCESS:
                Log.w("zfr", "success");
                if (!GlobalInfo.sIsTest) {
                    BusHandler.getInstance().mCurrentAction = Factory.createAction(mActionType);
                    startCountTimeout();
                    EnvManager.activeByName(mTaskId++ + "");
                }
                break;
        }
    }

    public int mTaskId = 0;
    public String mActionType;
    public void start() {
        sendEmptyMessage(MessageDef.SUCCESS);
    }

    public void startCountTimeout() {
        removeMessages(MessageDef.MSG_TIME_OUT);
        sendEmptyMessageDelayed(MessageDef.MSG_TIME_OUT, 10 * 1000L);
    }
}
