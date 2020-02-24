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
import com.example.jddata.action.unknown.SearchSkuAction
import com.example.jddata.shelldroid.Env
import com.example.jddata.shelldroid.EnvManager
import com.example.jddata.shelldroid.ListAppActivity
import com.example.jddata.storage.MyDatabaseOpenHelper
import com.example.jddata.util.*
import kotlinx.android.synthetic.main.activity_main.*
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.collections.ArrayList

class MainActivity : Activity() {

    var mActivity: Activity? = null
    val setedEnvs = ArrayList<String>()

    fun refreshRetainActions() {
        MainApplication.sExecutor.execute {
            val list = LogUtil.getSerilize()
            if (list != null) {
                var isOrigin = false
                one@for (en in list) {
                    if (en.isOrigin) {
                        GlobalInfo.sIsOrigin = true
                        break@one
                    }
                }
                runOnUiThread {
                    leftCount.setText("剩余${list.size}个未完成动作")
                }
            } else {
                runOnUiThread {
                    leftCount.setText("剩余0个未完成动作")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        version.setText("版本：1")
        refreshRetainActions()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = this
        setContentView(R.layout.activity_main)
        leftCount.setText("剩余0个未完成动作")

        val metrics = DisplayMetrics()
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics)
        GlobalInfo.width = metrics.widthPixels
        GlobalInfo.height = metrics.heightPixels
        Log.w("zfr", "width:${GlobalInfo.width}, height:${GlobalInfo.height}")

        is_origin.setOnCheckedChangeListener { buttonView, isChecked ->
            GlobalInfo.sIsOrigin = isChecked
        }

        test.setOnClickListener {
            doAction(ActionType.MOVE_SEARCH_HAIFEISI_SHOP)
        }

        open_setting.setOnClickListener {
            OpenAccessibilitySettingHelper.jumpToSettingPage(this@MainActivity)// 跳转到开启页面
        }

        refreshClient.setOnClickListener {
            MainApplication.sExecutor.execute {
                GlobalInfo.generateClient()
            }
        }
        searchBtn.setOnClickListener {
            if (!OpenAccessibilitySettingHelper.isAccessibilitySettingsOn(this@MainActivity)) {
                OpenAccessibilitySettingHelper.jumpToSettingPage(this@MainActivity)
                return@setOnClickListener
            }
            if (EnvManager.envs.size > 0) {
                val tt = search_key.text.toString()
                if (!TextUtils.isEmpty(tt)) {
                    for (env in EnvManager.envs) {
                        if (setedEnvs.contains(env.id)) {
                            // 避免重复添加
                            continue
                        }
                        setedEnvs.add(env.id!!)
                        val action = Factory.createAction(env, ActionType.FETCH_SEARCH)
                        if (action != null) {
                            action.setState(GlobalInfo.SEARCH_KEY, search_key.text.toString())
                            LogUtil.logCache(">>>>  env: ${env.envName}, createAction : ${action!!.mActionType}")
                            MainApplication.sActionQueue.add(action)
                        }
                    }
                    BusHandler.instance.startPollAction()
                } else {
                    Toast.makeText(this, "请输入搜索关键词", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "请创建账号", Toast.LENGTH_LONG).show()
            }
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
        good_hop.setOnClickListener { doAction(ActionType.FETCH_GOOD_SHOP) }
        typeKill.setOnClickListener { doAction(ActionType.FETCH_TYPE_KILL) }
        brandKill.setOnClickListener { doAction(ActionType.FETCH_BRAND_KILL) }
        worthBuy.setOnClickListener { doAction(ActionType.FETCH_WORTH_BUY) }
        leaderboard.setOnClickListener { doAction(ActionType.FETCH_LEADERBOARD) }
        fetchSearch.setOnClickListener { doAction(ActionType.FETCH_SEARCH) }
        move.setOnClickListener {
            doAction(ActionType.MOVE)
        }

        templateMove.setOnClickListener {
            doAction(ActionType.TEMPLATE_MOVE)
        }
        dmp.setOnClickListener { doAction(ActionType.FETCH_DMP) }
        fetch.setOnClickListener {
            doAction(ActionType.FETCH_ALL)
        }
        restore.setOnClickListener {
            if (!OpenAccessibilitySettingHelper.isAccessibilitySettingsOn(this@MainActivity)) {
                OpenAccessibilitySettingHelper.jumpToSettingPage(this@MainActivity)
                return@setOnClickListener
            }
            LogUtil.restoreActions()
            MainApplication.startJDKillThread()
        }

        searchShop.setOnClickListener {
            makeSearchSku(2, LogUtil.SHOP_OUT)
        }

        searchSku.setOnClickListener {
            makeSearchSku(1, LogUtil.SKU_OUT)
        }
        searchTitle.setOnClickListener {
            // 5个title
            makeSearchSku(5, LogUtil.TITLE_OUT)
        }

        startJdThread.setOnClickListener {
            MainApplication.startJDKillThread()

            Toast.makeText(MainApplication.sContext, "start jd thread done!!!", Toast.LENGTH_SHORT).show()
        }

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

        outputDatebase.setOnClickListener {
            MyDatabaseOpenHelper.outputDatabase()
        }

        clearJdCache.setOnClickListener {
            MainApplication.sExecutor.execute {
                EnvManager.clearAppCache()
                refreshRetainActions()

                runOnUiThread {
                    Toast.makeText(MainApplication.sContext, "clear jd task done", Toast.LENGTH_SHORT).show()
                }
            }
        }
        clearEnv.setOnClickListener {
            EnvManager.clear()
        }
    }

    fun makeSearchSku(num: Int, outFile: String) {
        if (!OpenAccessibilitySettingHelper.isAccessibilitySettingsOn(this@MainActivity)) {
            OpenAccessibilitySettingHelper.jumpToSettingPage(this@MainActivity)
            return
        }
        MainApplication.sExecutor.execute {
            val srcName = srcname.text.toString()
            MainApplication.sCurrentSkuFile = srcName
            val src = String(FileUtils.readBytes(LogUtil.EXTERNAL_FILE_FOLDER + "/${srcName}.txt"))
            val out = FileUtils.readBytes(LogUtil.EXTERNAL_FILE_FOLDER + "/out_${outFile}")
            var srcStr = src
            if (out != null && src != null) {
                val outStr = String(out, Charset.defaultCharset()).replace("\"", "")
                val lines = src.split("\n")
                for (line in lines) {
                    val line2 = line.replace("\r", "").replace("\n", "").replace("\"", "")
                    if (outStr.contains(line2)) {
                        srcStr = srcStr.replace(line, "")
                    }
                }
            }
            mActivity?.runOnUiThread {
                val skuAction = SearchSkuAction(EnvManager.envs[0])
                val lines = srcStr.split("\n")
                val queue = ConcurrentLinkedQueue<String>(lines)
                skuAction.setSrc(queue, num, outFile)
                MainApplication.sActionQueue.add(skuAction)
                BusHandler.instance.startPollAction()
            }
        }
    }

    private fun doAction(actionType: String) {
        if (!OpenAccessibilitySettingHelper.isAccessibilitySettingsOn(this@MainActivity)) {
            OpenAccessibilitySettingHelper.jumpToSettingPage(this@MainActivity)
            return
        }

        if (EnvManager.envs.isEmpty()) {
            Toast.makeText(this, "No Env...", Toast.LENGTH_LONG).show()
            return
        }

        if (day.text.toString().equals("")) {
            Toast.makeText(this, "day should not be blank", Toast.LENGTH_LONG).show()
            return
        }
        MainApplication.sDay = day.text.toString().toInt()
        if (MainApplication.sDay > 6) {
            Toast.makeText(this, "day should not bigger than 6", Toast.LENGTH_LONG).show()
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
                        return@execute
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
            val routes = env.envActions!!.days[0]
            val action = Factory.createTemplateAction(env, routes[0])
            if (action != null) {
                LogUtil.logCache(">>>>  env: ${env.envName}, createAction : ${action.mActionType}, moveId action: ${env.moveId!!.toInt()}")
                MainApplication.sActionQueue.add(action)
            } else {
                LogUtil.logCache("error", ">>>>>>> ${env.envName}, action is null")
            }
        } else if (actionType.equals(ActionType.MOVE)) {
            // 第九天做动作
            // 转为第九天动作，actionType是move开头
            val action = Factory.createDayNineAction(env)
            if (action != null) {
                LogUtil.logCache(">>>>  env: ${env.envName}, createAction : ${action.mActionType}, moveId action: ${env.moveId!!.toInt()}")
                MainApplication.sActionQueue.add(action)
            } else {
                LogUtil.logCache("error", ">>>>>>> ${env.envName}, action is null")
            }
        } else if (actionType.equals(ActionType.FETCH_ALL)) {
            // 京东秒杀，单独执行
            val intArray = ArrayList<Int>()
            for (i in 3..5) {
                intArray.add(i)
            }
            intArray.shuffle()

            for (i in intArray) {
                var type = ActionType.FETCH_HOME
                when (i) {
                    3 -> type = ActionType.FETCH_HOME
                    4 -> type = ActionType.FETCH_MY
                    5 -> type = ActionType.FETCH_CART
//                    6 -> type = ActionType.FETCH_GOOD_SHOP
//                    7 -> type = ActionType.FETCH_TYPE_KILL
//                    8 -> type = ActionType.FETCH_WORTH_BUY
//                    9 -> type = ActionType.FETCH_DMP
//                    10 -> type = ActionType.FETCH_LEADERBOARD
//                    11 -> type = ActionType.FETCH_BRAND_KILL
                }
                val action = Factory.createAction(env, type)
                if (action != null) {
                    LogUtil.logCache(">>>>  env: ${env.envName}, createAction : ${action!!.mActionType}")
                    MainApplication.sActionQueue.add(action)
                }
            }

            MainApplication.startJDKillThread()
        } else {
            val action = Factory.createAction(env, actionType)
            if (action != null) {
                LogUtil.logCache(">>>>  env: ${env.envName}, createAction : ${action.mActionType}")
                MainApplication.sActionQueue.add(action)
            }
        }
    }

}
