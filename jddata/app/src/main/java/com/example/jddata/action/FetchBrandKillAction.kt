package com.example.jddata.action

import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.BrandDetail
import com.example.jddata.Entity.BrandEntity
import com.example.jddata.Entity.RowData
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.BaseWorkBook
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.LogUtil
import java.util.ArrayList

class FetchBrandKillAction : BaseAction(ActionType.FETCH_BRAND_KILL) {

    var mBrandEntitys = ArrayList<BrandEntity>()
    var brandTitleStrings = HashSet<String>()
    var scrollIndex = 0
    var isEnd = false
    var otherIndex = 0
    var fetchBrandCount = 0     // 抓10个大类

    init {
        appendCommand(Command(ServiceCommand.HOME_BRAND_KILL).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.HOME_BRAND_KILL_SCROLL)
                        .addScene(AccService.MIAOSHA)
                        .delay(5000L)
                        .concernResult(true))
    }

    override fun initWorkbook() {
        workBook = BaseWorkBook("获取_" + GlobalInfo.BRAND_KILL)
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
            ServiceCommand.HOME_BRAND_KILL -> {
                workBook?.writeToSheetAppendWithTime("找到并点击 \"${GlobalInfo.BRAND_KILL}\"")
                return CommonConmmand.findHomeTextClick(mService!!, GlobalInfo.BRAND_KILL)
            }
            ServiceCommand.HOME_BRAND_KILL_SCROLL -> {
                val result = brandKillFetchBrand()
                if (scrollIndex < GlobalInfo.SCROLL_COUNT && command.concernResult && result) {
                    appendCommand(PureCommand(ServiceCommand.BRAND_SELECT)
                            .addScene(AccService.MIAOSHA))
                }
                if (isEnd) {
                    val command = getCurrentCommand()
                    mCommandArrayList.clear()
                    appendCommand(command!!)
                    return true
                }
                return result
            }
            ServiceCommand.BRAND_SELECT -> {
                val result = brandSelect()
                if (result) {
                    appendCommand(Command(ServiceCommand.BRAND_DETAIL)
                                    .addScene(AccService.BRAND_MIAOSHA)
                                    .addScene(AccService.BABEL_ACTIVITY)
                                    .addScene(AccService.JSHOP)
                                    .addScene(AccService.WEBVIEW_ACTIVITY))
                                .append(PureCommand(ServiceCommand.GO_BACK))
                                .append(Command(ServiceCommand.HOME_BRAND_KILL_SCROLL)
                                    .addScene(AccService.MIAOSHA)
                                    .concernResult(true))
                } else {
                    appendCommand(PureCommand(ServiceCommand.HOME_BRAND_KILL_SCROLL)
                            .addScene(AccService.MIAOSHA).concernResult(true))
                }
                return result
            }
            ServiceCommand.BRAND_DETAIL -> {
                itemCount = 0
                var result = brandDetail()
                if (!result) {
                    result = shopTypeFetch()
                }
                if (itemCount > 0) {
                    // 有记录才计数
                    fetchBrandCount++
                }
                return result
            }
        }
        return super.executeInner(command)
    }


    private fun brandDetail(): Boolean {
        var nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")
        if (AccessibilityUtils.isNodesAvalibale(nodes)) {
            val list = nodes!![0]
            var index = 0
            val detailList = HashSet<BrandDetail>()
            workBook?.writeToSheetAppend("时间", "位置", "产品", "价格", "原价", "标题", "副标题")
            do {
                val titles = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/limit_buy_product_item_name")
                if (AccessibilityUtils.isNodesAvalibale(titles)) {
                    for (titleNode in titles!!) {
                        val parent = titleNode.parent
                        if (parent != null) {
                            var product: String? = null
                            if (titleNode.text != null) {
                                product = titleNode.text.toString()
                            }

                            val prices = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_miaosha_price")
                            var price = AccessibilityUtils.getFirstText(prices)

                            val originPrices = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_jd_price")
                            var origin = AccessibilityUtils.getFirstText(originPrices)

                            if (!TextUtils.isEmpty(product) && !TextUtils.isEmpty(price) && detailList.add(BrandDetail(product, price, origin)) && currentBrandEntity != null) {
                                if (price != null) {
                                    price = price.replace("¥", "")
                                }
                                if (origin != null) {
                                    origin = origin.replace("¥", "")
                                }
                                workBook?.writeToSheetAppendWithTime("${itemCount+1}", product, price, origin, currentBrandEntity!!.title, currentBrandEntity!!.subtitle)

                                val map = HashMap<String, Any?>()
                                val row = RowData(map)
                                row.setDefaultData()
                                row.product = product?.replace("\n", "")?.replace(",", "、")
                                row.price = price.replace("\n", "")?.replace(",", "、")
                                row.originPrice = origin?.replace("\n", "")?.replace(",", "、")
                                row.biId = GlobalInfo.BRAND_KILL
                                row.itemIndex = "${itemCount+1}"
                                row.title = currentBrandEntity!!.title?.replace("\n", "")?.replace(",", "、")
                                row.subtitle = currentBrandEntity!!.subtitle?.replace("\n", "")?.replace(",", "、")
                                LogUtil.writeDataLog(row)

                                itemCount++
                                fetchCount++
                                if (itemCount >= GlobalInfo.FETCH_NUM) {
                                    workBook?.writeToSheetAppend(GlobalInfo.FETCH_ENOUGH_DATE)
                                    return true
                                }
                            }
                        }
                    }
                }
                index++
                if (index % 10 == 0) {
                    BusHandler.instance.startCountTimeout()
                }
                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while ((list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD))
                    && index < GlobalInfo.SCROLL_COUNT)
        }
        return false
    }

    fun shopTypeFetch():Boolean {
        var nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/a4n")
         // 有关注按钮，是店铺
        if (AccessibilityUtils.isNodesAvalibale(nodes)) {
            nodes = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")
        }
//        nodes = AccessibilityUtils.findChildByClassname(mService!!.rootInActiveWindow, "android.support.v7.widget.RecyclerView")
        if (AccessibilityUtils.isNodesAvalibale(nodes)) {
            val list = nodes[0]
            val detailList = HashSet<BrandDetail>()
            var titleMap = HashSet<String>()

            workBook?.writeToSheetAppend("时间", "位置", "产品", "价格", "原价", "标题", "副标题")

            var result = circular(list, detailList, titleMap)
            if (result) {
                // 获取成功
                return true
            }

            // 循环3次
            for (i in 0..2) {
                sleep(2000L)
                val scrollResult = (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                        || ExecUtils.fingerScroll()) && otherIndex < GlobalInfo.SCROLL_COUNT
                if (scrollResult) {
                    result = circular(list, detailList, titleMap)
                    if (result) {
                        return true
                    }
                }
            }
        }

        workBook?.writeToSheetAppend(GlobalInfo.NO_MORE_DATA)
        return false
    }

    fun circular(list: AccessibilityNodeInfo, detailList: HashSet<BrandDetail>, titleMap: HashSet<String>): Boolean {
        do {
            var result1 = shopType1(detailList)
            var result2 = shopType2(titleMap)
            var result3 = shopType3(titleMap)
            if (result1 || result2 || result3) {
                return true
            }

            otherIndex++
            if (otherIndex % 10 == 0) {
                BusHandler.instance.startCountTimeout()
            }
            sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
        } while ((list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                        || ExecUtils.fingerScroll())
                    && otherIndex < GlobalInfo.SCROLL_COUNT)
        return false
    }

    fun shopType1(detailList: HashSet<BrandDetail>): Boolean {
        // 针对不同页面
        val titleNodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/a1s")
        if (AccessibilityUtils.isNodesAvalibale(titleNodes)) {
            for (titleNode in titleNodes) {
                val parent = AccessibilityUtils.findParentClickable(titleNode)
                if (parent != null) {
                    val titles = parent.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/a1s")
                    var product = AccessibilityUtils.getFirstText(titles)

                    val prices = parent.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/a1x")
                    var price = AccessibilityUtils.getFirstText(prices)

                    val originPrices = parent.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/a1y")
                    var origin = AccessibilityUtils.getFirstText(originPrices)

                    if (!TextUtils.isEmpty(product) && !TextUtils.isEmpty(price) && detailList.add(BrandDetail(product, price, origin))) {
                        if (price != null) {
                            price = price.replace("¥", "")
                        }
                        if (origin != null) {
                            origin = origin.replace("¥", "")
                        }
                        workBook?.writeToSheetAppendWithTime("${itemCount+1}", product, price, origin, currentBrandEntity!!.title, currentBrandEntity!!.subtitle)// 收集100条

                        val map = HashMap<String, Any?>()
                        val row = RowData(map)
                        row.setDefaultData()
                        row.product = product.replace("\n", "")?.replace(",", "、")
                        row.price = price.replace("\n", "")?.replace(",", "、")
                        row.originPrice = origin?.replace("\n", "")?.replace(",", "、")
                        row.biId = GlobalInfo.BRAND_KILL
                        row.itemIndex = "${itemCount+1}"
                        row.title = currentBrandEntity!!.title?.replace("\n", "")?.replace(",", "、")
                        row.subtitle = currentBrandEntity!!.subtitle?.replace("\n", "")?.replace(",", "、")
                        LogUtil.writeDataLog(row)

                        itemCount++
                        fetchCount++
                        if (itemCount >= GlobalInfo.FETCH_NUM) {
                            workBook?.writeToSheetAppend(GlobalInfo.FETCH_ENOUGH_DATE)
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    fun shopType2(titleMap: HashSet<String>): Boolean {
        var result = false
        // 针对不同页面
        var titleNodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jingdong.app.mall:id/a39")
        if (AccessibilityUtils.isNodesAvalibale(titleNodes)) {
            for (title in titleNodes) {
                val parent = title.parent
                if (parent != null) {
                    val titles = parent.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/a39")
                    val priceNodes = parent.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/a3d")
                    val originNodes = parent.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/a3e")
                    if (AccessibilityUtils.isNodesAvalibale(titles) && AccessibilityUtils.isNodesAvalibale(priceNodes)) {
                        val product = AccessibilityUtils.getFirstText(titleNodes)
                        var price = AccessibilityUtils.getFirstText(priceNodes)
                        var origin = AccessibilityUtils.getFirstText(originNodes)
                        if (!TextUtils.isEmpty(product) && !TextUtils.isEmpty(price) && titleMap.add(product)) {
                            if (price != null) {
                                price = price.replace("¥", "")
                            }
                            if (origin != null) {
                                origin = origin.replace("¥", "")
                            }
                            workBook?.writeToSheetAppendWithTime("${itemCount+1}", product, price, origin, currentBrandEntity!!.title, currentBrandEntity!!.subtitle)

                            val map = HashMap<String, Any?>()
                            val row = RowData(map)
                            row.setDefaultData()
                            row.product = product.replace("\n", "")?.replace(",", "、")
                            row.price = price.replace("\n", "")?.replace(",", "、")
                            row.originPrice = origin?.replace("\n", "")?.replace(",", "、")
                            row.biId = GlobalInfo.BRAND_KILL
                            row.itemIndex = "${itemCount+1}"
                            row.title = currentBrandEntity!!.title?.replace("\n", "")?.replace(",", "、")
                            row.subtitle = currentBrandEntity!!.subtitle?.replace("\n", "")?.replace(",", "、")
                            LogUtil.writeDataLog(row)

                            itemCount++
                            fetchCount++
                            if (itemCount >= GlobalInfo.FETCH_NUM) {
                                workBook?.writeToSheetAppend(GlobalInfo.FETCH_ENOUGH_DATE)
                                return true
                            }
                        }
                    }
                }
            }
        }
        return result
    }

    fun shopType3(titleMap: HashSet<String>): Boolean {
        val priceTitleNodes = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, "¥")
        if (AccessibilityUtils.isNodesAvalibale(priceTitleNodes)) {
            for (priceNode in priceTitleNodes) {
                if (!TextUtils.isEmpty(priceNode.text)) {
                    val parent = priceNode.parent
                    // 针对不同页面
                    val textNodes = AccessibilityUtils.findChildByClassname(parent, "android.widget.TextView")
                    if (AccessibilityUtils.isNodesAvalibale(textNodes)) {
                        if (textNodes.size == 2) {
                            val product = textNodes[0].text?.toString()
                            var price = textNodes[1].text?.toString()

                            if (!TextUtils.isEmpty(product) && titleMap.add(product!!)
                                    && !TextUtils.isEmpty(price) && price!!.contains("¥")) {
                                if (price != null) {
                                    price = price.replace("¥", "")
                                }
                                workBook?.writeToSheetAppendWithTime("${itemCount+1}", product, price, "", currentBrandEntity!!.title, currentBrandEntity!!.subtitle)

                                val map = HashMap<String, Any?>()
                                val row = RowData(map)
                                row.setDefaultData()
                                row.product = product.replace("\n", "")?.replace(",", "、")
                                row.price = price.replace("\n", "")?.replace(",", "、")
                                row.biId = GlobalInfo.BRAND_KILL
                                row.itemIndex = "${itemCount+1}"
                                row.title = currentBrandEntity!!.title?.replace("\n", "")?.replace(",", "、")
                                row.subtitle = currentBrandEntity!!.subtitle?.replace("\n", "")?.replace(",", "、")
                                LogUtil.writeDataLog(row)

                                itemCount++
                                fetchCount++
                                if (itemCount >= GlobalInfo.FETCH_NUM) {
                                    workBook?.writeToSheetAppend(GlobalInfo.FETCH_ENOUGH_DATE)
                                    return true
                                }
                            }
                        }
                    }
                }
            }
        }
        return false
    }

    var currentBrandEntity: BrandEntity? = null
    private fun brandSelect(): Boolean {
        if (fetchBrandCount >= GlobalInfo.BRAND_KILL_COUNT) {
            mBrandEntitys.clear()
            return false
        }

        if (mBrandEntitys.isNotEmpty()) {
            var nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")

            if (AccessibilityUtils.isNodesAvalibale(nodes)) {
                val list = nodes[0]
                if (list != null) {
                    currentBrandEntity = mBrandEntitys.get(0)
                    var title = currentBrandEntity!!.title
                    val selectNodes = list.findAccessibilityNodeInfosByText(title)
                    if (AccessibilityUtils.isNodesAvalibale(selectNodes)) {
                        val parent = AccessibilityUtils.findParentClickable(selectNodes[0])
                        if (parent != null) {
                            workBook?.writeToSheetAppend("")
                            workBook?.writeToSheetAppendWithTime("找到并点击 $title")
                            workBook?.writeToSheetAppend("时间", "标题", "副标题")
                            workBook?.writeToSheetAppendWithTime(currentBrandEntity!!.title, currentBrandEntity!!.subtitle)

                            val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            if (result) {
                                mBrandEntitys.removeAt(0)
                            }
                            return result
                        }
                    }
                    // 没找到，原因是滚动时可能正好滚走了。本条就抛弃掉。
                    mBrandEntitys.removeAt(0)
                    return brandSelect()
                }
            }
        } else {
            currentBrandEntity = null
        }

        return false
    }

    private fun brandKillFetchBrand(): Boolean {
        if (mBrandEntitys.isNotEmpty()) {
            return true
        }
        if (fetchBrandCount >= GlobalInfo.BRAND_KILL_COUNT) {
            isEnd = true
            return false
        }

        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false
        val list = nodes!![0]
        if (list != null) {
            do {
                val brandTitles = list.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/miaosha_brand_title")
                for (brand in brandTitles) {
                    val parent = brand.parent
                    if (parent != null) {
                        var title: String? = null
                        if (brand.text != null) {
                            title = brand.text.toString()
                        }

                        val subTitles = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/miaosha_brand_subtitle")
                        var subTitle = AccessibilityUtils.getFirstText(subTitles)
                        if (title != null) {
                            if (brandTitleStrings.add(title)) {
                                // 能成功加进set去，说明之前没有记录
                                mBrandEntitys.add(BrandEntity(title, subTitle))
                            }
                        }
                    }
                }

                if (scrollIndex < GlobalInfo.SCROLL_COUNT && mBrandEntitys.isNotEmpty()) {
                    // 有新的记录，跳出循环
                    return true
                }
                scrollIndex++
                if (scrollIndex % 10 == 0) {
                    BusHandler.instance.startCountTimeout()
                }
                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while ((list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                            || ExecUtils.fingerScroll())
                    && scrollIndex < GlobalInfo.SCROLL_COUNT)
            isEnd = true
        }

        return false
    }

}