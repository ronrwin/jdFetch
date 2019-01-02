package com.example.jddata.action

import android.graphics.Rect
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.RowData
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.BaseLogFile
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.LogUtil
import java.util.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.indices

class FetchLeaderboardAction : BaseAction(ActionType.FETCH_LEADERBOARD) {

    var tabIndex = 0
    var tabTitles = ArrayList<String>()
    init {
        appendCommand(Command(ServiceCommand.LEADERBOARD).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.LEADERBOARD_TAB).addScene(AccService.NATIVE_COMMON).delay(20000L).concernResult(true))
    }

    override fun initLogFile() {
        logFile = BaseLogFile("获取_" + GlobalInfo.LEADERBOARD)
    }

    // 排行榜的页面比较特别，控件都是没有id的，只能根据固定的序号来判断了。
    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.LEADERBOARD_TAB -> {
                val result = leaderBoardTab()
                return result
            }
            ServiceCommand.LEADERBOARD -> {
                logFile?.writeToFileAppendWithTime("找到并点击 ${GlobalInfo.LEADERBOARD}")
                return CommonConmmand.findHomeTextClick(mService!!, GlobalInfo.LEADERBOARD)
            }
            ServiceCommand.LEADERBOARD_SELECT_TYPE -> {
                return selectLeaderboardTab()
            }
            ServiceCommand.LEADERBOARD_CONTENT -> {
                return leaderboardContent()
            }
        }
        return super.executeInner(command)
    }

    private fun selectLeaderboardTab(): Boolean {
        var root = mService!!.getRootInActiveWindow()
        if (root != null && tabTitles.size > tabIndex) {
            val tabString = tabTitles[tabIndex]
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
                            if (tab.text != null && tabString.equals(tab.text.toString())) {
                                tab.getBoundsInScreen(rect)
                                // 点击标签
                                ExecUtils.handleExecCommand("input tap ${rect.left+3} ${rect.top+3}")
                                logFile?.writeToFileAppendWithTime("\n点击标签 $tabString")
                                // 等待3秒
                                Thread.sleep(3000L)
                                return true
                            }
                        }
                    }
                }
            }
        }

        return false
    }

    private fun leaderboardContent(): Boolean {
        val root = mService!!.getRootInActiveWindow()
        // 开始获取内容
        val nodes = AccessibilityUtils.findChildByClassname(root, "android.widget.ScrollView")
        var cardCount = 0
        if (AccessibilityUtils.isNodesAvalibale(nodes) && nodes.size > tabIndex) {
            val scroll = nodes[tabIndex]
            tabIndex++
            val textNodes = AccessibilityUtils.findChildByClassname(scroll, "android.widget.TextView")
            if (AccessibilityUtils.isNodesAvalibale(textNodes)) {
                for (textNode in textNodes) {
                    if (textNode.text != null && "¥".equals(textNode.text.toString())) {
                        val parent = textNode.parent
                        val contentNodes = AccessibilityUtils.findChildByClassname(parent, "android.widget.TextView")
                        var title = ""
                        var price = ""
                        one@for (i in contentNodes.indices) {
                            val contentNode = contentNodes[i]
                            if (contentNode.text != null) {
                                if (contentNode.text.toString().length > 30) {
                                    title = contentNode.text.toString()
                                } else {
                                    if ("¥".equals(contentNode.text.toString())) {
                                        price = contentNode.text.toString() + contentNodes[i+1].text.toString()
                                        logFile?.writeToFileAppendWithTime(title, price)
                                        cardCount++
                                        break@one
                                    }
                                }
                            }
                        }
                        // 只取3个卡片内容
                        if (cardCount > 2) {
                            return true
                        }
                    }
                }
            }
        }
        return false
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
                    fetchCount++
                    if (itemCount >= GlobalInfo.FETCH_NUM) {
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