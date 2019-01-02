package com.example.jddata.action

import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.Entity.*
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.BaseLogFile
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.ExecUtils

class FetchHomeAction : BaseAction(ActionType.FETCH_HOME) {

    init {
        appendCommand(Command(ServiceCommand.COLLECT_ITEM).addScene(AccService.JD_HOME))
    }

    override fun initLogFile() {
        logFile = BaseLogFile("获取_" + GlobalInfo.HOME)
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.COLLECT_ITEM -> {
                val resultCode = collectItems()
                when (resultCode) {
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
                return true
            }
            ServiceCommand.CLICK_ITEM -> {
                while (fetchItems.size > 0) {
                    val item = fetchItems.firstOrNull()
                    if (item != null) {
                        fetchItems.remove(item)
                        val addToClicked = clickedItems.add(item)
                        if (addToClicked) {
                            currentItem = item
                            val title = currentItem!!.arg1
                            val titles = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, title)
                            if (AccessibilityUtils.isNodesAvalibale(titles)) {
                                appendCommand(Command(ServiceCommand.GET_SKU).addScene(AccService.PRODUCT_DETAIL).delay(2000))
                                        .append(PureCommand(ServiceCommand.GO_BACK))
                                        .append(Command(ServiceCommand.COLLECT_ITEM).addScene(AccService.JD_HOME))
                                val parent = AccessibilityUtils.findParentClickable(titles[0])
                                if (parent != null) {
                                    val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                    if (result) {
                                        logFile?.writeToFileAppendWithTime("点击第${itemCount+1}商品：", currentItem!!.arg1, currentItem!!.arg2)
                                        return result
                                    }
                                }
                            }
                        }
                    } else {
                        break
                    }
                }
                appendCommand(PureCommand(ServiceCommand.COLLECT_ITEM))
                return false
            }
        }
        return super.executeInner(command)
    }

    override fun fetchSkuid(skuid: String): Boolean {
        itemCount++
        // todo: 加数据库
        return super.fetchSkuid(skuid)
    }

    val fetchItems = LinkedHashSet<Data2>()
    val clickedItems = LinkedHashSet<Data2>()
    var currentItem: Data2? = null

    /**
     * 首页-为你推荐
     */
    fun collectItems(): Int {
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
                // 推荐部分
                val items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/c2g")
                if (AccessibilityUtils.isNodesAvalibale(items)) {
                    var addResult = false
                    for (item in items) {
                        var product = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/btx"))
                        var price = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/bty"))

                        if (!TextUtils.isEmpty(product) && !TextUtils.isEmpty(price)) {
                            if (product != null && product.startsWith("1 ")) {
                                product = product.replace("1 ", "");
                            }

                            val recommend = Data2(product, price)
                            if (!clickedItems.contains(recommend)) {
                                addResult = fetchItems.add(recommend)
                                if (addResult) {
                                    if (price != null) {
                                        price = price.replace("¥", "")
                                    }
                                    logFile?.writeToFileAppendWithTime("待点击商品：", product, price)

                                    if (itemCount >= GlobalInfo.FETCH_NUM) {
                                        return COLLECT_SUCCESS
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
                if (items != null) {
                    Thread.sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
                } else {
                    Thread.sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP_WAIT)
                }
            } while (ExecUtils.canscroll(list, index))

            logFile?.writeToFileAppendWithTime(GlobalInfo.NO_MORE_DATA)
            return COLLECT_FAIL
        }
        return COLLECT_FAIL
    }
}