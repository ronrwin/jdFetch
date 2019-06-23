package com.example.jddata.action.unknown

import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.Data2
import com.example.jddata.Entity.RowData
import com.example.jddata.GlobalInfo
import com.example.jddata.MainApplication
import com.example.jddata.action.BaseAction
import com.example.jddata.action.Command
import com.example.jddata.action.append
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.shelldroid.Env
import com.example.jddata.util.*
import java.util.concurrent.ConcurrentLinkedQueue

open class SearchSkuAction(env: Env) : BaseAction(env, ActionType.SEARCH_SKU) {
    var searchText: String? = null
    var originSearchText: String? = null


    init {
    }

    var outFile = LogUtil.SKU_OUT
    val fetchDelay = 2000L
    var lines: ConcurrentLinkedQueue<String>? = null
    var fetchNum = 3
    var currentIndex = 0

    fun getsText(): String? {
        if (lines != null && lines!!.isNotEmpty()) {
            var text = lines!!.poll().replace("\n", "").replace("\r", "")
            while (TextUtils.isEmpty(text)) {
                currentIndex++
                if (lines!!.isNotEmpty()) {
                    text = lines!!.poll().replace("\n", "").replace("\r", "")
                } else {
                    FileUtils.writeToFile(LogUtil.EXTERNAL_FILE_FOLDER, "${MainApplication.sCurrentSkuFile}_done", "", false)
                    return null
                }
            }
            originSearchText = text
            return text
        } else {
            FileUtils.writeToFile(LogUtil.EXTERNAL_FILE_FOLDER, "${MainApplication.sCurrentSkuFile}_done", "", false)
        }
        return null
    }

    fun setSrc(queue: ConcurrentLinkedQueue<String>, num: Int, outFile: String) {
        this.lines = queue
        this.outFile = outFile
        currentIndex = 0
        fetchNum = num
        appendCommand(Command().commandCode(ServiceCommand.CLICK_SEARCH).addScene(AccService.JD_HOME))
        searchText = getsText()
        if (!TextUtils.isEmpty(searchText)) {
            appendCommand(Command().commandCode(ServiceCommand.INPUT_FOR_SKU).addScene(AccService.SEARCH)
                    .setState(GlobalInfo.SEARCH_KEY, searchText!!))
                    .append(Command().commandCode(ServiceCommand.SEARCH))
                    .append(Command().commandCode(ServiceCommand.COLLECT_ITEM)
                            .addScene(AccService.PRODUCT_LIST).delay(fetchDelay))
        }
    }

