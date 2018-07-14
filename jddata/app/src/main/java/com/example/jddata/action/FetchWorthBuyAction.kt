package com.example.jddata.action

import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.RowData
import com.example.jddata.Entity.WorthBuyEntity
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.WorthBuyWorkBook
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.LogUtil

class FetchWorthBuyAction : BaseAction(ActionType.WORTH_BUY) {

    init {
        appendCommand(Command(ServiceCommand.WORTH_BUY).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.WORTH_BUY_SCROLL).addScene(AccService.WORTHBUY))
    }

    override fun initWorkbook() {
        workBook = WorthBuyWorkBook()
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.WORTH_BUY -> {
                workBook?.writeToSheetAppendWithTime("找到并点击 \"${GlobalInfo.WORTH_BUY}\"")
                return CommonConmmand.findHomeTextClick(mService!!, GlobalInfo.WORTH_BUY)
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
            workBook?.writeToSheetAppend("时间", "位置", "标题", "描述", "收藏数")
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

                    if (!TextUtils.isEmpty(title) && worthList.add(WorthBuyEntity(title, desc, collect))) {
                        workBook?.writeToSheetAppendWithTime("第${index+1}屏", title, desc, collect)

                        val map = HashMap<String, Any?>()
                        val row = RowData(map)
                        row.title = title
                        row.description = desc
                        row.markNum = collect
                        row.actionId = GlobalInfo.WORTH_BUY
                        row.scrollIndex = "第${index+1}屏"
                        LogUtil.writeDataLog(row)
                        hasFetchData = true

                        itemCount++
                        if (itemCount >= GlobalInfo.FETCH_NUM) {
                            workBook?.writeToSheetAppend(GlobalInfo.FETCH_ENOUGH_DATE)
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
                    || ExecUtils.fingerScroll())
                    && index < scrollCount)

            workBook?.writeToSheetAppend(GlobalInfo.NO_MORE_DATA)
            return true
        }
        return false
    }
}