package com.example.jddata.action

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.Data2
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.BaseLogFile
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import java.util.*

class MoveJdKillRemindAction : BaseAction(ActionType.MOVE_JD_KILL_REMIND) {

    var mBrandEntitys = ArrayList<Data2>()
    var brandTitleStrings = HashSet<String>()
    var scrollIndex = 0

    init {
        appendCommand(Command(ServiceCommand.HOME_JD_KILL)
                        .addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.JD_FIND_CLICK_COMING)
                        .addScene(AccService.MIAOSHA)
                        .delay(5000L))
                .append(PureCommand(ServiceCommand.JD_FIND_REMIND_ME)
                        .delay(5000L))
    }

    var miaoshaRoundTime = ""
    override fun initLogFile() {
        var date = Date(System.currentTimeMillis())
        var miaoshaTime = if (date.hours % 2 == 0) date.hours else date.hours - 1
        if (miaoshaTime < 6) {
            miaoshaTime = 0
        }
        if (miaoshaTime == 0) {
            miaoshaTime = 6
        } else {
            miaoshaTime = miaoshaTime + 2
            if (miaoshaTime >= 24) {
                miaoshaTime = 0
            }
        }
        miaoshaRoundTime = "${miaoshaTime}"
        logFile = BaseLogFile("动作_京东秒杀提醒_(${miaoshaTime}点)")
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
            ServiceCommand.HOME_JD_KILL -> {
                logFile?.writeToFileAppendWithTime("找到并点击 \"${GlobalInfo.JD_KILL}\"")
                addMoveExtra("找到并点击 \"${GlobalInfo.JD_KILL}\"")
                return AccessibilityUtils.performClick(mService, "com.jingdong.app.mall:id/bkt", false);
            }
            ServiceCommand.JD_FIND_CLICK_COMING -> {
                val result = clickComingPart()
                if (result) {
                    logFile?.writeToFileAppendWithTime("点击即将开始场次：${miaoshaRoundTime}")
                    addMoveExtra("点击即将开始场次：${miaoshaRoundTime}")
                }
                return result
            }
            ServiceCommand.JD_FIND_REMIND_ME -> {
                return remindMe()
            }
        }
        return super.executeInner(command)
    }

    fun clickComingPart(): Boolean {
        val comingNodes = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, "即将开始")
        if (AccessibilityUtils.isNodesAvalibale(comingNodes)) {
            val parent = AccessibilityUtils.findParentClickable(comingNodes[0])
            if (parent != null) {
                return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
        }
        return false
    }

    fun remindMe(): Boolean {
        val remindNodes = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, "提醒我")
        if (AccessibilityUtils.isNodesAvalibale(remindNodes)) {
            val remindNode = remindNodes[0]
            val parent = remindNode.parent
            if (parent != null) {
                val titleNodes = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/limit_buy_product_item_name")
                val priceNodes = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_miaosha_price")
                if (AccessibilityUtils.isNodesAvalibale(titleNodes) && AccessibilityUtils.isNodesAvalibale(priceNodes)) {
                    val title = AccessibilityUtils.getFirstText(titleNodes)
                    val price = AccessibilityUtils.getFirstText(priceNodes)

                    val result = remindNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    if (result) {
                        logFile?.writeToFileAppendWithTime("点击提醒商品：${title}，${price}")
                        addMoveExtra("点击提醒商品：${title}，${price}")
                    }
                    return result
                }
            }
        }
        return false
    }
}