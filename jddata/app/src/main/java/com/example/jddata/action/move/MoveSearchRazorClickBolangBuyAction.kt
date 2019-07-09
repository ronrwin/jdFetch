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

open class MoveSearchRazorClickBolangBuyAction(env: Env) : BaseAction(env, ActionType.MOVE_SEARCH_RAZOR_CLICK_BOLANG_BUY) {
    var searchText: String? = null
    var clickText: String? = null

    init {
        searchText = "剃须刀"
        clickText = "博朗"
        appendCommand(Command().commandCode(ServiceCommand.CLICK_SEARCH).addScene(AccService.JD_HOME))
                .append(Command().commandCode(ServiceCommand.INPUT).addScene(AccService.SEARCH)
                        .setState(GlobalInfo.SEARCH_KEY, searchText!!))
                .append(Command().commandCode(ServiceCommand.SEARCH))
                .append(Command().commandCode(ServiceCommand.SEARCH_CSELECT).addScene(AccService.PRODUCT_LIST))
                .append(Command().commandCode(ServiceCommand.TEMPLATE_ADD_TO_CART).delay(5000)
                        .addScene(AccService.PRODUCT_DETAIL))
                .append(Command().commandCode(ServiceCommand.PRODUCT_CONFIRM)
                        .delay(3000).canSkip(true))
    }

    override fun initLogFile() {
        isMoveAction = true
        logFile = BaseLogFile("动作_搜索_${searchText}_点击${clickText}_加购")
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
            ServiceCommand.SEARCH_CSELECT -> {
                val lists = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/a0t")
                if (AccessibilityUtils.isNodesAvalibale(lists)) {
                    var index = 0
                    do {
                        val titleNodes = lists[0].findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/zi")
                        if (AccessibilityUtils.isNodesAvalibale(titleNodes)) {
                            val title = AccessibilityUtils.getFirstText(titleNodes)
                            if (title != null && title.contains(clickText!!)) {
                                val parent = AccessibilityUtils.findParentClickable(titleNodes[0])
                                if (parent != null) {
                                    var price = AccessibilityUtils.getFirstText(parent.findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/a9k"))
                                    addMoveExtra("点击商品： " + title + "， 价格： " + price)
                                    return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
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
