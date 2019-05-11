package com.example.jddata

import android.app.Activity
import android.content.Intent
import android.os.*
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.util.SparseArray
import android.widget.Toast

import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.Route
import com.example.jddata.action.*
import com.example.jddata.shelldroid.Env
import com.example.jddata.shelldroid.EnvManager
import com.example.jddata.shelldroid.ListAppActivity
import com.example.jddata.storage.MyDatabaseOpenHelper
import com.example.jddata.util.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : Activity() {

    var mActivity: Activity? = null
    val setedEnvs = ArrayList<String>()
    val jdKillCheckThread = HandlerThread("jd_kill_check_thread")
    var jdKillCheckHandler: JdKillCheckHandler? = null

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
        fetch.setOnClickListener { doAction(ActionType.FETCH_ALL) }
        restore.setOnClickListener { CrashHandler.getInstance().restoreActions() }

        moveTest.setOnClickListener {
            if (!OpenAccessibilitySettingHelper.isAccessibilitySettingsOn(this@MainActivity)) {
                OpenAccessibilitySettingHelper.jumpToSettingPage(this@MainActivity)
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(testStart.text.toString())
                    || TextUtils.isEmpty(testEnd.text.toString())) {
                Toast.makeText(this, "开始于结束下标没有定义", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            MainApplication.sActionQueue.clear()
            MainApplication.sAllTaskCost = 0
            LogUtil.rowDatas.clear()

            var start = testStart.text.toString().toInt()
            var end = testEnd.text.toString().toInt()
            val sparceArray = SparseArray<Action>()
            for (env in EnvManager.envs) {
                for (i in env.envActions!!.days.indices) {
                    val routes = env.envActions!!.days[i]
                    for (route in routes) {
                        if (route.id in start..end) {
                            if (sparceArray.get(route.id) == null) {
                                val action = Factory.createTemplateAction(env, route)
                                sparceArray.put(route.id, action)
                            }
                        }
                    }
                }
            }

            for (i in start..end) {
                if (sparceArray.get(i) != null) {
                    val action = sparceArray[i]
                    LogUtil.logCache(">>>>  env: ${action.env!!.envName}, createAction : $${action.mActionType}, Route: ${i}")
                    MainApplication.sActionQueue.add(action)
                }
            }

            val ss = "abc".split(",")
            Log.d("zfr", ss[2])

            BusHandler.instance.startPollAction()
        }

        keywordTest.setOnClickListener {
            val sparseArray = SparseArray<ArrayList<Route>>()
            for (env in EnvManager.envs) {
                for (i in env.envActions!!.days.indices) {
                    val routes = env.envActions!!.days[i]
                    for (route in routes) {
                        if (sparseArray.get(route.id) == null) {
                            val list = ArrayList<Route>()
                            list.add(route)
                            sparseArray.put(route.id, list)
                        } else {
                            val list = sparseArray.get(route.id)
                            list.add(route)
                        }
                    }
                }
            }

            var allDone = true
            for (i in 0..399) {
                if (sparseArray.get(i) != null) {
                    val routes = sparseArray[i]
                    val keywordSizes = ArrayList<String>()
                    var lastSize = -1
                    var allSame = true
                    for (route in routes) {
                        if (lastSize != -1 && lastSize != route.keywords.size) {
                            allSame = false
                        }
                        lastSize = route.keywords.size
                        keywordSizes.add("${route.observation}_${route.day}_${route.keywords.size}")
                    }
                    if (!allSame) {
                        allDone = false
                        LogUtil.logCache(">>>> not all same, route id:${i}, value: ${keywordSizes}")
                    }
                }
            }
            if (allDone) {
                LogUtil.logCache("all keyword is ok")
            }
        }


        val testIds = "57".split(",")
        Log.d("zfr", "testIds size: ${testIds.size}")
        indexTest.setOnClickListener {
            if (!OpenAccessibilitySettingHelper.isAccessibilitySettingsOn(this@MainActivity)) {
                OpenAccessibilitySettingHelper.jumpToSettingPage(this@MainActivity)
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(indexStart.text.toString())
                    || TextUtils.isEmpty(indexEnd.text.toString())) {
                Toast.makeText(this, "开始于结束下标没有定义", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            MainApplication.sActionQueue.clear()
            MainApplication.sAllTaskCost = 0
            LogUtil.rowDatas.clear()

            var start = indexStart.text.toString().toInt()
            var end = indexEnd.text.toString().toInt()
            val sparceArray = SparseArray<Action>()
            for (env in EnvManager.envs) {
                for (i in env.envActions!!.days.indices) {
                    val routes = env.envActions!!.days[i]
                    for (route in routes) {
                        if (testIds.contains("${route.id}")) {
                            if (sparceArray.get(route.id) == null) {
                                val action = Factory.createTemplateAction(env, route)
                                sparceArray.put(route.id, action)
                            }
                        }
                    }
                }
            }

            for (i in start..end) {
                val index = testIds[i].toInt()
                if (sparceArray.get(index) != null) {
                    val action = sparceArray[index]
                    LogUtil.logCache(">>>>  env: ${action.env!!.envName}, createAction : $${action.mActionType}, Route: ${index}")
                    MainApplication.sActionQueue.add(action)
                }
            }
            BusHandler.instance.startPollAction()
        }

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
        if (!OpenAccessibilitySettingHelper.isAccessibilitySettingsOn(this@MainActivity)) {
            OpenAccessibilitySettingHelper.jumpToSettingPage(this@MainActivity)
            return
        }

        if (day.text.toString().equals("")) {
            Toast.makeText(this, "day should not be blank", Toast.LENGTH_LONG).show()
            return
        }
        MainApplication.sDay = day.text.toString().toInt()
        if (MainApplication.sDay > 7) {
            Toast.makeText(this, "day should not bigger than 7", Toast.LENGTH_LONG).show()
            return
        }

        MainApplication.sExecutor.execute {
            if (EnvManager.scanEnvs().size < 0) {
                Log.e(LogUtil.TAG, "stop. no env.")
                return@execute
            }

            MainApplication.sActionQueue.clear()
            MainApplication.sAllTaskCost = 0
            LogUtil.rowDatas.clear()
            setedEnvs.clear()
            val envs = EnvManager.envs

            val targetEnvString = targetEnv.text.toString()
            if (!TextUtils.isEmpty(targetEnvString)) {
                for (env in envs) {
                    if (env.envName.equals(targetEnvString)) {
                        mActivity?.runOnUiThread {
                            val targetRouteString = targetRoute.text.toString()
                            if (TextUtils.isEmpty(targetRouteString) || !actionType.equals(ActionType.TEMPLATE_MOVE)) {
                                makeAction(actionType, env)
                            } else {
                                val routeIndex = targetRouteString.toInt()
                                val action = Factory.createTemplateAction(env, env.envActions!!.days[MainApplication.sDay][routeIndex])
                                LogUtil.logCache(">>>>  env: ${env.envName}, createAction : $actionType, Route: ${env.envActions!!.days[MainApplication.sDay][routeIndex].id}")
                                MainApplication.sActionQueue.add(action)
                            }
                            BusHandler.instance.startPollAction()
                        }
                    }
                }
            } else {
                mActivity?.runOnUiThread {
                    if (envs.size > 0) {
                        for (env in envs) {
                            if (setedEnvs.contains(env.id)) {
                                // 避免重复添加
                                continue
                            }
                            setedEnvs.add(env.id!!)

                            makeAction(actionType, env)
                        }
                    }
                    BusHandler.instance.startPollAction()
                }
            }
        }
    }

    fun makeAction(actionType: String, env: Env) {
        if (actionType.equals(ActionType.TEMPLATE_MOVE)) {
            if (MainApplication.sDay == -1) {
                // 第九天做动作
                val day9No = env.day9!!.toInt()
                // 转为第九天动作，actionType是move开头
                val action = Factory.createDayNineAction(env, day9No)
                if (action != null) {
                    LogUtil.logCache(">>>>  env: ${env.envName}, createAction : ${action.mActionType}, day9 action: ${day9No}")
                    action.setState(GlobalInfo.MOVE_NO, day9No)
                    MainApplication.sActionQueue.add(action)
                } else {
                    LogUtil.logCache("error", ">>>>>>> ${env.envName}, action is null")
                }
            } else if (MainApplication.sDay == -2) {
                // 所有模板动作
                for (j in 0..6) {
                    val routes = env.envActions!!.days[j]
                    for (i in 0 until routes.size) {
                        val action = Factory.createTemplateAction(env, env.envActions!!.days[j][i])
                        if (action != null) {
                            LogUtil.logCache(">>>>  env: ${env.envName}, createAction : ${action.mActionType}, Route: ${env.envActions!!.days[j][i].id}")
                            MainApplication.sActionQueue.add(action)
                        }
                    }
                }
            } else {
                // 模板动作
                val routes = env.envActions!!.days[MainApplication.sDay]
                for (i in 0 until routes.size) {
                    val action = Factory.createTemplateAction(env, env.envActions!!.days[MainApplication.sDay][i])
                    if (action != null) {
                        LogUtil.logCache(">>>>  env: ${env.envName}, createAction : ${action.mActionType}, Route: ${env.envActions!!.days[MainApplication.sDay][i].id}")
                        MainApplication.sActionQueue.add(action)
                    }
                }
            }
        } else if (actionType.equals(ActionType.FETCH_ALL)) {
            if (!GlobalInfo.sIsOrigin && MainApplication.sDay == -1) {
                // 原始数据不收集搜索点位
                val day9No = env.day9!!.toInt()
                if (day9No < 4) {
                    val key = "${GlobalInfo.HAS_DONE_FETCH_SEARCH}_${env.id}"
                    val hasDoneFetchSearch = SharedPreferenceHelper.getInstance().getValue(key)
                    if (TextUtils.isEmpty(hasDoneFetchSearch)) {
                        val action = Factory.createAction(env, ActionType.FETCH_SEARCH)
                        if (action != null) {
                            LogUtil.logCache(">>>>  env: ${env.envName}, createAction : ${action.mActionType}")
                            MainApplication.sActionQueue.add(action)
                        }

                        SharedPreferenceHelper.getInstance().saveValue(key, "true")
                    }
                }
            }
            // 京东秒杀，单独执行
            for (i in 3..11) {
                var type = ActionType.FETCH_HOME
                when (i) {
                    4 -> type = ActionType.FETCH_CART
                    5 -> type = ActionType.FETCH_MY
                    6 -> type = ActionType.FETCH_BRAND_KILL
                    7 -> type = ActionType.FETCH_TYPE_KILL
                    8 -> type = ActionType.FETCH_WORTH_BUY
                    // fixme: 采集sku的方案
//                    9 -> type = ActionType.FETCH_NICE_BUY
                    9 -> type = ActionType.FETCH_NICE_BUY
                    10 -> type = ActionType.FETCH_LEADERBOARD
                    11 -> type = ActionType.FETCH_DMP
                }
                val action = Factory.createAction(env, type)
                if (action != null) {
                    LogUtil.logCache(">>>>  env: ${env.envName}, createAction : ${action!!.mActionType}")
                    MainApplication.sActionQueue.add(action)
                }
            }

            if (jdKillCheckHandler == null) {
                jdKillCheckThread.start()
                jdKillCheckHandler = JdKillCheckHandler(jdKillCheckThread.looper)
                jdKillCheckHandler!!.sendEmptyMessageDelayed(0, 0)
            }
        } else {
            val action = Factory.createAction(env, actionType)
            if (action != null) {
                LogUtil.logCache(">>>>  env: ${env.envName}, createAction : ${action.mActionType}")
                MainApplication.sActionQueue.add(action)
            }
        }
    }
}
