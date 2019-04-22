package com.example.jddata.action

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.Route
import com.example.jddata.GlobalInfo
import com.example.jddata.MainApplication
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.shelldroid.Env
import com.example.jddata.util.*
import java.util.*


abstract class BaseAction(env: Env, actionType: String, map: HashMap<String, String>?) : Action(env, actionType, map) {

    constructor(env: Env, actionType: String): this(env, actionType, null)

    val COLLECT_SUCCESS = 1
    val COLLECT_END = 0
    val COLLECT_FAIL = -1


    init {
        val today = ExecUtils.today()
        val key = GlobalInfo.TODAY_DO_ACTION + "-${env.envName}"
        val needCloseAd = !today.equals(SharedPreferenceHelper.getInstance().getValue(key))

        appendCommand(Command().commandCode(ServiceCommand.AGREE).addScene(AccService.PRIVACY).canSkip(true))
                .add((Command().commandCode(ServiceCommand.HOME_TAB).addScene(AccService.JD_HOME)))
        if (needCloseAd) {
            appendCommand(Command().commandCode(ServiceCommand.CLOSE_AD).delay(15000L))
            SharedPreferenceHelper.getInstance().saveValue(key, today)
        } else {
            appendCommand(Command().commandCode(ServiceCommand.CLOSE_AD).delay(10000L))
        }
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
            ServiceCommand.AGREE -> {
                return AccessibilityUtils.performClick(mService, "com.jingdong.app.mall:id/bw9", false) || AccessibilityUtils.performClick(mService, "com.jingdong.app.mall:id/btb", false)
            }
            ServiceCommand.DONE -> {
                return true
            }
            ServiceCommand.HOME_TAB -> {
                BusHandler.instance.startCountTimeout()
                val result = AccessibilityUtils.performClickByText(mService, "android.widget.FrameLayout", "首页", false)
                if (result) {
                    addMoveExtra("点击 首页 标签")
                }
                return result
            }
            ServiceCommand.TYPE_TAB -> {
                val result = AccessibilityUtils.performClickByText(mService, "android.widget.FrameLayout", "分类", false)
                if (result) {
                    addMoveExtra("点击 分类 标签")
                }
                return result
            }
            ServiceCommand.MY_TAB -> {
                val result =  AccessibilityUtils.performClickByText(mService, "android.widget.FrameLayout", "我的", false)
                if (result) {
                    addMoveExtra("点击 我的 标签")
                }
                return result
            }
            ServiceCommand.FIND_TAB -> {
                val result = AccessibilityUtils.performClickByText(mService, "android.widget.FrameLayout", "发现", false)
                if (result) {
                    addMoveExtra("点击 发现 标签")
                }
                return result
            }
            ServiceCommand.CART_TAB -> {
                val result = AccessibilityUtils.performClickByText(mService, "android.widget.FrameLayout", "购物车", false)
                if (result) {
                    addMoveExtra("点击 购物车 标签")
                }
                return result
            }
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
            ServiceCommand.DESKTOP -> {
                addMoveExtra("结束")
                AccessibilityUtils.performGlobalActionHome(mService)
                return true
            }
            ServiceCommand.SEARCH_SELECT -> {
                if (command.commandStates.containsKey(GlobalInfo.SEARCH_RESULT_SCROLL)) {
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
                        val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        if (result) {
                            val titleName = node.text.toString()
                            addMoveExtra("点击商品 ${titleName}")
                        }
                        return result
                    }
                }
                return false
            }
            ServiceCommand.TEMPLATE_INPUT -> {
                BusHandler.instance.startCountTimeout()
                // todo: 从配置中取关键词
//                val text = command.states.get(GlobalInfo.SEARCH_KEY)
                var index = 0
                val no = getState(GlobalInfo.TEMPLATE_SEARCH_INDEX)
                if (no != null) {
                    index = no.toString().toInt()
                }

                val temp = getState(GlobalInfo.ROUTE)
                var text = "洗发水"
                if (temp != null) {
                    val route = temp as Route
                    if (index < route.keywords.size) {
                        text = route.keywords[index]
                    } else {
                        LogUtil.writeResultLog("<< Route: ${route.id}, index: ${index}, not right")
                    }
                }

                if (text is String) {
                    val result = ExecUtils.commandInput(mService!!, "android.widget.EditText", "com.jd.lib.search:id/search_text", text)
                    if (result) {
                        addMoveExtra("搜索关键词：${text}")
                        setState(GlobalInfo.TEMPLATE_SEARCH_INDEX, index+1)
                    }
                    return result
                }
                return false
            }
            ServiceCommand.GO_BACK -> {
                addMoveExtra("点击回退")
                val result = AccessibilityUtils.performGlobalActionBack(mService)
                return result
            }
            ServiceCommand.QR_CODE -> {
                BusHandler.instance.startCountTimeout()
                val target = command.commandStates[GlobalInfo.QRCODE_PIC]
                if (target != null) {
                    if ("random".equals(target)) {
                        JdUtils.copyPicRandom()
                    } else {
                        JdUtils.copyPic(target)
                    }
                }

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
                BusHandler.instance.startCountTimeout()
                val resultCode = collectItems()
                when (resultCode) {
                    COLLECT_FAIL -> {
                        LogUtil.logCache("debug", "COLLECT_FAIL")
                        return false
                    }
                    COLLECT_END -> {
                        LogUtil.logCache("debug", "COLLECT_END")
                        return true
                    }
                    COLLECT_SUCCESS -> {
                        LogUtil.logCache("debug", "COLLECT_SUCCESS")
                        appendCommand(Command().commandCode(ServiceCommand.CLICK_ITEM))
                        return true
                    }
                }
                return true
            }
            ServiceCommand.CLICK_ITEM -> {
                BusHandler.instance.startCountTimeout()
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
                    appendCommand(Command().commandCode(ServiceCommand.LEAVE_PRODUCT_DETAIL).delay(3000))
                }
                return result
            }
            ServiceCommand.CLICK_PRODUCT_INFO -> {
                val result = clickProductInfo()
                if (result) {
                    appendCommand(Command().commandCode(ServiceCommand.FETCH_SKU).delay(4000))
                } else {
                    appendCommand(Command().commandCode(ServiceCommand.GO_BACK))
                    appendCommand(Command().commandCode(ServiceCommand.LEAVE_PRODUCT_DETAIL).delay(3000))
                }
                return result
            }
            ServiceCommand.FETCH_SKU -> {
                val result = fetchSku()
                appendCommand(Command().commandCode(ServiceCommand.GO_BACK))
                appendCommand(Command().commandCode(ServiceCommand.LEAVE_PRODUCT_DETAIL).delay(3000))
                return result
            }
            ServiceCommand.LEAVE_PRODUCT_DETAIL -> {
                beforeLeaveProductDetail()
                return AccessibilityUtils.performGlobalActionBack(mService)
            }
            ServiceCommand.CLICK_SEARCH -> {
                addMoveExtra("点击搜索栏")
                return ExecUtils.tapCommand(250, 75)
            }
            ServiceCommand.SHOP_CAR -> {
                val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.productdetail:id/goto_shopcar")
                if (AccessibilityUtils.isNodesAvalibale(nodes)) {
                    val parent = AccessibilityUtils.findParentClickable(nodes[0])
                    if (parent != null) {
                        val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        if (result) {
                            addMoveExtra("点击 购物车")
                        }
                        return result
                    }
                }
                return false
            }
            ServiceCommand.INPUT -> {
                val text = command.commandStates.get(GlobalInfo.SEARCH_KEY)
                if (text is String) {
                    val result = ExecUtils.commandInput(mService!!, "android.widget.EditText", "com.jd.lib.search:id/search_text", text)
                    if (result) {
                        addMoveExtra("搜索关键词：${text}")
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
//                BusHandler.instance.startCountTimeout()
                val lists = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")
                if (AccessibilityUtils.isNodesAvalibale(lists)) {
                    var index = 0
                    if (command.commandStates.containsKey(GlobalInfo.SEARCH_RESULT_SCROLL)) {
                        ExecUtils.canscroll(lists[0], index)
                    }

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
                        sleep(500)
                    } while (ExecUtils.canscroll(lists[0], index))
                }
                return false
            }
            ServiceCommand.TEMPLATE_CART_SELECT -> {
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
                        sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
                    } while (ExecUtils.canscroll(lists[0], index))
                }
                return false
            }
            ServiceCommand.TEMPLATE_TYPE_SELECT -> {
                val titleNodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/title1")
                if (AccessibilityUtils.isNodesAvalibale(titleNodes)) {
                    val node = titleNodes[Random().nextInt(titleNodes.size)]
                    val parent = AccessibilityUtils.findParentClickable(node)
                    if (parent != null) {
                        val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        if (result) {
                            val title = node.text.toString()
                            addMoveExtra("点击商品 ${title}")
                        }
                        return result
                    }
                }
                return false
            }
            ServiceCommand.TEMPLATE_JDKILL_SELECT -> {
                val titleNodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/limit_buy_product_item_name")
                if (AccessibilityUtils.isNodesAvalibale(titleNodes)) {
                    val node = titleNodes[Random().nextInt(titleNodes.size)]
                    val parent = AccessibilityUtils.findParentClickable(node)
                    if (parent != null) {
                        val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        if (result) {
                            val title = node.text.toString()
                            addMoveExtra("点击商品 ${title}")
                        }
                        return result
                    }
                }
                return false
            }
            ServiceCommand.HOME_DMP -> {
                ExecUtils.tapCommand(300, 200)
                return true
            }
            ServiceCommand.DMP_TITLE -> {
                var nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/ff")
                if (!AccessibilityUtils.isNodesAvalibale(nodes)) {
                    nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/a6s")
                }
                if (!AccessibilityUtils.isNodesAvalibale(nodes)) {
                    nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jshop:id/jshop_shopname")
                }

                if (AccessibilityUtils.isNodesAvalibale(nodes)) {
                    val title = AccessibilityUtils.getFirstText(nodes)
                    addMoveExtra("dmp广告标题：${title}")
                    return true
                }

                return false
            }
            ServiceCommand.TEMPLATE_JDKILL -> {
                val lists = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")
                if (AccessibilityUtils.isNodesAvalibale(lists)) {
                    for (list in lists) {
                        do {

                        } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD))
                    }

                    for (list in lists) {
                        var index = 0
                        do {
                            val result = AccessibilityUtils.performClick(mService, "com.jingdong.app.mall:id/bmv", false)
                            if (result) {
                                addMoveExtra("点击 京东秒杀")
                                return result
                            }

                            index++
                            sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
                        } while (ExecUtils.canscroll(list, index))
                    }
                }

                return false
            }
            ServiceCommand.TEMPLATE_WORTHBUY_SELECT -> {
                val titleNodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/product_name")
                if (AccessibilityUtils.isNodesAvalibale(titleNodes)) {
                    val node = titleNodes[Random().nextInt(titleNodes.size)]
                    val parent = AccessibilityUtils.findParentClickable(node)
                    if (parent != null) {
                        val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        if (result) {
                            val titleName = node.text.toString()
                            addMoveExtra("点击商品 $titleName")
                        }
                        return result
                    }
                }
                return false
            }
            ServiceCommand.TEMPLATE_MY_SELECT -> {
                var index = 0
                val lists = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")
                if (AccessibilityUtils.isNodesAvalibale(lists)) {
                    do {
                        val titleNodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/btx")
                        if (AccessibilityUtils.isNodesAvalibale(titleNodes)) {
                            val node = titleNodes[Random().nextInt(titleNodes.size)]
                            val parent = AccessibilityUtils.findParentClickable(node)
                            if (parent != null) {
                                val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                if (result) {
                                    val titleName = node.text.toString()
                                    addMoveExtra("点击商品 $titleName")
                                }
                                return result
                            }
                        }
                        index++
                        sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
                    } while (ExecUtils.canscroll(lists[0], index))
                }
                return false
            }
            ServiceCommand.MIAOSHA_TAB -> {
                if (command.commandStates.containsKey(GlobalInfo.MIAOSHA_TAB)) {
                    val tabName = command.commandStates[GlobalInfo.MIAOSHA_TAB]
                    val nodes = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, tabName)
                    if (AccessibilityUtils.isNodesAvalibale(nodes)) {
                        val result = nodes[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        if (result) {
                            addMoveExtra("点击 $tabName")
                        }
                        return result
                    }
                }
                return false
            }
            ServiceCommand.TEMPLATE_BRAND_SELECT -> {
                val lists = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")
                if (AccessibilityUtils.isNodesAvalibale(lists)) {
                    var index = 0
                    do {
                        val titleNodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/miaosha_brand_title")
                        if (AccessibilityUtils.isNodesAvalibale(titleNodes)) {
                            val node = titleNodes[Random().nextInt(titleNodes.size)]
                            val parent = AccessibilityUtils.findParentClickable(node)
                            if (parent != null) {
                                val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                if (result) {
                                    val titleName = node.text.toString()
                                    addMoveExtra("点击商品 $titleName")
                                }
                                return result
                            }
                        }
                        index++
                        sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
                    } while (ExecUtils.canscroll(lists[0], index))
                }
                return false
            }
            ServiceCommand.SEARCH_IN_RESULT -> {
                val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/layout_container")
                if (AccessibilityUtils.isNodesAvalibale(nodes)) {
                    val parent = AccessibilityUtils.findParentClickable(nodes[0])
                    if (parent != null) {
                        val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        if (result) {
                            addMoveExtra("点击搜索框")
                        }
                        return result
                    }
                }
                return false
            }
            ServiceCommand.CART_CLICK -> {
                val lists = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")
                if (AccessibilityUtils.isNodesAvalibale(lists)) {
                    var index = 0
                    do {
                        val items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/c2g")
                        if (AccessibilityUtils.isNodesAvalibale(items)) {
                            val item = items[0]
                            val title = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/btx"))
                            val price = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/bty"))
                            if (title != null && price != null) {
                                addMoveExtra("点击商品：${title}, 价格：${price}")
                                return true
                            }
                        }
                        index++
                        sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
                    } while (lists[0].performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) && index < 3)
                }
                return false
            }
            ServiceCommand.HOME_CARD_ITEM -> {
                val lists = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")
                if (AccessibilityUtils.isNodesAvalibale(lists)) {
                    for (list in lists) {
                        do {
                            // 回到最顶部
                        } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD))
                    }
                }

                val itemname = command.commandStates[GlobalInfo.HOME_CARD_NAME] as String
                val result = findHomeTextClick(itemname)
                if (result) {
                    addMoveExtra("点击${itemname}")
                }
                return result
            }
            ServiceCommand.HOME_TOP -> {
                val lists = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")
                if (AccessibilityUtils.isNodesAvalibale(lists)) {
                    for (list in lists) {
                        do {
                            sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
                        } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD))
                    }
                }
                return true
            }
            ServiceCommand.HOME_GRID_ITEM -> {
                val lists = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")
                if (AccessibilityUtils.isNodesAvalibale(lists)) {
                    for (list in lists) {
                        do {

                        } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD))
                    }
                }

                val itemname = command.commandStates[GlobalInfo.HOME_GRID_NAME]
                val items = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, "${itemname}")
                if (AccessibilityUtils.isNodesAvalibale(items)) {
                    val clickParent = AccessibilityUtils.findParentClickable(items[0])
                    if (clickParent != null) {
                        val result = clickParent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        if (result) {
                            addMoveExtra("点击${itemname}")
                        }
                        return result
                    }
                }

                return false
            }
        }
        return false
    }

    open fun beforeLeaveProductDetail() {}

    fun getSkuCommands(): ArrayList<Command> {
        val list = ArrayList<Command>()
        list.add(Command().commandCode(ServiceCommand.CLICK_PRODUCT_TAB2)
                .addScene(AccService.PRODUCT_DETAIL).delay(4000))
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


