package com.example.jddata.action.unknown

import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.Entity.ActionType
import com.example.jddata.GlobalInfo
import com.example.jddata.action.BaseAction
import com.example.jddata.action.Command
import com.example.jddata.action.PureCommand
import com.example.jddata.action.append
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.shelldroid.EnvManager
import com.example.jddata.shelldroid.Template
import com.example.jddata.util.AccessibilityUtils
import com.example.jddata.util.BaseLogFile
import com.example.jddata.util.ExecUtils
import org.json.JSONObject
import java.text.SimpleDateFormat

class MoveAction : BaseAction(ActionType.TEMPLATE_MOVE) {
    init {
        val env = EnvManager.sCurrentEnv
        if (env != null && env.templates != null) {
            val size = env.templates!!.length()
            for (i in 0..size-1) {
                val templateJson = env.templates!!.optJSONObject(i)
                val template = Template()
                template.id = templateJson.optInt("id")
                val actionsArray = templateJson.optJSONArray("actions")
                for (j in 0..actionsArray.length()-1) {
                    template.actions.addAll(makeCommands(actionsArray.optJSONObject(j)))
                }
            }
        }
    }
    var name = ""

    fun makeCommands(json : JSONObject): ArrayList<Command> {
        val commands = ArrayList<Command>()
        val action = json.optString("action")
        var delay = GlobalInfo.DEFAULT_COMMAND_INTERVAL
        if (json.has("delay")) {
            delay = json.optLong("delay")
        }
        when (action) {
            "search" -> {
                commands.add(Command(ServiceCommand.MOVE_SEARCH)
                        .setState(GlobalInfo.SEARCH_KEY, json.optString("keyword"))
                        .delay(delay))
            }
            "home" -> {
                commands.add(Command(ServiceCommand.HOME))
            }
        }
        return commands
    }

    override fun initLogFile() {
        name = "${EnvManager.sCurrentEnv.envName}_${ExecUtils.getCurrentTimeString(SimpleDateFormat("HH_mm"))}"
        logFile = BaseLogFile("动作_${name}")
    }

    override fun executeInner(command: Command): Boolean {
        when (command.commandCode) {
            ServiceCommand.MOVE_SEARCH -> {

            }
            ServiceCommand.GRID_ITEM -> {
                logFile?.writeToFileAppend("点击$name")
                val items = AccessibilityUtils.findAccessibilityNodeInfosByText(mService, "$name")
                if (AccessibilityUtils.isNodesAvalibale(items)) {
                    val clickParent = AccessibilityUtils.findParentClickable(items[0])
                    if (clickParent != null) {
                        val result = clickParent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        if (result) {
                            addMoveExtra("点击$name")
                        }
                        return result
                    }
                }

                return false
            }
        }
        return super.executeInner(command)
    }

}