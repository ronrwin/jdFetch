package com.example.jddata

import com.example.jddata.action.*
import com.example.jddata.shelldroid.Location
import java.util.HashMap

class GlobalInfo {
    companion object {
        @JvmField val DEFAULT_COMMAND_INTERVAL = 1500L
        @JvmField val DEFAULT_SCROLL_SLEEP = 100L
        @JvmField val SCROLL_COUNT = 100
        @JvmField val FETCH_NUM = 100

        @JvmField val LOCATION_FILE = "location"
        // 单个测试开关
        @JvmField var sIsTest = true
        // 一键执行
        @JvmField var sOneKeyRun = false
        // 自动一键执行
        @JvmField var sAutoFetch = false

        @JvmField var singleActionType: String? = null
        @JvmField var taskid = 0            // 账号下标

        @JvmField var sTargetEnvName = ""

        @JvmField var sArgMap : HashMap<String, String>? = null

        @JvmField val NO_MORE_DATA = "没有更多数据"
        @JvmField val FETCH_ENOUGH_DATE = "采集够 ${GlobalInfo.FETCH_NUM} 条数据，结束"

        @JvmField var sLocations = arrayOf(
                Location("广州", 113.23333,23.16667),
                Location("上海", 121.43333,34.50000),
                Location("昆明", 102.73333,25.05000),
                Location("呼和浩特", 111.65, 40.82),
                Location("北京", 116.41667,39.91667),
                Location("成都", 104.06667,30.66667),
                Location("长春", 125.35000,43.88333),
                Location("合肥", 117.17, 31.52),
                Location("济南", 117.0, 36.40),
                Location("太原", 112.53, 37.87),
                Location("南宁", 108.33, 22.84),
                Location("乌鲁木齐", 87.68, 43.77),
                Location("南京", 118.78, 32.04),
                Location("南昌", 115.89, 28.68),
                Location("石家庄", 114.48, 38.03),
                Location("郑州", 113.65, 34.76),
                Location("杭州", 120.20000,30.26667),
                Location("海口", 110.35, 20.02),
                Location("武汉", 114.31, 30.52),
                Location("长沙", 113.0, 28.21),
                Location("兰州", 103.73333,36.03333),
                Location("福州", 119.3, 26.08),
                Location("拉萨", 91.11, 29.97),
                Location("贵阳", 106.71667,26.56667),
                Location("沈阳", 123.38, 41.8),
                Location("重庆", 106.54, 29.59),
                Location("西安", 108.95, 34.27),
                Location("哈尔滨", 126.63333,45.75000),
                Location("香港", 114.1, 22.2),
                Location("澳门", 113.33, 22.13))
        @JvmField var sSelectLocation = sLocations[0]


        @JvmField val TABLE_NAME = "jdData"

        @JvmField val BRAND_KILL = "品牌秒杀"
        @JvmField val NICE_BUT = "会买专辑"
        @JvmField val HOME = "首页推荐"
        @JvmField val JD_KILL = "京东秒杀"
        @JvmField val LEADERBOARD = "排行榜"
        @JvmField val SEARCH = "搜索结果推荐"
        @JvmField val TYPE_KILL = "品类秒杀"
        @JvmField val WORTH_BUY = "发现好货"
        @JvmField val CART = "购物车"

        @JvmField var mCurrentAction: BaseAction? = null

        @JvmField var commandAction = ArrayList<Action>()

        @JvmField var currentOneKeyIndex = 0
    }
}