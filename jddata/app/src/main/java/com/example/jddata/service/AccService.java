package com.example.jddata.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Message;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.jddata.BusHandler;
import com.example.jddata.Entity.ActionType;
import com.example.jddata.Entity.MessageDef;
import com.example.jddata.util.AccessibilityUtils;
import com.example.jddata.util.ExecUtils;

import java.util.ArrayList;
import java.util.List;


public class AccService extends AccessibilityService {
    public static final String TAG = "zfr";
    public static final String PACKAGE_NAME = "com.jingdong.app.mall";

    public static final String PRIVACY = "com.jingdong.app.mall.main.privacy.PrivacyActivity";
    public static final String JD_HOME = "com.jingdong.app.mall.MainFrameActivity";
    public static final String SEARCH = "com.jd.lib.search.view.Activity.SearchActivity";
    public static final String PRODUCT_LIST = "com.jd.lib.search.view.Activity.ProductListActivity";
    public static final String LOGIN = "com.jd.lib.login.LoginActivity";
    public static final String NATIVE_COMMON = "com.jingdong.common.jdreactFramework.activities.JDReactNativeCommonActivity";
    public static final String MIAOSHA = "com.jd.lib.jdmiaosha.activity.MiaoShaActivity";
    public static final String WORTHBUY = "com.jd.lib.worthbuy.view.activity.WorthbuyListActivity";
    public static final String INVENTORY = "com.jd.lib.worthbuy.view.activity.InventoryDetailActivity";
    public static final String BRAND_MIAOSHA = "com.jd.lib.jdmiaosha.activity.MiaoShaNewBrandInnerActivity";
    public static final String WEBVIEW_ACTIVITY = "com.jingdong.app.mall.WebActivity";
    public static final String TYPE_MIAOSH_DETAIL = "com.jd.lib.jdmiaosha.activity.MiaoShaNewBrandCategoryInnerActivity";
    public static final String BABEL_ACTIVITY = "com.jingdong.common.babel.view.activity.BabelActivity";
    public static final String DIALOG = "com.jingdong.common.ui.JDDialog";
    public static final String SYSTEM_DIALOG = "android.app.Dialog";
    public static final String PRODUCT_DETAIL = "com.jd.lib.productdetail.ProductDetailActivity";
    public static final String BOTTOM_DIALOG = "com.jingdong.common.ui.JDBottomDialog";

    public String mLastCommandWindow = null;
    public volatile String mCurrentWindow;

    AccessibilityCommandHandler mAccessibilityCommandHandler;

