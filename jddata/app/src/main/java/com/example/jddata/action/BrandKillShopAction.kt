package com.example.jddata.action

import android.os.Message
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand

class BrandKillShopAction : BaseAction(ActionType.BRAND_KILL_AND_SHOP) {

    init {
        BusHandler.getInstance().mBrandEntitys.clear()
        appendCommand(Command(AccService.JD_HOME, ServiceCommand.HOME_BRAND_KILL))
                .append(Command(AccService.MIAOSHA, ServiceCommand.HOME_BRAND_KILL_SCROLL).concernResult(true))
    }

}