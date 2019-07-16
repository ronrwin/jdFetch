package com.example.jddata.action.fetch

import android.graphics.Rect
import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo

import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.RowData
import com.example.jddata.GlobalInfo
import com.example.jddata.action.BaseAction
import com.example.jddata.action.Command
import com.example.jddata.action.append
import com.example.jddata.util.BaseLogFile
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.shelldroid.Env
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.LogUtil
import java.util.ArrayList
import java.util.HashMap

class FetchLeaderboardActionNoSku(env: Env) : BaseAction(env, ActionType.FETCH_LEADERBOARD) {

    var tabTitles = ArrayList<String>()
    var clickedTabs = ArrayList<String>()
    var currentTab = ""
    var currentCity = ""
    var productSet = HashSet<String>()
    var tabClickCount = 0

    init {
        appendCommand(Command().commandCode(ServiceCommand.FIND_TEXT).addScene(AccService.JD_HOME))
                .append(Command().commandCode(ServiceCommand.LEADERBOARD_TAB_CONFIRM)
                        .addScene(AccService.NATIVE_COMMON).delay(12000))
                .append(Command().commandCode(ServiceCommand.LEADERBOARD_HOT).delay(5000))
                .append(Command().commandCode(ServiceCommand.LEADERBOARD_TAB)
                        .delay(5000L))
    }
    val name = GlobalInfo.LEADERBOARD

    override fun initLogFile() {
        logFile = BaseLogFile("获取_$name")
    }

    // 排行榜的页面比较特别，控件都是没有id的，只能根据固定的序号来判断了。
    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.LEADERBOARD_TAB_CONFIRM -> {
                val desNodes = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, "系统定位您在")
                if (AccessibilityUtils.isNodesAvalibale(desNodes)) {
                    val okNodes = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, "确定")
                    if (AccessibilityUtils.isNodesAvalibale(okNodes)) {
                        val rect = Rect()
                        okNodes[0].getBoundsInScreen(rect)
                        return ExecUtils.handleExecCommand("input tap ${rect.left+10} ${rect.top+10}")
                    }
                }
                BusHandler.instance.startCountTimeout()
                return false
            }
            ServiceCommand.LEADERBOARD_HOT -> {
                val nodes = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, "热卖榜")
                if (AccessibilityUtils.isNodesAvalibale(nodes)) {
                    for (node in nodes) {
                        val rect = Rect()
                        node.getBoundsInScreen(rect)
                        if (rect.bottom > ExecUtils.computeY(900)) {
                            ExecUtils.handleExecCommand("input tap ${rect.left + 10} ${rect.top + 10}")
                            return true
                        }
                    }
                }
                return false
            }
            ServiceCommand.LEADERBOARD_TAB -> {
                BusHandler.instance.startCountTimeout()
                val result = leaderBoardTab()
                return result
            }
            ServiceCommand.FIND_TEXT -> {
                logFile?.writeToFileAppend("找到并点击 ${name}")
                BusHandler.instance.startCountTimeout()

                val nodes = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")
                        ?: return false
                for (node in nodes) {
                    var index = GlobalInfo.SCROLL_COUNT - 7
                    do {
                        var leader = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, "跟榜购好物")
                        if (!AccessibilityUtils.isNodesAvalibale(leader)) {
                            leader = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, "排行榜")
                        }
                        if (AccessibilityUtils.isNodesAvalibale(leader)) {
                            val parent = AccessibilityUtils.findParentClickable(leader!![0])
                            if (parent != null) {
                                return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            }
                        }
                        index++
                        sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
                    } while (ExecUtils.canscroll(node, index))
                }
                return false
            }
        }
        return super.executeInner(command)
    }


    private fun leaderBoardTab(): Boolean {
        val root = mService!!.rootInActiveWindow
        if (root != null) {
            // 找第一个text，是当前城市。
            val citys = AccessibilityUtils.findChildByClassname(root, "android.widget.TextView")
            if (AccessibilityUtils.isNodesAvalibale(citys)) {
                val cityNode = citys[0]
                if (cityNode.text != null) {
                    val city = cityNode.text.toString()
                    logFile?.writeToFileAppend("城市")
                    logFile?.writeToFileAppend(city)
                    currentCity = city
                }
            }

            val scrolls = AccessibilityUtils.findChildByClassname(root, "android.widget.HorizontalScrollView")
            if (AccessibilityUtils.isNodesAvalibale(scrolls)) {
                for (scroll in scrolls) {
                    val rect = Rect()
                    scroll.getBoundsInScreen(rect)
                    if (rect.top < 0 || rect.left < 0 || rect.right < 0 || rect.bottom < 0
                            || rect.bottom > ExecUtils.computeY(500)) {
                        continue
                    }
                    do {
                        val tabNodes = AccessibilityUtils.findChildByClassname(scroll, "android.widget.TextView")
                        if (AccessibilityUtils.isNodesAvalibale(tabNodes)) {
                            for (tab in tabNodes!!) {
                                if (tab.text != null && !tabTitles.contains(tab.text)) {
                                    val title = tab.text.toString()
                                    tabTitles.add(title)
                                    logFile?.writeToFileAppend(title)

                                    val map = HashMap<String, Any?>()
                                    val row = RowData(map)
                                    row.setDefaultData(env!!)

                                    itemCount++
                                    row.biId = GlobalInfo.LEADERBOARD
                                    row.itemIndex = "${itemCount}"
                                    row.tab = title
                                    row.city = ExecUtils.translate(currentCity)
                                    LogUtil.dataCache(row)

                                    logFile?.writeToFileAppend("${itemCount}", title)
                                    if (itemCount >= GlobalInfo.FETCH_NUM) {
                                        return true
                                    }
                                }
                            }
                        }
                    } while (scroll.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD))
                }

                return true
            }
        }
        return false
    }

}