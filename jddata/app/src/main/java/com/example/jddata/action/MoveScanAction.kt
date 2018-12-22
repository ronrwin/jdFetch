package com.example.jddata.action

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.Entity.ActionType
import com.example.jddata.MainApplication
import com.example.jddata.excel.BaseLogFile
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils

class MoveScanAction : BaseAction(ActionType.MOVE_SCAN_PRODUCT) {
    init {
        MainApplication.copyPic("jd_detail.png")

        appendCommand(PureCommand(ServiceCommand.CAPTURE_SCAN))
                .append(Command(ServiceCommand.SCAN_CLBUM).delay(3000L)
                        .addScene(AccService.CAPTURE_SCAN))
                .append(Command(ServiceCommand.SCAN_PIC).delay(3000L)
                        .addScene(AccService.PHOTO_ALBUM))

        appendCommand(Command(ServiceCommand.PRODUCT_DETAIL).delay(8000L)
                .addScene(AccService.PRODUCT_DETAIL))
    }

    override fun initWorkbook() {
        logFile = BaseLogFile("动作_扫描特定二维码")
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.PRODUCT_DETAIL -> {
                val result = getProductDetail()
                return result
            }
        }
        return super.executeInner(command)
    }

    fun getProductDetail(): Boolean {
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
                        logFile?.writeToFileAppendWithTime("商品", title, price)
                        addExtra("商品：${title}，${price}")
                    }

                    return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
            }
        }
        return false
    }
}