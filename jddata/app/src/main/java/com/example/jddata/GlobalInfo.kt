package com.example.jddata

import com.example.jddata.shelldroid.Env
import com.example.jddata.shelldroid.EnvManager
import com.example.jddata.shelldroid.Location
import java.util.ArrayList
import java.util.HashMap

class GlobalInfo {
    companion object {
        @JvmField val DEFAULT_COMMAND_INTERVAL = 2000L
        @JvmField val DEFAULT_SCROLL_SLEEP = 100L
        @JvmField val SCROLL_COUNT = 2

        @JvmField var envs = EnvManager.scanEnvs()
        @JvmField var currentEnv: Env? = null

        @JvmField val LOCATION_FILE = "location"
        // 单个测试开关
        @JvmField var sIsTest = true
        // 一键启动开关
        @JvmField var sIsOneKey = false

        @JvmField var sTargetEnvName: String? = null

        var actionMapList = ArrayList<HashMap<Int, String>>()

        @JvmField var sSelectLocation: Location? = null
        @JvmField var sLocations = arrayOf(
                Location("广州", 113.23, 23.16),
                Location("上海", 121.48, 31.22),
                Location("昆明", 102.73, 25.04),
                Location("呼和浩特", 111.65, 40.82),
                Location("北京", 116.46, 39.92),
                Location("成都", 104.06, 30.67),
                Location("长春", 125.35, 43.88),
                Location("合肥", 117.27, 31.86),
                Location("济南", 117.0, 36.65),
                Location("太原", 112.53, 37.87),
                Location("南宁", 108.33, 22.84),
                Location("乌鲁木齐", 87.68, 43.77),
                Location("南京", 118.78, 32.04),
                Location("南昌", 115.89, 28.68),
                Location("石家庄", 114.48, 38.03),
                Location("郑州", 113.65, 34.76),
                Location("杭州", 120.19, 30.26),
                Location("海口", 110.35, 20.02),
                Location("武汉", 114.31, 30.52),
                Location("长沙", 113.0, 28.21),
                Location("兰州", 103.73, 36.03),
                Location("福州", 119.3, 26.08),
                Location("拉萨", 91.11, 29.97),
                Location("贵阳", 106.71, 26.57),
                Location("沈阳", 123.38, 41.8),
                Location("重庆", 106.54, 29.59),
                Location("西安", 108.95, 34.27),
                Location("哈尔滨", 126.63, 45.75),
                Location("香港", 114.1, 22.2),
                Location("澳门", 113.33, 22.13))
    }
}