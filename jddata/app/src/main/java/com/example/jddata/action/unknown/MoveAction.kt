package com.example.jddata.action.unknown

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.Route
import com.example.jddata.GlobalInfo
import com.example.jddata.MainApplication
import com.example.jddata.Session
import com.example.jddata.action.BaseAction
import com.example.jddata.action.Command
import com.example.jddata.service.ServiceCommand
import com.example.jddata.shelldroid.Env
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.BaseLogFile
import com.example.jddata.util.ExecUtils
import java.util.*

class MoveAction(env: Env, route: Route) : BaseAction(env, ActionType.TEMPLATE_MOVE) {
    var name = ""

    init {
        val sessionNo = route.id
        setState(GlobalInfo.ROUTE, route)
//        val sessionNo = 2
        if (sessionNo < Session.sTemplates.size()) {
            val template = Session.sTemplates[sessionNo]
            appendCommands(template.actions)
            name = "${template.templateId}号"
        }
    }

    override fun initLogFile() {
        isMoveAction = true
        logFile = BaseLogFile("动作_${name}")
        addMoveExtra("即将执行动作: ${name}")
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.INPUT -> {
                addMoveExtra("搜索：${command.states[GlobalInfo.SEARCH_KEY]}")
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
                        sleep(1000)
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

                val itemname = command.states[GlobalInfo.HOME_CARD_NAME] as String
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

                val itemname = command.states[GlobalInfo.HOME_GRID_NAME]
//                logFile?.writeToFileAppend("点击${itemname}")
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
                if (command.states.containsKey(GlobalInfo.MIAOSHA_TAB)) {
                    val tabName = command.states[GlobalInfo.MIAOSHA_TAB]
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
        }
        return super.executeInner(command)
    }

}