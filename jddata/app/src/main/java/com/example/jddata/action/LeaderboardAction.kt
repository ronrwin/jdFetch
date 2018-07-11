package com.example.jddata.action

import android.graphics.Rect
import android.util.Log
import com.example.jddata.Entity.ActionType
import com.example.jddata.excel.LeaderboardSheet
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand
import com.example.jddata.util.ExecUtils
import java.util.ArrayList

class LeaderboardAction : BaseAction(ActionType.LEADERBOARD) {

    var tabIndex = 0
    var tabTitles = ArrayList<String>()
    init {
        appendCommand(Command(ServiceCommand.LEADERBOARD).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.LEADERBOARD_TAB).addScene(AccService.NATIVE_COMMON).delay(6000L).concernResult(true))
        sheet = LeaderboardSheet()
    }

    // 排行榜的页面比较特别，控件都是没有id的，只能根据固定的序号来判断了。
    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.LEADERBOARD_TAB -> {
                val result = leaderBoardTab()
                if (result) {
                    for (i in 0..4) {
                        appendCommand(PureCommand(ServiceCommand.LEADERBOARD_SELECT_TYPE))
                        appendCommand(PureCommand(ServiceCommand.LEADERBOARD_CONTENT))
                    }
                }
                return result
            }
            ServiceCommand.LEADERBOARD -> {
                sheet?.writeToSheetAppendWithTime("找到并点击 排行榜")
                return CommonConmmand.findHomeTextClick(mService!!, "排行榜")
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
                                sheet?.writeToSheetAppend("")
                                sheet?.writeToSheetAppendWithTime("点击标签 $tabString")
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
                        cardCount++
                        val contentNodes = AccessibilityUtils.findChildByClassname(parent, "android.widget.TextView")
                        var contentBuilder = StringBuilder()
                        for (contentNode in contentNodes) {
                            if (contentNode.text != null) {
                                contentBuilder.append("${contentNode.text}   ")
                            }
                        }
                        sheet?.writeToSheetAppend(contentBuilder.toString())
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

    private fun leaderBoardTab(): Boolean {
        val root = mService!!.getRootInActiveWindow()
        if (root != null) {
            // 找第一个text，是当前城市。
            val citys = AccessibilityUtils.findChildByClassname(root, "android.widget.TextView")
            if (AccessibilityUtils.isNodesAvalibale(citys)) {
                val cityNode = citys[0]
                if (cityNode.text != null) {
                    val city = cityNode.text.toString()
                    sheet?.writeToSheetAppend("城市")
                    sheet?.writeToSheetAppend(city)
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

                sheet?.writeToSheetAppend("")
                sheet?.writeToSheetAppend("标签")
                for (title in tabTitles) {
                    sheet?.writeToSheetAppend(title)
                }
                sheet?.writeToSheetAppend("")
                return true
            }
        }
        return false
    }

}