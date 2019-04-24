package com.example.jddata

import android.accessibilityservice.AccessibilityService
import android.os.Looper
import android.os.Message
import com.example.jddata.Entity.ActionType

import com.example.jddata.Entity.MessageDef
import com.example.jddata.Entity.Route
import com.example.jddata.action.*
import com.example.jddata.action.unknown.TemplateMoveAction
import com.example.jddata.shelldroid.EnvManager
import com.example.jddata.util.LogUtil
import com.example.jddata.util.LogUtil.Companion.writeResultLog
import com.example.jddata.util.NetworkUtils

import java.util.concurrent.Executor
import java.util.concurrent.Executors

class BusHandler private constructor() : android.os.Handler(Looper.getMainLooper()) {

    // 线程池处理日志
    var singleThreadExecutor: Executor = Executors.newSingleThreadExecutor()
    var mAccessibilityService: AccessibilityService? = null

    var mCurrentAction: Action? = null

    override fun handleMessage(msg: Message) {
        if (mCurrentAction != null) {
            var type = mCurrentAction!!.mActionType
            if (type.equals(ActionType.TEMPLATE_MOVE)) {
                val temp = mCurrentAction!!.getState(GlobalInfo.ROUTE)
                if (temp != null) {
                    val route = temp as Route
                    type = type + "_day_${mCurrentAction!!.day}_route_${route.id}"
                }
            }

            val what = msg.what
            val network = if (NetworkUtils.isNetworkEnabled(MainApplication.sContext)) "wifi is ok" else "no network"
            val cost = (System.currentTimeMillis() - mCurrentAction!!.startTimeStamp) / 1000L
            MainApplication.sAllTaskCost += cost
            when (what) {
                MessageDef.MSG_TIME_OUT -> {
                    var failText = "<<<<<<<<<< ${mCurrentAction!!.env?.id}, actionTimeout : $type, ${network}, cost: ${cost}s"
                    LogUtil.logCache("warn", failText)
                    LogUtil.flushLog(mCurrentAction!!.env!!, false, true)
                    LogUtil.writeResultLog(failText)


                    reAddAction()

                    removeMessages(MessageDef.MSG_TIME_OUT)
                    mCurrentAction?.clear()
                    mCurrentAction = null
                }
                MessageDef.FAIL -> {
                    var failText = "<<<<<<<<<< ${mCurrentAction!!.env?.id}, actionFail : $type, ${network}, cost: ${cost}s"

                    LogUtil.logCache("warn", failText)
                    LogUtil.flushLog(mCurrentAction!!.env!!, false, true)
                    LogUtil.writeResultLog(failText)

                    reAddAction()

                    removeMessages(MessageDef.MSG_TIME_OUT)
                    mCurrentAction?.clear()
                    mCurrentAction = null
                }
                MessageDef.SUCCESS -> {
                    if (mCurrentAction!!.mActionType!!.startsWith("fetch")) {
                        if (mCurrentAction!!.itemCount <= 0) {
                            sendEmptyMessage(MessageDef.FAIL)
                            return
                        }
                    }

                    var failText = "----------- ${mCurrentAction!!.env?.id}, actionSuccess : $type, isOrigin: ${GlobalInfo.sIsOrigin}, cost: ${cost}s"

                    if (mCurrentAction!!.isMoveAction) {
                        LogUtil.writeMove(mCurrentAction!!)
                    }
                    LogUtil.logCache("debug", failText)
                    LogUtil.flushLog(mCurrentAction!!.env!!, true)
                    LogUtil.writeResultLog(failText)

                    removeMessages(MessageDef.MSG_TIME_OUT)
                    mCurrentAction?.clear()
                    mCurrentAction = null
                }
            }

            startPollAction()
        }
    }

    fun reAddAction() {
        if (mCurrentAction != null) {
            if (mCurrentAction!!.mActionType!!.startsWith("move")) {
                if (MainApplication.sDay == -1) {
                    // 第九天做动作
                    var day9No = mCurrentAction!!.getState(GlobalInfo.MOVE_NO) as Int
                    val env = mCurrentAction!!.env!!
                    val action = Factory.createDayNineAction(env, day9No)
                    if (action != null) {
                        action.setState(GlobalInfo.MOVE_NO, day9No)
                        LogUtil.logCache(">>>>  env: ${env.envName}, reRun, createAction : ${action.mActionType}, day9 action: ${day9No}")
                        MainApplication.sActionQueue.add(action)
                    } else {
                        LogUtil.logCache("error", ">>>>>>> ${env.envName}, action is null, reAdd Fail")
                    }
                }
                return
            } else if (mCurrentAction!!.mActionType.equals(ActionType.TEMPLATE_MOVE)) {
                val temp = mCurrentAction!!.getState(GlobalInfo.ROUTE)
                if (temp != null) {
                    val route = temp as Route
                    val action = Factory.createTemplateAction(mCurrentAction!!.env!!, route)
                    MainApplication.sActionQueue.add(action)
                }
            } else {
                val action = Factory.createAction(mCurrentAction!!.env!!, mCurrentAction!!.mActionType)
                if (action != null) {
                    MainApplication.sActionQueue.add(action)
                }
            }
        }
    }

    fun startPollAction() {
        BusHandler.instance.mCurrentAction = MainApplication.sActionQueue.poll()
        val action = BusHandler.instance.mCurrentAction
        if (action != null) {
            LogUtil.logCache("warn", "start action: ${action.mActionType}, Env: ${action.env}")
            LogUtil.logCache("warn", "left Action count: ${MainApplication.sActionQueue.size}")
            EnvManager.active(action.env)

            if (action.mActionType.equals(ActionType.TEMPLATE_MOVE)) {
                val tmplateAction = action as TemplateMoveAction
                LogUtil.logCache("warn", "do template move id: ${tmplateAction.sessionNo}")
            }

            action.startTimeStamp = System.currentTimeMillis()
            MainApplication.startMainJD(true)
        } else {
            LogUtil.logCache("=========== taskEnd, all action cost time: ${MainApplication.sAllTaskCost}")
            writeResultLog("=========== taskEnd, all action cost time: ${MainApplication.sAllTaskCost}")
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
