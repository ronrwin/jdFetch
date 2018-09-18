package com.example.jddata

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Toast

import com.example.jddata.Entity.ActionType
import com.example.jddata.Entity.RowData
import com.example.jddata.action.*
import com.example.jddata.service.AccService
import com.example.jddata.shelldroid.Env
import com.example.jddata.shelldroid.EnvManager
import com.example.jddata.shelldroid.ListAppActivity
import com.example.jddata.shelldroid.Location
import com.example.jddata.util.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

import kotlin.collections.HashMap

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val metrics = DisplayMetrics()
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics)
        GlobalInfo.width = metrics.widthPixels
        GlobalInfo.height = metrics.heightPixels
        Log.w("zfr", "width:${GlobalInfo.width}, height:${GlobalInfo.height}")

        is_test.isChecked = GlobalInfo.sIsTest
        is_test.setOnCheckedChangeListener { buttonView, isChecked -> GlobalInfo.sIsTest = isChecked }

        is_origin.isChecked = GlobalInfo.sIsOrigin
        is_origin.setOnCheckedChangeListener { buttonView, isChecked -> GlobalInfo.sIsOrigin = isChecked }

        autoFetch.isChecked = GlobalInfo.sAutoFetch
        autoFetch.setOnCheckedChangeListener { buttonView, isChecked -> GlobalInfo.sAutoFetch = isChecked }

        outputAsExcel.isChecked = GlobalInfo.outputAsExcel
        outputAsExcel.setOnCheckedChangeListener { buttonView, isChecked -> GlobalInfo.outputAsExcel = isChecked }

        val wifiLocation = SharedPreferenceHelper.getInstance().getValue(RowData.WIFI_LOCATION)
        if (!TextUtils.isEmpty(wifiLocation)) {
            wifiCity.setText(wifiLocation!!)
        }

        open_setting.setOnClickListener {
            OpenAccessibilitySettingHelper.jumpToSettingPage(this@MainActivity)// 跳转到开启页面
        }

        search.setOnClickListener {
            val map = HashMap<String, String>()
            map.put("searchText", searchText.text.toString())
            doAction(ActionType.FETCH_SEARCH, map)
        }
        searchClick.setOnClickListener {
            val map = HashMap<String, String>()
            map.put("searchText", searchText.text.toString())
            map.put("clickText", clickText.text.toString())
            doAction(ActionType.MOVE_SEARCH_AND_CLICK, map)
        }
        searchShop.setOnClickListener {
            val map = HashMap<String, String>()
            map.put("searchText", searchText.text.toString())
            map.put("clickText", clickText.text.toString())
            doAction(ActionType.MOVE_SEARCH_CLICK_AND_SHOP, map)
        }
