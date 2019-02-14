package com.example.jddata.shelldroid

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import com.example.jddata.GlobalInfo
import com.example.jddata.R
import com.example.jddata.action.Command
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.FileUtils
import kotlinx.android.synthetic.main.list_layout.*
import org.json.JSONObject
import java.io.File

class ListAppActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_layout)

        add.setOnClickListener {
            val intent = Intent(this@ListAppActivity, NewActivity::class.java)
            startActivityForResult(intent, 0)
        }

        clear.setOnClickListener {
            EnvManager.clear()
            rv.adapter = DataAdapter()
        }

        jsonFile.setOnClickListener {
            val file = File(Environment.getExternalStorageDirectory().absolutePath + "/json")
            val byteArray = FileUtils.readBytes(file)
            val ss = String(byteArray)
            val jsonArray = org.json.JSONArray(ss)
            val size = jsonArray.length()
            for (i in 0..size-1) {
                val jsonObject = jsonArray.optJSONObject(i)
                val env = Env()
                env.id = jsonObject.optInt("id").toString()
                env.envName = jsonObject.optString("name")
                env.locationName = jsonObject.optString("locationName")
                env.longitude = jsonObject.optDouble("longitude")
                env.latitude = jsonObject.optDouble("latitude")
                env.active = false
                env.createTime = ExecUtils.getCurrentTimeString()
                env.pkgName = AccService.PACKAGE_NAME
                env.appName = "京东"

                val envTemplates = ArrayList<Template>()
                val templates = jsonObject.optJSONArray("templates")
                if (templates != null) {
                    for (j in 0..templates.length() - 1) {
                        val templateJson = templates.optJSONObject(j)
                        val template = Template()
                        template.templateId = templateJson.optInt("templateId")
                        val actionsArray = templateJson.optJSONArray("actions")
                        if (actionsArray != null) {
                            for (k in 0..actionsArray.length() - 1) {
                                template.actions.addAll(makeCommands(actionsArray.optJSONObject(k)))
                            }
                        }
                        envTemplates.add(template)
                    }
                    env.templates = envTemplates
                }
                EnvManager.envDirBuild(env)
                Log.d("zfr", env.toString())
            }
            EnvManager.envs = EnvManager.scanEnvs()
            rv.adapter = DataAdapter()
        }
    }

    fun makeCommands(json : JSONObject): ArrayList<Command> {
        val commands = ArrayList<Command>()
        val action = json.optString("action")
        var delay = GlobalInfo.DEFAULT_COMMAND_INTERVAL
        if (json.has("delay")) {
            delay = json.optLong("delay")
        }
        when (action) {
            "search" -> {
                commands.add(Command().commandCode(ServiceCommand.CLICK_SEARCH).addScene(AccService.JD_HOME))
                commands.add(Command().commandCode(ServiceCommand.INPUT).addScene(AccService.SEARCH)
                        .setState(GlobalInfo.SEARCH_KEY, json.optString(GlobalInfo.SEARCH_KEY))
                        .delay(delay))
                commands.add(Command().commandCode(ServiceCommand.SEARCH))
            }
            "home" -> {
                commands.add(Command().commandCode(ServiceCommand.HOME))
                commands.add(Command().commandCode(ServiceCommand.HOME_TAB))
            }
            "home_tab" -> {
                commands.add(Command().commandCode(ServiceCommand.HOME_TAB))
            }
            "type_tab" -> {
                commands.add(Command().commandCode(ServiceCommand.TYPE_TAB))
            }
            "my_tab" -> {
                commands.add(Command().commandCode(ServiceCommand.MY_TAB))
            }
            "find_tab" -> {
                commands.add(Command().commandCode(ServiceCommand.FIND_TAB))
            }
            "cart_tab" -> {
                commands.add(Command().commandCode(ServiceCommand.CART_TAB))
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
            "home_card_item" -> {
                commands.add(Command().commandCode(ServiceCommand.HOME_CARD_ITEM)
                        .setState(GlobalInfo.HOME_CARD_NAME, json.optString(GlobalInfo.HOME_CARD_NAME)))
            }
            "cart_click" -> {
                commands.add(Command().commandCode(ServiceCommand.CART_CLICK))
            }
        }
        return commands
    }

    override fun onResume() {
        EnvManager.envs = EnvManager.scanEnvs()
        rv.adapter = DataAdapter()
        super.onResume()
    }
}
