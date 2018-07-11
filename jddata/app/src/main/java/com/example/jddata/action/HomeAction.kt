package com.example.jddata.action

import android.os.Message
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.Recommend
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.RecommendSheet
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand
import com.example.jddata.util.ExecUtils
import java.util.ArrayList

class HomeAction : BaseAction(ActionType.HOME) {

    init {
        appendCommand(Command(ServiceCommand.HOME_SCROLL).addScene(AccService.JD_HOME))
        sheet = RecommendSheet("首页")
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
            sheet?.writeToSheetAppendWithTime("时间", "位置", "标题", "价格")
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

                        if (recommendList.add(Recommend(title, price))) {
                            sheet?.writeToSheetAppendWithTime("第${index+1}屏", title, price)
                            // 收集100条
                            itemCount++
                            if (itemCount >= GlobalInfo.FETCH_NUM) {
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
                            || ExecUtils.handleExecCommand("input swipe 250 800 250 250"))
                    && index < GlobalInfo.SCROLL_COUNT)

            return true
        }
        return false
    }
}