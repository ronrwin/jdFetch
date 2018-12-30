package com.example.jddata.Entity

import android.view.accessibility.AccessibilityNodeInfo

/**
 * 购物车商品
 */
data class CartGoods(var title : String?, var price : String?, var num : String?)

/**
 * 品牌秒杀商品
 */
data class BrandDetail(var title : String?, var price : String?, var origin_price : String?)

/**
 * 品牌秒杀商品大类
 */
data class BrandEntity(var title : String?, var subtitle : String?)

/**
 * 京东秒杀商品
 */
data class MiaoshaRecommend(var title : String?, var price : String?, var miaoshaPrice : String?)

/**
 * 会买专辑商品
 */
data class NiceBuyDetail(var title : String?, var price : String?, var origin_price : String?)
/**
 * 会买专辑大类
 */
data class NiceBuyEntity(var title : String?, var desc : String?, var pageView : String?, var collect : String?)
/**
 * 推荐商品：首页推荐、购物车推荐
 */
data class Recommend(var title : String?, var price : String?)
/**
 * 推荐商品：首页推荐、购物车推荐
 */
data class HomeRecommend(var title : String?, var price : String?)

/**
 * 搜索结果页推荐商品
 */
data class SearchRecommend(var title : String?, var price : String?, var comment : String?, var likePercent : String?)
/**
 * 品类秒杀大类
 */
data class TypeEntity(var price1 : String?, var price2 : String?, var price3 : String?)
/**
 * 发现好货
 */
data class WorthBuyEntity(var title : String?, var desc : String?, var collect : String?)

class MessageDef {
    companion object {
        @JvmField val MSG_TIME_OUT : Int = 1
        @JvmField val SUCCESS : Int = 2
        @JvmField val FAIL : Int = 3
        @JvmField val TASK_END : Int = 4
    }
}

class ActionType {
    companion object {
        // 动作组
        @JvmField val MOVE_SEARCH = "move_search"   // 动作：搜索
        @JvmField val MOVE_SEARCH_AND_CLICK = "move_search_and_click"   // 搜索并点击
        @JvmField val MOVE_SEARCH_CLICK_AND_SHOP = "move_search_click_and_shop"   // 搜索点击加购
        @JvmField val MOVE_BRAND_KILL_CLICK = "move_brand_kill_click"   // 品牌秒杀并点击商品
        @JvmField val MOVE_BRAND_KILL_AND_SHOP = "move_brand_kill_and_shop"   // 品牌秒杀加购
        @JvmField val MOVE_DMP = "move_dmp"       // DMP广告
        @JvmField val MOVE_DMP_CLICK_SHOP = "move_dmp_click_shop"       // DMP广告加购
        @JvmField val MOVE_DMP_CLICK = "move_dmp_click"       // DMP广告点击某商品
        @JvmField val MOVE_JD_KILL_CLICK = "move_jd_kill_click"   // 京东秒杀并点击商品
        @JvmField val MOVE_JD_KILL_AND_SHOP = "move_jd_kill_and_shop"   // 京东秒杀加购
        @JvmField val MOVE_JD_KILL_REMIND = "move_jd_kill_remind"
        @JvmField val MOVE_SCAN_PRODUCT = "move_scan_product"
        @JvmField val MOVE_SCAN_PRODUCT_BUY = "move_scan_product_buy"

        // 9个bi点位
        @JvmField val FETCH_SEARCH = "fetch_search"   // 搜索
        @JvmField val FETCH_CART = "fetch_cart"       // 购物车
        @JvmField val FETCH_HOME = "fetch_home"       // 首页
        @JvmField val FETCH_BRAND_KILL = "fetch_brand_kill"   // 品牌秒杀
        @JvmField val FETCH_TYPE_KILL = "fetch_type_kill"     // 品类秒杀
        @JvmField val FETCH_LEADERBOARD = "fetch_leaderboard"     // 排行榜
        @JvmField val FETCH_JD_KILL = "fetch_jd_kill"       // 京东秒杀
        @JvmField val FETCH_NICE_BUY = "fetch_nice_buy"       // 会买专辑
        @JvmField val FETCH_WORTH_BUY = "fetch_worth_buy"       // 发现好货
    }
}


