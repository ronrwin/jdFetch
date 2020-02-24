package com.example.jddata.action.unknown

import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.Route
import com.example.jddata.GlobalInfo
import com.example.jddata.Session
import com.example.jddata.action.BaseAction
import com.example.jddata.action.Command
import com.example.jddata.shelldroid.Env
import com.example.jddata.util.BaseLogFile

class TemplateMoveAction(env: Env, route: Route) : BaseAction(env, ActionType.TEMPLATE_MOVE) {
    var name = ""
    var sessionNo = 0

    init {
        sessionNo = route.id
        setState(GlobalInfo.ROUTE, route)
        if (sessionNo < Session.sTemplates.size()) {
            val template = Session.sTemplates[sessionNo]
            appendCommands(template.actions)
            name = "${template.templateId}号"
        }
    }

    override fun initLogFile() {
        isMoveAction = true
        logFile = BaseLogFile("动作_${name}")
        addMoveExtra("账号: ${env!!.envName}: 动作: ${name}")
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {


        }
        return super.executeInner(command)
    }

}