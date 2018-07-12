package com.example.jddata.action

import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.BrandDetail
import com.example.jddata.Entity.BrandEntity
import com.example.jddata.Entity.MessageDef
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.BaseWorkBook
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand
import com.example.jddata.util.ExecUtils
import java.util.*
import kotlin.collections.HashSet

class BrandKillShopAction : BaseAction(ActionType.BRAND_KILL_AND_SHOP) {

    var mBrandEntitys = ArrayList<BrandEntity>()
    init {
        appendCommand(Command(ServiceCommand.HOME_BRAND_KILL).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.HOME_BRAND_KILL_SCROLL)
                        .addScene(AccService.MIAOSHA)
                        .concernResult(true))
        workBook = BaseWorkBook("品牌秒杀并加购")
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.HOME_BRAND_KILL -> {
                return CommonConmmand.findHomeTextClick(mService!!, "品牌秒杀")
            }
            ServiceCommand.HOME_BRAND_KILL_SCROLL -> {
                val result = brandKillFetchBrand()
                if (result) {
                    appendCommand(PureCommand(ServiceCommand.BRAND_SELECT_RANDOM).addScene(AccService.MIAOSHA))
                            .append(Command(ServiceCommand.BRAND_DETAIL_RANDOM_SHOP)
                                    .addScene(AccService.BRAND_MIAOSHA)
                                    .addScene(AccService.WEBVIEW_ACTIVITY)
                                    .addScene(AccService.BABEL_ACTIVITY))
                }
                return result

            }
            ServiceCommand.BRAND_SELECT_RANDOM -> {
                val result = brandSelectRandom()
                return result
            }
            ServiceCommand.BRAND_DETAIL_RANDOM_SHOP -> {
//                val result = brandDetail()
                if (mLastCommandWindow == AccService.WEBVIEW_ACTIVITY
                        || mLastCommandWindow == AccService.BABEL_ACTIVITY) {
                    appendCommand(PureCommand(ServiceCommand.GO_BACK))
                    appendCommand(Command(ServiceCommand.BRAND_SELECT_RANDOM).addScene(AccService.MIAOSHA))
                            .append(Command(ServiceCommand.BRAND_DETAIL_RANDOM_SHOP)
                                    .addScene(AccService.BRAND_MIAOSHA)
                                    .addScene(AccService.WEBVIEW_ACTIVITY)
                                    .addScene(AccService.BABEL_ACTIVITY))
                } else {
                    val result = brandDetailRandomShop()
                    if (result) {
                        appendCommand(Command(ServiceCommand.PRODUCT_BUY).delay(8000L)
                                .addScene(AccService.PRODUCT_DETAIL))
                    }
                }
                return false
            }
            ServiceCommand.PRODUCT_BUY -> {
                val result = getBuyProduct()
                if (result) {
                    appendCommand(Command(ServiceCommand.PRODUCT_CONFIRM).addScene(AccService.BOTTOM_DIALOG).canSkip(true))
                    // 如果不进去确定界面，3秒后视为成功
                    BusHandler.instance.sendEmptyMessageDelayed(MessageDef.SUCCESS, 3000L)
                } else {
                    appendCommand(PureCommand(ServiceCommand.GO_BACK))
                            .append(Command(ServiceCommand.BRAND_DETAIL_RANDOM_SHOP)
                                    .addScene(AccService.BRAND_MIAOSHA)
                                    .addScene(AccService.WEBVIEW_ACTIVITY)
                                    .addScene(AccService.BABEL_ACTIVITY))
                }
                return true
            }
            ServiceCommand.PRODUCT_CONFIRM -> {
                return AccessibilityUtils.performClick(mService, "com.jd.lib.productdetail:id/detail_style_add_2_car", false)
            }
        }
        return super.executeInner(command)
    }

    private fun brandDetail(): Boolean {
        var nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")
        if (AccessibilityUtils.isNodesAvalibale(nodes)) {
            val list = nodes!![0]
            var index = 0
            val detailList = HashSet<BrandDetail>()
            workBook?.writeToSheetAppend("时间", "位置", "产品", "价格", "原价")
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

                            if (!TextUtils.isEmpty(title) && detailList.add(BrandDetail(title, price, origin))) {
                                workBook?.writeToSheetAppendWithTime("第${index + 1}屏", title, price, origin)
                                itemCount++
                                if (itemCount >= GlobalInfo.FETCH_NUM) {
                                    workBook?.writeToSheetAppend(GlobalInfo.FETCH_ENOUGH_DATE)
                                    return true
                                }
                            }
                        }
                    }
                    index++
                    if (index % 10 == 0) {
                        BusHandler.instance.startCountTimeout()
                    }
                }
                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    && index < GlobalInfo.SCROLL_COUNT)
        }
        return false
    }


    fun getBuyProduct(): Boolean {
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, "加入购物车")
        if (AccessibilityUtils.isNodesAvalibale(nodes)) {
            for (node in nodes) {
                if (node.isClickable) {
                    val titleNodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.productdetail:id/detail_desc_description")

                    var priceNodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.productdetail:id/detail_price")
                    if (!AccessibilityUtils.isNodesAvalibale(priceNodes)) {
                        priceNodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.productdetail:id/lib_pd_jx_plusprice")
                    }
                    if (!AccessibilityUtils.isNodesAvalibale(priceNodes)) {
                        priceNodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.productdetail:id/pd_top_miaosha_price")
                    }
                    if (AccessibilityUtils.isNodesAvalibale(titleNodes) && AccessibilityUtils.isNodesAvalibale(priceNodes)) {
                        val title = AccessibilityUtils.getFirstText(titleNodes)
                        val price = AccessibilityUtils.getFirstText(priceNodes)
                        workBook?.writeToSheetAppendWithTime("加购商品", title, price)
                    }

                    return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
            }
        }
        return false
    }

    private fun brandDetailRandomShop(): Boolean {
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false
        val list = nodes!![0]
        if (list != null) {
            val randomScroll = Random().nextInt(5)
            var index = 0
            while (index < randomScroll) {
                list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
                index++
            }

            do {
                val shops = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/app_limit_buy_sale_ms_button")
                if (AccessibilityUtils.isNodesAvalibale(shops)) {
                    val select = Random().nextInt(shops.size)
                    var i = 0
                    one@for (info in shops!!) {
                        if (i < select) {
                            i++
                            continue@one
                        }
                        if (info.isClickable) {
                            return info.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        }
                    }
                }
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD))
        }
        return false
    }

    private fun brandSelectRandom(): Boolean {
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false
        val list = nodes!![0]
        if (list != null && !mBrandEntitys.isEmpty()) {
            var index = 0
            do {
                // 滑回顶部
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD))

            val num = Random().nextInt(mBrandEntitys.size)
            val brandEntity = mBrandEntitys[num]
            val title = brandEntity.title
            do {
                val selectNodes = list.findAccessibilityNodeInfosByText(title)
                if (AccessibilityUtils.isNodesAvalibale(selectNodes)) {
                    val parent = AccessibilityUtils.findParentClickable(selectNodes[0])
                    if (parent != null) {
                        return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                }
                index++
                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
                if (index % 10 == 0) {
                    BusHandler.instance.startCountTimeout()
                }
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    && index < 6)
        }
        return false
    }

    private fun brandKillFetchBrand(): Boolean {
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false
        val list = nodes!![0]
        if (list != null) {
            var index = 0
            val brandList = HashSet<BrandEntity>()
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
                        val entity = BrandEntity(title, subTitle)
                        if (brandList.add(entity)) {
                            mBrandEntitys.add(entity)
                        }
                    }
                }
                index++
                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
                if (index % 10 == 0) {
                    BusHandler.instance.startCountTimeout()
                }
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    && index < 6)

            return true
        }

        return false
    }

}