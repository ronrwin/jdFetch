package com.example.jddata.action.move

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
import com.example.jddata.util.JdUtils

open class MoveDmpQrcodeClickAction(env: Env) : BaseAction(env, ActionType.MOVE_DMP_QRCODE_CLICK) {

    init {
        appendCommand(Command().commandCode(ServiceCommand.QR_CODE).setState(GlobalInfo.QRCODE_PIC, GlobalInfo.HC01))
                .append(Command().commandCode(ServiceCommand.SCAN_ALBUM)
                        .addScene(AccService.CAPTURE_SCAN).delay(2000))
                .append(Command().commandCode(ServiceCommand.SCAN_PIC).addScene(AccService.PHOTO_ALBUM))
                .append(Command().commandCode(ServiceCommand.CLICK).delay(10000))
                .append(Command().commandCode(ServiceCommand.FETCH_PRODUCT)
                        .addScene(AccService.PRODUCT_DETAIL).delay(3000))
    }

    override fun initLogFile() {
        isMoveAction = true
        logFile = BaseLogFile("动作_dmp扫二维码_点击商品")
        val tmp = getState(GlobalInfo.MOVE_NO)
        if (tmp != null) {
            val day9No = getState(GlobalInfo.MOVE_NO) as Int
            addMoveExtra("动作：${day9No}")
        }
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
            ServiceCommand.CLICK -> {
                ExecUtils.fingerScroll()
                sleep(1000)
                ExecUtils.tapCommand(270, 500)
            }
            ServiceCommand.FETCH_PRODUCT -> {
                val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.productdetail:id/detail_desc_description")
                if (AccessibilityUtils.isNodesAvalibale(nodes)) {
                    addMoveExtra("点击商品：" + nodes[0].text.toString())
                    return true
                }
            }
        }
        return super.executeInner(command)
    }


}
