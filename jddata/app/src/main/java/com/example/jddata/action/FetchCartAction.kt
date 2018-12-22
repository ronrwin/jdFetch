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

class FetchCartAction : BaseAction(ActionType.FETCH_CART) {
    init {
        appendCommand(Command(ServiceCommand.CART_TAB).addScene(AccService.JD_HOME))
                .append(PureCommand(ServiceCommand.CART_SCROLL).addScene(AccService.JD_HOME))

    }

    override fun initWorkbook() {
        logFile = BaseLogFile("获取_" + GlobalInfo.CART)
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.CART_TAB -> {
                logFile?.writeToFileAppendWithTime("点击购物车")
                return AccessibilityUtils.performClickByText(mService, "android.widget.FrameLayout", "购物车", false)
            }
            ServiceCommand.CART_SCROLL -> {
                return cartRecommendScroll()
            }
        }
        return super.executeInner(command)
    }


    /**
     * 购物车-为你推荐
     */
    private fun cartRecommendScroll(): Boolean {
        var nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.cart:id/cart_no_login_tip")
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) {
            nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/by_")
        }
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false

        val list = AccessibilityUtils.findParentByClassname(nodes!![0], "android.support.v7.widget.RecyclerView")
        if (list != null) {
            logFile?.writeToFileAppendWithTime("推荐部分")
            logFile?.writeToFileAppendWithTime("位置", "标题", "价格")

            // 滚回最上层
            var index = 0
            while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD));

            val recommendList = HashSet<Recommend>()
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
                            row.product = product.replace("\n", "").replace(",", "、")
                            row.price = price.replace("\n", "").replace(",", "、")
                            row.biId = GlobalInfo.CART
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
            } while ((list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                            || ExecUtils.fingerScroll())
                    && index < GlobalInfo.SCROLL_COUNT)

            logFile?.writeToFileAppendWithTime(GlobalInfo.NO_MORE_DATA)
            return true
        }
        return false
    }

}