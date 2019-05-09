package com.example.jddata.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.jddata.BusHandler;
import com.example.jddata.Entity.MessageDef;
import com.example.jddata.GlobalInfo;
import com.example.jddata.MainApplication;
import com.example.jddata.action.Action;
import com.example.jddata.action.BaseAction;
import com.example.jddata.util.AccessibilityUtils;
import com.example.jddata.util.ExecUtils;

import java.io.Serializable;
import java.util.List;


public class AccService extends AccessibilityService implements Serializable {
    private static final long serialVersionUID = 1L;


    public static final String TAG = "AccService";
    public static final String PACKAGE_NAME = "com.jingdong.app.mall";

    public static final String PRIVACY = "com.jingdong.app.mall.main.privacy.PrivacyActivity";
    public static final String JD_HOME = "com.jingdong.app.mall.MainFrameActivity";
    public static final String SEARCH = "com.jd.lib.search.view.Activity.SearchActivity";
    public static final String PRODUCT_LIST = "com.jd.lib.search.view.Activity.ProductListActivity";
    public static final String NATIVE_COMMON = "com.jingdong.common.jdreactFramework.activities.JDReactNativeCommonActivity";
    public static final String MIAOSHA = "com.jd.lib.jdmiaosha.activity.MiaoShaActivity";
    public static final String WORTHBUY = "com.jd.lib.worthbuy.view.activity.WorthbuyListActivity";
    public static final String INVENTORY = "com.jd.lib.worthbuy.view.activity.InventoryDetailActivity";
    public static final String INVENTORY_2G = "com.jd.lib.worthbuy.view.activity.InventoryDetail2GActivity";
    public static final String ALBUM_DETAIL_2G = "com.jd.lib.worthbuy.view.activity.AlbumDetail2GActivity";
    public static final String BRAND_MIAOSHA = "com.jd.lib.jdmiaosha.activity.MiaoShaNewBrandInnerActivity";
    public static final String WEBVIEW_ACTIVITY = "com.jingdong.app.mall.WebActivity";
    public static final String TYPE_MIAOSH_DETAIL = "com.jd.lib.jdmiaosha.activity.MiaoShaNewBrandCategoryInnerActivity";
    public static final String BABEL_ACTIVITY = "com.jingdong.common.babel.view.activity.BabelActivity";
    public static final String DIALOG = "com.jingdong.common.ui.JDDialog";
    public static final String SYSTEM_DIALOG = "android.app.Dialog";
    public static final String PRODUCT_DETAIL = "com.jd.lib.productdetail.ProductDetailActivity";
    public static final String BOTTOM_DIALOG = "com.jingdong.common.ui.JDBottomDialog";
    public static final String JSHOP = "com.jd.lib.jshop.jshop.JshopMainShopActivity";
    public static final String CAPTURE_SCAN = "com.jd.lib.scan.lib.zxing.client.android.CaptureActivity";
    public static final String PHOTO_ALBUM = "com.jd.lib.unification.album.activity.PhotoAlbumActivity";
    public static final String SHARE = "com.jingdong.app.mall.basic.ShareActivity";
    public static final String LOGIN = "com.jd.lib.login.LoginActivity";
    public static final String WORTH_DETAIL = "com.jd.lib.worthbuy.view.activity.WorthbuyDetailActivity";
    public static final String WORTH_DETAIL_NEW = "com.jd.lib.worthbuy.view.activity.WorthbuyNewProductDetailActivity";
    public static final String CRASH = "com.android.server.am.AppErrorDialog";
    public static final String SHOPPING_CART = "com.jd.lib.cart.ShoppingCartNewActivity";

    @Override
    public void onCreate() {
        super.onCreate();
        int aa = Integer.parseInt("1");
        BusHandler.Companion.getInstance().setMAccessibilityService(this);
    }

    @Override
    public void onServiceConnected() {
        Log.d(TAG, "onServiceConnected");
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
                MainApplication.sCurrentScene = clzName;
//                if (LOGIN.equals(clzName)) {
//                    AccessibilityUtils.performGlobalActionBack(this);
//                    return;
//                }
                if (CRASH.equals(clzName)) {
                    AccessibilityUtils.performClick(this, "android:id/button1", false);
                    BusHandler.Companion.getInstance().sendEmptyMessage(MessageDef.FAIL);
                    return;
                }
                if (DIALOG.equals(clzName)) {
                    AccessibilityUtils.performClick(this, "com.jingdong.app.mall:id/ata", false);
                    AccessibilityUtils.performClick(this, "com.jingdong.app.mall:id/br", false);
                    return;
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

                List<AccessibilityNodeInfo> lists = AccessibilityUtils.findAccessibilityNodeInfosByText(this, "应用出错");
                if (AccessibilityUtils.isNodesAvalibale(lists)) {
                    List<AccessibilityNodeInfo> closed = AccessibilityUtils.findAccessibilityNodeInfosByText(this, "关闭");
                    if (AccessibilityUtils.isNodesAvalibale(closed)) {
                        for (AccessibilityNodeInfo n : closed) {
                            if (n.isClickable()) {
                                n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                return;
                            }
                        }
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

        Action action = BusHandler.Companion.getInstance().getMCurrentAction();
        if (action == null) {
            return;
        }

        action.handleEvent(event);
    }

}
