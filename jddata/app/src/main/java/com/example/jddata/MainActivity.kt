package com.example.jddata

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast

import com.example.jddata.Entity.ActionType
import com.example.jddata.action.*
import com.example.jddata.shelldroid.EnvManager
import com.example.jddata.shelldroid.ListAppActivity
import com.example.jddata.storage.MyDatabaseOpenHelper
import com.example.jddata.util.*
import kotlinx.android.synthetic.main.activity_main.*

import kotlin.collections.HashMap

class MainActivity : Activity() {

    var mActivity: Activity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = this
        setContentView(R.layout.activity_main)

        val metrics = DisplayMetrics()
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics)
        GlobalInfo.width = metrics.widthPixels
        GlobalInfo.height = metrics.heightPixels
        Log.w("zfr", "width:${GlobalInfo.width}, height:${GlobalInfo.height}")

        is_origin.isChecked = GlobalInfo.sIsOrigin
        is_origin.setOnCheckedChangeListener { _, isChecked -> GlobalInfo.sIsOrigin = isChecked }

        open_setting.setOnClickListener {
            OpenAccessibilitySettingHelper.jumpToSettingPage(this@MainActivity)// 跳转到开启页面
        }

        market.setOnClickListener { doAction(ActionType.JD_MARKET) }
        fresh.setOnClickListener { doAction(ActionType.JD_FRESH) }
        accessHome.setOnClickListener { doAction(ActionType.JD_ACCESS_HOME) }
        nut.setOnClickListener { doAction(ActionType.JD_NUT) }
        flash.setOnClickListener { doAction(ActionType.FLASH_BUY) }
        voucher.setOnClickListener { doAction(ActionType.COUPON) }
        plus.setOnClickListener { doAction(ActionType.PLUS) }
        my.setOnClickListener { doAction(ActionType.FETCH_MY) }
        home.setOnClickListener { doAction(ActionType.FETCH_HOME) }
        niceBuy.setOnClickListener { doAction(ActionType.FETCH_NICE_BUY) }
        cart.setOnClickListener { doAction(ActionType.FETCH_CART) }
        jdKill.setOnClickListener { doAction(ActionType.FETCH_JD_KILL) }
        typeKill.setOnClickListener { doAction(ActionType.FETCH_TYPE_KILL) }
        brandKill.setOnClickListener { doAction(ActionType.FETCH_BRAND_KILL) }
        worthBuy.setOnClickListener { doAction(ActionType.FETCH_WORTH_BUY) }
        leaderboard.setOnClickListener { doAction(ActionType.FETCH_LEADERBOARD) }
        fetchSearch.setOnClickListener { doAction(ActionType.FETCH_SEARCH) }
        move.setOnClickListener { doAction(ActionType.TEMPLATE_MOVE) }
        dmp.setOnClickListener { doAction(ActionType.FETCH_DMP) }

        outputCSV.setOnClickListener {
            val date = outputDate.text.toString()
            MyDatabaseOpenHelper.outputDatabaseDatas(date)
        }

        outputOriginCSV.setOnClickListener {
            MyDatabaseOpenHelper.outputDatabaseDatas("", true)
        }

        shelldroid.setOnClickListener {
            val intent = Intent(this@MainActivity, ListAppActivity::class.java)
            startActivity(intent)
        }

        clearJdCache.setOnClickListener {
            MainApplication.sExecutor.execute {
                EnvManager.clearAppCache()
            }
        }
        clearEnv.setOnClickListener {
            EnvManager.clear()
        }
    }

    private fun doAction(actionType: String) {
        if (day.text.toString().equals("")) {
            Toast.makeText(this, "day should not be blank", Toast.LENGTH_LONG).show()
            return
        }
        MainApplication.sDay = day.text.toString().toInt()

        MainApplication.sExecutor.execute {
            if (!OpenAccessibilitySettingHelper.isAccessibilitySettingsOn(this@MainActivity)) {
                mActivity?.runOnUiThread {
                    OpenAccessibilitySettingHelper.jumpToSettingPage(this@MainActivity)
                }
                return@execute
            }

            if (EnvManager.scanEnvs().size < 0) {
                Log.e(LogUtil.TAG, "stop. no env.")
                return@execute
            }

            MainApplication.sActionQueue.clear()
            mActivity?.runOnUiThread {
                val envs = EnvManager.scanEnvs()
                if (envs.size > 0) {
                    for (env in envs) {
                        if (!actionType.equals(ActionType.TEMPLATE_MOVE)) {
                            val action = Factory.createAction(env, actionType)
                            LogUtil.logCache(">>>>  env: ${env.envName}, createAction : $actionType")
                            MainApplication.sActionQueue.add(action)
                        } else {
                            val routes = env.envActions!!.days[MainApplication.sDay]
                            for (i in 0 until routes.size) {
                                val action = Factory.createTemplateAction(env, env.envActions!!.days[MainApplication.sDay][i])
                                LogUtil.logCache(">>>>  env: ${env.envName}, createAction : $actionType, Route: ${env.envActions!!.days[MainApplication.sDay][i].id}")
                                MainApplication.sActionQueue.add(action)
                            }
                        }
                    }
                }
                BusHandler.instance.startPollAction()
            }
        }
    }
}
