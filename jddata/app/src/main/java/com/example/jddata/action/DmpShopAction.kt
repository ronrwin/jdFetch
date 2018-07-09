package com.example.jddata.action

import com.example.jddata.Entity.ActionType
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand

class DmpShopAction : BaseAction(ActionType.DMP_AND_SHOP) {

    init {
        for (i in 0..7) {
            appendCommand(Command(ServiceCommand.DMP_CLICK).delay(5000L).addScene(AccService.JD_HOME))
                    .append(Command(ServiceCommand.DMP_FIND_PRICE).delay(3000L).concernResult(true)
                            .addScene(AccService.BABEL_ACTIVITY)
                            .addScene(AccService.WEBVIEW_ACTIVITY))
                    .append(PureCommand(ServiceCommand.GO_BACK))
        }
    }


}