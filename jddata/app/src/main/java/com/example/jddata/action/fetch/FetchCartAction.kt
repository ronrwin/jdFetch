package com.example.jddata.action.fetch

import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.Entity.ActionType
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

class FetchCartAction : BaseAction(ActionType.FETCH_CART) {
    init {
        appendCommand(Command(ServiceCommand.CART_TAB).addScene(AccService.JD_HOME))
                .append(PureCommand(ServiceCommand.COLLECT_ITEM))
    }

    override fun initLogFile() {
        logFile = BaseLogFile("获取_" + GlobalInfo.CART)
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
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
                            appendCommands(getSkuCommands())
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

    override fun beforeLeaveProductDetai() {
        appendCommand(Command(ServiceCommand.COLLECT_ITEM).addScene(AccService.JD_HOME))
        super.beforeLeaveProductDetai()
    }


    override fun fetchSkuid(skuid: String): Boolean {
        itemCount++
        logFile?.writeToFileAppendWithTime("记录商品：${currentItem.toString()}, sku: $skuid")
        // todo: 加数据库
        return super.fetchSkuid(skuid)
    }

    val fetchItems = LinkedHashSet<Data2>()
    val clickedItems = LinkedHashSet<Data2>()
    var currentItem: Data2? = null

    override fun collectItems(): Int {
        if (itemCount >= GlobalInfo.FETCH_NUM) {
            return COLLECT_END
        }
        if (fetchItems.size > 0) {
            return COLLECT_SUCCESS
        }

        var index = 0
        val lists = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")
        if (AccessibilityUtils.isNodesAvalibale(lists)) {
            do {
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
                                }
                            }
                        }
                    }
                    if (addResult) {
                        return COLLECT_SUCCESS
                    }
                }
                index++
                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while (ExecUtils.canscroll(lists[0], index))
            return COLLECT_END
        }
        return COLLECT_FAIL
    }

}