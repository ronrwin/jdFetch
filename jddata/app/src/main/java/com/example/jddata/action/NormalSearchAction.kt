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
        val searchSheet = SearchSheet(searchText)
        for (node in nodes) {
            val result = parseSearchRecommends(node, GlobalInfo.SCROLL_COUNT)
            for (recommend in result) {
                searchSheet.writeToSheetAppend(recommend.title, recommend.price, recommend.comment, recommend.likePercent)
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
        // 最多滑几屏
        var maxIndex = scrollCount
        if (maxIndex < 0) {
            maxIndex = 100
        }

        val recommendList = ArrayList<SearchRecommend>()
        do {
            val items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/product_list_item")
            if (items != null) {
                for (item in items) {
                    val titles = item.findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/product_item_name")
                    var title: String? = null
                    if (AccessibilityUtils.isNodesAvalibale(titles)) {
                        if (titles[0].text != null) {
                            title = titles[0].text.toString()
                        }
                    }
                    val prices = item.findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/product_item_jdPrice")
                    var price: String? = null
                    if (AccessibilityUtils.isNodesAvalibale(prices)) {
                        if (prices[0].text != null) {
                            price = prices[0].text.toString()
                        }
                    }
                    val comments = item.findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/product_item_commentNumber")
                    var comment: String? = null
                    if (AccessibilityUtils.isNodesAvalibale(comments)) {
                        if (comments[0].text != null) {
                            comment = comments[0].text.toString()
                        }
                    }

                    val percents = item.findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/product_item_good")
                    var percent: String? = null
                    if (AccessibilityUtils.isNodesAvalibale(percents)) {
                        if (percents[0].text != null) {
                            percent = percents[0].text.toString()
                        }
                    }
                    recommendList.add(SearchRecommend(title, price, comment, percent))
                }
            }
            index++
            sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
        } while ((listNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) || ExecUtils.handleExecCommand("input swipe 250 800 250 250")) && index <= maxIndex)

        val finalList = ArrayList<SearchRecommend>()
        // 排重
        val map = HashMap<String, SearchRecommend>()
        for (recommend in recommendList) {
            if (!map.containsKey(recommend.title)) {
                if (recommend.title != null) {
                    map.put(recommend.title!!, recommend)
                    finalList.add(recommend)
                }
            } else {
                val old = map[recommend.title]
                if (old != recommend) {
                    finalList.add(recommend)
                }
            }
        }
        return finalList
    }
}
