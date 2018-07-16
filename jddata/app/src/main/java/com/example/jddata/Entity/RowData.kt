package com.example.jddata.Entity

import android.text.TextUtils
import com.example.jddata.GlobalInfo
import com.example.jddata.shelldroid.EnvManager
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.SharedPreferenceHelper
import org.jetbrains.anko.db.MapRowParser
import java.text.SimpleDateFormat

class RowData(val map: MutableMap<String, Any?>) {

    var id: Int by map
    var deviceId: String? by map            // 账号编号
    var moveId: String? by map              // 动作组编号
    var date: String? by map
    var createTime: String? by map
    var mobile: String? by map     // 手机号
    var location: String? by map   // gps位置
    var wifiLocation: String? by map   // wifi位置
    var moveInterval: String? by map              // 动作组编号
    var biId: String? by map   // 动作组
    var itemIndex: String? by map // 滑到第几屏
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
    }

    fun setDefaultData() {
        this.moveId = GlobalInfo.moveId
        this.date = ExecUtils.getCurrentTimeString(SimpleDateFormat("MM-dd"))
        this.createTime = ExecUtils.getCurrentTimeString(SimpleDateFormat("HH:mm:ss:SSS"))
        this.moveInterval = GlobalInfo.MOVE_INTERVAL.toString()
        if (!TextUtils.isEmpty(GlobalInfo.sTargetEnvName)) {
            this.mobile = GlobalInfo.sTargetEnvName
        } else {
            if (EnvManager.sCurrentEnv != null) {
                this.mobile = EnvManager.sCurrentEnv.envName
            } else {
                this.mobile = "0"
            }
        }
        this.location = GlobalInfo.sSelectLocation.name
        val wifi = SharedPreferenceHelper.getInstance().getValue(WIFI_LOCATION)
        this.wifiLocation = if (!TextUtils.isEmpty(wifi)) wifi else GlobalInfo.sSelectLocation.name

        val ipLocation = GlobalInfo.getIPLocationId(wifiLocation!!)
        if (!TextUtils.isEmpty(mobile)) {
            this.deviceId = "${GlobalInfo.getLocationId(location!!)}${ipLocation}${String.format("%02d", moveId!!.toInt())}${String.format("%02d", mobile!!.toInt())}"
        }
    }

    companion object {
        @JvmField val ID = "id"
        @JvmField val DEVICE_ID = "deviceId"
        @JvmField val MOVE_ID = "moveId"
        @JvmField val DATE = "date"
        @JvmField val CREATE_TIME = "createTime"
        @JvmField val MOBILE = "mobile"
        @JvmField val LOCATION = "location"
        @JvmField val WIFI_LOCATION = "wifiLocation"
        @JvmField val MOVE_INTERVAL = "moveInterval"
        @JvmField val BI_ID = "biId"
        @JvmField val ITEM_INDEX = "itemIndex"
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
        sb.append("$deviceId,$moveId,$date,$createTime," +
                "$mobile,$location,$wifiLocation,$moveInterval,$biId,$itemIndex," +
                "$title,$subtitle,$product,$price,$originPrice,$description," +
                "$num,$leaderboardCity,$leaderboardTab,$markNum," +
                "$viewdNum,$comment,$goodFeedback,$jdKillRoundTime")

        val content = sb.toString().replace("null", "")
        return content
    }
}

class MyRowParser : MapRowParser<RowData> {
    override fun parseRow(columns: Map<String, Any?>): RowData {
        val rowData = RowData(columns as MutableMap<String, Any?>)
        return rowData
    }
}
