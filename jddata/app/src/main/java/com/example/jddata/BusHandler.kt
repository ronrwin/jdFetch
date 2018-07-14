package com.example.jddata

import android.accessibilityservice.AccessibilityService
import android.os.Looper
import android.os.Message

import com.example.jddata.Entity.MessageDef
import com.example.jddata.action.*
import com.example.jddata.shelldroid.EnvManager
import com.example.jddata.util.LogUtil

import java.util.concurrent.Executor
import java.util.concurrent.Executors

class BusHandler private constructor() : android.os.Handler(Looper.getMainLooper()) {

    var singleThreadExecutor: Executor = Executors.newSingleThreadExecutor()
    var mAccessibilityService: AccessibilityService? = null

    private object Holder {
        val mInstance = BusHandler()
    }

    var retryTime = 0
    override fun handleMessage(msg: Message) {
        if (GlobalInfo.mCurrentAction != null) {
            val type = GlobalInfo.mCurrentAction!!.mActionType
            val what = msg.what
            when (what) {
                MessageDef.MSG_TIME_OUT -> {
                    val failText = "<<<<<<<<<< ${EnvManager.sCurrentEnv?.envName}账号, actionTimeout : $type"
                    LogUtil.writeLog(failText)
                    LogUtil.flushLog()
                    LogUtil.writeResultLog(failText)
                    if (!GlobalInfo.sIsTest) {
                        runNextEnv(++GlobalInfo.taskid)
                    } else {
                        removeMessages(MessageDef.MSG_TIME_OUT)
                        GlobalInfo.mCurrentAction = null
                    }
                }
                MessageDef.FAIL -> {
                    val failText = "<<<<<<<<<< ${EnvManager.sCurrentEnv?.envName}账号, actionFail : $type"
                    LogUtil.writeLog(failText)
                    LogUtil.flushLog()
                    LogUtil.writeResultLog(failText)
                    if (!GlobalInfo.sIsTest) {
                        runNextEnv(++GlobalInfo.taskid)
                    } else {
                        removeMessages(MessageDef.MSG_TIME_OUT)
                        GlobalInfo.mCurrentAction = null
                    }
                }
                MessageDef.SUCCESS -> {
                    var failText = "----------- ${EnvManager.sCurrentEnv?.envName}, actionSuccess : $type"
                    if (GlobalInfo.mCurrentAction != null) {
                        var needRetry = false
                        if (!GlobalInfo.mCurrentAction!!.hasFetchData && retryTime < 2) {
                            failText = "<<<<<<<<<< ${EnvManager.sCurrentEnv?.envName}账号, actionFail : $type, 没有收集到数据， 重试第${retryTime}遍"
                            needRetry = true
                        }
                        LogUtil.writeLog(failText)
                        LogUtil.flushLog()
                        LogUtil.writeResultLog(failText)

                        if (needRetry) {
                            if (!GlobalInfo.sIsTest) {
                                runNextEnv(GlobalInfo.taskid)
                            }
                        } else {
                            if (!GlobalInfo.sIsTest) {
                                runNextEnv(++GlobalInfo.taskid)
                            } else {
                                removeMessages(MessageDef.MSG_TIME_OUT)
                                GlobalInfo.mCurrentAction = null
                            }
                        }
                    }
                }
                MessageDef.TASK_END -> {
                    LogUtil.taskEnd()
                }
            }
        }
    }

    fun oneKeyRun() {
        GlobalInfo.sIsTest = false
        GlobalInfo.commandAction.clear()
        GlobalInfo.currentOneKeyIndex = 0

        GlobalInfo.commandAction.add(FetchJdKillAction())
        val fetchMap = HashMap<String, String>()
        fetchMap.put("searchText", "洗发水")
        GlobalInfo.commandAction.add(FetchSearchAction(fetchMap))
        GlobalInfo.commandAction.add(FetchBrandKillAction())
        GlobalInfo.commandAction.add(FetchLeaderboardAction())
        GlobalInfo.commandAction.add(FetchHomeAction())
        GlobalInfo.commandAction.add(FetchCartAction())
        GlobalInfo.commandAction.add(FetchTypeKillAction())
        GlobalInfo.commandAction.add(FetchWorthBuyAction())
        GlobalInfo.commandAction.add(FetchNicebuyAction())
        GlobalInfo.taskid = 0
        BusHandler.instance.runNextEnv(0)
    }

    fun runNextEnv(id: Int) {
        val result = EnvManager.activeByIndex(id)

        if (GlobalInfo.sOneKeyRun) {
            if (!result) {
                if (GlobalInfo.currentOneKeyIndex < GlobalInfo.commandAction.size) {
                    GlobalInfo.currentOneKeyIndex++
                    GlobalInfo.taskid = 0
                    runNextEnv(GlobalInfo.taskid)
                } else {
                    // 所有任务跑完。
                    BusHandler.instance.sendMsg(MessageDef.TASK_END)
                }
            } else {
                if (GlobalInfo.currentOneKeyIndex < GlobalInfo.commandAction.size) {
                    val action = GlobalInfo.commandAction[GlobalInfo.currentOneKeyIndex]
                    GlobalInfo.mCurrentAction = Factory.createAction(action.mActionType, action.map)
                }
            }
        } else {
            if (!result) {
                BusHandler.instance.sendMsg(MessageDef.TASK_END)
            } else {
                if (GlobalInfo.singleActionType != null) {
                    GlobalInfo.mCurrentAction = Factory.createAction(GlobalInfo.singleActionType!!, GlobalInfo.sArgMap)
                }
            }
        }
    }

    fun sendMsg(what: Int) {
        removeMessages(MessageDef.MSG_TIME_OUT)
        removeMessages(what)
        sendEmptyMessage(what)
    }

    fun startCountTimeout() {
        removeMessages(MessageDef.MSG_TIME_OUT)
        sendEmptyMessageDelayed(MessageDef.MSG_TIME_OUT, 2 * 60 * 1000L)
    }

    companion object {
        val instance: BusHandler
            get() = Holder.mInstance
    }
}
