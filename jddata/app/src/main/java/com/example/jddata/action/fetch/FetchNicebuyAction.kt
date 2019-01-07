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

    init {
        appendCommand(Command(ServiceCommand.FIND_TEXT).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.COLLECT_ITEM).addScene(AccService.WORTHBUY).concernResult(true).delay(6000L))

    }

    override fun initLogFile() {
        logFile = BaseLogFile("获取_" + GlobalInfo.NICE_BUY)
    }


    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.FIND_TEXT -> {
                logFile?.writeToFileAppendWithTime("找到并点击 \"${GlobalInfo.NICE_BUY}\"")
                return findHomeTextClick(GlobalInfo.NICE_BUY)
            }
            ServiceCommand.GET_DETAIL -> {
                val fetchNum = 0
                val result = getDetail()
                if (result > 0) {
                    itemCount++
                }
                return fetchNum > 0
            }
        }
        return super.executeInner(command)
    }

    override fun clickItem(): Boolean {
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
                            appendCommand(Command(ServiceCommand.GET_DETAIL).addScene(AccService.INVENTORY).delay(5000L))
                                    .append(PureCommand(ServiceCommand.GO_BACK))
                                    .append(Command(ServiceCommand.COLLECT_ITEM).addScene(AccService.WORTHBUY))
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

    val fetchItems = LinkedHashSet<Data4>()
    val clickedItems = LinkedHashSet<Data4>()
    var currentItem: Data4? = null

    override fun collectItems(): Int {
        if (itemCount >= GlobalInfo.FETCH_NUM) {
            return COLLECT_END
        }
        if (fetchItems.size > 0) {
            return COLLECT_SUCCESS
        }

        val lists = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")

        for (list in lists) {
            var index = 0
            do {
                val titles = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/tv_zdm_inventory_title")
                if (AccessibilityUtils.isNodesAvalibale(titles)) {
                    var addResult = false
                    for (titleNode in titles) {
                        val parent = AccessibilityUtils.findParentClickable(titleNode)
                        if (parent != null) {
                            var title = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_zdm_inventory_title"))
                            val desc = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_zdm_inventory_desc"))
                            val pageView = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/page_view"))
                            val collect = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/text_collect_number"))

                            if (!TextUtils.isEmpty(title)) {
                                val recommend = Data4(title, desc, pageView, collect)
                                if (!clickedItems.contains(recommend)) {
                                    addResult = fetchItems.add(recommend)
                                    if (addResult) {
                                        logFile?.writeToFileAppendWithTime("待点击商品：", title, desc, pageView, collect)
                                    }
                                }
                            }
                        }
                    }
                    if (addResult) {
                        return COLLECT_SUCCESS
                    }
                }

                index++
            } while (ExecUtils.canscroll(list, index))

            logFile?.writeToFileAppendWithTime(GlobalInfo.NO_MORE_DATA)
            return COLLECT_END
        }
        return COLLECT_FAIL
    }

    private fun getDetail(): Int {
        val set = HashSet<Data3>()
        val lists = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/recycler_view")
        if (AccessibilityUtils.isNodesAvalibale(lists)) {
            var index = 0
            do {
                val desc = AccessibilityUtils.getFirstText(AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/tv_desc"))
                logFile?.writeToFileAppendWithTime("描述: ${desc}")

                var titles = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/tv_title")
                if (AccessibilityUtils.isNodesAvalibale(titles)) {
                    for (titleNode in titles) {
                        val parent = AccessibilityUtils.findParentClickable(titleNode)
                        if (parent != null) {
                            val title = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_title"))
                            var price = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_price"))
                            var origin = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_original_price"))

                            if (title != null && price != null) {
                                if (price != null) {
                                    price = price.replace("¥", "")
                                }
                                if (origin != null) {
                                    origin = origin.replace("¥", "")
                                }

                                if (set.add(Data3(title, price, origin))) {
                                    // todo: 写数据库

                                    val map = HashMap<String, Any?>()
                                    val row = RowData(map)
                                    row.setDefaultData()
                                    row.product = ExecUtils.translate(title)
                                    row.price = ExecUtils.translate(price)
                                    row.originPrice = ExecUtils.translate(origin)
                                    row.description = ExecUtils.translate(desc)
                                    row.title = ExecUtils.translate(currentItem!!.arg1)
                                    row.num = ExecUtils.translate(currentItem!!.arg2)
                                    row.viewdNum = ExecUtils.translate(currentItem!!.arg3)
                                    row.markNum = ExecUtils.translate(currentItem!!.arg4)
                                    row.biId = GlobalInfo.NICE_BUY
                                    row.itemIndex = "${itemCount+1}"
                                    LogUtil.dataCache(row)

                                    logFile?.writeToFileAppendWithTime("${set.size}", title, price, origin)
                                    if (set.size >= GlobalInfo.FETCH_NUM) {
                                        return set.size
                                    }
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

}