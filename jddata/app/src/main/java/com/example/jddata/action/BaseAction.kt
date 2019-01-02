package com.example.jddata.action

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.GlobalInfo
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.SharedPreferenceHelper


abstract class BaseAction(actionType: String, map: HashMap<String, String>?) : Action(actionType, map) {

    constructor(actionType: String): this(actionType, null)

    val COLLECT_SUCCESS = 1
    val COLLECT_END = 0
    val COLLECT_FAIL = -1

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
//                MainApplication.startMainJD(false)
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
                        addMoveExtra("扫描二维码")
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
            ServiceCommand.GET_SKU -> {
                var result = getSkuMethod1()
                if (!result) {
                    result = getSkuMethod2()
                }
                return result
            }
        }
        return super.executeInner(command)
    }

    open fun fetchSkuid(skuid: String):Boolean {
        return true
    }

    /**
     * 从立即购买的弹出框来找
     */
    private fun getSkuMethod1(): Boolean {
        val result = AccessibilityUtils.performClickByText(mService, "android.widget.TextView", "立即购买", false)
        if (result) {
            Thread.sleep(2000)
            val skuIds = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.productdetail:id/detail_style_skuid")
            if (AccessibilityUtils.isNodesAvalibale(skuIds)) {
                val text = skuIds[0].text.toString().replace("商品编号: ", "")
                val result1 = AccessibilityUtils.performGlobalActionBack(mService)
                if (result1) {
                    Thread.sleep(2000)
                    return fetchSkuid(text)
                }
            }
        }
        return false
    }

    /**
     * 去"规格参数"去找
     */
    private fun getSkuMethod2(): Boolean {
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
                    var currentCircle = 0
                    do {
                        Thread.sleep(2000)
                        val gridNodes = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.widget.GridView")
                        if (!AccessibilityUtils.isNodesAvalibale(gridNodes)) {
                            return false
                        }

                        val arrays = AccessibilityUtils.getAllContentDesc(mService!!.rootInActiveWindow)
                        for (i in 0..arrays.size - 1) {
                            if (arrays[i].equals("商品编号")) {
                                val result = AccessibilityUtils.performGlobalActionBack(mService)
                                if (result) {
                                    Thread.sleep(1000)
                                    return fetchSkuid(arrays[i + 1])
                                }
                            }
                        }
                        currentCircle++
                    } while (currentCircle < 3)
                }
            }
        }
        return false
    }

    fun handleClickboardText(text: String): Boolean {
        if (text.startsWith("http")) {
            val splits = text.split("?")
            val lastIndex = splits[0].lastIndexOf("/")
            val url = text.substring(lastIndex + 1, splits[0].length)
            logFile?.writeToFileAppendWithTime("商品链接：${url.split(".")[0]}")
            itemCount++
        }
        return true
    }
}


