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
    init {
        searchText = "洗发水"
        appendCommand(Command().commandCode(ServiceCommand.CLICK_SEARCH).addScene(AccService.JD_HOME))
                .append(Command().commandCode(ServiceCommand.INPUT).addScene(AccService.SEARCH)
                        .setState(GlobalInfo.SEARCH_KEY, searchText!!))
                .append(Command().commandCode(ServiceCommand.SEARCH))
    }

    override fun initLogFile() {
        isMoveAction = true
        logFile = BaseLogFile("动作_搜索_$searchText")
        val day9No = env!!.day9!!.toInt()
        addMoveExtra("动作：${day9No}")
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
        }
        return super.executeInner(command)
    }


}
