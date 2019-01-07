package com.example.jddata.action.fetch

import android.graphics.Rect
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.RowData
import com.example.jddata.GlobalInfo
import com.example.jddata.action.BaseAction
import com.example.jddata.action.Command
import com.example.jddata.action.append
import com.example.jddata.util.BaseLogFile
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.LogUtil
import java.util.ArrayList
import kotlin.collections.HashMap

class FetchLeaderboardAction : BaseAction(ActionType.FETCH_LEADERBOARD) {

    var tabTitles = ArrayList<String>()
    init {
        appendCommand(Command(ServiceCommand.FIND_TEXT).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.LEADERBOARD_TAB).addScene(AccService.NATIVE_COMMON).delay(20000L))
    }

    val name = GlobalInfo.LEADERBOARD
    override fun initLogFile() {
        logFile = BaseLogFile("获取_$name")
    }

    // 排行榜的页面比较特别，控件都是没有id的，只能根据固定的序号来判断了。
    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.LEADERBOARD_TAB -> {
                val result = leaderBoardTab()
                return result
            }
            ServiceCommand.FIND_TEXT -> {
                logFile?.writeToFileAppendWithTime("找到并点击 ${name}")
                return findHomeTextClick(name)
            }
        }
        return super.executeInner(command)
    }

    var currentCity = ""
    private fun leaderBoardTab(): Boolean {
        val root = mService!!.rootInActiveWindow
        if (root != null) {
            // 找第一个text，是当前城市。
            val citys = AccessibilityUtils.findChildByClassname(root, "android.widget.TextView")
            if (AccessibilityUtils.isNodesAvalibale(citys)) {
                val cityNode = citys[0]
                if (cityNode.text != null) {
                    val city = cityNode.text.toString()
                    logFile?.writeToFileAppendWithTime("城市")
                    logFile?.writeToFileAppendWithTime(city)
                    currentCity = city
                }
            }

            val scrolls = AccessibilityUtils.findChildByClassname(root, "android.widget.HorizontalScrollView")
            if (AccessibilityUtils.isNodesAvalibale(scrolls)) {
                for (scroll in scrolls) {
                    val rect = Rect()
                    scroll.getBoundsInScreen(rect)
                    if (rect.top < 0 || rect.left < 0 || rect.right < 0 || rect.bottom < 0
                            || rect.bottom > 170) {
                        continue
                    }
                    val tabNodes = AccessibilityUtils.findChildByClassname(scroll, "android.widget.TextView")
                    if (AccessibilityUtils.isNodesAvalibale(tabNodes)) {
                        for (tab in tabNodes!!) {
                            if (tab.text != null) {
                                tabTitles.add(tab.text.toString())
                            }
                        }
                    }
                }

                logFile?.writeToFileAppendWithTime("标签")
                for (i in tabTitles.indices) {
                    val title = tabTitles[i]
                    logFile?.writeToFileAppendWithTime(title)

                    val map = HashMap<String, Any?>()
                    val row = RowData(map)
                    row.setDefaultData()
                    row.leaderboardTab = ExecUtils.translate(title)
                    row.leaderboardCity = ExecUtils.translate(currentCity)
                    row.biId = GlobalInfo.LEADERBOARD
                    row.itemIndex = "${i+1}"
                    LogUtil.dataCache(row)

                    itemCount++
                    if (itemCount >= GlobalInfo.LEADERBOARD_COUNT) {
                        logFile?.writeToFileAppendWithTime(GlobalInfo.FETCH_ENOUGH_DATE)
                        return true
                    }
                }
                logFile?.writeToFileAppendWithTime("")
                return true
            }
        }
        return false
    }

}