package com.example.jddata.action.move

import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.GlobalInfo
import com.example.jddata.action.Command
import com.example.jddata.excel.BaseLogFile
import com.example.jddata.service.*
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.ExecUtils

open class MoveSearchAndClickAction(actionType: String, map: HashMap<String, String>?) : MoveSearchAction(actionType, map) {
    var clickText: String? = null

    constructor(map: HashMap<String, String>?): this(ActionType.MOVE_SEARCH_AND_CLICK, map)

    init {
        clickText = map!!.get("clickText")!!
        appendCommand(Command(ServiceCommand.SEACH_CLICK).addScene(AccService.PRODUCT_LIST))
    }

    override fun initLogFile() {
        logFile = BaseLogFile("动作_搜索_${searchText}_点击_${clickText}")
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.SEACH_CLICK -> {
                val result = findText()
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
                        if (product.contains(clickText!!)) {
                            val parent = AccessibilityUtils.findParentClickable(item)
                            if (parent != null) {
                                val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                if (result) {
                                    logFile?.writeToFileAppendWithTime("点击商品 $product")
                                    addMoveExtra("点击商品 $product")
                                }
                                return result
                            }
                        }
                    }
                }
            }
            index++

            if (index % 10 == 0) {
                BusHandler.instance.startCountTimeout()
            }
            sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
        } while ((nodes[0].performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                        || ExecUtils.fingerScroll())
                && index < GlobalInfo.SCROLL_COUNT)

        return false
    }
}
