package com.example.jddata.action

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.MiaoshaRecommend
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.MiaoshaSheet
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.LogUtil
import java.text.SimpleDateFormat
import java.util.*

class JdKillAction : BaseAction(ActionType.JD_KILL) {

    init {
        appendCommand(Command(ServiceCommand.HOME_JD_KILL).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.JD_KILL_SCROLL).addScene(AccService.MIAOSHA))

        val time = ExecUtils.getCurrentTimeString(SimpleDateFormat("HH"))
        var date = Date(System.currentTimeMillis())
        val miaoshaTime = if (date.hours % 2 == 0) date.hours else date.hours - 1

        sheet = MiaoshaSheet("京东秒杀_${miaoshaTime}场次")
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.HOME_JD_KILL -> {
                sheet?.writeToSheetAppendWithTime("")
                sheet?.writeToSheetAppendWithTime("找到并点击 \"京东秒杀\"")
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

        val tabs = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/miaosha_tab_text")
        for (tab in tabs!!) {
            if (tab.text != null) {
                val tabText = tab.text.toString()
                if ("抢购中" == tabText) {
                    val parent = tab.parent
                    if (parent != null) {
                        val times = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/miaosha_tab_time")
                        if (AccessibilityUtils.isNodesAvalibale(times) && times[0].text != null) {
                            sheet?.writeToSheetAppend("当前秒杀场： ${times[0].text}")
                        }
                    }
                }
            }
        }

        var index = 0

        sheet?.writeToSheetAppend("时间", "位置", "标题", "秒杀价", "京东价")
        val miaoshaList = HashSet<MiaoshaRecommend>()
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
                        var price = AccessibilityUtils.getFirstText(prices)

                        val miaoshaPrices = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_jd_price")
                        var miaoshaPrice = AccessibilityUtils.getFirstText(miaoshaPrices)

                        if(miaoshaList.add(MiaoshaRecommend(title, price, miaoshaPrice))) {
                            sheet?.writeToSheetAppendWithTime("第${index+1}屏", title, price, miaoshaPrice )
                            itemCount++
                            if (itemCount >= GlobalInfo.FETCH_NUM) {
                                sheet?.writeToSheetAppend("采集够 ${GlobalInfo.FETCH_NUM} 条数据")
                                LogUtil.writeLog("采集够 ${GlobalInfo.FETCH_NUM} 条数据")
                                return true
                            }
                        }
                    }
                }
                index++
                if (index % 10 == 0) {
                    BusHandler.instance.startCountTimeout()
                }
            }
            sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
        } while (index < scrollCount &&
                nodes!![0].performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD))

        sheet?.writeToSheetAppend("。。。 没有更多数据")
        return true
    }
}