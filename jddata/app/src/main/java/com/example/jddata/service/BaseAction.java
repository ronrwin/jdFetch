package com.example.jddata.service;

public class BaseAction {
    public static final String SEARCH = "search";   // 搜索
    public static final String SEARCH_AND_SHOP = "search_and_shop";   // 搜索加购
    public static final String CART = "cart";       // 购物车
    public static final String HOME = "home";       // 首页
    public static final String BRAND_KILL = "brand_kill";   // 品牌秒杀
    public static final String BRAND_KILL_AND_SHOP = "brand_kill_and_shop";   // 品牌秒杀加购
    public static final String TYPE_KILL = "type_kill";     // 品类秒杀
    public static final String LEADERBOARD = "leaderboard";     // 排行榜
    public static final String JD_KILL = "jd_kill";       // 京东秒杀
    public static final String WORTH_BUY = "worth_buy";       // 发现好货
    public static final String NICE_BUY = "nice_buy";       // 会买专辑
    public static final String DMP = "dmp";       // DMP广告
    public static final String DMP_AND_SHOP = "dmp_and_shop";       // DMP广告加购


    public String actionType;
    public ActionMachine machine;

    public BaseAction(String actionType) {
        this.actionType = actionType;
        this.machine = new ActionMachine(actionType);
    }
}
