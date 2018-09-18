package com.example.jddata.action

import com.example.jddata.Entity.ActionType
import com.example.jddata.MainApplication
import com.example.jddata.excel.BaseWorkBook
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand

class MoveScanAction : BaseAction(ActionType.MOVE_SCAN_PRODUCT) {
    init {
        MainApplication.copyPic("jd_detail.png")

        appendCommand(PureCommand(ServiceCommand.CAPTURE_SCAN))
                .append(Command(ServiceCommand.SCAN_CLBUM).delay(3000L)
                        .addScene(AccService.CAPTURE_SCAN))
                .append(Command(ServiceCommand.SCAN_PIC).delay(3000L)
                        .addScene(AccService.PHOTO_ALBUM))
    }

    override fun initWorkbook() {
        workBook = BaseWorkBook("动作_扫描特定二维码")
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
        }
        return super.executeInner(command)
    }
}