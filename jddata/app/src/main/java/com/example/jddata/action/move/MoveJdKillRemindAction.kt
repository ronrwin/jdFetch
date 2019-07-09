package com.example.jddata.action.move

import android.text.TextUtils
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

open class MoveJdKillRemindAction(env: Env) : BaseAction(env, ActionType.MOVE_JD_KILL_REMIND) {

    init {
        appendCommand(Command().commandCode(ServiceCommand.TEMPLATE_JDKILL))
                .append(Command().commandCode(ServiceCommand.DONE).delay(2000).addScene(AccService.MIAOSHA))
                .append(Command().commandCode(ServiceCommand.JD_KILL_NEXT).delay(3000))
                .append(Command().commandCode(ServiceCommand.REMIND_ME).delay(3000))
    }

    override fun initLogFile() {
        isMoveAction = true
        logFile = BaseLogFile("动作_京东秒杀_提醒我")
        var day9No = getState(GlobalInfo.MOVE_NO) as Int
        addMoveExtra("动作： " + day9No)
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
            ServiceCommand.JD_KILL_NEXT -> {
                val nodes = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "com.jd.lib.jdmiaosha.view.widget.LinearLayoutTab")
                if (AccessibilityUtils.isNodesAvalibale(nodes)) {
                    if (nodes.size > 4) {
                        val nextNodes = nodes[1]
                        val tipName = AccessibilityUtils.getFirstText(nextNodes.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/miaosha_tab_text"))
                        if (tipName != null && tipName.equals("即将开始")) {
                            val nextTitle = AccessibilityUtils.getFirstText(nextNodes.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/miaosha_tab_time"))
                            if (nextTitle != null) {
                                val result = nextNodes.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                if (result) {
                                    addMoveExtra("点击 下一场次 ${nextTitle}")
                                    return true
                                }
                            }
                        }
                    }
                }
                return false
            }
            ServiceCommand.REMIND_ME -> {
                val remindNodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/app_limit_buy_sale_ms_button")
                if (AccessibilityUtils.isNodesAvalibale(remindNodes)) {
                    val parent = remindNodes[0].parent
                    if (parent != null) {
                        val title = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/limit_buy_product_item_name"))
                        val price = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_miaosha_price"))
                        val originPrice = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_jd_price"))
                        if (!TextUtils.isEmpty(title)) {
                            val result = remindNodes[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            if (result) {
                                addMoveExtra("点击 提醒我 商品：${title} 秒杀价格：${price} 原价：${originPrice}")
                            }
                            return result
                        }
                    }
                }
                return false
            }
        }
        return super.executeInner(command)
    }


}
