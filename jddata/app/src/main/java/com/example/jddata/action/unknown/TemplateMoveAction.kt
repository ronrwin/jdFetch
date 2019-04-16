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

class TemplateMoveAction(env: Env, route: Route) : BaseAction(env, ActionType.TEMPLATE_MOVE) {
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
        return super.executeInner(command)
    }

}