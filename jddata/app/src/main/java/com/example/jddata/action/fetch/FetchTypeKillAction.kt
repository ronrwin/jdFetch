package com.example.jddata.action.fetch

import android.view.accessibility.AccessibilityNodeInfo
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

class FetchTypeKillAction(env: Env) : BaseAction(env, ActionType.FETCH_TYPE_KILL) {

    val fetchItems = LinkedHashSet<Data2>()
    val clickedItems = LinkedHashSet<Data2>()
    var currentItem: Data2? = null

    val fetchSubItems = LinkedHashSet<RowData>()
    val clickedSubItems = LinkedHashSet<String>()
    var currentSubItem: RowData? = null

    init {
        appendCommand(Command().commandCode(ServiceCommand.FIND_TEXT).addScene(AccService.JD_HOME))
                .append(Command().commandCode(ServiceCommand.COLLECT_ITEM).addScene(AccService.MIAOSHA))
    }

    override fun initLogFile() {
        logFile = BaseLogFile("获取_" + GlobalInfo.TYPE_KILL)
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.FIND_TEXT -> {
                logFile?.writeToFileAppend("找到并点击 \"${GlobalInfo.TYPE_KILL}\"")
                return findHomeTextClick(GlobalInfo.TYPE_KILL)
            }
        }
        return super.executeInner(command)
    }

    override fun fetchSkuid(skuid: String): Boolean {
        if (currentSubItem != null) {
            subItemCount++
            currentSubItem!!.sku = skuid
            currentSubItem!!.itemIndex = "${itemCount+1}---${subItemCount}"
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

    override fun collectSubItems(): Int {
        if (subItemCount >= GlobalInfo.TYPE_KILL_COUNT) {
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
                            var origin = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_jd_price"))
                            if (title != null && price != null) {
                                price = price.replace("¥", "")
                                if (origin != null) {
                                    origin = origin.replace("¥", "")
                                }
                                if (!clickedSubItems.contains(title)) {
                                    val map = HashMap<String, Any?>()
                                    val row = RowData(map)
                                    row.setDefaultData(env!!)
                                    row.title = currentItem?.arg1?.replace("\n", "")?.replace(",", "、")
                                    row.subtitle = currentItem?.arg2?.replace("\n", "")?.replace(",", "、")
                                    row.product = title?.replace("\n", "")?.replace(",", "、")
                                    row.price = price?.replace("\n", "")?.replace(",", "、")
                                    row.originPrice = origin?.replace("\n", "")?.replace(",", "、")
                                    row.biId = GlobalInfo.TYPE_KILL
//                                    row.itemIndex = "${itemCount+1}---${set.size}"
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
        }

        return COLLECT_FAIL
    }

    override fun beforeLeaveProductDetail() {
        appendCommand(Command().commandCode(ServiceCommand.COLLECT_SUB_ITEM)
                .addScene(AccService.TYPE_MIAOSH_DETAIL)
                .addScene(AccService.WEBVIEW_ACTIVITY)
                .addScene(AccService.BABEL_ACTIVITY))
        super.beforeLeaveProductDetail()
    }

    override fun shouldInterruptSubCollectItem(): Boolean {
        if (MainApplication.sCurrentScene.equals(AccService.BABEL_ACTIVITY)
                || MainApplication.sCurrentScene.equals(AccService.WEBVIEW_ACTIVITY)
                || MainApplication.sCurrentScene.equals(AccService.JSHOP)) {
            return true
        }
        return super.shouldInterruptSubCollectItem()
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
                            val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            if (result) {
                                clickedItems.add(item)

                                subItemCount = 0
                                fetchSubItems.clear()
                                clickedSubItems.clear()

                                appendCommand(Command().commandCode(ServiceCommand.COLLECT_SUB_ITEM)
                                        .addScene(AccService.TYPE_MIAOSH_DETAIL)
                                        .addScene(AccService.WEBVIEW_ACTIVITY)
                                        .addScene(AccService.BABEL_ACTIVITY))
                                currentItem = item
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

    override fun onCollectSubItemEnd() {
        appendCommand(Command().commandCode(ServiceCommand.GO_BACK).delay(500))
                .append(Command().commandCode(ServiceCommand.COLLECT_ITEM)
                        .addScene(AccService.MIAOSHA))
        super.onCollectSubItemEnd()
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
        val list = lists!![0]
        if (list != null) {
            var index = 0
            do {
                val titles = list.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/title1")
                var addResult = false
                if (AccessibilityUtils.isNodesAvalibale(titles)) {
                    for (titleNode in titles) {
                        val parent = titleNode.parent
                        if (parent != null) {
                            var title: String? = null
                            if (titleNode.text != null) {
                                title = titleNode.text.toString()
                            }

                            val subTitle = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/title2"))

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
            return COLLECT_END
        }

        return COLLECT_FAIL
    }
}