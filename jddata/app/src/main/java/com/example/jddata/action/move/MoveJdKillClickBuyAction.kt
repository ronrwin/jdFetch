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

open class MoveJdKillClickBuyAction(env: Env) : BaseAction(env, ActionType.MOVE_JD_KILL_CLICK_BUY) {

    init {
        appendCommand(Command().commandCode(ServiceCommand.TEMPLATE_JDKILL))
                .append(Command().commandCode(ServiceCommand.DONE).delay(2000).addScene(AccService.MIAOSHA))
                .append(Command().commandCode(ServiceCommand.TEMPLATE_JDKILL_SELECT).delay(3000))
                .append(Command().commandCode(ServiceCommand.DONE).delay(5000)
                        .addScene(AccService.PRODUCT_DETAIL))
                .append(Command().commandCode(ServiceCommand.TEMPLATE_ADD_TO_CART).delay(3000))
                .append(Command().commandCode(ServiceCommand.PRODUCT_CONFIRM)
                        .delay(3000).canSkip(true))
    }

    override fun initLogFile() {
        isMoveAction = true
        logFile = BaseLogFile("动作_京东秒杀_点击商品_加购")
        var day9No = getState(GlobalInfo.MOVE_NO) as Int
        addMoveExtra("动作： " + day9No)
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
        }
        return super.executeInner(command)
    }


}
