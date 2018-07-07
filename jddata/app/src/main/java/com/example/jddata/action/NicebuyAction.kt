package com.example.jddata.action

import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand

class NicebuyAction : BaseAction(ActionType.NICE_BUY) {

    init {
        BusHandler.getInstance().mNiceBuyTitles.clear()
        BusHandler.getInstance().mNiceBuySheet = null
        appendCommand(Command(AccService.JD_HOME, ServiceCommand.NICE_BUY))
                .append(Command(AccService.WORTHBUY, ServiceCommand.NICE_BUY_SCROLL).concernResult(true))
    }
}