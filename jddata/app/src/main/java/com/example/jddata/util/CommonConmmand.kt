package com.example.jddata.util

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.Recommend
import com.example.jddata.GlobalInfo
import java.util.ArrayList


class CommonConmmand {
    companion object {

        fun findHomeTextClick(service: AccessibilityService, text: String): Boolean {
            val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(service, "android:id/list")
                    ?: return false
            for (node in nodes) {
                var index = 0
                do {
                    val leader = AccessibilityUtils.findAccessibilityNodeInfosByText(service, text)
                    if (AccessibilityUtils.isNodesAvalibale(leader)) {
                        val parent = AccessibilityUtils.findParentClickable(leader!![0])
                        if (parent != null) {
                            return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        }
                    }
                    index++
                    if (index % 10 == 0) {
                        BusHandler.instance.startCountTimeout()
                    }
                    Thread.sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
                } while (node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) && index < 10)
            }
            return false
        }

        /**
         * 收集卡片信息
         * target: 只有标题，价格类型的卡片
         */
        fun parseRecommends(service: AccessibilityService, listNode: AccessibilityNodeInfo, scrollCount: Int): ArrayList<Recommend> {
            var index = 0

            while (listNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD));

            val recommendList = ArrayList<Recommend>()
            do {
                // 推荐部分
                val items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(service, "com.jingdong.app.mall:id/by_")
                if (items != null) {
                    for (item in items) {
                        val titles = item.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/br2")
                        var title: String? = null
                        if (AccessibilityUtils.isNodesAvalibale(titles)) {
                            if (titles[0].text != null) {
                                title = titles[0].text.toString()
                                if (title != null && title.startsWith("1 ")) {
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
                if (index % 10 == 0) {
                    BusHandler.instance.startCountTimeout()
                }
                Thread.sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while ((listNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                            || ExecUtils.handleExecCommand("input swipe 250 800 250 250"))
                    && index <= scrollCount)
            return ExecUtils.filterSingle(recommendList)
        }


        fun dmpclick(service: AccessibilityService): Boolean {
            val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(service, "com.jingdong.app.mall:id/h8")
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
}