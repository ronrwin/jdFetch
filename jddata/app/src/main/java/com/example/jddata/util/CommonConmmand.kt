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