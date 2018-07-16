package com.example.jddata.action

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.BusHandler
import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.BrandEntity
import com.example.jddata.GlobalInfo
import com.example.jddata.excel.BaseWorkBook
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.CommonConmmand
import com.example.jddata.util.ExecUtils
import java.util.ArrayList

class MoveBrandKillClickAction : BaseAction(ActionType.MOVE_BRAND_KILL_CLICK) {

    var mBrandEntitys = ArrayList<BrandEntity>()
    var brandTitleStrings = HashSet<String>()
    var scrollIndex = 0
    var isEnd = false

    init {
        appendCommand(Command(ServiceCommand.HOME_BRAND_KILL).addScene(AccService.JD_HOME))
                .append(Command(ServiceCommand.HOME_BRAND_KILL_SCROLL)
                        .addScene(AccService.MIAOSHA)
                        .delay(5000L)
                        .concernResult(true))
    }

    override fun initWorkbook() {
        workBook = BaseWorkBook("动作_品牌秒杀并点击商品")
    }

    override fun executeInner(command: Command): Boolean {
        when(command.commandCode) {
            ServiceCommand.HOME_BRAND_KILL -> {
                workBook?.writeToSheetAppendWithTime("找到并点击 \"${GlobalInfo.BRAND_KILL}\"")
                return CommonConmmand.findHomeTextClick(mService!!, GlobalInfo.BRAND_KILL)
            }
            ServiceCommand.HOME_BRAND_KILL_SCROLL -> {
                val result = brandKillFetchBrand()
                return result
            }
            ServiceCommand.BRAND_SELECT -> {
                val result = brandSelect()
                return result
            }
        }
        return super.executeInner(command)
    }

    var currentBrandEntity: BrandEntity? = null
    private fun brandSelect(): Boolean {
        if (mBrandEntitys.isNotEmpty()) {
            var nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(mService, "android:id/list")

            if (AccessibilityUtils.isNodesAvalibale(nodes)) {
                val list = nodes[0]
                if (list != null) {
                    currentBrandEntity = mBrandEntitys.get(0)
                    var title = currentBrandEntity!!.title.toString()
                    val selectNodes = list.findAccessibilityNodeInfosByText(title)
                    if (AccessibilityUtils.isNodesAvalibale(selectNodes)) {
                        val parent = AccessibilityUtils.findParentClickable(selectNodes[0])
                        if (parent != null) {
                            workBook?.writeToSheetAppend("")
                            workBook?.writeToSheetAppendWithTime("找到并点击 $title")
                            return true
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