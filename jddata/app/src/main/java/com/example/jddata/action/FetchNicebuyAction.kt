package com.example.jddata.action

import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.NiceBuyDetail
import com.example.jddata.Entity.NiceBuyEntity
import com.example.jddata.Entity.RowData
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.BaseWorkBook
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand
import com.example.jddata.util.LogUtil
import java.util.ArrayList

class FetchNicebuyAction : BaseAction(ActionType.FETCH_NICE_BUY) {

    var nicebuyTitles = HashSet<String>()
    var mNiceBuyTitleEntitys = ArrayList<NiceBuyEntity>()
    var scrollIndex = 0
    var isEnd = false

    init {
        appendCommand(Command(ServiceCommand.NICE_BUY).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.NICE_BUY_SCROLL).addScene(AccService.WORTHBUY).concernResult(true).delay(6000L))

    }

    override fun initWorkbook() {
        workBook = BaseWorkBook("获取_" + GlobalInfo.NICE_BUT)
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.NICE_BUY -> {
                workBook?.writeToSheetAppendWithTime("找到并点击 \"${GlobalInfo.NICE_BUT}\"")
                return CommonConmmand.findHomeTextClick(mService!!, GlobalInfo.NICE_BUT)
            }
            ServiceCommand.NICE_BUY_SCROLL -> {
                val result = niceBuyScroll()
                if (scrollIndex < GlobalInfo.SCROLL_COUNT && command.concernResult && result) {
                    appendCommand(PureCommand(ServiceCommand.NICE_BUY_SELECT).addScene(AccService.WORTHBUY))
                            .append(Command(ServiceCommand.NICE_BUY_DETAIL).addScene(AccService.INVENTORY))
                            .append(PureCommand(ServiceCommand.GO_BACK))
                            // 再次找可点击的标题
                            .append(Command(ServiceCommand.NICE_BUY_SCROLL).addScene(AccService.WORTHBUY).concernResult(true))
                }
                if (isEnd) {
                    mCommandArrayList.clear()
                    appendCommand(command!!)
                    return true
                }
                return result
            }
            ServiceCommand.NICE_BUY_SELECT -> {
                return niceBuySelect()
            }
            ServiceCommand.NICE_BUY_DETAIL -> {
                return niceBuyDetail()
            }
        }
        return super.executeInner(command)
    }

    private fun niceBuyDetail(): Boolean {
        itemCount = 0
        val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/recycler_view")
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false
        val list = nodes[0]
        var description = ""
        if (list != null) {
            val descs = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/tv_desc")
            if (AccessibilityUtils.isNodesAvalibale(descs)) {
                val desc = descs[0]
                if (desc.text != null) {
                    val des = desc.text.toString()
                    workBook?.writeToSheetAppend("时间", "描述")
                    workBook?.writeToSheetAppendWithTime(des)
                    description = des
                }
            }

            var index = 0
            val detailList = HashSet<NiceBuyDetail>()

            // 是否已经展示 你可能还想看
            var isShowRecommend = false
            var recommendTitles = HashSet<String>()
            workBook?.writeToSheetAppend("时间", "位置", "产品", "价格", "原价", "标题", "描述", "数量", "看过数", "收藏数")
            do {
                // 店铺商品列表
                var hasDatas = false
                val prices = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/tv_price")
                if (AccessibilityUtils.isNodesAvalibale(prices)) {
                    hasDatas = true
                    for (priceNode in prices!!) {
                        val parent = priceNode.parent
                        if (parent != null) {
                            val titles = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_title")
                            var product = AccessibilityUtils.getFirstText(titles)
                            if (product != null && product.startsWith("1 ")) {
                                product = product.replace("1 ", "");
                            }

                            var price: String? = null
                            if (priceNode.text != null) {
                                price = priceNode.text.toString()
                            }

                            val originPrices = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_original_price")
                            var origin = AccessibilityUtils.getFirstText(originPrices)

                            if (!TextUtils.isEmpty(product) && !TextUtils.isEmpty(price) && detailList.add(NiceBuyDetail(product, price, origin)) && currentNiceBuyEntity != null) {
                                if (price != null) {
                                    price = price.replace("¥", "")
                                }
                                if (origin != null) {
                                    origin = origin.replace("¥", "")
                                }
                                workBook?.writeToSheetAppendWithTime("${itemCount+1}", product, price, origin, currentNiceBuyEntity!!.title, description, currentNiceBuyEntity!!.desc, currentNiceBuyEntity!!.pageView, currentNiceBuyEntity!!.collect)

                                val map = HashMap<String, Any?>()
                                val row = RowData(map)
                                row.setDefaultData()
                                row.product = product.replace("\n", "")
                                row.price = price?.replace("\n", "")
                                row.originPrice = origin?.replace("\n", "")
                                row.description = description?.replace("\n", "")
                                row.biId = GlobalInfo.NICE_BUT
                                row.itemIndex = "${itemCount+1}"
                                row.title = currentNiceBuyEntity!!.title?.replace("\n", "")
                                row.num = currentNiceBuyEntity!!.desc?.replace("\n", "")
                                row.viewdNum = currentNiceBuyEntity!!.pageView?.replace("\n", "")
                                row.markNum = currentNiceBuyEntity!!.collect?.replace("\n", "")
                                LogUtil.writeDataLog(row)

                                itemCount++
                                if (itemCount >= GlobalInfo.FETCH_NUM) {
                                    workBook?.writeToSheetAppend(GlobalInfo.FETCH_ENOUGH_DATE)
                                    return true
                                }
                            }
                        }
                    }
                }

                // 你可能还想看
//                val descsNodes = list.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_zdm_inventory_desc")
//                if (AccessibilityUtils.isNodesAvalibale(descsNodes)) {
//                    hasDatas = true
//                    if (!isShowRecommend) {
//                        isShowRecommend = true
//                        workBook?.writeToSheetAppend("--------你可能还想看--------")
//                        workBook?.writeToSheetAppend("时间", "位置", "标题", "数量", "看过数", "收藏数")
//                    }
//                    for (descNode in descsNodes) {
//                        val parent = descNode.parent
//                        if (parent != null) {
//                            val titles = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_zdm_inventory_title")
//                            var product = AccessibilityUtils.getFirstText(titles)
//                            if (product != null && product.startsWith("1 ")) {
//                                product = product.replace("1 ", "");
//                            }
//
//                            val descs = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_zdm_inventory_desc")
//                            var desc = AccessibilityUtils.getFirstText(descs)
//
//                            val pageViews = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/page_view")
//                            var pageView = AccessibilityUtils.getFirstText(pageViews)
//
//                            val collects = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/text_collect_number")
//                            var collect = AccessibilityUtils.getFirstText(collects)
//
//                            val nice = NiceBuyEntity(product, desc, pageView, collect)
//                            if (!TextUtils.isEmpty(product) && recommendTitles.add(product)) {
//                                workBook?.writeToSheetAppendWithTime("第${index+1}屏", product, desc, pageView, collect)
//
//                                val map = HashMap<String, Any?>()
//                                val row = RowData(map)
//                                row.product = product
//                                row.description = desc
//                                row.viewdNum = pageView
//                                row.biId = "会买专辑"
//                                row.itemIndex = "第${index+1}屏"
//                                LogUtil.writeDataLog(row)
//
//                                itemCount++
//                                if (itemCount >= GlobalInfo.FETCH_NUM) {
//                                    workBook?.writeToSheetAppend(GlobalInfo.FETCH_ENOUGH_DATE)
//                                    return true
//                                }
//                            }
//                        }
//                    }
//                }

                if (hasDatas) {
                    index++
                    if (index % 10 == 0) {
                        BusHandler.instance.startCountTimeout()
                    }
                }
                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD))

            workBook?.writeToSheetAppend(GlobalInfo.NO_MORE_DATA)
            return true
        }
        return false
    }

    var currentNiceBuyEntity: NiceBuyEntity? = null
    private fun niceBuySelect(): Boolean {
        if (mNiceBuyTitleEntitys.isNotEmpty()) {
            val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/ll_zdm_inventory_header")
            if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false
            val list = AccessibilityUtils.findParentByClassname(nodes[0], "android.support.v7.widget.RecyclerView")
            if (list != null) {
                currentNiceBuyEntity = mNiceBuyTitleEntitys[0]
                val title = currentNiceBuyEntity!!.title
                val selectNodes = list.findAccessibilityNodeInfosByText(title)
                if (AccessibilityUtils.isNodesAvalibale(selectNodes)) {
                    val parent = AccessibilityUtils.findParentClickable(selectNodes[0])
                    if (parent != null) {
                        workBook?.writeToSheetAppend("")
                        workBook?.writeToSheetAppendWithTime("找到并点击 $title")
                        workBook?.writeToSheetAppend("时间", "标题", "数量", "看过数", "收藏数")
                        workBook?.writeToSheetAppendWithTime(currentNiceBuyEntity!!.title, currentNiceBuyEntity!!.desc, currentNiceBuyEntity!!.pageView, currentNiceBuyEntity!!.collect)

                        val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        if (result) {
                            mNiceBuyTitleEntitys.removeAt(0)
                        }
                        return result
                    }
                } else {
                    // 没找到，就循环
                    mNiceBuyTitleEntitys.removeAt(0)
                    return niceBuySelect()
                }
            }
        } else {
            currentNiceBuyEntity = null
        }

        return false
    }

    /**
     * 会买专辑，下拉抓取标题
     */
    private fun niceBuyScroll(): Boolean {
        if (mNiceBuyTitleEntitys.isNotEmpty()) {
            return true
        }

        var nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/ll_zdm_inventory_header")
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) {
            //可能网络问题
            Thread.sleep(8000L)
            nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "com.jd.lib.worthbuy:id/ll_zdm_inventory_header")
        }
        if (!AccessibilityUtils.isNodesAvalibale(nodes)) return false
        val list = AccessibilityUtils.findParentByClassname(nodes!![0], "android.support.v7.widget.RecyclerView")

        if (list != null) {
            do {
                val descsNodes = list.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_zdm_inventory_desc")
                if (AccessibilityUtils.isNodesAvalibale(descsNodes)) {
                    for (descNode in descsNodes) {
                        val parent = descNode.parent
                        if (parent != null) {
                            val titles = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_zdm_inventory_title")
                            var title = AccessibilityUtils.getFirstText(titles)
                            if (title != null && title.startsWith("1 ")) {
                                title = title.replace("1 ", "");
                            }

                            val descs = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/tv_zdm_inventory_desc")
                            var desc = AccessibilityUtils.getFirstText(descs)

                            val pageViews = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/page_view")
                            var pageView = AccessibilityUtils.getFirstText(pageViews)

                            val collects = parent.findAccessibilityNodeInfosByViewId("com.jd.lib.worthbuy:id/text_collect_number")
                            var collect = AccessibilityUtils.getFirstText(collects)

                            val nice = NiceBuyEntity(title, desc, pageView, collect)
                            if (nicebuyTitles.add(title)) {
                                // 能成功加进set去，说明之前没有记录
                                mNiceBuyTitleEntitys.add(nice)
                            }
                        }
                    }
                }

                if (scrollIndex < GlobalInfo.SCROLL_COUNT && mNiceBuyTitleEntitys.isNotEmpty()) {
                    // 有新的记录，跳出循环
                    return true
                }
                scrollIndex++
                if (scrollIndex % 10 == 0) {
                    BusHandler.instance.startCountTimeout()
                }

                sleep(GlobalInfo.DEFAULT_SCROLL_SLEEP)
            } while (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    && scrollIndex < GlobalInfo.SCROLL_COUNT)
            isEnd = true
        }
        return false
    }
}