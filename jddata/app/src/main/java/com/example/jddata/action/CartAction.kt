package com.example.jddata.action

import android.os.Message
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.CartGoods
import com.example.jddata.Entity.Recommend
import com.example.jddata.Entity.SearchRecommend
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.RecommendSheet
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.LogUtil
import java.util.ArrayList
import java.util.HashMap

class CartAction : BaseAction(ActionType.CART) {
    init {
        appendCommand(Command(ServiceCommand.CART_TAB).addScene(AccService.JD_HOME))
                .append(PureCommand(ServiceCommand.CART_SCROLL))

        sheet = RecommendSheet("购物车")
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.CART_TAB -> {
                sheet?.writeToSheetAppendWithTime("点击购物车")
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
            sheet?.writeToSheetAppend("购买部分")
            sheet?.writeToSheetAppend("时间", "位置", "标题", "价格", "数量")
            val buys = HashSet<CartGoods>()
            var buyIndex = 0;
            do {
                // 购买部分
                val buyRecommends = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.cart:id/cart_single_product_item_layout")
                if (AccessibilityUtils.isNodesAvalibale(buyRecommends)) {
                    for (item in buyRecommends) {
                        val titles = item.findAccessibilityNodeInfosByViewId("com.jd.lib.cart:id/cart_single_product_name")
                        var title = AccessibilityUtils.getFirstText(titles)
                        if (title != null && title.startsWith("1 ")) {
                            title = title.replace("1 ", "");
                        }

                        val prices = item.findAccessibilityNodeInfosByViewId("com.jd.lib.cart:id/cart_single_product_price")
                        var price = AccessibilityUtils.getFirstText(prices)

                        val nums = item.findAccessibilityNodeInfosByViewId("com.jd.lib.cart:id/cart_single_product_et_num")
                        var num = AccessibilityUtils.getFirstText(nums)

                        if (buys.add(CartGoods(title, price, num))) {
                            sheet?.writeToSheetAppendWithTime("第${buyIndex+1}屏", title, price, num)
                        }
                    }
                } else {
                    // 没有已购商品，则跳出循环
                    break
                }
                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    || ExecUtils.handleExecCommand("input swipe 250 800 250 250"))


            sheet?.writeToSheetAppend("")
            sheet?.writeToSheetAppend("推荐部分")
            sheet?.writeToSheetAppend("时间", "位置", "标题", "价格")

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
                        var title = AccessibilityUtils.getFirstText(titles)
                        if (title != null && title.startsWith("1 ")) {
                            title = title.replace("1 ", "");
                        }

                        val prices = item.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/br3")
                        var price = AccessibilityUtils.getFirstText(prices)

                        if (recommendList.add(Recommend(title, price))) {
                            sheet?.writeToSheetAppendWithTime("第${index+1}屏", title, price)
                            itemCount++
                            if (itemCount >= GlobalInfo.FETCH_NUM) {
                                sheet?.writeToSheetAppend("采集够 ${GlobalInfo.FETCH_NUM} 条数据")
                                LogUtil.writeLog("采集够 ${GlobalInfo.FETCH_NUM} 条数据")
                                return true
                            }
                        }
                    }
                    index++
                    if (index % 10 == 0) {
                        BusHandler.instance.startCountTimeout()
                    }
                }

                Thread.sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while ((list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                            || ExecUtils.handleExecCommand("input swipe 250 800 250 250"))
                    && index < GlobalInfo.SCROLL_COUNT)

            sheet?.writeToSheetAppend("。。。 没有更多数据")
            return true
        }
        return false
    }

}