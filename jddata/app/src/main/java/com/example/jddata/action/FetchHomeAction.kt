package com.example.jddata.action

import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
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
        appendCommand(Command(ServiceCommand.HOME_SCROLL).addScene(AccService.JD_HOME))
    }

    override fun initWorkbook() {
        logFile = BaseLogFile("获取_" + GlobalInfo.HOME)
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.HOME_SCROLL -> {
                return homeRecommendScroll()
            }
        }
        return super.executeInner(command)
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