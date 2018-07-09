package com.example.jddata.action

import android.os.Message
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.SearchRecommend
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.SearchSheet
import com.example.jddata.service.*
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.ExecUtils
import java.util.ArrayList
import java.util.HashMap

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

        for (node in nodes) {
            val result = parseSearchRecommends(node, GlobalInfo.SCROLL_COUNT)
            for (recommend in result) {
                sheet?.writeToSheetAppend(recommend.title, recommend.price, recommend.comment, recommend.likePercent)
            }
            return true
        }
        return false
    }

    /**
     * 收集搜索结果卡片信息
     * target: 只有标题，价格，评论数，好评率类型的卡片
     */
    private fun parseSearchRecommends(listNode: AccessibilityNodeInfo, scrollCount: Int): ArrayList<SearchRecommend> {
        var index = 0

        val recommendList = ArrayList<SearchRecommend>()
        do {
            val items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/product_list_item")
            if (items != null) {
                for (item in items) {
                    val titles = item.findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/product_item_name")
                    val title = AccessibilityUtils.getFirstText(titles)

                    val prices = item.findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/product_item_jdPrice")
                    val price = AccessibilityUtils.getFirstText(prices)

                    val comments = item.findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/product_item_commentNumber")
                    val comment = AccessibilityUtils.getFirstText(comments)

                    val percents = item.findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/product_item_good")
                    val percent = AccessibilityUtils.getFirstText(percents)
                    recommendList.add(SearchRecommend(title, price, comment, percent))
                }
            }
            index++
            sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
        } while ((listNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                        || ExecUtils.handleExecCommand("input swipe 250 800 250 250"))
                && index < scrollCount)

        return ExecUtils.filterSingle(recommendList)
    }
}
