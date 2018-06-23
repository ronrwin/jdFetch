package com.example.jddata;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.jddata.Entity.Recommend;
import com.example.jddata.Entity.SearchRecommend;
import com.example.jddata.excel.RecommendSheet;
import com.example.jddata.excel.SearchSheet;
import com.example.jddata.shelldroid.EnvManager;

import java.util.ArrayList;
import java.util.HashMap;
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

    private String mCurrentSearch;

    public AccessibilityCommandHandler(AccessibilityService service, CommandResult commandResult) {
        mService = service;
        mCommandResult = commandResult;
    }

    @Override
    public void handleMessage(final Message msg) {
        mResult = false;
        switch (msg.what) {
            case ServiceCommand.CLICK_SEARCH:
                mResult = focusSearch();
                break;
            case ServiceCommand.CATEGORY:
                mResult = commandCategory();
                break;
            case ServiceCommand.HOME_SCROLL:
                mResult = homeRecommendScroll();
                break;
            case ServiceCommand.INPUT:
                if (msg.obj != null) {
                    String text = (String) msg.obj;
                    checkClipBoard(text);
                    mCurrentSearch = text;
                    mResult = commandInput("android.widget.EditText", "com.jd.lib.search:id/search_text", text);
                }
                break;
            case ServiceCommand.SEARCH:
                mResult = search();
                break;
            case ServiceCommand.SEARCH_DATA:
                mResult = searchData();
                break;
            case ServiceCommand.AGREE:
                mResult = agree();
                break;
            case ServiceCommand.CLOSE_AD:
                mResult = closeAd();
                try {
                    Thread.sleep(2000L);
                    MainApplication.startMainJD();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case ServiceCommand.GO_BACK:
                mResult = AccessibilityUtils.performGlobalActionBack(mService);
                break;
            case ServiceCommand.CART_TAB:
                mResult = cartTab();
                break;
            case ServiceCommand.CART_SCROLL:
                mResult = cartRecommendScroll();
                break;
            case ServiceCommand.HOME_TAB:
                mResult = homeTab();
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

    private boolean agree() {
        return AccessibilityUtils.performClick(mService, "com.jingdong.app.mall:id/btb", false);
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

    private void parseChild(AccessibilityNodeInfo node, int index, StringBuilder builder) {
        if (node != null && node.getClassName().toString().contains("TextView")) {
            StringBuilder stringBuilder = new StringBuilder("");
            for (int i = 0; i < index; i++) {
                stringBuilder.append("-");
            }
            if (node.getText() != null) {
                Log.w("zfr", stringBuilder + node.getText().toString());
                builder.append(stringBuilder + node.getText().toString() + "\n");
            }
        }

        if (node != null) {
            for (int i = 0; i < node.getChildCount(); i++) {
                parseChild(node.getChild(i), index + 1, builder);
            }
        }
    }

    /**
     * 购物车-为你推荐
     */
    private boolean cartRecommendScroll() {
        List<AccessibilityNodeInfo> nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.cart:id/cart_no_login_tip");
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) {
            nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/by_");
        }
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false;
        AccessibilityNodeInfo list = AccessibilityUtils.findParentByClassname(nodes.get(0), "android.support.v7.widget.RecyclerView");
        if (list != null) {
            RecommendSheet cartSheet = new RecommendSheet("cart");
            ArrayList<Recommend> result = parseRecommends(list, 10);
            for (int i = 0; i < result.size(); i++) {
                Recommend recommend = result.get(i);
                cartSheet.writeToSheet(i+1, recommend.title, recommend.price);
            }
        }
        return false;
    }

    private boolean searchData() {
        List<AccessibilityNodeInfo> nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/product_list");
        if (nodes == null) return false;
        SearchSheet searchSheet = new SearchSheet(mCurrentSearch);
        for (AccessibilityNodeInfo node : nodes) {
            ArrayList<SearchRecommend> result = parseSearchRecommends(node, 6);
            for (int i = 0; i < result.size(); i++) {
                SearchRecommend recommend = result.get(i);
                searchSheet.writeToSheet(i+1, recommend.title, recommend.price, recommend.comment, recommend.likePercent);
            }
        }
        return false;
    }

    /**
     * 首页-为你推荐
     */
    private boolean homeRecommendScroll() {
        List<AccessibilityNodeInfo> nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list");
        if (nodes == null) return false;
        for (AccessibilityNodeInfo node : nodes) {
            RecommendSheet homeSheet = new RecommendSheet("home");
            ArrayList<Recommend> result = parseRecommends(node, 10);
            for (int i = 0; i < result.size(); i++) {
                Recommend recommend = result.get(i);
                homeSheet.writeToSheet(i+1, recommend.title, recommend.price);
            }
        }
        return false;
    }

    /**
     * 收集卡片信息
     * target: 只有标题，价格类型的卡片
     */
    private ArrayList<Recommend> parseRecommends(AccessibilityNodeInfo listNode, int scrollCount) {
        int index = 0;
        // 最多滑几屏
        int maxIndex = scrollCount;
        if (maxIndex < 0) {
            maxIndex = 100;
        }

        boolean startCount = false;

        ArrayList<Recommend> recommendList = new ArrayList<>();
        do {
            List<AccessibilityNodeInfo> items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/by_");
            if (items != null) {
                startCount = true;
                for (AccessibilityNodeInfo item : items) {
                    List<AccessibilityNodeInfo> titles = item.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/br2");
                    String title = null;
                    if (AccessibilityUtils.isNodesAvalibale(titles)) {
                        if (titles.get(0).getText() != null) {
                            title = titles.get(0).getText().toString();
                        }
                    }
                    List<AccessibilityNodeInfo> prices = item.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/br3");
                    String price = null;
                    if (AccessibilityUtils.isNodesAvalibale(prices)) {
                        if (prices.get(0).getText() != null) {
                            price = prices.get(0).getText().toString();
                        }
                    }
                    recommendList.add(new Recommend(title, price));
                }
            }
            if (startCount) {
                index++;
            }
            try {
                Thread.sleep(1000L);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } while ((listNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                || ExecUtils.handleExecCommand("input swipe 250 800 250 250")) && index <= maxIndex);

        ArrayList<Recommend> finalList = new ArrayList<>();
        // 排重
        HashMap<String, Recommend> map = new HashMap<>();
        for (Recommend recommend : recommendList) {
            if (!map.containsKey(recommend.title)) {
                map.put(recommend.title, recommend);
                finalList.add(recommend);
            } else {
                Recommend old = map.get(recommend.title);
                if (old.price != null && !old.price.equals(recommend.price)) {
                    finalList.add(recommend);
                }
            }
        }
        return finalList;
    }

    /**
     * 收集搜索结果卡片信息
     * target: 只有标题，价格，评论数，好评率类型的卡片
     */
    private ArrayList<SearchRecommend> parseSearchRecommends(AccessibilityNodeInfo listNode, int scrollCount) {
        int index = 0;
        // 最多滑几屏
        int maxIndex = scrollCount;
        if (maxIndex < 0) {
            maxIndex = 100;
        }
        boolean startCount = false;

        ArrayList<SearchRecommend> recommendList = new ArrayList<>();
        do {
            List<AccessibilityNodeInfo> items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/product_list_item");
            if (items != null) {
                startCount = true;
                for (AccessibilityNodeInfo item : items) {
                    List<AccessibilityNodeInfo> titles = item.findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/product_item_name");
                    String title = null;
                    if (AccessibilityUtils.isNodesAvalibale(titles)) {
                        if (titles.get(0).getText() != null) {
                            title = titles.get(0).getText().toString();
                        }
                    }
                    List<AccessibilityNodeInfo> prices = item.findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/product_item_jdPrice");
                    String price = null;
                    if (AccessibilityUtils.isNodesAvalibale(prices)) {
                        if (prices.get(0).getText() != null) {
                            price = prices.get(0).getText().toString();
                        }
                    }
                    List<AccessibilityNodeInfo> comments = item.findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/product_item_commentNumber");
                    String comment = null;
                    if (AccessibilityUtils.isNodesAvalibale(comments)) {
                        if (comments.get(0).getText() != null) {
                            comment = comments.get(0).getText().toString();
                        }
                    }

                    List<AccessibilityNodeInfo> percents = item.findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/product_item_good");
                    String percent = null;
                    if (AccessibilityUtils.isNodesAvalibale(percents)) {
                        if (percents.get(0).getText() != null) {
                            percent = percents.get(0).getText().toString();
                        }
                    }
                    recommendList.add(new SearchRecommend(title, price, comment, percent));
                }
            }
            if (startCount) {
                index++;
            }
            try {
                Thread.sleep(1000L);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } while ((listNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                || ExecUtils.handleExecCommand("input swipe 250 800 250 250")) && index <= maxIndex);

        ArrayList<SearchRecommend> finalList = new ArrayList<>();
        // 排重
        HashMap<String, SearchRecommend> map = new HashMap<>();
        for (SearchRecommend recommend : recommendList) {
            if (!map.containsKey(recommend.title)) {
                map.put(recommend.title, recommend);
                finalList.add(recommend);
            } else {
                SearchRecommend old = map.get(recommend.title);
                if ((old.price != null && !old.price.equals(recommend.price))
                        || (old.comment != null && !old.comment.equals(recommend.comment))
                        || (old.likePercent != null && !old.likePercent.equals(recommend.likePercent))) {
                    finalList.add(recommend);
                }
            }
        }
        return finalList;
    }

    /**
     * 购物车
     */
    private boolean cartTab() {
        return AccessibilityUtils.performClickByText(mService, "android.widget.FrameLayout", "购物车", false);
    }

    /**
     * 首页
     */
    private boolean homeTab() {
        return AccessibilityUtils.performClickByText(mService, "android.widget.FrameLayout", "首页", false);
    }

    private boolean closeAd() {
        ExecUtils.handleExecCommand("input tap 500 75");
        return true;
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
