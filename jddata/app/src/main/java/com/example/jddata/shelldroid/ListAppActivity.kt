package com.example.jddata.shelldroid

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.jddata.Entity.EnvActions
import com.example.jddata.GlobalInfo
import com.example.jddata.MainApplication
import com.example.jddata.R
import com.example.jddata.service.AccService
import com.example.jddata.storage.database
import com.example.jddata.storage.toVarargArray
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.FileUtils
import com.example.jddata.util.LogUtil
import com.example.jddata.util.SharedPreferenceHelper
import kotlinx.android.synthetic.main.card.*
import kotlinx.android.synthetic.main.list_layout.*
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.*
import org.json.JSONArray
import org.json.JSONObject

class ListAppActivity : Activity() {

    private val mActivity : Activity? = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_layout)

        rv.adapter = DataAdapter()
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
                mActivity!!.runOnUiThread {
                    rv.adapter = DataAdapter()
                }
            }
        }

        getFromJson.setOnClickListener {
            if (startNo.text.toString().equals("") || pickCount.text.toString().equals("")) {
                Toast.makeText(this, "startNo or count should not be blank", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val sNo = startNo.text.toString().toInt()
            val countNo = pickCount.text.toString().toInt()

            MainApplication.sExecutor.execute {
                val notCreateNames = ArrayList<String>()
                val ss = FileUtils.readFromAssets(MainApplication.sContext, "account.json")
                val jsonArray = org.json.JSONArray(ss)
                val size = jsonArray.length()
                for (i in 0 until size) {
                    if (i >= sNo && i < sNo + countNo) {
                        val json = jsonArray.optJSONObject(i)
                        if (json != null) {
                            val env = Env()
                            createEnv(json, env)
                            notCreateNames.add(env.envName!!)
                        }
                    }
                }

                while (!cheakEnvDone(jsonArray, notCreateNames)) {}

                EnvManager.envs = EnvManager.scanEnvs()
                mActivity!!.runOnUiThread {
                    rv.adapter = DataAdapter()
                }
            }
        }
    }

    fun cheakEnvDone(jsonArray: JSONArray, notCreateNames: ArrayList<String>): Boolean {
        EnvManager.envs = EnvManager.scanEnvs()
        for (env in EnvManager.envs) {
            if (notCreateNames.contains(env.envName)) {
                notCreateNames.remove(env.envName)
            }
        }

        Log.e("zfr", "not created: ${notCreateNames}")

        val size = jsonArray.length()
        for (i in 0 until size) {
            val json = jsonArray.optJSONObject(i)
            if (json != null) {
                val name = json.optString("name")
                if (notCreateNames.contains(name)) {
                    val env = Env()
                    createEnv(json, env)
                }
            }
        }
        return notCreateNames.size <= 0
    }

    fun createEnv(json: JSONObject, env: Env) {
        env.envName = json.optString("name")
        env.id = json.optString("id") + "_" + env.envName
        env.appName = "京东"
        env.pkgName = AccService.PACKAGE_NAME
        env.locationNo = json.optString("locationNo")
        env.createTime = ExecUtils.getCurrentTimeString()
        env.locationName = json.optString("locationName")
        env.longitude = json.optDouble("longitude")
        env.latitude = json.optDouble("latitude")
        env.observation = json.optString("observation")
        env.day9 = json.optString("day9")
        val actionJson = json.optJSONObject("actions")
        if (actionJson != null) {
            env.envActions = EnvActions()
            env.envActions!!.parseJson(actionJson)
        }

        EnvManager.envDirBuild(env)
    }
}
