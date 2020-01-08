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

open class MoveSearchQuxieiClickAction(env: Env) : BaseAction(env, ActionType.MOVE_SEARCH_QUXIE_CLICK) {

    init {
        searchText = "去屑"
        clickText = "海飞丝"
        appendCommand(Command().commandCode(ServiceCommand.CLICK_SEARCH).addScene(AccService.JD_HOME))
                .append(Command().commandCode(ServiceCommand.INPUT).addScene(AccService.SEARCH)
                        .setState(GlobalInfo.SEARCH_KEY, searchText!!))
                .append(Command().commandCode(ServiceCommand.SEARCH))
                .append(Command().commandCode(ServiceCommand.SEARCH_CSELECT).addScene(AccService.PRODUCT_LIST))
    }

    override fun initLogFile() {
        isMoveAction = true
        logFile = BaseLogFile("动作_搜索_${searchText}_点击${clickText}")
        val day9No = env!!.moveId!!.toInt()
        addMoveExtra("动作：${day9No}")
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {

        }
        return super.executeInner(command)
    }


}
