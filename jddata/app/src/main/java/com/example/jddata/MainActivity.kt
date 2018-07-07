package com.example.jddata

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.TextView

import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.CartGoods
import com.example.jddata.service.AccService
import com.example.jddata.action.Factory
import com.example.jddata.shelldroid.Env
import com.example.jddata.shelldroid.EnvManager
import com.example.jddata.shelldroid.ListAppActivity
import com.example.jddata.util.FileUtils
import com.example.jddata.util.OpenAccessibilitySettingHelper
import com.example.jddata.util.ScreenUtils
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

import java.util.HashMap

class MainActivity : Activity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        is_test.isChecked = GlobalInfo.sIsTest
        is_test.setOnCheckedChangeListener { buttonView, isChecked -> GlobalInfo.sIsTest = isChecked }

        open_setting.setOnClickListener {
            OpenAccessibilitySettingHelper.jumpToSettingPage(this@MainActivity)// 跳转到开启页面
        }


        search.setOnClickListener {
            doAction(ActionType.SEARCH, searchText.text.toString())
        }
        home.setOnClickListener { doAction(ActionType.HOME) }
        dmp.setOnClickListener { doAction(ActionType.DMP) }
        dmpShop.setOnClickListener { doAction(ActionType.DMP_AND_SHOP) }
        niceBuy.setOnClickListener { doAction(ActionType.NICE_BUY) }
        cart.setOnClickListener { doAction(ActionType.CART) }
        screenshot.setOnClickListener { ScreenUtils.scrrenShot() }
        jdKill.setOnClickListener { doAction(ActionType.JD_KILL) }
        typeKill.setOnClickListener { doAction(ActionType.TYPE_KILL) }
        brandKill.setOnClickListener { doAction(ActionType.BRAND_KILL) }
        worthBuy.setOnClickListener { doAction(ActionType.WORTH_BUY) }
        leaderboard.setOnClickListener { doAction(ActionType.LEADERBOARD) }
        brandKillAndShop.setOnClickListener { doAction(ActionType.BRAND_KILL_AND_SHOP) }
        searchShop.setOnClickListener {
            doAction(ActionType.SEARCH_AND_SHOP, searchText.text.toString())
        }

        oneKeyRun.setOnClickListener {
            GlobalInfo.sIsOneKey = !GlobalInfo.sIsOneKey
            if (GlobalInfo.sIsOneKey) {
                oneKeyLayout.visibility = View.VISIBLE
                textColor(Color.RED)
            } else {
                oneKeyLayout.visibility = View.GONE
                textColor(Color.BLACK)
            }
        }

        shelldroid.setOnClickListener {
            val intent = Intent(this@MainActivity, ListAppActivity::class.java)
            startActivity(intent)
        }

        create10.setOnClickListener {
            val envs = EnvManager.scanEnvs()
            val map = HashMap<String, Env>()
            for (env in envs) {
                val name = env.envName
                if (name != null && !map.containsKey(name)) {
                    map.put(env.envName!!, env)
                }
            }

            var index = 0
            var createCount = 0
            while (createCount < 10) {
                if (map.containsKey("" + index)) {
                    index++
                    continue
                } else {
                    EnvManager.envDirBuild(EnvManager.createJDApp(AccService.PACKAGE_NAME, "" + index))
                    index++
                    createCount++
                }
            }
        }

        citySpinner.adapter = object : BaseAdapter() {
            override fun getCount(): Int {
                return GlobalInfo.sLocations.size
            }

            override fun getItem(position: Int): Any {
                return GlobalInfo.sLocations[position]
            }

            override fun getItemId(position: Int): Long {
                return 0
            }

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                var textView = TextView(this@MainActivity)
                textView.text = GlobalInfo.sLocations[position].name
                return textView
            }
        }

        val locationStr = String(FileUtils.readBytes(Environment.getExternalStorageDirectory().toString() + File.separator + GlobalInfo.LOCATION_FILE)!!)
        if (!TextUtils.isEmpty(locationStr)) {
            val loc = locationStr.split(",")
            val name = loc[0]
            for (s in 0..GlobalInfo.sLocations.size-1) {
                val location = GlobalInfo.sLocations.get(s)
                if (name.equals(location.name)) {
                    citySpinner.setSelection(s)
                }
            }
        }

        citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                GlobalInfo.sSelectLocation = GlobalInfo.sLocations[position]
                FileUtils.writeToFile(Environment.getExternalStorageDirectory().absolutePath, GlobalInfo.LOCATION_FILE, GlobalInfo.sSelectLocation.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }

    private fun oneKeyRun() {

    }

    private fun textColor(color : Int) {
        search.setTextColor(color)
        searchShop.setTextColor(color)
        searchText.setTextColor(color)
        cart.setTextColor(color)
        home.setTextColor(color)
        leaderboard.setTextColor(color)
        niceBuy.setTextColor(color)
        worthBuy.setTextColor(color)
        jdKill.setTextColor(color)
        dmp.setTextColor(color)
        dmpShop.setTextColor(color)
        brandKill.setTextColor(color)
        brandKillAndShop.setTextColor(color)
        typeKill.setTextColor(color)
    }

    private fun doAction(action: String) {
        doAction(action, null)
    }

    private fun doAction(action: String, obj: Any?) {
        if (!OpenAccessibilitySettingHelper.isAccessibilitySettingsOn(this@MainActivity)) {
            OpenAccessibilitySettingHelper.jumpToSettingPage(this@MainActivity)
            return
        }

        if (GlobalInfo.sIsOneKey) {

        } else {
            GlobalInfo.sTargetEnvName = oneEnv.text.toString()
            if (TextUtils.isEmpty(GlobalInfo.sTargetEnvName)) {
                if (GlobalInfo.sIsTest) {
                    BusHandler.getInstance().mCurrentAction = Factory.createAction(action, obj)
                    MainApplication.startMainJD()
                } else {
                    BusHandler.getInstance().mActionType = action
                    BusHandler.getInstance().mTaskId = 0
                    BusHandler.getInstance().start()
                }
            } else {
                BusHandler.getInstance().mCurrentAction = Factory.createAction(action, obj)
                EnvManager.activeByName(GlobalInfo.sTargetEnvName)
            }
        }
    }
}
