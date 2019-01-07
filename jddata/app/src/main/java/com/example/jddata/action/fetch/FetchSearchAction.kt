package com.example.jddata.action.fetch

import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.Data2
import com.example.jddata.GlobalInfo
import com.example.jddata.action.BaseAction
import com.example.jddata.action.Command
import com.example.jddata.action.PureCommand
import com.example.jddata.action.append
import com.example.jddata.util.BaseLogFile
import com.example.jddata.service.*
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.ExecUtils

open class FetchSearchAction : BaseAction(ActionType.FETCH_SEARCH) {
    var searchText: String? = null

    init {
        appendCommand(Command(ServiceCommand.CLICK_SEARCH).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.INPUT).addScene(AccService.SEARCH))
                .append(PureCommand(ServiceCommand.SEARCH))
                .append(Command(ServiceCommand.COLLECT_ITEM).addScene(AccService.PRODUCT_LIST))
    }

    override fun initLogFile() {
        logFile = BaseLogFile("获取_搜索_$searchText")
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
            ServiceCommand.CLICK_SEARCH -> {
                logFile?.writeToFileAppendWithTime("点击搜索栏")
                addMoveExtra("点击搜索栏")
                return ExecUtils.tapCommand(250, 75)
            }
            ServiceCommand.INPUT -> {
                val text = "洗发水"
                if (text is String) {
                    logFile?.writeToFileAppendWithTime("输入 $text")
                    addMoveExtra("输入 $text")
                    return ExecUtils.commandInput(mService!!, "android.widget.EditText", "com.jd.lib.search:id/search_text", text)
                }
            }
            ServiceCommand.SEARCH -> {
                logFile?.writeToFileAppendWithTime("点击搜索按钮")
                val result =  AccessibilityUtils.performClick(mService, "com.jingdong.app.mall:id/awn", false)
                return result
            }
        }
        return super.executeInner(command)
    }

    val fetchItems = LinkedHashSet<Data2>()
    val clickedItems = LinkedHashSet<Data2>()
    var currentItem: Data2? = null

    override fun collectItems(): Int {
        if (itemCount >= GlobalInfo.FETCH_NUM) {
            return COLLECT_END
        }
        if (fetchItems.size > 0) {
            return COLLECT_SUCCESS
        }

        val lists = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/product_list")
        if (AccessibilityUtils.isNodesAvalibale(lists)) {
            var index = 0
            do {
                val items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/product_list_item")
                if (AccessibilityUtils.isNodesAvalibale(items)) {
                    var addResult = false
                    for (item in items) {
                        var product = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/product_item_name"))
                        var price = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/product_item_jdPrice"))
                        if (product != null && price != null) {
                            if (product.startsWith("1 ")) {
                                product = product.replace("1 ", "");
                            }
                            price = price.replace("¥", "")
                            val recommend = Data2(product, price)
                            if (!clickedItems.contains(recommend)) {
                                addResult = fetchItems.add(recommend)
                                if (addResult) {
                                    logFile?.writeToFileAppendWithTime("待点击商品：", product, price)
                                }
                            }
                        }
                    }
                    if (addResult) {
                        return COLLECT_SUCCESS
                    }
                }
                index++
                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while (ExecUtils.canscroll(lists[0], index))

            logFile?.writeToFileAppendWithTime(GlobalInfo.NO_MORE_DATA)
            return COLLECT_END
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
                            appendCommands(getSkuCommands())
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

    override fun beforeLeaveProductDetai() {
        appendCommand(Command(ServiceCommand.COLLECT_ITEM).addScene(AccService.PRODUCT_LIST))
        super.beforeLeaveProductDetai()
    }

    override fun fetchSkuid(skuid: String): Boolean {
        itemCount++
        logFile?.writeToFileAppendWithTime("记录商品：${currentItem.toString()}, sku: $skuid")
        // todo: 加数据库
        return super.fetchSkuid(skuid)
    }
}