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

class DmpClickProductAction : BaseAction(ActionType.MOVE_DMP_CLICK) {

    var clickedPrice = ArrayList<String>()
    var currentIndex = 0
    var currentTitle = ""

    init {
        appendCommand(Command(ServiceCommand.DMP_CLICK).delay(5000L).addScene(AccService.JD_HOME).setState("index", currentIndex))
                .append(Command(ServiceCommand.DMP_TITLE).delay(8000L)
                        .addScene(AccService.WEBVIEW_ACTIVITY)
                        .addScene(AccService.JSHOP)
                        .addScene(AccService.BABEL_ACTIVITY))
                .append(PureCommand(ServiceCommand.DMP_FIND_PRICE)
                        .addScene(AccService.WEBVIEW_ACTIVITY)
                        .addScene(AccService.JSHOP)
                        .addScene(AccService.BABEL_ACTIVITY))
    }

    override fun initWorkbook() {
        workBook = BaseWorkBook("dmp广告点击商品")
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
                } else {
                    appendCommand(PureCommand(ServiceCommand.GO_BACK))
                    currentIndex++
                    if (currentIndex < 16) {
                        appendCommand(Command(ServiceCommand.DMP_CLICK).delay(5000L).addScene(AccService.JD_HOME).setState("index", currentIndex))
                                .append(Command(ServiceCommand.DMP_TITLE).delay(8000L)
                                        .addScene(AccService.WEBVIEW_ACTIVITY)
                                        .addScene(AccService.JSHOP)
                                        .addScene(AccService.BABEL_ACTIVITY))
                                .append(PureCommand(ServiceCommand.DMP_FIND_PRICE)
                                        .addScene(AccService.WEBVIEW_ACTIVITY)
                                        .addScene(AccService.JSHOP)
                                        .addScene(AccService.BABEL_ACTIVITY))
                    }
                }
                return result
            }
            ServiceCommand.DMP_TITLE -> {
                return dmpTitle()
            }
        }
        return super.executeInner(command)
    }


    fun dmpTitle(): Boolean {
        var nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/ff")
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) {
            nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jshop:id/jshop_shopname")
        }
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) {
            nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/ab7")
        }

        if (AccessibilityUtils.isNodesAvalibale(nodes)) {
            val titleNode = nodes!![0]
            if (titleNode.text != null) {
                val title = titleNode.text.toString()
                workBook?.writeToSheetAppend("时间", "广告标题")
                workBook?.writeToSheetAppendWithTime(title)
                currentTitle = title
                return true
            } else {
                if (titleNode.className.equals("android.widget.ImageView")) {
                    workBook?.writeToSheetAppend("时间", "广告标题")
                    workBook?.writeToSheetAppendWithTime("京东超市")
                    currentTitle = "京东超市"
                    return true
                }
            }
        }
        return false
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
            workBook?.writeToSheetAppend("没有找到 ¥ 关键字")
        }
        return false
    }

}