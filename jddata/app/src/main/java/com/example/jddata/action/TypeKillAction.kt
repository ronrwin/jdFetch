package com.example.jddata.action

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.BrandDetail
import com.example.jddata.Entity.TypeEntity
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.TypeSheet
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand
import com.example.jddata.util.ExecUtils
import java.util.ArrayList
import java.util.HashMap

class TypeKillAction : BaseAction(ActionType.TYPE_KILL) {

    var mTypePrices = ArrayList<TypeEntity>()
    var mTypeSheet = TypeSheet()

    init {
        appendCommand(Command(ServiceCommand.HOME_TYPE_KILL).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.HOME_TYPE_KILL_SCROLL).addScene(AccService.MIAOSHA).concernResult(true))
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.HOME_TYPE_KILL -> {
                return CommonConmmand.findHomeTextClick(mService!!, "品类秒杀")
            }
            ServiceCommand.HOME_TYPE_KILL_SCROLL -> {
                val result = typeKillScroll(GlobalInfo.SCROLL_COUNT)
                for (i in mTypePrices) {
                    appendCommand(Command(ServiceCommand.TYPE_SELECT).addScene(AccService.MIAOSHA))
                            .append(Command(ServiceCommand.TYPE_DETAIl)
                                    .addScene(AccService.TYPE_MIAOSH_DETAIL)
                                    .addScene(AccService.WEBVIEW_ACTIVITY))
                            .append(PureCommand(ServiceCommand.GO_BACK))
                }
                return result
            }
            ServiceCommand.TYPE_SELECT -> {
                return typeSelect(GlobalInfo.SCROLL_COUNT)
            }
            ServiceCommand.TYPE_DETAIl -> {
                return typeDetail(GlobalInfo.SCROLL_COUNT)
            }
        }
        return super.executeInner(command)
    }

    private fun typeDetail(scrollCount: Int): Boolean {
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false
        val list = nodes!![0]
        if (list != null) {
            var index = 0

            val detailList = ArrayList<BrandDetail>()
            do {
                val titles = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/limit_buy_product_item_name")
                if (AccessibilityUtils.isNodesAvalibale(titles)) {
                    for (titleNode in titles!!) {
                        val parent = titleNode.parent
                        if (parent != null) {
                            var title: String? = null
                            if (titleNode.text != null) {
                                title = titleNode.text.toString()
                            }

                            val prices = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_miaosha_price")
                            var price: String? = null
                            if (AccessibilityUtils.isNodesAvalibale(prices)) {
                                if (prices[0].text != null) {
                                    price = prices[0].text.toString()
                                }
                            }

                            val originPrices = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_jd_price")
                            var origin: String? = null
                            if (AccessibilityUtils.isNodesAvalibale(originPrices)) {
                                if (originPrices[0].text != null) {
                                    origin = originPrices[0].text.toString()
                                }
                            }
                            detailList.add(BrandDetail(title, price, origin))
                        }
                    }
                }

                index++
                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    && index < scrollCount)

            val finalList = ExecUtils.filterSingle(detailList)

            mTypeSheet.writeToSheetAppend("")
            mTypeSheet.writeToSheetAppend("产品", "价格", "原价")
            for ((title, price, origin_price) in finalList) {
                mTypeSheet.writeToSheetAppend(title, price, origin_price)
            }
            return true
        }
        return false
    }

    private fun typeSelect(scrollCount: Int): Boolean {
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false
        val list = nodes!![0]

        if (list != null && !mTypePrices.isEmpty()) {
            var index = 0
            do {
                // 滑回顶部
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD))

            do {
                val entity = mTypePrices.get(0)

                val selectNodes = list.findAccessibilityNodeInfosByText(entity.price1)
                val price2Nodes = list.findAccessibilityNodeInfosByText(entity.price2)
                val price3Nodes = list.findAccessibilityNodeInfosByText(entity.price3)

                if (AccessibilityUtils.isNodesAvalibale(selectNodes) && AccessibilityUtils.isNodesAvalibale(price2Nodes) && AccessibilityUtils.isNodesAvalibale(price3Nodes)) {
                    for (price1 in selectNodes) {
                        val parent1 = AccessibilityUtils.findParentClickable(price1)
                        for (price2 in price2Nodes) {
                            val parent2 = AccessibilityUtils.findParentClickable(price2)
                            for (price3 in price3Nodes) {
                                val parent3 = AccessibilityUtils.findParentClickable(price3)
                                if (parent1 != null && parent2 != null && parent3 != null) {
                                    val rect1 = Rect()
                                    val rect2 = Rect()
                                    val rect3 = Rect()
                                    parent1.getBoundsInParent(rect1)
                                    parent2.getBoundsInParent(rect2)
                                    parent3.getBoundsInParent(rect3)
                                    if (rect1.left == rect2.left && rect1.left == rect3.left
                                            && rect1.right == rect2.right && rect1.right == rect3.right
                                            && rect1.top == rect2.top && rect1.top == rect3.top) {
                                        if (mTypeSheet != null) {
                                            mTypeSheet!!.writeToSheetAppend("")
                                        }
                                        val result = parent1.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                        if (result) {
                                            mTypePrices.removeAt(0)
                                        }
                                        return result
                                    }
                                }
                            }
                        }
                    }
                }
                index++
                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while (!mTypePrices.isEmpty()
                    && list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    && index < scrollCount)
        }
        return mTypePrices.isEmpty()
    }

    private fun typeKillScroll(scrollCount: Int): Boolean {
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false
        val list = nodes!![0]
        if (list != null) {
            var index = 0
            // 最多滑几屏
            var maxIndex = scrollCount
            if (maxIndex < 0) {
                maxIndex = 100
            }

            val priceList = ArrayList<TypeEntity>()
            do {
                val prices1 = list.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/brand_item_price1")
                for (price1 in prices1) {
                    val parent = price1.parent
                    if (parent != null) {
                        var title: String? = null
                        if (price1.text != null) {
                            title = price1.text.toString()
                        }

                        val prices2 = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/brand_item_price2")
                        var price2: String? = null
                        if (AccessibilityUtils.isNodesAvalibale(prices2)) {
                            if (prices2[0].text != null) {
                                price2 = prices2[0].text.toString()
                            }
                        }

                        val prices3 = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/brand_item_price3")
                        var price3: String? = null
                        if (AccessibilityUtils.isNodesAvalibale(prices3)) {
                            if (prices3[0].text != null) {
                                price3 = prices3[0].text.toString()
                            }
                        }

                        priceList.add(TypeEntity(title, price2, price3))
                    }
                }
                index++
                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) && index <= maxIndex)

            mTypePrices = ExecUtils.filterSingle(priceList)
            mTypeSheet = TypeSheet()
            return true
        }

        return false
    }
}