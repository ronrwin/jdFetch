package com.example.jddata.action

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.BaseWorkBook
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand
import com.example.jddata.util.ExecUtils
import java.util.*

class DmpShopAction : BaseAction(ActionType.DMP_AND_SHOP) {

    var clickedPrice = ArrayList<String>()

    init {
        for (i in 0..7) {
            appendCommand(Command(ServiceCommand.DMP_CLICK).delay(5000L).addScene(AccService.JD_HOME))
                    .append(Command(ServiceCommand.DMP_FIND_PRICE).delay(5000L)
                            .addScene(AccService.BABEL_ACTIVITY)
                            .addScene(AccService.WEBVIEW_ACTIVITY))
                    .append(PureCommand(ServiceCommand.GO_BACK))
        }
        workBook = BaseWorkBook("dmp广告加购")
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
                    appendCommand(Command(ServiceCommand.PRODUCT_BUY).addScene(AccService.PRODUCT_DETAIL).delay(3000L))
                }
                return result
            }
            ServiceCommand.PRODUCT_BUY -> {
                val nodes = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, "加入购物车")
                if (AccessibilityUtils.isNodesAvalibale(nodes)) {
                    for (node in nodes) {
                        if (node.isClickable) {
                            appendCommand(Command(ServiceCommand.PRODUCT_CONFIRM).addScene(AccService.BOTTOM_DIALOG).canSkip(true))
                            return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        }
                    }
                } else {
                    appendCommand(PureCommand(ServiceCommand.GO_BACK))
                            .append(Command(ServiceCommand.DMP_FIND_PRICE).delay(2000L)
                                    .addScene(AccService.BABEL_ACTIVITY)
                                    .addScene(AccService.WEBVIEW_ACTIVITY))
                }

                return true
            }

            ServiceCommand.PRODUCT_CONFIRM -> {
                return AccessibilityUtils.performClick(mService, "com.jd.lib.productdetail:id/detail_style_add_2_car", false)
            }
        }
        return super.executeInner(command)
    }

    private fun dmpFindPrice(): Boolean {
        var lists = AccessibilityUtils.findChildByClassname(mService!!.getRootInActiveWindow(), "android.support.v7.widget.RecyclerView")

        if (AccessibilityUtils.isNodesAvalibale(lists)) {
            for (list in lists) {
                var index = 0
                val count = 10
                do {
                    val prices = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, "¥")
                    if (AccessibilityUtils.isNodesAvalibale(prices)) {
                        var select = Random().nextInt(prices.size)
                        var selectCount = 0
                        one@ for (price in prices!!) {
                            if (selectCount < select) {
                                continue@one
                            }

                            if (price.text != null) {
                                if (clickedPrice.contains(price.text.toString())) {
                                    continue@one
                                }
                            }

                            if (price.isClickable) {
                                if (price.text != null) {
                                    clickedPrice.add(price!!.text.toString())
                                }
                                return price.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            } else {
                                val parent = AccessibilityUtils.findParentClickable(price)
                                if (parent != null) {
                                    if (price.text != null) {
                                        clickedPrice.add(price!!.text.toString())
                                    }
                                    return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                }
                            }
                        }
                    }
                    index++
                    if (index % 10 == 0) {
                        BusHandler.instance.startCountTimeout()
                    }
                    sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
                } while ((list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                                || ExecUtils.fingerScroll())
                        && index < count)
            }
        }
        return false
    }

}