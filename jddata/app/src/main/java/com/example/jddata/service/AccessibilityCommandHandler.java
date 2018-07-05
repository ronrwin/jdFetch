package com.example.jddata.service;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.jddata.BusHandler;
import com.example.jddata.Entity.BrandDetail;
import com.example.jddata.Entity.BrandEntity;
import com.example.jddata.Entity.CartGoods;
import com.example.jddata.Entity.MiaoshaRecommend;
import com.example.jddata.Entity.NiceBuyDetail;
import com.example.jddata.Entity.NiceBuyEntity;
import com.example.jddata.Entity.Recommend;
import com.example.jddata.Entity.SearchRecommend;
import com.example.jddata.Entity.TypeEntity;
import com.example.jddata.Entity.WorthBuyEntity;
import com.example.jddata.MainApplication;
import com.example.jddata.excel.BrandSheet;
import com.example.jddata.excel.LeaderboardSheet;
import com.example.jddata.excel.MiaoshaSheet;
import com.example.jddata.excel.NiceBuySheet;
import com.example.jddata.excel.RecommendSheet;
import com.example.jddata.excel.SearchSheet;
import com.example.jddata.excel.TypeSheet;
import com.example.jddata.excel.WorthBuySheet;
import com.example.jddata.util.AccessibilityUtils;
import com.example.jddata.util.ExecUtils;
import com.example.jddata.util.ScreenUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class AccessibilityCommandHandler extends Handler {

    public static final long DEFAULT_COMMAND_INTERVAL = 2000L;
    public static final long DEFAULT_SCROLL_SLEEP = 100L;
    public static final int SCROLL_COUNT = 3;

    public static final String TAG = "CommandHandler";

    private AccessibilityService mService;
    private CommandResult mCommandResult;
    private ArrayList<AccessibilityNodeInfo> mConversationList = new ArrayList<>();
    private boolean mResult;

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
            case ServiceCommand.SEARCH_DATA_RANDOM_BUY:
                mResult = searchDataRandomBuy(SCROLL_COUNT);
                break;
            case ServiceCommand.DMP_FIND_PRICE:
                mResult = dmpFindPrice();
                break;
            case ServiceCommand.AGREE:
                mResult = agree();
                break;
            case ServiceCommand.CLOSE_AD:
                mResult = closeAd();
                sleep(2000L);
                MainApplication.startMainJD(false);
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
            case ServiceCommand.LEADERBOARD:
                mResult = findHomeTextClick("排行榜");
                break;
            case ServiceCommand.HOME_JD_KILL:
                mResult = jdKill();
                break;
            case ServiceCommand.JD_KILL_SCROLL:
                mResult = jdKillScroll(SCROLL_COUNT);
                break;
            case ServiceCommand.WORTH_BUY:
                mResult = findHomeTextClick("发现好货");
                break;
            case ServiceCommand.WORTH_BUY_SCROLL:
                mResult = worthBuyScroll(SCROLL_COUNT);
                break;
            case ServiceCommand.NICE_BUY:
                mResult = findHomeTextClick("会买专辑");
                break;
            case ServiceCommand.NICE_BUY_SCROLL:
                mResult = niceBuyScroll(SCROLL_COUNT);
                break;
            case ServiceCommand.HOME_BRAND_KILL:
                mResult = findHomeTextClick("品牌秒杀");
                break;
            case ServiceCommand.HOME_BRAND_KILL_SCROLL:
                mResult = brandKillScroll(SCROLL_COUNT);
                if (mResult) {
                    BusHandler.getInstance().mBrandSheet = new BrandSheet();
                }
                break;
            case ServiceCommand.HOME_BRAND_KILL_AND_SHOP:
                mResult = brandKillScroll(SCROLL_COUNT);
                break;
            case ServiceCommand.BRAND_SELECT_ALL:
                mResult = brandSelectAll(SCROLL_COUNT);
                break;
            case ServiceCommand.BRAND_SELECT_RANDOM:
                mResult = brandSelectRandom(SCROLL_COUNT);
                break;
            case ServiceCommand.BRAND_DETAIL:
                mResult = brandDetail(SCROLL_COUNT);
                break;
            case ServiceCommand.BRAND_DETAIL_RANDOM_SHOP:
                if (((AccService)mService).mLastCommandWindow.equals(AccService.WEBVIEW_ACTIVITY)
                        || ((AccService)mService).mLastCommandWindow.equals(AccService.BABEL_ACTIVITY)) {
                    mResult = AccessibilityUtils.performGlobalActionBack(mService);
                } else {
                    mResult = brandDetailRandomShop(SCROLL_COUNT);
                }
                break;
            case ServiceCommand.PRODUCT_BUY:
                mResult = AccessibilityUtils.performClick(mService, "com.jd.lib.productdetail:id/pd_invite_friend", false);
                break;
            case ServiceCommand.PRODUCT_CONFIRM:
                mResult = AccessibilityUtils.performClick(mService, "com.jd.lib.productdetail:id/detail_style_add_2_car", false);
                break;
            case ServiceCommand.HOME_TYPE_KILL:
                mResult = findHomeTextClick("品类秒杀");
                break;
            case ServiceCommand.HOME_TYPE_KILL_SCROLL:
                mResult = typeKillScroll(SCROLL_COUNT);
                break;
            case ServiceCommand.TYPE_SELECT:
                mResult = typeSelect(SCROLL_COUNT);
                break;
            case ServiceCommand.TYPE_DETAIl:
                mResult = typeDetail(SCROLL_COUNT);
                break;
            case ServiceCommand.SCREENSHOT:
                ScreenUtils.scrrenShot();
                mResult = true;
                break;
            case ServiceCommand.NICE_BUY_SELECT:
                mResult = niceBuySelect(SCROLL_COUNT);
                break;
            case ServiceCommand.NICE_BUY_DETAIL:
                mResult = niceBuyDetail(SCROLL_COUNT);
                break;
            case ServiceCommand.LEADERBOARD_TAB:
                mResult = leaderBoardTab();
                break;
            case ServiceCommand.DMP_CLICK:
                mResult = dmpclick();
                break;
            case ServiceCommand.DMP_TITLE:
                mResult = dmpTitle();
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

    private boolean dmpTitle() {
        List<AccessibilityNodeInfo> nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/ff");
        if (AccessibilityUtils.isNodesAvalibale(nodes)) {
            AccessibilityNodeInfo titleNode = nodes.get(0);
            if (titleNode.getText() != null) {
                String title = titleNode.getText().toString();
                BusHandler.getInstance().mDmpSheet.writeToSheetAppend(title);
            }
        }
        return false;
    }

    private boolean dmpclick() {
        List<AccessibilityNodeInfo> nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/h8");
        if (AccessibilityUtils.isNodesAvalibale(nodes)) {
            AccessibilityNodeInfo scroll = nodes.get(0);
            if (scroll != null && scroll.getChildCount() > 0) {
                AccessibilityNodeInfo child = scroll.getChild(0);
                if (child != null) {
                    return child.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }
        return false;
    }

    private boolean leaderBoardTab() {
        LeaderboardSheet leaderboardSheet = new LeaderboardSheet();
        AccessibilityNodeInfo root = mService.getRootInActiveWindow();
        if (root != null) {
            // 找第一个text，是当前城市。
            List<AccessibilityNodeInfo> citys = AccessibilityUtils.findChildByClassname(root, "android.widget.TextView");
            if (AccessibilityUtils.isNodesAvalibale(citys)) {
                AccessibilityNodeInfo cityNode = citys.get(0);
                if (cityNode.getText() != null) {
                    String city = cityNode.getText().toString();
                    leaderboardSheet.writeToSheetAppend("城市");
                    leaderboardSheet.writeToSheetAppend(city);
                }
            }

            List<AccessibilityNodeInfo> scrolls = AccessibilityUtils.findChildByClassname(root, "android.widget.HorizontalScrollView");
            if (AccessibilityUtils.isNodesAvalibale(scrolls)) {
                ArrayList<String> tabTitles = new ArrayList<>();
                for (AccessibilityNodeInfo scroll : scrolls) {
                    Rect rect = new Rect();
                    scroll.getBoundsInScreen(rect);
                    if (rect.top < 0 || rect.left < 0 || rect.right < 0 || rect.bottom < 0
                            || rect.bottom > 170) {
                        continue;
                    }
                    List<AccessibilityNodeInfo> tabs = AccessibilityUtils.findChildByClassname(scroll, "android.widget.TextView");
                    if (AccessibilityUtils.isNodesAvalibale(tabs)) {
                        for (AccessibilityNodeInfo tab : tabs) {
                            if (tab.getText() != null) {
                                tabTitles.add(tab.getText().toString());
                            }
                        }
                    }
                }

                leaderboardSheet.writeToSheetAppend("");
                leaderboardSheet.writeToSheetAppend("标签");
                for (String title : tabTitles) {
                    leaderboardSheet.writeToSheetAppend(title);
                }
                return true;
            }
        }
        return false;
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

        RecommendSheet cartSheet = new RecommendSheet("购物车");

        AccessibilityNodeInfo list = AccessibilityUtils.findParentByClassname(nodes.get(0), "android.support.v7.widget.RecyclerView");
        if (list != null) {
            ArrayList<CartGoods> buys = parseBuyRecommends(list);
            cartSheet.writeToSheetAppend("购买部分");
            cartSheet.writeToSheetAppend("标题", "价格", "数量");
            for (CartGoods goods : buys) {
                cartSheet.writeToSheetAppend(goods.getTitle(), goods.getPrice(), goods.getNum());
            }

            cartSheet.writeToSheetAppend("");
            cartSheet.writeToSheetAppend("推荐部分");
            cartSheet.writeToSheetAppend("标题", "价格");
            ArrayList<Recommend> result = parseRecommends(list, SCROLL_COUNT);
            for (int i = 0; i < result.size(); i++) {
                Recommend recommend = result.get(i);
                cartSheet.writeToSheetAppend(recommend.getTitle(), recommend.getPrice());
            }
            return true;
        }
        return false;
    }

    private boolean dmpFindPrice() {
        List<AccessibilityNodeInfo> lists = AccessibilityUtils.findChildByClassname(mService.getRootInActiveWindow(), "android.support.v7.widget.RecyclerView");
        if (AccessibilityUtils.isNodesAvalibale(lists)) {
            AccessibilityNodeInfo list = lists.get(0);
            int index = 0;
            int count = 10;
            do {
                List<AccessibilityNodeInfo> prices = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, "¥");
                if (AccessibilityUtils.isNodesAvalibale(prices)) {
                    for (AccessibilityNodeInfo price : prices) {
                        if (price.isClickable()) {
                           return price.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        } else {
                            AccessibilityNodeInfo parent = AccessibilityUtils.findParentClickable(price);
                            if (parent != null) {
                                return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            }
                        }
                    }
                }
                index++;
            } while ((list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    || ExecUtils.handleExecCommand("input swipe 250 800 250 250")) && index < count);
        }
        return false;
    }

    private boolean searchDataRandomBuy(int scrollCount) {
        List<AccessibilityNodeInfo> nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/product_list");
        if (nodes == null) return false;
        for (AccessibilityNodeInfo node : nodes) {
            int index = 0;
            int random = new Random().nextInt(scrollCount);
            while (index <= random) {
                boolean s = node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                        || ExecUtils.handleExecCommand("input swipe 250 800 250 250");
                index++;
            }

            List<AccessibilityNodeInfo> items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/btn_shopcart");
            if (items != null) {
                for (AccessibilityNodeInfo item : items) {
                    if (item.isClickable()) {
                        return item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }
            }
        }
        return false;
    }

    private boolean searchData() {
        List<AccessibilityNodeInfo> nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/product_list");
        if (nodes == null) return false;
        SearchSheet searchSheet = new SearchSheet(mCurrentSearch);
        for (AccessibilityNodeInfo node : nodes) {
            ArrayList<SearchRecommend> result = parseSearchRecommends(node, SCROLL_COUNT);
            for (int i = 0; i < result.size(); i++) {
                SearchRecommend recommend = result.get(i);
                searchSheet.writeToSheetAppend(recommend.getTitle(), recommend.getPrice(), recommend.getComment(), recommend.getLikePercent());
            }
            return true;
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
            RecommendSheet homeSheet = new RecommendSheet("首页");
            ArrayList<Recommend> result = parseRecommends(node, SCROLL_COUNT);
            for (Recommend recommend : result) {
                homeSheet.writeToSheetAppend(recommend.getTitle(), recommend.getPrice());
            }
            return true;
        }
        return false;
    }

    private ArrayList<CartGoods> parseBuyRecommends(AccessibilityNodeInfo listNode) {
        ArrayList<CartGoods> buys = new ArrayList<>();
        do {
            // 购买部分
            List<AccessibilityNodeInfo> buyRecommends = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.cart:id/cart_single_product_item_layout");
            if (buyRecommends != null) {
                for (AccessibilityNodeInfo item : buyRecommends) {
                    List<AccessibilityNodeInfo> titles = item.findAccessibilityNodeInfosByViewId("com.jd.lib.cart:id/cart_single_product_name");
                    String title = null;
                    if (AccessibilityUtils.isNodesAvalibale(titles)) {
                        if (titles.get(0).getText() != null) {
                            title = titles.get(0).getText().toString();
                            if (title.startsWith("1 ")) {
                                title = title.replace("1 ", "");
                            }
                        }
                    }
                    List<AccessibilityNodeInfo> prices = item.findAccessibilityNodeInfosByViewId("com.jd.lib.cart:id/cart_single_product_price");
                    String price = null;
                    if (AccessibilityUtils.isNodesAvalibale(prices)) {
                        if (prices.get(0).getText() != null) {
                            price = prices.get(0).getText().toString();
                        }
                    }

                    List<AccessibilityNodeInfo> nums = item.findAccessibilityNodeInfosByViewId("com.jd.lib.cart:id/cart_single_product_et_num");
                    String num = null;
                    if (AccessibilityUtils.isNodesAvalibale(nums)) {
                        if (nums.get(0).getText() != null) {
                            num = nums.get(0).getText().toString();
                        }
                    }
                    buys.add(new CartGoods(title, price, num));
                }
            } else {
                break;
            }
            sleep(DEFAULT_SCROLL_SLEEP);
        } while ((listNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                || ExecUtils.handleExecCommand("input swipe 250 800 250 250")));

        ArrayList<CartGoods> buyFinalList = new ArrayList<>();
        // 排重
        HashMap<String, CartGoods> buyMap = new HashMap<>();
        for (CartGoods recommend : buys) {
            if (!buyMap.containsKey(recommend.getTitle())) {
                buyMap.put(recommend.getTitle(), recommend);
                buyFinalList.add(recommend);
            } else {
                CartGoods old = buyMap.get(recommend.getTitle());
                if (old.getPrice() != null && !old.getPrice().equals(recommend.getPrice())
                        && old.getNum() != null && !old.getNum().equals(recommend.getNum())) {
                    buyFinalList.add(recommend);
                }
            }
        }
        return buyFinalList;
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

        while (listNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD));

        ArrayList<Recommend> recommendList = new ArrayList<>();
        do {
            // 推荐部分
            List<AccessibilityNodeInfo> items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/by_");
            if (items != null) {
                for (AccessibilityNodeInfo item : items) {
                    List<AccessibilityNodeInfo> titles = item.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/br2");
                    String title = null;
                    if (AccessibilityUtils.isNodesAvalibale(titles)) {
                        if (titles.get(0).getText() != null) {
                            title = titles.get(0).getText().toString();
                            if (title.startsWith("1 ")) {
                                title = title.replace("1 ", "");
                            }
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
            index++;
            sleep(DEFAULT_SCROLL_SLEEP);
        } while ((listNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                || ExecUtils.handleExecCommand("input swipe 250 800 250 250")) && index <= maxIndex);

        ArrayList<Recommend> finalList = new ArrayList<>();
        // 排重
        HashMap<String, Recommend> map = new HashMap<>();
        for (Recommend recommend : recommendList) {
            if (!map.containsKey(recommend.getTitle())) {
                map.put(recommend.getTitle(), recommend);
                finalList.add(recommend);
            } else {
                Recommend old = map.get(recommend.getTitle());
                if (old.getPrice() != null && !old.getPrice().equals(recommend.getPrice())) {
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

        ArrayList<SearchRecommend> recommendList = new ArrayList<>();
        do {
            List<AccessibilityNodeInfo> items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/product_list_item");
            if (items != null) {
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
            index++;
            sleep(DEFAULT_SCROLL_SLEEP);
        } while ((listNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                || ExecUtils.handleExecCommand("input swipe 250 800 250 250")) && index <= maxIndex);

        ArrayList<SearchRecommend> finalList = new ArrayList<>();
        // 排重
        HashMap<String, SearchRecommend> map = new HashMap<>();
        for (SearchRecommend recommend : recommendList) {
            if (!map.containsKey(recommend.getTitle())) {
                map.put(recommend.getTitle(), recommend);
                finalList.add(recommend);
            } else {
                SearchRecommend old = map.get(recommend.getTitle());
                if ((old.getPrice() != null && !old.getPrice().equals(recommend.getPrice()))
                        || (old.getComment() != null && !old.getComment().equals(recommend.getComment()))
                        || (old.getLikePercent() != null && !old.getLikePercent().equals(recommend.getLikePercent()))) {
                    finalList.add(recommend);
                }
            }
        }
        return finalList;
    }

    private void sleep(long sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    /**
     * 首页
     */
    private boolean jdKill() {
        return AccessibilityUtils.performClick(mService, "com.jingdong.app.mall:id/bkt", false);
    }

    /**
     * 发现好货
     */
    private boolean worthBuyScroll(int scrollCount) {
        List<AccessibilityNodeInfo> nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/product_item");
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false;
        AccessibilityNodeInfo list = AccessibilityUtils.findParentByClassname(nodes.get(0), "android.support.v7.widget.RecyclerView");

        if (list != null) {
            int index = 0;
            // 最多滑几屏
            int maxIndex = scrollCount;
            if (maxIndex < 0) {
                maxIndex = 100;
            }

            ArrayList<WorthBuyEntity> worthList = new ArrayList<>();
            do {
                List<AccessibilityNodeInfo> products = list.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/product_item");
                for (AccessibilityNodeInfo product : products) {
                    List<AccessibilityNodeInfo> titles = product.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/product_name");
                    String title = null;
                    if (AccessibilityUtils.isNodesAvalibale(titles)) {
                        if (titles.get(0).getText() != null) {
                            title = titles.get(0).getText().toString();
                        }
                    }
                    List<AccessibilityNodeInfo> descs = product.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/product_desc");
                    String desc = null;
                    if (AccessibilityUtils.isNodesAvalibale(descs)) {
                        if (descs.get(0).getText() != null) {
                            desc = descs.get(0).getText().toString();
                        }
                    }
                    List<AccessibilityNodeInfo> collects = product.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/text_collect_number");
                    String collect = null;
                    if (AccessibilityUtils.isNodesAvalibale(collects)) {
                        if (collects.get(0).getText() != null) {
                            collect = collects.get(0).getText().toString();
                        }
                    }
                    worthList.add(new WorthBuyEntity(title, desc, collect));
                }
                index++;
                sleep(DEFAULT_SCROLL_SLEEP);
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) && index <= maxIndex);

            ArrayList<WorthBuyEntity> finalList = new ArrayList<>();
            // 排重
            HashMap<String, WorthBuyEntity> map = new HashMap<>();
            for (WorthBuyEntity worth : worthList) {
                if (!map.containsKey(worth.getTitle())) {
                    map.put(worth.getTitle(), worth);
                    finalList.add(worth);
                } else {
                    WorthBuyEntity old = map.get(worth.getTitle());
                    if ((old.getDesc() != null && !old.getDesc().equals(worth.getDesc()))
                            || (old.getCollect() != null && !old.getCollect().equals(worth.getCollect()))) {
                        finalList.add(worth);
                    }
                }
            }

            WorthBuySheet worthSheet = new WorthBuySheet();
            for (WorthBuyEntity worth : finalList) {
                worthSheet.writeToSheetAppend(worth.getTitle(), worth.getDesc(), worth.getCollect());
            }
            return true;
        }
        return false;
    }

    private boolean niceBuyDetail(int scrollCount) {
        List<AccessibilityNodeInfo> nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/recycler_view");
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false;
        AccessibilityNodeInfo list = nodes.get(0);
        if (list != null) {
            List<AccessibilityNodeInfo> descs = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/tv_desc");
            if (AccessibilityUtils.isNodesAvalibale(descs)) {
                AccessibilityNodeInfo desc = descs.get(0);
                if (desc.getText() != null) {
                    String des = desc.getText().toString();
                    if (BusHandler.getInstance().mNiceBuySheet != null) {
                        BusHandler.getInstance().mNiceBuySheet.writeToSheetAppend("描述");
                        BusHandler.getInstance().mNiceBuySheet.writeToSheetAppend(des);
                    }
                }
            }


            int index = 0;
            // 最多滑几屏
            int maxIndex = scrollCount;
            if (maxIndex < 0) {
                maxIndex = 100;
            }

            ArrayList<NiceBuyDetail> detailList = new ArrayList<>();
            do {
                List<AccessibilityNodeInfo> prices = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/tv_price");
                if (AccessibilityUtils.isNodesAvalibale(prices)) {
                    for (AccessibilityNodeInfo priceNode : prices) {
                        AccessibilityNodeInfo parent = priceNode.getParent();
                        if (parent != null) {
                            List<AccessibilityNodeInfo> titles = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_title");
                            String title = null;
                            if (AccessibilityUtils.isNodesAvalibale(titles)) {
                                if (titles.get(0).getText() != null) {
                                    title = titles.get(0).getText().toString();
                                }
                            }

                            String price = null;
                            if (priceNode.getText() != null) {
                                price = priceNode.getText().toString();
                            }

                            List<AccessibilityNodeInfo> originPrices = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_original_price");
                            String origin = null;
                            if (AccessibilityUtils.isNodesAvalibale(originPrices)) {
                                if (originPrices.get(0).getText() != null) {
                                    origin = originPrices.get(0).getText().toString();
                                }
                            }
                            detailList.add(new NiceBuyDetail(title, price, origin));
                        }
                    }
                }

                index++;
                sleep(DEFAULT_SCROLL_SLEEP);
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) && index <= maxIndex);

            ArrayList<NiceBuyDetail> finalList = new ArrayList<>();
            // 排重
            HashMap<String, NiceBuyDetail> map = new HashMap<>();
            for (NiceBuyDetail worth : detailList) {
                if (!map.containsKey(worth.getTitle())) {
                    map.put(worth.getTitle(), worth);
                    finalList.add(worth);
                } else {
                    NiceBuyDetail old = map.get(worth.getTitle());
                    if ((old.getPrice() != null && !old.getPrice().equals(worth.getPrice()))
                            || (old.getOrigin_price() != null && !old.getOrigin_price().equals(worth.getOrigin_price()))) {
                        finalList.add(worth);
                    }
                }
            }

            BusHandler.getInstance().mNiceBuySheet.writeToSheetAppend("产品", "价格", "原价");
            for (NiceBuyDetail detail : finalList) {
                BusHandler.getInstance().mNiceBuySheet.writeToSheetAppend(detail.getTitle(), detail.getPrice(), detail.getOrigin_price());
            }
            return true;
        }
        return false;
    }

    private boolean niceBuySelect(int scrollCount) {
        List<AccessibilityNodeInfo> nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/ll_zdm_inventory_header");
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false;
        AccessibilityNodeInfo list = AccessibilityUtils.findParentByClassname(nodes.get(0), "android.support.v7.widget.RecyclerView");

        if (list != null && !BusHandler.getInstance().mNiceBuyTitles.isEmpty()) {
            int index = 0;
            // 最多滑几屏
            int maxIndex = scrollCount;
            if (maxIndex < 0) {
                maxIndex = 100;
            }
            do {
                // 滑回顶部
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD));

            do {
                NiceBuyEntity niceBuyEntity = BusHandler.getInstance().mNiceBuyTitles.get(0);
                String title = niceBuyEntity.getTitle();
                List<AccessibilityNodeInfo> selectNodes = list.findAccessibilityNodeInfosByText(title);
                if (AccessibilityUtils.isNodesAvalibale(selectNodes)) {
                    AccessibilityNodeInfo parent = AccessibilityUtils.findParentClickable(selectNodes.get(0));
                    if (parent != null) {
                        if (BusHandler.getInstance().mNiceBuySheet != null) {
                            BusHandler.getInstance().mNiceBuySheet.writeToSheetAppend("");
                            BusHandler.getInstance().mNiceBuySheet.addTitleRow();
                            BusHandler.getInstance().mNiceBuySheet.writeToSheetAppend(niceBuyEntity.getTitle(), niceBuyEntity.getDesc(), niceBuyEntity.getPageView(), niceBuyEntity.getCollect());
                        }
                        boolean result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        if (result) {
                            BusHandler.getInstance().mNiceBuyTitles.remove(0);
                        }
                        return result;
                    }
                }
                index++;
            } while (!BusHandler.getInstance().mNiceBuyTitles.isEmpty() && list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) && index <= maxIndex);
        }
        return BusHandler.getInstance().mNiceBuyTitles.isEmpty();
    }

    /**
     * 会买专辑，下拉抓取标题
     */
    private boolean niceBuyScroll(int scrollCount) {
        List<AccessibilityNodeInfo> nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/ll_zdm_inventory_header");
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false;
        AccessibilityNodeInfo list = AccessibilityUtils.findParentByClassname(nodes.get(0), "android.support.v7.widget.RecyclerView");

        if (list != null) {
            int index = 0;
            // 最多滑几屏
            int maxIndex = scrollCount;
            if (maxIndex < 0) {
                maxIndex = 100;
            }

            ArrayList<NiceBuyEntity> worthList = new ArrayList<>();
            do {
                List<AccessibilityNodeInfo> descsNodes = list.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_zdm_inventory_desc");
                if (AccessibilityUtils.isNodesAvalibale(descsNodes)) {
                    for (AccessibilityNodeInfo descNode : descsNodes) {
                        AccessibilityNodeInfo parent = descNode.getParent();
                        if (parent != null) {
                            List<AccessibilityNodeInfo> titles = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_zdm_inventory_title");
                            String title = null;
                            if (AccessibilityUtils.isNodesAvalibale(titles)) {
                                if (titles.get(0).getText() != null) {
                                    title = titles.get(0).getText().toString();
                                }
                            }
                            List<AccessibilityNodeInfo> descs = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_zdm_inventory_desc");
                            String desc = null;
                            if (AccessibilityUtils.isNodesAvalibale(descs)) {
                                if (descs.get(0).getText() != null) {
                                    desc = descs.get(0).getText().toString();
                                }
                            }
                            List<AccessibilityNodeInfo> pageViews = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/page_view");
                            String pageView = null;
                            if (AccessibilityUtils.isNodesAvalibale(pageViews)) {
                                if (pageViews.get(0).getText() != null) {
                                    pageView = pageViews.get(0).getText().toString();
                                }
                            }
                            List<AccessibilityNodeInfo> collects = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/text_collect_number");
                            String collect = null;
                            if (AccessibilityUtils.isNodesAvalibale(collects)) {
                                if (collects.get(0).getText() != null) {
                                    collect = collects.get(0).getText().toString();
                                }
                            }
                            worthList.add(new NiceBuyEntity(title, desc, pageView, collect));
                        }
                    }
                }

                index++;
                sleep(DEFAULT_SCROLL_SLEEP);
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) && index <= maxIndex);

            ArrayList<NiceBuyEntity> finalList = new ArrayList<>();
            // 排重
            HashMap<String, NiceBuyEntity> map = new HashMap<>();
            for (NiceBuyEntity worth : worthList) {
                if (!map.containsKey(worth.getTitle())) {
                    map.put(worth.getTitle(), worth);
                    finalList.add(worth);
                } else {
                    NiceBuyEntity old = map.get(worth.getTitle());
                    if ((old.getDesc() != null && !old.getDesc().equals(worth.getDesc()))
                            || (old.getCollect() != null && !old.getCollect().equals(worth.getCollect()))
                            || (old.getPageView() != null && !old.getPageView().equals(worth.getPageView()))) {
                        finalList.add(worth);
                    }
                }
            }

            // 记录标题列表
            BusHandler.getInstance().mNiceBuyTitles = finalList;
            // 初始化。
            BusHandler.getInstance().mNiceBuySheet = new NiceBuySheet();
            return true;
        }
        return false;
    }

    private boolean jdKillScroll(int scrollCount) {
        List<AccessibilityNodeInfo> nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list");
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false;

        String miaoshaTime = null;
        List<AccessibilityNodeInfo> tabs = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/miaosha_tab_text");
        for (AccessibilityNodeInfo tab : tabs) {
            if (tab.getText() != null) {
                String tabText = tab.getText().toString();
                if ("抢购中".equals(tabText)) {
                    AccessibilityNodeInfo parent = tab.getParent();
                    if (parent != null) {
                        List<AccessibilityNodeInfo> times = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/miaosha_tab_time");
                        if (AccessibilityUtils.isNodesAvalibale(times) && times.get(0).getText() != null) {
                            miaoshaTime = times.get(0).getText().toString().replace(":", "_");
                        }
                    }
                }
            }
        }

        int index = 0;
        // 最多滑几屏
        int maxIndex = scrollCount;
        if (maxIndex < 0) {
            maxIndex = 100;
        }

        ArrayList<MiaoshaRecommend> miaoshaList = new ArrayList<>();
        do {
            List<AccessibilityNodeInfo> titles = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/limit_buy_product_item_name");
            if (AccessibilityUtils.isNodesAvalibale(titles)) {
                for (AccessibilityNodeInfo titleNode : titles) {
                    AccessibilityNodeInfo parent = titleNode.getParent();
                    if (parent != null) {
                        String title = null;
                        if (titleNode.getText() != null) {
                            title = titleNode.getText().toString();
                        }
                        List<AccessibilityNodeInfo> prices = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_miaosha_price");
                        String price = null;
                        if (AccessibilityUtils.isNodesAvalibale(prices)) {
                            if (prices.get(0).getText() != null) {
                                price = prices.get(0).getText().toString();
                            }
                        }
                        List<AccessibilityNodeInfo> miaoshaPrices = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_jd_price");
                        String miaoshaPrice = null;
                        if (AccessibilityUtils.isNodesAvalibale(miaoshaPrices)) {
                            if (miaoshaPrices.get(0).getText() != null) {
                                miaoshaPrice = miaoshaPrices.get(0).getText().toString();
                            }
                        }
                        miaoshaList.add(new MiaoshaRecommend(title, price, miaoshaPrice));
                    }
                }
            }
            index++;
            sleep(DEFAULT_SCROLL_SLEEP);
        } while (nodes.get(0).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) && index <= maxIndex);

        ArrayList<MiaoshaRecommend> finalList = new ArrayList<>();
        // 排重
        HashMap<String, MiaoshaRecommend> map = new HashMap<>();
        for (MiaoshaRecommend miaosha : miaoshaList) {
            if (!map.containsKey(miaosha.getTitle())) {
                map.put(miaosha.getTitle(), miaosha);
                finalList.add(miaosha);
            } else {
                MiaoshaRecommend old = map.get(miaosha.getTitle());
                if ((old.getPrice() != null && !old.getPrice().equals(miaosha.getPrice()))
                        || (old.getMiaoshaPrice() != null && !old.getMiaoshaPrice().equals(miaosha.getMiaoshaPrice()))) {
                    finalList.add(miaosha);
                }
            }
        }

        MiaoshaSheet miaoshaSheet = new MiaoshaSheet("京东秒杀_" + miaoshaTime);
        for (MiaoshaRecommend miaosha : finalList) {
            miaoshaSheet.writeToSheetAppend(miaosha.getTitle(), miaosha.getPrice(), miaosha.getMiaoshaPrice());
        }
        return true;
    }

    private boolean brandKillRandomSelect() {

        return false;
    }

    private boolean brandKillScroll(int scrollCount) {
        List<AccessibilityNodeInfo> nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list");
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false;
        AccessibilityNodeInfo list = nodes.get(0);
        if (list != null) {
            int index = 0;
            // 最多滑几屏
            int maxIndex = scrollCount;
            if (maxIndex < 0) {
                maxIndex = 100;
            }
            ArrayList<BrandEntity> brandList = new ArrayList<>();
            do {
                List<AccessibilityNodeInfo> brandTitles = list.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/miaosha_brand_title");
                for (AccessibilityNodeInfo brand : brandTitles) {
                    AccessibilityNodeInfo parent = brand.getParent();
                    if (parent != null) {
                        String title = null;
                        if (brand.getText() != null) {
                            title = brand.getText().toString();
                        }

                        List<AccessibilityNodeInfo> subTitles = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/miaosha_brand_subtitle");
                        String subTitle = null;
                        if (AccessibilityUtils.isNodesAvalibale(subTitles)) {
                            if (subTitles.get(0).getText() != null) {
                                subTitle = subTitles.get(0).getText().toString();
                            }
                        }
                        brandList.add(new BrandEntity(title, subTitle));
                    }
                }
                index++;
                sleep(DEFAULT_SCROLL_SLEEP);
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) && index <= maxIndex);

            ArrayList<BrandEntity> finalList = new ArrayList<>();
            // 排重
            HashMap<String, BrandEntity> map = new HashMap<>();
            for (BrandEntity worth : brandList) {
                if (!map.containsKey(worth.getTitle())) {
                    map.put(worth.getTitle(), worth);
                    finalList.add(worth);
                } else {
                    BrandEntity old = map.get(worth.getTitle());
                    if ((old.getSubtitle() != null && !old.getSubtitle().equals(worth.getSubtitle()))) {
                        finalList.add(worth);
                    }
                }
            }
            BusHandler.getInstance().mBrandEntitys = finalList;
            return true;
        }

        return false;
    }

    private boolean brandSelectRandom(int scrollCount) {
        List<AccessibilityNodeInfo> nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list");
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false;
        AccessibilityNodeInfo list = nodes.get(0);
        if (list != null && !BusHandler.getInstance().mBrandEntitys.isEmpty()) {
            int index = 0;
            // 最多滑几屏
            int maxIndex = scrollCount;
            if (maxIndex < 0) {
                maxIndex = 100;
            }
            do {
                // 滑回顶部
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD));

            int num = new Random().nextInt(BusHandler.getInstance().mBrandEntitys.size());
            BrandEntity brandEntity = BusHandler.getInstance().mBrandEntitys.get(num);
            String title = brandEntity.getTitle();
            do {
                List<AccessibilityNodeInfo> selectNodes = list.findAccessibilityNodeInfosByText(title);
                if (AccessibilityUtils.isNodesAvalibale(selectNodes)) {
                    AccessibilityNodeInfo parent = AccessibilityUtils.findParentClickable(selectNodes.get(0));
                    if (parent != null) {
                        boolean result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        return result;
                    }
                }
                index++;
                sleep(DEFAULT_SCROLL_SLEEP);
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) && index <= maxIndex);
        }
        return false;
    }

    private boolean brandSelectAll(int scrollCount) {
        List<AccessibilityNodeInfo> nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list");
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false;
        AccessibilityNodeInfo list = nodes.get(0);

        if (list != null && !BusHandler.getInstance().mBrandEntitys.isEmpty()) {
            int index = 0;
            // 最多滑几屏
            int maxIndex = scrollCount;
            if (maxIndex < 0) {
                maxIndex = 100;
            }
            do {
                // 滑回顶部
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD));

            do {
                BrandEntity brandEntity = BusHandler.getInstance().mBrandEntitys.get(0);
                String title = brandEntity.getTitle();

                List<AccessibilityNodeInfo> selectNodes = list.findAccessibilityNodeInfosByText(title);
                if (AccessibilityUtils.isNodesAvalibale(selectNodes)) {
                    AccessibilityNodeInfo parent = AccessibilityUtils.findParentClickable(selectNodes.get(0));
                    if (parent != null) {
                        if (BusHandler.getInstance().mBrandSheet != null) {
                            BusHandler.getInstance().mBrandSheet.writeToSheetAppend("");
                            BusHandler.getInstance().mBrandSheet.addTitleRow();
                            BusHandler.getInstance().mBrandSheet.writeToSheetAppend(brandEntity.getTitle(), brandEntity.getSubtitle());
                        }
                        boolean result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        if (result) {
                            BusHandler.getInstance().mBrandEntitys.remove(0);
                        }
                        return result;
                    }
                }
                index++;
                sleep(DEFAULT_SCROLL_SLEEP);
            } while (!BusHandler.getInstance().mBrandEntitys.isEmpty() && list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) && index <= maxIndex);
        }
        return BusHandler.getInstance().mBrandEntitys.isEmpty();
    }

    private boolean brandDetailRandomShop(int scrollCount) {
        List<AccessibilityNodeInfo> nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list");
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false;
        AccessibilityNodeInfo list = nodes.get(0);
        if (list != null) {
            int randomScroll = new Random().nextInt(scrollCount);
            int index = 0;
            while (index < randomScroll) {
                list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                sleep(DEFAULT_SCROLL_SLEEP);
                index++;
            }

            do {
                List<AccessibilityNodeInfo> shops = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/app_limit_buy_sale_ms_button");
                if (AccessibilityUtils.isNodesAvalibale(shops)) {
                    for (AccessibilityNodeInfo info : shops) {
                        if (info.isClickable()) {
                            return info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    }
                }
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD));

            return true;
        }
        return false;
    }

    private boolean brandDetail(int scrollCount) {
        List<AccessibilityNodeInfo> nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list");
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false;
        AccessibilityNodeInfo list = nodes.get(0);
        if (list != null) {
            int index = 0;
            // 最多滑几屏
            int maxIndex = scrollCount;
            if (maxIndex < 0) {
                maxIndex = 100;
            }

            ArrayList<BrandDetail> detailList = new ArrayList<>();
            do {
                List<AccessibilityNodeInfo> titles = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/limit_buy_product_item_name");
                if (AccessibilityUtils.isNodesAvalibale(titles)) {
                    for (AccessibilityNodeInfo titleNode : titles) {
                        AccessibilityNodeInfo parent = titleNode.getParent();
                        if (parent != null) {
                            String title = null;
                            if (titleNode.getText() != null) {
                                title = titleNode.getText().toString();
                            }

                            List<AccessibilityNodeInfo> prices = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_miaosha_price");
                            String price = null;
                            if (AccessibilityUtils.isNodesAvalibale(prices)) {
                                if (prices.get(0).getText() != null) {
                                    price = prices.get(0).getText().toString();
                                }
                            }

                            List<AccessibilityNodeInfo> originPrices = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_jd_price");
                            String origin = null;
                            if (AccessibilityUtils.isNodesAvalibale(originPrices)) {
                                if (originPrices.get(0).getText() != null) {
                                    origin = originPrices.get(0).getText().toString();
                                }
                            }
                            detailList.add(new BrandDetail(title, price, origin));
                        }
                    }
                }

                index++;
                sleep(DEFAULT_SCROLL_SLEEP);
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) && index <= maxIndex);

            ArrayList<BrandDetail> finalList = new ArrayList<>();
            // 排重
            HashMap<String, BrandDetail> map = new HashMap<>();
            for (BrandDetail worth : detailList) {
                if (!map.containsKey(worth.getTitle())) {
                    map.put(worth.getTitle(), worth);
                    finalList.add(worth);
                } else {
                    BrandDetail old = map.get(worth.getTitle());
                    if ((old.getPrice() != null && !old.getPrice().equals(worth.getPrice()))
                            || (old.getOrigin_price() != null && !old.getOrigin_price().equals(worth.getOrigin_price()))) {
                        finalList.add(worth);
                    }
                }
            }

            BusHandler.getInstance().mBrandSheet.writeToSheetAppend("产品", "价格", "原价");
            for (BrandDetail detail : finalList) {
                BusHandler.getInstance().mBrandSheet.writeToSheetAppend(detail.getTitle(), detail.getPrice(), detail.getOrigin_price());
            }
            return true;
        }
        return false;
    }

    private boolean typeKillScroll(int scrollCount) {
        List<AccessibilityNodeInfo> nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list");
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false;
        AccessibilityNodeInfo list = nodes.get(0);
        if (list != null) {
            int index = 0;
            // 最多滑几屏
            int maxIndex = scrollCount;
            if (maxIndex < 0) {
                maxIndex = 100;
            }

            ArrayList<TypeEntity> priceList = new ArrayList<>();
            do {
                List<AccessibilityNodeInfo> prices1 = list.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/brand_item_price1");
                for (AccessibilityNodeInfo price1 : prices1) {
                    AccessibilityNodeInfo parent = price1.getParent();
                    if (parent != null) {
                        String title = null;
                        if (price1.getText() != null) {
                            title = price1.getText().toString();
                        }

                        List<AccessibilityNodeInfo> prices2 = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/brand_item_price2");
                        String price2 = null;
                        if (AccessibilityUtils.isNodesAvalibale(prices2)) {
                            if (prices2.get(0).getText() != null) {
                                price2 = prices2.get(0).getText().toString();
                            }
                        }

                        List<AccessibilityNodeInfo> prices3 = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/brand_item_price3");
                        String price3 = null;
                        if (AccessibilityUtils.isNodesAvalibale(prices3)) {
                            if (prices3.get(0).getText() != null) {
                                price3 = prices3.get(0).getText().toString();
                            }
                        }

                        priceList.add(new TypeEntity(title, price2, price3));
                    }
                }
                index++;
                sleep(DEFAULT_SCROLL_SLEEP);
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) && index <= maxIndex);

            ArrayList<TypeEntity> finalList = new ArrayList<>();
            // 排重
            HashMap<String, TypeEntity> map = new HashMap<>();
            for (TypeEntity worth : priceList) {
                if (!map.containsKey(worth.getPrice1())) {
                    map.put(worth.getPrice1(), worth);
                    finalList.add(worth);
                } else {
                    TypeEntity old = map.get(worth.getPrice1());
                    if ((old.getPrice2() != null && !old.getPrice2().equals(worth.getPrice2()))
                            || (old.getPrice3() != null && !old.getPrice3().equals(worth.getPrice3()))) {
                        finalList.add(worth);
                    }
                }
            }
            BusHandler.getInstance().mTypePrices = finalList;
            BusHandler.getInstance().mTypeSheet = new TypeSheet();
            return true;
        }

        return false;
    }

    private boolean typeSelect(int scrollCount) {
        List<AccessibilityNodeInfo> nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list");
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false;
        AccessibilityNodeInfo list = nodes.get(0);

        ArrayList<TypeEntity> arrayList = BusHandler.getInstance().mTypePrices;

        if (list != null && !arrayList.isEmpty()) {
            int index = 0;
            // 最多滑几屏
            int maxIndex = scrollCount;
            if (maxIndex < 0) {
                maxIndex = 100;
            }
            do {
                // 滑回顶部
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD));

            do {
                TypeEntity entity = arrayList.get(0);

                List<AccessibilityNodeInfo> selectNodes = list.findAccessibilityNodeInfosByText(entity.getPrice1());
                List<AccessibilityNodeInfo> price2Nodes = list.findAccessibilityNodeInfosByText(entity.getPrice2());
                List<AccessibilityNodeInfo> price3Nodes = list.findAccessibilityNodeInfosByText(entity.getPrice3());

                if (AccessibilityUtils.isNodesAvalibale(selectNodes) && AccessibilityUtils.isNodesAvalibale(price2Nodes) && AccessibilityUtils.isNodesAvalibale(price3Nodes)) {
                    for (AccessibilityNodeInfo price1 : selectNodes) {
                        AccessibilityNodeInfo parent1 = AccessibilityUtils.findParentClickable(price1);
                        for (AccessibilityNodeInfo price2 : price2Nodes) {
                            AccessibilityNodeInfo parent2 = AccessibilityUtils.findParentClickable(price2);
                            for (AccessibilityNodeInfo price3 : price3Nodes) {
                                AccessibilityNodeInfo parent3 = AccessibilityUtils.findParentClickable(price3);
                                if (parent1 != null && parent2 != null && parent3 != null) {
                                    Rect rect1 = new Rect();
                                    Rect rect2 = new Rect();
                                    Rect rect3 = new Rect();
                                    parent1.getBoundsInParent(rect1);
                                    parent2.getBoundsInParent(rect2);
                                    parent3.getBoundsInParent(rect3);
                                    if (rect1.left == rect2.left && rect1.left == rect3.left
                                            && rect1.right == rect2.right && rect1.right == rect3.right
                                            && rect1.top == rect2.top && rect1.top == rect3.top) {
                                        if (BusHandler.getInstance().mTypeSheet != null) {
                                            BusHandler.getInstance().mTypeSheet.writeToSheetAppend("");
                                        }
                                        boolean result = parent1.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                        if (result) {
                                            arrayList.remove(0);
                                        }
                                        return result;
                                    }
                                }
                            }
                        }
                    }
                }
                index++;
                sleep(DEFAULT_SCROLL_SLEEP);
            } while (!arrayList.isEmpty() && list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) && index <= maxIndex);
        }
        return arrayList.isEmpty();
    }

    private boolean typeDetail(int scrollCount) {
        List<AccessibilityNodeInfo> nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list");
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false;
        AccessibilityNodeInfo list = nodes.get(0);
        if (list != null) {
            int index = 0;
            // 最多滑几屏
            int maxIndex = scrollCount;
            if (maxIndex < 0) {
                maxIndex = 100;
            }

            ArrayList<BrandDetail> detailList = new ArrayList<>();
            do {
                List<AccessibilityNodeInfo> titles = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/limit_buy_product_item_name");
                if (AccessibilityUtils.isNodesAvalibale(titles)) {
                    for (AccessibilityNodeInfo titleNode : titles) {
                        AccessibilityNodeInfo parent = titleNode.getParent();
                        if (parent != null) {
                            String title = null;
                            if (titleNode.getText() != null) {
                                title = titleNode.getText().toString();
                            }

                            List<AccessibilityNodeInfo> prices = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_miaosha_price");
                            String price = null;
                            if (AccessibilityUtils.isNodesAvalibale(prices)) {
                                if (prices.get(0).getText() != null) {
                                    price = prices.get(0).getText().toString();
                                }
                            }

                            List<AccessibilityNodeInfo> originPrices = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_jd_price");
                            String origin = null;
                            if (AccessibilityUtils.isNodesAvalibale(originPrices)) {
                                if (originPrices.get(0).getText() != null) {
                                    origin = originPrices.get(0).getText().toString();
                                }
                            }
                            detailList.add(new BrandDetail(title, price, origin));
                        }
                    }
                }

                index++;
                sleep(DEFAULT_SCROLL_SLEEP);
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) && index <= maxIndex);

            ArrayList<BrandDetail> finalList = new ArrayList<>();
            // 排重
            HashMap<String, BrandDetail> map = new HashMap<>();
            for (BrandDetail worth : detailList) {
                if (!map.containsKey(worth.getTitle())) {
                    map.put(worth.getTitle(), worth);
                    finalList.add(worth);
                } else {
                    BrandDetail old = map.get(worth.getTitle());
                    if ((old.getPrice() != null && !old.getPrice().equals(worth.getPrice()))
                            || (old.getOrigin_price() != null && !old.getOrigin_price().equals(worth.getOrigin_price()))) {
                        finalList.add(worth);
                    }
                }
            }

            BusHandler.getInstance().mTypeSheet.writeToSheetAppend("");
            BusHandler.getInstance().mTypeSheet.writeToSheetAppend("产品", "价格", "原价");
            for (BrandDetail detail : finalList) {
                BusHandler.getInstance().mTypeSheet.writeToSheetAppend(detail.getTitle(), detail.getPrice(), detail.getOrigin_price());
            }
            return true;
        }
        return false;
    }

    private boolean findHomeTextClick(String text) {
        List<AccessibilityNodeInfo> nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list");
        if (nodes == null) return false;
        for (AccessibilityNodeInfo node : nodes) {
            int index = 0;
            do {
                List<AccessibilityNodeInfo> leader = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, text);
                if (AccessibilityUtils.isNodesAvalibale(leader)) {
                    AccessibilityNodeInfo parent = AccessibilityUtils.findParentClickable(leader.get(0));
                    if (parent != null) {
                        return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }
                index++;
                sleep(DEFAULT_SCROLL_SLEEP);
            } while (node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) && index < 10);
        }
        return false;
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
