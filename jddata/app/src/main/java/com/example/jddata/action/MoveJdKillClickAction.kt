package com.example.jddata.action

import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.Data3
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.BaseLogFile
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import java.util.*

class MoveJdKillClickAction : BaseAction(ActionType.MOVE_JD_KILL_CLICK) {

    init {
        appendCommand(Command(ServiceCommand.HOME_JD_KILL).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.JD_KILL_CLICK).addScene(AccService.MIAOSHA))
    }

    var miaoshaRoundTime = ""
    override fun initLogFile() {
        var date = Date(System.currentTimeMillis())
        var miaoshaTime = if (date.hours % 2 == 0) date.hours else date.hours - 1
        if (miaoshaTime < 6) {
            miaoshaTime = 0
        }
        miaoshaRoundTime = "${miaoshaTime}点"
        logFile = BaseLogFile("动作_京东秒杀并点击商品")
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.HOME_JD_KILL -> {
                logFile?.writeToFileAppendWithTime("找到并点击 \"${GlobalInfo.JD_KILL}\"")
                addMoveExtra("找到并点击 \"${GlobalInfo.JD_KILL}\"")
                return AccessibilityUtils.performClick(mService, "com.jingdong.app.mall:id/bkt", false);
            }
            ServiceCommand.JD_KILL_CLICK -> {
                return jdKillClick()
            }
        }
        return super.executeInner(command)
    }

    private fun jdKillClick(): Boolean {
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
                            logFile?.writeToFileAppendWithTime("当前秒杀场： ${times[0].text}")
                            addMoveExtra("当前秒杀场： ${times[0].text}")
                        }
                    }
                }
            }
        }

        var index = 0
        val miaoshaList = HashSet<Data3>()
        do {
            val titles = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/limit_buy_product_item_name")
            if (AccessibilityUtils.isNodesAvalibale(titles)) {
                val selectIndex = Random().nextInt(titles.size)
                var selectCount = 0
                one@for (titleNode in titles!!) {
                    if (selectCount < selectIndex) {
                        selectCount++
                        continue@one
                    }

                    val parent = AccessibilityUtils.findParentClickable(titleNode)
                    if (parent != null) {
                        var product: String? = null
                        if (titleNode.text != null) {
                            product = titleNode.text.toString()
                        }

                        val prices = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_miaosha_price")
                        var price = AccessibilityUtils.getFirstText(prices)

                        val originPrices = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_jd_price")
                        var originPrice = AccessibilityUtils.getFirstText(originPrices)

                        if(!TextUtils.isEmpty(product) && !TextUtils.isEmpty(price) && miaoshaList.add(Data3(product, price, originPrice))) {
                            if (price != null) {
                                price = price.replace("¥", "")
                            }
                            if (originPrice != null) {
                                originPrice = originPrice.replace("¥", "")
                                originPrice = originPrice.replace("京东价", "")
                            }
                            val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            if (result) {
                                logFile?.writeToFileAppendWithTime("点击商品", product, price, originPrice)
                                addMoveExtra("点击商品：$product，$price，$originPrice")
                                return true
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