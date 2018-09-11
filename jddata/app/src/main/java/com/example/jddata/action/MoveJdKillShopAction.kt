package com.example.jddata.action

import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.MessageDef
import com.example.jddata.Entity.MiaoshaRecommend
import com.example.jddata.Entity.RowData
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.BaseWorkBook
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.LogUtil
import java.util.*

class MoveJdKillShopAction : BaseAction(ActionType.MOVE_JD_KILL_AND_SHOP) {

    init {
        appendCommand(Command(ServiceCommand.HOME_JD_KILL).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.JD_KILL_BUY).addScene(AccService.MIAOSHA))
    }

    var miaoshaRoundTime = ""
    override fun initWorkbook() {
        var date = Date(System.currentTimeMillis())
        var miaoshaTime = if (date.hours % 2 == 0) date.hours else date.hours - 1
        if (miaoshaTime < 6) {
            miaoshaTime = 0
        }
        miaoshaRoundTime = "${miaoshaTime}点"
        workBook = BaseWorkBook("动作_京东秒杀加购商品")
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.HOME_JD_KILL -> {
                workBook?.writeToSheetAppendWithTime("")
                workBook?.writeToSheetAppendWithTime("找到并点击 \"${GlobalInfo.JD_KILL}\"")
                return AccessibilityUtils.performClick(mService, "com.jingdong.app.mall:id/bkt", false);
            }
            ServiceCommand.JD_KILL_BUY -> {
                val result = jdKillBuy()
                if (result) {
                    appendCommand(Command(ServiceCommand.PRODUCT_BUY).addScene(AccService.PRODUCT_DETAIL).delay(8000L))
                }
            }
            ServiceCommand.PRODUCT_BUY -> {
                val result = getBuyProduct()
                if (result) {
                    appendCommand(Command(ServiceCommand.PRODUCT_CONFIRM).addScene(AccService.BOTTOM_DIALOG).canSkip(true))
                    // 如果不进去确定界面，3秒后视为成功
                    BusHandler.instance.sendEmptyMessageDelayed(MessageDef.SUCCESS, 3000L)
                }
                return true
            }
            ServiceCommand.PRODUCT_CONFIRM -> {
                val result = AccessibilityUtils.performClick(mService, "com.jd.lib.productdetail:id/detail_style_add_2_car", false)
                return result
            }
        }
        return super.executeInner(command)
    }

    fun getBuyProduct(): Boolean {
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, "加入购物车")
        if (AccessibilityUtils.isNodesAvalibale(nodes)) {
            for (node in nodes) {
                if (node.isClickable) {
                    return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
            }
        }
        return false
    }

    private fun jdKillBuy(): Boolean {
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false

        val tabs = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/miaosha_tab_text")
        for (tab in tabs!!) {
            if (tab.text != null) {
                val tabText = tab.text.toString()
                if ("抢购中" == tabText) {
                    val parent = tab.parent
                    if (parent != null) {
                        val times = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/miaosha_tab_time")
                        if (AccessibilityUtils.isNodesAvalibale(times) && times[0].text != null) {
                            miaoshaRoundTime = times[0].text.toString()
                            workBook?.writeToSheetAppend("当前秒杀场： ${times[0].text}")
                        }
                    }
                }
            }
        }

        var index = 0
        val miaoshaList = HashSet<MiaoshaRecommend>()
        do {
            val buyNodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/app_limit_buy_sale_ms_button")

            if (AccessibilityUtils.isNodesAvalibale(buyNodes)) {
                for (buyNode in buyNodes!!) {
                    if (buyNode.text != null && "立即抢购".equals(buyNode.text.toString())) {
                        val parent = buyNode.parent
                        if (parent != null) {
                            val titleNodes = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/limit_buy_product_item_name")
                            var product = AccessibilityUtils.getFirstText(titleNodes)

                            val prices = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_miaosha_price")
                            var price = AccessibilityUtils.getFirstText(prices)

                            val originPrices = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_jd_price")
                            var originPrice = AccessibilityUtils.getFirstText(originPrices)

                            if (!TextUtils.isEmpty(product) && !TextUtils.isEmpty(price) && miaoshaList.add(MiaoshaRecommend(product, price, originPrice))) {
                                if (price != null) {
                                    price = price.replace("¥", "")
                                }
                                if (originPrice != null) {
                                    originPrice = originPrice.replace("¥", "")
                                    originPrice = originPrice.replace("京东价", "")
                                }

                                val result = buyNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                if (result) {
                                    workBook?.writeToSheetAppendWithTime("加购商品", product, price, originPrice)
                                    addExtra("加购商品：$product，$price，$originPrice")
                                    return true
                                }
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
        } while (index < GlobalInfo.SCROLL_COUNT &&
                nodes!![0].performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD))

        return true
    }
}