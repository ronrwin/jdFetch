package com.example.jddata.action.fetch

import com.example.jddata.BusHandler
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
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.LogUtil

class FetchGoodShopActionNoSku(env: Env) : BaseAction(env, ActionType.FETCH_GOOD_SHOP) {

    val set = HashSet<String>()
    init {
        appendCommand(Command().commandCode(ServiceCommand.FIND_TEXT).addScene(AccService.JD_HOME))
                .append(Command().commandCode(ServiceCommand.FETCH_PRODUCT).delay(18000L))
    }

    val name = GlobalInfo.GOOD_SHOP
    override fun initLogFile() {
        logFile = BaseLogFile("获取_$name")
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.FIND_TEXT -> {
                BusHandler.instance.startCountTimeout()
                logFile?.writeToFileAppend("找到并点击 $name")
                return findHomeTextClick(name)
            }
            ServiceCommand.FETCH_PRODUCT -> {
                val result = fetchProduct()
                return result
            }
        }
        return super.executeInner(command)
    }

    fun fetchProduct(): Boolean {
        val lists = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.widget.ScrollView")
        if (AccessibilityUtils.isNodesAvalibale(lists)) {
            val textNodes = AccessibilityUtils.findChildByClassname(lists[0], "android.widget.TextView")
            if (AccessibilityUtils.isNodesAvalibale(textNodes)) {
                for (node in textNodes) {
                    if (node.text != null &&
                            (node.text.toString().contains("旗舰店")
                                    || node.text.toString().contains("自营店")
                                    || node.text.toString().contains("专营店"))) {
                        val shop = node.text.toString()
                        if (set.add(shop)) {
                            itemCount++
                            logFile?.writeToFileAppend("获取第${itemCount}个店鋪：${shop}")

                            val map = HashMap<String, Any?>()
                            val row = RowData(map)
                            row.setDefaultData(env!!)
                            row.shop = shop.replace("\n", "")?.replace(",", "、")
                            row.biId = GlobalInfo.GOOD_SHOP
                            row.tab = "精选"
                            row.itemIndex = "${itemCount}"
                            LogUtil.dataCache(row)

                            if (itemCount >= GlobalInfo.FETCH_NUM) {
                                return true
                            }
                        }
                    }
                }
            }
            ExecUtils.fingerScroll()
            appendCommand(Command().commandCode(ServiceCommand.FETCH_PRODUCT).delay(1000L))
            return true
        }

        return false
    }

}