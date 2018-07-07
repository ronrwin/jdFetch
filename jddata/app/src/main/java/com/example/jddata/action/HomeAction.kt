package com.example.jddata.action

import android.os.Message
import com.example.jddata.Entity.ActionType
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand

class HomeAction : BaseAction(ActionType.HOME) {

    init {
        appendCommand(Command(AccService.JD_HOME, ServiceCommand.HOME_SCROLL))
    }

}