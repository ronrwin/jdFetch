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

    var mNiceBuyTitles = ArrayList<NiceBuyEntity>()
    var mNiceBuySheet = NiceBuySheet()

    init {
        appendCommand(Command(ServiceCommand.NICE_BUY).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.NICE_BUY_SCROLL).addScene(AccService.WORTHBUY).concernResult(true))
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.NICE_BUY -> {
                return CommonConmmand.findHomeTextClick(mService!!, "会买专辑")
            }
            ServiceCommand.NICE_BUY_SCROLL -> {
                val result = niceBuyScroll(GlobalInfo.SCROLL_COUNT)
                if (command.concernResult) {

                }
                for (i in mNiceBuyTitles) {
                    appendCommand(Command(ServiceCommand.NICE_BUY_SELECT).addScene(AccService.WORTHBUY))
                            .append(Command(ServiceCommand.NICE_BUY_DETAIL).addScene(AccService.INVENTORY))
                            .append(PureCommand(ServiceCommand.GO_BACK))
                }
            }
            ServiceCommand.NICE_BUY_SELECT -> {
                return niceBuySelect(GlobalInfo.SCROLL_COUNT)
            }
            ServiceCommand.NICE_BUY_DETAIL -> {
                return niceBuyDetail(GlobalInfo.SCROLL_COUNT)
            }
        }
        return super.executeInner(command)
    }

    private fun niceBuyDetail(scrollCount: Int): Boolean {
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/recycler_view")
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false
        val list = nodes!![0]
        if (list != null) {
            val descs = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/tv_desc")
            if (AccessibilityUtils.isNodesAvalibale(descs)) {
                val desc = descs!![0]
                if (desc.text != null) {
                    val des = desc.text.toString()
                    mNiceBuySheet.writeToSheetAppend("描述")
                    mNiceBuySheet.writeToSheetAppend(des)
                }
            }

            var index = 0
            val detailList = ArrayList<NiceBuyDetail>()
            do {
                val prices = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/tv_price")
                if (AccessibilityUtils.isNodesAvalibale(prices)) {
                    for (priceNode in prices!!) {
                        val parent = priceNode.parent
                        if (parent != null) {
                            val titles = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_title")
                            var title: String? = null
                            if (AccessibilityUtils.isNodesAvalibale(titles)) {
                                if (titles[0].text != null) {
                                    title = titles[0].text.toString()
                                }
                            }

                            var price: String? = null
                            if (priceNode.text != null) {
                                price = priceNode.text.toString()
                            }

                            val originPrices = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_original_price")
                            var origin: String? = null
                            if (AccessibilityUtils.isNodesAvalibale(originPrices)) {
                                if (originPrices[0].text != null) {
                                    origin = originPrices[0].text.toString()
                                }
                            }
                            detailList.add(NiceBuyDetail(title, price, origin))
                        }
                    }
                }

                index++
                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    && index < scrollCount)

            val finalList = ExecUtils.filterSingle(detailList)
            mNiceBuySheet.writeToSheetAppend("产品", "价格", "原价")
            for ((title, price, origin_price) in finalList) {
                mNiceBuySheet.writeToSheetAppend(title, price, origin_price)
            }
            return true
        }
        return false
    }

    private fun niceBuySelect(scrollCount: Int): Boolean {
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/ll_zdm_inventory_header")
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false
        val list = AccessibilityUtils.findParentByClassname(nodes!![0], "android.support.v7.widget.RecyclerView")

        if (list != null && !mNiceBuyTitles.isEmpty()) {
            var index = 0
            do {
                // 滑回顶部
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD))

            do {
                val niceBuyEntity = mNiceBuyTitles[0]
                val title = niceBuyEntity.title
                val selectNodes = list.findAccessibilityNodeInfosByText(title)
                if (AccessibilityUtils.isNodesAvalibale(selectNodes)) {
                    val parent = AccessibilityUtils.findParentClickable(selectNodes[0])
                    if (parent != null) {
                        mNiceBuySheet.writeToSheetAppend("")
                        mNiceBuySheet.addTitleRow()
                        mNiceBuySheet.writeToSheetAppend(niceBuyEntity.title, niceBuyEntity.desc, niceBuyEntity.pageView, niceBuyEntity.collect)

                        val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        if (result) {
                            mNiceBuyTitles.removeAt(0)
                        }
                        return result
                    }
                }
                index++
            } while (!mNiceBuyTitles.isEmpty()
                    && list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    && index < scrollCount)
        }
        return mNiceBuyTitles.isEmpty()
    }

    /**
     * 会买专辑，下拉抓取标题
     */
    private fun niceBuyScroll(scrollCount: Int): Boolean {
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/ll_zdm_inventory_header")
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false
        val list = AccessibilityUtils.findParentByClassname(nodes!![0], "android.support.v7.widget.RecyclerView")

        if (list != null) {
            var index = 0

            val worthList = ArrayList<NiceBuyEntity>()
            do {
                val descsNodes = list.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_zdm_inventory_desc")
                if (AccessibilityUtils.isNodesAvalibale(descsNodes)) {
                    for (descNode in descsNodes) {
                        val parent = descNode.parent
                        if (parent != null) {
                            val titles = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_zdm_inventory_title")
                            var title: String? = null
                            if (AccessibilityUtils.isNodesAvalibale(titles)) {
                                if (titles[0].text != null) {
                                    title = titles[0].text.toString()
                                }
                            }
                            val descs = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_zdm_inventory_desc")
                            var desc: String? = null
                            if (AccessibilityUtils.isNodesAvalibale(descs)) {
                                if (descs[0].text != null) {
                                    desc = descs[0].text.toString()
                                }
                            }
                            val pageViews = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/page_view")
                            var pageView: String? = null
                            if (AccessibilityUtils.isNodesAvalibale(pageViews)) {
                                if (pageViews[0].text != null) {
                                    pageView = pageViews[0].text.toString()
                                }
                            }
                            val collects = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/text_collect_number")
                            var collect: String? = null
                            if (AccessibilityUtils.isNodesAvalibale(collects)) {
                                if (collects[0].text != null) {
                                    collect = collects[0].text.toString()
                                }
                            }
                            worthList.add(NiceBuyEntity(title, desc, pageView, collect))
                        }
                    }
                }

                index++
                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    && index < scrollCount)

            // 记录标题列表
            mNiceBuyTitles = ExecUtils.filterSingle(worthList)
            return true
        }
        return false
    }
}