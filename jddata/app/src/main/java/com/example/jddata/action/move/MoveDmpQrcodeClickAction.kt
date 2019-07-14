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
                .append(Command().commandCode(ServiceCommand.JSHOP_DMP_TAB_PRODUCT)
                        .addScene(AccService.JSHOP).delay(10000))
                .append(Command().commandCode(ServiceCommand.JSHOP_DMP_CLICK).delay(5000))
    }

    override fun initLogFile() {
        isMoveAction = true
        logFile = BaseLogFile("动作_dmp扫二维码_点击商品")
        var day9No = getState(GlobalInfo.MOVE_NO) as Int
        addMoveExtra("动作： " + day9No)
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
            ServiceCommand.JSHOP_DMP_TAB_PRODUCT -> {
                val tabNodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jshop:id/tvName")
                if (AccessibilityUtils.isNodesAvalibale(tabNodes)) {
                    one@for (tabNode in tabNodes) {
                        if (tabNode.text != null && tabNode.text.equals("商品")) {
                            val parent = AccessibilityUtils.findParentClickable(tabNode)
                            if (parent != null) {
                                val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                return result
                            }
                        }
                    }
                }
            }
            ServiceCommand.JSHOP_DMP_CLICK -> {
                val lists = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jshop:id/product_list_recycler_view")
                if (AccessibilityUtils.isNodesAvalibale(lists)) {
                    var index = 0
                    do {
                        val titleNodes = lists[0].findAccessibilityNodeInfosByViewId("com.jd.lib.jshop:id/product_item_name")
                        if (AccessibilityUtils.isNodesAvalibale(titleNodes)) {
                            val parent = AccessibilityUtils.findParentClickable(titleNodes[0])
                            if (parent != null) {
                                val title = AccessibilityUtils.getFirstText(titleNodes)
                                var price = AccessibilityUtils.getFirstText(lists[0].findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/product_item_jdPrice"))
                                if (title != null) {
                                    val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                    if (result) {
                                        addMoveExtra("点击商品：${title.replace("1 ", "")}，价格：${price}")
                                        return result
                                    }
                                }
                            }
                        }
                        index++
                        sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
                    } while (ExecUtils.canscroll(lists[0], index))
                }
            }
        }
        return super.executeInner(command)
    }


}
