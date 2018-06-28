package com.example.jddata;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.example.jddata.Entity.BrandEntity;
import com.example.jddata.Entity.MessageDef;
import com.example.jddata.Entity.NiceBuyEntity;
import com.example.jddata.Entity.TypeEntity;
import com.example.jddata.excel.BrandSheet;
import com.example.jddata.excel.DmpSheet;
import com.example.jddata.excel.NiceBuySheet;
import com.example.jddata.excel.TypeSheet;
import com.example.jddata.service.Action;
import com.example.jddata.service.ActionMachine;
import com.example.jddata.shelldroid.EnvManager;

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

    public ActionMachine mCurrentMachine;

    public ArrayList<NiceBuyEntity> mNiceBuyTitles = new ArrayList<>();
    public NiceBuySheet mNiceBuySheet;

    public ArrayList<BrandEntity> mBrandEntitys = new ArrayList<>();
    public BrandSheet mBrandSheet;

    public ArrayList<TypeEntity> mTypePrices = new ArrayList<>();
    public TypeSheet mTypeSheet;

    public DmpSheet mDmpSheet;

    @Override
    public void handleMessage(Message msg) {
        int what = msg.what;
        switch (what) {
            case MessageDef.MSG_TIME_OUT:

                break;
            case MessageDef.SUCCESS:
                createMachine(mAction);
                EnvManager.activeByName(mTaskId++ + "");
                break;
        }
    }

    public int mTaskId = 0;
    public String mAction;
    public void start() {
        sendEmptyMessage(MessageDef.SUCCESS);
    }

//    public HandlerThread handlerthread = new HandlerThread("MyThread");
//    public BackHandler mBackHandler = new BackHandler(handlerthread.getLooper());
//    public class BackHandler extends Handler {
//
//        public BackHandler(Looper looper) {
//            super(looper);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            EnvManager.activeByName(msg.what + "");
//        }
//    }

    public void createMachine(String actionType) {
        Action action = new Action();
        action.actionType = actionType;
        ActionMachine machine = new ActionMachine(action);
        BusHandler.getInstance().mCurrentMachine = machine;
    }
}
