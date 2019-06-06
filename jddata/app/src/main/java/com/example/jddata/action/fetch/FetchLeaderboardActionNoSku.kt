package com.example.jddata.action.fetch

import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.RowData
import com.example.jddata.GlobalInfo
import com.example.jddata.action.BaseAction
import com.example.jddata.action.Command
import com.example.jddata.action.append
import com.example.jddata.util.BaseLogFile
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.shelldroid.Env
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.LogUtil
import java.util.ArrayList
import java.util.HashMap

class FetchLeaderboardActionNoSku(env: Env) : BaseAction(env, ActionType.FETCH_LEADERBOARD) {

    var tabTitles = ArrayList<String>()
    var clickedTabs = ArrayList<String>()
    var currentTab = ""
    var currentCity = ""
    var productSet = HashSet<String>()
    var tabClickCount = 0

    init {
        appendCommand(Command().commandCode(ServiceCommand.FIND_TEXT).addScene(AccService.JD_HOME))
                .append(Command().commandCode(ServiceCommand.LEADERBOARD_TAB_CONFIRM)
                        .addScene(AccService.NATIVE_COMMON).delay(8000))
                .append(Command().commandCode(ServiceCommand.LEADERBOARD_TAB)
                        .delay(10000L))
                .append(Command().commandCode(ServiceCommand.CLICK_TAB))
    }
    val name = GlobalInfo.LEADERBOARD

    override fun initLogFile() {
        logFile = BaseLogFile("获取_$name")
    }

