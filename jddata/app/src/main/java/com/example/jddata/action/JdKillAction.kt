package com.example.jddata.action

import android.os.Message
import com.example.jddata.Entity.ActionType
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand

class JdKillAction : BaseAction(ActionType.JD_KILL) {

    init {
        appendCommand(Command(AccService.JD_HOME, ServiceCommand.HOME_JD_KILL))
                .append(Command(AccService.MIAOSHA, ServiceCommand.JD_KILL_SCROLL))
    }

}