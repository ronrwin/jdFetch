package com.example.jddata.action

import com.example.jddata.Entity.ActionType
import com.example.jddata.excel.BaseWorkBook
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand

class MoveDmpAction : BaseAction(ActionType.MOVE_DMP) {
    init {
        appendCommand(Command(ServiceCommand.DMP_CLICK).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.DMP_TITLE).delay(2000L)
                        .addScene(AccService.WEBVIEW_ACTIVITY)
                        .addScene(AccService.JSHOP)
                        .addScene(AccService.BABEL_ACTIVITY))
    }

    override fun initWorkbook() {
        workBook = BaseWorkBook("动作_dmp广告")
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.DMP_CLICK -> {
                return CommonConmmand.dmpclick(mService!!)
            }
            ServiceCommand.DMP_TITLE -> {
                val result = dmpTitle()
                sleep(2000L)
                return result
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
            } else {
                if (titleNode.className.equals("android.widget.ImageView")) {
                    workBook?.writeToSheetAppend("时间", "广告标题")
                    workBook?.writeToSheetAppendWithTime("京东超市")
                    return true
                }
            }
        }
        return false
    }
}