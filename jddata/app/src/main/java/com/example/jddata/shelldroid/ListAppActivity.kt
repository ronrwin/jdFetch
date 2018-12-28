package com.example.jddata.shelldroid

import android.app.Activity
import android.content.Intent
import android.os.Bundle

import com.example.jddata.R
import kotlinx.android.synthetic.main.list_layout.*

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
    }

    override fun onResume() {
        EnvManager.envs = EnvManager.scanEnvs()
        rv.adapter = DataAdapter()
        super.onResume()
    }
}
