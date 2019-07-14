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

class FetchProductDetailJilieAction(env: Env) : BaseAction(env, ActionType.FETCH_PRODUCT_JILIE) {

    init {
        appendCommand(Command().commandCode(ServiceCommand.QR_CODE)
                .setState(GlobalInfo.QRCODE_PIC, GlobalInfo.JILIE_1))
                .append(Command().commandCode(ServiceCommand.SCAN_ALBUM)
                        .addScene(AccService.CAPTURE_SCAN).delay(2000))
                .append(Command().commandCode(ServiceCommand.SCAN_PIC)
                        .addScene(AccService.PHOTO_ALBUM))
                .append((Command().commandCode(ServiceCommand.PRODUCT_DONE)
                        .addScene(AccService.PRODUCT_DETAIL).delay(4000)))
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
            var index = GlobalInfo.SCROLL_COUNT - 10
            do {
                val proItems = lists[0].findAccessibilityNodeInfosByViewId("com.jd.lib.productdetail:id/pd_recommend_more")
                if (AccessibilityUtils.isNodesAvalibale(proItems)) {
                    val pagers = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.productdetail:id/detail_recommend_viewpager")
                    if (AccessibilityUtils.isNodesAvalibale(pagers)) {
                        for (page in pagers) {
                            var pageIndex = GlobalInfo.SCROLL_COUNT - 5

                            do {
                                val items = page.findAccessibilityNodeInfosByViewId("com.jd.lib.productdetail:id/pd_recommend_item_layout")
                                for (item in items) {
                                    var product = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jd.lib.productdetail:id/detail_recommend_name"))
                                    var price = AccessibilityUtils.getFirstText(item.findAccessibilityNodeInfosByViewId("com.jd.lib.productdetail:id/detail_recommend_price"))

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
                                            row.biId = GlobalInfo.HOME_PRODUCT_RECOMMEND
                                            row.itemIndex = "${itemCount}"
                                            LogUtil.dataCache(row)

                                            logFile?.writeToFileAppend("收集${itemCount}商品：", product, price)

                                            if (itemCount >= GlobalInfo.FETCH_NUM) {
                                                return true
                                            }
                                        }
                                    }
                                }
                                pageIndex++
                                sleep(1000)
                            } while (ExecUtils.canscroll(page, pageIndex))
                        }
                    }
                }

                index++
                sleep(1000)
            } while (ExecUtils.canscroll(lists[0], index))

            logFile?.writeToFileAppend(GlobalInfo.NO_MORE_DATA)
            return true
        }

        return false
    }
}