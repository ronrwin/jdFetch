package com.example.jddata.action.fetch

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.Data3
import com.example.jddata.GlobalInfo
import com.example.jddata.action.BaseAction
import com.example.jddata.action.Command
import com.example.jddata.action.PureCommand
import com.example.jddata.action.append
import com.example.jddata.excel.BaseLogFile
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.ExecUtils

class FetchWorthBuyAction : BaseAction(ActionType.FETCH_WORTH_BUY) {

    val fetchItems = LinkedHashSet<Data3>()
    val clickedItems = LinkedHashSet<Data3>()
    var currentItem: Data3? = null

    init {
        appendCommand(Command(ServiceCommand.WORTH_BUY).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.COLLECT_ITEM).addScene(AccService.WORTHBUY))
    }

    override fun initLogFile() {
        logFile = BaseLogFile("获取_" + GlobalInfo.WORTH_BUY)
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.WORTH_BUY -> {
                logFile?.writeToFileAppendWithTime("找到并点击 \"${GlobalInfo.WORTH_BUY}\"")
                return findHomeTextClick(GlobalInfo.WORTH_BUY)
            }
            ServiceCommand.GO_BUY -> {
                return AccessibilityUtils.performClick(mService, "com.jd.lib.worthbuy:id/go_buy", false)
            }
        }
        return super.executeInner(command)
    }

    override fun collectItems(): Int {
        if (itemCount >= GlobalInfo.TYPE_KILL_COUNT) {
            return COLLECT_END
        }
        if (fetchItems.size > 0) {
            return COLLECT_SUCCESS
        }

        val lists = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")
        if (AccessibilityUtils.isNodesAvalibale(lists)) {
            for (list in lists) {
                var index = 0
                do {
                    val items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/product_item")
                    var addResult = false
                    if (AccessibilityUtils.isNodesAvalibale(items)) {
                        for (item in items) {
                            var title = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/product_name"))
                            var desc = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/product_desc"))
                            var collectNum = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/text_collect_number"))
                            if (title != null) {
                                val data = Data3(title, desc, collectNum)
                                if (!clickedItems.contains(data)) {
                                    addResult = fetchItems.add(data)
                                    if (addResult) {
                                        logFile?.writeToFileAppendWithTime("待点击商品：", title, desc)
                                    }
                                }
                            }
                        }
                    }

                    if (addResult) {
                        return COLLECT_SUCCESS
                    }
                    index++
                    sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
                } while (ExecUtils.canscroll(list, index))
            }
        }

        return COLLECT_FAIL
    }

    override fun clickItem(): Boolean {
        while (fetchItems.size > 0) {
            val item = fetchItems.firstOrNull()
            if (item != null) {
                fetchItems.remove(item)
                if (!clickedItems.contains(item)) {
                    currentItem = item
                    val titles = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, item.arg1)
                    if (AccessibilityUtils.isNodesAvalibale(titles)) {
                        val parent = AccessibilityUtils.findParentClickable(titles[0])
                        if (parent != null) {
                            clickedItems.add(item)
                            appendCommand(Command(ServiceCommand.GO_BUY)
                                            .addScene(AccService.WORTH_DETAIL)
                                            .delay(2000))
                                    .append(Command(ServiceCommand.GET_SKU)
                                            .addScene(AccService.PRODUCT_DETAIL)
                                            .delay(2000))
                                    .append(PureCommand(ServiceCommand.GO_BACK))
                                    .append(Command(ServiceCommand.GO_BACK)
                                            .addScene(AccService.WORTH_DETAIL))
                                    .append(Command(ServiceCommand.COLLECT_ITEM)
                                            .addScene(AccService.WORTHBUY))
                            val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            if (result) {
                                logFile?.writeToFileAppendWithTime("点击第${itemCount+1}商品：", item.arg1)
                                return result
                            }
                        }
                    }
                }
                logFile?.writeToFileAppendWithTime("没找到点击商品：", item.arg1)
            } else {
                break
            }
        }
        appendCommand(PureCommand(ServiceCommand.COLLECT_ITEM))
        return false
    }

    override fun fetchSkuid(skuid: String): Boolean {
        itemCount++
        // todo: 数据库

        return super.fetchSkuid(skuid)
    }
}