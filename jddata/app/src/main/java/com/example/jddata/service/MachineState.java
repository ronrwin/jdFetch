package com.example.jddata.service;

public class MachineState {
    public String scene;
    public int commandCode;
    public long delay;
    public boolean canSkip;
    public boolean concernResult;      // 如果事件失败，则任务中断失败
    public boolean waitForContentChange;
    public Object obj;
    public String[] extraScene;         // 有可能有多个场景可执行相同的步骤

    public MachineState(String scene, Integer commandCodes) {
        this(scene, false, commandCodes);
    }

    public MachineState(String scene, boolean concernResult, Integer commandCodes) {
        this(scene, concernResult, AccessibilityCommandHandler.DEFAULT_COMMAND_INTERVAL, commandCodes);
    }

    public MachineState(String scene, boolean concernResult, long delay, Integer commandCodes) {
        this.concernResult = concernResult;
        this.commandCode = commandCodes;
        this.scene = scene;
        this.delay = delay;
    }

    private boolean hasExtraSceneMatch(String scene) {
        if (extraScene == null) return false;
        for (String s : extraScene) {
            if (s.equals(scene)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSceneMatch(String scene) {
        return this.scene.equals(scene) || hasExtraSceneMatch(scene);
    }
}
