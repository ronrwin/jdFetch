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

open class MoveJdKillWorthAction(env: Env) : BaseAction(env, ActionType.MOVE_JD_KILL_WORTH) {

    init {
        appendCommand(Command().commandCode(ServiceCommand.TEMPLATE_JDKILL))
                .append(Command().commandCode(ServiceCommand.DONE).delay(2000).addScene(AccService.MIAOSHA))
                .append(Command().commandCode(ServiceCommand.MIAOSHA_TAB).delay(2000)
                        .setState(GlobalInfo.MIAOSHA_TAB, GlobalInfo.WORTH_PING))
    }

    override fun initLogFile() {
        isMoveAction = true
        logFile = BaseLogFile("动作_京东秒杀_${GlobalInfo.WORTH_PING}")
        val day9No = env!!.day9!!.toInt()
        addMoveExtra("动作：${day9No}")
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
        }
        return super.executeInner(command)
    }


}
