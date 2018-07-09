package com.example.jddata.action

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.BrandDetail
import com.example.jddata.Entity.BrandEntity
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.BrandSheet
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand
import com.example.jddata.util.ExecUtils
import java.util.ArrayList
import java.util.HashMap

class BrandKillAction : BaseAction(ActionType.BRAND_KILL) {

    var mBrandEntitys = ArrayList<BrandEntity>()
    var mBrandSheet: BrandSheet? = null

    init {
        appendCommand(Command(ServiceCommand.HOME_BRAND_KILL).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.HOME_BRAND_KILL_SCROLL)
                        .addScene(AccService.MIAOSHA)
                        .concernResult(true))
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
            ServiceCommand.HOME_BRAND_KILL -> return CommonConmmand.findHomeTextClick(mService!!, "品牌秒杀")
            ServiceCommand.HOME_BRAND_KILL_SCROLL -> {
                val result =  brandKillFetchBrand(GlobalInfo.SCROLL_COUNT)
                if (result) {
                    for (entity in mBrandEntitys) {
                        appendCommand(Command(ServiceCommand.BRAND_SELECT_ALL).addScene(AccService.MIAOSHA))
                                .append(Command(ServiceCommand.BRAND_DETAIL)
                                        .addScene(AccService.BRAND_MIAOSHA)
                                        .addScene(AccService.WEBVIEW_ACTIVITY))
                                .append(PureCommand(ServiceCommand.GO_BACK))
                    }
                }
                return result
            }
            ServiceCommand.BRAND_SELECT_ALL -> {
                return brandSelectAll(GlobalInfo.SCROLL_COUNT)
            }
            ServiceCommand.BRAND_DETAIL -> {
                return brandDetail(GlobalInfo.SCROLL_COUNT)
            }
        }
        return super.executeInner(command)
    }


    private fun brandDetail(scrollCount: Int): Boolean {
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
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) && index < scrollCount)

            val finalList = ExecUtils.filterSingle(detailList)

            if (mBrandSheet != null) {
                mBrandSheet!!.writeToSheetAppend("产品", "价格", "原价")
                for ((title, price, origin_price) in finalList) {
                    mBrandSheet!!.writeToSheetAppend(title, price, origin_price)
                }
            }
            return true
        }
        return false
    }


    private fun brandSelectAll(scrollCount: Int): Boolean {
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false
        val list = nodes!![0]

        if (list != null && !mBrandEntitys.isEmpty()) {
            var index = 0
            do {
                // 滑回顶部
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD))

            do {
                val brandEntity = mBrandEntitys.get(0)
                val title = brandEntity.title

                val selectNodes = list.findAccessibilityNodeInfosByText(title)
                if (AccessibilityUtils.isNodesAvalibale(selectNodes)) {
                    val parent = AccessibilityUtils.findParentClickable(selectNodes[0])
                    if (parent != null) {
                        if (mBrandSheet != null) {
                            mBrandSheet!!.writeToSheetAppend("")
                            mBrandSheet!!.addTitleRow()
                            mBrandSheet!!.writeToSheetAppend(brandEntity.title, brandEntity.subtitle)
                        }
                        val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        if (result) {
                            mBrandEntitys.removeAt(0)
                        }
                        return result
                    }
                }
                index++
                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while (!mBrandEntitys.isEmpty() && list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) && index < scrollCount)
        }
        return mBrandEntitys.isEmpty()
    }

    private fun brandKillFetchBrand(scrollCount: Int): Boolean {
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false
        val list = nodes!![0]
        if (list != null) {
            var index = 0
            val brandList = ArrayList<BrandEntity>()
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
                        var subTitle: String? = null
                        if (AccessibilityUtils.isNodesAvalibale(subTitles)) {
                            if (subTitles[0].text != null) {
                                subTitle = subTitles[0].text.toString()
                            }
                        }
                        brandList.add(BrandEntity(title, subTitle))
                    }
                }
                index++
                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) && index < scrollCount)

            mBrandEntitys = ExecUtils.filterSingle(brandList)
            mBrandSheet = BrandSheet()
            return true
        }

        return false
    }

}