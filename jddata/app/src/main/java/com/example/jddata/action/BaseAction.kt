package com.example.jddata.action

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.GlobalInfo
import com.example.jddata.MainApplication
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.shelldroid.EnvManager
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.SharedPreferenceHelper
import java.util.*


abstract class BaseAction(actionType: String, map: HashMap<String, String>?) : Action(actionType, map) {

    constructor(actionType: String): this(actionType, null)

    val COLLECT_SUCCESS = 1
    val COLLECT_END = 0
    val COLLECT_FAIL = -1

    init {
        val today = ExecUtils.today()
        val key = GlobalInfo.TODAY_DO_ACTION + "-${EnvManager.sCurrentEnv.envName}"
        val needCloseAd = !today.equals(SharedPreferenceHelper.getInstance().getValue(key))

        appendCommand(Command().commandCode(ServiceCommand.AGREE).addScene(AccService.PRIVACY).canSkip(true))
                .add((Command().commandCode(ServiceCommand.HOME_TAB).addScene(AccService.JD_HOME)))
        if (needCloseAd) {
            appendCommand(Command().commandCode(ServiceCommand.CLOSE_AD).delay(15000L))
            SharedPreferenceHelper.getInstance().saveValue(key, today)
        } else {
            appendCommand(Command().commandCode(ServiceCommand.CLOSE_AD).delay(3000L))
        }
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
            ServiceCommand.AGREE -> {
                return AccessibilityUtils.performClick(mService, "com.jingdong.app.mall:id/bw9", false) || AccessibilityUtils.performClick(mService, "com.jingdong.app.mall:id/btb", false)
            }
            ServiceCommand.HOME_TAB -> return AccessibilityUtils.performClickByText(mService, "android.widget.FrameLayout", "首页", false)
            ServiceCommand.TYPE_TAB -> return AccessibilityUtils.performClickByText(mService, "android.widget.FrameLayout", "分类", false)
            ServiceCommand.MY_TAB -> return AccessibilityUtils.performClickByText(mService, "android.widget.FrameLayout", "我的", false)
            ServiceCommand.FIND_TAB -> return AccessibilityUtils.performClickByText(mService, "android.widget.FrameLayout", "发现", false)
            ServiceCommand.CART_TAB -> return AccessibilityUtils.performClickByText(mService, "android.widget.FrameLayout", "购物车", false)
            ServiceCommand.CLOSE_AD -> {
                ExecUtils.tapCommand(500, 75)
                sleep(2000L)
                MainApplication.startMainJD(false)
                return true
            }
            ServiceCommand.HOME -> {
                addMoveExtra("回到首页")
                MainApplication.startMainJD(false)
                return true
            }
            ServiceCommand.BACK_JD_HOME -> {
                AccessibilityUtils.performGlobalActionHome(mService)
                return true
            }
            ServiceCommand.SEARCH_SELECT -> {
                if (command.states.containsKey(GlobalInfo.SEARCH_RESULT_SCROLL)) {
                    val lists = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/product_list")
                    if (AccessibilityUtils.isNodesAvalibale(lists)) {
                        lists[0].performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    }
                }

                val titles = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/product_item_name")
                if (AccessibilityUtils.isNodesAvalibale(titles)) {

                    val index = Random().nextInt(titles.size)
                    val node = titles[index]
                    val parent = AccessibilityUtils.findParentClickable(node)
                    if (parent != null) {
                        return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                }
                return false
            }
            ServiceCommand.TEMPLATE_INPUT -> {
                // todo: 从配置中取关键词
//                val text = command.states.get(GlobalInfo.SEARCH_KEY)
                val text = "洗发水"
                if (text is String) {
                    return ExecUtils.commandInput(mService!!, "android.widget.EditText", "com.jd.lib.search:id/search_text", text)
                }
            }
            ServiceCommand.GO_BACK -> {
                logFile?.writeToFileAppend("点击 回退")
                val result = AccessibilityUtils.performGlobalActionBack(mService)
                return result
            }
            ServiceCommand.QR_CODE -> {
                val nodes = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, "扫啊扫")
                if (AccessibilityUtils.isNodesAvalibale(nodes)) {
                    val parent = AccessibilityUtils.findParentClickable(nodes[0])
                    if (parent != null) {
                        val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        if (result) {
                            addMoveExtra("点击 扫啊扫")
                        }
                        return result
                    }
                }
                return false
            }
            ServiceCommand.SCAN_ALBUM -> {
                val result = AccessibilityUtils.performClick(mService, "com.jd.lib.scan:id/btn_scan_album", false)
                if (result) {
                    addMoveExtra("点击相册")
                }
                return result
            }
            ServiceCommand.SCAN_PIC -> {
                val pics = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.unification:id/lib_ec_photo_album_image")
                if (AccessibilityUtils.isNodesAvalibale(pics)) {
                    val parent = AccessibilityUtils.findParentClickable(pics[0])
                    val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    if (result) {
                        addMoveExtra("选择扫描二维码")
                    }
                    return result
                }
                return false
            }
            ServiceCommand.COLLECT_ITEM -> {
                val resultCode = collectItems()
                when (resultCode) {
                    COLLECT_FAIL -> {
                        return false
                    }
                    COLLECT_END -> {
                        return true
                    }
                    COLLECT_SUCCESS -> {
                        appendCommand(Command().commandCode(ServiceCommand.CLICK_ITEM))
                        return true
                    }
                }
                return true
            }
            ServiceCommand.CLICK_ITEM -> {
                return clickItem()
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
                val result = getSkuMethod2()
                return result
            }
            ServiceCommand.CLICK_PRODUCT_TAB2 -> {
                val result = clickProductTab2()
                if (result) {
                    appendCommand(Command().commandCode(ServiceCommand.CLICK_PRODUCT_INFO))
                } else {
                    appendCommand(Command().commandCode(ServiceCommand.LEAVE_PRODUCT_DETAIL))
                }
                return result
            }
            ServiceCommand.CLICK_PRODUCT_INFO -> {
                val result = clickProductInfo()
                if (result) {
                    appendCommand(Command().commandCode(ServiceCommand.FETCH_SKU).delay(3000))
                } else {
                    appendCommand(Command().commandCode(ServiceCommand.GO_BACK))
                    appendCommand(Command().commandCode(ServiceCommand.LEAVE_PRODUCT_DETAIL))
                }
                return result
            }
            ServiceCommand.FETCH_SKU -> {
                val result = fetchSku()
                appendCommand(Command().commandCode(ServiceCommand.GO_BACK))
                appendCommand(Command().commandCode(ServiceCommand.LEAVE_PRODUCT_DETAIL))
                return result
            }
            ServiceCommand.LEAVE_PRODUCT_DETAIL -> {
                beforeLeaveProductDetai()
                return AccessibilityUtils.performGlobalActionBack(mService)
            }
            ServiceCommand.CLICK_SEARCH -> {
                addMoveExtra("点击搜索栏")
                return ExecUtils.tapCommand(250, 75)
            }
            ServiceCommand.INPUT -> {
                val text = command.states.get(GlobalInfo.SEARCH_KEY)
                if (text is String) {
                    val result = ExecUtils.commandInput(mService!!, "android.widget.EditText", "com.jd.lib.search:id/search_text", text)
                    if (result) {
                        addMoveExtra("输入搜索关键词：${text}")
                    }
                    return result
                }
            }
            ServiceCommand.SEARCH -> {
                logFile?.writeToFileAppend("点击搜索按钮")
                val nodes = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, "搜索")
                if (AccessibilityUtils.isNodesAvalibale(nodes)) {
                    for (node in nodes) {
                        val parent = AccessibilityUtils.findParentClickable(node)
                        if (parent != null) {
                            val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            if (result) {
                                addMoveExtra("点击搜索按钮")
                            }
                            return result
                        }
                    }
                }
                return false
            }
            ServiceCommand.TEMPLATE_ADD_TO_CART -> {
                val nodes = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, "加入购物车")
                if (AccessibilityUtils.isNodesAvalibale(nodes)) {
                    for (node in nodes) {
                        val parent = AccessibilityUtils.findParentClickable(node)
                        if (parent != null) {
                            val result =  parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            if (result) {
                                addMoveExtra("加入购物车")
                            }
                            return result
                        }
                    }
                }
                return false
            }
            ServiceCommand.SETTLE -> {
                val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.cart:id/cart_settle_accounts_but")
                if (AccessibilityUtils.isNodesAvalibale(nodes)) {
                    val result = nodes[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    if (result) {
                        addMoveExtra("结算")
                    }
                    return result
                }
                return false
            }
            ServiceCommand.TEMPLATE_HOME_SELECT -> {
                val lists = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")
                if (AccessibilityUtils.isNodesAvalibale(lists)) {
                    var index = 0
                    do {
                        val titleNodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/btx")
                        if (AccessibilityUtils.isNodesAvalibale(titleNodes)) {
                            val selectedIndex = Random().nextInt(titleNodes.size)
                            val parent = AccessibilityUtils.findParentClickable(titleNodes[selectedIndex])
                            if (parent != null) {
                                val titleName = titleNodes[selectedIndex].text.toString()
                                val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                if (result) {
                                    addMoveExtra("点击商品 ${titleName}")
                                }
                                return result
                            }
                        }
                        index++
                        sleep(200)
                    } while (ExecUtils.canscroll(lists[0], index))
                }
                return false
            }
        }
        return false
    }

    open fun beforeLeaveProductDetai() {}

    fun getSkuCommands(): ArrayList<Command> {
        val list = ArrayList<Command>()
        list.add(Command().commandCode(ServiceCommand.CLICK_PRODUCT_TAB2).addScene(AccService.PRODUCT_DETAIL).delay(2000))
        return list
    }

    open fun collectItems(): Int {
        return COLLECT_END
    }

    open fun clickItem(): Boolean {
        return true
    }

    open fun fetchSkuid(skuid: String):Boolean {
        logFile?.writeToFileAppend("商品sku：${skuid}")
        return true
    }

    /**
     * 从立即购买的弹出框来找
     */
    private fun getSkuMethod1(): Boolean {
        val result = AccessibilityUtils.performClickByText(mService, "android.widget.TextView", "立即购买", false)
        if (result) {
            sleep(1000)
            val skuIds = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.productdetail:id/detail_style_skuid")
            if (AccessibilityUtils.isNodesAvalibale(skuIds)) {
                val text = skuIds[0].text.toString().replace("商品编号: ", "")
                val result1 = AccessibilityUtils.performGlobalActionBack(mService)
                if (result1) {
                    sleep(1000)
                    return fetchSkuid(text)
                }
            }
        }
        return false
    }

    private fun clickProductTab2() : Boolean {
        ExecUtils.fingerScroll()

        val detailNodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.productdetail:id/pd_tab2")
        if (AccessibilityUtils.isNodesAvalibale(detailNodes)) {
            val clickDetailTab = detailNodes[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
            if (clickDetailTab) {
                return true
            }
        }
        return false
    }

    private fun fetchSku(): Boolean {
        var currentCircle = 0
        do {
            val gridNodes = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.widget.GridView")
            if (!AccessibilityUtils.isNodesAvalibale(gridNodes)) {
                return false
            }

            val arrays = AccessibilityUtils.getAllContentDesc(mService!!.rootInActiveWindow)
            for (i in 0 until arrays.size) {
                if (arrays[i].equals("商品编号")) {
                    return fetchSkuid(arrays[i + 1])
                }
            }
            currentCircle++
        } while (currentCircle < 3)
        return false
    }

    private fun clickProductInfo(): Boolean {
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
            return true
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
                sleep(1000)
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
                        sleep(1000)
                        val gridNodes = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.widget.GridView")
                        if (!AccessibilityUtils.isNodesAvalibale(gridNodes)) {
                            return false
                        }

                        val arrays = AccessibilityUtils.getAllContentDesc(mService!!.rootInActiveWindow)
                        for (i in 0 until arrays.size) {
                            if (arrays[i].equals("商品编号")) {
                                val result = AccessibilityUtils.performGlobalActionBack(mService)
                                if (result) {
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

    fun findHomeTextClick(text: String): Boolean {
        val nodes = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")
                ?: return false
        for (node in nodes) {
            var index = 0
            do {
                val leader = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, text)
                if (AccessibilityUtils.isNodesAvalibale(leader)) {
                    val parent = AccessibilityUtils.findParentClickable(leader!![0])
                    if (parent != null) {
                        return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                }
                index++
                Thread.sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while (node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) && index < 5)
        }
        return false
    }
}


