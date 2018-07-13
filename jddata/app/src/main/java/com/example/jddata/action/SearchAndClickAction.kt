package com.example.jddata.action

import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.BaseWorkBook
import com.example.jddata.service.*
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.ExecUtils

open class SearchAndClickAction(searchText: String, var clickText: String) : SearchAction(searchText) {

    init {
        appendCommand(Command(ServiceCommand.SEACH_CLICK).addScene(AccService.PRODUCT_LIST))
    }

    override fun initWorkbook() {
        workBook = BaseWorkBook("搜索_${searchText}_点击_${clickText}")
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.SEACH_CLICK -> {
                val result = findText()
                if (result) {
                    Thread.sleep(2000L)
                }
                return result
            }
        }
        return super.executeInner(command)
    }

    fun findText(): Boolean {
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/product_list")
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) {
            return false
        }
        var index = 0
        do {
            val items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/product_list_item")
            if (items != null) {
                for (item in items) {
                    val products = item.findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/product_item_name")
                    var product = AccessibilityUtils.getFirstText(products)
                    if (!TextUtils.isEmpty(product)) {
                        if (product.contains(clickText)) {
                            val parent = AccessibilityUtils.findParentClickable(item)
                            if (parent != null) {
                                workBook?.writeToSheetAppendWithTime("点击商品 $product")
                                return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            }
                        }
                    }
                }
            }
        } while ((nodes[0].performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                        || ExecUtils.fingerScroll()) && index < GlobalInfo.SCROLL_COUNT)

        return false
    }
}
