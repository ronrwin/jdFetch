package com.example.jddata.action.fetch

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.*
import com.example.jddata.GlobalInfo
import com.example.jddata.action.BaseAction
import com.example.jddata.action.Command
import com.example.jddata.action.append
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.shelldroid.Env
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.BaseLogFile
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.LogUtil

class FetchWorthBuyAction(env: Env) : BaseAction(env, ActionType.FETCH_WORTH_BUY) {

    val fetchTabs = ArrayList<String>()
    val clickedTabs = ArrayList<String>()
    var currentTab: String? = null

    init {
        appendCommand(Command().commandCode(ServiceCommand.FIND_TEXT).addScene(AccService.JD_HOME))
                .append(Command().commandCode(ServiceCommand.COLLECT_TAB).addScene(AccService.WORTHBUY))
    }

    val name = GlobalInfo.WORTH_BUY
    override fun initLogFile() {
        logFile = BaseLogFile("获取_$name")
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.FIND_TEXT -> {
                logFile?.writeToFileAppend("找到并点击 $name")
                return findHomeTextClick(name)
            }
            ServiceCommand.FETCH_PRODUCT -> {
                val result = fetchProduct()
                if (result) {
                    appendCommand(Command().commandCode(ServiceCommand.COLLECT_TAB))
                }
                return result
            }
            ServiceCommand.COLLECT_TAB -> {
                BusHandler.instance.startCountTimeout()
                val resultCode = collectTabs()
                when (resultCode) {
                    COLLECT_FAIL -> {
                        return false
                    }
                    COLLECT_END -> {
                        return false
                    }
                    COLLECT_SUCCESS -> {
                        appendCommand(Command().commandCode(ServiceCommand.CLICK_TAB))
                        return true
                    }
                }
                return true
            }
            ServiceCommand.CLICK_TAB -> {
                val result = clickTab()
                if (result) {
                    fetchItems.clear()
                    itemCount = 0
                    currentItem = null
                    clickedItems.clear()

                    appendCommand(Command().commandCode(ServiceCommand.COLLECT_ITEM))
                }
                return result
            }
            ServiceCommand.WORTH_GO_BUY -> {
                val result = AccessibilityUtils.performClick(mService, "com.jd.lib.worthbuy:id/go_buy", false)
                if (result) {
                    appendCommands(getSkuCommands())
                }

                return result
            }
            ServiceCommand.WORTH_BUY_SUB_PRODUCT -> {
                var fetchCount = HashSet<String>()
                val lists = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/recycler_view")
                if (AccessibilityUtils.isNodesAvalibale(lists)) {
                    var index = GlobalInfo.SCROLL_COUNT-5
                    do {
                        val nodes = lists[0].findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/similar_product_subtitle")
                        if (AccessibilityUtils.isNodesAvalibale(nodes)) {
                            for (node in nodes) {
                                if (node.text != null) {
                                    val product = node.text.toString()

                                    val addResult = fetchCount.add(product)
                                    if (addResult) {
                                        val map = HashMap<String, Any?>()
                                        val row = RowData(map)
                                        row.setDefaultData(env!!)
                                        if (fetchCount.size == 1) {
                                            itemCount++
                                            row.product = currentItem?.arg1?.replace("\n", "")?.replace(",", "、")
                                        } else {
                                            row.product = product?.replace("\n", "")?.replace(",", "、")
                                        }
                                        row.description = currentItem?.arg2?.replace("\n", "")?.replace(",", "、")
                                        row.likeNum = currentItem?.arg3?.replace("\n", "")?.replace(",", "、")
                                        row.price = currentItem?.arg5?.replace("\n", "")?.replace(",", "、")
                                        row.biId = GlobalInfo.WORTH_BUY
                                        row.tab = currentTab
                                        row.itemIndex = "${clickedTabs.size}-${itemCount}-${fetchCount.size}"
                                        LogUtil.dataCache(row)

                                        logFile?.writeToFileAppend("获取第${row.itemIndex}个商品信息：${row.product}", "${map}")

                                        if (fetchCount.size >= GlobalInfo.WORTH_BUY_COUNT) {
                                            appendCommand(Command().commandCode(ServiceCommand.GO_BACK))
                                            appendCommand(Command().commandCode(ServiceCommand.COLLECT_ITEM)
                                                    .addScene(AccService.WORTHBUY))
                                            return true
                                        }
                                    }
                                }
                            }
                        }
                        index++
                        sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
                    } while (ExecUtils.canscroll(lists[0], index))
                }

                appendCommand(Command().commandCode(ServiceCommand.GO_BACK))
                appendCommand(Command().commandCode(ServiceCommand.COLLECT_ITEM)
                        .addScene(AccService.WORTHBUY))
                return false
            }
        }
        return super.executeInner(command)
    }


    val fetchItems = LinkedHashSet<Data3>()
    val clickedItems = LinkedHashSet<Data3>()
    var currentItem: Data5? = null

    override fun collectItems(): Int {
        if (itemCount >= GlobalInfo.FETCH_NUM) {
            return COLLECT_END
        }
        if (fetchItems.size > 0) {
            return COLLECT_SUCCESS
        }

        val lists = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")
        if (AccessibilityUtils.isNodesAvalibale(lists) && lists.size > 1) {
            val last = lists[lists.size - 1]
            var list = lists[lists.size - 2]
            if (last != null && AccessibilityUtils.getAllText(last).isNotEmpty() && clickedTabs.size > 2) {
                list = last
            }

            var index = 0
            do {
                val items = list.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/product_item")
                if (AccessibilityUtils.isNodesAvalibale(items)) {
                    var addResult = false
                    for (item in items) {
                        var title = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/product_name"))
                        var desc = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/product_desc"))
                        var collectNum = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/text_collect_number"))
                        if (title != null) {
                            val data = Data3(title, desc, collectNum)
                            if (!clickedItems.contains(data)) {
                                addResult = fetchItems.add(data)
                                if (addResult) {
                                    logFile?.writeToFileAppend("待点击商品：", data.arg1, data.arg2, data.arg3)
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
            } while (ExecUtils.canscroll(list, index))
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
                    val titles = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, item.arg1)
                    if (AccessibilityUtils.isNodesAvalibale(titles)) {
                        val parent = AccessibilityUtils.findParentClickable(titles[0])
                        if (parent != null) {
                            clickedItems.add(item)
                            val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            if (result) {
                                currentItem = Data5(item.arg1, item.arg2, item.arg3, "", "")
                                // 取sku则用ServiceCommand.WORTH_GO_BUY
//                                appendCommand(Command().commandCode(ServiceCommand.WORTH_GO_BUY)
//                                        .addScene(AccService.WORTH_DETAIL)
//                                        .addScene(AccService.WORTH_DETAIL_NEW)
//                                        .delay(4000))
                                appendCommand(Command().commandCode(ServiceCommand.WORTH_BUY_SUB_PRODUCT)
                                        .addScene(AccService.WORTH_DETAIL)
                                        .addScene(AccService.WORTH_DETAIL_NEW).delay(3000))
                                logFile?.writeToFileAppend("点击第${itemCount+1}商品：", item.arg1)

                                return result
                            }
                        }
                    }
                }
                logFile?.writeToFileAppend("没找到未点击商品：", item.arg1)
            } else {
                break
            }
        }
        appendCommand(Command().commandCode(ServiceCommand.COLLECT_ITEM))
        return false
    }

    override fun beforeLeaveProductDetail() {
        appendCommand(Command().commandCode(ServiceCommand.GO_BACK))
        appendCommand(Command().commandCode(ServiceCommand.COLLECT_ITEM)
                .addScene(AccService.WORTHBUY))
        super.beforeLeaveProductDetail()
    }

    override fun changeProduct(product: String) {
        if (currentItem != null) {
            currentItem!!.arg4 = product
        }
    }

    override fun changePrice(price: String) {
        if (currentItem != null) {
            currentItem!!.arg5 = price
        }
    }

    override fun fetchSkuid(skuid: String): Boolean {
        itemCount++

        val map = HashMap<String, Any?>()
        val row = RowData(map)
        row.setDefaultData(env!!)
        row.title = currentItem?.arg1?.replace("\n", "")?.replace(",", "、")
        row.description = currentItem?.arg2?.replace("\n", "")?.replace(",", "、")
        row.likeNum = currentItem?.arg3?.replace("\n", "")?.replace(",", "、")
        row.product = currentItem?.arg4?.replace("\n", "")?.replace(",", "、")
        row.price = currentItem?.arg5?.replace("\n", "")?.replace(",", "、")
        row.biId = GlobalInfo.WORTH_BUY
        row.tab = currentTab
        row.itemIndex = "${clickedTabs.size}-${itemCount}"
        LogUtil.dataCache(row)

        logFile?.writeToFileAppend("获取第${itemCount}个商品信息：${map}")

        return super.fetchSkuid(skuid)
    }

    fun fetchProduct(): Boolean {
        val lists = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")
        if (AccessibilityUtils.isNodesAvalibale(lists)) {
            for (list in lists) {
                var index = 0
                do {
                    val items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/product_item")
                    if (AccessibilityUtils.isNodesAvalibale(items)) {
                        for (item in items) {
                            var title = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/product_name"))
                            var desc = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/product_desc"))
                            var collectNum = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/text_collect_number"))
                            if (title != null) {
                                val data = Data3(title, desc, collectNum)
                                if (fetchItems.add(data)) {
                                    itemCount++
                                    logFile?.writeToFileAppend("获取第${itemCount}个商品信息：${data}")

                                    val map = HashMap<String, Any?>()
                                    val row = RowData(map)
                                    row.setDefaultData(env!!)
                                    row.title = title?.replace("\n", "")?.replace(",", "、")
                                    row.description = desc?.replace("\n", "")?.replace(",", "、")
                                    row.likeNum = collectNum?.replace("\n", "")?.replace(",", "、")
                                    row.biId = GlobalInfo.WORTH_BUY
                                    row.tab = currentTab
                                    row.itemIndex = "${itemCount}"
                                    LogUtil.dataCache(row)

                                    if (itemCount >= GlobalInfo.FETCH_NUM) {
                                        return true
                                    }
                                }
                            }
                        }
                    }

                    index++
                    sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
                } while (ExecUtils.canscroll(list, index))
            }
            return true
        }

        return false
    }

    fun clickTab(): Boolean {
        while (fetchTabs.size > 0) {
            val item = fetchTabs.removeAt(0)
            if (!clickedTabs.contains(item)) {
                currentTab = item
                val titles = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, item)
                if (AccessibilityUtils.isNodesAvalibale(titles)) {
                    clickedTabs.add(item)
                    val result = titles[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    if (result) {
                        logFile?.writeToFileAppend("点击第${clickedTabs.size}标签：", item)
                        itemCount = 0
                        return result
                    }
                }
            }
            logFile?.writeToFileAppend("没找到标签：", item)
        }
        appendCommand(Command().commandCode(ServiceCommand.COLLECT_TAB))
        return false
    }

    override fun onCollectItemEnd() {
        appendCommand(Command().commandCode(ServiceCommand.COLLECT_TAB))
        super.onCollectItemEnd()
    }

    fun collectTabs(): Int {
        if (clickedTabs.size >= GlobalInfo.WORTH_BUY_TAB) {
            return COLLECT_END
        }
        if (fetchTabs.size > 0) {
            return COLLECT_SUCCESS
        }
        val scrolls = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/tab")
        if (AccessibilityUtils.isNodesAvalibale(scrolls)) {
            // 减少滑动次数
            var index = GlobalInfo.SCROLL_COUNT - 5
            do {
                var addResult = false
                val texts = AccessibilityUtils.findChildByClassname(scrolls[0], "android.widget.TextView")
                if (AccessibilityUtils.isNodesAvalibale(texts)) {
                    for (textNode in texts) {
                        if (textNode.text != null) {
                            val tab = textNode.text.toString()
                            if (!clickedTabs.contains(tab)) {
                                fetchTabs.add(tab)
                                addResult = true
                                logFile?.writeToFileAppend("待点击标签：$tab")
                            }
                        }
                    }
                }
                if (addResult) {
                    return COLLECT_SUCCESS
                }

                index++
                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while (ExecUtils.canscroll(scrolls[0], index))

            logFile?.writeToFileAppend("没有更多标签")
            return COLLECT_END
        }
        return COLLECT_FAIL
    }

}