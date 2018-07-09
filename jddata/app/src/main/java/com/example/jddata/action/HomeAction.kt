package com.example.jddata.action

import android.os.Message
import com.example.jddata.Entity.ActionType
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.RecommendSheet
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand

class HomeAction : BaseAction(ActionType.HOME) {

    init {
        appendCommand(Command(ServiceCommand.HOME_SCROLL).addScene(AccService.JD_HOME))
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
            val homeSheet = RecommendSheet("首页")
            val result = CommonConmmand.parseRecommends(mService!!, node, GlobalInfo.SCROLL_COUNT)
            for (recommend in result) {
                homeSheet.writeToSheetAppend(recommend.title, recommend.price)
            }
            return true
        }
        return false
    }
}