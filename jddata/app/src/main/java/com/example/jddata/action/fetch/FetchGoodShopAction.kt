package com.example.jddata.action.fetch

import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.*
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

class FetchGoodShopAction(env: Env) : BaseAction(env, ActionType.FETCH_GOOD_SHOP) {

    val tabSub = 0
    val fetchTabs = ArrayList<String>()
    val clickedTabs = ArrayList<String>()
    var currentTab: String? = null

    val fetchItems = ArrayList<Data4>()
    val clickedItems = ArrayList<String>()
    var currentItem: Data4? = null

    init {
        appendCommand(Command().commandCode(ServiceCommand.FIND_TEXT).addScene(AccService.JD_HOME))
                .append(Command().commandCode(ServiceCommand.COLLECT_TAB)
                        .addScene(AccService.BABEL_ACTIVITY).delay(3000L))
    }

    val name = GlobalInfo.GOOD_SHOP
    override fun initLogFile() {
        logFile = BaseLogFile("获取_$name")
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.FIND_TEXT -> {
                logFile?.writeToFileAppend("找到并点击 $name")
                return findHomeTextClick(name)
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
                    itemCount = 0
                    fetchItems.clear()
                    clickedItems.clear()

                    appendCommand(Command().commandCode(ServiceCommand.COLLECT_ITEM))
                }
                return result
            }
            ServiceCommand.CLICK_RECT -> {
                if (!MainApplication.sCurrentScene.equals(AccService.BABEL_ACTIVITY)) {
                    appendCommand(Command().commandCode(ServiceCommand.GO_BACK))
                    appendCommand(Command().commandCode(ServiceCommand.CLICK_RECT))
                    return false
                }
                val result =  clickRect()
                if (!result) {
                    appendCommand(Command().commandCode(ServiceCommand.COLLECT_ITEM))
                }
                return result
            }
            ServiceCommand.GO_BACK -> {
                // 再列表页就阻断回退
                if (MainApplication.sCurrentScene.equals(AccService.BABEL_ACTIVITY)) {
                    return true
                }
            }
        }
        return super.executeInner(command)
    }

    override fun onCollectItemFail() {
        super.onCollectItemFail()
    }

    val rects = ArrayList<Rect>()
    val clickedRects = ArrayList<Rect>()

    fun clickRect(): Boolean {
        if (subItemCount >= GlobalInfo.GOOD_SHOP_COUNT) {
            return false
        }
        while (rects.size > 0) {
            val rect = rects.firstOrNull()
            if (rect != null) {
                rects.remove(rect)
                if (!clickedRects.contains(rect)) {
                    clickedRects.add(rect)
                    ExecUtils.handleExecCommand("input tap ${rect.left + 10} ${rect.top + 10}")
                    appendCommands(getSkuCommands())
                    logFile?.writeToFileAppend("点击第${itemCount}商品：")
                    return true
                }
            } else{
                break
            }
        }
        return false
    }

    override fun clickItem(): Boolean {
        while (fetchItems.size > 0) {
            val item = fetchItems.firstOrNull()
            if (item != null) {
                fetchItems.remove(item)
                if (!clickedItems.contains(item.arg1)) {
                    val titles = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, item.arg1)
                    if (AccessibilityUtils.isNodesAvalibale(titles)) {
                        val parent = AccessibilityUtils.findParentClickable(titles[0])
                        if (parent != null) {
                            clickedItems.add(item!!.arg1!!)
                            currentItem = item
                            if (parent.className.equals("android.widget.LinearLayout")
                                    && parent.childCount > 1) {
                                val imageNodess = AccessibilityUtils.findChildByClassname(parent, "android.widget.ImageView")
                                if (imageNodess.size == 3) {
                                    itemCount++
                                    subItemCount = 0
                                    clickedRects.clear()
                                    rects.clear()
                                    one@for (imageNode in imageNodess) {
                                        val rect = Rect()
                                        imageNode.getBoundsInScreen(rect)
                                        if (rect.top < GlobalInfo.height *5/6 && rect.top > GlobalInfo.height /6) {
                                            rects.add(rect)
                                            if (rects.size >= GlobalInfo.GOOD_SHOP_COUNT) {
                                                break@one
                                            }
                                        }
                                    }
                                    appendCommand(Command().commandCode(ServiceCommand.CLICK_RECT).delay(200))
                                    return true
//                                    for (imageNode in imageNodess) {
//                                        val rect = Rect()
//                                        imageNode.getBoundsInScreen(rect)
//                                        val width = rect.right - rect.left
//                                        val height = rect.bottom - rect.top
//                                        if (width == height && width > 0) {
//                                            Log.d("zfr", "${rect}")
//                                            ExecUtils.handleExecCommand("input tap ${rect.left + 10} ${rect.top + 10}")
//                                            appendCommands(getSkuCommands())
//                                            logFile?.writeToFileAppend("点击第${itemCount + 1}商品：", item.arg1)
//                                            return true
//                                        } else {
//                                            if (width > 650 * GlobalInfo.width / 1080) {
//                                                Log.d("zfr", "${rect}")
//                                                ExecUtils.handleExecCommand("input tap ${rect.left + 10} ${rect.top + 10}")
//                                                appendCommands(getSkuCommands())
//                                                logFile?.writeToFileAppend("点击第${itemCount + 1}商品：", item.arg1)
//                                                return true
//                                            }
//                                        }
//                                    }
                                }
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

    override fun shouldInterruptCollectItem(): Boolean {
        if (MainApplication.sCurrentScene.equals(AccService.PRODUCT_DETAIL)) {
            appendCommand(Command().commandCode(ServiceCommand.GO_BACK))
                    .append(Command().commandCode(ServiceCommand.COLLECT_ITEM))
            return true
        }
        return super.shouldInterruptCollectItem()
    }

    override fun beforeLeaveProductDetail() {
        appendCommand(Command().commandCode(ServiceCommand.CLICK_RECT)
                .addScene(AccService.BABEL_ACTIVITY)
                .addScene(AccService.PRODUCT_DETAIL)
                .addScene(AccService.WEBVIEW_ACTIVITY)
                .addScene(AccService.JSHOP)
                .delay(1000))
        super.beforeLeaveProductDetail()
    }

    override fun changeProduct(product: String) {
        if (currentItem != null) {
            currentItem!!.arg3 = product
        }
        super.changeProduct(product)
    }

    override fun changePrice(price: String) {
        if (currentItem != null) {
            currentItem!!.arg4 = price
        }
        super.changePrice(price)
    }

    override fun fetchSkuid(skuid: String): Boolean {
//        itemCount++
        subItemCount++

        val map = HashMap<String, Any?>()
        val row = RowData(map)
        row.setDefaultData(env!!)
        row.shop = currentItem?.arg1?.replace("\n", "")?.replace(",", "、")
        row.markNum = currentItem?.arg2?.replace("\n", "")?.replace(",", "、")
        row.product = currentItem?.arg3?.replace("\n", "")?.replace(",", "、")
        row.price = currentItem?.arg4?.replace("\n", "")?.replace(",", "、")
        row.biId = GlobalInfo.GOOD_SHOP
        row.tab = currentTab
        row.itemIndex = "${clickedTabs.size}---${itemCount}---${subItemCount}"
        LogUtil.dataCache(row)
        logFile?.writeToFileAppend("存储${row.itemIndex}商品：${map}")
        return super.fetchSkuid(skuid)
    }

    override fun onCollectItemEnd() {
        appendCommand(Command().commandCode(ServiceCommand.COLLECT_TAB))
    }

    override fun collectItems(): Int {
        if (itemCount >= GlobalInfo.FETCH_NUM) {
            return COLLECT_END
        }
        if (fetchItems.size > 0) {
            return COLLECT_SUCCESS
        }

        val lists = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")
        if (AccessibilityUtils.isNodesAvalibale(lists)) {
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

            var index = GlobalInfo.SCROLL_COUNT - 10
            do {
                val items = list.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/a7_")
                if (AccessibilityUtils.isNodesAvalibale(items)) {
                    var addResult = false
                    for (item in items) {
                        val titleNodes = item.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/a7c")
                        var title = AccessibilityUtils.getFirstText(titleNodes)
                        var markNum = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/a7g"))
                        if (title != null) {
                            val rect = Rect()
                            titleNodes[0].getBoundsInScreen(rect)
                            if (rect.bottom < GlobalInfo.height * 4 / 5 && rect.top < GlobalInfo.height * 4 / 5) {
                                val data = Data4(title, markNum, "", "")
                                if (!clickedItems.contains(title)) {
                                    addResult = fetchItems.add(data)
                                    retryTime = 0
                                    if (addResult) {
                                        logFile?.writeToFileAppend("待点击商店：${data}")
                                    }
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
        }

        if (itemCount < GlobalInfo.FETCH_NUM && retryTime < 3) {
            appendCommand(Command().commandCode(ServiceCommand.COLLECT_ITEM))
            retryTime++
        }

        return COLLECT_FAIL
    }

    var retryTime = 0

    fun clickTab(): Boolean {
        while (fetchTabs.size > 0) {
            var item = fetchTabs.removeAt(0)
            if (!clickedTabs.contains(item)) {
                val titles = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, item)
                if (AccessibilityUtils.isNodesAvalibale(titles)) {
                    clickedTabs.add(item)
                    val result = titles[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    if (result) {
                        currentTab = item
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

    fun collectTabs(): Int {
        if (clickedTabs.size >= GlobalInfo.GOOD_SHOP_TAB) {
            return COLLECT_END
        }
        if (fetchTabs.size > 0) {
            return COLLECT_SUCCESS
        }
        val scrolls = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/a2n")
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