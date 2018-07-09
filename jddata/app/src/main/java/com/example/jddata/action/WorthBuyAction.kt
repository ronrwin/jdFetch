package com.example.jddata.action

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.WorthBuyEntity
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.WorthBuySheet
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand
import com.example.jddata.util.ExecUtils
import java.util.ArrayList

class WorthBuyAction : BaseAction(ActionType.WORTH_BUY) {

    var worthSheet = WorthBuySheet()

    init {
        appendCommand(Command(ServiceCommand.WORTH_BUY).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.WORTH_BUY_SCROLL).addScene(AccService.WORTHBUY))
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.WORTH_BUY -> {
                return CommonConmmand.findHomeTextClick(mService!!, "发现好货")
            }
            ServiceCommand.WORTH_BUY_SCROLL -> {
                val result = worthBuyScroll(GlobalInfo.SCROLL_COUNT)
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
            var index = 0

            val worthList = ArrayList<WorthBuyEntity>()
            do {
                val products = list.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/product_item")
                for (product in products) {
                    val titles = product.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/product_name")
                    var title: String? = null
                    if (AccessibilityUtils.isNodesAvalibale(titles)) {
                        if (titles[0].text != null) {
                            title = titles[0].text.toString()
                        }
                    }
                    val descs = product.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/product_desc")
                    var desc: String? = null
                    if (AccessibilityUtils.isNodesAvalibale(descs)) {
                        if (descs[0].text != null) {
                            desc = descs[0].text.toString()
                        }
                    }
                    val collects = product.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/text_collect_number")
                    var collect: String? = null
                    if (AccessibilityUtils.isNodesAvalibale(collects)) {
                        if (collects[0].text != null) {
                            collect = collects[0].text.toString()
                        }
                    }
                    worthList.add(WorthBuyEntity(title, desc, collect))
                }
                index++
                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    && index < scrollCount)

            val finalList = ExecUtils.filterSingle(worthList)
            for ((title, desc, collect) in finalList) {
                worthSheet.writeToSheetAppend(title, desc, collect)
            }
            return true
        }
        return false
    }
}