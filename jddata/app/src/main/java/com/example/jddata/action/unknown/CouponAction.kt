package com.example.jddata.action.unknown

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.Entity.ActionType
import com.example.jddata.GlobalInfo
import com.example.jddata.action.BaseAction
import com.example.jddata.action.Command
import com.example.jddata.action.PureCommand
import com.example.jddata.action.append
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.BaseLogFile

class CouponAction : BaseAction(ActionType.COUPON) {
    init {
        appendCommand(Command(ServiceCommand.GRID_ITEM).addScene(AccService.JD_HOME))
                .append(PureCommand(ServiceCommand.GO_BACK))

    }

    val name = GlobalInfo.COUPON
    override fun initLogFile() {
        logFile = BaseLogFile("动作_$name")
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.GRID_ITEM -> {
                logFile?.writeToFileAppend("点击$name")
                val items = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, "$name")
                if (AccessibilityUtils.isNodesAvalibale(items)) {
                    val clickParent = AccessibilityUtils.findParentClickable(items[0])
                    if (clickParent != null) {
                        val result = clickParent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        if (result) {
                            addMoveExtra("点击$name")
                        }
                        return result
                    }
                }

                return false
            }
        }
        return super.executeInner(command)
    }

}