package com.example.jddata.action

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.BrandEntity
import com.example.jddata.GlobalInfo
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand
import com.example.jddata.util.ExecUtils
import java.util.*

class BrandKillShopAction : BaseAction(ActionType.BRAND_KILL_AND_SHOP) {

    var mBrandEntitys = ArrayList<BrandEntity>()
    init {
        appendCommand(Command(ServiceCommand.HOME_BRAND_KILL).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.HOME_BRAND_KILL_SCROLL)
                        .addScene(AccService.MIAOSHA)
                        .concernResult(true))
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.HOME_BRAND_KILL -> return CommonConmmand.findHomeTextClick(mService!!, "品牌秒杀")
            ServiceCommand.HOME_BRAND_KILL_SCROLL -> {
                val result = brandKillFetchBrand(GlobalInfo.SCROLL_COUNT)
                if (result) {
                    appendCommand(Command(ServiceCommand.BRAND_SELECT_RANDOM).addScene(AccService.MIAOSHA))
                            .append(Command(ServiceCommand.BRAND_DETAIL_RANDOM_SHOP)
                                    .addScene(AccService.BRAND_MIAOSHA)
                                    .addScene(AccService.WEBVIEW_ACTIVITY)
                                    .addScene(AccService.BABEL_ACTIVITY))
                            .append(Command(ServiceCommand.PRODUCT_BUY).delay(3000L)
                                    .addScene(AccService.PRODUCT_DETAIL))
                            .append(Command(ServiceCommand.PRODUCT_CONFIRM)
                                    .addScene(AccService.BOTTOM_DIALOG))
                }
            }
            ServiceCommand.BRAND_SELECT_RANDOM -> {
                return brandSelectRandom(GlobalInfo.SCROLL_COUNT)
            }
            ServiceCommand.BRAND_DETAIL_RANDOM_SHOP -> {
                if ((mService as AccService).mLastCommandWindow == AccService.WEBVIEW_ACTIVITY
                        || (mService as AccService).mLastCommandWindow == AccService.BABEL_ACTIVITY) {
                    return AccessibilityUtils.performGlobalActionBack(mService)
                } else {
                    return brandDetailRandomShop(GlobalInfo.SCROLL_COUNT)
                }
            }
            ServiceCommand.PRODUCT_BUY -> {
                return AccessibilityUtils.performClick(mService, "com.jd.lib.productdetail:id/pd_invite_friend", false);
            }
        }
        return super.executeInner(command)
    }

    private fun brandDetailRandomShop(scrollCount: Int): Boolean {
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false
        val list = nodes!![0]
        if (list != null) {
            val randomScroll = Random().nextInt(scrollCount)
            var index = 0
            while (index < randomScroll) {
                list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
                index++
            }

            do {
                val shops = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/app_limit_buy_sale_ms_button")
                if (AccessibilityUtils.isNodesAvalibale(shops)) {
                    for (info in shops!!) {
                        if (info.isClickable) {
                            return info.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        }
                    }
                }
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD))

            return true
        }
        return false
    }

    private fun brandSelectRandom(scrollCount: Int): Boolean {
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
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    && index < scrollCount)
        }
        return false
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
            return true
        }

        return false
    }

}