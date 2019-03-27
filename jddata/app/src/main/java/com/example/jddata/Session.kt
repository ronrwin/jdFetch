package com.example.jddata

import android.os.Environment
import android.util.Log
import com.example.jddata.action.Command
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.shelldroid.DataAdapter
import com.example.jddata.util.FileUtils
import com.example.jddata.util.JdUtils
import kotlinx.android.synthetic.main.list_layout.*
import org.json.JSONObject
import java.io.File
import kotlin.collections.ArrayList

class Session {
    companion object {
        @JvmField val sTemplates = ArrayList<Template>()

        @JvmStatic fun initTemplates() {
            val file = File(Environment.getExternalStorageDirectory().absolutePath + "/sessions.json")
            val byteArray = FileUtils.readBytes(file)
            val ss = String(byteArray)
            val jsonArray = org.json.JSONArray(ss)
            val size = jsonArray.length()
            for (i in 0 until size) {
                val templateJson = jsonArray.optJSONObject(i)
                val template = Template()
                template.templateId = templateJson.optInt("templateId")
                val actions = templateJson.optJSONArray("actions")
                val actionSize = actions.length()
                for (j in 0 until actionSize) {
                    template.actions.addAll(makeCommands(actions.optJSONObject(j)))
                }
                sTemplates.add(template)
            }
            Log.d("zfr", sTemplates.toString())
        }


        @JvmStatic fun makeCommands(json : JSONObject): ArrayList<Command> {
            val commands = ArrayList<Command>()
            val action = json.optString("action")
            var delay = GlobalInfo.DEFAULT_COMMAND_INTERVAL
            if (json.has("delay")) {
                delay = json.optLong("delay")
            }
            if (json.has("pic_no")) {
                val picNo = json.optInt("pic_no")
                when (picNo) {
                    1 -> {
                        JdUtils.copyPic("haifeisi.png")
                    }
                }
            }
            when (action) {
                "search" -> {
                    commands.add(Command().commandCode(ServiceCommand.HOME))
                    commands.add(Command().commandCode(ServiceCommand.HOME_TAB).addScene(AccService.JD_HOME).canSkip(true))
                    commands.add(Command().commandCode(ServiceCommand.CLICK_SEARCH))
                    commands.add(Command().commandCode(ServiceCommand.TEMPLATE_INPUT).addScene(AccService.SEARCH)
                            .delay(delay))
                    commands.add(Command().commandCode(ServiceCommand.SEARCH))
                }
                "home_tab" -> {
                    commands.add(Command().commandCode(ServiceCommand.HOME))
                    commands.add(Command().commandCode(ServiceCommand.HOME_TAB).addScene(AccService.JD_HOME).canSkip(true))
                }
                "type_tab" -> {
                    commands.add(Command().commandCode(ServiceCommand.HOME))
                    commands.add(Command().commandCode(ServiceCommand.TYPE_TAB).addScene(AccService.JD_HOME).canSkip(true))
                }
                "my_tab" -> {
                    commands.add(Command().commandCode(ServiceCommand.HOME))
                    commands.add(Command().commandCode(ServiceCommand.MY_TAB).addScene(AccService.JD_HOME).canSkip(true))
                }
                "find_tab" -> {
                    commands.add(Command().commandCode(ServiceCommand.HOME))
                    commands.add(Command().commandCode(ServiceCommand.FIND_TAB).addScene(AccService.JD_HOME).canSkip(true))
                }
                "cart_tab" -> {
                    commands.add(Command().commandCode(ServiceCommand.HOME))
                    commands.add(Command().commandCode(ServiceCommand.CART_TAB).addScene(AccService.JD_HOME).canSkip(true))
                }
                "home_grid_item" -> {
                    commands.add(Command().commandCode(ServiceCommand.HOME_GRID_ITEM)
                            .setState(GlobalInfo.HOME_GRID_NAME, json.optString(GlobalInfo.HOME_GRID_NAME)))
                }
                "home_dmp" -> {
                    commands.add(Command().commandCode(ServiceCommand.HOME_DMP))
                }
                "back" -> {
                    commands.add(Command().commandCode(ServiceCommand.GO_BACK))
                }
                "home_card_item" -> {   // 首页导航项
                    commands.add(Command().commandCode(ServiceCommand.HOME_CARD_ITEM)
                            .setState(GlobalInfo.HOME_CARD_NAME, json.optString(GlobalInfo.HOME_CARD_NAME)))
                }
                "cart_click" -> {   // 搜索结果页，点击购物车按钮
                    commands.add(Command().commandCode(ServiceCommand.CART_CLICK))
                }
                "search_and_select" -> {    // 搜索+商品详情页
                    commands.add(Command().commandCode(ServiceCommand.HOME))
                    commands.add(Command().commandCode(ServiceCommand.HOME_TAB).addScene(AccService.JD_HOME).canSkip(true))
                    commands.add(Command().commandCode(ServiceCommand.CLICK_SEARCH).delay(2000))
                    commands.add(Command().commandCode(ServiceCommand.TEMPLATE_INPUT).addScene(AccService.SEARCH)
                            .delay(delay))
                    commands.add(Command().commandCode(ServiceCommand.SEARCH).delay(2000))
                    commands.add(Command().commandCode(ServiceCommand.SEARCH_SELECT)
                            .addScene(AccService.PRODUCT_LIST).delay(3000))

                    if (json.has("select_more")) {
                        val selectCount = json.optInt("select_more")
                        if (selectCount > 0) {
                            for (i in 0 until selectCount) {
                                commands.add(Command().commandCode(ServiceCommand.GO_BACK))
                                commands.add(Command().commandCode(ServiceCommand.SEARCH_SELECT)
                                        .addScene(AccService.PRODUCT_LIST)
                                        // 有这个参数，则操作列表向下滚动一次
                                        .setState(GlobalInfo.SEARCH_RESULT_SCROLL, ""+i))
                            }
                        }
                    }
                }
                "dmp_activity" -> { // 临时活动
                    commands.add(Command().commandCode(ServiceCommand.HOME))
                    commands.add(Command().commandCode(ServiceCommand.HOME_TAB).addScene(AccService.JD_HOME).canSkip(true))
                    commands.add(Command().commandCode(ServiceCommand.QR_CODE))
                    commands.add(Command().commandCode(ServiceCommand.SCAN_ALBUM)
                            .addScene(AccService.CAPTURE_SCAN).delay(2000))
                    commands.add(Command().commandCode(ServiceCommand.SCAN_PIC).addScene(AccService.PHOTO_ALBUM))
                    commands.add(Command().commandCode(ServiceCommand.HOME).delay(5000))
                }
                "add_to_cart" -> {  // 加入购物车
                    commands.add(Command().commandCode(ServiceCommand.TEMPLATE_ADD_TO_CART)
                            .addScene(AccService.PRODUCT_DETAIL).delay(3000))
                    commands.add(Command().commandCode(ServiceCommand.PRODUCT_CONFIRM)
                            .delay(2000).canSkip(true).concernResult(false))
                }
                "settle" -> {   // 结算
                    commands.add(Command().commandCode(ServiceCommand.SETTLE).delay(3000))
                }
                "home_select" -> {   // 首页推荐随便点
                    commands.add(Command().commandCode(ServiceCommand.TEMPLATE_HOME_SELECT))
                }
                "close" -> {
                    commands.add(Command().commandCode(ServiceCommand.BACK_JD_HOME))
                }
            }
            return commands
        }
    }
}