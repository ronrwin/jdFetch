package com.example.jddata.action

import com.example.jddata.Entity.ActionType
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand

class WorthBuyAction : BaseAction(ActionType.WORTH_BUY) {

    init {
        appendCommand(Command(AccService.JD_HOME, ServiceCommand.WORTH_BUY))
                .append(Command(AccService.WORTHBUY, ServiceCommand.WORTH_BUY_SCROLL))
    }
}