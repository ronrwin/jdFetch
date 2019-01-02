package com.example.jddata.action

import android.text.TextUtils
import android.util.ArraySet
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.*
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.BaseLogFile
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.LogUtil

class FetchHomeAction : BaseAction(ActionType.FETCH_HOME) {

    init {
        appendCommand(Command(ServiceCommand.COLLECT_HOME_ITEM).addScene(AccService.JD_HOME))
    }

    override fun initWorkbook() {
        logFile = BaseLogFile("获取_" + GlobalInfo.HOME)
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.HOME_SCROLL -> {
                return homeRecommendScroll()
            }
            ServiceCommand.COLLECT_HOME_ITEM -> {
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
                            val title = currentItem!!.title
                            val titles = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, title)
                            if (AccessibilityUtils.isNodesAvalibale(titles)) {
                                val title = titles[0]
                                appendCommand(Command(ServiceCommand.GET_SKU).addScene(AccService.PRODUCT_DETAIL).delay(2000))
                                        .append(PureCommand(ServiceCommand.GO_BACK))
                                        .append(Command(ServiceCommand.COLLECT_HOME_ITEM).addScene(AccService.JD_HOME))
                                val parent = AccessibilityUtils.findParentClickable(title)
                                if (parent != null) {
                                    val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                    if (result) {
                                        logFile?.writeToFileAppendWithTime("点击第${itemCount}商品：", currentItem!!.title, currentItem!!.price)
                                        return result
                                    }
                                }
                            }
                        }
                    } else {
                        break
                    }
                }
                appendCommand(PureCommand(ServiceCommand.COLLECT_HOME_ITEM))
                return false
            }
        }
        return super.executeInner(command)
    }

    override fun fetchSkuid(skuid: String): Boolean {
        logFile?.writeToFileAppendWithTime("商品sku：${skuid}")
        itemCount++
        return true
    }

    val fetchItems = LinkedHashSet<HomeRecommend>()
    val clickedItems = LinkedHashSet<HomeRecommend>()
    var currentItem: HomeRecommend? = null

    /**
     * 首页-为你推荐
     */
    fun collectItems(): Int {
        if (itemCount > GlobalInfo.FETCH_NUM) {
            return COLLECT_END
        }
        if (fetchItems.size > 0) {
            return COLLECT_SUCCESS
        }

        val lists = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")

        for (list in lists) {
            var index = 0
            do {
                // 推荐部分
                val items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/c2g")
                if (AccessibilityUtils.isNodesAvalibale(items)) {
                    var addResult = false
                    for (item in items) {
                        val titles = item.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/btx")
                        var product = AccessibilityUtils.getFirstText(titles)
                        if (product != null && product.startsWith("1 ")) {
                            product = product.replace("1 ", "");
                        }

                        val prices = item.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/bty")
                        var price = AccessibilityUtils.getFirstText(prices)

                        if (!TextUtils.isEmpty(product) && !TextUtils.isEmpty(price)) {
                            val recommend = HomeRecommend(product, price)
                            if (!clickedItems.contains(recommend)) {
                                addResult = fetchItems.add(recommend)
                                if (addResult) {
                                    if (price != null) {
                                        price = price.replace("¥", "")
                                    }
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
                if (items != null) {
                    Thread.sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
                } else {
                    Thread.sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP_WAIT)
                }
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    || ExecUtils.fingerScroll() && index < GlobalInfo.SCROLL_COUNT)

            logFile?.writeToFileAppendWithTime(GlobalInfo.NO_MORE_DATA)
            return COLLECT_FAIL
        }
        return COLLECT_FAIL
    }

    /**
     * 首页-为你推荐
     */
    fun homeRecommendScroll(): Boolean {
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")
                ?: return false
        for (node in nodes) {
            var index = 0

            while (node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD));

            val recommendList = HashSet<Recommend>()
            logFile?.writeToFileAppendWithTime("位置", "标题", "价格")
            do {
                // 推荐部分
                val items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/by_")
                if (AccessibilityUtils.isNodesAvalibale(items)) {
                    for (item in items) {
                        val titles = item.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/br2")
                        var product = AccessibilityUtils.getFirstText(titles)
                        if (product != null && product.startsWith("1 ")) {
                            product = product.replace("1 ", "");
                        }

                        val prices = item.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/br3")
                        var price = AccessibilityUtils.getFirstText(prices)

                        if (!TextUtils.isEmpty(product) && !TextUtils.isEmpty(price) && recommendList.add(Recommend(product, price))) {
                            if (price != null) {
                                price = price.replace("¥", "")
                            }
                            logFile?.writeToFileAppendWithTime("${itemCount+1}", product, price)

                            val map = HashMap<String, Any?>()
                            val row = RowData(map)
                            row.setDefaultData()
                            row.product = product.replace("\n", "")?.replace(",", "、")
                            row.price = price
                            row.biId = GlobalInfo.HOME
                            row.itemIndex = "${itemCount+1}"
                            LogUtil.dataCache(row)

                            itemCount++
                            fetchCount++
                            if (itemCount >= GlobalInfo.FETCH_NUM) {
                                logFile?.writeToFileAppendWithTime(GlobalInfo.FETCH_ENOUGH_DATE)
                                return true
                            }
                        }
                    }
                }
                index++
                if (index % 10 == 0) {
                    BusHandler.instance.startCountTimeout()
                }
                Thread.sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while ((node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                            || ExecUtils.fingerScroll())
                    && index < GlobalInfo.SCROLL_COUNT)

            logFile?.writeToFileAppendWithTime(GlobalInfo.NO_MORE_DATA)
            return true
        }
        return false
    }
}