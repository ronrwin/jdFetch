package com.example.jddata.action.fetch

import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.Data3
import com.example.jddata.Entity.MessageDef
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
import kotlin.collections.HashSet

class FetchJdKillAction(env: Env) : BaseAction(env, ActionType.FETCH_JD_KILL) {

    init {
        appendCommand(Command().commandCode(ServiceCommand.FIND_TEXT).addScene(AccService.JD_HOME))
//                .append(Command().commandCode(ServiceCommand.COLLECT_ITEM).addScene(AccService.MIAOSHA))
                // 不需要抓京东秒杀sku
                .append(Command().commandCode(ServiceCommand.FETCH_PRODUCT).addScene(AccService.MIAOSHA))
    }

    var miaoshaRoundTime = ""
    override fun initLogFile() {
        var date = Date(System.currentTimeMillis())
        var miaoshaTime = if (date.hours % 2 == 0) date.hours else date.hours - 1
        if (miaoshaTime < 6) {
            miaoshaTime = 0
        }
        miaoshaRoundTime = "${miaoshaTime}点"
        logFile = BaseLogFile("获取_京东秒杀_($miaoshaRoundTime)场次")
    }

    override fun executeInner(command: Command): Boolean {
        var date = Date(System.currentTimeMillis())
        var shouldRum = false
        if (date.hours >= 10 && date.hours < 12) {
            shouldRum = true
        } else if (date.hours >= 20 && date.hours < 22) {
            shouldRum = true
        }
        if (!shouldRum) {
            BusHandler.instance.sendEmptyMessage(MessageDef.FAIL)
            return false
        }

        when (command.commandCode) {
            ServiceCommand.FIND_TEXT -> {
                logFile?.writeToFileAppend("找到并点击 \"${GlobalInfo.JD_KILL}\"")
                val nodes = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")
                if (AccessibilityUtils.isNodesAvalibale(nodes)) {
                    var index = 0
                    do {
                        val result = AccessibilityUtils.performClick(mService, "com.jingdong.app.mall:id/bmv", false)
                        if (result) {
                            return true
                        }
                        index++
                        sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
                    } while (ExecUtils.canscroll(nodes[0], index))
                }
                return false
            }
            ServiceCommand.FETCH_PRODUCT -> {
                val lists = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")

                if (AccessibilityUtils.isNodesAvalibale(lists)) {
                    for (list in lists) {
                        var index = 0
                        val set = HashSet<Data3>()
                        do {
                            val titles = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/limit_buy_product_item_name")
                            if (AccessibilityUtils.isNodesAvalibale(titles)) {
                                for (title in titles) {
                                    var addResult = false
                                    var product = title.text.toString()
                                    val parent = AccessibilityUtils.findParentClickable(title)
                                    if (parent != null) {
                                        var price = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_miaosha_price"))
                                        var originPrice = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_jd_price"))

                                        if (!TextUtils.isEmpty(product) && !TextUtils.isEmpty(price)) {
                                            if (price != null) {
                                                price = price.replace("¥", "")
                                            }
                                            if (originPrice != null) {
                                                originPrice = originPrice.replace("¥", "")
                                                originPrice = originPrice.replace("京东价", "")
                                            }

                                            val buttons = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/app_limit_buy_sale_ms_button")
                                            if (AccessibilityUtils.isNodesAvalibale(buttons)
                                                    && buttons[0].text != null
                                                    && buttons[0].text.toString().equals("立即抢购")) {
                                                val recommend = Data3(product, price, originPrice)
                                                addResult = set.add(recommend)
                                                if (addResult) {
                                                    logFile?.writeToFileAppend("采集第${set.size}商品：", product, price, originPrice)

                                                    val map = HashMap<String, Any?>()
                                                    val row = RowData(map)
                                                    row.setDefaultData(env!!)
                                                    row.product = recommend?.arg1?.replace("\n", "")?.replace(",", "、")
                                                    row.price = recommend?.arg2?.replace("\n", "")?.replace(",", "、")
                                                    row.originPrice = recommend?.arg3?.replace("\n", "")?.replace(",", "、")
                                                    row.biId = GlobalInfo.JD_KILL
                                                    row.itemIndex = "${set.size}"
                                                    LogUtil.dataCache(row)

                                                    if (set.size >= GlobalInfo.FETCH_NUM) {
                                                        return true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            index++
                            sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
                        } while (ExecUtils.canscroll(list, index))

                        logFile?.writeToFileAppend(GlobalInfo.NO_MORE_DATA)
                        return true
                    }
                }
                return false
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
                    val titles = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, item.arg1)
                    if (AccessibilityUtils.isNodesAvalibale(titles)) {
                        val parent = AccessibilityUtils.findParentClickable(titles[0])
                        if (parent != null) {
                            clickedItems.add(item)

                            val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            if (result) {
                                currentItem = item
                                appendCommands(getSkuCommands())
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

    override fun beforeLeaveProductDetail() {
        appendCommand(Command().commandCode(ServiceCommand.COLLECT_ITEM).addScene(AccService.MIAOSHA))
        super.beforeLeaveProductDetail()
    }

    override fun fetchSkuid(skuid: String): Boolean {
        itemCount++
        logFile?.writeToFileAppend("记录商品：${currentItem.toString()}, sku: $skuid")

        val map = HashMap<String, Any?>()
        val row = RowData(map)
        row.setDefaultData(env!!)
        row.sku = skuid
        row.product = currentItem?.arg1?.replace("\n", "")?.replace(",", "、")
        row.price = currentItem?.arg2?.replace("\n", "")?.replace(",", "、")
        row.originPrice = currentItem?.arg3?.replace("\n", "")?.replace(",", "、")
        row.biId = GlobalInfo.JD_KILL
        row.itemIndex = "${itemCount}"
        LogUtil.dataCache(row)

        return super.fetchSkuid(skuid)
    }

    val fetchItems = LinkedHashSet<Data3>()
    val clickedItems = LinkedHashSet<Data3>()
    var currentItem: Data3? = null

    override fun collectItems(): Int {
        if (itemCount >= GlobalInfo.FETCH_NUM) {
            return COLLECT_END
        }
        if (fetchItems.size > 0) {
            return COLLECT_SUCCESS
        }

        val lists = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")

        if (AccessibilityUtils.isNodesAvalibale(lists)) {
            for (list in lists) {
                var index = 0
                do {
                    val titles = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/limit_buy_product_item_name")
                    if (AccessibilityUtils.isNodesAvalibale(titles)) {
                        for (title in titles) {
                            var addResult = false
                            var product = title.text.toString()
                            val parent = AccessibilityUtils.findParentClickable(title)
                            if (parent != null) {
                                var price = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_miaosha_price"))
                                var originPrice = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_jd_price"))

                                if (!TextUtils.isEmpty(product) && !TextUtils.isEmpty(price)) {
                                    if (price != null) {
                                        price = price.replace("¥", "")
                                    }
                                    if (originPrice != null) {
                                        originPrice = originPrice.replace("¥", "")
                                        originPrice = originPrice.replace("京东价", "")
                                    }

                                    val buttons = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/app_limit_buy_sale_ms_button")
                                    if (AccessibilityUtils.isNodesAvalibale(buttons)
                                            && buttons[0].text != null
                                            && buttons[0].text.toString().equals("立即抢购")) {
                                        val recommend = Data3(product, price, originPrice)
                                        if (!clickedItems.contains(recommend)) {
                                            addResult = fetchItems.add(recommend)
                                            if (addResult) {
                                                logFile?.writeToFileAppend("待点击商品：", product, price, originPrice)
                                            }
                                        }
                                    }
                                }
                            }
                            if (addResult) {
                                return COLLECT_SUCCESS
                            }
                        }
                    }

                    index++
                    sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
                } while (ExecUtils.canscroll(list, index))

                logFile?.writeToFileAppend(GlobalInfo.NO_MORE_DATA)
                return COLLECT_END
            }
        }
        return COLLECT_FAIL
    }
}