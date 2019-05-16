package com.example.jddata.action.fetch

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.*
import com.example.jddata.GlobalInfo
import com.example.jddata.action.BaseAction
import com.example.jddata.action.Command
import com.example.jddata.action.append
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.shelldroid.Env
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.BaseLogFile
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.LogUtil

class FetchGoodShopAction(env: Env) : BaseAction(env, ActionType.FETCH_GOOD_SHOP) {

    val fetchTabs = ArrayList<String>()
    val clickedTabs = ArrayList<String>()
    var currentTab: String? = null

    init {
        appendCommand(Command().commandCode(ServiceCommand.FIND_TEXT).addScene(AccService.JD_HOME))
                .append(Command().commandCode(ServiceCommand.COLLECT_TAB).addScene(AccService.BABEL_ACTIVITY).delay(6000L))
    }

    val name = GlobalInfo.GOOD_SHOP
    override fun initLogFile() {
        logFile = BaseLogFile("获取_$name")
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.FIND_TEXT -> {
                logFile?.writeToFileAppend("找到并点击 $name")
                return findHomeTextClick(name)
            }
            ServiceCommand.FETCH_PRODUCT -> {
                val result = fetchProduct()
                if (result) {
                    appendCommand(Command().commandCode(ServiceCommand.COLLECT_TAB))
                }
                return result
            }
            ServiceCommand.COLLECT_TAB -> {
                BusHandler.instance.startCountTimeout()
                val resultCode = collectTabs()
                when (resultCode) {
                    COLLECT_FAIL -> {
                        return false
                    }
                    COLLECT_END -> {
                        return false
                    }
                    COLLECT_SUCCESS -> {
                        appendCommand(Command().commandCode(ServiceCommand.CLICK_TAB))
                        return true
                    }
                }
                return true
            }
            ServiceCommand.CLICK_TAB -> {
                val result = clickTab()
                if (result) {
                    itemCount = 0

                    appendCommand(Command().commandCode(ServiceCommand.FETCH_PRODUCT))
                }
                return result
            }
        }
        return super.executeInner(command)
    }

    fun fetchProduct(): Boolean {
        val set = HashSet<Data2>()
        val lists = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")
        if (AccessibilityUtils.isNodesAvalibale(lists)) {
            for (list in lists) {
                var index = GlobalInfo.SCROLL_COUNT - 10
                do {
                    val items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/a7_")
                    if (AccessibilityUtils.isNodesAvalibale(items)) {
                        for (item in items) {
                            var title = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/a7c"))
                            var markNum = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/a7g"))
                            if (title != null) {
                                val data = Data2(title, markNum)
                                if (set.add(data)) {
                                    itemCount++
                                    logFile?.writeToFileAppend("获取第${itemCount}个商品信息：${data}")

                                    val map = HashMap<String, Any?>()
                                    val row = RowData(map)
                                    row.setDefaultData(env!!)
                                    row.shop = title?.replace("\n", "")?.replace(",", "、")
                                    row.markNum = markNum?.replace("\n", "")?.replace(",", "、")
                                    row.biId = GlobalInfo.GOOD_SHOP
                                    row.tab = currentTab
                                    row.itemIndex = "${clickedTabs.size}---${itemCount}"
                                    LogUtil.dataCache(row)

                                    if (itemCount >= GlobalInfo.FETCH_NUM) {
                                        return true
                                    }
                                }
                            }
                        }
                    }

                    index++
                    sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
                } while (ExecUtils.canscroll(list, index))
            }
            return true
        }

        return false
    }

    fun clickTab(): Boolean {
        while (fetchTabs.size > 0) {
            val item = fetchTabs.removeAt(0)
            if (!clickedTabs.contains(item)) {
                val titles = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, item)
                if (AccessibilityUtils.isNodesAvalibale(titles)) {
                    clickedTabs.add(item)
                    val result = titles[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    if (result) {
                        currentTab = item
                        logFile?.writeToFileAppend("点击第${clickedTabs.size}标签：", item)
                        itemCount = 0
                        return result
                    }
                }
            }
            logFile?.writeToFileAppend("没找到标签：", item)
        }
        appendCommand(Command().commandCode(ServiceCommand.COLLECT_TAB))
        return false
    }

    fun collectTabs(): Int {
        if (clickedTabs.size >= GlobalInfo.WGOOD_SHOP_TAB) {
            return COLLECT_END
        }
        if (fetchTabs.size > 0) {
            return COLLECT_SUCCESS
        }
        val scrolls = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/a2n")
        if (AccessibilityUtils.isNodesAvalibale(scrolls)) {
            // 减少滑动次数
            var index = GlobalInfo.SCROLL_COUNT - 5
            do {
                var addResult = false
                val texts = AccessibilityUtils.findChildByClassname(scrolls[0], "android.widget.TextView")
                if (AccessibilityUtils.isNodesAvalibale(texts)) {
                    for (textNode in texts) {
                        if (textNode.text != null) {
                            val tab = textNode.text.toString()
                            if (!clickedTabs.contains(tab)) {
                                fetchTabs.add(tab)
                                addResult = true
                                logFile?.writeToFileAppend("待点击标签：$tab")
//                                if (fetchTabs.size >= GlobalInfo.WGOOD_SHOP_TAB) {
//                                    return COLLECT_SUCCESS
//                                }
                            }
                        }
                    }
                }
                if (addResult) {
                    return COLLECT_SUCCESS
                }

                index++
                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while (ExecUtils.canscroll(scrolls[0], index))

            logFile?.writeToFileAppend("没有更多标签")
            return COLLECT_END
        }
        return COLLECT_FAIL
    }

}