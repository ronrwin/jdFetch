package com.example.jddata.action.move

import com.example.jddata.Entity.ActionType
import com.example.jddata.GlobalInfo
import com.example.jddata.action.BaseAction
import com.example.jddata.action.Command
import com.example.jddata.action.append
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.shelldroid.Env
import com.example.jddata.util.BaseLogFile
import com.example.jddata.util.ExecUtils

open class MoveSearchAction(env: Env) : BaseAction(env, ActionType.MOVE_SEARCH) {
    var searchText: String? = null

    init {
        searchText = "洗发水"
        appendCommand(Command().commandCode(ServiceCommand.CLICK_SEARCH).addScene(AccService.JD_HOME))
                .append(Command().commandCode(ServiceCommand.INPUT).addScene(AccService.SEARCH)
                        .setState(GlobalInfo.SEARCH_KEY, searchText!!))
                .append(Command().commandCode(ServiceCommand.SEARCH))
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
                val text = command.states.get(GlobalInfo.SEARCH_KEY)
                if (text is String) {
                    logFile?.writeToFileAppend("输入 $text")
                    addMoveExtra("输入 $text")
                    return ExecUtils.commandInput(mService!!, "android.widget.EditText", "com.jd.lib.search:id/search_text", text)
                }
            }
        }
        return super.executeInner(command)
    }


}
