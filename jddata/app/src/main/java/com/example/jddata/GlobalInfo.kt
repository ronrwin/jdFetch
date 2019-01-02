package com.example.jddata

import com.example.jddata.action.*
import com.example.jddata.shelldroid.Location
import java.util.HashMap

class GlobalInfo {
    companion object {
        @JvmField var width = 0
        @JvmField var height = 0

        @JvmField val DEFAULT_COMMAND_INTERVAL = 1000L
        @JvmField val DEFAULT_SCROLL_SLEEP = 100L
        @JvmField val DEFAULT_SCROLL_SLEEP_WAIT = 1000L
        @JvmField val SCROLL_COUNT = 10
        @JvmField val FETCH_NUM = 6

        @JvmField val MOVE_INTERVAL = 20

        @JvmField val BRAND_KILL_COUNT = 10
        @JvmField val BRAND_NICEBUY_COUNT = 10

        @JvmField var emulatorId = ""

        @JvmField val EXTRA = "extra"

        @JvmField val LOCATION_FILE = "location"
        // 单个测试开关
        @JvmField var sIsTest = true
        // 一键执行
        @JvmField var sOneKeyRun = false
        // 是否原始数据
        @JvmField var sIsOrigin = false

        @JvmField var singleActionType: String? = null
        @JvmField var taskid = 0            // 账号名称

        @JvmField var sTargetEnvName = ""

        @JvmField var sArgMap : HashMap<String, String>? = null

        @JvmField var COMPUTER_NUM  =  "computerNum"

        @JvmField val TODAY_DO_ACTION = "todayDoAction"

        @JvmField val NO_MORE_DATA = "没有更多数据"
        @JvmField val FETCH_ENOUGH_DATE = "采集够 ${GlobalInfo.FETCH_NUM} 条数据，结束"

        @JvmField var sLocations = arrayOf(
                Location("广州", 113.2688,23.11462),
                Location("上海", 121.4737,31.23037),
                Location("北京", 116.40717,39.90469),
                Location("成都", 104.06476,30.5702),
                Location("沈阳", 123.4631, 41.67718),
                Location("西安", 108.93984, 34.34127),
                Location("安顺", 105.9462, 26.25367),
                Location("湛江", 110.35894, 21.27134)
        )
        @JvmField var sSelectLocation = sLocations[0]

        @JvmField val TABLE_NAME = "jdData"

        @JvmField val JD_KILL = "京东秒杀"
        @JvmField val SEARCH = "搜索洗发水结果推荐"
        @JvmField val BRAND_KILL = "品牌秒杀"
        @JvmField val LEADERBOARD = "排行榜"
        @JvmField val HOME = "首页推荐"
        @JvmField val CART = "购物车"
        @JvmField val TYPE_KILL = "品类秒杀"
        @JvmField val WORTH_BUY = "发现好货"
        @JvmField val NICE_BUY = "会买专辑"

        @JvmField var commandAction = ArrayList<Action>()

        @JvmField var currentOneKeyIndex = 0


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

        @JvmStatic fun getIPLocationId(ipLocation: String): String? {
            var map = HashMap<String, String>()
            map.put("广州", "0")
            map.put("北京", "1")
            return map[ipLocation]
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

    }
}