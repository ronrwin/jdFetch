package com.example.jddata.action

import android.os.Message
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.MiaoshaRecommend
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.MiaoshaSheet
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.ExecUtils
import java.util.ArrayList
import java.util.HashMap

class JdKillAction : BaseAction(ActionType.JD_KILL) {

    init {
        appendCommand(Command(ServiceCommand.HOME_JD_KILL).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.JD_KILL_SCROLL).addScene(AccService.MIAOSHA))
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.HOME_JD_KILL -> {
                return AccessibilityUtils.performClick(mService, "com.jingdong.app.mall:id/bkt", false);
            }
            ServiceCommand.JD_KILL_SCROLL -> {
                return jdKillScroll(GlobalInfo.SCROLL_COUNT)
            }
        }
        return super.executeInner(command)
    }

    private fun jdKillScroll(scrollCount: Int): Boolean {
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false

        var miaoshaTime: String? = null
        val tabs = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/miaosha_tab_text")
        for (tab in tabs!!) {
            if (tab.text != null) {
                val tabText = tab.text.toString()
                if ("抢购中" == tabText) {
                    val parent = tab.parent
                    if (parent != null) {
                        val times = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/miaosha_tab_time")
                        if (AccessibilityUtils.isNodesAvalibale(times) && times[0].text != null) {
                            miaoshaTime = times[0].text.toString().replace(":", "_")
                        }
                    }
                }
            }
        }

        var index = 0

        val miaoshaList = ArrayList<MiaoshaRecommend>()
        do {
            val titles = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/limit_buy_product_item_name")
            if (AccessibilityUtils.isNodesAvalibale(titles)) {
                for (titleNode in titles!!) {
                    val parent = titleNode.parent
                    if (parent != null) {
                        var title: String? = null
                        if (titleNode.text != null) {
                            title = titleNode.text.toString()
                        }
                        val prices = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_miaosha_price")
                        var price: String? = null
                        if (AccessibilityUtils.isNodesAvalibale(prices)) {
                            if (prices[0].text != null) {
                                price = prices[0].text.toString()
                            }
                        }
                        val miaoshaPrices = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_jd_price")
                        var miaoshaPrice: String? = null
                        if (AccessibilityUtils.isNodesAvalibale(miaoshaPrices)) {
                            if (miaoshaPrices[0].text != null) {
                                miaoshaPrice = miaoshaPrices[0].text.toString()
                            }
                        }
                        miaoshaList.add(MiaoshaRecommend(title, price, miaoshaPrice))
                    }
                }
            }
            index++
            sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
        } while (nodes!![0].performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                && index < scrollCount)

        val finalList = ExecUtils.filterSingle(miaoshaList)
        val miaoshaSheet = MiaoshaSheet("京东秒杀_" + miaoshaTime!!)
        for ((title, price, miaoshaPrice) in finalList) {
            miaoshaSheet.writeToSheetAppend(title, price, miaoshaPrice)
        }
        return true
    }
}