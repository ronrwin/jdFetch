package com.example.jddata.action.fetch

import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.RowData
import com.example.jddata.GlobalInfo
import com.example.jddata.action.BaseAction
import com.example.jddata.action.Command
import com.example.jddata.action.append
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.shelldroid.Env
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.BaseLogFile
import com.example.jddata.util.LogUtil

class FetchDmpAction(env: Env) : BaseAction(env, ActionType.FETCH_DMP) {

    init {
        appendCommand(Command().commandCode(ServiceCommand.HOME_DMP).delay(4000))
                .append(Command().commandCode(ServiceCommand.DMP_TITLE)
//                        .addScene(AccService.WEBVIEW_ACTIVITY)
//                        .addScene(AccService.BABEL_ACTIVITY)
//                        .addScene(AccService.JSHOP)
                        .delay(5000))
                .append(Command().commandCode(ServiceCommand.HOME))
        for (i in 0 until 7) {
            appendCommand(Command().commandCode(ServiceCommand.HOME_DMP).delay(4000).addScene(AccService.JD_HOME))
                    .append(Command().commandCode(ServiceCommand.DMP_TITLE)
//                            .addScene(AccService.WEBVIEW_ACTIVITY)
//                            .addScene(AccService.BABEL_ACTIVITY)
//                            .addScene(AccService.JSHOP)
                            .delay(5000))
                    .append(Command().commandCode(ServiceCommand.HOME))
        }
    }

    override fun initLogFile() {
        logFile = BaseLogFile("获取_" + GlobalInfo.DMP)
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.DMP_TITLE -> {
                var nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/ff")
                if (AccessibilityUtils.isNodesAvalibale(nodes)) {
                    val title = AccessibilityUtils.getFirstText(nodes)
                    itemCount++

                    addMoveExtra("dmp广告标题：${title}")

                    val map = HashMap<String, Any?>()
                    val row = RowData(map)
                    row.setDefaultData(env!!)
                    row.title = title?.replace("\n", "")?.replace(",", "、")
                    row.biId = GlobalInfo.DMP
                    row.itemIndex = "${itemCount}"
                    LogUtil.dataCache(row)

                    return true
                }

                nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/a6s")
                if (AccessibilityUtils.isNodesAvalibale(nodes)) {
                    val title = AccessibilityUtils.getFirstText(nodes)
                    itemCount++
                    addMoveExtra("dmp广告标题：${title}")
                    return true
                }

                nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jshop:id/jshop_shopname")
                if (AccessibilityUtils.isNodesAvalibale(nodes)) {
                    val title = AccessibilityUtils.getFirstText(nodes)
                    itemCount++
                    addMoveExtra("dmp广告标题：${title}")
                    return true
                }
                return false
            }
        }
        return super.executeInner(command)
    }
}