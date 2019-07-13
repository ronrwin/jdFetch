package com.example.jddata.action.fetch

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.Data2
import com.example.jddata.Entity.RowData
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
import java.util.*
import kotlin.collections.HashMap

class FetchBrandKillActionNoSkuTitle(env: Env) : BaseAction(env, ActionType.FETCH_BRAND_KILL) {
    val fetchItems = LinkedHashSet<Data2>()
    val clickedItems = LinkedHashSet<Data2>()
    var currentItem: Data2? = null
    val fetchTabs = ArrayList<String>()
    val clickedTabs = ArrayList<String>()
    var currentTab: String? = null

    init {
        appendCommand(Command().commandCode(ServiceCommand.FIND_TEXT).addScene(AccService.JD_HOME))
                .append(Command().commandCode(ServiceCommand.MIAOSHA_TAB).addScene(AccService.MIAOSHA).delay(3000))
                .append(Command().commandCode(ServiceCommand.COLLECT_TAB).delay(2000))
    }

    override fun initLogFile() {
        logFile = BaseLogFile("获取_" + GlobalInfo.BRAND_KILL)
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
            ServiceCommand.MIAOSHA_TAB -> {
                val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/a21")
                if (AccessibilityUtils.isNodesAvalibale(nodes) && nodes[0].childCount == 5) {
                    val node = nodes[0].getChild(2)
                    val result = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    return result
                }
                return false
            }
            ServiceCommand.FIND_TEXT -> {
                logFile?.writeToFileAppend("找到并点击 品类秒杀")
                return findHomeTextClick("品类秒杀")
            }
            ServiceCommand.FETCH_FIRST_PRODUCT -> {
                val result = fetchProduct()
                appendCommand(Command().commandCode(ServiceCommand.COLLECT_TAB))
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
                        return true
                    }
                    COLLECT_SUCCESS -> {
                        appendCommand(Command().commandCode(ServiceCommand.CLICK_TAB))
                        return true
                    }
                }
                return true
            }
            ServiceCommand.CLICK_TAB -> {
                BusHandler.instance.startCountTimeout()
                val result = clickTab()
                if (result) {;
                    itemCount = 0
                    fetchItems.clear()
                    clickedItems.clear()
                    appendCommand(Command().commandCode(ServiceCommand.FETCH_FIRST_PRODUCT).delay(3000))
                }
                return result
            }
        }
        return super.executeInner(command)
    }

    fun collectTabs(): Int {
        if (clickedTabs.size >= GlobalInfo.BRAND_KILL_TAB) {
            return COLLECT_END
        }
        if (fetchTabs.size > 0) {
            return COLLECT_SUCCESS
        }
        val scrolls = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/a7g")
        if (AccessibilityUtils.isNodesAvalibale(scrolls)) {
            var index = GlobalInfo.SCROLL_COUNT - 5
            do {
                var addResult = false
                val texts = AccessibilityUtils.findChildByClassname(scrolls[0], "android.widget.RadioButton")
                if (AccessibilityUtils.isNodesAvalibale(texts)) {
                    for (textNode in texts) {
                        if (textNode.text != null) {
                            val tab = textNode.text.toString()
                            if (!clickedTabs.contains(tab)) {
                                fetchTabs.add(tab)
                                addResult = true
                                logFile?.writeToFileAppend("待点击标签：$tab")
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

    fun clickTab(): Boolean {
        while (fetchTabs.size > 0 && clickedTabs.size < GlobalInfo.BRAND_KILL_TAB) {
            val item = fetchTabs[0]
            fetchTabs.removeAt(0)
            if (!clickedTabs.contains(item)) {
                val titles = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, item)
                if (AccessibilityUtils.isNodesAvalibale(titles)) {
                    currentTab = item
                    clickedTabs.add(item)
                    val result = titles[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    if (result) {
                        logFile?.writeToFileAppend("点击第${clickedTabs.size}标签：", item)
                        return result
                    }
                }
            }
            logFile?.writeToFileAppend("没找到标签：", item)
        }
        appendCommand(Command().commandCode(ServiceCommand.COLLECT_TAB))
        return false
    }


    fun fetchProduct(): Boolean {
        val lists = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")
        if (!AccessibilityUtils.isNodesAvalibale(lists)) return false
        val last = lists[lists.size - 1]
        var list = last
        if (lists.size > 1) {
            list = lists[lists.size - 2]
            if (last != null && AccessibilityUtils.getAllText(last).isNotEmpty() && clickedTabs.size > 2 && lists.size == 2) {
                list = last
            }
        }

        val set = HashSet<String>()
        if (list != null) {
            var index = 0
            do {
                val brandTitles = list.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/gx")
                if (AccessibilityUtils.isNodesAvalibale(brandTitles)) {
                    for (brand in brandTitles) {
                        val parent = brand.parent
                        if (parent != null) {
                            var title: String? = null
                            if (brand.text != null) {
                                title = brand.text.toString()
                            }

                            val subTitle = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/xs"))
                            if (title != null) {
                                val entity = Data2(title, subTitle)
                                if (set.add(title)) {
                                    val map = HashMap<String, Any?>()
                                    val row = RowData(map)
                                    row.setDefaultData(env!!)
                                    row.tab = currentTab
                                    row.title = title?.replace("\n", "")?.replace(",", "、")
                                    row.subtitle = subTitle?.replace("\n", "")?.replace(",", "、")
                                    row.biId = GlobalInfo.BRAND_KILL
                                    row.itemIndex = "${clickedTabs.size}---${set.size}"
                                    LogUtil.dataCache(row)

                                    itemCount++
                                    logFile?.writeToFileAppend("${row.itemIndex}", title)
                                    if (set.size >= GlobalInfo.FETCH_NUM) {
                                        return true
                                    }
                                }
                            }
                        }
                    }
                }
                index++
                sleep(600)
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) || index < GlobalInfo.SCROLL_COUNT)

            return true
        }

        return false
    }

}