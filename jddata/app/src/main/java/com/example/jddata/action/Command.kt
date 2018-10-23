package com.example.jddata.action

import com.example.jddata.GlobalInfo
import com.example.jddata.service.ServiceCommand
import java.util.HashMap

enum class EventType(val type: Int) {
    COMMAND(0),             // 指令自己抛出的事件，不能设置skip
    TYPE_WINDOW_STATE_CHANGED(1)        // Accessibility事件
}

class PureCommand(commandCode: ServiceCommand) : Command(commandCode) {
    init {
        this.eventType = EventType.COMMAND
    }
}

open class Command(var commandCode: ServiceCommand) {
    var canSkip: Boolean = false
    var waitForContentChange: Boolean = false
    var obj: Any? = null
    var mScene = ArrayList<String>()         // 有可能有多个场景可执行相同的步骤
    private var states: HashMap<String, Any> = hashMapOf()
    var delay = GlobalInfo.DEFAULT_COMMAND_INTERVAL
    var concernResult = false
    var eventType = EventType.TYPE_WINDOW_STATE_CHANGED

    fun isSceneMatch(scene: String): Boolean {
        return mScene.contains(scene)
    }

    /**
     * 如果eventType 为 EventType.Command, 则忽略canSkip参数值
     */
    fun canSkip(skip: Boolean): Command {
        this.canSkip = skip
        return this
    }

    fun addScene(extraScene: String): Command {
        this.mScene.add(extraScene)
        return this
    }

    fun delay(delay: Long): Command {
        this.delay = delay
        return this
    }

    fun waitForContentChange(flag: Boolean): Command {
        this.waitForContentChange = flag
        return this
    }

    fun concernResult(flag: Boolean): Command {
        this.concernResult = flag
        return this
    }

    fun setState(key: String, value: Any): Command {
        this.states[key] = value
        return this
    }

    fun getState(key: String): Any? {
        return this.states[key]
    }

    fun getStates(): HashMap<String, Any> {
        return this.states
    }
}