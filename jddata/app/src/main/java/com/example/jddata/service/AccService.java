package com.example.jddata.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.jddata.BusHandler;
import com.example.jddata.Entity.ActionType;
import com.example.jddata.action.BaseAction;
import com.example.jddata.action.Command;
import com.example.jddata.action.OutputCode;
import com.example.jddata.util.AccessibilityUtils;
import com.example.jddata.util.ExecUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class AccService extends AccessibilityService {
    public static final String TAG = "zfr";
    public static final String PACKAGE_NAME = "com.jingdong.app.mall";

    public static final String PRIVACY = "com.jingdong.app.mall.main.privacy.PrivacyActivity";
    public static final String JD_HOME = "com.jingdong.app.mall.MainFrameActivity";
    public static final String SEARCH = "com.jd.lib.search.view.Activity.SearchActivity";
    public static final String PRODUCT_LIST = "com.jd.lib.search.view.Activity.ProductListActivity";
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
//            BaseAction action = BusHandler.getInstance().mCurrentAction;
//            if (action == null) return;
//            Command currentCommand = action.getCurrentCommand();
//            if (currentCommand == null) return;
//
//            HashMap<OutputCode, Command> ss = new HashMap<>();
//
//            Iterator<Map.Entry<OutputCode, Command>> it = ss.entrySet().iterator();
//            while (it.hasNext()) {
//                Map.Entry<OutputCode, Command> entry = it.next();
//                Command command = entry.getValue();
//            }
//
//
//            if (currentCommand.getConcernResult()) {
//                if (!currentCommand.isSceneMatch(mLastCommandWindow)) {
//                    // 等待超时
//                    Log.w("zfr", "wait timeout");
//                    return;
//                }
//
//                // 会买专辑特殊处理
//                if (action.getMActionType().equals(ActionType.NICE_BUY)) {
//                    if (currentCommand.getCommandCode() == ServiceCommand.NICE_BUY_SCROLL) {
//                        for (int i = 0; i < BusHandler.getInstance().mNiceBuyTitles.size(); i++) {
//                            action.getMCommandArrayList().add(new Command(ServiceCommand.NICE_BUY_SELECT).addScene(AccService.WORTHBUY));
//                            action.getMCommandArrayList().add(new Command(ServiceCommand.NICE_BUY_DETAIL).addScene(AccService.INVENTORY));
//                            action.getMCommandArrayList().add(new Command(ServiceCommand.GO_BACK).addScene(AccService.INVENTORY));
//                        }
//                    }
//                } else if (action.getMActionType().equals(ActionType.BRAND_KILL)) {
//                    if (currentCommand.getCommandCode() == ServiceCommand.HOME_BRAND_KILL_SCROLL) {
//                        for (int i = 0; i < BusHandler.getInstance().mBrandEntitys.size(); i++) {
//                            action.getMCommandArrayList().add(new Command(AccService.MIAOSHA, ServiceCommand.BRAND_SELECT_ALL));
//                            Command detail = new Command(AccService.BRAND_MIAOSHA, ServiceCommand.BRAND_DETAIL);
//                            detail.addScene(WEBVIEW_ACTIVITY);
//                            action.getMCommandArrayList().add(detail);
//                            Command back = new Command(AccService.BRAND_MIAOSHA, ServiceCommand.GO_BACK);
//                            back.addScene(WEBVIEW_ACTIVITY);
//                            action.getMCommandArrayList().add(back);
//                        }
//                    }
//                } else if (action.getMActionType().equals(ActionType.BRAND_KILL_AND_SHOP)) {
//                    if (currentCommand.getCommandCode() == ServiceCommand.HOME_BRAND_KILL_SCROLL
//                            || mLastCommandWindow.equals(WEBVIEW_ACTIVITY)
//                            || mLastCommandWindow.equals(BABEL_ACTIVITY)) {
//                        action.getMCommandArrayList().add(new Command(AccService.MIAOSHA, ServiceCommand.BRAND_SELECT_RANDOM));
//                        Command detail = new Command(AccService.BRAND_MIAOSHA, ServiceCommand.BRAND_DETAIL_RANDOM_SHOP);
//                        detail.addScene(WEBVIEW_ACTIVITY);
//                        detail.addScene(BABEL_ACTIVITY);
//                        action.getMCommandArrayList().add(detail);
//                        action.getMCommandArrayList().add(new Command(AccService.PRODUCT_DETAIL, ServiceCommand.PRODUCT_BUY));
//                        action.getMCommandArrayList().add(new Command(AccService.BOTTOM_DIALOG, ServiceCommand.PRODUCT_CONFIRM));
//                    }
//                } else if (action.getMActionType().equals(ActionType.TYPE_KILL)) {
//                    if (currentCommand.getCommandCode() == ServiceCommand.HOME_TYPE_KILL_SCROLL) {
//                        for (int i = 0; i < BusHandler.getInstance().mTypePrices.size(); i++) {
//                            action.getMCommandArrayList().add(new Command(AccService.MIAOSHA, ServiceCommand.TYPE_SELECT));
//                            Command detail = new Command(AccService.TYPE_MIAOSH_DETAIL, ServiceCommand.TYPE_DETAIl);
//                            detail.addScene(WEBVIEW_ACTIVITY);
//                            action.getMCommandArrayList().add(detail);
//                            Command back = new Command(AccService.TYPE_MIAOSH_DETAIL, ServiceCommand.GO_BACK);
//                            back.addScene(WEBVIEW_ACTIVITY);
//                            action.getMCommandArrayList().add(back);
//                        }
//                    }
//                } else if (action.getMActionType().equals(ActionType.DMP_AND_SHOP)) {
//                    if (currentCommand.getCommandCode() == ServiceCommand.DMP_FIND_PRICE) {
//                        if (result) {
//                            // 成功点击，保留当前任务，后面的清掉，重新构建后面的序列。
//                            Command current = action.getCurrentCommand();
//                            ArrayList<Command> lists = action.getMCommandArrayList();
//                            lists.clear();
//                            lists.add(current);
//
//                            lists.add(new Command(AccService.PRODUCT_DETAIL, ServiceCommand.PRODUCT_BUY));
//                            Command bottomBuy = new Command(AccService.BOTTOM_DIALOG, ServiceCommand.PRODUCT_CONFIRM);
//                            bottomBuy.setCanSkip(true);
//                            lists.add(bottomBuy);
//                        }
//                    }
//                }
//
//                if (result) {
//                    // 当前任务完成。
//                    if (currentCommand.getCommandCode() == commandCode) {
//                        turnNextEvent(action, currentCommand.getScene());
//                    }
//                } else {
//                    if (!currentCommand.getWaitForContentChange()) {
//                    }
//                }
//            } else {
//                turnNextEvent(action, currentCommand.getScene());
//            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        BusHandler.getInstance().mAccessibilityService = this;
    }

    @Override
    public void onServiceConnected() {
        Log.d(TAG, "onServiceConnected");
        mAccessibilityCommandHandler = new AccessibilityCommandHandler(this, mCommandResult);
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

        if (BusHandler.getInstance().mCurrentAction == null) {
            return;
        }

        BaseAction action = BusHandler.getInstance().mCurrentAction;

        if (action != null) {
            action.handleEvent(event);
            return;
        }
//
//        Command currentCommand = action.getCurrentCommand();
//        if (currentCommand == null) return;
//
//        switch (eventType) {
//            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
//                Log.w(TAG, "TYPE_WINDOW_STATE_CHANGED : " + clzName);
//                mCurrentWindow = clzName;
//                if (currentCommand.isSceneMatch(clzName)) {
//                    doCommand(currentCommand);
//                    mLastCommandWindow = clzName;
//                } else {
//                    // 当前场景不符合当前任务，检查是否自动跳到下一个步骤场景。
//                    if (currentCommand.getCanSkip()) {
//                        Command nextState = action.getState(1);
//                        if (nextState == null) {
//                            Log.w(TAG, "success");
//                            return;
//                        } else {
//                            if (nextState.isSceneMatch(clzName)) {
//                                mAccessibilityCommandHandler.removeMessages(currentCommand.getCommandCode());
//                                action.removeCurrentState();
//                                doCommand(nextState);
//                                mLastCommandWindow = clzName;
//                            }
//                        }
//                    }
//                }
//                break;
//        }
    }

}
