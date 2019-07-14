package com.example.jddata.action.move

import com.example.jddata.Entity.ActionType
import com.example.jddata.GlobalInfo
import com.example.jddata.action.BaseAction
import com.example.jddata.action.Command
import com.example.jddata.action.append
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.shelldroid.Env
import com.example.jddata.util.BaseLogFile

open class MoveJilieShopProductAction(env: Env) : BaseAction(env, ActionType.MOVE_JILIE_SHOP_PRODUCT) {

    init {
        searchText = "吉列"
        appendCommand(Command().commandCode(ServiceCommand.CLICK_SEARCH).addScene(AccService.JD_HOME))
                .append(Command().commandCode(ServiceCommand.INPUT).addScene(AccService.SEARCH)
                        .setState(GlobalInfo.SEARCH_KEY, searchText!!))
                .append(Command().commandCode(ServiceCommand.SEARCH))
                .append(Command().commandCode(ServiceCommand.SEARCH_SHOP)
                        .addScene(AccService.PRODUCT_LIST).delay(3000))
                .append(Command().commandCode(ServiceCommand.JSHOP_DMP_TAB_PRODUCT).addScene(AccService.JSHOP).delay(10000))
                .append(Command().commandCode(ServiceCommand.JSHOP_DMP_CLICK).delay(5000))

    }

    override fun initLogFile() {
        isMoveAction = true
        logFile = BaseLogFile("动作_吉列_店铺_商品")
        val tem = getState(GlobalInfo.MOVE_NO)
        if (tem != null) {
            var day9No = tem as Int
            addMoveExtra("动作： " + day9No)
        }
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
        }
        return super.executeInner(command)
    }


}
