package com.example.jddata.action

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Message
import android.view.accessibility.AccessibilityEvent
import com.example.jddata.BusHandler
import com.example.jddata.Entity.MessageDef
import com.example.jddata.excel.BaseWorkBook
import com.example.jddata.util.LogUtil
import java.util.ArrayList

open class Action(actionType: String): Handler() {
    open var mActionType: String? = null
    open var mCommandArrayList = ArrayList<Command>()
    var mService : AccessibilityService? = null
    var command: Command? = null
    var mLastCommandWindow: String? = null
    var workBook: BaseWorkBook? = null
    var log = StringBuilder()
    var hasInitWorkbook = false

    init {
        this.mActionType = actionType
        this.mService = BusHandler.instance.mAccessibilityService
        post(Runnable {
            initWorkbook()
        })
    }

    open fun initWorkbook() {

    }

    override fun handleMessage(msg: Message) {
        if (msg.obj == null) return
        val command: Command = msg.obj as Command
        val result = executeInner(command)
        LogUtil.writeLog("Command ${command.commandCode},  result: $result")
        onResult(result)

        super.handleMessage(msg)
    }

    open fun executeInner(command: Command): Boolean {
        return false
    }

    fun addAction(action: Action):Action {
        this.mCommandArrayList.addAll(action.mCommandArrayList)
        return this
    }

    fun onResult(result: Boolean) {
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
        val next = getState(1)
        removeCurrentState()        // 把当前移走
        if (next != null) {
            if (next.canSkip) {
                turnNextEvent(event)
            } else {
                handleEvent(event)
            }
        } else {
            BusHandler.instance.sendMsg(MessageDef.SUCCESS)
        }
    }

    fun handleEvent(event : AccessibilityEvent) {
        val eventType = event.eventType
        val clzName = event.className.toString()
        val currentCommand = getCurrentCommand() ?: return

        when (eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                LogUtil.writeLog("window_state_change : $clzName")
                if (currentCommand.eventType == EventType.TYPE_WINDOW_STATE_CHANGED) {
                    if (currentCommand.isSceneMatch(clzName)) {
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

    fun sleep(time : Long) {
        Thread.sleep(time)
    }

    fun doCommand(state: Command) {
        LogUtil.writeLog("doCommand: ${state.commandCode}, scene: ${state.mScene}, delay ${state.delay}")
        removeMessages(state.commandCode)
        val msg = Message.obtain()
        msg.what = state.commandCode
        msg.obj = state
        sendMessageDelayed(msg, state.delay)

        BusHandler.instance.startCountTimeout()
    }

    private fun turnNextCommand() {
        val next = getState(1)
        removeCurrentState()        // 把当前移走
        if (next != null) {
            when (next.eventType) {
                EventType.COMMAND -> doCommand(next)
            }
        } else {
            removeMessages(MessageDef.MSG_TIME_OUT)
            BusHandler.instance.removeMessages(MessageDef.SUCCESS)
            BusHandler.instance.sendEmptyMessage(MessageDef.SUCCESS)
        }
    }

    fun getCurrentCommand() : Command? {
        return if (mCommandArrayList.isEmpty()) {
            null
        } else mCommandArrayList[0]
    }

    fun getState(index: Int): Command? {
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


fun ArrayList<Command>.append(command: Command) : ArrayList<Command> {
    this.add(command)
    return this
}

fun ArrayList<Command>.appendAll(commands: ArrayList<Command>) : ArrayList<Command> {
    this.addAll(commands)
    return this
}
