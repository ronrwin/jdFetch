package com.example.jddata.action

import com.example.jddata.MainApplication
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.ExecUtils

open class BaseAction(actionType: String, map: HashMap<String, String>?) : Action(actionType, map) {
    var itemCount = 0

    constructor(actionType: String): this(actionType, null)

    init {
        // 解决广告弹出阻碍步骤
        appendCommand(Command(ServiceCommand.AGREE).addScene(AccService.PRIVACY).canSkip(true))
                .append(Command(ServiceCommand.HOME_TAB).addScene(AccService.JD_HOME))
                .append(PureCommand(ServiceCommand.CLOSE_AD).delay(6000L))
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


