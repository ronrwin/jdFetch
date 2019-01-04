package com.example.jddata.action.unknown

import com.example.jddata.Entity.ActionType
import com.example.jddata.GlobalInfo
import com.example.jddata.action.BaseAction
import com.example.jddata.action.Command
import com.example.jddata.action.PureCommand
import com.example.jddata.action.append
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.BaseLogFile

class FlashBuyAction : BaseAction(ActionType.FLASH_BUY) {

    init {
        appendCommand(Command(ServiceCommand.FIND_TEXT).addScene(AccService.JD_HOME))
                .append(PureCommand(ServiceCommand.GO_BACK))
    }

    val name = GlobalInfo.FLASH_BUY
    override fun initLogFile() {
        logFile = BaseLogFile("动作_$name")
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.FIND_TEXT -> {
                logFile?.writeToFileAppendWithTime("找到并点击 $name")
                val result =  findHomeTextClick(name)
                if (result) {
                    addMoveExtra("点击$name")
                }
                return result
            }
        }
        return super.executeInner(command)
    }


}