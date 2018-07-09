package com.example.jddata.action

import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.excel.DmpSheet
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand

class DmpAction : BaseAction(ActionType.DMP) {

    var mDmpSheet: DmpSheet? = null

    init {
        mDmpSheet = DmpSheet()
        for (i in 0..7) {
            appendCommand(Command(ServiceCommand.DMP_CLICK).addScene(AccService.JD_HOME).delay(5000L))
                    .append(Command(ServiceCommand.DMP_TITLE).delay(3000L)
                            .addScene(AccService.WEBVIEW_ACTIVITY)
                            .addScene(AccService.BABEL_ACTIVITY))
                    .append(PureCommand(ServiceCommand.GO_BACK))
        }
    }
}