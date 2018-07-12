package com.example.jddata.action

import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.SearchRecommend
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.SearchWorkBook
import com.example.jddata.service.*
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.ExecUtils

class NormalSearchAction(searchText: String) : SearchAction(searchText) {

    init {
        appendCommand(Command(ServiceCommand.SEARCH_DATA).addScene(AccService.PRODUCT_LIST))
        workBook = SearchWorkBook(searchText)
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

        workBook?.writeToSheetAppendWithTime("开始抓取数据")
        workBook?.writeToSheetAppend("")
        workBook?.writeToSheetAppend("时间", "位置", "标题", "价格", "评价", "好评率")
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

                    if (!TextUtils.isEmpty(title) && recommendList.add(SearchRecommend(title, price, comment, percent))) {
                        workBook?.writeToSheetAppendWithTime("第${index+1}屏", title, price, comment, percent)
                        itemCount++
                        if (itemCount >= GlobalInfo.FETCH_NUM) {
                            workBook?.writeToSheetAppend(GlobalInfo.FETCH_ENOUGH_DATE)
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
                        || ExecUtils.fingerScroll())
                && index < GlobalInfo.SCROLL_COUNT)

        workBook?.writeToSheetAppend(GlobalInfo.NO_MORE_DATA)
        return true
    }
}
