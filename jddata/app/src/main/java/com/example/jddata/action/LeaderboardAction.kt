package com.example.jddata.action

import android.graphics.Rect
import com.example.jddata.Entity.ActionType
import com.example.jddata.excel.LeaderboardSheet
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand
import java.util.ArrayList

class LeaderboardAction : BaseAction(ActionType.LEADERBOARD) {

    init {
        appendCommand(Command(ServiceCommand.LEADERBOARD_TAB).addScene(AccService.NATIVE_COMMON))
                .append(Command(ServiceCommand.LEADERBOARD).addScene(AccService.JD_HOME))
    }

    // 排行榜的页面比较特别，控件都是没有id的，只能根据固定的序号来判断了。
    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.LEADERBOARD_TAB -> {
                return leaderBoardTab()
            }
            ServiceCommand.LEADERBOARD -> {
                return CommonConmmand.findHomeTextClick(mService!!, "排行榜")
            }
        }
        return super.executeInner(command)
    }

    private fun leaderBoardTab(): Boolean {
        val leaderboardSheet = LeaderboardSheet()
        val root = mService!!.getRootInActiveWindow()
        if (root != null) {
            // 找第一个text，是当前城市。
            val citys = AccessibilityUtils.findChildByClassname(root, "android.widget.TextView")
            if (AccessibilityUtils.isNodesAvalibale(citys)) {
                val cityNode = citys[0]
                if (cityNode.text != null) {
                    val city = cityNode.text.toString()
                    leaderboardSheet.writeToSheetAppend("城市")
                    leaderboardSheet.writeToSheetAppend(city)
                }
            }

            val scrolls = AccessibilityUtils.findChildByClassname(root, "android.widget.HorizontalScrollView")
            if (AccessibilityUtils.isNodesAvalibale(scrolls)) {
                val tabTitles = ArrayList<String>()
                for (scroll in scrolls) {
                    val rect = Rect()
                    scroll.getBoundsInScreen(rect)
                    if (rect.top < 0 || rect.left < 0 || rect.right < 0 || rect.bottom < 0
                            || rect.bottom > 170) {
                        continue
                    }
                    val tabs = AccessibilityUtils.findChildByClassname(scroll, "android.widget.TextView")
                    if (AccessibilityUtils.isNodesAvalibale(tabs)) {
                        for (tab in tabs) {
                            if (tab.text != null) {
                                tabTitles.add(tab.text.toString())
                            }
                        }
                    }
                }

                leaderboardSheet.writeToSheetAppend("")
                leaderboardSheet.writeToSheetAppend("标签")
                for (title in tabTitles) {
                    leaderboardSheet.writeToSheetAppend(title)
                }
                return true
            }
        }
        return false
    }

}