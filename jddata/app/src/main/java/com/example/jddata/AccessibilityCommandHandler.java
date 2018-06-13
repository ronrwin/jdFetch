package com.example.jddata;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class AccessibilityCommandHandler extends Handler {

    public static final long DEFAULT_COMMAND_INTERVAL = 700L;
    public static final long DEFAULT_SCROLL_SLEEP = 100L;

    public static final String TAG = "CommandHandler";

    private AccessibilityService mService;
    private CommandResult mCommandResult;
    private ArrayList<AccessibilityNodeInfo> mConversationList = new ArrayList<>();
    private boolean mResult;

    public ActionMachine.MachineState machineState;

    public AccessibilityCommandHandler(AccessibilityService service, CommandResult commandResult) {
        mService = service;
        mCommandResult = commandResult;
    }

    @Override
    public void handleMessage(final Message msg) {
        mResult = false;
        switch (msg.what) {
            case ServiceCommand.CLICK_SEARCH:
                focusSearch();
                break;
            case ServiceCommand.CATEGORY:
                category();
                break;
        }

        if (!mResult) {
            // mResult == false，则不做其他操作，等待超时再执行下一个。
            Log.w(TAG, "command mResult fail， commandCode： " + msg.what);
        } else {
            Log.w(TAG, "command success， commandCode： " + msg.what);
        }

        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mCommandResult != null) {
                    mCommandResult.result(msg.what, mResult);
                }

            }
        }, 1000L);
    }

    private boolean focusSearch() {
        List<AccessibilityNodeInfo> nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/zj");
        if (nodes == null) return false;
        for (AccessibilityNodeInfo item : nodes) {
            return item.performAction(AccessibilityNodeInfo.ACTION_COLLAPSE);
        }

        return false;
//        try {
//            Process process = Runtime.getRuntime().exec("input tap 250 75");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
    }

    private boolean category() {
        boolean result =  AccessibilityUtils.performClickByText(mService, "android.widget.ImageView", "分类", false);
        return result;
    }

    public interface CommandResult {
        void result(int commandCode, boolean result);
    }
}
