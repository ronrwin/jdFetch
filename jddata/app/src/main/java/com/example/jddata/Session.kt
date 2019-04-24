package com.example.jddata

import android.util.Log
import android.util.SparseArray
import com.example.jddata.action.Command
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.FileUtils
import com.example.jddata.util.LogUtil
import org.json.JSONObject
import java.lang.Exception
import kotlin.collections.ArrayList

class Session {
    companion object {
        @JvmField val sTemplates = SparseArray<Template>()

        @JvmStatic fun initTemplates() {
            val start = System.currentTimeMillis();
            val ss = FileUtils.readFromAssets(MainApplication.sContext, "sessions.json")
            val jsonArray = org.json.JSONArray(ss)
            val size = jsonArray.length()
            for (i in 0 until size) {
                val templateJson = jsonArray.optJSONObject(i)
                val template = Template()
                template.templateId = templateJson.optInt("templateId")
                val actions = templateJson.optJSONArray("actions")
                val actionSize = actions.length()
                if (actionSize > 0) {
                    for (j in 0 until actionSize) {
                        try {
                            template.actions.addAll(makeCommands(actions.getJSONObject(j)))
                        } catch (e: Exception) {
                            Log.e("zfr", "id: ${template.templateId}, ${j} not right")
                        }
                    }
                }
                sTemplates.put(template.templateId, template)
            }
            Log.d("zfr", "initTemplate done. cost: ${(System.currentTimeMillis() - start)} ms")
            Log.d("zfr", "initTemplate done. ==== ${sTemplates}")
        }


        @JvmStatic fun makeCommands(json : JSONObject): ArrayList<Command> {
            val commands = ArrayList<Command>()
            val action = json.optString("action")
            val delay = 3000L
            var flag = false
            when (action) {
                "home_tab" -> {
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.HOME))
                    commands.add(Command().commandCode(ServiceCommand.HOME_TAB).delay(3000))
                }
                "type_tab" -> {
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.HOME))
                    commands.add(Command().commandCode(ServiceCommand.TYPE_TAB).delay(3000)
                            .addScene(AccService.JD_HOME).canSkip(true))
                }
                "my_tab" -> {
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.HOME))
                    commands.add(Command().commandCode(ServiceCommand.MY_TAB).delay(3000)
                            .addScene(AccService.JD_HOME).canSkip(true))
                }
                "find_tab" -> {
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.HOME))
                    commands.add(Command().commandCode(ServiceCommand.FIND_TAB).delay(3000)
                            .addScene(AccService.JD_HOME).canSkip(true))
                }
                "cart_tab" -> {
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.HOME))
                    commands.add(Command().commandCode(ServiceCommand.CART_TAB).delay(3000)
                            .addScene(AccService.JD_HOME).canSkip(true))
                }
                "home_grid_item" -> {   // 首页导航项
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.HOME))
                    commands.add(Command().commandCode(ServiceCommand.HOME_TAB).delay(3000))
                    commands.add(Command().commandCode(ServiceCommand.HOME_TOP).delay(3000))
                    commands.add(Command().commandCode(ServiceCommand.HOME_GRID_ITEM).delay(2000)
                            .setState(GlobalInfo.HOME_GRID_NAME, json.optString(GlobalInfo.HOME_GRID_NAME)))
                }
                "dmp" -> {
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.HOME))
                    commands.add(Command().commandCode(ServiceCommand.HOME_TAB).delay(3000))
                    commands.add(Command().commandCode(ServiceCommand.HOME_TOP).delay(3000))
                    commands.add(Command().commandCode(ServiceCommand.HOME_DMP).delay(3000))
                    commands.add(Command().commandCode(ServiceCommand.DMP_TITLE).delay(3000))
                }
                "back" -> {
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.GO_BACK))
                }
                "home_top" -> {
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.HOME_TOP))
                }
                "shop_car" -> {
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.SHOP_CAR).delay(3000))
                    commands.add(Command().commandCode(ServiceCommand.DONE).delay(2000)
                            .addScene(AccService.SHOPPING_CART))
                }
                "home_card_item" -> {   // 首页卡片项
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.HOME))
                    commands.add(Command().commandCode(ServiceCommand.HOME_TAB).delay(3000))
                    commands.add(Command().commandCode(ServiceCommand.HOME_TOP).delay(3000))
                    commands.add(Command().commandCode(ServiceCommand.HOME_CARD_ITEM)
                            .setState(GlobalInfo.HOME_CARD_NAME, json.optString(GlobalInfo.HOME_CARD_NAME)))
                }
                "cart_click" -> {   // 搜索结果页，点击购物车按钮
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.CART_CLICK))
                }
                "search" -> {
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.CLICK_SEARCH).delay(4000))
                    commands.add(Command().commandCode(ServiceCommand.TEMPLATE_INPUT).addScene(AccService.SEARCH)
                            .delay(delay))
                    commands.add(Command().commandCode(ServiceCommand.SEARCH))
                    commands.add(Command().commandCode(ServiceCommand.DONE).delay(2000)
                            .addScene(AccService.PRODUCT_LIST))
                }
                "home_search" -> {
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.HOME))
                    commands.add(Command().commandCode(ServiceCommand.HOME_TAB).delay(2000))
                    commands.add(Command().commandCode(ServiceCommand.CLICK_SEARCH).delay(2000))
                    commands.add(Command().commandCode(ServiceCommand.TEMPLATE_INPUT).addScene(AccService.SEARCH)
                            .delay(delay))
                    commands.add(Command().commandCode(ServiceCommand.SEARCH))
                    commands.add(Command().commandCode(ServiceCommand.DONE).delay(2000)
                            .addScene(AccService.PRODUCT_LIST))
                }
                "home_search_select" -> {
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.HOME))
                    commands.add(Command().commandCode(ServiceCommand.HOME_TAB).delay(2000))
                    commands.add(Command().commandCode(ServiceCommand.CLICK_SEARCH).delay(2000))
                    commands.add(Command().commandCode(ServiceCommand.TEMPLATE_INPUT).addScene(AccService.SEARCH)
                            .delay(delay))
                    commands.add(Command().commandCode(ServiceCommand.SEARCH))
                    commands.add(Command().commandCode(ServiceCommand.SEARCH_SELECT).addScene(AccService.PRODUCT_LIST)
                            .delay(4000))
                    commands.add(Command().commandCode(ServiceCommand.DONE).delay(2000)
                            .addScene(AccService.PRODUCT_DETAIL))
                }
                "search_in_result" -> {
                    flag = true
                    var times = 1
                    if (json.has("repeat")) {
                        times = json.optInt("repeat")
                    }
                    for (i in 0 until times) {
                        commands.add(Command().commandCode(ServiceCommand.SEARCH_IN_RESULT))
                        commands.add(Command().commandCode(ServiceCommand.TEMPLATE_INPUT)
                                .addScene(AccService.SEARCH).delay(delay))
                        commands.add(Command().commandCode(ServiceCommand.SEARCH))
                        commands.add(Command().commandCode(ServiceCommand.DONE).delay(2000)
                                .addScene(AccService.PRODUCT_LIST))
                    }
                }
                "back_search" -> {
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.GO_BACK).delay(2000))
                    commands.add(Command().commandCode(ServiceCommand.SEARCH_IN_RESULT)
                            .addScene(AccService.PRODUCT_LIST))
                    commands.add(Command().commandCode(ServiceCommand.TEMPLATE_INPUT)
                            .addScene(AccService.SEARCH).delay(delay))
                    commands.add(Command().commandCode(ServiceCommand.SEARCH))
                    commands.add(Command().commandCode(ServiceCommand.DONE).delay(2000)
                            .addScene(AccService.PRODUCT_LIST))
                }
                "back_select_in_result" -> {
                    flag = true
                    var times = 1
                    if (json.has("repeat")) {
                        times = json.optInt("repeat")
                    }
                    for (i in 0 until times) {
                        commands.add(Command().commandCode(ServiceCommand.GO_BACK).delay(2000))
                        commands.add(Command().commandCode(ServiceCommand.SEARCH_SELECT)
                                .addScene(AccService.PRODUCT_LIST).delay(4000)
                                // 有这个参数，则操作列表向下滚动一次
                                .setState(GlobalInfo.SEARCH_RESULT_SCROLL, "1"))
                        commands.add(Command().commandCode(ServiceCommand.DONE).delay(2000)
                                .addScene(AccService.PRODUCT_DETAIL))
                    }
                }
                "select_in_result" -> {    // 商品详情页
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.SEARCH_SELECT)
                            .delay(4000))
                    commands.add(Command().commandCode(ServiceCommand.DONE).delay(2000)
                            .addScene(AccService.PRODUCT_DETAIL))
                }
                "back_search_select" -> {
                    flag = true
                    var times = 1
                    if (json.has("repeat")) {
                        times = json.optInt("repeat")
                    }
                    for (i in 0 until times) {
                        commands.add(Command().commandCode(ServiceCommand.GO_BACK).delay(2000))
                        commands.add(Command().commandCode(ServiceCommand.SEARCH_IN_RESULT)
                                .addScene(AccService.PRODUCT_LIST).delay(2000))
                        commands.add(Command().commandCode(ServiceCommand.TEMPLATE_INPUT)
                                .addScene(AccService.SEARCH).delay(delay))
                        commands.add(Command().commandCode(ServiceCommand.SEARCH))
                        commands.add(Command().commandCode(ServiceCommand.SEARCH_SELECT)
                                .addScene(AccService.PRODUCT_LIST).delay(4000))
                        commands.add(Command().commandCode(ServiceCommand.DONE).delay(2000)
                                .addScene(AccService.PRODUCT_DETAIL))
                    }
                }
                "qrcode" -> { // 临时活动
                    flag = true
                    var times = 1
                    if (json.has("repeat")) {
                        times = json.optInt("repeat")
                    }

                    for (i in 0 until times) {
                        commands.add(Command().commandCode(ServiceCommand.HOME))
                        commands.add(Command().commandCode(ServiceCommand.HOME_TAB).delay(3000))
                        commands.add(Command().commandCode(ServiceCommand.HOME_TOP).delay(3000))
                        commands.add(Command().commandCode(ServiceCommand.QR_CODE)
                                .setState(GlobalInfo.QRCODE_PIC, "random"))
                        commands.add(Command().commandCode(ServiceCommand.SCAN_ALBUM)
                                .addScene(AccService.CAPTURE_SCAN).delay(2000))
                        commands.add(Command().commandCode(ServiceCommand.SCAN_PIC).addScene(AccService.PHOTO_ALBUM))
                        commands.add(Command().commandCode(ServiceCommand.DONE).delay(12000))
                    }

                }
                "add_to_cart" -> {  // 加入购物车
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.TEMPLATE_ADD_TO_CART).delay(3000))
                    commands.add(Command().commandCode(ServiceCommand.PRODUCT_CONFIRM)
                            .delay(3000).canSkip(true))
                }
                "settle" -> {   // 结算
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.SETTLE).delay(3000))
                    commands.add(Command().commandCode(ServiceCommand.DONE).delay(4000))
                }
                "home_select" -> {   // 首页推荐随便点商品
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.HOME))
                    commands.add(Command().commandCode(ServiceCommand.HOME_TAB).delay(3000))
                    commands.add(Command().commandCode(ServiceCommand.TEMPLATE_HOME_SELECT).delay(2000))
                    commands.add(Command().commandCode(ServiceCommand.DONE).delay(2000)
                            .addScene(AccService.PRODUCT_DETAIL))
                }
                "back_select_in_home" -> {
                    flag = true
                    var times = 1
                    if (json.has("repeat")) {
                        times = json.optInt("repeat")
                    }

                    for (i in 0 until times) {
                        commands.add(Command().commandCode(ServiceCommand.GO_BACK).delay(2000))
                        commands.add(Command().commandCode(ServiceCommand.TEMPLATE_HOME_SELECT)
                                .addScene(AccService.JD_HOME)
                                // 有这个参数，则操作列表向下滚动一次
                                .setState(GlobalInfo.SEARCH_RESULT_SCROLL, "1"))
                        commands.add(Command().commandCode(ServiceCommand.DONE).delay(2000)
                                .addScene(AccService.PRODUCT_DETAIL))
                    }
                }
                "cart_select" -> {  // 购物车推荐随便点商品
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.TEMPLATE_CART_SELECT).delay(2000))
                    commands.add(Command().commandCode(ServiceCommand.DONE).delay(2000).addScene(AccService.PRODUCT_DETAIL))
                }
                "close" -> {
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.DESKTOP))
                }
                "type_kill_select" -> {
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.TEMPLATE_TYPE_SELECT).delay(3000))
                    commands.add(Command().commandCode(ServiceCommand.DONE).delay(2000).addScene(AccService.PRODUCT_DETAIL))
                }
                "jd_kill" -> {
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.HOME))
                    commands.add(Command().commandCode(ServiceCommand.HOME_TAB).delay(3000))
                    commands.add(Command().commandCode(ServiceCommand.HOME_TOP).delay(3000))
                    commands.add(Command().commandCode(ServiceCommand.TEMPLATE_JDKILL).delay(2000))
                    commands.add(Command().commandCode(ServiceCommand.DONE).delay(2000).addScene(AccService.MIAOSHA))
                }
                "jd_kill_select" -> {
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.TEMPLATE_JDKILL_SELECT).delay(3000))
                    commands.add(Command().commandCode(ServiceCommand.DONE).delay(2000).addScene(AccService.PRODUCT_DETAIL))
                }
                "worth_buy" -> {
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.HOME))
                    commands.add(Command().commandCode(ServiceCommand.HOME_TAB).delay(3000))
                    commands.add(Command().commandCode(ServiceCommand.HOME_TOP).delay(3000))
                    commands.add(Command().commandCode(ServiceCommand.HOME_CARD_ITEM)
                            .setState(GlobalInfo.HOME_CARD_NAME, "发现好货"))
                    commands.add(Command().commandCode(ServiceCommand.DONE).delay(2000).addScene(AccService.WORTHBUY))
                }
                "nice_buy" -> {
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.HOME))
                    commands.add(Command().commandCode(ServiceCommand.HOME_TAB).delay(3000))
                    commands.add(Command().commandCode(ServiceCommand.HOME_TOP).delay(3000))
                    commands.add(Command().commandCode(ServiceCommand.HOME_CARD_ITEM)
                            .setState(GlobalInfo.HOME_CARD_NAME, "会买专辑"))
                    commands.add(Command().commandCode(ServiceCommand.DONE).delay(2000).addScene(AccService.WORTHBUY))
                }
                "brand_kill" -> {
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.HOME))
                    commands.add(Command().commandCode(ServiceCommand.HOME_TAB).delay(3000))
                    commands.add(Command().commandCode(ServiceCommand.HOME_TOP).delay(3000))
                    commands.add(Command().commandCode(ServiceCommand.HOME_CARD_ITEM)
                            .setState(GlobalInfo.HOME_CARD_NAME, "品牌秒杀"))
                    commands.add(Command().commandCode(ServiceCommand.DONE).delay(2000).addScene(AccService.MIAOSHA))
                }
                "type_kill" -> {
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.HOME))
                    commands.add(Command().commandCode(ServiceCommand.HOME_TAB).delay(3000))
                    commands.add(Command().commandCode(ServiceCommand.HOME_TOP).delay(3000))
                    commands.add(Command().commandCode(ServiceCommand.HOME_CARD_ITEM)
                            .setState(GlobalInfo.HOME_CARD_NAME, "品类秒杀"))
                    commands.add(Command().commandCode(ServiceCommand.DONE).delay(2000).addScene(AccService.MIAOSHA))
                }
                "worth_buy_select" -> {
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.TEMPLATE_WORTHBUY_SELECT).delay(3000))
                    commands.add(Command().commandCode(ServiceCommand.DONE).delay(2000).addScene(AccService.WORTH_DETAIL))
                }
                "my_select" -> {
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.TEMPLATE_MY_SELECT).delay(3000))
                    commands.add(Command().commandCode(ServiceCommand.DONE).delay(2000).addScene(AccService.PRODUCT_DETAIL))
                }
                "miaosha_tab" -> {
                    flag = true
                    if (json.has("tab_name")) {
                        val tabName = json.optString("tab_name")
                        commands.add(Command().commandCode(ServiceCommand.MIAOSHA_TAB).delay(2000)
                                .setState(GlobalInfo.MIAOSHA_TAB, tabName))
                    }
                }
                "brand_kill_select" -> {
                    flag = true
                    commands.add(Command().commandCode(ServiceCommand.TEMPLATE_BRAND_SELECT)
                            .addScene(AccService.MIAOSHA).delay(2000))
                    commands.add(Command().commandCode(ServiceCommand.DONE).delay(2000).addScene(AccService.PRODUCT_DETAIL))
                }
            }

            if (!flag) {
                LogUtil.logCache("没有找到 指令：${action}")
            }
            return commands
        }
    }
}