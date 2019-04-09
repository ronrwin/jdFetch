package com.example.jddata.action.fetch

import com.example.jddata.Entity.ActionType
import com.example.jddata.GlobalInfo
import com.example.jddata.action.BaseAction
import com.example.jddata.action.Command
import com.example.jddata.action.append
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.shelldroid.Env
import com.example.jddata.util.BaseLogFile

class FetchDmpAction(env: Env) : BaseAction(env, ActionType.FETCH_DMP) {

    init {
        appendCommand(Command().commandCode(ServiceCommand.HOME_DMP).delay(4000))
                .append(Command().commandCode(ServiceCommand.DMP_TITLE)
//                        .addScene(AccService.WEBVIEW_ACTIVITY)
//                        .addScene(AccService.BABEL_ACTIVITY)
//                        .addScene(AccService.JSHOP)
                        .delay(5000))
                .append(Command().commandCode(ServiceCommand.HOME))
        for (i in 0 until 2) {
            appendCommand(Command().commandCode(ServiceCommand.HOME_DMP).delay(4000).addScene(AccService.JD_HOME))
                    .append(Command().commandCode(ServiceCommand.DMP_TITLE)
//                            .addScene(AccService.WEBVIEW_ACTIVITY)
//                            .addScene(AccService.BABEL_ACTIVITY)
//                            .addScene(AccService.JSHOP)
                            .delay(5000))
                    .append(Command().commandCode(ServiceCommand.HOME))
        }
    }

    override fun initLogFile() {
        logFile = BaseLogFile("获取_" + GlobalInfo.DMP)
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {

        }
        return super.executeInner(command)
    }
}