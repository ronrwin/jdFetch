package com.example.jddata.action.fetch

import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.Data2
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

class FetchHomeAction(env: Env) : BaseAction(env, ActionType.FETCH_HOME) {

    init {
        appendCommand(Command().commandCode(ServiceCommand.FETCH_PRODUCT))
    }

    override fun initLogFile() {
        logFile = BaseLogFile("获取_" + GlobalInfo.HOME)
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.FETCH_PRODUCT -> {
                return fetchProduct()
            }
        }
        return super.executeInner(command)
    }

    fun fetchProduct(): Boolean {
        val lists = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")

        val set = HashSet<String>()
        if (AccessibilityUtils.isNodesAvalibale(lists)) {
            for (list in lists) {
                var index = -10
                do {
                    // 推荐部分
                    val items = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/bxa")
                    if (AccessibilityUtils.isNodesAvalibale(items)) {
                        for (item in items) {
                            var product = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/bpt"))
                            var price = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/bpu"))

                            if (!TextUtils.isEmpty(product) && !TextUtils.isEmpty(price)) {
                                if (product.startsWith("1 ")) {
                                    product = product.replace("1 ", "");
                                }
                                price = price.replace("¥", "")
                                val recommend = Data2(product, price)
                                if (set.add(product)) {
                                    itemCount++

                                    val map = HashMap<String, Any?>()
                                    val row = RowData(map)
                                    row.setDefaultData(env!!)
                                    row.product = recommend.arg1?.replace("1 ", "")?.replace("\n", "")?.replace(",", "、")
                                    row.price = recommend.arg2?.replace("\n", "")?.replace(",", "、")
                                    row.biId = GlobalInfo.HOME
                                    row.itemIndex = "${itemCount}"
                                    LogUtil.dataCache(row)

                                    logFile?.writeToFileAppend("收集${itemCount}商品：", product, price)

                                    if (itemCount >= GlobalInfo.FETCH_NUM) {
                                        return true
                                    }
                                }
                            }
                        }

                    }

                    index++
                    sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
                } while (ExecUtils.canscroll(list, index))

                logFile?.writeToFileAppend(GlobalInfo.NO_MORE_DATA)
                return true
            }
        }

        return false
    }

}