package com.example.jddata.action

import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.excel.BaseWorkBook
import com.example.jddata.service.*
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.ExecUtils
import java.util.*

class SearchAndShopAction(searchText: String) : SearchAction(searchText) {
    init {
        appendCommand(Command(ServiceCommand.SEARCH_DATA_RANDOM_BUY).addScene(AccService.PRODUCT_LIST))
        workBook = BaseWorkBook("搜索并加购_$searchText")
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
                        || ExecUtils.fingerScroll()
                index++
            }

            val items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/btn_shopcart")
            if (items != null) {
                for (item in items) {
                    if (item.isClickable) {
                        val parent = item.parent
                        if (parent != null) {
                            val titles = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/product_item_name")
                            val prices = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/product_item_jdPrice")
                            var title = AccessibilityUtils.getFirstText(titles)
                            val price = AccessibilityUtils.getFirstText(prices)
                            if (!TextUtils.isEmpty(title)) {
                                if (title.startsWith("1 ")) {
                                    title = title.replace("1 ", "")
                                }

                                workBook?.writeToSheetAppendWithTime("加购商品",  title, price)
                                return item.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            }
                        }
                    }
                }
            }
        }
        return false
    }
}