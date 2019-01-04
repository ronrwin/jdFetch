package com.example.jddata.action.move

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.MessageDef
import com.example.jddata.MainApplication
import com.example.jddata.action.BaseAction
import com.example.jddata.action.Command
import com.example.jddata.action.PureCommand
import com.example.jddata.action.append
import com.example.jddata.util.BaseLogFile
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand
import com.example.jddata.util.ExecUtils
import java.util.*

class MoveDmpClickShopAction : BaseAction(ActionType.MOVE_DMP_CLICK_SHOP) {

    var clickedPrice = ArrayList<String>()
    var currentIndex = 0
    var currentTitle = ""

    init {
        MainApplication.copyPic("haifeisi.png")

        appendCommand(PureCommand(ServiceCommand.CAPTURE_SCAN))
                .append(Command(ServiceCommand.SCAN_CLBUM).delay(3000L)
                        .addScene(AccService.CAPTURE_SCAN))
                .append(Command(ServiceCommand.SCAN_PIC).delay(3000L)
                        .addScene(AccService.PHOTO_ALBUM))
                .append(Command(ServiceCommand.DMP_TITLE).delay(8000L)
                        .addScene(AccService.WEBVIEW_ACTIVITY)
                        .addScene(AccService.JSHOP)
                        .addScene(AccService.BABEL_ACTIVITY))
                .append(PureCommand(ServiceCommand.DMP_FIND_PRICE)
                        .addScene(AccService.WEBVIEW_ACTIVITY)
                        .addScene(AccService.JSHOP)
                        .addScene(AccService.BABEL_ACTIVITY))

//        appendCommand(Command(ServiceCommand.DMP_CLICK).delay(5000L).addScene(AccService.JD_HOME).setState("index", currentIndex))
//                .append(Command(ServiceCommand.DMP_TITLE).delay(8000L)
//                        .addScene(AccService.WEBVIEW_ACTIVITY)
//                        .addScene(AccService.JSHOP)
//                        .addScene(AccService.BABEL_ACTIVITY))
//                .append(PureCommand(ServiceCommand.DMP_FIND_PRICE)
//                        .addScene(AccService.WEBVIEW_ACTIVITY)
//                        .addScene(AccService.JSHOP)
//                        .addScene(AccService.BABEL_ACTIVITY))
    }

    override fun initLogFile() {
        logFile = BaseLogFile("动作_dmp广告加购")
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.DMP_CLICK -> {
                val index = getState("index")
                logFile?.writeToFileAppendWithTime("点击 第${index}个广告")
                return CommonConmmand.dmpclick(mService!!)
            }
            ServiceCommand.DMP_FIND_PRICE -> {
                val result = dmpFindPrice()
                if (result) {
//                    // 成功点击，保留当前任务，后面的清掉，重新构建后面的序列。
//                    val current = getCurrentCommand()
//                    mCommandArrayList.clear()
//                    if (current != null) {
//                        appendCommand(current)
//                    }
                    appendCommand(Command(ServiceCommand.PRODUCT_BUY).addScene(AccService.PRODUCT_DETAIL).delay(8000L))
                } else {
                    appendCommand(PureCommand(ServiceCommand.GO_BACK))
//                    currentIndex++
//                    if (currentIndex < 8) {
//                        appendCommand(Command(ServiceCommand.DMP_CLICK).delay(5000L).addScene(AccService.JD_HOME).setState("index", currentIndex))
//                                .append(Command(ServiceCommand.DMP_TITLE).delay(8000L)
//                                        .addScene(AccService.WEBVIEW_ACTIVITY)
//                                        .addScene(AccService.JSHOP)
//                                        .addScene(AccService.BABEL_ACTIVITY))
//                                .append(PureCommand(ServiceCommand.DMP_FIND_PRICE)
//                                        .addScene(AccService.WEBVIEW_ACTIVITY)
//                                        .addScene(AccService.JSHOP)
//                                        .addScene(AccService.BABEL_ACTIVITY))
//                    }
                }
                return result
            }
            ServiceCommand.DMP_TITLE -> {
                return dmpTitle()
            }
            ServiceCommand.PRODUCT_BUY -> {
                val result = getBuyProduct()
                if (result) {
                    appendCommand(Command(ServiceCommand.PRODUCT_CONFIRM).addScene(AccService.BOTTOM_DIALOG).canSkip(true))
                    // 如果不进去确定界面，3秒后视为成功
                    BusHandler.instance.sendEmptyMessageDelayed(MessageDef.SUCCESS, 3000L)
                } else {
                    appendCommand(PureCommand(ServiceCommand.GO_BACK))
                            .append(Command(ServiceCommand.DMP_FIND_PRICE).delay(2000L)
                                    .addScene(AccService.BABEL_ACTIVITY)
                                    .addScene(AccService.JSHOP)
                                    .addScene(AccService.WEBVIEW_ACTIVITY))
                }

                return result
            }
        }
        return super.executeInner(command)
    }

    fun getBuyProduct(): Boolean {
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, "加入购物车")
        if (AccessibilityUtils.isNodesAvalibale(nodes)) {
            for (node in nodes) {
                if (node.isClickable) {
                    val titleNodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.productdetail:id/detail_desc_description")

                    var priceNodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.productdetail:id/detail_price")
                    if (!AccessibilityUtils.isNodesAvalibale(priceNodes)) {
                        priceNodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.productdetail:id/lib_pd_jx_plusprice")
                    }
                    if (!AccessibilityUtils.isNodesAvalibale(priceNodes)) {
                        priceNodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.productdetail:id/pd_top_miaosha_price")
                    }
                    if (AccessibilityUtils.isNodesAvalibale(titleNodes) && AccessibilityUtils.isNodesAvalibale(priceNodes)) {
                        val title = AccessibilityUtils.getFirstText(titleNodes)
                        val price = AccessibilityUtils.getFirstText(priceNodes)
                        logFile?.writeToFileAppendWithTime("加购商品", title, price)
                        addMoveExtra("加购商品：${title}，${price}")
                    }

                    return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
            }
        }
        return false
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
                logFile?.writeToFileAppendWithTime(title)
                currentTitle = title
                return true
            } else {
                if (titleNode.className.equals("android.widget.ImageView")) {
                    logFile?.writeToFileAppendWithTime("京东超市")
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
                                logFile?.writeToFileAppendWithTime("点击商品：${title}，价格：${price.text}")
                                addMoveExtra("dmp广告标题：${currentTitle}，点击商品：${title}，价格：${price.text}")
                                return result
                            }
                        }
                    }
                }
            }

            scrollcount++
        } while (ExecUtils.fingerScroll() && scrollcount < 10)
        logFile?.writeToFileAppendWithTime("没有找到 ¥ 关键字 或 没有多于15个字的商品标题")
        return false
    }

}