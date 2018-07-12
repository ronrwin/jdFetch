package com.example.jddata.action

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.WorthBuyEntity
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.WorthBuySheet
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.LogUtil
import java.util.ArrayList

class WorthBuyAction : BaseAction(ActionType.WORTH_BUY) {

    init {
        appendCommand(Command(ServiceCommand.WORTH_BUY).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.WORTH_BUY_SCROLL).addScene(AccService.WORTHBUY))
        sheet = WorthBuySheet()
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.WORTH_BUY -> {
                sheet?.writeToSheetAppendWithTime("找到并点击 \"发现好货\"")
                return CommonConmmand.findHomeTextClick(mService!!, "发现好货")
            }
            ServiceCommand.WORTH_BUY_SCROLL -> {
                return worthBuyScroll(GlobalInfo.SCROLL_COUNT)
            }
        }
        return super.executeInner(command)
    }

    /**
     * 发现好货
     */
    private fun worthBuyScroll(scrollCount: Int): Boolean {
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/product_item")
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false
        val list = AccessibilityUtils.findParentByClassname(nodes!![0], "android.support.v7.widget.RecyclerView")

        if (list != null) {
            sheet?.writeToSheetAppend("时间", "位置", "标题", "描述", "收藏数")
            var index = 0

            val worthList = HashSet<WorthBuyEntity>()
            do {
                val products = list.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/product_item")
                for (product in products) {
                    val titles = product.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/product_name")
                    var title = AccessibilityUtils.getFirstText(titles)
                    if (title != null && title.startsWith("1 ")) {
                        title = title.replace("1 ", "");
                    }

                    val descs = product.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/product_desc")
                    var desc = AccessibilityUtils.getFirstText(descs)

                    val collects = product.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/text_collect_number")
                    var collect = AccessibilityUtils.getFirstText(collects)

                    if (worthList.add(WorthBuyEntity(title, desc, collect))) {
                        sheet?.writeToSheetAppendWithTime("第${index+1}屏", title, desc, collect)
                        itemCount++
                        if (itemCount >= GlobalInfo.FETCH_NUM) {
                            sheet?.writeToSheetAppend("采集够 ${GlobalInfo.FETCH_NUM} 条数据")
                            LogUtil.writeLog("采集够 ${GlobalInfo.FETCH_NUM} 条数据")
                            return true
                        }
                    }
                }
                index++
                if (index % 10 == 0) {
                    BusHandler.instance.startCountTimeout()
                }
                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while ((list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    || ExecUtils.handleExecCommand("input swipe 250 800 250 250"))
                    && index < scrollCount)

            sheet?.writeToSheetAppend("。。。 没有更多数据")
            return true
        }
        return false
    }
}