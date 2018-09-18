package com.example.jddata.action

import com.example.jddata.Entity.ActionType
import com.example.jddata.MainApplication
import com.example.jddata.excel.BaseWorkBook
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand

class MoveDmpAction : BaseAction(ActionType.MOVE_DMP) {
    init {
        MainApplication.copyPic("haifeisi.png")

        appendCommand(PureCommand(ServiceCommand.CAPTURE_SCAN))
                .append(Command(ServiceCommand.SCAN_CLBUM).delay(3000L)
                        .addScene(AccService.CAPTURE_SCAN))
                .append(Command(ServiceCommand.SCAN_PIC).delay(3000L)
                        .addScene(AccService.PHOTO_ALBUM))
                .append(Command(ServiceCommand.DMP_TITLE).delay(2000L)
                        .addScene(AccService.WEBVIEW_ACTIVITY)
                        .addScene(AccService.JSHOP)
                        .addScene(AccService.BABEL_ACTIVITY))

//        appendCommand(Command(ServiceCommand.DMP_CLICK).addScene(AccService.JD_HOME))
//                .append(Command(ServiceCommand.DMP_TITLE).delay(2000L)
//                        .addScene(AccService.WEBVIEW_ACTIVITY)
//                        .addScene(AccService.JSHOP)
//                        .addScene(AccService.BABEL_ACTIVITY))
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
                workBook?.writeToSheetAppendWithTime("dmp广告标题：$title")
                addExtra("dmp广告，标题：$title")
                return true
            } else {
                if (titleNode.className.equals("android.widget.ImageView")) {
                    workBook?.writeToSheetAppendWithTime("京东超市")
                    addExtra("dmp广告，标题：京东超市")
                    return true
                }
            }
        }
        return false
    }
}