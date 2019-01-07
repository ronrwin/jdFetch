package com.example.jddata.action.fetch

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.Data3
import com.example.jddata.Entity.Data2
import com.example.jddata.GlobalInfo
import com.example.jddata.action.BaseAction
import com.example.jddata.action.Command
import com.example.jddata.action.PureCommand
import com.example.jddata.action.append
import com.example.jddata.util.BaseLogFile
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.ExecUtils
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashSet
import kotlin.collections.firstOrNull

class FetchBrandKillAction : BaseAction(ActionType.FETCH_BRAND_KILL) {
    val fetchItems = LinkedHashSet<Data2>()
    val clickedItems = LinkedHashSet<Data2>()
    var currentItem: Data2? = null

    init {
        appendCommand(Command(ServiceCommand.FIND_TEXT).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.COLLECT_ITEM)
                        .addScene(AccService.MIAOSHA)
                        .delay(5000L)
                        .concernResult(true))
    }

    override fun initLogFile() {
        logFile = BaseLogFile("获取_" + GlobalInfo.BRAND_KILL)
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
            ServiceCommand.FIND_TEXT -> {
                logFile?.writeToFileAppendWithTime("找到并点击 ${GlobalInfo.BRAND_KILL}")
                return findHomeTextClick(GlobalInfo.BRAND_KILL)
            }
            ServiceCommand.GET_DETAIL -> {
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
                }
                return result > 0
            }
        }
        return super.executeInner(command)
    }

    override fun clickItem():Boolean {
        while (fetchItems.size > 0) {
            val item = fetchItems.firstOrNull()
            if (item != null) {
                fetchItems.remove(item)
                if (!clickedItems.contains(item)) {
                    currentItem = item
                    val titles = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, item.arg1)
                    if (AccessibilityUtils.isNodesAvalibale(titles)) {
                        val parent = AccessibilityUtils.findParentClickable(titles[0])
                        if (parent != null) {
                            clickedItems.add(item)
                            appendCommand(Command(ServiceCommand.GET_DETAIL)
                                    .addScene(AccService.BRAND_MIAOSHA)
                                    .addScene(AccService.BABEL_ACTIVITY)
                                    .addScene(AccService.JSHOP)
                                    .addScene(AccService.WEBVIEW_ACTIVITY))
                                    .append(PureCommand(ServiceCommand.GO_BACK))
                                    .append(Command(ServiceCommand.COLLECT_ITEM)
                                            .addScene(AccService.MIAOSHA))

                            val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            if (result) {
                                logFile?.writeToFileAppendWithTime("点击第${itemCount+1}商品：", item.arg1)
                                return result
                            }
                        }
                    }
                }

                logFile?.writeToFileAppendWithTime("没找到点击商品：", item.arg1)
            } else {
                break
            }
        }
        appendCommand(PureCommand(ServiceCommand.COLLECT_ITEM))
        return false
    }

    private fun getDetailMethod1(): Int {
        val set = HashSet<Data3>()
        val lists = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")
        if (AccessibilityUtils.isNodesAvalibale(lists)) {
            var index = 0
            do {
                val items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/a0g")
                if (AccessibilityUtils.isNodesAvalibale(items)) {
                    for (item in items) {
                        var title = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/a3y"))
                        var price = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/a41"))
                        if (title != null && price != null) {
                            if (set.add(Data3(title, price, null))) {
                                // todo: 写数据库
                                logFile?.writeToFileAppendWithTime("${set.size}", title, price)
                                if (set.size >= GlobalInfo.FETCH_NUM) {
                                    return set.size
                                }
                            }
                        }
                    }
                }
                index++
                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while (ExecUtils.canscroll(lists[0], index))
        }
        return set.size
    }

    private fun getDetailMethod2(): Int {
        val set = HashSet<Data3>()
        val lists = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")
        if (AccessibilityUtils.isNodesAvalibale(lists)) {
            for (list in lists) {
                var index = 0
                do {
                    val items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/miaosha_brand_inner_title")
                    if (AccessibilityUtils.isNodesAvalibale(items)) {
                        for (item in items) {
                            var title = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/limit_buy_product_item_name"))
                            var price = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_miaosha_price"))
                            var originPrice = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_jd_price"))
                            if (title != null && price != null) {
                                if (set.add(Data3(title, price, originPrice))) {
                                    // todo: 写数据库

                                    logFile?.writeToFileAppendWithTime("${set.size}", title, price, originPrice)
                                    if (set.size >= GlobalInfo.FETCH_NUM) {
                                        return set.size
                                    }
                                }
                            }
                        }
                    }
                    index++
                    sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
                } while (ExecUtils.canscroll(list, index))
            }
        }
        return set.size
    }

    override fun collectItems(): Int {
        if (itemCount >= GlobalInfo.BRAND_KILL_COUNT) {
            return COLLECT_END
        }
        if (fetchItems.size > 0) {
            return COLLECT_SUCCESS
        }

        val lists = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")
        if (!AccessibilityUtils.isNodesAvalibale(lists)) return COLLECT_FAIL
        val list = lists!![0]
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
                                val entity = Data2(title, subTitle)
                                if (!clickedItems.contains(entity)) {
                                    addResult = fetchItems.add(entity)
                                    if (addResult) {
                                        logFile?.writeToFileAppendWithTime("待点击商品：", title, subTitle)
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
            } while (ExecUtils.canscroll(list, index))
            return COLLECT_END
        }

        return COLLECT_FAIL
    }

}