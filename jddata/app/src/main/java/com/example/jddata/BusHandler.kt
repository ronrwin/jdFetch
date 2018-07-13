package com.example.jddata

import android.accessibilityservice.AccessibilityService
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.util.Log

import com.example.jddata.Entity.MessageDef
import com.example.jddata.action.Factory
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

    override fun handleMessage(msg: Message) {
        if (GlobalInfo.mCurrentAction != null) {
            val type = GlobalInfo.mCurrentAction!!.mActionType
            val what = msg.what
            when (what) {
                MessageDef.MSG_TIME_OUT -> {
                    val failText = "<<<<<<<<<< ${EnvManager.sCurrentEnv?.envName}, actionTimeout : $type"
                    LogUtil.writeLog(failText)
                    LogUtil.flushLog()
                    LogUtil.wroteFailLog(failText)
                    if (!GlobalInfo.sIsTest) {
                        runNextEnv(++GlobalInfo.taskid)
                    } else {
                        removeMessages(MessageDef.MSG_TIME_OUT)
                        GlobalInfo.mCurrentAction = null
                    }
                }
                MessageDef.FAIL -> {
                    val failText = "<<<<<<<<<< ${EnvManager.sCurrentEnv?.envName}, actionFail : $type"
                    LogUtil.writeLog(failText)
                    LogUtil.flushLog()
                    LogUtil.wroteFailLog(failText)
                    if (!GlobalInfo.sIsTest) {
                        runNextEnv(++GlobalInfo.taskid)
                    } else {
                        removeMessages(MessageDef.MSG_TIME_OUT)
                        GlobalInfo.mCurrentAction = null
                    }
                }
                MessageDef.SUCCESS -> {
                    LogUtil.writeLog("----------- ${EnvManager.sCurrentEnv?.envName}, actionSuccess : $type")
                    LogUtil.flushLog()
                    if (!GlobalInfo.sIsTest) {
                        runNextEnv(++GlobalInfo.taskid)
                    } else {
                        removeMessages(MessageDef.MSG_TIME_OUT)
                        GlobalInfo.mCurrentAction = null
                    }
                }
                MessageDef.TASK_END -> {
                    LogUtil.taskEnd()
                    GlobalInfo.mCurrentAction = null
                }
            }
        }
    }


    fun runNextEnv(id: Int) {
        val result = EnvManager.activeByIndex(id)

        if (!result) {
            BusHandler.instance.sendMsg(MessageDef.TASK_END)
        } else {
            if (!TextUtils.isEmpty(GlobalInfo.singleType)) {
                GlobalInfo.mCurrentAction = Factory.createAction(GlobalInfo.singleType!!, GlobalInfo.sArgMap)
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
        sendEmptyMessageDelayed(MessageDef.MSG_TIME_OUT, 60 * 1000L)
    }

    companion object {

        val instance: BusHandler
            get() = Holder.mInstance
    }
}
