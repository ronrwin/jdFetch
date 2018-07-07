package com.example.jddata.action

import com.example.jddata.GlobalInfo
import java.util.HashMap

enum class EventType(val type: Int) {
    COMMAND(0),             // 指令自己抛出的事件，不能设置skip
    TYPE_WINDOW_STATE_CHANGED(1)        // Accessibility事件
}

enum class OutputCode(val code: Int) {
    SUCCESS(0),
    FAILED(1),
    EXCEPTION(2),
    ENDING(3),
}

data class CommandOutput(var code: OutputCode, var result: Any? = null) {
    companion object {
        var SUCCESS: CommandOutput = CommandOutput(OutputCode.SUCCESS)
        var FAILED: CommandOutput = CommandOutput(OutputCode.FAILED)
        var EXCEPTION: CommandOutput = CommandOutput(OutputCode.EXCEPTION)
        var ENDING: CommandOutput = CommandOutput(OutputCode.ENDING)
    }
}

class PureCommand(commandCode: Int) : Command(commandCode) {
    init {
        this.eventType = EventType.COMMAND
    }
}

open class Command(var commandCode: Int) {
    private var routes: HashMap<OutputCode, Command> = hashMapOf()
    var canSkip: Boolean = false
    var waitForContentChange: Boolean = false
    var obj: Any? = null
    var mScene = ArrayList<String>()         // 有可能有多个场景可执行相同的步骤
    private var states: HashMap<String, Any> = hashMapOf()
    var delay = GlobalInfo.DEFAULT_COMMAND_INTERVAL
    var concernResult = false
    var eventType = EventType.TYPE_WINDOW_STATE_CHANGED

    // 多分支链路
    fun bind(code: OutputCode, command: Command): Command {
        this.routes.put(code, command)
        return command
    }

    // 汇向一个节点
    fun join(nextCommand: Command): Command {
        val it = routes.entries.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            val command = entry.value
            command.successTo(nextCommand)
        }
        return nextCommand
    }

    // 正常流向
    fun successTo(nextCommand: Command): Command {
        this.bind(OutputCode.SUCCESS, nextCommand)
        return nextCommand
    }


    fun eventType(eventType: EventType): Command {
        this.eventType = eventType
        return this
    }

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