    override fun initLogFile() {
        logFile = BaseLogFile("获取_搜索_sku")
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
            ServiceCommand.INPUT_FOR_SKU -> {
                val text = command.commandStates[GlobalInfo.SEARCH_KEY]
                if (text is String) {
                    val result = ExecUtils.commandInput(mService!!, "android.widget.EditText", "com.jd.lib.search:id/search_text", text)
                    LogUtil.logCache("搜索第${currentIndex}个： ${text}")
                    return result
                }
            }
            ServiceCommand.INPUT_FOR_SKU_IN_LIST -> {
                val text = command.commandStates[GlobalInfo.SEARCH_KEY]
                val result = ExecUtils.commandInput(mService!!, "android.widget.EditText", "com.jd.lib.search:id/search_text", text!!)
                return result
            }
            ServiceCommand.SEARCH_IN_RESULT -> {
                var result = false

                val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/layout_container")
                if (AccessibilityUtils.isNodesAvalibale(nodes)) {
                    val parent = AccessibilityUtils.findParentClickable(nodes[0])
                    if (parent != null) {
                        appendCommand(Command().commandCode(ServiceCommand.INPUT_FOR_SKU).addScene(AccService.SEARCH)
                                .setState(GlobalInfo.SEARCH_KEY, searchText!!))
                                .append(Command().commandCode(ServiceCommand.SEARCH))
                                .append(Command().commandCode(ServiceCommand.COLLECT_ITEM)
                                        .addScene(AccService.PRODUCT_LIST).delay(fetchDelay))

                        result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        if (result) {
                            LogUtil.logCache("点击搜索框")
                        }
                        return result
                    }
                }

                if (!result) {
                    searchText = getsText()
                    if (!TextUtils.isEmpty(searchText)) {
                        appendCommand(Command().commandCode(ServiceCommand.JD_HOME).delay(4000))
                        appendCommand(Command().commandCode(ServiceCommand.CLICK_SEARCH).addScene(AccService.JD_HOME))
                        appendCommand(Command().commandCode(ServiceCommand.INPUT_FOR_SKU).addScene(AccService.SEARCH)
                                .setState(GlobalInfo.SEARCH_KEY, searchText!!))
                                .append(Command().commandCode(ServiceCommand.SEARCH))
                                .append(Command().commandCode(ServiceCommand.COLLECT_ITEM)
                                        .addScene(AccService.PRODUCT_LIST).delay(fetchDelay))
                    }
                }
                return false
            }
        }
        return super.executeInner(command)
    }

    val fetchItems = LinkedHashSet<Data2>()
    val clickedItems = LinkedHashSet<Data2>()
    var currentItem: Data2? = null

    var tryFlag = 1
    override fun onCollectItemFail() {
        if (!TextUtils.isEmpty(searchText)) {
            val splitParts = searchText!!.trim().split(" ")
            if (splitParts.size > 1) {
                if (tryFlag == 1) {
                    searchText = searchText!!.replace(splitParts[0], "").trim()
                } else {
                    searchText = searchText!!.replace(splitParts[splitParts.size - 1], "").trim()
                }
                if (!TextUtils.isEmpty(searchText)) {
                    appendCommand(Command().commandCode(ServiceCommand.SEARCH_IN_RESULT))
                    return
                } else {
                    nextSearch()
                }
            } else {
                if (tryFlag == 1) {
                    tryFlag = 2
                    searchText = originSearchText
                    onCollectItemFail()
                } else if (tryFlag == 2) {
                    searchText = searchText!!.substring(0, searchText!!.length-1)
                    if (!TextUtils.isEmpty(searchText)) {
                        appendCommand(Command().commandCode(ServiceCommand.SEARCH_IN_RESULT))
                        return
                    } else {
                        nextSearch()
                    }
                } else {
                    nextSearch()
                }
            }
        } else {
            nextSearch()
        }
    }

    fun nextSearch() {
        tryFlag = 1
        itemCount = 0
        currentIndex++
        searchText = getsText()
        if (!TextUtils.isEmpty(searchText)) {
            appendCommand(Command().commandCode(ServiceCommand.SEARCH_IN_RESULT))
        }
    }

    override fun onCollectItemEnd() {
        nextSearch()
    }

    override fun collectItems(): Int {
        if (itemCount >= fetchNum) {
            return COLLECT_END
        }
        if (fetchItems.size > 0) {
            return COLLECT_SUCCESS
        }
        val lists = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/product_list")
        if (AccessibilityUtils.isNodesAvalibale(lists)) {
            for (list in lists) {
                var index = GlobalInfo.SCROLL_COUNT - 2
                var addResult = false
                do {
                    val items = list.findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/product_item_name")
                    if (AccessibilityUtils.isNodesAvalibale(items)) {
                        for (item in items) {
                            val parent = AccessibilityUtils.findParentClickable(item)
                            var product = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/product_item_name"))
                            var price = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/product_item_jdPrice"))
                            if (product != null && price != null) {
                                if (product != null && product.startsWith("1 ")) {
                                    product = product.replace("1 ", "");
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
                } while (ExecUtils.canscroll(list, index))

                if (addResult) {
                    return COLLECT_SUCCESS
                }
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
                    clickedItems.add(item)
                    val titles = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, item.arg1)
                    if (AccessibilityUtils.isNodesAvalibale(titles)) {
                        val parent = AccessibilityUtils.findParentClickable(titles[0])
                        if (parent != null) {
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

        val product = currentItem?.arg1?.replace("1 ", "")?.replace("\n", "")?.replace(",", "、")
        val price = currentItem?.arg2?.replace("\n", "")?.replace(",", "、")

        val content = "${originSearchText!!.replace("\r", "").replace("\n", "")}->${itemCount}--------${product},${price},${skuid}\n"
        MainApplication.sExecutor.execute {
            FileUtils.writeToFile(LogUtil.EXTERNAL_FILE_FOLDER, outFile, content, true)
            if (itemCount == 1) {
                FileUtils.writeToFile(LogUtil.EXTERNAL_FILE_FOLDER, "out_${outFile}", originSearchText + "\n", true)
            }
        }
        return super.fetchSkuid(skuid)
    }
}
