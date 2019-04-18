package com.example.jddata.action

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Message
import android.view.accessibility.AccessibilityEvent
import com.example.jddata.BusHandler
import com.example.jddata.Entity.MessageDef
import com.example.jddata.GlobalInfo
import com.example.jddata.MainApplication
import com.example.jddata.shelldroid.Env
import com.example.jddata.util.BaseLogFile
import com.example.jddata.shelldroid.EnvManager
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.LogUtil
import java.util.ArrayList

abstract class Action(env: Env, actionType: String, map: HashMap<String, String>?) : Handler() {
    var mActionType: String? = null
    var mCommandArrayList = ArrayList<Command>()
    var mCommandsBackup = ArrayList<Command>()
    var mService: AccessibilityService? = null
    var command: Command? = null
    var mLastCommandWindow: String? = null
    var logFile: BaseLogFile? = null
    var map: HashMap<String, String>? = null
    val states: HashMap<String, Any>? = hashMapOf()
    var itemCount = 0
    var createTime = ""
    var isMoveAction = false
    var env: Env? = null
    var startTimeStamp = 0L

    init {
        this.env = env
        this.map = map
        this.mActionType = actionType
        mLastCommandWindow = null
        itemCount = 0
        this.mService = BusHandler.instance.mAccessibilityService
        this.createTime = ExecUtils.getCurrentTimeString()
        post {
            initLogFile()
            logFile?.setEnv(env)
            mCommandsBackup.addAll(mCommandArrayList)
        }
    }

    fun setEnv(env: Env): Action {
        this.env = env
        return this
    }

    fun clear() {
        mCommandArrayList.clear()
        map?.clear()
        states?.clear()
    }

    fun setState(key: String, value: Any) {
        this.states!![key] = value
    }

    fun getState(key: String): Any? {
        return this.states!![key]
    }

    fun addMoveExtra(extraStr: String) {
        logFile?.writeToFileAppend("动作： $extraStr")
        if (map == null) {
            map = HashMap()
        }

        val extra = map!!.get(GlobalInfo.EXTRA)
        if (extra is String) {
            this.map!!.put(GlobalInfo.EXTRA, "${extra},${extraStr}")
        } else {
            this.map!!.put(GlobalInfo.EXTRA, "${extraStr}")
        }
    }

    abstract fun executeInner(command: Command): Boolean
    abstract fun initLogFile()

    override fun handleMessage(msg: Message) {
        if (msg.obj == null) return
        val command: Command = msg.obj as Command
        val result = executeInner(command)
        LogUtil.logCache("CommandResult ${MainApplication.sCommandMap[command.commandCode]},  result: ${result}, concernResult: ${command.concernResult}")
        onResult(result)

        super.handleMessage(msg)
    }

    fun addAction(action: Action): Action {
        this.mCommandArrayList.addAll(action.mCommandArrayList)
        return this
    }

    fun onResult(result: Boolean) {
        if (getCurrentCommand() == null) {
            LogUtil.logCache("no command in list")
            return
        }
        if (getCurrentCommand()!!.concernResult) {
            if (result && getCurrentCommand()!!.isSceneMatch(mLastCommandWindow!!)) {
                // 当前任务完成。
                turnNextCommand()
            } else {
                BusHandler.instance.sendMsg(MessageDef.FAIL)
            }
        } else {
            turnNextCommand()
        }
    }

    fun turnNextEvent(event: AccessibilityEvent) {
        val next = getCommand(1)
        removeCurrentState()        // 把当前移走
        if (next != null) {
            if (next.canSkip) {
                LogUtil.logCache("skip: $next")
                turnNextEvent(event)
            } else {
                handleEvent(event)
            }
        } else {
            BusHandler.instance.sendMsg(MessageDef.SUCCESS)
        }
    }

    fun handleEvent(event: AccessibilityEvent) {
        val eventType = event.eventType
        val clzName = event.className.toString()
        val currentCommand = getCurrentCommand() ?: return
        val currentSceneTemp = getState(GlobalInfo.CURRENT_SCENE)

        when (eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                LogUtil.logCache("window_state_change : $clzName")
                if (currentCommand.eventType == EventType.TYPE_WINDOW_STATE_CHANGED) {
                    if (currentSceneTemp != null) {
                        val currentScene = currentSceneTemp as String
                        if (currentScene.equals(clzName)) {
                            return
                        }
                    }

                    // fixme: test
                    currentCommand.setState(GlobalInfo.CURRENT_SCENE, clzName)
                    if (currentCommand.isSceneMatch(clzName)) {
                        setState(GlobalInfo.CURRENT_SCENE, clzName)
                        doCommand(currentCommand)
                        mLastCommandWindow = clzName
                    } else {
                        if (currentCommand.canSkip) {
                            turnNextEvent(event)
                        }
                    }
                }
            }
        }
    }

    fun sleep(time: Long) {
        Thread.sleep(time)
    }

    fun doCommand(state: Command) {
        LogUtil.logCache("doCommand: ${MainApplication.sCommandMap[state.commandCode]}, scene: ${state.mScene}, delay ${state.delay}")
//        removeMessages(state.commandCode)
        val msg = Message.obtain()
        msg.what = state.commandCode
        msg.obj = state
        sendMessageDelayed(msg, state.delay)
    }

    private fun turnNextCommand() {
        val next = getCommand(1)
        removeCurrentState()        // 把当前移走
        if (next != null) {
            when (next.eventType) {
                EventType.COMMAND -> doCommand(next)
                EventType.TYPE_WINDOW_STATE_CHANGED -> {
                    val content = "next command: ${MainApplication.sCommandMap[next.commandCode]} " +
                            "----- ${env?.envName}账号, actionType : $mActionType, " +
                            "next command scene is ${next.mScene}"
                    LogUtil.logCache(content)
                    BusHandler.instance.startCountTimeout()
                }
            }
        } else {
            removeMessages(MessageDef.MSG_TIME_OUT)
            BusHandler.instance.removeMessages(MessageDef.SUCCESS)
            BusHandler.instance.sendEmptyMessage(MessageDef.SUCCESS)
        }
    }

    fun getCurrentCommand(): Command? {
        return if (mCommandArrayList.isEmpty()) {
            null
        } else mCommandArrayList[0]
    }

    private fun getCommand(index: Int): Command? {
        if (mCommandArrayList.isEmpty()) {
            return null
        }
        var target: Command? = null
        if (mCommandArrayList.size > index) {
            target = mCommandArrayList[index]
        }
        return target
    }

    fun removeCurrentState() {
        if (!mCommandArrayList.isEmpty()) {
            mCommandArrayList.removeAt(0)
        }
    }

    fun appendCommand(command: Command): ArrayList<Command> {
        mCommandArrayList.add(command)
        return mCommandArrayList
    }

    fun appendCommands(commands: ArrayList<Command>): ArrayList<Command> {
        mCommandArrayList.addAll(commands)
        return mCommandArrayList
    }
}


fun ArrayList<Command>.append(command: Command): ArrayList<Command> {
    this.add(command)
    return this
}

fun ArrayList<Command>.appendAll(commands: ArrayList<Command>): ArrayList<Command> {
    this.addAll(commands)
    return this
}
