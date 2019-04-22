package com.example.jddata.action.fetch

import android.text.TextUtils
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

class FetchNicebuyAction(env: Env) : BaseAction(env, ActionType.FETCH_NICE_BUY) {
    val fetchTabs = ArrayList<String>()
    val clickedTabs = ArrayList<String>()
    var currentTab: String? = null
    var currentNiceBuyCard: NiceBuyCard? = null
    var currentProduct: Data2? = null
    val clickedProduct = LinkedHashSet<String>()

    init {
        appendCommand(Command().commandCode(ServiceCommand.FIND_TEXT).addScene(AccService.JD_HOME))
                .append(Command().commandCode(ServiceCommand.COLLECT_TAB).addScene(AccService.WORTHBUY).delay(6000))

    }

    var name = GlobalInfo.NICE_BUY
    override fun initLogFile() {
        logFile = BaseLogFile("获取_${name}")
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
                    currentNiceBuyCard = null
                    currentProduct = null
                    clickedProduct.clear()
                    fetchItems.clear()
                    clickedItems.clear()
                    appendCommand(Command().commandCode(ServiceCommand.COLLECT_ITEM).delay(3000))
                }
                return result
            }
            ServiceCommand.FETCH_FIRST_PRODUCT -> {
                if (mLastCommandWindow.equals(AccService.ALBUM_DETAIL_2G)) {
                    appendCommand(Command().commandCode(ServiceCommand.GO_BACK))
                    appendCommand(Command().commandCode(ServiceCommand.COLLECT_ITEM))
                    return false
                }

                val lists = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/recycler_view")
                if (AccessibilityUtils.isNodesAvalibale(lists)) {
                    var index = 0
                    do {
                        val titleNodes = lists[0].findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_title")
                        if (AccessibilityUtils.isNodesAvalibale(titleNodes)) {
                            for (titleNode in titleNodes) {
                                val parent = AccessibilityUtils.findParentClickable(titleNode)
                                if (parent != null) {
                                    val title = titleNode.text?.toString()
                                    val price = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_price"))

                                    if (title != null && !clickedProduct.contains(title)) {
                                        val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                        if (result) {
                                            currentProduct = Data2(title, price)
                                            clickedProduct.add(title!!)
                                            appendCommands(getSkuCommands())
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
                return false
            }
        }
        return super.executeInner(command)
    }

    var hasFetchSku = false
    override fun fetchSkuid(skuid: String): Boolean {
        hasFetchSku = true
        itemCount++
        val map = HashMap<String, Any?>()
        val row = RowData(map)
        row.setDefaultData(env!!)
        row.tab = currentTab
        row.title = currentNiceBuyCard?.title?.replace("\n", "")?.replace(",", "、")
        row.num = currentNiceBuyCard?.num?.replace("\n", "")?.replace(",", "、")
        row.viewdNum = currentNiceBuyCard?.viewdNum?.replace("\n", "")?.replace(",", "、")
        row.likeNum = currentNiceBuyCard?.likeNum?.replace("\n", "")?.replace(",", "、")
        row.fromWhere = currentNiceBuyCard?.fromWhere?.replace("\n", "")?.replace(",", "、")
        row.subtitle = currentNiceBuyCard?.subTitle?.replace("\n", "")?.replace(",", "、")
        row.biId = GlobalInfo.NICE_BUY
        row.product = currentProduct?.arg1?.replace("\n", "")?.replace(",", "、")
        row.price = currentProduct?.arg2?.replace("\n", "")?.replace(",", "、")
        row.sku = skuid
        row.itemIndex = "${itemCount}"
        LogUtil.dataCache(row)

        return super.fetchSkuid(skuid)
    }

    override fun beforeLeaveProductDetail() {
        if (hasFetchSku) {
            appendCommand(Command().commandCode(ServiceCommand.GO_BACK))
            appendCommand(Command().commandCode(ServiceCommand.COLLECT_ITEM))
        } else {
            appendCommand(Command().commandCode(ServiceCommand.FETCH_FIRST_PRODUCT)
                    .addScene(AccService.INVENTORY)
                    .addScene(AccService.INVENTORY_2G)
                    .addScene(AccService.ALBUM_DETAIL_2G))
        }
        super.beforeLeaveProductDetail()
    }

    override fun changeProduct(product: String) {
        if (currentProduct != null) {
            currentProduct!!.arg1 = product.replace(".", "").replace(",", "，")
        }
    }

    val fetchItems = LinkedHashSet<String>()
    val clickedItems = LinkedHashSet<String>()

    override fun collectItems(): Int {
        if (itemCount >= GlobalInfo.FETCH_NUM) {
            return COLLECT_END
        }
        if (fetchItems.size > 0) {
            return COLLECT_SUCCESS
        }

        val lists = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")
        if (AccessibilityUtils.isNodesAvalibale(lists) && lists.size > 1) {
            val list = lists[lists.size - 2]
            logFile?.writeToFileAppend("当前List: ${AccessibilityUtils.getAllText(list)}")

            var index = 0
            do {
                if (collectItemMethod1(list) == COLLECT_SUCCESS) {
                    return COLLECT_SUCCESS
                }

                if (collectItemMethod2(list) == COLLECT_SUCCESS) {
                    return COLLECT_SUCCESS
                }

                index++
            } while (ExecUtils.canscroll(list, index))

            logFile?.writeToFileAppend(GlobalInfo.NO_MORE_DATA)
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
                    val titles = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, item)
                    if (AccessibilityUtils.isNodesAvalibale(titles)) {
                        val parent = AccessibilityUtils.findParentClickable(titles[0])
                        if (parent != null) {
                            clickedItems.add(item)
                            val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            if (result) {
                                appendCommand(Command().commandCode(ServiceCommand.FETCH_FIRST_PRODUCT)
                                        .addScene(AccService.INVENTORY)
                                        .addScene(AccService.INVENTORY_2G)
                                        .addScene(AccService.ALBUM_DETAIL_2G))

                                logFile?.writeToFileAppend("点击第${itemCount+1}商品：", item)
                                return result
                            }
                        }
                    }
                }
                logFile?.writeToFileAppend("没找到未点击商品：", item)
            } else {
                break
            }
        }
        appendCommand(Command().commandCode(ServiceCommand.COLLECT_ITEM))
        return false
    }

    fun collectItemMethod1(list: AccessibilityNodeInfo): Int {
        var titles = list.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_zdm_maintitle")
        if (AccessibilityUtils.isNodesAvalibale(titles)) {
            var addResult = false
            for (titleNode in titles) {
                val parent = AccessibilityUtils.findParentClickable(titleNode)
                if (parent != null) {
                    var title = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_zdm_maintitle"))
                    if (!TextUtils.isEmpty(title)) {
                        currentNiceBuyCard = NiceBuyCard(title, "", "", "", "", "")
                        //  出处列
                        var desc = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/talent_name"))
                        if (desc == null) {
                            //  副标题列
                            desc = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_zdm_subtitle"))
                            currentNiceBuyCard!!.subTitle = desc?.replace("\n", "")?.replace(",", "、")
                        } else {
                            currentNiceBuyCard!!.fromWhere = desc.replace("\n", "")?.replace(",", "、")
                        }
                        val pageView = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/page_view"))
                        //  喜欢数
                        val collect = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/text_collect_number"))

                        currentNiceBuyCard!!.likeNum = collect?.replace("\n", "")?.replace(",", "、")
                        currentNiceBuyCard!!.viewdNum = pageView?.replace("\n", "")?.replace(",", "、")

                        if (!clickedItems.contains(title)) {
                            if (fetchItems.add(title)) {
                                addResult = true
                                logFile?.writeToFileAppend("待点击卡片：${currentNiceBuyCard!!}")
                            }
                        }
                    }
                }
            }

            if (addResult) {
                return COLLECT_SUCCESS
            }
        }
        return COLLECT_FAIL
    }

    fun collectItemMethod2(list: AccessibilityNodeInfo): Int {
        // 第二种
        var titles = list.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_zdm_inventory_title")
        if (AccessibilityUtils.isNodesAvalibale(titles)) {
            var addResult = false
            for (titleNode in titles) {
                val parent = AccessibilityUtils.findParentClickable(titleNode)
                if (parent != null) {
                    var title = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_zdm_inventory_title"))
                    if (!TextUtils.isEmpty(title)) {
                        currentNiceBuyCard = NiceBuyCard(title, "", "", "", "", "")
                        val num = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_zdm_inventory_desc"))
                        val pageView = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/page_view"))
                        val collect = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/text_collect_number"))

                        currentNiceBuyCard!!.num = num?.replace("\n", "")?.replace(",", "、")
                        currentNiceBuyCard!!.viewdNum = pageView?.replace("\n", "")?.replace(",", "、")
                        currentNiceBuyCard!!.likeNum = collect?.replace("\n", "")?.replace(",", "、")

                        if (!clickedItems.contains(title)) {
                            if (fetchItems.add(title)) {
                                addResult = true
                                logFile?.writeToFileAppend("待点击卡片：${currentNiceBuyCard!!}")
                            }
                        }
                    }
                }
            }

            if (addResult) {
                return COLLECT_SUCCESS
            }
        }
        return COLLECT_FAIL
    }


    fun clickTab(): Boolean {
        while (fetchTabs.size > 0) {
            val item = fetchTabs[0]
            fetchTabs.removeAt(0)
            if (!clickedTabs.contains(item)) {
                currentTab = item
                val titles = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, item)
                if (AccessibilityUtils.isNodesAvalibale(titles)) {
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

    fun collectTabs(): Int {
        if (clickedTabs.size >= GlobalInfo.NICE_BUY_COUNT) {
            return COLLECT_END
        }
        if (fetchTabs.size > 0) {
            return COLLECT_SUCCESS
        }
        val scrolls = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/tab")
        if (AccessibilityUtils.isNodesAvalibale(scrolls)) {
            var index = 0
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