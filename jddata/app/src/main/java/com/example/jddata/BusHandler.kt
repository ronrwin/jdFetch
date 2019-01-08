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
                    LogUtil.logCache(failText)
                    LogUtil.flushLog()
                    LogUtil.writeResultLog(failText)
                    removeMessages(MessageDef.MSG_TIME_OUT)
                    mCurrentAction?.clear()
                    mCurrentAction = null
                }
                MessageDef.FAIL -> {
                    var failText = "<<<<<<<<<< ${EnvManager.sCurrentEnv?.envName}账号, actionFail : $type, ${network}"

                    LogUtil.logCache(failText)
                    LogUtil.flushLog()
                    LogUtil.writeResultLog(failText)

                    removeMessages(MessageDef.MSG_TIME_OUT)
                    mCurrentAction?.clear()
                    mCurrentAction = null
                }
                MessageDef.SUCCESS -> {
                    var failText = "----------- ${EnvManager.sCurrentEnv?.envName}, actionSuccess : $type"

                    LogUtil.writeMove(mCurrentAction!!)
                    LogUtil.logCache(failText)
                    LogUtil.flushLog()
                    LogUtil.writeResultLog(failText)

                    removeMessages(MessageDef.MSG_TIME_OUT)
                    mCurrentAction?.clear()
                    mCurrentAction = null
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
