package com.example.jddata

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.example.jddata.Entity.ActionType
import com.example.jddata.action.Factory
import com.example.jddata.shelldroid.EnvManager
import com.example.jddata.util.LogUtil
import java.util.*

/**

@author: zengfr
@date:  2019/05/11
 */
class JdKillCheckHandler(looper: Looper) : Handler(looper) {
    override fun handleMessage(msg: Message?) {
        val shouldAdd = check()
        if (shouldAdd) {
            sendEmptyMessageDelayed(0, 6 * 60 * 60 * 1000L)
        } else {
            sendEmptyMessageDelayed(0, 600 * 1000L)
        }
        super.handleMessage(msg)
    }

    fun check(): Boolean {
        var date = Date(System.currentTimeMillis())
        var shouldAdd = false
        if (date.hours >= 10 && date.hours < 12) {
            shouldAdd = true
        } else if (date.hours >= 20 && date.hours < 22) {
            shouldAdd = true
        }

        if (shouldAdd) {
            var shouldpoll = false
            if (MainApplication.sActionQueue.size == 0) {
                shouldpoll = true
            } else {
                for (ac in MainApplication.sActionQueue) {
                    if (ac.mActionType.equals(ActionType.FETCH_JD_KILL)) {
                        return false
                    }
                }
            }
            for (env in EnvManager.envs) {
                val action = Factory.createAction(env, ActionType.FETCH_JD_KILL)
                if (action != null) {
                    LogUtil.logCache(">>>>  env: ${env.envName}, createAction : ${action!!.mActionType}")
                    MainApplication.sActionQueue.addFirst(action)
                }
            }

            if (shouldpoll) {
                BusHandler.instance.startPollAction()
            }
        }
        LogUtil.logCache("debug", "check jd_kill, shouldAdd: ${shouldAdd}")
        return shouldAdd
    }
}