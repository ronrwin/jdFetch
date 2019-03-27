package com.example.jddata.action.unknown

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.Entity.ActionType
import com.example.jddata.GlobalInfo
import com.example.jddata.Session
import com.example.jddata.action.BaseAction
import com.example.jddata.action.Command
import com.example.jddata.service.ServiceCommand
import com.example.jddata.shelldroid.EnvManager
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.BaseLogFile
import com.example.jddata.util.ExecUtils
import java.text.SimpleDateFormat

class MoveAction : BaseAction(ActionType.TEMPLATE_MOVE) {
    var name = ""
    init {
        val sessionNo = Session.sTemplates[0].templateId
        appendCommands(Session.sTemplates[0].actions)
        name = "${sessionNo}"
    }

    override fun initLogFile() {
        isMoveAction = true
        logFile = BaseLogFile("动作_${name}")
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
                val itemname = command.states[GlobalInfo.HOME_CARD_NAME] as String
                val result = findHomeTextClick(itemname)
                if (result) {
                    addMoveExtra("点击${itemname}")
                }
                return result
            }
            ServiceCommand.HOME_DMP -> {
                ExecUtils.handleExecCommand("input tap 300 200")
                sleep(2000)
                val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/ff")
                if (AccessibilityUtils.isNodesAvalibale(nodes)) {
                    val title = AccessibilityUtils.getFirstText(nodes)
                    addMoveExtra("dmp广告标题：${title}")
                    return true
                }
                return false
            }
            ServiceCommand.HOME_GRID_ITEM -> {
                val itemname = command.states[GlobalInfo.HOME_GRID_NAME]
                logFile?.writeToFileAppend("点击${itemname}")
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
            ServiceCommand.HOME_FLASH_BUY -> {
                logFile?.writeToFileAppend("找到并点击 闪购")
                val result =  findHomeTextClick(name)
                if (result) {
                    addMoveExtra("点击闪购")
                }
                return result
            }
        }
        return super.executeInner(command)
    }

}