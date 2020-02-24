package com.example.jddata

import com.example.jddata.util.FileUtils
import com.example.jddata.util.LogUtil
import com.example.jddata.util.StringUtils
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class GlobalInfo {
    companion object {
        @JvmField var width = 0
        @JvmField var height = 0

        const val DEFAULT_COMMAND_INTERVAL = 800L
        const val DEFAULT_SCROLL_SLEEP = 200L
        const val DEFAULT_SCROLL_SLEEP_WAIT = 1000L
        const val SCROLL_COUNT = 30
        const val FETCH_NUM = 20  // 抓多少个商品

        const val BRAND_KILL_COUNT = 4
        const val GOOD_SHOP_COUNT = 1
        const val GOOD_SHOP_COUNT_SECOND = 2
        const val TYPE_KILL_COUNT = 5  // 测试 原本要20个
        const val WORTH_BUY_COUNT = 3
        const val NICE_BUY_COUNT = 2
        const val DMP_COUNT = 8
        const val LEADERBOARD_COUNT = 5

        const val LEADERBOARD_TAB_COUNT = 10 // 抓多少个tab
        const val BRAND_KILL_TAB = 5
        const val NICE_BUY_TAB = 11
        const val WORTH_BUY_TAB = 6
        const val GOOD_SHOP_TAB = 15

        const val EXTRA = "extra"

        // 是否原始数据
        @JvmField var sIsOrigin = false

        @JvmField var CURRENT_SCENE  =  "currentScene"

        const val TODAY_DO_ACTION = "todayDoAction"

        const val SEARCH_KEY = "keyword"
        const val HOME_GRID_NAME = "gridName"
        const val HOME_CARD_NAME = "cardName"

        const val NO_MORE_DATA = "没有更多数据"

        @JvmField val sLocations = arrayOf(
                Location("广州", 113.2688, 23.11462),
                Location("上海", 121.4737, 31.23037),
                Location("北京", 116.40717, 39.90469),
                Location("成都", 104.06476, 30.5702),
                Location("沈阳", 123.4631, 41.67718),
                Location("西安", 108.93984, 34.34127),
                Location("安顺", 105.9462, 26.25367),
                Location("湛江", 110.35894, 21.27134)
        )
        const val TABLE_NAME = "jdData"

        const val JD_KILL = "京东秒杀"
        const val BRAND_KILL = "品牌秒杀"
        const val LEADERBOARD = "排行榜"
        const val HOME = "首页推荐"
        const val CART = "购物车"
        const val MY = "我的"
        const val TYPE_KILL = "品类秒杀"
        const val WORTH_BUY = "发现好货"
        const val NICE_BUY = "会买专辑"
        const val DMP = "DMP点位"
        const val SEARCH = "搜索结果"
        const val GOOD_SHOP = "逛好店"

        const val JD_MARKET = "京东超市"
        const val JD_FRESH = "京东生鲜"
        const val JD_ACCESS_HOME = "京东到家"
        const val JD_NUT = "领京豆"
        const val FLASH_BUY = "闪购"
        const val COUPON = "领券"
        const val PLUS = "Plus会员"


        const val WORTH_PING = "超值秒拼"
        const val SALE_OUT = "即将售罄"


        @JvmStatic fun getLocationId(location: String): String? {
            var map = HashMap<String, String>()
            map.put("广州", "GZ")
            map.put("上海", "SH")
            map.put("成都", "CD")
            map.put("北京", "BJ")
            map.put("沈阳", "SY")
            map.put("安顺", "AS")
            map.put("湛江", "ZJ")
            map.put("西安", "XA")
            return map[location]
        }

        @JvmStatic fun getProvice(location: String): String? {
            var map = HashMap<String, String>()
            map.put("广州", "广东")
            map.put("北京", "北京")
            map.put("上海", "上海")
            map.put("成都", "四川")
            map.put("沈阳", "辽宁")
            map.put("安顺", "贵州")
            map.put("湛江", "广东")
            map.put("西安", "陕西")
            return map[location]
        }

        const val SEARCH_RESULT_SCROLL = "SEARCH_RESULT_SCROLL"
        const val MIAOSHA_TAB = "MIAOSHA_TAB"
        const val TEMPLATE_SEARCH_INDEX = "TEMPLATE_SEARCH_INDEX"
        const val LIMIT = "LIMIT"
        const val ROUTE = "ROUTE"
        const val HAS_DONE_FETCH_SEARCH = "HAS_DONE_FETCH_SEARCH"


        const val HC01 = "pic.png"
        const val HC02 = "hc02.png"
        const val TMP01 = "tmp01.png"
        const val TMP02 = "tmp02.png"

        const val QRCODE_PIC = "QRCODE_PIC"


        @JvmStatic fun generateClient() {
            MainApplication.sExecutor.execute {
                val array = JSONArray()
                val map = HashMap<String, Int>()
                var i = 0
                val set = HashSet<Int>()
                for (j in 0..10) {
                    set.clear()
                    for (k in 0 until 36) {
                        val json = JSONObject()
                        json.put("id", "${i + 1}")
                        var locationx = sLocations[k % 8]
                        if (k > 31) {
                            var num = Random().nextInt(8)
                            while (!set.add(num)) {
                                num = Random().nextInt(8)
                            }
                            locationx = sLocations[num]
                        }
                        val moveId = j
                        json.put("locationName", locationx.name)
                        json.put("longitude", locationx.longitude)
                        json.put("latitude", locationx.latitude)
                        val cityNo = getLocationId(locationx.name)
                        json.put("move", "${moveId}")
                        json.put("imei", "865124" + StringUtils.getNumRandomString(9))
                        json.put("createTime", System.currentTimeMillis())
                        val idStr = String.format("%03d", i+1)
                        val moveStr= String.format("%02d", moveId)
                        val key = cityNo + moveStr
                        if (map.containsKey(key)) {
                            var no = map[key] as Int
                            map.put(key, no + 1)
                        } else {
                            map.put(key, 0)
                        }
                        val name = key + map[key]
                        json.put("name", String.format("%s-%s-%s-%s", idStr, moveStr, cityNo, map[key]))
                        array.put(i, json)
                        i++
                    }
                }
                val str = array.toString()
                FileUtils.writeToFile(LogUtil.EXTERNAL_FILE_FOLDER, "account5.json", str, false)
            }
        }
    }
}