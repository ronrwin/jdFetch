package com.example.jddata.action

import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.CartGoods
import com.example.jddata.Entity.Recommend
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.RecommendWorkBook
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.ExecUtils

class CartAction : BaseAction(ActionType.CART) {
    init {
        appendCommand(Command(ServiceCommand.CART_TAB).addScene(AccService.JD_HOME))
                .append(PureCommand(ServiceCommand.CART_SCROLL))

        workBook = RecommendWorkBook("购物车")
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.CART_TAB -> {
                workBook?.writeToSheetAppendWithTime("点击购物车")
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
            workBook?.writeToSheetAppend("购买部分")
            workBook?.writeToSheetAppend("时间", "位置", "标题", "价格", "数量")
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

                        if (!TextUtils.isEmpty(title) && buys.add(CartGoods(title, price, num))) {
                            workBook?.writeToSheetAppendWithTime("第${buyIndex+1}屏", title, price, num)
                        }
                    }
                } else {
                    // 没有已购商品，则跳出循环
                    break
                }
                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    || ExecUtils.fingerScroll())


            workBook?.writeToSheetAppend("")
            workBook?.writeToSheetAppend("推荐部分")
            workBook?.writeToSheetAppend("时间", "位置", "标题", "价格")

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

                        if (!TextUtils.isEmpty(title) && recommendList.add(Recommend(title, price))) {
                            workBook?.writeToSheetAppendWithTime("第${index+1}屏", title, price)
                            itemCount++
                            if (itemCount >= GlobalInfo.FETCH_NUM) {
                                workBook?.writeToSheetAppend(GlobalInfo.FETCH_ENOUGH_DATE)
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

            workBook?.writeToSheetAppend(GlobalInfo.NO_MORE_DATA)
            return true
        }
        return false
    }

}