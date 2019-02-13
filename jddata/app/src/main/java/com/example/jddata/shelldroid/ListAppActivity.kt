package com.example.jddata.shelldroid

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray

import com.example.jddata.R
import com.example.jddata.service.AccService
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.FileUtils
import kotlinx.android.synthetic.main.list_layout.*
import java.io.File

class ListAppActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_layout)

        add.setOnClickListener {
            val intent = Intent(this@ListAppActivity, NewActivity::class.java)
            startActivityForResult(intent, 0)
        }

        clear.setOnClickListener {
            EnvManager.clear()
            rv.adapter = DataAdapter()
        }

        jsonFile.setOnClickListener {
            val file = File(Environment.getExternalStorageDirectory().absolutePath + "/json")
            val byteArray = FileUtils.readBytes(file)
            val ss = String(byteArray)
            val jsonArray = org.json.JSONArray(ss)
            val size = jsonArray.length()
            for (i in 0..size-1) {
                val env = JSON.parseObject(jsonArray.optJSONObject(i).toString(), Env::class.java)
                env.active = false
                env.createTime = ExecUtils.getCurrentTimeString()
                env.pkgName = AccService.PACKAGE_NAME
                env.appName = "京东"
                EnvManager.envDirBuild(env)
                Log.d("zfr", env.toString())
            }
            EnvManager.envs = EnvManager.scanEnvs()
            rv.adapter = DataAdapter()
        }
    }

    override fun onResume() {
        EnvManager.envs = EnvManager.scanEnvs()
        rv.adapter = DataAdapter()
        super.onResume()
    }
}
