package com.example.jddata.shelldroid

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import com.example.jddata.GlobalInfo
import com.example.jddata.R
import com.example.jddata.Template
import com.example.jddata.action.Command
import com.example.jddata.service.AccService
import com.example.jddata.service.ServiceCommand
import com.example.jddata.util.ExecUtils
import com.example.jddata.util.FileUtils
import kotlinx.android.synthetic.main.list_layout.*
import org.json.JSONObject
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
    }


    override fun onResume() {
        EnvManager.envs = EnvManager.scanEnvs()
        rv.adapter = DataAdapter()
        super.onResume()
    }
}
