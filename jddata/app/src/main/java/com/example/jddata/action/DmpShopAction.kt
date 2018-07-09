package com.example.jddata.action

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.Entity.ActionType
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand
import com.example.jddata.util.ExecUtils

class DmpShopAction : BaseAction(ActionType.DMP_AND_SHOP) {

    init {
        for (i in 0..7) {
            appendCommand(Command(ServiceCommand.DMP_CLICK).delay(5000L).addScene(AccService.JD_HOME))
                    .append(Command(ServiceCommand.DMP_FIND_PRICE).delay(3000L).concernResult(true)
                            .addScene(AccService.BABEL_ACTIVITY)
                            .addScene(AccService.WEBVIEW_ACTIVITY))
                    .append(PureCommand(ServiceCommand.GO_BACK))
        }
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.DMP_CLICK -> {
                return CommonConmmand.dmpclick(mService!!)
            }
            ServiceCommand.DMP_FIND_PRICE -> {
                val result = dmpFindPrice()
                if (result) {
                    // 成功点击，保留当前任务，后面的清掉，重新构建后面的序列。
                    val current = getCurrentCommand()
                    mCommandArrayList.clear()
                    if (current != null) {
                        appendCommand(current)
                    }
                    appendCommand(Command(ServiceCommand.PRODUCT_BUY).addScene(AccService.PRODUCT_DETAIL))
                            .append(Command(ServiceCommand.PRODUCT_CONFIRM).addScene(AccService.BOTTOM_DIALOG).canSkip(true))
                }
                return result
            }
            ServiceCommand.PRODUCT_BUY -> {
                return AccessibilityUtils.performClick(mService, "com.jd.lib.productdetail:id/pd_invite_friend", false)
            }
            ServiceCommand.PRODUCT_CONFIRM -> {
                return AccessibilityUtils.performClick(mService, "com.jd.lib.productdetail:id/detail_style_add_2_car", false)
            }
        }
        return super.executeInner(command)
    }

    private fun dmpFindPrice(): Boolean {
        val lists = AccessibilityUtils.findChildByClassname(mService!!.getRootInActiveWindow(), "android.support.v7.widget.RecyclerView")
        if (AccessibilityUtils.isNodesAvalibale(lists)) {
            val list = lists[0]
            var index = 0
            val count = 10
            do {
                val prices = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, "¥")
                if (AccessibilityUtils.isNodesAvalibale(prices)) {
                    for (price in prices!!) {
                        if (price.isClickable) {
                            return price.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        } else {
                            val parent = AccessibilityUtils.findParentClickable(price)
                            if (parent != null) {
                                return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            }
                        }
                    }
                }
                index++
            } while ((list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) || ExecUtils.handleExecCommand("input swipe 250 800 250 250")) && index < count)
        }
        return false
    }

}