    // 排行榜的页面比较特别，控件都是没有id的，只能根据固定的序号来判断了。
    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.LEADERBOARD_TAB_CONFIRM -> {
                val desNodes = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, "系统定位您在")
                if (AccessibilityUtils.isNodesAvalibale(desNodes)) {
                    val okNodes = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, "确定")
                    if (AccessibilityUtils.isNodesAvalibale(okNodes)) {
                        val rect = Rect()
                        okNodes[0].getBoundsInScreen(rect)
                        return ExecUtils.handleExecCommand("input tap ${rect.left+10} ${rect.top+10}")
                    }
                }
                BusHandler.instance.startCountTimeout()
                return false
            }
            ServiceCommand.LEADERBOARD_TAB -> {
                val result = leaderBoardTab()
                return result
            }
            ServiceCommand.FIND_TEXT -> {
//                logFile?.writeToFileAppend("找到并点击 ${name}")
                logFile?.writeToFileAppend("找到并点击 ${name}")
                BusHandler.instance.startCountTimeout()
                return findHomeTextClick("爆款好物尽在排行榜")
            }
            ServiceCommand.CLICK_TAB -> {
                BusHandler.instance.startCountTimeout()
                productSet.clear()
                testScroll = 0
                val result = clickTab()
                when (result) {
                    COLLECT_SUCCESS -> {
                        appendCommand(Command().commandCode(ServiceCommand.FETCH_PRODUCT).delay(2000))
                        return true
                    }
                    COLLECT_FAIL -> {
                        if (tabClickCount < GlobalInfo.TAB_COUNT) {
                            appendCommand(Command().commandCode(ServiceCommand.CLICK_TAB))
                            tabClickCount++
                        }
                        return false
                    }
                    COLLECT_END -> {
                        return true
                    }
                }
             }
            ServiceCommand.FETCH_PRODUCT -> {
                // 每次滚动之后，都需要重新从handler中获取指令
                var result = true
                if (testScroll < GlobalInfo.SCROLL_COUNT) {
                    result = fetchProductTest()
                    if (itemCount >= GlobalInfo.FETCH_NUM) {
                        appendCommand(Command().commandCode(ServiceCommand.CLICK_TAB))
                        return true
                    }
                    appendCommand(Command().commandCode(ServiceCommand.FETCH_PRODUCT))
                } else {
                    appendCommand(Command().commandCode(ServiceCommand.CLICK_TAB))
                }

                return result
            }
        }
        return super.executeInner(command)
    }

    var testScroll = 0
    fun fetchProductTest(): Boolean {
        val lists = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.widget.ScrollView")
        if (AccessibilityUtils.isNodesAvalibale(lists)) {
            val list = lists.last()

            val textNodes = AccessibilityUtils.findChildByClassname(list, "android.widget.TextView")
            if (AccessibilityUtils.isNodesAvalibale(textNodes)) {
                one@for (textNode in textNodes) {
                    if (textNode.text != null && textNode.text.toString().length > 30) {
                        val title = textNode.text.toString()
                        if (productSet.contains(title)) {
                            continue@one
                        }

                        val parent = textNode.parent
                        // 这里是一个卡片项

                        itemCount++
                        productSet.add(title)

                        val map = HashMap<String, Any?>()
                        val row = RowData(map)
                        row.setDefaultData(env!!)
                        row.product = title.replace("\n", "")?.replace(",", "、")


                        val childTextNodes = AccessibilityUtils.findChildByClassname(parent, "android.widget.TextView")
                        if (AccessibilityUtils.isNodesAvalibale(childTextNodes)) {
                            for (i in childTextNodes.indices) {
                                val child = childTextNodes[i]
                                if (child.text != null) {
                                    if ("¥".equals(child.text.toString())) {
                                        val price = childTextNodes[i + 1].text.toString()
                                        logFile?.writeToFileAppend("获取第${itemCount}个商品：${title}, ${price}")
                                        row.price = price.replace("\n", "")?.replace(",", "、")
                                    } else if ("热卖指数".equals(child.text.toString())) {
                                        val percent = childTextNodes[i + 1].text.toString()
                                        row.salePercent = percent
                                    } else if ("自营".equals(child.text.toString())) {
                                        val selfSale = true
                                        row.isSelfSale = "自营"
                                    }
                                }
                            }
                        }

                        row.biId = GlobalInfo.LEADERBOARD
                        row.itemIndex = "${clickedTabs.size}---${itemCount}"
                        row.tab = currentTab
                        row.city = ExecUtils.translate(currentCity)
                        LogUtil.dataCache(row)

                        if (itemCount >= GlobalInfo.FETCH_NUM) {
                            return true
                        }
                    }
                }
            }

            list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
            testScroll++
            return true
        }
        return false
    }

    fun clickTab(): Int {
        if (clickedTabs.size >= GlobalInfo.TAB_COUNT) {
            return COLLECT_END
        }

        val root = mService!!.rootInActiveWindow
        if (root != null) {
            val tabWrapper = AccessibilityUtils.findChildByClassname(root, "android.widget.HorizontalScrollView")
            if (AccessibilityUtils.isNodesAvalibale(tabWrapper)) {
                for (scroll in tabWrapper) {
                    val rect = Rect()
                    scroll.getBoundsInScreen(rect)
                    if (rect.top < 0 || rect.left < 0 || rect.right < 0 || rect.bottom < 0
                            || rect.bottom > ExecUtils.computeY(170)) {
                        continue
                    }

                    for (tabName in tabTitles) {
                        if (!clickedTabs.contains(tabName)) {
                            val nodes = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, tabName)
                            if (AccessibilityUtils.isNodesAvalibale(nodes)) {
                                val tabNode = nodes[0]
                                val tabRect = Rect()
                                tabNode.getBoundsInScreen(tabRect)
                                if (tabRect.left <= rect.right-5) {
                                    currentTab = tabName
                                    clickedTabs.add(currentTab)

                                    logFile?.writeToFileAppend("click tab ${currentTab},  ${tabRect}")
                                    logFile?.writeToFileAppend("input tap ${tabRect.left + 5} ${tabRect.top + 5}")
                                    ExecUtils.handleExecCommand("input tap ${tabRect.left + 5} ${tabRect.top + 5}")

                                    itemCount = 0
                                    return COLLECT_SUCCESS
                                } else {
                                    // 当前找不到，就滑动再找
                                    scroll.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                                    return COLLECT_FAIL
                                }
                            }
                        }
                    }
                }
            }
        }
        return COLLECT_FAIL
    }

    private fun leaderBoardTab(): Boolean {
        val root = mService!!.rootInActiveWindow
        if (root != null) {
            // 找第一个text，是当前城市。
            val citys = AccessibilityUtils.findChildByClassname(root, "android.widget.TextView")
            if (AccessibilityUtils.isNodesAvalibale(citys)) {
                val cityNode = citys[0]
                if (cityNode.text != null) {
                    val city = cityNode.text.toString()
                    logFile?.writeToFileAppend("城市")
                    logFile?.writeToFileAppend(city)
                    currentCity = city
                }
            }

            val scrolls = AccessibilityUtils.findChildByClassname(root, "android.widget.HorizontalScrollView")
            if (AccessibilityUtils.isNodesAvalibale(scrolls)) {
                for (scroll in scrolls) {
                    val rect = Rect()
                    scroll.getBoundsInScreen(rect)
                    if (rect.top < 0 || rect.left < 0 || rect.right < 0 || rect.bottom < 0
                            || rect.bottom > ExecUtils.computeY(170)) {
                        continue
                    }
                    val tabNodes = AccessibilityUtils.findChildByClassname(scroll, "android.widget.TextView")
                    if (AccessibilityUtils.isNodesAvalibale(tabNodes)) {
                        for (tab in tabNodes!!) {
                            if (tab.text != null) {
                                tabTitles.add(tab.text.toString())
                            }
                        }
                    }
                }

                logFile?.writeToFileAppend("标签")
                for (i in tabTitles.indices) {
                    val title = tabTitles[i]
                    logFile?.writeToFileAppend(title)

                }
                return true
            }
        }
        return false
    }

}