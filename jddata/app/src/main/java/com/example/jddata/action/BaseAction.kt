package com.example.jddata.action

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.GlobalInfo
import com.example.jddata.MainApplication
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.SharedPreferenceHelper
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE


abstract class BaseAction(actionType: String, map: HashMap<String, String>?) : Action(actionType, map) {

    constructor(actionType: String): this(actionType, null)

    init {
        val today = ExecUtils.today()
        val needCloseAd = !today.equals(SharedPreferenceHelper.getInstance().getValue(GlobalInfo.TODAY_DO_ACTION))

        appendCommand(Command(ServiceCommand.AGREE).addScene(AccService.PRIVACY).canSkip(true))
                .append(Command(ServiceCommand.HOME_TAB).addScene(AccService.JD_HOME))
        if (needCloseAd) {
            appendCommand(PureCommand(ServiceCommand.CLOSE_AD).delay(12000L))
            SharedPreferenceHelper.getInstance().saveValue(GlobalInfo.TODAY_DO_ACTION, today)
        } else {
            appendCommand(PureCommand(ServiceCommand.CLOSE_AD).delay(4000L))
        }
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
            ServiceCommand.AGREE -> {
                return AccessibilityUtils.performClick(mService, "com.jingdong.app.mall:id/bw9", false) || AccessibilityUtils.performClick(mService, "com.jingdong.app.mall:id/btb", false)
            }
            ServiceCommand.HOME_TAB -> return AccessibilityUtils.performClickByText(mService, "android.widget.FrameLayout", "首页", false)
            ServiceCommand.CLOSE_AD -> {
                ExecUtils.tapCommand(500, 75)
                sleep(2000L)
                MainApplication.startMainJD(false)
                return true
            }
            ServiceCommand.GO_BACK -> {
                logFile?.writeToFileAppendWithTime("点击 回退")
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
                    val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    if (result) {
                        addExtra("扫描二维码")
                    }
                    return result
                }
                return false
            }
            ServiceCommand.PRODUCT_CONFIRM -> {
                return AccessibilityUtils.performClick(mService, "com.jd.lib.productdetail:id/detail_style_add_2_car", false)
            }
            ServiceCommand.CLICK_SHARE -> {
                return AccessibilityUtils.performClick(mService, "com.jd.lib.productdetail:id/pd_nav_share", false)
            }
            ServiceCommand.COPY_LINK -> {
                val copyNodes = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, "复制链接")
                if (AccessibilityUtils.isNodesAvalibale(copyNodes)) {
                    val parent = AccessibilityUtils.findParentClickable(copyNodes[0])
                    if (parent != null) {
                        return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                }
                return false
            }

            ServiceCommand.GET_CLIPBOARD -> {
                val text = ExecUtils.getClipBoardText()
                return handleClickboardText(text)
            }
            ServiceCommand.GET_SKU -> {
                ExecUtils.fingerScroll()

                val detailNodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.productdetail:id/pd_tab2")
                if (AccessibilityUtils.isNodesAvalibale(detailNodes)) {
                    val clickDetailTab = detailNodes[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    if (clickDetailTab) {
                        Thread.sleep(2000)
                        val paramNodes = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, "规格参数")
                        if (!AccessibilityUtils.isNodesAvalibale(paramNodes)) {
                            return false
                        }
                        val paramParent = AccessibilityUtils.findParentClickable(paramNodes[0])
                        if (paramParent == null) {
                            return false
                        }
                        val paremResult = paramParent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        if (paremResult) {
                            Thread.sleep(3000)
                            val gridNodes = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.widget.GridView")
                            if (!AccessibilityUtils.isNodesAvalibale(gridNodes)) {
                                return false
                            }

                            // fixme : 这里抓不到web控件内容。要调整
                            val childrens = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.view.View")
                            if (!AccessibilityUtils.isNodesAvalibale(childrens)) {
                                return false
                            }
                            val childCount = childrens.size
                            if (childCount > 0) {
                                for (i in 0..childCount - 1) {
                                    val child = childrens[i]
                                    if (child.contentDescription != null && child.contentDescription.toString().equals("商品编号")) {
                                        if (i < childCount - 1) {
                                            val skuNode = childrens[i + 1]
                                            if (skuNode.contentDescription != null) {
                                                return fetchSkuid(skuNode.contentDescription.toString())
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                return false
            }
            ServiceCommand.ADD_TO_CAR -> {
                val result = AccessibilityUtils.performClick(mService, "com.jd.lib.productdetail:id/add_2_car", false)
                if (result) {
                    val skuIds = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.productdetail:id/detail_style_skuid")
                    if (AccessibilityUtils.isNodesAvalibale(skuIds)) {
                        val text = skuIds[0].text.toString().replace("商品编号: ", "")
                        return fetchSkuid(text)
                    }
                }
            }
        }
        return super.executeInner(command)
    }

    open fun handleClickboardText(text: String):Boolean {
        return true
    }

    open fun fetchSkuid(skuid: String):Boolean {
        return true
    }
}


