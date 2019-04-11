package com.example.jddata

import android.accessibilityservice.AccessibilityService
import android.os.Looper
import android.os.Message
import com.example.jddata.Entity.ActionType

import com.example.jddata.Entity.MessageDef
import com.example.jddata.Entity.Route
import com.example.jddata.action.*
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
            val type = mCurrentAction!!.mActionType
            val what = msg.what
            val network = if (NetworkUtils.isNetworkEnabled(MainApplication.sContext)) "wifi is ok" else "no network"
            when (what) {
                MessageDef.MSG_TIME_OUT -> {
                    var failText = "<<<<<<<<<< ${mCurrentAction!!.env?.envName}账号, actionTimeout : $type, ${network}"
                    LogUtil.logCache("warn", failText)
                    LogUtil.flushLog(mCurrentAction!!.env!!, false)
                    LogUtil.writeResultLog(failText)

                    reAddAction()

                    removeMessages(MessageDef.MSG_TIME_OUT)
                    mCurrentAction?.clear()
                    mCurrentAction = null
                }
                MessageDef.FAIL -> {
                    var failText = "<<<<<<<<<< ${mCurrentAction!!.env?.envName}账号, actionFail : $type, ${network}"

                    LogUtil.logCache("warn", failText)
                    LogUtil.flushLog(mCurrentAction!!.env!!, false)
                    LogUtil.writeResultLog(failText)

                    reAddAction()

                    removeMessages(MessageDef.MSG_TIME_OUT)
                    mCurrentAction?.clear()
                    mCurrentAction = null
                }
                MessageDef.SUCCESS -> {
                    var failText = "----------- ${mCurrentAction!!.env?.envName}, actionSuccess : $type"

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
            if (!mCurrentAction!!.mActionType.equals(ActionType.TEMPLATE_MOVE)) {
                val action = Factory.createAction(mCurrentAction!!.env!!, mCurrentAction!!.mActionType)
                MainApplication.sActionQueue.add(action)
            } else {
                val temp = mCurrentAction!!.getState(GlobalInfo.ROUTE)
                if (temp != null) {
                    val route = temp as Route
                    val action = Factory.createTemplateAction(mCurrentAction!!.env!!, route)
                    MainApplication.sActionQueue.add(action)
                }
            }
        }
    }

    fun startPollAction() {
        BusHandler.instance.mCurrentAction = MainApplication.sActionQueue.poll()
        val action = BusHandler.instance.mCurrentAction
        if (action != null) {
            LogUtil.logCache("debug", "start Env: ${action.env}")
            LogUtil.logCache("debug", "left Action count: ${MainApplication.sActionQueue.size}")
            EnvManager.active(action!!.env)
            MainApplication.startMainJD(true)
        } else {
            LogUtil.logCache("=========== taskEnd")
            writeResultLog("=========== taskEnd")
        }
    }

    fun sendMsg(what: Int) {
        removeMessages(MessageDef.MSG_TIME_OUT)
        removeMessages(what)
        sendEmptyMessage(what)
    }

    fun startCountTimeout() {
        removeMessages(MessageDef.MSG_TIME_OUT)
        sendEmptyMessageDelayed(MessageDef.MSG_TIME_OUT,  120 * 1000L)
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
