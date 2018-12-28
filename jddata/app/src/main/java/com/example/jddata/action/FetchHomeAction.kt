package com.example.jddata.action

import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.HomeRecommend
import com.example.jddata.Entity.Recommend
import com.example.jddata.Entity.RowData
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
                    -1 -> {
                        return false
                    }
                    1 -> {
                        appendCommand(PureCommand(ServiceCommand.CLICK_ITEM))
                        return true
                    }
                }
                return true
            }
            ServiceCommand.CLICK_ITEM -> {
                if (fetchItems.size > 0) {
                    val item = fetchItems.firstOrNull()
                    if (item != null) {
                        currentItem = item
                        fetchItems.remove(item)
                        val nodeinfo = item.nodeInfo
                        val result = nodeinfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        if (result) {
                            clickedItems.add(item)
                            // todo: 数据库写
                        }
                        return result
                    }
                }
                return false
            }
            ServiceCommand.GET_CLIPBOARD -> {
                val text = ExecUtils.getClipBoardText()
                if (currentItem != null) {
                    currentItem!!.sku = text
                    logFile?.writeToFileAppendWithTime("sku: ${text}")
                    return true
                }
            }
        }
        return super.executeInner(command)
    }

    val fetchItems = HashSet<HomeRecommend>()
    val clickedItems = HashSet<HomeRecommend>()
    var currentItem: HomeRecommend? = null

    /**
     * 首页-为你推荐
     */
    fun collectItems(): Int {
        if (fetchItems.size > 0) {
            return 1
        }

        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")
                ?: return -1
        for (node in nodes) {
            logFile?.writeToFileAppendWithTime("位置", "标题", "价格")
            var index = 0
            do {
                // 推荐部分
                val items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/by_")
                if (AccessibilityUtils.isNodesAvalibale(items)) {
                    var addResult = false
                    for (item in items) {
                        val titles = item.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/br2")
                        var product = AccessibilityUtils.getFirstText(titles)
                        if (product != null && product.startsWith("1 ")) {
                            product = product.replace("1 ", "");
                        }

                        val prices = item.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/br3")
                        var price = AccessibilityUtils.getFirstText(prices)

                        if (!TextUtils.isEmpty(product) && !TextUtils.isEmpty(price)) {
                            addResult = fetchItems.add(HomeRecommend(product, price, item, null))
                            if (addResult) {
                                if (price != null) {
                                    price = price.replace("¥", "")
                                }
                                logFile?.writeToFileAppendWithTime("${itemCount + 1}", product, price)
                            }
                        }
                    }
                    if (addResult) {
                        return 1
                    }
                }

                index++
                if (items != null) {
                    Thread.sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
                } else {
                    Thread.sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP_WAIT)
                }
            } while (node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    || ExecUtils.fingerScroll() && index < GlobalInfo.SCROLL_COUNT)

            logFile?.writeToFileAppendWithTime(GlobalInfo.NO_MORE_DATA)
            return -1
        }
        return -1
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