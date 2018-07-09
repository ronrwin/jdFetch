package com.example.jddata.action

import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.excel.DmpSheet
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand

class DmpAction : BaseAction(ActionType.DMP) {

    var mDmpSheet: DmpSheet? = null

    init {
        mDmpSheet = DmpSheet()
        for (i in 0..7) {
            appendCommand(Command(ServiceCommand.DMP_CLICK).addScene(AccService.JD_HOME).delay(5000L))
                    .append(Command(ServiceCommand.DMP_TITLE).delay(3000L)
                            .addScene(AccService.WEBVIEW_ACTIVITY)
                            .addScene(AccService.BABEL_ACTIVITY))
                    .append(PureCommand(ServiceCommand.GO_BACK))
        }
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.DMP_CLICK -> {
                return CommonConmmand.dmpclick(mService!!)
            }
            ServiceCommand.DMP_TITLE -> {
                return dmpTitle()
            }
        }
        return super.executeInner(command)
    }

    fun dmpTitle(): Boolean {
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/ff")
        if (AccessibilityUtils.isNodesAvalibale(nodes)) {
            val titleNode = nodes!![0]
            if (titleNode.text != null) {
                val title = titleNode.text.toString()
                mDmpSheet!!.writeToSheetAppend(title)
            }
        }
        return false
    }
}