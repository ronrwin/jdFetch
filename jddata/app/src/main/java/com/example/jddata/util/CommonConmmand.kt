package com.example.jddata.util

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.GlobalInfo


class CommonConmmand {
    companion object {

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