//        fetchSearch.setOnClickListener {
//            val map = HashMap<String, String>()
//            map.put("searchText", searchText.text.toString())
//            doAction(ActionType.FETCH_SEARCH, map)
//        }
        home.setOnClickListener { doAction(ActionType.FETCH_HOME) }
        dmp.setOnClickListener { doAction(ActionType.MOVE_DMP) }
        dmpClick.setOnClickListener { doAction(ActionType.MOVE_DMP_CLICK) }
        dmpClickShop.setOnClickListener { doAction(ActionType.MOVE_DMP_CLICK_SHOP) }
        niceBuy.setOnClickListener { doAction(ActionType.FETCH_NICE_BUY) }
        cart.setOnClickListener { doAction(ActionType.FETCH_CART) }
        jdKill.setOnClickListener { doAction(ActionType.FETCH_JD_KILL) }
        typeKill.setOnClickListener { doAction(ActionType.FETCH_TYPE_KILL) }
        brandKill.setOnClickListener { doAction(ActionType.FETCH_BRAND_KILL) }
        worthBuy.setOnClickListener { doAction(ActionType.FETCH_WORTH_BUY) }
        leaderboard.setOnClickListener { doAction(ActionType.FETCH_LEADERBOARD) }
        brandKillAndShop.setOnClickListener { doAction(ActionType.MOVE_BRAND_KILL_AND_SHOP) }
        brandKillClick.setOnClickListener { doAction(ActionType.MOVE_BRAND_KILL_CLICK) }
        jdKillClick.setOnClickListener { doAction(ActionType.MOVE_JD_KILL_CLICK) }
        jdKillAndShop.setOnClickListener { doAction(ActionType.MOVE_JD_KILL_AND_SHOP) }
        jdKillRemindMe.setOnClickListener { doAction(ActionType.MOVE_JD_KILL_REMIND) }
        scanProduct.setOnClickListener { doAction(ActionType.MOVE_SCAN_PRODUCT) }
        scanProductShop.setOnClickListener { doAction(ActionType.MOVE_SCAN_PRODUCT_BUY) }

        outputCSV.setOnClickListener {
            GlobalInfo.emulatorId = machineNum.text.toString()
            if (TextUtils.isEmpty(GlobalInfo.emulatorId)) {
                Toast.makeText(this, "请输入手机id", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val date = outputDate.text.toString()
            StorageUtil.outputDatabaseDatas(date)
        }

        outputOriginCSV.setOnClickListener {
            GlobalInfo.emulatorId = machineNum.text.toString()
            if (TextUtils.isEmpty(GlobalInfo.emulatorId)) {
                Toast.makeText(this, "请输入手机id", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            StorageUtil.outputDatabaseDatas("", true)
        }

        onKeyRun.setOnClickListener {
            if (!OpenAccessibilitySettingHelper.isAccessibilitySettingsOn(this@MainActivity)) {
                OpenAccessibilitySettingHelper.jumpToSettingPage(this@MainActivity)
                return@setOnClickListener
            }

            GlobalInfo.sOneKeyRun = true
            doAction(null, null)
        }

        shelldroid.setOnClickListener {
            val intent = Intent(this@MainActivity, ListAppActivity::class.java)
            startActivity(intent)
        }

        create.setOnClickListener {
            val envs = EnvManager.envs
            val map = HashMap<String, Env>()
            for (env in envs) {
                val name = env.envName
                if (name != null && !map.containsKey(name)) {
                    map.put(env.envName!!, env)
                }
            }

            val start = Integer.parseInt(startNum.text.toString())
            val end = Integer.parseInt(endNum.text.toString())
            for (i in start..end) {
                if (!map.containsKey("$i")) {
                    EnvManager.envDirBuild(EnvManager.createJDApp(AccService.PACKAGE_NAME, "$i"))
                }
            }

            EnvManager.envs = EnvManager.scanEnvs()
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

        val bytes = FileUtils.readBytes(Environment.getExternalStorageDirectory().toString() + File.separator + GlobalInfo.LOCATION_FILE)
        if (bytes != null) {
            val locationStr = String(bytes)
            if (!TextUtils.isEmpty(locationStr)) {
                val loc = locationStr.split(",")
                val name = loc[0]
                for (s in 0..GlobalInfo.sLocations.size-1) {
                    val location = GlobalInfo.sLocations.get(s)
                    if (name.equals(location.name)) {
                        citySpinner.setSelection(s)
                        GlobalInfo.sSelectLocation = GlobalInfo.sLocations[s]
                        locationCity.setText(GlobalInfo.sSelectLocation.name)
                        longitude.setText(GlobalInfo.sSelectLocation.longitude.toString())
                        latitude.setText(GlobalInfo.sSelectLocation.latitude.toString())
                    }
                }
            }
        } else {
            FileUtils.writeToFile(Environment.getExternalStorageDirectory().absolutePath, GlobalInfo.LOCATION_FILE, GlobalInfo.sSelectLocation.toString())
        }

        citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                GlobalInfo.sSelectLocation = GlobalInfo.sLocations[position]
                FileUtils.writeToFile(Environment.getExternalStorageDirectory().absolutePath, GlobalInfo.LOCATION_FILE, GlobalInfo.sSelectLocation.toString())
                locationCity.setText(GlobalInfo.sSelectLocation.name)
                longitude.setText(GlobalInfo.sSelectLocation.longitude.toString())
                latitude.setText(GlobalInfo.sSelectLocation.latitude.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        val computerNumStr = SharedPreferenceHelper.getInstance().getValue(GlobalInfo.COMPUTER_NUM)
        computerNum.setText(computerNumStr)

        val emulatorId = SharedPreferenceHelper.getInstance().getValue(RowData.MOVE_ID)
        if (!TextUtils.isEmpty(emulatorId)) {
            machineNum.setText(emulatorId)
            GlobalInfo.emulatorId = emulatorId
        }
        val wifiCityStr = SharedPreferenceHelper.getInstance().getValue(RowData.WIFI_LOCATION)
        if (!TextUtils.isEmpty(wifiCityStr)) {
            wifiCity.setText(wifiCityStr)
        }

        biActionText.setText("bi采集顺序:\n" +
//                "1:${GlobalInfo.JD_KILL}\n" +
//                "1:搜索洗发水结果推荐\n" +
                "1:${GlobalInfo.BRAND_KILL}\n" +
                "2:${GlobalInfo.LEADERBOARD}\n" +
                "3:${GlobalInfo.HOME}\n" +
                "4:${GlobalInfo.CART}\n" +
                "5:${GlobalInfo.TYPE_KILL}\n" +
                "6:${GlobalInfo.WORTH_BUY}\n" +
                "7:${GlobalInfo.NICE_BUT}")

        reRun.setOnClickListener {
            if (!OpenAccessibilitySettingHelper.isAccessibilitySettingsOn(this@MainActivity)) {
                OpenAccessibilitySettingHelper.jumpToSettingPage(this@MainActivity)
                return@setOnClickListener
            }

            val startActionId = reRunActionId.text.toString().toInt()
            val startMobileId = reRunMobileId.text.toString().toInt()
            if (startActionId < 1 || startActionId > 9) {
                Toast.makeText(this@MainActivity, "动作Id为（1-9）", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (startMobileId < 1 || startMobileId > 7) {
                Toast.makeText(this@MainActivity, "账号Id为（1-7）", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            BusHandler.instance.reRunTask(startActionId, startMobileId)
        }

        clearJdCache.setOnClickListener {
            EnvManager.clearAppCache()
        }
    }

    private fun doAction(action: String) {
        doAction(action, null)
    }

    private fun doAction(action: String?, map : HashMap<String, String>?) {
        val computerNumStr = computerNum.text.toString()
        if (TextUtils.isEmpty(computerNumStr)) {
            Toast.makeText(this, "请输入电脑机id", Toast.LENGTH_LONG).show()
            return
        }

        SharedPreferenceHelper.getInstance().saveValue(GlobalInfo.COMPUTER_NUM, computerNumStr)

        GlobalInfo.emulatorId = machineNum.text.toString()
        if (TextUtils.isEmpty(GlobalInfo.emulatorId)) {
            Toast.makeText(this, "请输入手机id", Toast.LENGTH_LONG).show()
            return
        }

        SharedPreferenceHelper.getInstance().saveValue(RowData.MOVE_ID, machineNum.text.toString())

        val wifiCityStr = wifiCity.text.toString()
        if (TextUtils.isEmpty(wifiCityStr)) {
            Toast.makeText(this, "请输入ip所属城市", Toast.LENGTH_LONG).show()
            return
        }

        val city = locationCity.text.toString()
        val longitudeStr = longitude.text.toString()
        val latitudeStr = latitude.text.toString()
        if(TextUtils.isEmpty(city) || TextUtils.isEmpty(longitudeStr) || TextUtils.isEmpty(latitudeStr)) {
            Toast.makeText(this, "经纬度与城市不能为空", Toast.LENGTH_LONG).show()
            return
        } else {
            GlobalInfo.sSelectLocation = Location(city, longitudeStr.toDouble(), latitudeStr.toDouble())
            FileUtils.writeToFile(Environment.getExternalStorageDirectory().absolutePath, GlobalInfo.LOCATION_FILE, GlobalInfo.sSelectLocation.toString())
        }

        SharedPreferenceHelper.getInstance().saveValue(RowData.WIFI_LOCATION, wifiCityStr)

        if (!OpenAccessibilitySettingHelper.isAccessibilitySettingsOn(this@MainActivity)) {
            OpenAccessibilitySettingHelper.jumpToSettingPage(this@MainActivity)
            return
        }

        if (GlobalInfo.sOneKeyRun) {
            BusHandler.instance.oneKeyRun()
        } else {
            GlobalInfo.sTargetEnvName = oneEnv.text.toString()
            GlobalInfo.singleActionType = null
            if (TextUtils.isEmpty(GlobalInfo.sTargetEnvName)) {
                if (GlobalInfo.sIsTest) {
                    MainApplication.startMainJD()
                    GlobalInfo.mCurrentAction = Factory.createAction(action, map)
                } else {
                    GlobalInfo.singleActionType = action
                    GlobalInfo.taskid = 0
                    GlobalInfo.sArgMap = map
                    BusHandler.instance.runNextEnv(0)
                }
            } else {
                val result = EnvManager.activeByName(GlobalInfo.sTargetEnvName)
                if (result) {
                    GlobalInfo.mCurrentAction = Factory.createAction(action, map)
                } else {
                    Toast.makeText(this, "启动账号出错", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
