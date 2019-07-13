package com.example.jddata.action.move

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

open class MoveBolangQrcodeAction(env: Env) : BaseAction(env, ActionType.MOVE_BOLANG_QRCODE) {

    init {
        appendCommand(Command().commandCode(ServiceCommand.QR_CODE)
                .setState(GlobalInfo.QRCODE_PIC, GlobalInfo.BOLANG_2))
                .append(Command().commandCode(ServiceCommand.SCAN_ALBUM)
                        .addScene(AccService.CAPTURE_SCAN).delay(2000))
                .append(Command().commandCode(ServiceCommand.SCAN_PIC)
                        .addScene(AccService.PHOTO_ALBUM))
                .append((Command().commandCode(ServiceCommand.PRODUCT_DONE)
                        .addScene(AccService.PRODUCT_DETAIL).delay(7000)))
    }

    override fun initLogFile() {
        isMoveAction = true
        logFile = BaseLogFile("动作_jilie_扫二维码")
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
            ServiceCommand.PRODUCT_DONE -> {
                val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.productdetail:id/a7l")
                if (AccessibilityUtils.isNodesAvalibale(nodes)) {
                    val title = AccessibilityUtils.getFirstText(nodes)
                    addMoveExtra("进入商品页：${title}")
                }
                return true
            }
        }
        return super.executeInner(command)
    }


}
