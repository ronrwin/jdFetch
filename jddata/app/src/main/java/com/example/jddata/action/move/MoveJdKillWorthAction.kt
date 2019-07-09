package com.example.jddata.action.move

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.Entity.ActionType
import com.example.jddata.GlobalInfo
import com.example.jddata.action.BaseAction
import com.example.jddata.action.Command
import com.example.jddata.action.append
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.shelldroid.Env
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.BaseLogFile

open class MoveJdKillWorthAction(env: Env) : BaseAction(env, ActionType.MOVE_JD_KILL_WORTH) {

    init {
        appendCommand(Command().commandCode(ServiceCommand.TEMPLATE_JDKILL))
                .append(Command().commandCode(ServiceCommand.DONE).delay(2000).addScene(AccService.MIAOSHA))
                .append(Command().commandCode(ServiceCommand.MIAOSHA_TAB).delay(2000)
                        .setState(GlobalInfo.MIAOSHA_TAB, GlobalInfo.WORTH_PING))
                .append(Command().commandCode(ServiceCommand.CLICK_WORTH_PRODUCT).delay(2000))
    }

    override fun initLogFile() {
        isMoveAction = true
        logFile = BaseLogFile("动作_京东秒杀_${GlobalInfo.WORTH_PING}")
        var day9No = getState(GlobalInfo.MOVE_NO) as Int
        addMoveExtra("动作： " + day9No)
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
            ServiceCommand.CLICK_WORTH_PRODUCT -> {
                val buyNodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/app_limit_buy_sale_ms_button")
                if (AccessibilityUtils.isNodesAvalibale(buyNodes)) {
                    val buyNode = buyNodes[0]
                    val parent = buyNode.parent
                    if (parent != null) {
                        val titles = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/limit_buy_product_item_name")
                        val prices = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_miaosha_price")

                        val title = AccessibilityUtils.getFirstText(titles)
                        val price = AccessibilityUtils.getFirstText(prices)
                        if (title != null) {
                            val result = buyNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            addMoveExtra("发起团拼：商品：" + title + "，价格：" + price)
                        }
                    }
                }
            }
        }
        return super.executeInner(command)
    }


}
