package com.example.jddata.action

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.SearchRecommend
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.SearchSheet
import com.example.jddata.service.*
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.LogUtil

class NormalSearchAction(searchText: String) : SearchAction(searchText) {

    init {
        appendCommand(Command(ServiceCommand.SEARCH_DATA).addScene(AccService.PRODUCT_LIST))
        sheet = SearchSheet(searchText)
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.SEARCH_DATA -> return searchData()
        }
        return super.executeInner(command)
    }

    private fun searchData(): Boolean {
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/product_list")
                ?: return false

        var index = 0

        sheet?.writeToSheetAppendWithTime("开始抓取数据")
        sheet?.writeToSheetAppend("")
        sheet?.writeToSheetAppend("时间", "位置", "标题", "价格", "评价", "好评率")
        val recommendList = HashSet<SearchRecommend>()

        do {
            val items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/product_list_item")
            if (items != null) {
                for (item in items) {
                    val titles = item.findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/product_item_name")
                    var title = AccessibilityUtils.getFirstText(titles)
                    if (title != null && title.startsWith("1 ")) {
                        title = title.replace("1 ", "");
                    }

                    val prices = item.findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/product_item_jdPrice")
                    val price = AccessibilityUtils.getFirstText(prices)

                    val comments = item.findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/product_item_commentNumber")
                    val comment = AccessibilityUtils.getFirstText(comments)

                    val percents = item.findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/product_item_good")
                    val percent = AccessibilityUtils.getFirstText(percents)

                    if (recommendList.add(SearchRecommend(title, price, comment, percent))) {
                        sheet?.writeToSheetAppendWithTime("第${index+1}屏", title, price, comment, percent)
                        itemCount++
                        if (itemCount >= GlobalInfo.FETCH_NUM) {
                            sheet?.writeToSheetAppend("采集够 ${GlobalInfo.FETCH_NUM} 条数据")
                            LogUtil.writeLog("采集够 ${GlobalInfo.FETCH_NUM} 条数据")
                            return true
                        }
                    }
                }
            }
            index++
            if (index % 10 == 0) {
                BusHandler.instance.startCountTimeout()
            }
            sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
        } while ((nodes[0].performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                        || ExecUtils.handleExecCommand("input swipe 250 800 250 250"))
                && index < GlobalInfo.SCROLL_COUNT)

        sheet?.writeToSheetAppend("。。。 没有更多数据")
        return true
    }
}
