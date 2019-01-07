package com.example.jddata

import android.accessibilityservice.AccessibilityService
import android.os.Looper
import android.os.Message

import com.example.jddata.Entity.MessageDef
import com.example.jddata.action.*
import com.example.jddata.action.fetch.*
import com.example.jddata.shelldroid.EnvManager
import com.example.jddata.util.LogUtil
import com.example.jddata.util.NetworkUtils

import java.util.concurrent.Executor
import java.util.concurrent.Executors

class BusHandler private constructor() : android.os.Handler(Looper.getMainLooper()) {

    var singleThreadExecutor: Executor = Executors.newSingleThreadExecutor()
    var mAccessibilityService: AccessibilityService? = null

    var mCurrentAction: BaseAction? = null

    override fun handleMessage(msg: Message) {
        if (mCurrentAction != null) {
            val type = mCurrentAction!!.mActionType
            val what = msg.what
            val network = if (NetworkUtils.isNetworkEnabled(MainApplication.sContext)) "wifi is ok" else "no network"
            when (what) {
                MessageDef.MSG_TIME_OUT -> {
                    var failText = "<<<<<<<<<< ${EnvManager.sCurrentEnv?.envName}账号, actionTimeout : $type, ${network}"

                    if (!GlobalInfo.sIsTest) {
                        LogUtil.logCache(failText)
                        LogUtil.flushLog()
                        LogUtil.writeResultLog(failText)
                        runNextEnv(++GlobalInfo.taskid)
                    } else {
                        LogUtil.logCache(failText)
                        LogUtil.flushLog()
                        LogUtil.writeResultLog(failText)
                        removeMessages(MessageDef.MSG_TIME_OUT)
                        mCurrentAction = null
                    }
                }
                MessageDef.FAIL -> {
                    var failText = "<<<<<<<<<< ${EnvManager.sCurrentEnv?.envName}账号, actionFail : $type, ${network}"

                    if (!GlobalInfo.sIsTest) {
                        LogUtil.logCache(failText)
                        LogUtil.flushLog()
                        LogUtil.writeResultLog(failText)
                        runNextEnv(++GlobalInfo.taskid)
                    } else {
                        LogUtil.logCache(failText)
                        LogUtil.flushLog()
                        LogUtil.writeResultLog(failText)

                        removeMessages(MessageDef.MSG_TIME_OUT)
                        mCurrentAction = null
                    }
                }
                MessageDef.SUCCESS -> {
                    var failText = "----------- ${EnvManager.sCurrentEnv?.envName}, actionSuccess : $type"
                    if (mCurrentAction!!.mActionType!!.startsWith("move")) {
                        LogUtil.writeMove(mCurrentAction!!)
                        Thread.sleep(GlobalInfo.MOVE_INTERVAL * 1000L)  // 等20秒开始执行
                    }

                    if (!GlobalInfo.sIsTest) {
                        if (!type!!.startsWith("move")) {
                            if (mCurrentAction!!.itemCount <= 0) {
                                failText = "<<<<<<<<<< ${EnvManager.sCurrentEnv?.envName}账号, actionFail : $type, 没有收集到数据。 ${network}"
                            }
                        }

                        LogUtil.logCache(failText)
                        LogUtil.flushLog()
                        LogUtil.writeResultLog(failText)
                        removeMessages(MessageDef.MSG_TIME_OUT)

                        runNextEnv(++GlobalInfo.taskid)
                    } else {
                        LogUtil.logCache(failText)
                        LogUtil.flushLog()
                        LogUtil.writeResultLog(failText)

                        removeMessages(MessageDef.MSG_TIME_OUT)
                        mCurrentAction = null
                    }
                }
                MessageDef.TASK_END -> {
                    LogUtil.taskEnd()
                }
            }
        }
    }

    fun oneKeyRun() {
        GlobalInfo.sOneKeyRun = true
        GlobalInfo.sIsTest = false
        GlobalInfo.currentOneKeyIndex = 0
        GlobalInfo.taskid = 0
        addActions()
        BusHandler.instance.runNextEnv(GlobalInfo.taskid)
    }

    fun reRunTask(actionId: Int, mobileId: Int) {
        GlobalInfo.sOneKeyRun = true
        GlobalInfo.sIsTest = false
        GlobalInfo.currentOneKeyIndex = actionId-1
        GlobalInfo.taskid = mobileId-1
        addActions()
        BusHandler.instance.runNextEnv(GlobalInfo.taskid)
    }

    fun addActions() {
        GlobalInfo.commandAction.clear()
//        GlobalInfo.commandAction.add(FetchJdKillAction())
//        val fetchMap = HashMap<String, String>()
//        fetchMap.put("searchText", "洗发水")
//        GlobalInfo.commandAction.add(FetchSearchAction(fetchMap))
        GlobalInfo.commandAction.add(FetchBrandKillAction())
        GlobalInfo.commandAction.add(FetchLeaderboardAction())
        GlobalInfo.commandAction.add(FetchHomeAction())
        GlobalInfo.commandAction.add(FetchCartAction())
        GlobalInfo.commandAction.add(FetchTypeKillAction())
        GlobalInfo.commandAction.add(FetchWorthBuyAction())
        GlobalInfo.commandAction.add(FetchNicebuyAction())
    }

    fun runNextEnv(id: Int) {
        var result = true
        if (GlobalInfo.sOneKeyRun) {
            result = EnvManager.activeByName((id+1).toString())

            if (GlobalInfo.currentOneKeyIndex < GlobalInfo.commandAction.size) {
                if (result) {
                    val action = GlobalInfo.commandAction[GlobalInfo.currentOneKeyIndex]
                    mCurrentAction = Factory.createAction(action.mActionType, action.map)
                } else {
                    GlobalInfo.currentOneKeyIndex++
                    GlobalInfo.taskid = 0
                    runNextEnv(GlobalInfo.taskid)
                }
            } else {
                // 所有任务跑完。
                BusHandler.instance.sendMsg(MessageDef.TASK_END)
            }
        } else {
            if (GlobalInfo.singleActionType != null) {
                result = EnvManager.activeByName((id+1).toString())
                if (result) {
                    mCurrentAction = Factory.createAction(GlobalInfo.singleActionType!!, GlobalInfo.sArgMap)
                } else {
                    BusHandler.instance.sendMsg(MessageDef.TASK_END)
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
        sendEmptyMessageDelayed(MessageDef.MSG_TIME_OUT,  60 * 1000L)
    }

    fun startCountTimeout(delayed: Long) {
        removeMessages(MessageDef.MSG_TIME_OUT)
        sendEmptyMessageDelayed(MessageDef.MSG_TIME_OUT,  delayed)
    }

    private object Holder {
        val mInstance = BusHandler()
    }
    
    companion object {
        val instance: BusHandler
            get() = Holder.mInstance
    }

}