    private AccessibilityCommandHandler.CommandResult mCommandResult = new AccessibilityCommandHandler.CommandResult() {
        @Override
        public void result(int commandCode, boolean result) {
            BaseAction action = BusHandler.getInstance().mCurrentAction;
            if (action == null) return;
            ActionMachine currentMachine = action.getMachine();
            if (currentMachine == null) return;
            MachineState currentMachineState = currentMachine.getCurrentMachineState();
            if (currentMachineState == null) return;

            if (currentMachineState.getConcernResult()) {
                if (!currentMachineState.isSceneMatch(mLastCommandWindow)) {
                    // 等待超时
                    Log.w("zfr", "wait timeout");
                    return;
                }

                // 会买专辑特殊处理
                if (currentMachine.getCurrentActionType().equals(ActionType.NICE_BUY)) {
                    if (currentMachineState.getCommandCode() == ServiceCommand.NICE_BUY_SCROLL) {
                        for (int i = 0; i < BusHandler.getInstance().mNiceBuyTitles.size(); i++) {
                            currentMachine.getCommandArrayList().add(new MachineState(AccService.WORTHBUY, ServiceCommand.NICE_BUY_SELECT));
                            currentMachine.getCommandArrayList().add(new MachineState(AccService.INVENTORY, ServiceCommand.NICE_BUY_DETAIL));
                            currentMachine.getCommandArrayList().add(new MachineState(AccService.INVENTORY, ServiceCommand.GO_BACK));
                        }
                    }
                } else if (currentMachine.getCurrentActionType().equals(ActionType.BRAND_KILL)) {
                    if (currentMachineState.getCommandCode() == ServiceCommand.HOME_BRAND_KILL_SCROLL) {
                        for (int i = 0; i < BusHandler.getInstance().mBrandEntitys.size(); i++) {
                            currentMachine.getCommandArrayList().add(new MachineState(AccService.MIAOSHA, ServiceCommand.BRAND_SELECT_ALL));
                            MachineState detail = new MachineState(AccService.BRAND_MIAOSHA, ServiceCommand.BRAND_DETAIL);
                            detail.setExtraScene(new String[] {WEBVIEW_ACTIVITY});
                            currentMachine.getCommandArrayList().add(detail);
                            MachineState back = new MachineState(AccService.BRAND_MIAOSHA, ServiceCommand.GO_BACK);
                            back.setExtraScene(new String[] {WEBVIEW_ACTIVITY});
                            currentMachine.getCommandArrayList().add(back);
                        }
                    }
                } else if (currentMachine.getCurrentActionType().equals(ActionType.BRAND_KILL_AND_SHOP)) {
                    if (currentMachineState.getCommandCode() == ServiceCommand.HOME_BRAND_KILL_SCROLL
                            || mLastCommandWindow.equals(WEBVIEW_ACTIVITY)
                            || mLastCommandWindow.equals(BABEL_ACTIVITY)) {
                        currentMachine.getCommandArrayList().add(new MachineState(AccService.MIAOSHA, ServiceCommand.BRAND_SELECT_RANDOM));
                        MachineState detail = new MachineState(AccService.BRAND_MIAOSHA, ServiceCommand.BRAND_DETAIL_RANDOM_SHOP);
                        detail.setExtraScene(new String[] {WEBVIEW_ACTIVITY, BABEL_ACTIVITY});
                        currentMachine.getCommandArrayList().add(detail);
                        currentMachine.getCommandArrayList().add(new MachineState(AccService.PRODUCT_DETAIL, ServiceCommand.PRODUCT_BUY));
                        currentMachine.getCommandArrayList().add(new MachineState(AccService.BOTTOM_DIALOG, ServiceCommand.PRODUCT_CONFIRM));
                    }
                } else if (currentMachine.getCurrentActionType().equals(ActionType.TYPE_KILL)) {
                    if (currentMachineState.getCommandCode() == ServiceCommand.HOME_TYPE_KILL_SCROLL) {
                        for (int i = 0; i < BusHandler.getInstance().mTypePrices.size(); i++) {
                            currentMachine.getCommandArrayList().add(new MachineState(AccService.MIAOSHA, ServiceCommand.TYPE_SELECT));
                            MachineState detail = new MachineState(AccService.TYPE_MIAOSH_DETAIL, ServiceCommand.TYPE_DETAIl);
                            detail.setExtraScene(new String[] {WEBVIEW_ACTIVITY});
                            currentMachine.getCommandArrayList().add(detail);
                            MachineState back = new MachineState(AccService.TYPE_MIAOSH_DETAIL, ServiceCommand.GO_BACK);
                            back.setExtraScene(new String[] {WEBVIEW_ACTIVITY});
                            currentMachine.getCommandArrayList().add(back);
                        }
                    }
                } else if (currentMachine.getCurrentActionType().equals(ActionType.DMP_AND_SHOP)) {
                    if (currentMachineState.getCommandCode() == ServiceCommand.DMP_FIND_PRICE) {
                        if (result) {
                            // 成功点击，保留当前任务，后面的清掉，重新构建后面的序列。
                            MachineState current = currentMachine.getCurrentMachineState();
                            ArrayList<MachineState> lists = currentMachine.getCommandArrayList();
                            lists.clear();
                            lists.add(current);

                            lists.add(new MachineState(AccService.PRODUCT_DETAIL, ServiceCommand.PRODUCT_BUY));
                            MachineState bottomBuy = new MachineState(AccService.BOTTOM_DIALOG, ServiceCommand.PRODUCT_CONFIRM);
                            bottomBuy.setCanSkip(true);
                            lists.add(bottomBuy);
                        }
                    }
                }

                if (result) {
                    // 当前任务完成。
                    if (currentMachineState.getCommandCode() == commandCode) {
                        turnNextState(currentMachine, currentMachineState.getScene());
                    }
                } else {
                    if (!currentMachineState.getWaitForContentChange()) {
                    }
                }
            } else {
                turnNextState(currentMachine, currentMachineState.getScene());
            }
        }
    };

    @Override
    public void onServiceConnected() {
        Log.d(TAG, "onServiceConnected");
        mAccessibilityCommandHandler = new AccessibilityCommandHandler(this, mCommandResult);
    }

    private void turnNextState(ActionMachine machine, String scene) {
        MachineState next = machine.getState(1);
        machine.removeCurrentState();
        if (next != null) {
            if (next.isSceneMatch(scene)) {
                doCommand(next);
            } else {
                if (next.getCanSkip()) {
                    turnNextState(machine, scene);
                }
            }
        } else {
            BusHandler.getInstance().sendEmptyMessage(MessageDef.SUCCESS);
        }
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

    public void doCommand(MachineState state) {
        Log.w(TAG, "doCommand: " + state.getCommandCode());
        mAccessibilityCommandHandler.removeMessages(state.getCommandCode());
        Message msg = Message.obtain();
        msg.what = state.getCommandCode();
        msg.obj = state.getObj();
        mAccessibilityCommandHandler.sendMessageDelayed(msg, state.getDelay());
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
                Log.w(TAG, clzName);
                if (DIALOG.equals(clzName)) {
                    AccessibilityUtils.performClick(this, "com.jingdong.app.mall:id/ata", false);
                }
                if (SYSTEM_DIALOG.equals(clzName)) {
                    List<AccessibilityNodeInfo> oks = AccessibilityUtils.findAccessibilityNodeInfosByText(this, "确定");
                    if (AccessibilityUtils.isNodesAvalibale(oks)) {
                        Rect rect = new Rect();
                        AccessibilityNodeInfo ok = oks.get(0);
                        ok.getBoundsInScreen(rect);
                        ExecUtils.handleExecCommand("input tap " + (rect.left + 10) + " " + (rect.top + 10));
                        return;
                    }
                }
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

        ActionMachine currentMachine = BusHandler.getInstance().mCurrentAction.getMachine();
        if (currentMachine == null) return;
        MachineState currentMachineState = currentMachine.getCurrentMachineState();
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
                    if (currentMachineState.getCanSkip()) {
                        MachineState nextState = currentMachine.getState(1);
                        if (nextState == null) {
                            Log.w(TAG, "success");
                            return;
                        } else {
                            if (nextState.isSceneMatch(clzName)) {
                                mAccessibilityCommandHandler.removeMessages(currentMachineState.getCommandCode());
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
