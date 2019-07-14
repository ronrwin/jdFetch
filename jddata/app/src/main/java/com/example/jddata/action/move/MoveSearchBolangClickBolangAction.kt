package com.example.jddata.action.move

import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.Entity.ActionType
import com.example.jddata.GlobalInfo
import com.example.jddata.action.BaseAction
import com.example.jddata.action.Command
import com.example.jddata.action.append
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.shelldroid.Env
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.BaseLogFile
import com.example.jddata.util.ExecUtils

open class MoveSearchBolangClickBolangAction(env: Env) : BaseAction(env, ActionType.MOVE_SEARCH_BOLANG_CLICK_BOLANG) {

    init {
        searchText = "博朗"
        clickText = "博朗"
        appendCommand(Command().commandCode(ServiceCommand.CLICK_SEARCH).addScene(AccService.JD_HOME))
                .append(Command().commandCode(ServiceCommand.INPUT).addScene(AccService.SEARCH)
                        .setState(GlobalInfo.SEARCH_KEY, searchText!!))
                .append(Command().commandCode(ServiceCommand.SEARCH))
                .append(Command().commandCode(ServiceCommand.SEARCH_CSELECT).addScene(AccService.PRODUCT_LIST).delay(3000))
    }

    override fun initLogFile() {
        isMoveAction = true
        logFile = BaseLogFile("动作_搜索_${searchText}_点击${clickText}")
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
