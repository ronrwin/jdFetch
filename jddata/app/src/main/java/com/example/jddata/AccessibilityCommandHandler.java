package com.example.jddata;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

public class AccessibilityCommandHandler extends Handler {

    public static final long DEFAULT_COMMAND_INTERVAL = 2000L;
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
                commandCategory();
                break;
            case ServiceCommand.SCROLL_FORWARD:
                scrollForward();
                break;
            case ServiceCommand.INPUT:
                if (msg.obj != null) {
                    String text = (String) msg.obj;
                    checkClipBoard(text);
                    commandInput("android.widget.EditText", "com.jd.lib.search:id/search_text", text);
                }
                break;
            case ServiceCommand.SEARCH:
                search();
                break;
            case ServiceCommand.RECYCLER_SCROLL_FORWARD:
                recyclerScrollForward();
                break;
        }

        if (!mResult) {
            // mResult == false，则不做其他操作，等待超时再执行下一个。
            Log.w(TAG, "command mResult fail， commandCode： " + msg.what);
        } else {
            Log.w(TAG, "command success， commandCode： " + msg.what);
        }

        if (mCommandResult != null) {
            mCommandResult.result(msg.what, mResult);
        }
    }

    private boolean search() {
        return AccessibilityUtils.performClick(mService, "com.jingdong.app.mall:id/avs", false);
    }

    /**
     * 输入内容
     */
    private boolean commandInput(String className, String viewId, String text) {
        List<AccessibilityNodeInfo> nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, viewId);
        if (nodes == null) return false;

        for (AccessibilityNodeInfo node : nodes) {
            if (className.equals(node.getClassName())) {
                if (node.isEnabled() && node.isClickable()) {
                    checkClipBoard(text);
                    node.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                    node.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * For whatsapp的内容预取
     *
     * @param message
     */
    public void checkClipBoard(String message) {
        try {
            ClipboardManager clipboardManager = (ClipboardManager) MainApplication.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboardManager != null) {

                String lastClip = null;
                if (clipboardManager.getText() != null) {
                    lastClip = clipboardManager.getText().toString();
                }
                if (TextUtils.isEmpty(lastClip) || !lastClip.equals(message)) {
                    clipboardManager.setText(message);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void parseChild(AccessibilityNodeInfo node, int index) {
        if (node != null && node.getClassName().toString().contains("TextView")) {
            StringBuilder stringBuilder = new StringBuilder("");
            for (int i = 0; i < index; i++) {
                stringBuilder.append("-");
            }
            if (node.getText() != null) {
                Log.w("zfr", stringBuilder + node.getText().toString());
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            parseChild(node.getChild(i), index + 1);
        }
    }


    private boolean recyclerScrollForward() {
        List<AccessibilityNodeInfo> nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/product_list");
        if (nodes == null) return false;
        for (AccessibilityNodeInfo item : nodes) {
            do {
                List<AccessibilityNodeInfo> oo = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/product_list");
                Log.w("zfr", "---------------------------------------");
                if (oo != null) {
                    parseChild(oo.get(0), 0);
                }
                Log.w("zfr", "---------------------------------------");
                try {
                    Thread.sleep(1000L);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (item.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD));
        }
        return false;
    }

    private boolean scrollForward() {
        List<AccessibilityNodeInfo> nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list");
        if (nodes == null) return false;
        for (AccessibilityNodeInfo item : nodes) {
            do {
                List<AccessibilityNodeInfo> oo = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list");
                Log.w("zfr", "---------------------------------------");
                if (oo != null) {
                    parseChild(oo.get(0), 0);
                }
                Log.w("zfr", "---------------------------------------");
                try {
                    Thread.sleep(1000L);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (item.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD));
        }
        return false;
    }

    private boolean focusSearch() {
        ExecUtils.handleExecCommand("input tap 250 75");
        return true;
    }

    private boolean commandCategory() {
        boolean result =  AccessibilityUtils.performClickByText(mService, "android.widget.ImageView", "分类", false);
        return result;
    }

    public interface CommandResult {
        void result(int commandCode, boolean result);
    }
}
