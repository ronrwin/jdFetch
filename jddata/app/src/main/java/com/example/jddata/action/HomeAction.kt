package com.example.jddata.action

import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.Recommend
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.RecommendWorkBook
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.ExecUtils

class HomeAction : BaseAction(ActionType.HOME) {

    init {
        appendCommand(Command(ServiceCommand.HOME_SCROLL).addScene(AccService.JD_HOME))
        workBook = RecommendWorkBook("首页")
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.HOME_SCROLL -> {
                return homeRecommendScroll()
            }
        }
        return super.executeInner(command)
    }

    /**
     * 首页-为你推荐
     */
    fun homeRecommendScroll(): Boolean {
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")
                ?: return false
        for (node in nodes) {
            var index = 0


            while (node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD));

            val recommendList = HashSet<Recommend>()
            workBook?.writeToSheetAppend("时间", "位置", "标题", "价格")
            do {
                // 推荐部分
                val items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/by_")
                if (AccessibilityUtils.isNodesAvalibale(items)) {
                    for (item in items) {
                        val titles = item.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/br2")
                        var title = AccessibilityUtils.getFirstText(titles)
                        if (title != null && title.startsWith("1 ")) {
                            title = title.replace("1 ", "");
                        }

                        val prices = item.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/br3")
                        var price = AccessibilityUtils.getFirstText(prices)

                        if (!TextUtils.isEmpty(title) && recommendList.add(Recommend(title, price))) {
                            workBook?.writeToSheetAppendWithTime("第${index+1}屏", title, price)
                            // 收集100条
                            itemCount++
                            if (itemCount >= GlobalInfo.FETCH_NUM) {
                                workBook?.writeToSheetAppend(GlobalInfo.FETCH_ENOUGH_DATE)
                                return true
                            }
                        }
                    }
                    index++
                    if (index % 10 == 0) {
                        BusHandler.instance.startCountTimeout()
                    }
                }
                Thread.sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)

            } while ((node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                            || ExecUtils.fingerScroll())
                    && index < GlobalInfo.SCROLL_COUNT)

            workBook?.writeToSheetAppend(GlobalInfo.NO_MORE_DATA)
            return true
        }
        return false
    }
}