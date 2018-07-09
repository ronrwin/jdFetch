package com.example.jddata.action

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.NiceBuyDetail
import com.example.jddata.Entity.NiceBuyEntity
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.NiceBuySheet
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand
import com.example.jddata.util.ExecUtils
import java.util.ArrayList
import java.util.HashMap

class NicebuyAction : BaseAction(ActionType.NICE_BUY) {

    var nicebuyTitles = HashSet<String>()
    var mNiceBuyTitleEntitys = ArrayList<NiceBuyEntity>()
    var scrollIndex = 0

    init {
        appendCommand(Command(ServiceCommand.NICE_BUY).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.NICE_BUY_SCROLL).addScene(AccService.WORTHBUY).concernResult(true))
        sheet = NiceBuySheet()
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.NICE_BUY -> {
                sheet?.writeToSheetAppendWithTime("找到并点击 \"会买专辑\"")
                return CommonConmmand.findHomeTextClick(mService!!, "会买专辑")
            }
            ServiceCommand.NICE_BUY_SCROLL -> {
                val result = niceBuyScroll()
                if (scrollIndex < GlobalInfo.SCROLL_COUNT && command.concernResult && result) {
                    appendCommand(PureCommand(ServiceCommand.NICE_BUY_SELECT))
                            .append(Command(ServiceCommand.NICE_BUY_DETAIL).addScene(AccService.INVENTORY))
                            .append(PureCommand(ServiceCommand.GO_BACK))
                            // 再次找可点击的标题
                            .append(Command(ServiceCommand.NICE_BUY_SCROLL).addScene(AccService.WORTHBUY).concernResult(true))
                }
                return result
            }
            ServiceCommand.NICE_BUY_SELECT -> {
                return niceBuySelect()
            }
            ServiceCommand.NICE_BUY_DETAIL -> {
                return niceBuyDetail()
            }
        }
        return super.executeInner(command)
    }

    private fun niceBuyDetail(): Boolean {
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/recycler_view")
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false
        val list = nodes[0]
        if (list != null) {
            val descs = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/tv_desc")
            if (AccessibilityUtils.isNodesAvalibale(descs)) {
                val desc = descs[0]
                if (desc.text != null) {
                    val des = desc.text.toString()
                    sheet?.writeToSheetAppend("时间", "描述")
                    sheet?.writeToSheetAppendWithTime(des)
                }
            }

            var index = 0
            val detailList = HashSet<NiceBuyDetail>()

            sheet?.writeToSheetAppend("时间", "位置", "产品", "价格", "原价")
            do {
                val prices = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/tv_price")
                if (AccessibilityUtils.isNodesAvalibale(prices)) {
                    for (priceNode in prices!!) {
                        val parent = priceNode.parent
                        if (parent != null) {
                            val titles = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_title")
                            var title = AccessibilityUtils.getFirstText(titles)
                            if (title.startsWith("1 ")) {
                                title = title.replace("1 ", "");
                            }

                            var price: String? = null
                            if (priceNode.text != null) {
                                price = priceNode.text.toString()
                            }

                            val originPrices = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_original_price")
                            var origin = AccessibilityUtils.getFirstText(originPrices)

                            if (detailList.add(NiceBuyDetail(title, price, origin))) {
                                sheet?.writeToSheetAppendWithTime("第${index+1}屏", title, price, origin)
                            }
                        }
                    }
                    index++
                }

                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD))

            return true
        }
        return false
    }

    private fun niceBuySelect(): Boolean {
        if (mNiceBuyTitleEntitys.isNotEmpty()) {
            val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/ll_zdm_inventory_header")
            if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false
            val list = AccessibilityUtils.findParentByClassname(nodes[0], "android.support.v7.widget.RecyclerView")
            if (list != null) {
                val niceBuyEntity = mNiceBuyTitleEntitys[0]
                val title = niceBuyEntity.title
                val selectNodes = list.findAccessibilityNodeInfosByText(title)
                if (AccessibilityUtils.isNodesAvalibale(selectNodes)) {
                    val parent = AccessibilityUtils.findParentClickable(selectNodes[0])
                    if (parent != null) {
                        sheet?.writeToSheetAppend("")
                        sheet?.writeToSheetAppendWithTime("找到并点击 $title")
                        sheet?.writeToSheetAppend("时间", "标题", "数量", "看过数", "收藏数")
                        sheet?.writeToSheetAppendWithTime(niceBuyEntity.title, niceBuyEntity.desc, niceBuyEntity.pageView, niceBuyEntity.collect)

                        val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        if (result) {
                            mNiceBuyTitleEntitys.removeAt(0)
                        }
                        return result
                    }
                }
            }
        }

        return true
    }

    /**
     * 会买专辑，下拉抓取标题
     */
    private fun niceBuyScroll(): Boolean {
        if (mNiceBuyTitleEntitys.isNotEmpty()) {
            return true
        }

        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/ll_zdm_inventory_header")
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false
        val list = AccessibilityUtils.findParentByClassname(nodes!![0], "android.support.v7.widget.RecyclerView")

        if (list != null) {
            do {
                val descsNodes = list.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_zdm_inventory_desc")
                if (AccessibilityUtils.isNodesAvalibale(descsNodes)) {
                    for (descNode in descsNodes) {
                        val parent = descNode.parent
                        if (parent != null) {
                            val titles = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_zdm_inventory_title")
                            var title = AccessibilityUtils.getFirstText(titles)
                            if (title.startsWith("1 ")) {
                                title = title.replace("1 ", "");
                            }

                            val descs = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_zdm_inventory_desc")
                            var desc = AccessibilityUtils.getFirstText(descs)

                            val pageViews = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/page_view")
                            var pageView = AccessibilityUtils.getFirstText(pageViews)

                            val collects = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/text_collect_number")
                            var collect = AccessibilityUtils.getFirstText(collects)

                            val nice = NiceBuyEntity(title, desc, pageView, collect)
                            if (nicebuyTitles.add(title)) {
                                // 能成功加进set去，说明之前没有记录
                                mNiceBuyTitleEntitys.add(nice)
                            }
                        }
                    }
                }

                if (scrollIndex < GlobalInfo.SCROLL_COUNT && mNiceBuyTitleEntitys.isNotEmpty()) {
                    // 有新的记录，跳出循环
                    return true
                }
                scrollIndex++

                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    && scrollIndex < GlobalInfo.SCROLL_COUNT)

            return true
        }
        return false
    }
}