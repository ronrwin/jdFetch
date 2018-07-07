package com.example.jddata.action

import android.os.Message
import com.example.jddata.Entity.ActionType
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand

class CartAction : BaseAction(ActionType.CART) {
    init {
        appendCommand(Command(AccService.JD_HOME, ServiceCommand.CART_TAB))
                .append(Command(AccService.JD_HOME, ServiceCommand.CART_SCROLL))
    }

}