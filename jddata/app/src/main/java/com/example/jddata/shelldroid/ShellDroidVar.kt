package com.example.jddata.shelldroid

import android.graphics.drawable.Drawable

class AppInfo(var appName: String?, var pkgName: String?, var icon: Drawable?)
class Location(var name: String, var longitude: Double, var latitude: Double) {
    override fun toString(): String {
        return "$name,$longitude,$latitude"
    }
}
