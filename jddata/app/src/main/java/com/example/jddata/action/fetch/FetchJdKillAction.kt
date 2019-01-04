package com.example.jddata.action.fetch

import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.Data3
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
import java.util.*

class FetchJdKillAction : BaseAction(ActionType.FETCH_JD_KILL) {

    init {
        appendCommand(Command(ServiceCommand.FIND_TEXT).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.COLLECT_ITEM).addScene(AccService.MIAOSHA))
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
        when (command.commandCode) {
            ServiceCommand.FIND_TEXT -> {
                logFile?.writeToFileAppendWithTime("找到并点击 \"${GlobalInfo.JD_KILL}\"")
                return AccessibilityUtils.performClick(mService, "com.jingdong.app.mall:id/bmv", false)
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
                            appendCommand(Command(ServiceCommand.GET_SKU).addScene(AccService.PRODUCT_DETAIL).delay(2000))
                                    .append(PureCommand(ServiceCommand.GO_BACK))
                                    .append(Command(ServiceCommand.COLLECT_ITEM).addScene(AccService.MIAOSHA))

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

    override fun fetchSkuid(skuid: String): Boolean {
        itemCount++
        // todo: 加数据库
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
                                if (buttons != null && buttons[0].text != null && buttons[0].text.toString().equals("立即抢购")) {
                                    val recommend = Data3(product, price, originPrice)
                                    if (!clickedItems.contains(recommend)) {
                                        addResult = fetchItems.add(recommend)
                                        if (addResult) {
                                            logFile?.writeToFileAppendWithTime("待点击商品：", product, price, originPrice)
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
            } while (ExecUtils.canscroll(list, index))

            logFile?.writeToFileAppendWithTime(GlobalInfo.NO_MORE_DATA)
            return COLLECT_FAIL
        }
        return COLLECT_FAIL
    }
}