package com.example.jddata.Entity

import com.example.jddata.shelldroid.Env
import com.example.jddata.util.ExecUtils
import org.jetbrains.anko.db.MapRowParser
import java.text.SimpleDateFormat

class RowData(val map: MutableMap<String, Any?>) {

    var id: Int by map
    var deviceId: String? by map                    // 账号编号
    var deviceCreateTime: String? by map            // 账号创建时间
    var date: String? by map
    var imei: String? by map                        // imei
    var moveId: String? by map                      // 动作组编号
    var createTime: String? by map                  // 商品记录创建时间
    var location: String? by map                    // gps位置
    var biId: String? by map                        // 动作组
    var itemIndex: String? by map                   // 商品下标
    var title: String? by map                       // 标题
    var subtitle: String? by map                    // 副标题
    var product: String? by map                     // 产品
    var sku: String? by map                         // 产品sku
    var price: String? by map                       // 价格、秒杀价
    var originPrice: String? by map                 // 原价
    var description: String? by map                 // 描述
    var num: String? by map                         // 数量
    var city: String? by map                        // 城市
    var tab: String? by map                         // 标签
    var markNum: String? by map                     // 收藏数
    var viewdNum: String? by map                    // 看过数
    var comment: String? by map                     // 评论
    var from: String? by map                        // 出处
    var goodFeedback: String? by map                // 好评
    var likeNum: String? by map                     // 喜欢数
    var salePercent: String? by map                 // 热卖指数
    var isSelfSale: String? by map                  // 是否自营
    var hasSalePercent: String? by map                     // 已售
    var jdKillRoundTime: String? by map             // 京东秒杀场次
    var brand: String? by map                       // brand
    var category: String? by map                    // category
    var isOrigin: String? by map                    // 是否原始数据

    init {
    }

    fun setDefaultData(env: Env) {
        deviceId = env.envName
        deviceCreateTime = env.createTime
        this.date = ExecUtils.getCurrentTimeString(SimpleDateFormat("MM-dd"))
        this.createTime = ExecUtils.getCurrentTimeString(SimpleDateFormat("HH:mm:ss:SSS"))
        location = env.locationName
        imei = env.imei
    }

    companion object {
        @JvmField val ID = "id"
        @JvmField val DEVICE_ID = "imei"
        @JvmField val DEVICE_CREATE_TIME = "deviceCreateTime"
        @JvmField val DATE = "date"
        @JvmField val IMEI = "imei"
        @JvmField val MOVE_ID = "moveId"
        @JvmField val CREATE_TIME = "createTime"
        @JvmField val LOCATION = "location"
        @JvmField val BI_ID = "biId"
        @JvmField val ITEM_INDEX = "itemIndex"
        @JvmField val TITLE = "title"
        @JvmField val SUBTITLE = "subtitle"
        @JvmField val PRODUCT = "product"
        @JvmField val SKU = "sku"
        @JvmField val PRICE = "price"
        @JvmField val ORIGIN_PRICE = "originPrice"
        @JvmField val DESCRIPTION = "description"
        @JvmField val NUM = "num"
        @JvmField val CITY = "city"
        @JvmField val TAB = "tab"
        @JvmField val MARK_NUM = "markNum"
        @JvmField val VIEW_NUM = "viewdNum"
        @JvmField val COMMENT = "comment"
        @JvmField val FROM = "from"
        @JvmField val GOOD_FEEDBACK = "goodFeedback"
        @JvmField val LIKE_NUM = "likeNum"
        @JvmField val SALE_PERCENT = "salePercent"
        @JvmField val IS_SELF_SALE = "isSelfSale"
        @JvmField val HAS_SALE_PERCENT = "hasSalePercent"
        @JvmField val JDKILL_ROUND_TIME = "jdKillRoundTime"
        @JvmField val BRAND = "brand"
        @JvmField val CATEGORY = "category"
        @JvmField val IS_ORIGIN = "isOrigin"
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("${deviceId}," +
                "${deviceCreateTime}," +
                "${date}," +
                "${imei}," +
                "${moveId}," +
                "${createTime}," +
                "${location}," +
                "${biId}," +
                "${itemIndex}," +
                "${title?.replace(",", "、")}," +
                "${subtitle?.replace(",", "、")}," +
                "${product?.replace(",", "、")}," +
                "${sku}," +
                "${price}," +
                "${originPrice}," +
                "${description?.replace(",", "、")}," +
                "${num}," +
                "${city}," +
                "${tab}," +
                "${markNum}," +
                "${viewdNum}," +
                "${comment?.replace(",", "、")}," +
                "${from?.replace(",", "、")}," +
                "${goodFeedback}," +
                "${likeNum}," +
                "${salePercent}," +
                "${isSelfSale}," +
                "${hasSalePercent}," +
                "${jdKillRoundTime}," +
                "${brand}," +
                "${category}," +
                "${isOrigin}")

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
