package com.example.jddata.action

import android.graphics.Rect
import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.BrandDetail
import com.example.jddata.Entity.RowData
import com.example.jddata.Entity.TypeEntity
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.BaseWorkBook
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.LogUtil
import java.util.ArrayList

class FetchTypeKillAction : BaseAction(ActionType.FETCH_TYPE_KILL) {

    var titleStrings = HashSet<String>()
    var mEntitys = ArrayList<TypeEntity>()
    var scrollIndex = 0
    var isEnd = false

    init {
        appendCommand(Command(ServiceCommand.HOME_TYPE_KILL).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.HOME_TYPE_KILL_SCROLL)
                        .addScene(AccService.MIAOSHA)
                        .concernResult(true))
    }

    override fun initWorkbook() {
        workBook = BaseWorkBook("获取_" + GlobalInfo.TYPE_KILL)
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.HOME_TYPE_KILL -> {
                workBook?.writeToSheetAppendWithTime("找到并点击 \"${GlobalInfo.TYPE_KILL}\"")
                return CommonConmmand.findHomeTextClick(mService!!, GlobalInfo.TYPE_KILL)
            }
            ServiceCommand.HOME_TYPE_KILL_SCROLL -> {
                val result = typeKillScroll()
                if (scrollIndex < GlobalInfo.SCROLL_COUNT && command.concernResult && result) {
                    appendCommand(PureCommand(ServiceCommand.TYPE_SELECT).addScene(AccService.MIAOSHA))
                            .append(Command(ServiceCommand.TYPE_DETAIl)
                                    .addScene(AccService.TYPE_MIAOSH_DETAIL)
                                    .addScene(AccService.WEBVIEW_ACTIVITY))
                            .append(PureCommand(ServiceCommand.GO_BACK))
                            .append(Command(ServiceCommand.HOME_TYPE_KILL_SCROLL)
                                    .addScene(AccService.MIAOSHA)
                                    .concernResult(true))
                }
                if (isEnd) {
                    mCommandArrayList.clear()
                    appendCommand(command!!)
                    return true
                }

                return result
            }
            ServiceCommand.TYPE_SELECT -> {
                return typeSelect()
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

            val detailList = HashSet<BrandDetail>()
            workBook?.writeToSheetAppend("时间", "位置", "产品", "价格", "原价")
            itemCount = 0
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
                            var origin = AccessibilityUtils.getFirstText(originPrices)

                            if (!TextUtils.isEmpty(product) && !TextUtils.isEmpty(price) && detailList.add(BrandDetail(product, price, origin))) {
                                if (price != null) {
                                    price = price.replace("¥", "")
                                }
                                if (origin != null) {
                                    origin = origin.replace("¥", "")
                                }
                                workBook?.writeToSheetAppendWithTime("${itemCount+1}", product, price, origin)

                                val map = HashMap<String, Any?>()
                                val row = RowData(map)
                                row.setDefaultData()
                                row.product = product?.replace("\n", "")?.replace(",", "、")
                                row.price = price.replace("\n", "")?.replace(",", "、")
                                row.originPrice = origin?.replace("\n", "")?.replace(",", "、")
                                row.biId = GlobalInfo.TYPE_KILL
                                row.itemIndex = "${itemCount+1}"
                                LogUtil.writeDataLog(row)

                                itemCount++
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
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    && index < scrollCount)

            workBook?.writeToSheetAppend(GlobalInfo.NO_MORE_DATA)
            return true
        }
        return false
    }


    var clickProductCount = 0
    private fun typeSelect(): Boolean {
        if (mEntitys.isNotEmpty()) {
            val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")
            if (AccessibilityUtils.isNodesAvalibale(nodes)) {
                val list = nodes!![0]

                if (list != null) {
                    val entity = mEntitys.get(0)

                    val price1Nodes = list.findAccessibilityNodeInfosByText(entity.price1)
                    val price2Nodes = list.findAccessibilityNodeInfosByText(entity.price2)
                    val price3Nodes = list.findAccessibilityNodeInfosByText(entity.price3)

                    if (AccessibilityUtils.isNodesAvalibale(price1Nodes)
                            && AccessibilityUtils.isNodesAvalibale(price2Nodes)
                            && AccessibilityUtils.isNodesAvalibale(price3Nodes)) {
                        for (price1 in price1Nodes) {
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
                                        if (rect1 == rect2 && rect1 == rect3) {
                                            workBook?.writeToSheetAppend("")
                                            clickProductCount++
                                            workBook?.writeToSheetAppendWithTime("找到并点击 第${clickProductCount}个商品，价格${price1.text}, ${price2.text}, ${price3.text}")
                                            val result = parent1.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                            if (result) {
                                                mEntitys.removeAt(0)
                                            }
                                            return result
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // 没找到就抛掉继续跑
                        mEntitys.removeAt(0)
                        return typeSelect()
                    }
                }
            }
        }

        return false
    }

    private fun typeKillScroll(): Boolean {
        if (mEntitys.isNotEmpty()) {
            return true
        }

        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false
        val list = nodes!![0]
        if (list != null) {
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

                        if (title != null) {
                            if (titleStrings.add(title)) {
                                // 能成功加进set去，说明之前没有记录
                                mEntitys.add(TypeEntity(title, price2, price3))
                            }
                        }
                    }
                }

                if (scrollIndex < GlobalInfo.SCROLL_COUNT && mEntitys.isNotEmpty()) {
                    // 有新的记录，跳出循环
                    return true
                }
                scrollIndex++

                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while (ExecUtils.fingerScroll()
                    && scrollIndex <= GlobalInfo.SCROLL_COUNT)
            isEnd = true
        }

        return false
    }
}