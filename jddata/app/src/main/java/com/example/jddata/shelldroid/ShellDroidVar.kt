package com.example.jddata.shelldroid

import android.graphics.drawable.Drawable
import com.example.jddata.action.Command

class AppInfo(var appName: String?, var pkgName: String?, var icon: Drawable?)
class Location(var name: String, var longitude: Double, var latitude: Double) {
    override fun toString(): String {
        return "$name,$longitude,$latitude"
    }
}

class Template {
    var templateId = 0
    var actions = ArrayList<Command>()
}
