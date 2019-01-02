package com.example.jddata.action

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.MessageDef
import com.example.jddata.MainApplication
import com.example.jddata.excel.BaseLogFile
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils

class MoveScanShopAction : BaseAction(ActionType.MOVE_SCAN_PRODUCT_BUY) {
    init {
        MainApplication.copyPic("jd_detail.png")

        appendCommand(PureCommand(ServiceCommand.CAPTURE_SCAN))
                .append(Command(ServiceCommand.SCAN_CLBUM).delay(3000L)
                        .addScene(AccService.CAPTURE_SCAN))
                .append(Command(ServiceCommand.SCAN_PIC).delay(3000L)
                        .addScene(AccService.PHOTO_ALBUM))

        appendCommand(Command(ServiceCommand.PRODUCT_BUY).delay(8000L)
                .addScene(AccService.PRODUCT_DETAIL))
    }

    override fun initWorkbook() {
        logFile = BaseLogFile("动作_扫描特定二维码并加购")
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.PRODUCT_BUY -> {
                val result = getBuyProduct()
                if (result) {
                    appendCommand(Command(ServiceCommand.PRODUCT_CONFIRM).addScene(AccService.BOTTOM_DIALOG).canSkip(true))
                    // 如果不进去确定界面，3秒后视为成功
                    BusHandler.instance.sendEmptyMessageDelayed(MessageDef.SUCCESS, 3000L)
                }
                return result
            }
        }
        return super.executeInner(command)
    }

    fun getBuyProduct(): Boolean {
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, "加入购物车")
        if (AccessibilityUtils.isNodesAvalibale(nodes)) {
            for (node in nodes) {
                if (node.isClickable) {
                    val titleNodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.productdetail:id/detail_desc_description")

                    var priceNodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.productdetail:id/detail_price")
                    if (!AccessibilityUtils.isNodesAvalibale(priceNodes)) {
                        priceNodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.productdetail:id/lib_pd_jx_plusprice")
                    }
                    if (!AccessibilityUtils.isNodesAvalibale(priceNodes)) {
                        priceNodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.productdetail:id/pd_top_miaosha_price")
                    }
                    if (AccessibilityUtils.isNodesAvalibale(titleNodes) && AccessibilityUtils.isNodesAvalibale(priceNodes)) {
                        val title = AccessibilityUtils.getFirstText(titleNodes)
                        val price = AccessibilityUtils.getFirstText(priceNodes)
                        logFile?.writeToFileAppendWithTime("加购商品", title, price)
                        addMoveExtra("加购商品：${title}，${price}")
                    }

                    return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
            }
        }
        return false
    }
}