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

open class MoveJdKillSaleOutAction(env: Env) : BaseAction(env, ActionType.MOVE_JD_KILL_SALE_OUT) {

    init {
        appendCommand(Command().commandCode(ServiceCommand.TEMPLATE_JDKILL))
                .append(Command().commandCode(ServiceCommand.DONE).delay(2000).addScene(AccService.MIAOSHA))
                .append(Command().commandCode(ServiceCommand.MIAOSHA_TAB).delay(2000)
                        .setState(GlobalInfo.MIAOSHA_TAB, GlobalInfo.SALE_OUT))
                .append(Command().commandCode(ServiceCommand.BUY_QUICK).delay(3000))
                .append(Command().commandCode(ServiceCommand.DONE).delay(7000)
                        .addScene(AccService.PRODUCT_DETAIL))
                .append(Command().commandCode(ServiceCommand.TEMPLATE_ADD_TO_CART).delay(5000))
                .append(Command().commandCode(ServiceCommand.PRODUCT_CONFIRM)
                        .delay(3000).canSkip(true))
    }

    override fun initLogFile() {
        isMoveAction = true
        logFile = BaseLogFile("动作_京东秒杀_${GlobalInfo.SALE_OUT}")
        val tmp = getState(GlobalInfo.MOVE_NO)
        if (tmp != null) {
            val day9No = getState(GlobalInfo.MOVE_NO) as Int
            addMoveExtra("动作：${day9No}")
        }
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
            ServiceCommand.BUY_QUICK -> {
                val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/app_limit_buy_sale_ms_button")
                if (AccessibilityUtils.isNodesAvalibale(nodes)) {
                    val parent = nodes[0].parent
                    if (parent != null) {
                        val title = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/limit_buy_product_item_name"))
                        val price = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_miaosha_price"))
                        val originPrice = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_jd_price"))

                        if (title != null) {
                            val result = nodes[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            if (result) {
                                addMoveExtra("立即抢购商品：${title}，价格：${price}，原价：${originPrice}")
                                return true
                            }
                        }
                    }
                }
                return false
            }
        }
        return super.executeInner(command)
    }


}
