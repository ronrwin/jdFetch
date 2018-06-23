package com.example.jddata;

import android.os.Message;

public class BusHandler extends android.os.Handler{


    private static class Holder {
        private static BusHandler mInstance = new BusHandler();

        private Holder() {
        }
    }

    public static BusHandler getInstance() {
        return Holder.mInstance;
    }

    @Override
    public void handleMessage(Message msg) {
        int what = msg.what;
        switch (what) {
            case MessageDef.MSG_TIME_OUT:

                break;
        }
    }
}
