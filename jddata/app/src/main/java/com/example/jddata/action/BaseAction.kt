package com.example.jddata.action

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.Entity.BrandEntity
import com.example.jddata.Entity.Recommend
import com.example.jddata.GlobalInfo
import com.example.jddata.MainApplication
import com.example.jddata.excel.BrandSheet
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.ExecUtils
import java.util.ArrayList
import java.util.HashMap

open class BaseAction(actionType: String) : Action(actionType) {
    init {
        // 解决广告弹出阻碍步骤
        appendCommand(Command(ServiceCommand.AGREE).addScene(AccService.PRIVACY).canSkip(true))
                .append(Command(ServiceCommand.HOME_TAB).addScene(AccService.JD_HOME))
                .append(PureCommand(ServiceCommand.CLOSE_AD).delay(5000L))
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
            ServiceCommand.AGREE -> return AccessibilityUtils.performClick(mService, "com.jingdong.app.mall:id/btb", false)
            ServiceCommand.HOME_TAB -> return AccessibilityUtils.performClickByText(mService, "android.widget.FrameLayout", "首页", false)
            ServiceCommand.CLOSE_AD -> {
                ExecUtils.handleExecCommand("input tap 500 75")
                sleep(2000L)
                MainApplication.startMainJD(false)
                return true
            }
            ServiceCommand.GO_BACK -> {
                return AccessibilityUtils.performGlobalActionBack(mService)
            }
        }
        return super.executeInner(command)
    }

    fun findHomeTextClick(text: String): Boolean {
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")
                ?: return false
        for (node in nodes) {
            var index = 0
            do {
                val leader = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, text)
                if (AccessibilityUtils.isNodesAvalibale(leader)) {
                    val parent = AccessibilityUtils.findParentClickable(leader!![0])
                    if (parent != null) {
                        return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                }
                index++
                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while (node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) && index < 10)
        }
        return false
    }

    /**
     * 收集卡片信息
     * target: 只有标题，价格类型的卡片
     */
    fun parseRecommends(listNode: AccessibilityNodeInfo, scrollCount: Int): ArrayList<Recommend> {
        var index = 0

        while (listNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD));

        val recommendList = ArrayList<Recommend>()
        do {
            // 推荐部分
            val items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/by_")
            if (items != null) {
                for (item in items) {
                    val titles = item.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/br2")
                    var title: String? = null
                    if (AccessibilityUtils.isNodesAvalibale(titles)) {
                        if (titles[0].text != null) {
                            title = titles[0].text.toString()
                            if (title.startsWith("1 ")) {
                                title = title.replace("1 ", "")
                            }
                        }
                    }
                    val prices = item.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/br3")
                    var price: String? = null
                    if (AccessibilityUtils.isNodesAvalibale(prices)) {
                        if (prices[0].text != null) {
                            price = prices[0].text.toString()
                        }
                    }
                    recommendList.add(Recommend(title, price))
                }
            }
            index++
            sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
        } while ((listNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                        || ExecUtils.handleExecCommand("input swipe 250 800 250 250"))
                && index <= scrollCount)
        return ExecUtils.filterSingle(recommendList)
    }


    fun dmpclick(): Boolean {
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/h8")
        if (AccessibilityUtils.isNodesAvalibale(nodes)) {
            val scroll = nodes!![0]
            if (scroll != null && scroll.childCount > 0) {
                val child = scroll.getChild(0)
                if (child != null) {
                    return child.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
            }
        }
        return false
    }
}


