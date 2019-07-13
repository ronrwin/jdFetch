package com.example.jddata.action.fetch

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.Data2
import com.example.jddata.Entity.Data3
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
import java.util.ArrayList

class FetchTypeKillActionNoSku(env: Env) : BaseAction(env, ActionType.FETCH_TYPE_KILL) {

    val fetchItems = LinkedHashSet<Data2>()
    val clickedItems = LinkedHashSet<Data2>()
    var currentItem: Data2? = null

    init {
        appendCommand(Command().commandCode(ServiceCommand.FIND_TEXT).addScene(AccService.JD_HOME))
                .append(Command().commandCode(ServiceCommand.COLLECT_TAB).addScene(AccService.MIAOSHA))
    }

    override fun initLogFile() {
        logFile = BaseLogFile("获取_" + GlobalInfo.TYPE_KILL)
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.FIND_TEXT -> {
                logFile?.writeToFileAppend("找到并点击 \"${GlobalInfo.TYPE_KILL}\"")
                return findHomeTextClick(GlobalInfo.TYPE_KILL)
            }
            ServiceCommand.GET_DETAIL -> {
                var scene = ""
                var tmp = getState(GlobalInfo.CURRENT_SCENE)
                if (tmp != null) {
                    scene = tmp as String
                }
                var result = 0
                when (scene) {
                    AccService.TYPE_MIAOSH_DETAIL -> {
                        result = getDetail()
                    }
                }
                if (result > 0) {
                    itemCount++
                }
                return result > 0
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
                    appendCommand(Command().commandCode(ServiceCommand.COLLECT_ITEM).delay(3000))
                }
                return result
            }
        }
        return super.executeInner(command)
    }
    override fun onCollectItemEnd() {
        appendCommand(Command().commandCode(ServiceCommand.COLLECT_TAB))
        super.onCollectItemEnd()
    }

    fun collectTabs(): Int {
        if (clickedTabs.size >= GlobalInfo.TYPE_KILL_TAB) {
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

    val fetchTabs = ArrayList<String>()
    val clickedTabs = ArrayList<String>()
    var currentTab: String? = null
    fun clickTab(): Boolean {
        while (fetchTabs.size > 0 && clickedTabs.size < GlobalInfo.TYPE_KILL_TAB) {
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

    private fun getDetail(): Int {
        val set = HashSet<String>()
        val lists = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")
        if (AccessibilityUtils.isNodesAvalibale(lists)) {
            for (list in lists) {
                var index = 0
                do {
                    val items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/a2c")
                    if (AccessibilityUtils.isNodesAvalibale(items)) {
                        for (item in items) {
                            var title = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/a6j"))
                            var price = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/a6p"))
                            var origin = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/a6o"))
                            if (title != null && price != null) {
                                price = price.replace("¥", "")
                                if (origin != null) {
                                    origin = origin.replace("¥", "")
                                }
                                if (set.add(title)) {
                                    val map = HashMap<String, Any?>()
                                    val row = RowData(map)
                                    row.setDefaultData(env!!)
                                    row.title = currentItem?.arg1?.replace("\n", "")?.replace(",", "、")
                                    row.subtitle = currentItem?.arg2?.replace("\n", "")?.replace(",", "、")
                                    row.product = title?.replace("\n", "")?.replace(",", "、")
                                    row.price = price?.replace("\n", "")?.replace(",", "、")
                                    row.originPrice = origin?.replace("\n", "")?.replace(",", "、")
                                    row.biId = GlobalInfo.TYPE_KILL
                                    row.itemIndex = "${clickedTabs.size}---${itemCount+1}---${set.size}"
                                    LogUtil.dataCache(row)

                                    logFile?.writeToFileAppend("${row.itemIndex}", title, price, origin)
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



    override fun collectSubItems(): Int {
        return super.collectSubItems()
    }

    override fun clickItem(): Boolean {
        while (fetchItems.size > 0) {
            val item = fetchItems.firstOrNull()
            if (item != null) {
                fetchItems.remove(item)
                if (!clickedItems.contains(item)) {
                    val titles = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, item.arg1)
                    if (AccessibilityUtils.isNodesAvalibale(titles)) {
                        val parent = AccessibilityUtils.findParentClickable(titles[0])
                        if (parent != null) {
                            clickedItems.add(item)
                            appendCommand(Command().commandCode(ServiceCommand.GET_DETAIL)
                                        .addScene(AccService.TYPE_MIAOSH_DETAIL)
                                        .addScene(AccService.WEBVIEW_ACTIVITY)
                                        .addScene(AccService.BABEL_ACTIVITY))
                                    .append(Command().commandCode(ServiceCommand.GO_BACK))
                                    .append(Command().commandCode(ServiceCommand.COLLECT_ITEM)
                                            .addScene(AccService.MIAOSHA))
                            val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            if (result) {
                                currentItem = item
                                logFile?.writeToFileAppend("点击第${itemCount+1}商品：", item.arg1)
                                return result
                            }
                        }
                    }
                }
                logFile?.writeToFileAppend("没找到未点击商品：", item.arg1)
            } else {
                break
            }
        }
        appendCommand(Command().commandCode(ServiceCommand.COLLECT_ITEM))
        return false
    }

    override fun collectItems(): Int {
        if (itemCount >= GlobalInfo.FETCH_NUM) {
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
                val titles = list.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/a2o")
                var addResult = false
                if (AccessibilityUtils.isNodesAvalibale(titles)) {
                    for (titleNode in titles) {
                        val parent = titleNode.parent
                        if (parent != null) {
                            var title: String? = null
                            if (titleNode.text != null) {
                                title = titleNode.text.toString()
                            }

                            val subTitle = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/a2p"))

                            if (title != null) {
                                val entity = Data2(title, subTitle)
                                if (!clickedItems.contains(entity)) {
                                    addResult = fetchItems.add(entity)
                                    if (addResult) {
                                        logFile?.writeToFileAppend("待点击商品：", title, subTitle)
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