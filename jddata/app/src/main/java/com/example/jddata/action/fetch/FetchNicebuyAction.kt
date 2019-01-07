package com.example.jddata.action.fetch

import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.Data3
import com.example.jddata.Entity.Data4
import com.example.jddata.Entity.RowData
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
import com.example.jddata.util.LogUtil

class FetchNicebuyAction : BaseAction(ActionType.FETCH_NICE_BUY) {
    val fetchTabs = ArrayList<String>()
    val clickedTabs = ArrayList<String>()
    var currentTab: String? = null

    init {
        appendCommand(Command(ServiceCommand.FIND_TEXT).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.COLLECT_TAB).addScene(AccService.WORTHBUY).delay(6000))

    }

    var name = GlobalInfo.NICE_BUY
    override fun initLogFile() {
        logFile = BaseLogFile("获取_${name}")
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.FIND_TEXT -> {
                logFile?.writeToFileAppendWithTime("找到并点击 $name")
                return findHomeTextClick(name)
            }
            ServiceCommand.COLLECT_TAB -> {
                val resultCode = collectTabs()
                when (resultCode) {
                    COLLECT_FAIL -> {
                        return false
                    }
                    COLLECT_END -> {
                        return true
                    }
                    COLLECT_SUCCESS -> {
                        appendCommand(PureCommand(ServiceCommand.CLICK_TAB))
                        return true
                    }
                }
                return true
            }
            ServiceCommand.CLICK_TAB -> {
                val result = clickTab()
                if (result) {
                    appendCommand(Command(ServiceCommand.FETCH_FIRST_PRODUCT))
                }
                return result
            }
            ServiceCommand.FETCH_FIRST_PRODUCT -> {
                val result = fetchFirstProduct1()
                if (result) {
                    appendCommand(Command(ServiceCommand.COLLECT_TAB))
                }
                return result
            }
        }
        return super.executeInner(command)
    }

    fun fetchFirstProduct1(): Boolean {
        val set = HashSet<Data4>()
        val lists = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")

        logFile?.writeToFileAppendWithTime("有${lists.size}个list")
        for (list in lists) {
            logFile?.writeToFileAppendWithTime("${AccessibilityUtils.getAllText(list)}")
        }

        if (AccessibilityUtils.isNodesAvalibale(lists) && lists.size > 1) {
            val list = lists[lists.size-2]
            logFile?.writeToFileAppendWithTime("当前List: ${AccessibilityUtils.getAllText(list)}")

            var type = 0
            var index = 0
            do {
                when(type) {
                    0 -> {
                        if (type1(list,set)) {
                            type = 1
                        } else if (type2(list, set)) {
                            type = 2
                        }
                    }
                    1 -> {
                        return type1(list, set)
                    }
                    2 -> {
                        return type2(list, set)
                    }
                }

                index++
            } while (ExecUtils.canscroll(list, index))

            logFile?.writeToFileAppendWithTime(GlobalInfo.NO_MORE_DATA)
            return false
        }

        return false
    }

    fun type1(list: AccessibilityNodeInfo, set: HashSet<Data4>): Boolean {
        var titles = list.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_zdm_maintitle")
        if (AccessibilityUtils.isNodesAvalibale(titles)) {
            for (titleNode in titles) {
                val prarent = AccessibilityUtils.findParentClickable(titleNode)
                var title = AccessibilityUtils.getFirstText(prarent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_zdm_maintitle"))
                // todo: 出处列
                var desc = AccessibilityUtils.getFirstText(prarent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/talent_name"))
                if (desc == null) {
                    // todo: 副标题列
                    desc = AccessibilityUtils.getFirstText(prarent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_zdm_subtitle"))
                }
                val pageView = AccessibilityUtils.getFirstText(prarent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/page_view"))
                val collect = AccessibilityUtils.getFirstText(prarent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/text_collect_number"))

                if (!TextUtils.isEmpty(title)) {
                    val recommend = Data4(title, desc, pageView, collect)
                    if (set.add(recommend)) {
                        itemCount++
                        logFile?.writeToFileAppendWithTime("获取第${itemCount}个商品：${recommend}")
                        // todo: 数据库
                        if (itemCount >= GlobalInfo.FETCH_NUM) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    fun type2(list: AccessibilityNodeInfo, set: HashSet<Data4>): Boolean {
        // 第二种
        var titles = list.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_zdm_inventory_title")
        if (AccessibilityUtils.isNodesAvalibale(titles)) {
            for (titleNode in titles) {
                val prarent = AccessibilityUtils.findParentClickable(titleNode)
                var title = AccessibilityUtils.getFirstText(prarent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_zdm_inventory_title"))
                // todo: 描述列
                val desc = AccessibilityUtils.getFirstText(prarent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_zdm_inventory_desc"))
                val pageView = AccessibilityUtils.getFirstText(prarent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/page_view"))
                val collect = AccessibilityUtils.getFirstText(prarent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/text_collect_number"))

                if (!TextUtils.isEmpty(title)) {
                    val recommend = Data4(title, desc, pageView, collect)
                    if (set.add(recommend)) {
                        itemCount++
                        logFile?.writeToFileAppendWithTime("获取第${itemCount}个商品：${recommend}")
                        // todo: 数据库
                        if (itemCount >= GlobalInfo.FETCH_NUM) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    fun clickTab(): Boolean {
        while (fetchTabs.size > 0) {
            val item = fetchTabs[0]
            fetchTabs.removeAt(0)
            if (!clickedTabs.contains(item)) {
                currentTab = item
                val titles = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, item)
                if (AccessibilityUtils.isNodesAvalibale(titles)) {
                    clickedTabs.add(item)
                    val result = titles[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    if (result) {
                        logFile?.writeToFileAppendWithTime("点击第${clickedTabs.size}标签：", item)
                        itemCount = 0
                        sleep(3000)
                        return result
                    }
                }
            }
            logFile?.writeToFileAppendWithTime("没找到标签：", item)
        }
        appendCommand(PureCommand(ServiceCommand.COLLECT_TAB))
        return false
    }

    fun collectTabs(): Int {
        if (clickedTabs.size >= GlobalInfo.TAB_COUNT) {
            return COLLECT_END
        }
        if (fetchTabs.size > 0) {
            return COLLECT_SUCCESS
        }
        val scrolls = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/tab")
        if (AccessibilityUtils.isNodesAvalibale(scrolls)) {
            var index = 0
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
                                logFile?.writeToFileAppendWithTime("待点击标签：$tab")
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

            logFile?.writeToFileAppendWithTime("没有更多标签")
            return COLLECT_END
        }
        return COLLECT_FAIL
    }

}