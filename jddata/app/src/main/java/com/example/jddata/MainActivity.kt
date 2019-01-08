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

        is_origin.isChecked = GlobalInfo.sIsOrigin
        is_origin.setOnCheckedChangeListener { _, isChecked -> GlobalInfo.sIsOrigin = isChecked }

        open_setting.setOnClickListener {
            OpenAccessibilitySettingHelper.jumpToSettingPage(this@MainActivity)// 跳转到开启页面
        }

        search.setOnClickListener {
            val map = HashMap<String, String>()
            map.put("searchText", searchText.text.toString())
            doAction(ActionType.MOVE_SEARCH, map)
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
        fetchSearch.setOnClickListener {
            val map = HashMap<String, String>()
            map.put("searchText", searchText.text.toString())
            doAction(ActionType.FETCH_SEARCH, map)
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
            val date = outputDate.text.toString()
            StorageUtil.outputDatabaseDatas(date)
        }

        outputOriginCSV.setOnClickListener {
            StorageUtil.outputDatabaseDatas("", true)
        }

        shelldroid.setOnClickListener {
            val intent = Intent(this@MainActivity, ListAppActivity::class.java)
            startActivity(intent)
        }

        clearJdCache.setOnClickListener {
            EnvManager.clearAppCache()
        }
        clearEnv.setOnClickListener {
            EnvManager.clear()
        }
    }

    private fun doAction(action: String) {
        doAction(action, null)
    }

    private fun doAction(action: String?, map : HashMap<String, String>?) {
        if (!OpenAccessibilitySettingHelper.isAccessibilitySettingsOn(this@MainActivity)) {
            OpenAccessibilitySettingHelper.jumpToSettingPage(this@MainActivity)
            return
        }

        MainApplication.startMainJD(true)
        BusHandler.instance.mCurrentAction = Factory.createAction(action, map)

    }
}
