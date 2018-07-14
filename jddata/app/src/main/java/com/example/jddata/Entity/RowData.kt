package com.example.jddata.Entity

import android.text.TextUtils
import com.example.jddata.GlobalInfo
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.SharedPreferenceHelper
import org.jetbrains.anko.db.rowParser

class RowData(val map: MutableMap<String, Any?>) {

    var id: Int by map
    var createTimeMillis: String? by map
    var createTime: String? by map
    var mobile: String? by map     // 手机号
    var location: String? by map   // gps位置
    var wifiLocation: String? by map   // wifi位置
    var actionId: String? by map   // 动作组
    var scrollIndex: String? by map // 滑到第几屏
    var title: String? by map  // 标题
    var subtitle: String? by map  // 副标题
    var product: String? by map  // 产品
    var price: String? by map  // 价格、秒杀价
    var originPrice: String? by map    // 原价
    var description: String? by map    // 描述
    var num: String? by map        // 数量
    var leaderboardCity: String? by map    // 排行榜城市
    var leaderboardTab: String? by map// 排行榜标签
    var markNum: String? by map    // 收藏数
    var viewdNum: String? by map    // 看过数
    var comment: String? by map    // 评论
    var goodFeedback: String? by map    // 好评
    var jdKillRoundTime: String? by map    // 京东秒杀场次

    init {
        this.createTimeMillis = "${System.currentTimeMillis()}"
        this.createTime = ExecUtils.getCurrentTimeString()
        this.mobile = GlobalInfo.sTargetEnvName
        this.location = GlobalInfo.sSelectLocation.name
        val wifi = SharedPreferenceHelper.getInstance().getValue(WIFI_LOCATION)
        this.wifiLocation = if (!TextUtils.isEmpty(wifi)) wifi else GlobalInfo.sSelectLocation.name
        this.actionId = SharedPreferenceHelper.getInstance().getValue(ACTION_ID)
    }

    companion object {
        @JvmField val ID = "id"
        @JvmField val CREATE_MILLIS = "createTimeMillis"
        @JvmField val CREATE_TIME = "createTime"
        @JvmField val MOBILE = "mobile"
        @JvmField val LOCATION = "location"
        @JvmField val WIFI_LOCATION = "wifiLocation"
        @JvmField val ACTION_ID = "actionId"
        @JvmField val SCROLL_INDEX = "scrollIndex"
        @JvmField val TITLE = "title"
        @JvmField val SUBTITLE = "subtitle"
        @JvmField val PRODUCT = "product"
        @JvmField val PRICE = "price"
        @JvmField val ORIGIN_PRICE = "originPrice"
        @JvmField val DESCRIPTION = "description"
        @JvmField val NUM = "num"
        @JvmField val LEADERBOARD_CITY = "leaderboardCity"
        @JvmField val LEADERBOARD_TAB = "leaderboardTab"
        @JvmField val MARK_NUM = "markNum"
        @JvmField val VIEW_NUM = "viewdNum"
        @JvmField val COMMENT = "comment"
        @JvmField val GOOD_FEEDBACK = "goodFeedback"
        @JvmField val JDKILL_ROUND_TIME = "jdKillRoundTime"
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("$id,$createTimeMillis,$createTime," +
                "$mobile,$location,$wifiLocation,$actionId,$scrollIndex," +
                "$title,$subtitle,$product,$price,$originPrice,$description," +
                "$num,$leaderboardCity,$leaderboardTab,$markNum," +
                "$viewdNum,$comment,$goodFeedback,$jdKillRoundTime")

        return sb.toString()
    }
}

val parser = rowParser {
    id: Int,
    createTimeMillis: String?,
    createTime: String?,
    mobile: String?,
    location: String?,
    wifiLocation: String?,
    actionId: String?,
    scrollIndex: String?,
    title: String?,
    subtitle: String?,
    product: String?,
    price: String?,
    originPrice: String?,
    description: String?,
    num: String?,
    leaderboardCity: String?,
    leaderboardTab: String?,
    markNum: String?,
    viewdNum: String?,
    comment: String?,
    goodFeedback: String?,
    jdKillRoundTime: String?->
    {
        val map = HashMap<String, Any?>()
        val row = RowData(map)
        row.id = id
        row.createTimeMillis = createTimeMillis
        row.createTime = createTime
        row.mobile = mobile
        row.location = location
        row.wifiLocation = wifiLocation
        row.actionId = actionId
        row.scrollIndex = scrollIndex
        row.title = title
        row.subtitle = subtitle
        row.product = product
        row.price = price
        row.originPrice = originPrice
        row.description = description
        row.num = num
        row.leaderboardCity = leaderboardCity
        row.leaderboardTab = leaderboardTab
        row.markNum = markNum
        row.viewdNum = viewdNum
        row.comment = comment
        row.goodFeedback = goodFeedback
        row.jdKillRoundTime = jdKillRoundTime
         row
    }
}