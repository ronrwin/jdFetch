package com.example.jddata.shelldroid

import android.app.Activity
import android.os.Bundle

import com.example.jddata.R

import kotlinx.android.synthetic.main.new_layout.*

class NewActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_layout)
        spinner!!.adapter = SpinnerAdapter()

        btn!!.setOnClickListener {
            val appInfo = spinner!!.selectedItem as AppInfo
            val env = Env()
            env.id = java.util.UUID.randomUUID().toString()
            env.envName = textName!!.text.toString()
            env.appName = appInfo.appName
            env.pkgName = appInfo.pkgName
            env.active = false
            env.deviceId = textImei!!.text.toString()
            save(env)
            quit()
        }
    }

    fun save(env: Env) {
        EnvManager.envDirBuild(env)
    }

    fun quit() {
        setResult(0)
        super.finish()
    }

}
