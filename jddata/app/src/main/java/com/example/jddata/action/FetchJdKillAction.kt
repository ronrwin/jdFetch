package com.example.jddata.action

import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.Data3
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.BaseLogFile
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.ExecUtils
import java.util.*

class FetchJdKillAction : BaseAction(ActionType.FETCH_JD_KILL) {

    init {
        appendCommand(Command(ServiceCommand.HOME_JD_KILL).addScene(AccService.JD_HOME))
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
            ServiceCommand.HOME_JD_KILL -> {
                logFile?.writeToFileAppendWithTime("找到并点击 \"${GlobalInfo.JD_KILL}\"")
                return AccessibilityUtils.performClick(mService, "com.jingdong.app.mall:id/bmv", false)
            }
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
                                        .append(Command(ServiceCommand.COLLECT_ITEM).addScene(AccService.MIAOSHA))
                                val parent = AccessibilityUtils.findParentClickable(titles[0])
                                if (parent != null) {
                                    val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                    if (result) {
                                        logFile?.writeToFileAppendWithTime("点击第${itemCount+1}商品：", currentItem!!.arg1, currentItem!!.arg2, currentItem!!.arg3)
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

    val fetchItems = LinkedHashSet<Data3>()
    val clickedItems = LinkedHashSet<Data3>()
    var currentItem: Data3? = null

    fun collectItems(): Int {
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

                                            if (itemCount >= GlobalInfo.FETCH_NUM) {
                                                return COLLECT_SUCCESS
                                            }
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