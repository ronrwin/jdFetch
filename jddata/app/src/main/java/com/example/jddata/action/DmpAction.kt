package com.example.jddata.action

import com.example.jddata.Entity.ActionType
import com.example.jddata.excel.DmpWorkBook
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand

class DmpAction : BaseAction(ActionType.DMP) {
    init {
        workBook = DmpWorkBook()
        for (i in 1..8) {
            appendCommand(Command(ServiceCommand.DMP_CLICK).addScene(AccService.JD_HOME)
                    .delay(5000L).setState("index", i))
                    .append(Command(ServiceCommand.DMP_TITLE).delay(2000L)
                            .addScene(AccService.WEBVIEW_ACTIVITY)
                            .addScene(AccService.JSHOP)
                            .addScene(AccService.BABEL_ACTIVITY))
                    .append(PureCommand(ServiceCommand.GO_BACK))
        }
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.DMP_CLICK -> {
                val index = command.getState("index")
                workBook?.writeToSheetAppend("")
                workBook?.writeToSheetAppendWithTime("点击 第${index}个广告")
                return CommonConmmand.dmpclick(mService!!)
            }
            ServiceCommand.DMP_TITLE -> {
                return dmpTitle()
            }
        }
        return super.executeInner(command)
    }

    fun dmpTitle(): Boolean {
        var nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/ff")
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) {
            nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jshop:id/jshop_shopname")
        }
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) {
            nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/ab7")
        }

        if (AccessibilityUtils.isNodesAvalibale(nodes)) {
            val titleNode = nodes!![0]
            if (titleNode.text != null) {
                val title = titleNode.text.toString()
                workBook?.writeToSheetAppend("时间", "广告标题")
                workBook?.writeToSheetAppendWithTime("$title")
                return true
            }
        }
        return false
    }
}