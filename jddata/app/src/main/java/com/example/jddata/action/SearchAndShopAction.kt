package com.example.jddata.action

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.GlobalInfo
import com.example.jddata.service.*
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.ExecUtils
import java.util.*

class SearchAndShopAction(searchText: String) : SearchAction(searchText) {
    init {
        appendCommand(Command(ServiceCommand.SEARCH_DATA_RANDOM_BUY).addScene(AccService.PRODUCT_LIST))
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.SEARCH_DATA_RANDOM_BUY -> {
                return searchDataRandomBuy()
            }
        }
        return super.executeInner(command)
    }

    private fun searchDataRandomBuy(): Boolean {
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/product_list")
                ?: return false
        for (node in nodes) {
            var index = 0
            val random = Random().nextInt(7)
            while (index <= random) {
                val s = node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                        || ExecUtils.handleExecCommand("input swipe 250 800 250 250")
                index++
            }

            val items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/btn_shopcart")
            if (items != null) {
                for (item in items) {
                    if (item.isClickable) {
                        return item.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                }
            }
        }
        return false
    }
}