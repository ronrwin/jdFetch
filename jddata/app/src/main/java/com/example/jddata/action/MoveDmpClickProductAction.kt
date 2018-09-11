package com.example.jddata.action

import android.app.Activity
import android.content.Intent
import android.view.accessibility.AccessibilityNodeInfo
import android.webkit.WebView
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.GlobalInfo
import com.example.jddata.MainApplication
import com.example.jddata.WebActivity
import com.example.jddata.excel.BaseWorkBook
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand
import com.example.jddata.util.ExecUtils
import java.util.*

class MoveDmpClickProductAction : BaseAction(ActionType.MOVE_DMP_CLICK) {

    var clickedPrice = ArrayList<String>()
    var currentIndex = 0
    var currentTitle = ""

    init {
        appendCommand(PureCommand(ServiceCommand.CAPTURE_SCAN))
                .append(Command(ServiceCommand.SCAN_CLBUM).delay(3000L)
                        .addScene(AccService.CAPTURE_SCAN))
                .append(Command(ServiceCommand.SCAN_PIC).delay(3000L)
                        .addScene(AccService.PHOTO_ALBUM))
                .append(Command(ServiceCommand.DMP_TITLE).delay(10000L)
                        .addScene(AccService.WEBVIEW_ACTIVITY)
                        .addScene(AccService.JSHOP)
                        .addScene(AccService.BABEL_ACTIVITY))
                .append(PureCommand(ServiceCommand.DMP_FIND_PRICE).delay(5000L)
                        .addScene(AccService.WEBVIEW_ACTIVITY)
                        .addScene(AccService.JSHOP)
                        .addScene(AccService.BABEL_ACTIVITY))

//        appendCommand(Command(ServiceCommand.DMP_CLICK).delay(5000L).addScene(AccService.JD_HOME).setState("index", currentIndex))
//                .append(Command(ServiceCommand.DMP_TITLE).delay(10000L)
//                        .addScene(AccService.WEBVIEW_ACTIVITY)
//                        .addScene(AccService.JSHOP)
//                        .addScene(AccService.BABEL_ACTIVITY))
//                .append(PureCommand(ServiceCommand.DMP_FIND_PRICE).delay(5000L)
//                        .addScene(AccService.WEBVIEW_ACTIVITY)
//                        .addScene(AccService.JSHOP)
//                        .addScene(AccService.BABEL_ACTIVITY))
    }

    override fun initWorkbook() {
        workBook = BaseWorkBook("动作_dmp广告点击商品")
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
                    if (currentIndex < 8) {
                        appendCommand(Command(ServiceCommand.DMP_CLICK).delay(5000L).addScene(AccService.JD_HOME).setState("index", currentIndex))
                                .append(Command(ServiceCommand.DMP_TITLE).delay(10000L)
                                        .addScene(AccService.WEBVIEW_ACTIVITY)
                                        .addScene(AccService.JSHOP)
                                        .addScene(AccService.BABEL_ACTIVITY))
                                .append(PureCommand(ServiceCommand.DMP_FIND_PRICE).delay(5000L)
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
                workBook?.writeToSheetAppendWithTime("dmp广告标题：$title")
                currentTitle = title
                return true
            } else {
                if (titleNode.className.equals("android.widget.ImageView")) {
                    workBook?.writeToSheetAppendWithTime("dmp广告标题：京东超市")
                    currentTitle = "京东超市"
                    return true
                }
            }
        }
        return false
    }

    private fun dmpFindPrice(): Boolean {
        var scrollcount = 0
        do {
            val prices = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, "¥")
            if (AccessibilityUtils.isNodesAvalibale(prices)) {
                for (price in prices) {
                    val parent = AccessibilityUtils.findParentClickable(price)
                    if (parent != null) {
                        val title = AccessibilityUtils.getChildTitle(parent)
                        if (title != null) {
                            val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            if (result) {
                                workBook?.writeToSheetAppend("点击商品：${title}，价格：${price.text}")
                                addExtra("dmp广告标题：${currentTitle}，点击商品：${title}，价格：${price.text}")
                                return result
                            }
                        }
                    }
                }
            }

            scrollcount++
        } while (ExecUtils.fingerScroll() && scrollcount < 10)
        workBook?.writeToSheetAppend("没有找到 ¥ 关键字 或 没有多于15个字的商品标题")
        return false
    }

}