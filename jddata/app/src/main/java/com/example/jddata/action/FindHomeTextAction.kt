package com.example.jddata.action

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.GlobalInfo
import com.example.jddata.util.AccessibilityUtils

open class FindHomeTextAction(actionType: String) : BaseAction(actionType) {

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
}