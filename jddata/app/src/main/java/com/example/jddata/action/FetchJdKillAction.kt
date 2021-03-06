package com.example.jddata.action

import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.MiaoshaRecommend
import com.example.jddata.Entity.RowData
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.BaseWorkBook
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.LogUtil
import java.util.*

class FetchJdKillAction : BaseAction(ActionType.FETCH_JD_KILL) {

    init {
        appendCommand(Command(ServiceCommand.HOME_JD_KILL).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.JD_KILL_SCROLL).addScene(AccService.MIAOSHA))
    }

    var miaoshaRoundTime = ""
    override fun initWorkbook() {
        var date = Date(System.currentTimeMillis())
        var miaoshaTime = if (date.hours % 2 == 0) date.hours else date.hours - 1
        if (miaoshaTime < 6) {
            miaoshaTime = 0
        }
        miaoshaRoundTime = "${miaoshaTime}点"
        workBook = BaseWorkBook("获取_京东秒杀_($miaoshaRoundTime)场次")
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.HOME_JD_KILL -> {
                workBook?.writeToSheetAppendWithTime("")
                workBook?.writeToSheetAppendWithTime("找到并点击 \"${GlobalInfo.JD_KILL}\"")
                return AccessibilityUtils.performClick(mService, "com.jingdong.app.mall:id/bkt", false);
            }
            ServiceCommand.JD_KILL_SCROLL -> {
                return jdKillScroll(GlobalInfo.SCROLL_COUNT)
            }
        }
        return super.executeInner(command)
    }

    private fun jdKillScroll(scrollCount: Int): Boolean {
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false

        val tabs = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/miaosha_tab_text")
        for (tab in tabs!!) {
            if (tab.text != null) {
                val tabText = tab.text.toString()
                if ("抢购中" == tabText) {
                    val parent = tab.parent
                    if (parent != null) {
                        val times = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/miaosha_tab_time")
                        if (AccessibilityUtils.isNodesAvalibale(times) && times[0].text != null) {
                            miaoshaRoundTime = times[0].text.toString()
                            workBook?.writeToSheetAppend("当前秒杀场： ${times[0].text}")
                        }
                    }
                }
            }
        }

        var index = 0

        workBook?.writeToSheetAppend("时间", "位置", "标题", "秒杀价", "京东价")
        val miaoshaList = HashSet<MiaoshaRecommend>()
        do {
            val titles = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/limit_buy_product_item_name")
            if (AccessibilityUtils.isNodesAvalibale(titles)) {
                for (titleNode in titles!!) {
                    val parent = titleNode.parent
                    if (parent != null) {
                        var product: String? = null
                        if (titleNode.text != null) {
                            product = titleNode.text.toString()
                        }

                        val prices = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_miaosha_price")
                        var price = AccessibilityUtils.getFirstText(prices)


                        val originPrices = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_jd_price")
                        var originPrice = AccessibilityUtils.getFirstText(originPrices)

                        if(!TextUtils.isEmpty(product) && !TextUtils.isEmpty(price) && miaoshaList.add(MiaoshaRecommend(product, price, originPrice))) {
                            if (price != null) {
                                price = price.replace("¥", "")
                            }
                            if (originPrice != null) {
                                originPrice = originPrice.replace("¥", "")
                                originPrice = originPrice.replace("京东价", "")
                            }
                            workBook?.writeToSheetAppendWithTime("${itemCount+1}", product, price, originPrice )

                            val map = HashMap<String, Any?>()
                            val row = RowData(map)
                            row.setDefaultData()
                            row.product = product?.replace("\n", "")?.replace(",", "、")
                            row.price = price
                            row.originPrice = originPrice?.replace("\n", "")?.replace(",", "、")
                            row.jdKillRoundTime = miaoshaRoundTime
                            row.biId = GlobalInfo.JD_KILL
                            row.itemIndex = "${itemCount+1}"
                            LogUtil.writeDataLog(row)

                            itemCount++
                            fetchCount++
                            if (itemCount >= GlobalInfo.FETCH_NUM) {
                                workBook?.writeToSheetAppend(GlobalInfo.FETCH_ENOUGH_DATE)
                                return true
                            }
                        }
                    }
                }
            }
            index++
            if (index % 10 == 0) {
                BusHandler.instance.startCountTimeout()
            }
            sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
        } while (index < scrollCount &&
                nodes!![0].performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD))

        workBook?.writeToSheetAppend(GlobalInfo.NO_MORE_DATA)
        return true
    }
}