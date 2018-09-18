package com.example.jddata.action

import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.Entity.ActionType
import com.example.jddata.GlobalInfo
import com.example.jddata.MainApplication
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.SharedPreferenceHelper
import android.util.DisplayMetrics



open class BaseAction(actionType: String, map: HashMap<String, String>?) : Action(actionType, map) {

    constructor(actionType: String): this(actionType, null)

    init {
        var needCloseAd = false
        if (GlobalInfo.sOneKeyRun) {
            if (mActionType.equals(ActionType.FETCH_JD_KILL) || mActionType.equals(ActionType.FETCH_BRAND_KILL)) {
                needCloseAd = true
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
//                val x = 500 * (GlobalInfo.width/540f)
//                val y = 75 * (GlobalInfo.height/960f)
//                ExecUtils.handleExecCommand("input tap ${x} ${y}")
                ExecUtils.tapCommand(500, 75)
                sleep(2000L)
                MainApplication.startMainJD(false)
                return true
            }
            ServiceCommand.GO_BACK -> {
                workBook?.writeToSheetAppendWithTime("点击 回退")
                return AccessibilityUtils.performGlobalActionBack(mService)
            }
            ServiceCommand.CAPTURE_SCAN -> {
                return AccessibilityUtils.performClick(mService, "com.jingdong.app.mall:id/r9", false)
            }
            ServiceCommand.SCAN_CLBUM -> {
                return AccessibilityUtils.performClick(mService, "com.jd.lib.scan:id/btn_scan_album", false)
            }
            ServiceCommand.SCAN_PIC -> {
                val pics = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.unification:id/lib_ec_photo_album_image")
                if (AccessibilityUtils.isNodesAvalibale(pics)) {
                    val parent = AccessibilityUtils.findParentClickable(pics[0])
                    return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
                return false;
            }
            ServiceCommand.PRODUCT_CONFIRM -> {
                val result = AccessibilityUtils.performClick(mService, "com.jd.lib.productdetail:id/detail_style_add_2_car", false)
                return result
            }
        }
        return super.executeInner(command)
    }
}


