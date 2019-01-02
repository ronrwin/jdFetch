package com.example.jddata.action

import android.text.TextUtils
import android.util.ArraySet
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.BrandDetail
import com.example.jddata.Entity.BrandEntity
import com.example.jddata.Entity.RowData
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.BaseLogFile
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.LogUtil
import java.util.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashSet
import kotlin.collections.firstOrNull

class FetchBrandKillAction : BaseAction(ActionType.FETCH_BRAND_KILL) {

    var mBrandEntitys = ArrayList<BrandEntity>()
    var otherIndex = 0

    val fetchItems = LinkedHashSet<BrandEntity>()
    val clickedItems = LinkedHashSet<BrandEntity>()
    var currentItem: BrandEntity? = null

    init {
        appendCommand(Command(ServiceCommand.HOME_BRAND_KILL).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.COLLECT_BRAND_ITEM)
                        .addScene(AccService.MIAOSHA)
                        .delay(5000L)
                        .concernResult(true))
    }

    override fun initWorkbook() {
        logFile = BaseLogFile("获取_" + GlobalInfo.BRAND_KILL)
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
            ServiceCommand.HOME_BRAND_KILL -> {
                logFile?.writeToFileAppendWithTime("找到并点击 ${GlobalInfo.BRAND_KILL}")
                return CommonConmmand.findHomeTextClick(mService!!, GlobalInfo.BRAND_KILL)
            }
            ServiceCommand.COLLECT_BRAND_ITEM -> {
                val result = colletItems()
                when (result) {
                    COLLECT_FAIL -> {
                        return false
                    }
                    COLLECT_END -> {
                        return true
                    }
                    COLLECT_SUCCESS -> {
                        appendCommand(PureCommand(ServiceCommand.CLICK_ITEM))
                        return true
                    }
                }
            }
            ServiceCommand.CLICK_ITEM -> {
                while (fetchItems.size > 0) {
                    val item = fetchItems.firstOrNull()
                    if (item != null) {
                        fetchItems.remove(item)
                        val addToClicked = clickedItems.add(item)
                        if (addToClicked) {
                            currentItem = item
                            val title = currentItem!!.title
                            val titles = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, title)
                            if (AccessibilityUtils.isNodesAvalibale(titles)) {
                                val title = titles[0]
                                appendCommand(Command(ServiceCommand.BRAND_DETAIL)
                                        .addScene(AccService.BRAND_MIAOSHA)
                                        .addScene(AccService.BABEL_ACTIVITY)
                                        .addScene(AccService.JSHOP)
                                        .addScene(AccService.WEBVIEW_ACTIVITY))
                                        .append(PureCommand(ServiceCommand.GO_BACK))
                                        .append(Command(ServiceCommand.COLLECT_BRAND_ITEM)
                                                .addScene(AccService.MIAOSHA))

                                val parent = AccessibilityUtils.findParentClickable(title)
                                if (parent != null) {
                                    val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                    if (result) {
                                        logFile?.writeToFileAppendWithTime("点击第${itemCount+1}商品：", currentItem!!.title, currentItem!!.subtitle)
                                        return result
                                    }
                                }
                            }
                        }
                    } else {
                        break
                    }
                }
                appendCommand(PureCommand(ServiceCommand.COLLECT_BRAND_ITEM))
                return false
            }
            ServiceCommand.BRAND_DETAIL -> {
//                var result = brandDetail()
                var scene = ""
                var tmp = getState(GlobalInfo.CURRENT_SCENE)
                if (tmp != null) {
                    scene = tmp as String
                }
                var result = 0
                when (scene) {
                    AccService.BABEL_ACTIVITY -> {
                        result = getDetailMethod1()
                    }
                    AccService.BRAND_MIAOSHA -> {
                        result = getDetailMethod2()
                    }
                }
                if (result > 0) {
                    itemCount++
//                    result = shopTypeFetch()
                }
                return result > 0
            }
        }
        return super.executeInner(command)
    }

    private fun getDetailMethod1(): Int {
        var fetchNum = 0
        val lists = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")
        if (AccessibilityUtils.isNodesAvalibale(lists)) {
            var index = 0
            val set = HashSet<BrandDetail>()
            do {
                val items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/a0g")
                if (AccessibilityUtils.isNodesAvalibale(items)) {
                    for (item in items) {
                        var title = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/a3y"))
                        var price = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/a41"))
                        if (title != null && price != null) {
                            if (set.add(BrandDetail(title, price, null))) {
                                // todo: 写数据库
                                fetchNum++
                                logFile?.writeToFileAppendWithTime("${fetchNum}", title, price)
                                if (fetchNum >= GlobalInfo.FETCH_NUM) {
                                    return fetchNum
                                }
                            }
                        }
                    }
                }
                index++
                Thread.sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while (ExecUtils.canscroll(lists[0], index))
        }
        return fetchNum
    }

    private fun getDetailMethod2(): Int {
        var fetchNum = 0
        val lists = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")
        if (AccessibilityUtils.isNodesAvalibale(lists)) {
            for (list in lists) {
                var index = 0
                val set = HashSet<BrandDetail>()
                do {
                    val items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/miaosha_brand_inner_title")
                    if (AccessibilityUtils.isNodesAvalibale(items)) {
                        for (item in items) {
                            var title = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/limit_buy_product_item_name"))
                            var price = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_miaosha_price"))
                            var originPrice = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_jd_price"))
                            if (title != null && price != null) {
                                if (set.add(BrandDetail(title, price, originPrice))) {
                                    // todo: 写数据库

                                    fetchNum++
                                    logFile?.writeToFileAppendWithTime("${fetchNum}", title, price, originPrice)
                                    if (fetchNum >= GlobalInfo.FETCH_NUM) {
                                        return fetchNum
                                    }
                                }
                            }
                        }
                    }
                    index++
                    Thread.sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
                } while (ExecUtils.canscroll(list, index))
            }
        }
        return fetchNum
    }

    private fun colletItems(): Int {
        if (itemCount >= GlobalInfo.BRAND_KILL_COUNT) {
            return COLLECT_END
        }
        if (fetchItems.size > 0) {
            return COLLECT_SUCCESS
        }

        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return COLLECT_FAIL
        val list = nodes!![0]
        if (list != null) {
            var index = 0
            do {
                val brandTitles = list.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/miaosha_brand_title")
                var addResult = false
                if (AccessibilityUtils.isNodesAvalibale(brandTitles)) {
                    for (brand in brandTitles) {
                        val parent = brand.parent
                        if (parent != null) {
                            var title: String? = null
                            if (brand.text != null) {
                                title = brand.text.toString()
                            }

                            val subTitle = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/miaosha_brand_subtitle"))
                            if (title != null) {
                                val entity = BrandEntity(title, subTitle)
                                if (!clickedItems.contains(entity)) {
                                    addResult = fetchItems.add(entity)
                                    if (addResult) {
                                        logFile?.writeToFileAppendWithTime("待点击商品：", title, subTitle)

                                        if (itemCount >= GlobalInfo.BRAND_KILL_COUNT) {
                                            return COLLECT_SUCCESS
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (addResult) {
                    return COLLECT_SUCCESS
                }
                index++
                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                            || ExecUtils.fingerScroll() && index < GlobalInfo.SCROLL_COUNT)
        }

        return COLLECT_FAIL
    }

}