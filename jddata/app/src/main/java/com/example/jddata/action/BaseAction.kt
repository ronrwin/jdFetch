package com.example.jddata.action

import com.example.jddata.Entity.ActionType
import com.example.jddata.GlobalInfo
import com.example.jddata.MainApplication
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.SharedPreferenceHelper

open class BaseAction(actionType: String, map: HashMap<String, String>?) : Action(actionType, map) {

    constructor(actionType: String): this(actionType, null)

    init {
        var needCloseAd = true
        if (GlobalInfo.sOneKeyRun) {
            if (!mActionType.equals(ActionType.FETCH_JD_KILL) || !mActionType.equals(ActionType.FETCH_SEARCH)) {
                needCloseAd = false
            }
        }

        appendCommand(Command(ServiceCommand.AGREE).addScene(AccService.PRIVACY).canSkip(true))
                .append(Command(ServiceCommand.HOME_TAB).addScene(AccService.JD_HOME))
        if (needCloseAd) {
            appendCommand(PureCommand(ServiceCommand.CLOSE_AD).delay(12000L))
        } else {
            appendCommand(PureCommand(ServiceCommand.CLOSE_AD).delay(6000L))
        }
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
            ServiceCommand.AGREE -> return AccessibilityUtils.performClick(mService, "com.jingdong.app.mall:id/btb", false)
            ServiceCommand.HOME_TAB -> return AccessibilityUtils.performClickByText(mService, "android.widget.FrameLayout", "首页", false)
            ServiceCommand.CLOSE_AD -> {
                ExecUtils.handleExecCommand("input tap 500 75")
                sleep(2000L)
                MainApplication.startMainJD(false)
                return true
            }
            ServiceCommand.GO_BACK -> {
                workBook?.writeToSheetAppendWithTime("点击 回退")
                return AccessibilityUtils.performGlobalActionBack(mService)
            }
        }
        return super.executeInner(command)
    }
}


