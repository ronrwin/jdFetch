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
                .append(Command().commandCode(ServiceCommand.COLLECT_TAB).addScene(AccService.MIAOSHA).delay(2000))
    }

    override fun initLogFile() {
        logFile = BaseLogFile("获取_" + GlobalInfo.BRAND_KILL)
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
            ServiceCommand.FIND_TEXT -> {
                logFile?.writeToFileAppend("找到并点击 ${GlobalInfo.BRAND_KILL}")
                return findHomeTextClick(GlobalInfo.BRAND_KILL)
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
        val scrolls = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/id_newproduct_tab")
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

        val set = HashSet<Data2>()
        val array = ArrayList<ArrayList<RowData>>()
        if (list != null) {
            var index = 0
            do {
                val brandTitles = list.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/miaosha_brand_title")
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
                                if (set.add(entity)) {
                                    val datas = ArrayList<RowData>()
                                    for (i in 1..4) {
                                        val map = HashMap<String, Any?>()
                                        val row = RowData(map)
                                        row.setDefaultData(env!!)
                                        row.tab = currentTab
                                        row.title = title?.replace("\n", "")?.replace(",", "、")
                                        row.subtitle = subTitle?.replace("\n", "")?.replace(",", "、")
                                        row.biId = GlobalInfo.BRAND_KILL
//                                        row.itemIndex = "${clickedTabs.size}---${set.size}---${i}"
//                                        LogUtil.dataCache(row)
//
//                                        logFile?.writeToFileAppend("${row.itemIndex}", title)
                                        datas.add(row)
                                    }
                                    array.add(datas)
                                }
                            }
                        }
                    }
                }
                index++
                sleep(600)
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) || index < GlobalInfo.SCROLL_COUNT)

            // 抓20之后的。
            val size = array.size
            if (size > 20) {
                var count = 0
                if (size >= 40) {
                    for (item in 20 until 40) {
                        val lis = array[item]
                        count++
                        for (i in 0 until 4) {
                            val row = lis[i]
                            row.itemIndex = "${clickedTabs.size}---${count}---${i+1}"
                            LogUtil.dataCache(row)
                            logFile?.writeToFileAppend("${row.itemIndex}", row.title)
                        }
                    }
                } else {
                    val remain = 40 - size
                    for (item in 20 until size) {
                        count++
                        val lis = array[item]
                        for (i in 0 until 4) {
                            val row = lis[i]
                            row.itemIndex = "${clickedTabs.size}---${count}---${i+1}"
                            LogUtil.dataCache(row)
                            logFile?.writeToFileAppend("${row.itemIndex}", row.title)
                        }
                    }
                    for (item in 0 until remain) {
                        val lis = array[item]
                        count++
                        for (i in 0 until 4) {
                            val row = lis[i]
                            row.itemIndex = "${clickedTabs.size}---${count}---${i+1}"
                            LogUtil.dataCache(row)
                            logFile?.writeToFileAppend("${row.itemIndex}", row.title)
                        }
                    }
                }
            } else {
                for (item in 0 until array.size) {
                    val lis = array[item]
                    for (i in 0 until 4) {
                        val row = lis[i]
                        row.itemIndex = "${clickedTabs.size}---${item}---${i+1}"
                        LogUtil.dataCache(row)
                        logFile?.writeToFileAppend("${row.itemIndex}", row.title)
                    }
                }
            }

            // 随机方式
//            val hash = HashMap<String, Boolean>()
//            var count = 0
//            if (array.size >= 20) {
//                while (count < 20) {
//                    val ranIndex = Random().nextInt(array.size)
//                    if (!hash.containsKey(ranIndex.toString())) {
//                        hash.put(ranIndex.toString(), true)
//                        count++
//                        val lis = array[ranIndex]
//                        for (i in 0 until 4) {
//                            val row = lis[i]
//                            row.itemIndex = "${clickedTabs.size}---${count}---${i+1}"
//                            LogUtil.dataCache(row)
//                            logFile?.writeToFileAppend("${row.itemIndex}", row.title)
//                        }
//                    }
//                }
//            } else {
//                for (item in 0 until array.size) {
//                    val lis = array[item]
//                    for (i in 0 until 4) {
//                        val row = lis[i]
//                        row.itemIndex = "${clickedTabs.size}---${item}---${i+1}"
//                        LogUtil.dataCache(row)
//                        logFile?.writeToFileAppend("${row.itemIndex}", row.title)
//                    }
//                }
//            }
            return true
        }

        return false
    }

}