package com.example.jddata.action.fetch

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.Data2
import com.example.jddata.Entity.Data3
import com.example.jddata.Entity.RowData
import com.example.jddata.GlobalInfo
import com.example.jddata.MainApplication
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

class FetchBrandKillAction(env: Env) : BaseAction(env, ActionType.FETCH_BRAND_KILL) {
    val fetchItems = LinkedHashSet<Data2>()
    val clickedItems = LinkedHashSet<Data2>()
    var currentItem: Data2? = null
    val fetchTabs = ArrayList<String>()
    val clickedTabs = ArrayList<String>()
    var currentTab: String? = null

    val fetchSubItems = LinkedHashSet<RowData>()
    val clickedSubItems = LinkedHashSet<String>()
    var currentSubItem: RowData? = null

    init {
        appendCommand(Command().commandCode(ServiceCommand.FIND_TEXT).addScene(AccService.JD_HOME))
                .append(Command().commandCode(ServiceCommand.COLLECT_TAB))
    }

    override fun initLogFile() {
        logFile = BaseLogFile("获取_" + GlobalInfo.BRAND_KILL)
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
            ServiceCommand.FIND_TEXT -> {
                logFile?.writeToFileAppend("找到并点击 ${GlobalInfo.BRAND_KILL}")
                return findHomeTextClick(GlobalInfo.BRAND_KILL)
            }
            ServiceCommand.COLLECT_ITEM -> {
                BusHandler.instance.startCountTimeout()
                if (MainApplication.sCurrentScene.equals(AccService.JSHOP)
                        || MainApplication.sCurrentScene.equals(AccService.WEBVIEW_ACTIVITY)
                        || MainApplication.sCurrentScene.equals(AccService.BABEL_ACTIVITY)) {
                    appendCommand(Command().commandCode(ServiceCommand.GO_BACK).delay(500))
                    appendCommand(Command().commandCode(ServiceCommand.COLLECT_ITEM)
                                .addScene(AccService.MIAOSHA)
                                .addScene(AccService.WEBVIEW_ACTIVITY)
                                .addScene(AccService.JSHOP)
                                .addScene(AccService.BABEL_ACTIVITY))
                    return false
                }
                val resultCode = collectItems()
                when (resultCode) {
                    COLLECT_FAIL -> {
                        LogUtil.logCache("debug", "COLLECT_FAIL")
                        onCollectItemFail()
                        return false
                    }
                    COLLECT_END -> {
                        LogUtil.logCache("debug", "COLLECT_END")
                        onCollectItemEnd()
                        return true
                    }
                    COLLECT_SUCCESS -> {
                        LogUtil.logCache("debug", "COLLECT_SUCCESS")
                        appendCommand(Command().commandCode(ServiceCommand.CLICK_ITEM))
                        return true
                    }
                }
                return true
            }
            ServiceCommand.COLLECT_TAB -> {
                BusHandler.instance.startCountTimeout()
                val resultCode = collectTabs()
                when (resultCode) {
                    COLLECT_FAIL -> {
                        return false
                    }
                    COLLECT_END -> {
                        return true
                    }
                    COLLECT_SUCCESS -> {
                        appendCommand(Command().commandCode(ServiceCommand.CLICK_TAB))
                        return true
                    }
                }
                return true
            }
            ServiceCommand.CLICK_TAB -> {
                BusHandler.instance.startCountTimeout()
                val result = clickTab()
                if (result) {;
                    itemCount = 0
                    fetchItems.clear()
                    clickedItems.clear()
                    appendCommand(Command().commandCode(ServiceCommand.COLLECT_ITEM).delay(400))
                }
                return result
            }
        }
        return super.executeInner(command)
    }

    override fun shouldInterruptSubCollectItem(): Boolean {
        if (MainApplication.sCurrentScene.equals(AccService.PRODUCT_DETAIL)) {
            appendCommand(Command().commandCode(ServiceCommand.GO_BACK).delay(500))
                    .append(Command().commandCode(ServiceCommand.COLLECT_SUB_ITEM))
            return true
        } else if (MainApplication.sCurrentScene.equals(AccService.BABEL_ACTIVITY)
                || MainApplication.sCurrentScene.equals(AccService.WEBVIEW_ACTIVITY)
                || MainApplication.sCurrentScene.equals(AccService.JSHOP)) {

            appendCommand(Command().commandCode(ServiceCommand.GO_BACK).delay(500))
                    .append(Command().commandCode(ServiceCommand.COLLECT_ITEM)
                            .addScene(AccService.MIAOSHA)
                            .addScene(AccService.WEBVIEW_ACTIVITY)
                            .addScene(AccService.JSHOP)
                            .addScene(AccService.BABEL_ACTIVITY))
            return true
        }
        return super.shouldInterruptSubCollectItem()
    }

    override fun onCollectItemEnd() {
        appendCommand(Command().commandCode(ServiceCommand.COLLECT_TAB))
        super.onCollectItemEnd()
    }

    fun collectTabs(): Int {
        if (clickedTabs.size >= GlobalInfo.BRAND_KILL_TAB) {
            return COLLECT_END
        }
        if (fetchTabs.size > 0) {
            return COLLECT_SUCCESS
        }
        val scrolls = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/id_newproduct_tab")
        if (AccessibilityUtils.isNodesAvalibale(scrolls)) {
            var index = GlobalInfo.SCROLL_COUNT - 5
            do {
                var addResult = false
                val texts = AccessibilityUtils.findChildByClassname(scrolls[0], "android.widget.RadioButton")
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

    fun clickTab(): Boolean {
        while (fetchTabs.size > 0) {
            val item = fetchTabs[0]
            fetchTabs.removeAt(0)
            if (!clickedTabs.contains(item)) {
                val titles = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, item)
                if (AccessibilityUtils.isNodesAvalibale(titles)) {
                    currentTab = item
                    clickedTabs.add(item)
                    val result = titles[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    if (result) {
                        logFile?.writeToFileAppend("点击第${clickedTabs.size}标签：", item)
                        return result
                    }
                }
            }
            logFile?.writeToFileAppend("没找到标签：", item)
        }
        appendCommand(Command().commandCode(ServiceCommand.COLLECT_TAB))
        return false
    }

    override fun beforeLeaveProductDetail() {
        appendCommand(Command().commandCode(ServiceCommand.COLLECT_SUB_ITEM)
                .addScene(AccService.BRAND_MIAOSHA)
                .addScene(AccService.WEBVIEW_ACTIVITY)
                .addScene(AccService.JSHOP)
                .addScene(AccService.BABEL_ACTIVITY))
        super.beforeLeaveProductDetail()
    }

    override fun onCollectSubItemEnd() {
        appendCommand(Command().commandCode(ServiceCommand.GO_BACK).delay(500))
                .append(Command().commandCode(ServiceCommand.COLLECT_ITEM)
                        .addScene(AccService.MIAOSHA)
                        .addScene(AccService.WEBVIEW_ACTIVITY)
                        .addScene(AccService.JSHOP)
                        .addScene(AccService.BABEL_ACTIVITY))
        super.onCollectSubItemEnd()
    }

    override fun clickItem():Boolean {
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
                            val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            if (result) {
                                logFile?.writeToFileAppend("点击第${itemCount+1}主商品：", item.arg1)
                                subItemCount = 0
                                fetchSubItems.clear()
                                clickedSubItems.clear()
                                clickedItems.add(item)
                                appendCommand(Command().commandCode(ServiceCommand.COLLECT_SUB_ITEM)
                                        .addScene(AccService.BRAND_MIAOSHA)
                                        .addScene(AccService.WEBVIEW_ACTIVITY)
                                        .addScene(AccService.JSHOP)
                                        .addScene(AccService.BABEL_ACTIVITY))
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

    override fun collectSubItems(): Int {
        if (subItemCount >= GlobalInfo.BRAND_KILL_COUNT) {
            itemCount++
            return COLLECT_END
        }
        if (fetchSubItems.size > 0) {
            return COLLECT_SUCCESS
        }

        val lists = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")
        if (AccessibilityUtils.isNodesAvalibale(lists)) {
            for (list in lists) {
                var index = 0
                do {
                    val items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/miaosha_brand_inner_title")
                    if (AccessibilityUtils.isNodesAvalibale(items)) {
                        var addResult = false
                        for (item in items) {
                            var title = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/limit_buy_product_item_name"))
                            var price = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_miaosha_price"))
                            var originPrice = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_jd_price"))
                            var hasSalePercent = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/app_limit_buy_stock_text"))

                            if (price != null) {
                                price = price.replace("¥", "")
                            }
                            if (originPrice != null) {
                                originPrice = originPrice.replace("¥", "")
                            }

                            if (title != null && price != null) {
                                if (!clickedSubItems.contains(title)) {
                                    val map = HashMap<String, Any?>()
                                    val row = RowData(map)
                                    row.setDefaultData(env!!)
                                    row.tab = currentTab
                                    row.title = currentItem?.arg1?.replace("\n", "")?.replace(",", "、")
                                    row.subtitle = currentItem?.arg2?.replace("\n", "")?.replace(",", "、")
                                    row.product = title?.replace("\n", "")?.replace(",", "、")
                                    row.price = price?.replace("\n", "")?.replace(",", "、")
                                    row.originPrice = originPrice?.replace("\n", "")?.replace(",", "、")
                                    row.hasSalePercent = hasSalePercent?.replace("\n", "")?.replace(",", "、")
                                    row.biId = GlobalInfo.BRAND_KILL
//                                    row.itemIndex = "${clickedTabs.size}---${itemCount+1}---${set.size}"
//                                    LogUtil.dataCache(row)
                                    fetchSubItems.add(row)
                                    addResult = true

                                    logFile?.writeToFileAppend("待点击商品：${row.product}")

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
            }
            return COLLECT_END
        }

        return COLLECT_FAIL
    }

    override fun fetchSkuid(skuid: String): Boolean {
        if (currentSubItem != null) {
            subItemCount++
            currentSubItem!!.sku = skuid
            currentSubItem!!.itemIndex = "${clickedTabs.size}---${itemCount+1}---${subItemCount}"
            LogUtil.dataCache(currentSubItem!!)
            logFile?.writeToFileAppend("存储商品：${currentSubItem!!.product}")
        }
        return super.fetchSkuid(skuid)
    }

    override fun clickSubItem(): Boolean {
        while (fetchSubItems.size > 0) {
            val item = fetchSubItems.firstOrNull()
            if (item != null && item.product != null) {
                fetchSubItems.remove(item)
                if (!clickedSubItems.contains(item.product!!)) {
                    val titles = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, item.product)
                    if (AccessibilityUtils.isNodesAvalibale(titles)) {
                        val parent = AccessibilityUtils.findParentClickable(titles[0])
                        if (parent != null) {
                            clickedSubItems.add(item.product!!)
                            val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            if (result) {
                                currentSubItem = item
                                appendCommands(getSkuCommands())
                                logFile?.writeToFileAppend("点击第${subItemCount+1}商品：", item.product)
                                return result
                            }
                        }
                    }
                }
                logFile?.writeToFileAppend("没找到未点击商品：", item.product)
            } else {
                break
            }
        }
        appendCommand(Command().commandCode(ServiceCommand.COLLECT_SUB_ITEM))
        return false
    }

    override fun collectItems(): Int {
        if (itemCount >= GlobalInfo.FETCH_NUM) {
            return COLLECT_END
        }
        if (fetchItems.size > 0) {
            return COLLECT_SUCCESS
        }

        val lists = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")
        if (!AccessibilityUtils.isNodesAvalibale(lists)) return COLLECT_FAIL
        val last = lists[lists.size - 1]
        var list = last
        if (lists.size > 1) {
            list = lists[lists.size - 2]
            if (last != null && AccessibilityUtils.getAllText(last).isNotEmpty() && clickedTabs.size > 2 && lists.size == 2) {
                list = last
            }
        }

        logFile?.writeToFileAppend("当前List: ${AccessibilityUtils.getAllText(list)}")

        for (i in lists) {
            logFile?.writeToFileAppend("所有List: ${AccessibilityUtils.getAllText(i)}")
        }

        if (list != null) {
            var index = GlobalInfo.SCROLL_COUNT - 10
            do {
                val brandTitles = list.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/miaosha_brand_title")
                var addResult = false
                if (AccessibilityUtils.isNodesAvalibale(brandTitles)) {
                    for (brand in brandTitles) {
                        val parent = brand.parent
                        if (parent != null) {
                            var title: String? = null
                            if (brand.text != null) {
                                title = brand.text.toString()
                            }

                            val subTitle = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/miaosha_brand_subtitle"))
                            if (title != null) {
                                val entity = Data2(title, subTitle)
                                if (!clickedItems.contains(entity)) {
                                    addResult = fetchItems.add(entity)
                                    if (addResult) {
                                        logFile?.writeToFileAppend("待点击商品：", title, subTitle)
                                    }
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

        if (itemCount < GlobalInfo.FETCH_NUM && retryTime < 3) {
            appendCommand(Command().commandCode(ServiceCommand.COLLECT_ITEM))
            retryTime++
        }
        return COLLECT_FAIL
    }

    var retryTime = 0
}