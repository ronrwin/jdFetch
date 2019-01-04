package com.example.jddata.action.move

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.Data2
import com.example.jddata.Entity.MessageDef
import com.example.jddata.GlobalInfo
import com.example.jddata.action.BaseAction
import com.example.jddata.action.Command
import com.example.jddata.action.PureCommand
import com.example.jddata.action.append
import com.example.jddata.util.BaseLogFile
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import java.util.*
import kotlin.collections.HashSet

class MoveBrandKillShopAction : BaseAction(ActionType.MOVE_BRAND_KILL_AND_SHOP) {

    var mBrandEntitys = ArrayList<Data2>()
    init {
        appendCommand(Command(ServiceCommand.HOME_BRAND_KILL).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.HOME_BRAND_KILL_SCROLL)
                        .addScene(AccService.MIAOSHA)
                        .concernResult(true))
    }

    override fun initLogFile() {
        logFile = BaseLogFile("动作_品牌秒杀并加购")
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.HOME_BRAND_KILL -> {
                return findHomeTextClick(GlobalInfo.BRAND_KILL)
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
        }
        return super.executeInner(command)
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
                        logFile?.writeToFileAppendWithTime("加购商品", title, price)
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
            val title = brandEntity.arg1
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
            val brandList = HashSet<Data2>()
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
                        val entity = Data2(title, subTitle)
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