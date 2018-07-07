package com.example.jddata.action

import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.excel.DmpSheet
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand

class DmpAction : BaseAction(ActionType.DMP) {

    init {
        BusHandler.getInstance().mDmpSheet = DmpSheet()
        for (i in 0..7) {
            appendCommand(Command(AccService.JD_HOME, ServiceCommand.DMP_CLICK).delay(5000L))
                    .append(Command(AccService.BABEL_ACTIVITY, ServiceCommand.DMP_TITLE).delay(3000L)
                            .addScene(AccService.WEBVIEW_ACTIVITY))
                    .append(Command(AccService.BABEL_ACTIVITY, ServiceCommand.GO_BACK)
                            .addScene(AccService.WEBVIEW_ACTIVITY))
        }
    }
}