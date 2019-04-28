package com.example.jddata.action

import com.example.jddata.GlobalInfo
import java.io.Serializable
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

open class Command : Serializable {
    private val serialVersionUID = 1L

    var commandCode: Int = 0
    var canSkip: Boolean = false
    var mScene = ArrayList<String>()         // 有可能有多个场景可执行相同的步骤
    var delay = GlobalInfo.DEFAULT_COMMAND_INTERVAL
    var concernResult = false
    var eventType = EventType.COMMAND
    var commandStates = HashMap<String, String>()

    fun commandCode(code: Int): Command {
        commandCode = code
        return this
    }

    fun setState(key: String, state: String): Command {
        commandStates.put(key, state)
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
        eventType = EventType.TYPE_WINDOW_STATE_CHANGED
        this.mScene.add(extraScene)
        return this
    }

    fun delay(delay: Long): Command {
        this.delay = delay
        return this
    }

    fun concernResult(flag: Boolean): Command {
        this.concernResult = flag
        return this
    }

}