package com.example.jddata;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Message;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;


public class AccService extends AccessibilityService {
    public static final String TAG = "AccService";
    public static final String PACKAGE_NAME = "com.jingdong.app.mall";

    public static final String PRIVACY = "com.jingdong.app.mall.main.privacy.PrivacyActivity";
    public static final String JD_HOME = "com.jingdong.app.mall.MainFrameActivity";
    public static final String SEARCH = "com.jd.lib.search.view.Activity.SearchActivity";
    public static final String PRODUCT_LIST = "com.jd.lib.search.view.Activity.ProductListActivity";
    public static final String LOGIN = "com.jd.lib.login.LoginActivity";
    public static final String NATIVE_COMMON = "com.jingdong.common.jdreactFramework.activities.JDReactNativeCommonActivity";
    public static final String MIAOSHA = "com.jd.lib.jdmiaosha.activity.MiaoShaActivity";
    public static final String WORTHBUY = "com.jd.lib.worthbuy.view.activity.WorthbuyListActivity";

    public String mLastCommandWindow = null;
    public volatile String mCurrentWindow;

    AccessibilityCommandHandler mAccessibilityCommandHandler;

    private AccessibilityCommandHandler.CommandResult mCommandResult = new AccessibilityCommandHandler.CommandResult() {
        @Override
        public void result(int commandCode, boolean result) {
            ActionMachine currentMachine = MainHandler.getInstance().mCurrentMachine;
            if (currentMachine == null) return;
            Action currentAction = currentMachine.getCurrentAction();
            if (currentAction == null) return;
            ActionMachine.MachineState currentMachineState = currentMachine.getCurrentMachineState();
            if (currentMachineState == null) return;

            if (currentMachineState.concernResult) {
                if (!currentMachineState.isSceneMatch(mLastCommandWindow)) {
                    // 等待超时
                    Log.w("zfr", "wait timeout");
                    return;
                }

                if (result) {
                    // 当前任务完成。
                    if (currentMachineState.commandCode == commandCode) {
                        turnNextState(currentMachine, currentMachineState.scene);
                    }
                } else {
                    if (!currentMachineState.waitForContentChange) {
                    }
                }
            } else {
                turnNextState(currentMachine, currentMachineState.scene);
            }
        }
    };

    @Override
    public void onServiceConnected() {
        Log.d(TAG, "onServiceConnected");
        mAccessibilityCommandHandler = new AccessibilityCommandHandler(this, mCommandResult);
    }

    private void turnNextState(ActionMachine machine, String scene) {
        ActionMachine.MachineState next = machine.getState(1);
        if (next != null) {
            if (next.isSceneMatch(scene)) {
                doCommand(next);
            }
//            else {
//                if (next.canSkip) {
//                    ActionMachine.MachineState aNext = machine.getState(2);
//                    if (aNext != null) {
//                        if (aNext.isSceneMatch(scene)) {
//                            machine.removeCurrentState();
//                            doCommand(aNext);
//                        }
//                    }
//                }
//            }
        }
        machine.removeCurrentState();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return false;
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "onInterrupt");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    public void doCommand(ActionMachine.MachineState state) {
        Log.w(TAG, "doCommand: " + state.commandCode);
        mAccessibilityCommandHandler.removeMessages(state.commandCode);
        Message msg = Message.obtain();
        msg.what = state.commandCode;
        msg.obj = state.obj;
        mAccessibilityCommandHandler.sendMessageDelayed(msg, state.delay);
    }

    public AccessibilityCommandHandler getAccessibilityCommandHandler() {
        return mAccessibilityCommandHandler;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getSource() == null) {
            return;
        }

        int type = event.getEventType();
        String clzName = event.getClassName().toString();
        switch (type) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                Log.w("zfr", clzName);
                break;
        }

        CharSequence packageName = event.getPackageName();
        if (packageName == null) {
            return;
        }

        if (packageName.equals("com.jingdong.app.mall")) {
            handleEvent(event);
        }
    }

    private void handleEvent(final AccessibilityEvent event) {
        if (event == null) {
            return;
        }

        int eventType = event.getEventType();
        String clzName = event.getClassName().toString();

        ActionMachine currentMachine = MainHandler.getInstance().mCurrentMachine;
        if (currentMachine == null) return;
        ActionMachine.MachineState currentMachineState = currentMachine.getCurrentMachineState();
        if (currentMachineState == null) return;

        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                Log.w(TAG, "TYPE_WINDOW_STATE_CHANGED : " + clzName);
                mCurrentWindow = clzName;
                if (currentMachineState.isSceneMatch(clzName)) {
                    doCommand(currentMachineState);
                    mLastCommandWindow = clzName;
                } else {
                    // 当前场景不符合当前任务，检查是否自动跳到下一个步骤场景。
                    if (currentMachineState.canSkip) {
                        ActionMachine.MachineState nextState = currentMachine.getState(1);
                        if (nextState == null) {
                            Log.w("zfr", "success");
                            return;
                        } else {
                            if (nextState.isSceneMatch(clzName)) {
                                mAccessibilityCommandHandler.removeMessages(currentMachineState.commandCode);
                                currentMachine.removeCurrentState();
                                doCommand(nextState);
                                mLastCommandWindow = clzName;
                            }
                        }
                    }
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:

                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                break;
        }
    }

}
