package com.example.jddata.action

import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.BrandDetail
import com.example.jddata.Entity.BrandEntity
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.BrandWorkBook
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand
import com.example.jddata.util.ExecUtils
import java.util.ArrayList

class BrandKillAction : BaseAction(ActionType.BRAND_KILL) {

    var mBrandEntitys = ArrayList<BrandEntity>()
    var brandTitleStrings = HashSet<String>()
    var scrollIndex = 0
    var isEnd = false
    var otherIndex = 0

    init {
        appendCommand(Command(ServiceCommand.HOME_BRAND_KILL).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.HOME_BRAND_KILL_SCROLL)
                        .addScene(AccService.MIAOSHA)
                        .delay(5000L)
                        .concernResult(true))
        workBook = BrandWorkBook()
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
            ServiceCommand.HOME_BRAND_KILL -> {
                workBook?.writeToSheetAppendWithTime("找到并点击 \"品牌秒杀\"")
                return CommonConmmand.findHomeTextClick(mService!!, "品牌秒杀")
            }
            ServiceCommand.HOME_BRAND_KILL_SCROLL -> {
                val result = brandKillFetchBrand()
                if (scrollIndex < GlobalInfo.SCROLL_COUNT && command.concernResult && result) {
                    appendCommand(PureCommand(ServiceCommand.BRAND_SELECT)
                            .addScene(AccService.MIAOSHA))
                }
                if (isEnd) {
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
            workBook?.writeToSheetAppend("时间", "位置", "产品", "价格", "原价")
            do {
                val titles = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.jdmiaosha:id/limit_buy_product_item_name")
                if (AccessibilityUtils.isNodesAvalibale(titles)) {
                    for (titleNode in titles!!) {
                        val parent = titleNode.parent
                        if (parent != null) {
                            var title: String? = null
                            if (titleNode.text != null) {
                                title = titleNode.text.toString()
                            }

                            val prices = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_miaosha_price")
                            var price = AccessibilityUtils.getFirstText(prices)

                            val originPrices = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.jdmiaosha:id/tv_miaosha_item_jd_price")
                            var origin = AccessibilityUtils.getFirstText(originPrices)

                            if (!TextUtils.isEmpty(title) && detailList.add(BrandDetail(title, price, origin))) {
                                workBook?.writeToSheetAppendWithTime("第${index + 1}屏", title, price, origin)
                                itemCount++
                                if (itemCount >= GlobalInfo.FETCH_NUM) {
                                    workBook?.writeToSheetAppend(GlobalInfo.FETCH_ENOUGH_DATE)
                                    return true
                                }
                            }
                        }
                    }
                    index++
                    if (index % 10 == 0) {
                        BusHandler.instance.startCountTimeout()
                    }
                }
                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
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

            workBook?.writeToSheetAppend("时间", "位置", "产品", "价格", "原价")

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
                    var title = AccessibilityUtils.getFirstText(titles)

                    val prices = parent.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/a1x")
                    var price = AccessibilityUtils.getFirstText(prices)

                    val originPrices = parent.findAccessibilityNodeInfosByViewId("com.jingdong.app.mall:id/a1y")
                    var origin = AccessibilityUtils.getFirstText(originPrices)

                    if (!TextUtils.isEmpty(title) && detailList.add(BrandDetail(title, price, origin))) {
                        workBook?.writeToSheetAppendWithTime("第${otherIndex + 1}屏", title, price, origin)// 收集100条
                        itemCount++
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
                        val title = AccessibilityUtils.getFirstText(titleNodes)
                        val price = AccessibilityUtils.getFirstText(priceNodes)
                        val origin = AccessibilityUtils.getFirstText(originNodes)
                        if (!TextUtils.isEmpty(title) && titleMap.add(title)) {
                            workBook?.writeToSheetAppendWithTime("第${otherIndex + 1}屏", title, price, origin)
                            itemCount++
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
                            val title = textNodes[0].text?.toString()
                            val price = textNodes[1].text?.toString()
                            if (!TextUtils.isEmpty(title) && titleMap.add(title!!)
                                    && !TextUtils.isEmpty(price) && price!!.contains("¥")) {
                                workBook?.writeToSheetAppendWithTime("第${otherIndex + 1}屏", title, price)
                                itemCount++
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

    private fun brandSelect(): Boolean {
        if (mBrandEntitys.isNotEmpty()) {
            var nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")

            if (AccessibilityUtils.isNodesAvalibale(nodes)) {
                val list = nodes[0]
                if (list != null) {
                    val brandEntity = mBrandEntitys.get(0)
                    var title = brandEntity.title
//                    title = "杰士邦专场"
                    val selectNodes = list.findAccessibilityNodeInfosByText(title)
                    if (AccessibilityUtils.isNodesAvalibale(selectNodes)) {
                        val parent = AccessibilityUtils.findParentClickable(selectNodes[0])
                        if (parent != null) {
                            workBook?.writeToSheetAppend("")
                            workBook?.writeToSheetAppendWithTime("找到并点击 $title")
                            workBook?.writeToSheetAppend("时间", "标题", "副标题")
                            workBook?.writeToSheetAppendWithTime(brandEntity.title, brandEntity.subtitle)

                            val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            if (result) {
                                mBrandEntitys.removeAt(0)
                            }
                            return result
                        }
                    }
                    // 没找到，原因是滚动时可能正好滚走了。本条就抛弃掉。
                    mBrandEntitys.removeAt(0)
                    Log.w("sss", "2 $title")
                    return brandSelect()
                }
            }
        }

        return false
    }

    private fun brandKillFetchBrand(): Boolean {
        if (mBrandEntitys.isNotEmpty()) {
            return true
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

    fun deleyScroll(list: AccessibilityNodeInfo): Boolean {
        sleep(2000L)
        val result = list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
        if (!result) {
            sleep(2000L)
            return list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
        }
        return result
    }

}