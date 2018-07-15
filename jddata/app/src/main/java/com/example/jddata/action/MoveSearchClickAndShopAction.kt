package com.example.jddata.action

import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.MessageDef
import com.example.jddata.excel.BaseWorkBook
import com.example.jddata.service.*
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.ExecUtils
import java.util.*

class MoveSearchClickAndShopAction(map: HashMap<String, String>?) : MoveSearchAndClickAction(ActionType.MOVE_SEARCH_CLICK_AND_SHOP, map) {
    init {
        appendCommand(Command(ServiceCommand.PRODUCT_BUY).addScene(AccService.PRODUCT_DETAIL).delay(8000L))
    }

    override fun initWorkbook() {
        workBook = BaseWorkBook("动作_搜索_${searchText}_点击_${clickText}_加购")
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.PRODUCT_BUY -> {
                val result = getBuyProduct()
                if (result) {
                    appendCommand(Command(ServiceCommand.PRODUCT_CONFIRM).addScene(AccService.BOTTOM_DIALOG).canSkip(true))
                    // 如果不进去确定界面，3秒后视为成功
                    BusHandler.instance.sendEmptyMessageDelayed(MessageDef.SUCCESS, 3000L)
                } else {
                    appendCommand(PureCommand(ServiceCommand.GO_BACK))
                    appendCommand(Command(ServiceCommand.SEACH_CLICK).addScene(AccService.PRODUCT_LIST))
                }

                return result
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
                        workBook?.writeToSheetAppendWithTime("加购商品", title, price)
                    }

                    return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
            }
        }
        return false
    }

    private fun searchDataRandomBuy(): Boolean {
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/product_list")
                ?: return false
        for (node in nodes) {
            var index = 0
            val random = Random().nextInt(7)
            while (index <= random) {
                val s = node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                        || ExecUtils.fingerScroll()
                index++
            }

            val items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/btn_shopcart")
            if (items != null) {
                for (item in items) {
                    if (item.isClickable) {
                        val parent = item.parent
                        if (parent != null) {
                            val titles = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/product_item_name")
                            val prices = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/product_item_jdPrice")
                            var title = AccessibilityUtils.getFirstText(titles)
                            val price = AccessibilityUtils.getFirstText(prices)
                            if (!TextUtils.isEmpty(title)) {
                                if (title.startsWith("1 ")) {
                                    title = title.replace("1 ", "")
                                }

                                workBook?.writeToSheetAppendWithTime("加购商品",  title, price)
                                return item.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            }
                        }
                    }
                }
            }
        }
        return false
    }
}