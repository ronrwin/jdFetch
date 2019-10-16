package com.example.jddata.action.fetch

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.Data2
import com.example.jddata.Entity.RowData
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

open class FetchSearchAction(env: Env) : BaseAction(env, ActionType.FETCH_SEARCH) {

    init {
        searchText = "洗发水"
        val moveNo = env.day9!!.toInt()
        if (moveNo < 5) {
            if (moveNo < 4) {
                searchText = "洗发水"
            } else {
                searchText = "海飞丝"
            }
        }
        appendCommand(Command().commandCode(ServiceCommand.CLICK_SEARCH).addScene(AccService.JD_HOME))
                .append(Command().commandCode(ServiceCommand.INPUT).addScene(AccService.SEARCH)
                        .setState(GlobalInfo.SEARCH_KEY, searchText!!))
                .append(Command().commandCode(ServiceCommand.SEARCH))
                .append(Command().commandCode(ServiceCommand.FETCH_PRODUCT)
                        .addScene(AccService.PRODUCT_LIST).delay(2000))
    }

    override fun initLogFile() {
        logFile = BaseLogFile("获取_搜索_$searchText")
    }

    var retryTime = 0

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
            ServiceCommand.FETCH_PRODUCT -> {
                return fetchProduct()
            }
        }
        return super.executeInner(command)
    }

    fun fetchProduct(): Boolean {
        val set = HashSet<String>()
        val lists = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/product_list")
        if (AccessibilityUtils.isNodesAvalibale(lists)) {
            var index = 0
            do {
                val items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/a3g")
                if (AccessibilityUtils.isNodesAvalibale(items)) {
                    for (item in items) {
                        val parent = AccessibilityUtils.findParentClickable(item)
                        if (parent != null) {
                            var product = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/a3g"))
                            var price = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/aa2"))
                            if (product != null && price != null) {
                                if (product.startsWith("1 ")) {
                                    product = product.replace("1 ", "");
                                }
                                price = price.replace("¥", "")
                                val recommend = Data2(product, price)
                                if (set.add(product)) {
                                    itemCount++

                                    val map = HashMap<String, Any?>()
                                    val row = RowData(map)
                                    row.setDefaultData(env!!)
                                    row.product = product.replace("1 ", "")?.replace("\n", "")?.replace(",", "、")
                                    row.price = price?.replace("\n", "")?.replace(",", "、")
                                    row.biId = GlobalInfo.SEARCH
                                    row.itemIndex = "${itemCount}"
                                    LogUtil.dataCache(row)

                                    logFile?.writeToFileAppend("收集${itemCount}点击商品：", product, price)
                                    if (itemCount >= GlobalInfo.FETCH_NUM) {
                                        return true
                                    }
                                }
                            }
                        }
                    }
                }
                index++
                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while (ExecUtils.canscroll(lists[0], index))

            logFile?.writeToFileAppend(GlobalInfo.NO_MORE_DATA)
            return true
        }
        return false
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
                val items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/product_item_name")
                if (AccessibilityUtils.isNodesAvalibale(items)) {
                    var addResult = false
                    for (item in items) {
                        val parent = AccessibilityUtils.findParentClickable(item)
                        var product = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/product_item_name"))
                        var price = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/product_item_jdPrice"))
                        if (product != null && price != null) {
                            if (product.startsWith("1 ")) {
//                                product = product.replace("1 ", "");
                            }
                            price = price.replace("¥", "")
                            val recommend = Data2(product, price)
                            if (!clickedItems.contains(recommend)) {
                                addResult = fetchItems.add(recommend)
                                if (addResult) {
                                    logFile?.writeToFileAppend("待点击商品：", product, price)
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

            logFile?.writeToFileAppend(GlobalInfo.NO_MORE_DATA)
            return COLLECT_END
        }

        if (itemCount < GlobalInfo.FETCH_NUM && retryTime < 3) {
            appendCommand(Command().commandCode(ServiceCommand.COLLECT_ITEM))
            retryTime++
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
                                currentItem = item
                                appendCommands(getSkuCommands())
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
        appendCommand(Command().commandCode(ServiceCommand.COLLECT_ITEM).addScene(AccService.PRODUCT_LIST))
        super.beforeLeaveProductDetail()
    }

    override fun fetchSkuid(skuid: String): Boolean {
        itemCount++
        logFile?.writeToFileAppend("记录商品：${currentItem.toString()}, sku: $skuid")

        val map = HashMap<String, Any?>()
        val row = RowData(map)
        row.setDefaultData(env!!)
        row.sku = skuid
        row.product = currentItem?.arg1?.replace("1 ", "")?.replace("\n", "")?.replace(",", "、")
        row.price = currentItem?.arg2?.replace("\n", "")?.replace(",", "、")
        row.biId = GlobalInfo.SEARCH
        row.itemIndex = "${itemCount}"
        LogUtil.dataCache(row)

        return super.fetchSkuid(skuid)
    }
}
