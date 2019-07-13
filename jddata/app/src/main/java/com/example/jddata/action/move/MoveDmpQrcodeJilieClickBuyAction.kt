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
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.JdUtils

open class MoveDmpQrcodeJilieClickBuyAction(env: Env) : BaseAction(env, ActionType.MOVE_DMP_QRCODE_JILIE_CLICK_BUY) {

    init {
        appendCommand(Command().commandCode(ServiceCommand.QR_CODE)
                .setState(GlobalInfo.QRCODE_PIC, GlobalInfo.JILIE_2))
                .append(Command().commandCode(ServiceCommand.SCAN_ALBUM)
                        .addScene(AccService.CAPTURE_SCAN).delay(2000))
                .append(Command().commandCode(ServiceCommand.SCAN_PIC)
                        .addScene(AccService.PHOTO_ALBUM))
                .append(Command().commandCode(ServiceCommand.DMP_DONE).delay(4000))
                .append(Command().commandCode(ServiceCommand.CLICK_RECT).delay(2000))
                .append(Command().commandCode(ServiceCommand.PRODUCT_DONE)
                        .addScene(AccService.PRODUCT_DETAIL).delay(2000))
                .append(Command().commandCode(ServiceCommand.TEMPLATE_ADD_TO_CART).delay(3000))
                .append(Command().commandCode(ServiceCommand.PRODUCT_CONFIRM)
                        .delay(3000).canSkip(true))

    }

    override fun initLogFile() {
        isMoveAction = true
        logFile = BaseLogFile("动作_dmp_jilie")
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
            ServiceCommand.DMP_DONE -> {
                addMoveExtra("进入页面：吉利夏日锋芒")
                ExecUtils.fingerScroll()
                return true
            }
            ServiceCommand.CLICK_RECT -> {
                ExecUtils.tapCommand(250, 500)
                addMoveExtra("点击商品")
                return true
            }

        }
        return super.executeInner(command)
    }


}