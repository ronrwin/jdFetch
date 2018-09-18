package com.example.jddata

import android.accessibilityservice.AccessibilityService
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.widget.Toast
import com.example.jddata.Entity.ActionType

import com.example.jddata.Entity.MessageDef
import com.example.jddata.action.*
import com.example.jddata.shelldroid.EnvManager
import com.example.jddata.util.LogUtil
import com.example.jddata.util.NetworkUtils
import kotlinx.android.synthetic.main.activity_main.*

import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.math.log

class BusHandler private constructor() : android.os.Handler(Looper.getMainLooper()) {

    var singleThreadExecutor: Executor = Executors.newSingleThreadExecutor()
    var mAccessibilityService: AccessibilityService? = null

    private object Holder {
        val mInstance = BusHandler()
    }

    override fun handleMessage(msg: Message) {
        if (GlobalInfo.mCurrentAction != null) {
            val type = GlobalInfo.mCurrentAction!!.mActionType
            val what = msg.what
            val network = if (NetworkUtils.isNetworkEnabled(MainApplication.getContext())) "wifi is ok" else "no network"
            when (what) {
                MessageDef.MSG_TIME_OUT -> {
                    var failText = "<<<<<<<<<< ${EnvManager.sCurrentEnv?.envName}账号, actionTimeout : $type, ${network}"

                    if (!GlobalInfo.sIsTest) {
                        val needRetry = GlobalInfo.retryTime < GlobalInfo.MAX_RETRY_TIME
                        if (GlobalInfo.mCurrentAction != null) {
                            if (needRetry) {
                                failText = "<<<<<<<<<< ${EnvManager.sCurrentEnv?.envName}账号, actionTimeout : $type, 重试第${GlobalInfo.retryTime+1}遍, ${network}"
                            }
                        }

                        LogUtil.writeLog(failText)
                        LogUtil.flushLog(!needRetry)
                        LogUtil.writeResultLog(failText)
                        if (needRetry) {
                            GlobalInfo.mCurrentAction!!.addRetryTime()
                            runNextEnv(GlobalInfo.taskid)
                        } else {
                            GlobalInfo.retryTime = 0
                            runNextEnv(++GlobalInfo.taskid)
                        }
                    } else {
                        LogUtil.writeLog(failText)
                        LogUtil.flushLog()
                        LogUtil.writeResultLog(failText)
                        removeMessages(MessageDef.MSG_TIME_OUT)
                        GlobalInfo.mCurrentAction = null
                    }
                }
                MessageDef.FAIL -> {
                    var failText = "<<<<<<<<<< ${EnvManager.sCurrentEnv?.envName}账号, actionFail : $type, ${network}"

                    if (!GlobalInfo.sIsTest) {
                        val needRetry = GlobalInfo.retryTime < GlobalInfo.MAX_RETRY_TIME
                        if (GlobalInfo.mCurrentAction != null) {
                            if (needRetry) {
                                failText = "<<<<<<<<<< ${EnvManager.sCurrentEnv?.envName}账号, actionFail : $type, 重试第${GlobalInfo.retryTime+1}遍, ${network}"
                            }
                        }

                        LogUtil.writeLog(failText)
                        LogUtil.flushLog(!needRetry)
                        LogUtil.writeResultLog(failText)

                        if (needRetry) {
                            GlobalInfo.mCurrentAction!!.addRetryTime()
                            runNextEnv(GlobalInfo.taskid)
                        } else {
                            GlobalInfo.retryTime = 0
                            runNextEnv(++GlobalInfo.taskid)
                        }
                    } else {
                        LogUtil.writeLog(failText)
                        LogUtil.flushLog()
                        LogUtil.writeResultLog(failText)

                        removeMessages(MessageDef.MSG_TIME_OUT)
                        GlobalInfo.mCurrentAction = null
                    }
                }
                MessageDef.SUCCESS -> {
                    var failText = "----------- ${EnvManager.sCurrentEnv?.envName}, actionSuccess : $type"
                    if (GlobalInfo.mCurrentAction != null) {
                        if (GlobalInfo.mCurrentAction!!.mActionType!!.startsWith("move")
                            || ActionType.FETCH_SEARCH.equals(GlobalInfo.mCurrentAction!!.mActionType!!)) {
                            LogUtil.writeMoveTime(GlobalInfo.mCurrentAction!!)
                            Thread.sleep(GlobalInfo.MOVE_INTERVAL * 1000L)  // 等20秒开始执行
                        }

                        if (!GlobalInfo.sIsTest) {
                            var shouldRetry = GlobalInfo.mCurrentAction!!.needRetry()
                            if (ActionType.FETCH_NICE_BUY.equals(type)) {
                                if (GlobalInfo.mCurrentAction!!.fetchCount < 100 && GlobalInfo.retryTime < GlobalInfo.MAX_RETRY_TIME) {
                                    shouldRetry = true
                                }
                            }

                            if (shouldRetry) {
                                failText = "<<<<<<<<<< ${EnvManager.sCurrentEnv?.envName}账号, actionFail : $type, 没有收集到数据， 重试第${GlobalInfo.retryTime+1}遍, ${network}"
                            } else {
                                if (!type!!.startsWith("move")) {
                                    if (GlobalInfo.mCurrentAction!!.itemCount <= 0) {
                                        failText = "<<<<<<<<<< ${EnvManager.sCurrentEnv?.envName}账号, actionFail : $type, 没有收集到数据。 ${network}"
                                    } else {
                                        var fetchFull = GlobalInfo.mCurrentAction!!.itemCount >= GlobalInfo.FETCH_NUM
                                        if (!fetchFull && GlobalInfo.retryTime < GlobalInfo.MAX_RETRY_TIME) {
                                            // 没采集满
                                            when (type) {
                                                ActionType.FETCH_HOME -> {
                                                    shouldRetry = true
                                                    failText = "<<<<<<<<<< ${EnvManager.sCurrentEnv?.envName}账号, actionFail : $type, 没有收集满。重试第${GlobalInfo.retryTime + 1}遍, ${network}"
                                                }
                                                ActionType.FETCH_CART -> {
                                                    shouldRetry = true
                                                    failText = "<<<<<<<<<< ${EnvManager.sCurrentEnv?.envName}账号, actionFail : $type, 没有收集满。重试第${GlobalInfo.retryTime + 1}遍, ${network}"
                                                }
                                                ActionType.FETCH_SEARCH -> {
                                                    shouldRetry = true
                                                    failText = "<<<<<<<<<< ${EnvManager.sCurrentEnv?.envName}账号, actionFail : $type, 没有收集满。重试第${GlobalInfo.retryTime + 1}遍, ${network}"
                                                }
                                                ActionType.FETCH_WORTH_BUY -> {
                                                    shouldRetry = true
                                                    failText = "<<<<<<<<<< ${EnvManager.sCurrentEnv?.envName}账号, actionFail : $type, 没有收集满。重试第${GlobalInfo.retryTime + 1}遍, ${network}"
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            LogUtil.writeLog(failText)
                            LogUtil.flushLog(!shouldRetry)
                            LogUtil.writeResultLog(failText)
                            removeMessages(MessageDef.MSG_TIME_OUT)

                            if (shouldRetry) {
                                GlobalInfo.mCurrentAction!!.addRetryTime()
                                runNextEnv(GlobalInfo.taskid)
                            } else {
                                GlobalInfo.retryTime = 0

                                if (ActionType.FETCH_SEARCH.equals(type)) {
                                    // 搜索动作做完后马上获取结果数据
                                    LogUtil.writeMoveTime(GlobalInfo.mCurrentAction!!)
                                    Thread.sleep(5 * 1000L)
//                                    Thread.sleep(GlobalInfo.MOVE_INTERVAL * 1000L)  // 等20秒开始执行
                                }

                                runNextEnv(++GlobalInfo.taskid)
                            }
                        } else {
                            LogUtil.writeLog(failText)
                            LogUtil.flushLog()
                            LogUtil.writeResultLog(failText)

                            removeMessages(MessageDef.MSG_TIME_OUT)
                            GlobalInfo.mCurrentAction = null
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
                    GlobalInfo.mCurrentAction = Factory.createAction(action.mActionType, action.map)
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
                    GlobalInfo.mCurrentAction = Factory.createAction(GlobalInfo.singleActionType!!, GlobalInfo.sArgMap)
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
        sendEmptyMessageDelayed(MessageDef.MSG_TIME_OUT, 2 * 60 * 1000L)
    }

    companion object {
        val instance: BusHandler
            get() = Holder.mInstance
    }
}
