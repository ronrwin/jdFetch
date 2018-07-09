package com.example.jddata.action

import android.os.Message
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.CartGoods
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.RecommendSheet
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand
import com.example.jddata.util.ExecUtils
import java.util.ArrayList
import java.util.HashMap

class CartAction : BaseAction(ActionType.CART) {
    init {
        appendCommand(Command(ServiceCommand.CART_TAB).addScene(AccService.JD_HOME))
                .append(PureCommand(ServiceCommand.CART_SCROLL))
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.CART_TAB -> return AccessibilityUtils.performClickByText(mService, "android.widget.FrameLayout", "购物车", false)
            ServiceCommand.CART_SCROLL -> return cartRecommendScroll()
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

        val cartSheet = RecommendSheet("购物车")

        val list = AccessibilityUtils.findParentByClassname(nodes!![0], "android.support.v7.widget.RecyclerView")
        if (list != null) {
            val buys = parseBuyRecommends(list)
            cartSheet.writeToSheetAppend("购买部分")
            cartSheet.writeToSheetAppend("标题", "价格", "数量")
            for (goods in buys) {
                cartSheet.writeToSheetAppend(goods.title, goods.price, goods.num)
            }

            cartSheet.writeToSheetAppend("")
            cartSheet.writeToSheetAppend("推荐部分")
            cartSheet.writeToSheetAppend("标题", "价格")
            val result = CommonConmmand.parseRecommends(mService!!, list, GlobalInfo.SCROLL_COUNT)
            for (i in result.indices) {
                val recommend = result.get(i)
                cartSheet.writeToSheetAppend(recommend.title, recommend.price)
            }
            return true
        }
        return false
    }


    fun parseBuyRecommends(listNode: AccessibilityNodeInfo): ArrayList<CartGoods> {
        val buys = ArrayList<CartGoods>()
        do {
            // 购买部分
            val buyRecommends = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.cart:id/cart_single_product_item_layout")
            if (buyRecommends != null) {
                for (item in buyRecommends) {
                    val titles = item.findAccessibilityNodeInfosByViewId("com.jd.lib.cart:id/cart_single_product_name")
                    var title: String? = null
                    if (AccessibilityUtils.isNodesAvalibale(titles)) {
                        if (titles[0].text != null) {
                            title = titles[0].text.toString()
                            if (title.startsWith("1 ")) {
                                title = title.replace("1 ", "")
                            }
                        }
                    }
                    val prices = item.findAccessibilityNodeInfosByViewId("com.jd.lib.cart:id/cart_single_product_price")
                    var price: String? = null
                    if (AccessibilityUtils.isNodesAvalibale(prices)) {
                        if (prices[0].text != null) {
                            price = prices[0].text.toString()
                        }
                    }

                    val nums = item.findAccessibilityNodeInfosByViewId("com.jd.lib.cart:id/cart_single_product_et_num")
                    var num: String? = null
                    if (AccessibilityUtils.isNodesAvalibale(nums)) {
                        if (nums[0].text != null) {
                            num = nums[0].text.toString()
                        }
                    }
                    buys.add(CartGoods(title, price, num))
                }
            } else {
                break
            }
            sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
        } while (listNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                || ExecUtils.handleExecCommand("input swipe 250 800 250 250"))
        return ExecUtils.filterSingle(buys)
    }

}