package com.example.jddata.Entity

/**
 * 购物车商品
 */
data class CartGoods(var title : String, var price : String, var num : String)

/**
 * 品牌秒杀商品
 */
data class BrandDetail(var title : String, var price : String, var origin_price : String)

/**
 * 品牌秒杀商品大类
 */
data class BrandEntity(var title : String, var subtitle : String)

/**
 * 京东秒杀商品
 */
data class MiaoshaRecommend(var title : String, var price : String, var miaoshaPrice : String)

/**
 * 会买专辑商品
 */
data class NiceBuyDetail(var title : String, var price : String, var origin_price : String)
/**
 * 会买专辑大类
 */
data class NiceBuyEntity(var title : String, var desc : String, var pageView : String, var collect : String)
/**
 * 推荐商品：首页推荐、购物车推荐
 */
data class Recommend(var title : String, var price : String)
/**
 * 搜索结果页推荐商品
 */
data class SearchRecommend(var title : String, var price : String, var comment : String, var likePercent : String)
/**
 * 品类秒杀大类
 */
data class TypeEntity(var price1 : String, var price2 : String, var price3 : String)
/**
 * 发现好货
 */
data class WorthBuyEntity(var title : String, var desc : String, var collect : String)

class MessageDef {
    companion object {
        @JvmField val MSG_TIME_OUT : Int = 1
        @JvmField val SUCCESS : Int = 2
    }
}

class ActionType {
    companion object {
        @JvmField val SEARCH = "search"   // 搜索
        @JvmField val SEARCH_AND_SHOP = "search_and_shop"   // 搜索加购
        @JvmField val CART = "cart"       // 购物车
        @JvmField val HOME = "home"       // 首页
        @JvmField val BRAND_KILL = "brand_kill"   // 品牌秒杀
        @JvmField val BRAND_KILL_AND_SHOP = "brand_kill_and_shop"   // 品牌秒杀加购
        @JvmField val TYPE_KILL = "type_kill"     // 品类秒杀
        @JvmField val LEADERBOARD = "leaderboard"     // 排行榜
        @JvmField val JD_KILL = "jd_kill"       // 京东秒杀
        @JvmField val WORTH_BUY = "worth_buy"       // 发现好货
        @JvmField val NICE_BUY = "nice_buy"       // 会买专辑
        @JvmField val DMP = "dmp"       // DMP广告
        @JvmField val DMP_AND_SHOP = "dmp_and_shop"       // DMP广告加购
    }
}


