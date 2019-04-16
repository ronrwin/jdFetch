package com.example.jddata.action.unknown

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.Route
import com.example.jddata.GlobalInfo
import com.example.jddata.MainApplication
import com.example.jddata.Session
import com.example.jddata.action.BaseAction
import com.example.jddata.action.Command
import com.example.jddata.service.ServiceCommand
import com.example.jddata.shelldroid.Env
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.BaseLogFile
import com.example.jddata.util.ExecUtils
import java.util.*

class TemplateMoveAction(env: Env, route: Route) : BaseAction(env, ActionType.TEMPLATE_MOVE) {
    var name = ""

    init {
        val sessionNo = route.id
        setState(GlobalInfo.ROUTE, route)
//        val sessionNo = 2
        if (sessionNo < Session.sTemplates.size()) {
            val template = Session.sTemplates[sessionNo]
            appendCommands(template.actions)
            name = "${template.templateId}号"
        }
    }

    override fun initLogFile() {
        isMoveAction = true
        logFile = BaseLogFile("动作_${name}")
        addMoveExtra("即将执行动作: ${name}")
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {


        }
        return super.executeInner(command)
    }

}