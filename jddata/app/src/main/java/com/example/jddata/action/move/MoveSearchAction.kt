package com.example.jddata.action.move

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.Entity.ActionType
import com.example.jddata.action.BaseAction
import com.example.jddata.action.Command
import com.example.jddata.action.PureCommand
import com.example.jddata.action.append
import com.example.jddata.util.BaseLogFile
import com.example.jddata.service.*
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.ExecUtils

open class MoveSearchAction(actionType: String, map: HashMap<String, String>?) : BaseAction(actionType, map) {
    var searchText: String? = null

    constructor(map: HashMap<String, String>?): this(ActionType.MOVE_SEARCH, map)

    init {
        searchText = map!!.get("searchText")!!
        setState("searchText", searchText!!)
        appendCommand(Command(ServiceCommand.CLICK_SEARCH).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.INPUT).addScene(AccService.SEARCH))
                .append(PureCommand(ServiceCommand.SEARCH).addScene(AccService.SEARCH))
    }

    override fun initLogFile() {
        logFile = BaseLogFile("动作_搜索_$searchText")
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
            ServiceCommand.CLICK_SEARCH -> {
                logFile?.writeToFileAppend("点击搜索栏")
                addMoveExtra("点击搜索栏")
                return ExecUtils.tapCommand(250, 75)
            }
            ServiceCommand.INPUT -> {
                val text = getState("searchText")
                if (text is String) {
                    logFile?.writeToFileAppend("输入 $text")
                    addMoveExtra("输入 $text")
                    return commandInput("android.widget.EditText", "com.jd.lib.search:id/search_text", text)
                }
            }
            ServiceCommand.SEARCH -> {
                logFile?.writeToFileAppend("点击搜索按钮")
                val result =  AccessibilityUtils.performClick(mService, "com.jingdong.app.mall:id/avs", false)
                return result
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
