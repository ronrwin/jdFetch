package com.example.jddata.shelldroid

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import com.example.jddata.GlobalInfo
import com.example.jddata.MainApplication
import com.example.jddata.R
import com.example.jddata.Template
import com.example.jddata.action.Command
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.FileUtils
import com.example.jddata.util.SharedPreferenceHelper
import kotlinx.android.synthetic.main.list_layout.*
import org.json.JSONObject
import java.io.File

class ListAppActivity : Activity() {

    private val mActivity : Activity?=this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_layout)

        add.setOnClickListener {
            val intent = Intent(this@ListAppActivity, NewActivity::class.java)
            startActivityForResult(intent, 0)
        }

        clear.setOnClickListener {
            MainApplication.sExecutor.execute {
                for (env in EnvManager.envs) {
                    val key = GlobalInfo.TODAY_DO_ACTION + "-${env.envName}"
                    SharedPreferenceHelper.getInstance().saveValue(key, "")
                }
                EnvManager.clear()
                EnvManager.envs = EnvManager.scanEnvs()
                mActivity!!.runOnUiThread {
                    rv.adapter = DataAdapter()
                }
            }
        }

        getFromJson.setOnClickListener {
            MainApplication.sExecutor.execute {
                val file = File(Environment.getExternalStorageDirectory().absolutePath + "/account.json")
                val byteArray = FileUtils.readBytes(file)
                val ss = String(byteArray)
                val jsonArray = org.json.JSONArray(ss)
                val size = jsonArray.length()
                for (i in 0 until size) {
                    val json = jsonArray.optJSONObject(i)
                    if (json != null) {
                        val env = Env()
                        env.id = json.optString("name")
                        env.envName = json.optString("name")
                        env.appName = "京东"
                        env.pkgName = AccService.PACKAGE_NAME
                        env.imei = json.optString("imei")
                        env.createTime = "" + System.currentTimeMillis()
                        env.locationName = json.optString("locationName")
                        env.longitude = json.optDouble("longitude")
                        env.latitude = json.optDouble("latitude")
                        EnvManager.envDirBuild(env)
                    }
                }
                EnvManager.envs = EnvManager.scanEnvs()
                mActivity!!.runOnUiThread {
                    rv.adapter = DataAdapter()
                }
            }
        }
    }


    override fun onResume() {
        EnvManager.envs = EnvManager.scanEnvs()
        rv.adapter = DataAdapter()
        super.onResume()
    }
}
