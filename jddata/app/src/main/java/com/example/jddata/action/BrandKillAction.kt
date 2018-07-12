package com.example.jddata.action

import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.BrandDetail
import com.example.jddata.Entity.BrandEntity
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.BrandSheet
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand
import com.example.jddata.util.LogUtil
import java.util.ArrayList

class BrandKillAction : BaseAction(ActionType.BRAND_KILL) {

    var mBrandEntitys = ArrayList<BrandEntity>()
    var brandTitleStrings = HashSet<String>()
    var scrollIndex = 0

    init {
        appendCommand(Command(ServiceCommand.HOME_BRAND_KILL).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.HOME_BRAND_KILL_SCROLL)
                        .addScene(AccService.MIAOSHA)
                        .concernResult(true))
        sheet = BrandSheet()
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
            ServiceCommand.HOME_BRAND_KILL -> {
                sheet?.writeToSheetAppendWithTime("找到并点击 \"品牌秒杀\"")
                return CommonConmmand.findHomeTextClick(mService!!, "品牌秒杀")
            }
            ServiceCommand.HOME_BRAND_KILL_SCROLL -> {
                val result =  brandKillFetchBrand()
                if (scrollIndex < GlobalInfo.SCROLL_COUNT && command.concernResult && result) {
                    appendCommand(PureCommand(ServiceCommand.BRAND_SELECT))
                            .append(Command(ServiceCommand.BRAND_DETAIL)
                                    .addScene(AccService.BRAND_MIAOSHA)
                                    .addScene(AccService.WEBVIEW_ACTIVITY))
                            .append(PureCommand(ServiceCommand.GO_BACK))
                            .append(Command(ServiceCommand.HOME_BRAND_KILL_SCROLL)
                                    .addScene(AccService.MIAOSHA)
                                    .concernResult(true))
                }
                return result
            }
            ServiceCommand.BRAND_SELECT -> {
                return brandSelect()
            }
            ServiceCommand.BRAND_DETAIL -> {
                return brandDetail(GlobalInfo.SCROLL_COUNT)
            }
        }
        return super.executeInner(command)
    }


    private fun brandDetail(scrollCount: Int): Boolean {
        var nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")
        if (AccessibilityUtils.isNodesAvalibale(nodes)) {
            val list = nodes!![0]
            var index = 0
            val detailList = HashSet<BrandDetail>()
            sheet?.writeToSheetAppend("时间", "位置", "产品", "价格", "原价")
            itemCount = 0
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
                            var price = AccessibilityUtils.getFirstText(prices)

                            val originPrices = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_jd_price")
                            var origin = AccessibilityUtils.getFirstText(originPrices)

                            if (detailList.add(BrandDetail(title, price, origin))) {
                                sheet?.writeToSheetAppendWithTime("第${index + 1}屏", title, price, origin)
                                itemCount++
                                if (itemCount >= GlobalInfo.FETCH_NUM) {
                                    sheet?.writeToSheetAppend("采集够 ${GlobalInfo.FETCH_NUM} 条数据")
                                    LogUtil.writeLog("采集够 ${GlobalInfo.FETCH_NUM} 条数据")
                                    return true
                                }
                            }
                        }
                    }
                    index++
                }
                if (index % 10 == 0) {
                    BusHandler.instance.startCountTimeout()
                }
                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    && index < scrollCount)

            sheet?.writeToSheetAppend("。。。 没有更多数据")
            return true
        }

        nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/a4n")
        // 有关注按钮，是店铺
        if (AccessibilityUtils.isNodesAvalibale(nodes)) {
            nodes = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")
        }
        if (AccessibilityUtils.isNodesAvalibale(nodes)) {
            val list = nodes[0]
            var index = 0
            val detailList = HashSet<BrandDetail>()
            sheet?.writeToSheetAppend("时间", "位置", "产品", "价格", "原价")
            do {
                val titleNodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/a1s")
                if (AccessibilityUtils.isNodesAvalibale(titleNodes)) {
                    for (titleNode in titleNodes) {
                        val parent = AccessibilityUtils.findParentClickable(titleNode)
                        if (parent != null) {
                            val titles = parent.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/a1s")
                            var title = AccessibilityUtils.getFirstText(titles)

                            val prices = parent.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/a1x")
                            var price = AccessibilityUtils.getFirstText(prices)

                            val originPrices = parent.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/a1y")
                            var origin = AccessibilityUtils.getFirstText(originPrices)

                            if (detailList.add(BrandDetail(title, price, origin))) {
                                sheet?.writeToSheetAppendWithTime("第${index + 1}屏", title, price, origin)// 收集100条
                                itemCount++
                                if (itemCount >= GlobalInfo.FETCH_NUM) {
                                    sheet?.writeToSheetAppend("采集够 ${GlobalInfo.FETCH_NUM} 条数据")
                                    LogUtil.writeLog("采集够 ${GlobalInfo.FETCH_NUM} 条数据")
                                    return true
                                }
                            }
                        }
                    }
                }

                val priceTitleNodes = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, "¥")
                if (AccessibilityUtils.isNodesAvalibale(priceTitleNodes)) {
                    for (priceNode in priceTitleNodes) {
                        if (!TextUtils.isEmpty(priceNode.viewIdResourceName)) {
                            val parent = priceNode.parent
                            val textNodes = AccessibilityUtils.findChildByClassname(parent, "android.widget.TextView")
                            if (AccessibilityUtils.isNodesAvalibale(textNodes)) {
                                if (textNodes.size == 2) {
                                    val title = textNodes[0].text?.toString()
                                    val price = textNodes[1].text?.toString()
                                    sheet?.writeToSheetAppendWithTime("第${index + 1}屏", title, price)
                                    itemCount++
                                    if (itemCount >= GlobalInfo.FETCH_NUM) {
                                        sheet?.writeToSheetAppend("采集够 ${GlobalInfo.FETCH_NUM} 条数据")
                                        LogUtil.writeLog("采集够 ${GlobalInfo.FETCH_NUM} 条数据")
                                        return true
                                    }
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

            sheet?.writeToSheetAppend("。。。 没有更多数据")
        }

        return true
    }


    private fun brandSelect(): Boolean {
        if (mBrandEntitys.isNotEmpty()) {
            var nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")

            if (AccessibilityUtils.isNodesAvalibale(nodes)) {
                val list = nodes!![0]

                if (list != null) {
                    val brandEntity = mBrandEntitys.get(0)
                    val title = brandEntity.title
                    val selectNodes = list.findAccessibilityNodeInfosByText(title)
                    if (AccessibilityUtils.isNodesAvalibale(selectNodes)) {
                        val parent = AccessibilityUtils.findParentClickable(selectNodes[0])
                        if (parent != null) {
                            sheet?.writeToSheetAppend("")
                            sheet?.writeToSheetAppendWithTime("找到并点击 $title")
                            sheet?.writeToSheetAppend("时间", "标题", "副标题")
                            sheet?.writeToSheetAppendWithTime(brandEntity.title, brandEntity.subtitle)

                            val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            if (result) {
                                mBrandEntitys.removeAt(0)
                            }
                            return result
                        }
                    }
                }
            }
        }

        return true
    }

    private fun brandKillFetchBrand(): Boolean {
        if (mBrandEntitys.isNotEmpty()) {
            return true
        }

        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false
        val list = nodes!![0]
        if (list != null) {
            do {
                val brandTitles = list.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/miaosha_brand_title")
                for (brand in brandTitles) {
                    val parent = brand.parent
                    if (parent != null) {
                        var title: String? = null
                        if (brand.text != null) {
                            title = brand.text.toString()
                        }

                        val subTitles = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/miaosha_brand_subtitle")
                        var subTitle = AccessibilityUtils.getFirstText(subTitles)
                        if (title != null) {
                            if (brandTitleStrings.add(title)) {
                                // 能成功加进set去，说明之前没有记录
                                mBrandEntitys.add(BrandEntity(title, subTitle))
                            }
                        }
                    }
                }

                if (scrollIndex < GlobalInfo.SCROLL_COUNT && mBrandEntitys.isNotEmpty()) {
                    // 有新的记录，跳出循环
                    return true
                }
                scrollIndex++
                if (scrollIndex % 10 == 0) {
                    BusHandler.instance.startCountTimeout()
                }
                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    && scrollIndex < GlobalInfo.SCROLL_COUNT)

            return true
        }

        return false
    }

}