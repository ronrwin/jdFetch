package com.example.jddata.shelldroid

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import com.example.jddata.AppInfo
import com.example.jddata.GlobalInfo
import com.example.jddata.Location

import com.example.jddata.R

import kotlinx.android.synthetic.main.new_layout.*

class NewActivity : Activity() {
    var location: Location? = GlobalInfo.sLocations[0]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_layout)
        spinner!!.adapter = SpinnerAdapter()

        locationSpinner.adapter = LocationSpinnerAdapter()
        locationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                location = GlobalInfo.sLocations[position]
            }
        }

        btn!!.setOnClickListener {
            val appInfo = spinner!!.selectedItem as AppInfo
            val env = Env()
//            env.id = java.util.UUID.randomUUID().toString()
            env.id = textName!!.text.toString()
            env.envName = textName!!.text.toString()
            env.appName = appInfo.appName
            env.pkgName = appInfo.pkgName
            env.createTime = "${System.currentTimeMillis()}"
            env.active = false
            env.locationName = location?.name
            env.longitude = location?.longitude
            env.latitude = location?.latitude
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
