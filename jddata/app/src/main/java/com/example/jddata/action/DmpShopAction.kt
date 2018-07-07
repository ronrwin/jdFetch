package com.example.jddata.action

import com.example.jddata.Entity.ActionType
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand

class DmpShopAction : BaseAction(ActionType.DMP_AND_SHOP) {

    init {
        for (i in 0..7) {
            appendCommand(Command(AccService.JD_HOME, ServiceCommand.DMP_CLICK).delay(5000L))
                    .append(Command(AccService.BABEL_ACTIVITY, ServiceCommand.DMP_FIND_PRICE).delay(3000L).concernResult(true)
                            .addScene(AccService.WEBVIEW_ACTIVITY))
                    .append(Command(AccService.BABEL_ACTIVITY, ServiceCommand.GO_BACK)
                            .addScene(AccService.WEBVIEW_ACTIVITY))
        }
    }

}