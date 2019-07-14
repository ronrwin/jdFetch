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

open class MoveSearchHaifeisiClickAction(env: Env) : BaseAction(env, ActionType.MOVE_SEARCH_HAIFEISI_CLICK) {
    init {
        searchText = "海飞丝"
        clickText = "海飞丝"
        appendCommand(Command().commandCode(ServiceCommand.CLICK_SEARCH).addScene(AccService.JD_HOME))
                .append(Command().commandCode(ServiceCommand.INPUT).addScene(AccService.SEARCH)
                        .setState(GlobalInfo.SEARCH_KEY, searchText!!))
                .append(Command().commandCode(ServiceCommand.SEARCH))
                .append(Command().commandCode(ServiceCommand.SEARCH_CSELECT).addScene(AccService.PRODUCT_LIST))
    }

    override fun initLogFile() {
        isMoveAction = true
        logFile = BaseLogFile("动作_搜索_${searchText}_点击${clickText}")
        var day9No = getState(GlobalInfo.MOVE_NO) as Int
        addMoveExtra("动作： " + day9No)
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
            ServiceCommand.SEARCH_CSELECT -> {
                val lists = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.search:id/product_list")
                if (AccessibilityUtils.isNodesAvalibale(lists)) {
                    var index = 0
                    do {
                        val titleNodes = lists[0].findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/product_item_name")
                        if (AccessibilityUtils.isNodesAvalibale(titleNodes)) {
                            var title = AccessibilityUtils.getFirstText(titleNodes)
                            var price = AccessibilityUtils.getFirstText(lists[0].findAccessibilityNodeInfosByViewId("com.jd.lib.search:id/product_item_jdPrice"))
                            if (title != null && title.contains(clickText!!)) {
                                title = title.replace("1 ", "");
                                val parent = AccessibilityUtils.findParentClickable(titleNodes[0])
                                if (parent != null) {
                                    addMoveExtra("点击商品： " + title.replace("1 ", "") + "， 价格： " + price)
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
