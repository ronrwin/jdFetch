package com.example.jddata.Entity

import android.text.method.Touch
import org.json.JSONObject

data class Data1(var arg1 : String?)
data class Data2(var arg1 : String?, var arg2 : String?)
data class Data3(var arg1 : String?, var arg2 : String?, var arg3 : String?)
data class Data4(var arg1 : String?, var arg2 : String?, var arg3 : String?, var arg4 : String?)
data class NiceBuyCard(var title : String?, var subTitle : String?, var fromWhere : String?, var viewdNum : String?,
                       var likeNum : String?, var num : String?)

class Route {
    var id = 0
    var keywords = ArrayList<String>()
}

class EnvActions {
    val days = ArrayList<ArrayList<Route>>()

    fun parseJson(json: JSONObject) {
        for (i in 0 until 7) {
            val array = json.optJSONArray("Day${i+1}")
            val routes = ArrayList<Route>()
            val size = array.length()
            for (j in 0 until size) {
                val routeJson = array.optJSONObject(j)
                val route = Route()
                route.id = routeJson.optInt("Route")
                val keywords = routeJson.optString("keywords")
                val keys = keywords.split(",")
                for (key in keys) {
                    route.keywords.add(key)
                }

                routes.add(route)
            }

            days.add(routes)
        }
    }
}

class MessageDef {
    companion object {
        @JvmField val MSG_TIME_OUT : Int = 1
        @JvmField val SUCCESS : Int = 2
        @JvmField val FAIL : Int = 3
    }
}

class ActionType {
    companion object {
//        // 动作组
//        @JvmField val MOVE_SEARCH = "move_search"   // 动作：搜索
//        @JvmField val MOVE_SEARCH_AND_CLICK = "move_search_and_click"   // 搜索并点击
//        @JvmField val MOVE_SEARCH_CLICK_AND_SHOP = "move_search_click_and_shop"   // 搜索点击加购
//        @JvmField val MOVE_BRAND_KILL_CLICK = "move_brand_kill_click"   // 品牌秒杀并点击商品
//        @JvmField val MOVE_BRAND_KILL_AND_SHOP = "move_brand_kill_and_shop"   // 品牌秒杀加购
//        @JvmField val MOVE_DMP = "move_dmp"       // DMP广告
//        @JvmField val MOVE_DMP_CLICK_SHOP = "move_dmp_click_shop"       // DMP广告加购
//        @JvmField val MOVE_DMP_CLICK = "move_dmp_click"       // DMP广告点击某商品
//        @JvmField val MOVE_JD_KILL_CLICK = "move_jd_kill_click"   // 京东秒杀并点击商品
//        @JvmField val MOVE_JD_KILL_AND_SHOP = "move_jd_kill_and_shop"   // 京东秒杀加购
//        @JvmField val MOVE_JD_KILL_REMIND = "move_jd_kill_remind"
//        @JvmField val MOVE_SCAN_PRODUCT = "move_scan_product"
//        @JvmField val MOVE_SCAN_PRODUCT_BUY = "move_scan_product_buy"

        // bi点位
        @JvmField val FETCH_CART = "fetch_cart"       // 购物车
        @JvmField val FETCH_SEARCH = "fetch_search"       // 搜索
        @JvmField val FETCH_HOME = "fetch_home"       // 首页
        @JvmField val FETCH_BRAND_KILL = "fetch_brand_kill"   // 品牌秒杀
        @JvmField val FETCH_TYPE_KILL = "fetch_type_kill"     // 品类秒杀
        @JvmField val FETCH_LEADERBOARD = "fetch_leaderboard"     // 排行榜
        @JvmField val FETCH_JD_KILL = "fetch_jd_kill"       // 京东秒杀
        @JvmField val FETCH_NICE_BUY = "fetch_nice_buy"       // 会买专辑
        @JvmField val FETCH_WORTH_BUY = "fetch_worth_buy"       // 发现好货
        @JvmField val FETCH_MY = "fetch_my"       // 我的
        @JvmField val FETCH_DMP = "fetch_dmp"       // dmp
        @JvmField val FETCH_ALL = "fetch_all"   // 采集数据

        @JvmField val JD_MARKET = "jd_market"   // 京东超市
        @JvmField val JD_FRESH = "jd_fresh"   // 京东生鲜
        @JvmField val JD_ACCESS_HOME = "jd_access_home"   // 京东到家
        @JvmField val JD_NUT = "jd_nut"   // 领京豆
        @JvmField val FLASH_BUY = "flash_buy"   // 闪购
        @JvmField val COUPON = "voucher"   // 领券

        @JvmField val PLUS = "plus"   // Plus会员
        @JvmField val TEMPLATE_MOVE = "template_move"   // 模板动作

        @JvmField val MOVE_SEARCH = "move_search"   // 0：搜索
        @JvmField val MOVE_SEARCH_HAIFEISI_CLICK = "move_search_haifeisi_click"   // 3： 搜索海飞丝
        @JvmField val MOVE_SEARCH_CLICK = "move_search_click"   // 1： 搜索点击
        @JvmField val MOVE_SEARCH_CLICK_BUY = "move_search_click_buy"   // 2： 搜索点击，加购
        @JvmField val MOVE_DMP_QRCODE = "move_dmp_qrcode"   // 4，  扫二维码活动
        @JvmField val MOVE_DMP_QRCODE_CLICK = "move_dmp_qrcode_click"   // 5，  扫二维码活动，点击商品
        @JvmField val MOVE_DMP_QRCODE_CLICK_BUY = "move_dmp_qrcode_click_buy"   // 6，  扫二维码活动，点击商品，加购
        @JvmField val MOVE_JD_KILL_CLICK = "move_jd_kill_click"   //7， 京东秒杀，点击
        @JvmField val MOVE_JD_KILL_CLICK_BUY = "move_jd_kill_click_buy"   //8，  京东秒杀，点击，加购
        @JvmField val MOVE_JD_KILL_REMIND = "move_jd_kill_remind"   // 9，京东秒杀，下一场次，提醒我
        @JvmField val MOVE_JD_KILL_WORTH = "move_jd_kill_worth"   // 10，京东秒杀，超值秒拼
        @JvmField val MOVE_JD_KILL_SALE_OUT = "move_jd_kill_sale_out"   // 11，京东秒杀，即将售罄
    }
}


