package com.example.jddata.action

import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.*
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.BaseLogFile
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.LogUtil
import java.util.ArrayList

class FetchNicebuyAction : BaseAction(ActionType.FETCH_NICE_BUY) {

    var nicebuyTitles = HashSet<String>()
    var mNiceBuyTitleEntitys = ArrayList<NiceBuyEntity>()
    var scrollIndex = 0
    var isEnd = false

    init {
        appendCommand(Command(ServiceCommand.NICE_BUY).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.COLLECT_ITEM).addScene(AccService.WORTHBUY).concernResult(true).delay(6000L))

    }

    override fun initLogFile() {
        logFile = BaseLogFile("获取_" + GlobalInfo.NICE_BUY)
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.NICE_BUY -> {
                logFile?.writeToFileAppendWithTime("找到并点击 \"${GlobalInfo.NICE_BUY}\"")
                return CommonConmmand.findHomeTextClick(mService!!, GlobalInfo.NICE_BUY)
            }
            ServiceCommand.COLLECT_ITEM -> {
                val resultCode = collectItems()
                when (resultCode) {
                    COLLECT_FAIL -> {
                        return false
                    }
                    COLLECT_END -> {
                        return true
                    }
                    COLLECT_SUCCESS -> {
                        appendCommand(PureCommand(ServiceCommand.CLICK_ITEM))
                        return true
                    }
                }
                return true
            }
            ServiceCommand.CLICK_ITEM -> {
                while (fetchItems.size > 0) {
                    val item = fetchItems.firstOrNull()
                    if (item != null) {
                        fetchItems.remove(item)
                        val addToClicked = clickedItems.add(item)
                        if (addToClicked) {
                            currentItem = item
                            val title = currentItem!!.arg1
                            val titles = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, title)
                            if (AccessibilityUtils.isNodesAvalibale(titles)) {
                                val parent = AccessibilityUtils.findParentClickable(titles[0])
                                if (parent != null) {
                                    appendCommand(Command(ServiceCommand.NICE_BUY_DETAIL).addScene(AccService.INVENTORY).delay(5000L))
                                            .append(PureCommand(ServiceCommand.GO_BACK))
                                            .append(Command(ServiceCommand.COLLECT_ITEM).addScene(AccService.WORTHBUY))
                                    val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                    if (result) {
                                        logFile?.writeToFileAppendWithTime("点击第${itemCount+1}商品：", currentItem!!.arg1, currentItem!!.arg2, currentItem!!.arg3, currentItem!!.arg4)
                                        return result
                                    }
                                }
                            }
                        }
                    } else {
                        break
                    }
                }
                appendCommand(PureCommand(ServiceCommand.COLLECT_ITEM))
                return false
            }
            ServiceCommand.NICE_BUY_DETAIL -> {
                val fetchNum = 0
                val result = getDetail()
                if (result > 0) {
                    itemCount++
                }
                return fetchNum > 0
            }
        }
        return super.executeInner(command)
    }

    val fetchItems = LinkedHashSet<Data4>()
    val clickedItems = LinkedHashSet<Data4>()
    var currentItem: Data4? = null

    fun collectItems(): Int {
        if (itemCount >= GlobalInfo.NICEBUY_COUNT) {
            return COLLECT_END
        }
        if (fetchItems.size > 0) {
            return COLLECT_SUCCESS
        }

        val lists = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")

        for (list in lists) {
            var index = 0
            do {
                val titles = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/tv_zdm_inventory_title")
                if (AccessibilityUtils.isNodesAvalibale(titles)) {
                    var addResult = false
                    for (titleNode in titles) {
                        val parent = AccessibilityUtils.findParentClickable(titleNode)
                        if (parent != null) {
                            var title = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_zdm_inventory_title"))
                            val desc = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_zdm_inventory_desc"))
                            val pageView = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/page_view"))
                            val collect = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/text_collect_number"))

                            if (!TextUtils.isEmpty(title)) {
                                val recommend = Data4(title, desc, pageView, collect)
                                if (!clickedItems.contains(recommend)) {
                                    addResult = fetchItems.add(recommend)
                                    if (addResult) {
                                        logFile?.writeToFileAppendWithTime("待点击商品：", title, desc, pageView, collect)

                                        if (itemCount >= GlobalInfo.NICEBUY_COUNT) {
                                            return COLLECT_SUCCESS
                                        }
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
            } while (ExecUtils.canscroll(list, index))

            logFile?.writeToFileAppendWithTime(GlobalInfo.NO_MORE_DATA)
            return COLLECT_FAIL
        }
        return COLLECT_FAIL
    }

    private fun getDetail(): Int {
        var fetchNum = 0

        val lists = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/recycler_view")
        if (AccessibilityUtils.isNodesAvalibale(lists)) {
            var index = 0
            val set = HashSet<Data3>()
            do {
                val desc = AccessibilityUtils.getFirstText(AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/tv_desc"))
                logFile?.writeToFileAppendWithTime("描述: ${desc}")

                var titles = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/tv_title")
                if (AccessibilityUtils.isNodesAvalibale(titles)) {
                    for (titleNode in titles) {
                        val parent = AccessibilityUtils.findParentClickable(titleNode)
                        if (parent != null) {
                            val title = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_title"))
                            var price = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_price"))
                            var origin = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_original_price"))

                            if (title != null && price != null) {
                                if (price != null) {
                                    price = price.replace("¥", "")
                                }
                                if (origin != null) {
                                    origin = origin.replace("¥", "")
                                }

                                if (set.add(Data3(title, price, origin))) {
                                    // todo: 写数据库

                                    fetchNum++
                                    logFile?.writeToFileAppendWithTime("${fetchNum}", title, price, origin)
                                    if (fetchNum >= GlobalInfo.FETCH_NUM) {
                                        return fetchNum
                                    }
                                }
                            }
                        }
                    }
                }

                index++
                Thread.sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while (ExecUtils.canscroll(lists[0], index))
        }
        return fetchNum
    }

    private fun niceBuyDetail(): Int {
        var fetchNum = 0
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/recycler_view")
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return fetchNum
        val list = nodes[0]
        var description = ""
        if (list != null) {
            val descs = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/tv_desc")
            if (AccessibilityUtils.isNodesAvalibale(descs)) {
                val desc = descs[0]
                if (desc.text != null) {
                    val des = desc.text.toString()
                    logFile?.writeToFileAppendWithTime("描述")
                    logFile?.writeToFileAppendWithTime(des)
                    description = des
                }
            }

            var index = 0
            val detailList = HashSet<NiceBuyDetail>()

            logFile?.writeToFileAppendWithTime("位置", "产品", "价格", "原价", "标题", "描述", "数量", "看过数", "收藏数")
            do {
                val prices = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/tv_price")
                if (AccessibilityUtils.isNodesAvalibale(prices)) {
                    for (priceNode in prices!!) {
                        val parent = priceNode.parent
                        if (parent != null) {
                            var product = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_title"))
                            if (product != null && product.startsWith("1 ")) {
                                product = product.replace("1 ", "");
                            }

                            var price: String? = null
                            if (priceNode.text != null) {
                                price = priceNode.text.toString()
                            }

                            var origin = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_original_price"))

                            if (!TextUtils.isEmpty(product) && !TextUtils.isEmpty(price) && detailList.add(NiceBuyDetail(product, price, origin)) && currentNiceBuyEntity != null) {
                                if (price != null) {
                                    price = price.replace("¥", "")
                                }
                                if (origin != null) {
                                    origin = origin.replace("¥", "")
                                }
                                logFile?.writeToFileAppendWithTime("${itemCount+1}", product, price, origin, currentNiceBuyEntity!!.title, description, currentNiceBuyEntity!!.desc, currentNiceBuyEntity!!.pageView, currentNiceBuyEntity!!.collect)

                                val map = HashMap<String, Any?>()
                                val row = RowData(map)
                                row.setDefaultData()
                                row.product = ExecUtils.translate(product)
                                row.price = ExecUtils.translate(price)
                                row.originPrice = ExecUtils.translate(origin)
                                row.description = ExecUtils.translate(description)
                                row.title = ExecUtils.translate(currentNiceBuyEntity!!.title)
                                row.num = ExecUtils.translate(currentNiceBuyEntity!!.desc)
                                row.viewdNum = ExecUtils.translate(currentNiceBuyEntity!!.pageView)
                                row.markNum = ExecUtils.translate(currentNiceBuyEntity!!.collect)
                                row.biId = GlobalInfo.NICE_BUY
                                row.itemIndex = "${itemCount+1}"
                                LogUtil.dataCache(row)

                                itemCount++
                                fetchCount++
                                if (itemCount >= GlobalInfo.FETCH_NUM) {
                                    logFile?.writeToFileAppendWithTime(GlobalInfo.FETCH_ENOUGH_DATE)
                                    return fetchNum
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
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) &&
                    index < GlobalInfo.SCROLL_COUNT)

            logFile?.writeToFileAppendWithTime(GlobalInfo.NO_MORE_DATA)
            return fetchNum
        }
        return fetchNum
    }

    var currentNiceBuyEntity: NiceBuyEntity? = null
}