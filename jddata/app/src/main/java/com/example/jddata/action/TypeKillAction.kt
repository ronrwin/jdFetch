package com.example.jddata.action

import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand

class TypeKillAction : BaseAction(ActionType.TYPE_KILL) {

    init {
        BusHandler.getInstance().mTypePrices.clear()
        BusHandler.getInstance().mTypeSheet = null
        appendCommand(Command(AccService.JD_HOME, ServiceCommand.HOME_TYPE_KILL))
                .append(Command(AccService.MIAOSHA, ServiceCommand.HOME_TYPE_KILL_SCROLL).concernResult(true))
    }
}