package com.example.jddata.action.fetch

import android.view.accessibility.AccessibilityNodeInfo
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

open class FetchSearchAction(env: Env) : BaseAction(env, ActionType.FETCH_SEARCH) {
    init {
        searchText = "剃须刀"
        appendCommand(Command().commandCode(ServiceCommand.CLICK_SEARCH).addScene(AccService.JD_HOME))
                .append(Command().commandCode(ServiceCommand.INPUT).addScene(AccService.SEARCH)
                        .setState(GlobalInfo.SEARCH_KEY, searchText!!))
                .append(Command().commandCode(ServiceCommand.SEARCH))
                .append(Command().commandCode(ServiceCommand.FETCH_PRODUCT).addScene(AccService.PRODUCT_LIST))
    }

    override fun initLogFile() {
        logFile = BaseLogFile("获取_搜索_$searchText")
    }

    var retryTime = 0

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
            ServiceCommand.FETCH_PRODUCT -> {
                return fetchProduct()
            }
        }
        return super.executeInner(command)
    }

    fun fetchProduct(): Boolean {
        val set = HashSet<String>()
        val lists = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/product_list")
        if (AccessibilityUtils.isNodesAvalibale(lists)) {
            var index = 0
            do {
                val items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/product_item_name")
                if (AccessibilityUtils.isNodesAvalibale(items)) {
                    for (item in items) {
                        val parent = AccessibilityUtils.findParentClickable(item)
                        var product = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/product_item_name"))
                        var price = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/product_item_jdPrice"))
                        if (product != null && price != null) {
                            if (product.startsWith("1 ")) {
//                                product = product.replace("1 ", "");
                            }
                            price = price.replace("¥", "")
                            val recommend = Data2(product, price)
                            if (set.add(product)) {
                                itemCount++

                                val map = HashMap<String, Any?>()
                                val row = RowData(map)
                                row.setDefaultData(env!!)
                                row.product = product?.replace("1 ", "")?.replace("\n", "")?.replace(",", "、")
                                row.price = price?.replace("\n", "")?.replace(",", "、")
                                row.biId = GlobalInfo.SEARCH
                                row.itemIndex = "${itemCount}"
                                LogUtil.dataCache(row)

                                logFile?.writeToFileAppend("收集${itemCount}点击商品：", product, price)
                                if (itemCount >= GlobalInfo.FETCH_SEARCH_NUM) {
                                    return true
                                }
                            }
                        }
                    }
                }
                index++
                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while (ExecUtils.canscroll(lists[0], index))

            logFile?.writeToFileAppend(GlobalInfo.NO_MORE_DATA)
            return true
        }
        return false
    }
}
