package com.example.jddata.action

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.Entity.ActionType
import com.example.jddata.service.*
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.ExecUtils

open class SearchAction(var searchText: String) : BaseAction(ActionType.SEARCH) {
    init {
        appendCommand(Command(ServiceCommand.CLICK_SEARCH).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.INPUT).setState("searchText", searchText).addScene(AccService.SEARCH))
                .append(PureCommand(ServiceCommand.SEARCH))
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
            ServiceCommand.CLICK_SEARCH -> {
                sheet?.writeToSheetAppendWithTime("点击搜索栏")
                return ExecUtils.handleExecCommand("input tap 250 75")
            }
            ServiceCommand.INPUT -> {
                val text = command.getState("searchText")
                if (text is String) {
                    sheet?.writeToSheetAppendWithTime("输入 $text")
                    return commandInput("android.widget.EditText", "com.jd.lib.search:id/search_text", text)
                }
            }
            ServiceCommand.SEARCH -> {
                sheet?.writeToSheetAppendWithTime("点击搜索按钮")
                return AccessibilityUtils.performClick(mService, "com.jingdong.app.mall:id/avs", false)
            }
        }
        return super.executeInner(command)
    }

    /**
     * 输入内容
     */
    private fun commandInput(className: String, viewId: String, text: String): Boolean {
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, viewId)
                ?: return false

        for (node in nodes) {
            if (className == node.className) {
                if (node.isEnabled && node.isClickable) {
                    ExecUtils.checkClipBoard(text)
                    node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
                    node.performAction(AccessibilityNodeInfo.ACTION_PASTE)
                    return true
                }
            }
        }
        return false
    }
}
