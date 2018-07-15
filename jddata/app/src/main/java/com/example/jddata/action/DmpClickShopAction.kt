package com.example.jddata.action

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.MessageDef
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.BaseWorkBook
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand
import com.example.jddata.util.ExecUtils
import java.util.*

class DmpClickShopAction : BaseAction(ActionType.MOVE_DMP_CLICK_SHOP) {

    var clickedPrice = ArrayList<String>()
    var currentIndex = 0

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
        workBook = BaseWorkBook("dmp广告加购")
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.DMP_CLICK -> {
                val index = command.getState("index")
                workBook?.writeToSheetAppend("")
                workBook?.writeToSheetAppendWithTime("点击 第${index}个广告")
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
                    appendCommand(Command(ServiceCommand.PRODUCT_BUY).addScene(AccService.PRODUCT_DETAIL).delay(8000L))
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

            ServiceCommand.PRODUCT_CONFIRM -> {
                return AccessibilityUtils.performClick(mService, "com.jd.lib.productdetail:id/detail_style_add_2_car", false)
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
                        workBook?.writeToSheetAppendWithTime("加购商品", title, price)
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
                workBook?.writeToSheetAppend("时间", "广告标题")
                workBook?.writeToSheetAppendWithTime(title)
                return true
            } else {
                if (titleNode.className.equals("android.widget.ImageView")) {
                    workBook?.writeToSheetAppend("时间", "广告标题")
                    workBook?.writeToSheetAppendWithTime("京东超市")
                    return true
                }
            }
        }
        return false
    }

    private fun dmpFindPrice(): Boolean {
        var webNodes = AccessibilityUtils.findChildByClassname(mService!!.getRootInActiveWindow(), "android.webkit.WebView")
        if (AccessibilityUtils.isNodesAvalibale(webNodes)) {
            for (webNode in webNodes) {
                if (webNode.contentDescription != null) {
                    workBook?.writeToSheetAppend("网页内容： ${webNode.contentDescription.toString()}")
                }
            }
        }

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
        } else {
            workBook?.writeToSheetAppend("没有可滑动列表")
        }
        return false
    }

}