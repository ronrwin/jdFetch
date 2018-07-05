package com.example.jddata.service

open class BaseAction(var actionType: String) {
    var machine: ActionMachine

    init {
        this.machine = ActionMachine(actionType)
    }
}

class MachineState(var scene: String, var concernResult: Boolean      // 如果事件失败，则任务中断失败
                   , var delay: Long, var commandCode: Int) {
    var canSkip: Boolean = false
    var waitForContentChange: Boolean = false
    var obj: Any? = null
    var extraScene: Array<String>? = null         // 有可能有多个场景可执行相同的步骤

    constructor(scene: String, commandCodes: Int) : this(scene, false, commandCodes) {}

    constructor(scene: String, concernResult: Boolean, commandCodes: Int) : this(scene, concernResult, AccessibilityCommandHandler.DEFAULT_COMMAND_INTERVAL, commandCodes) {}

    private fun hasExtraSceneMatch(scene: String): Boolean {
        if (extraScene == null) return false
        for (s in extraScene!!) {
            if (s == scene) {
                return true
            }
        }
        return false
    }

    fun isSceneMatch(scene: String): Boolean {
        return this.scene == scene || hasExtraSceneMatch(scene)
    }
}
