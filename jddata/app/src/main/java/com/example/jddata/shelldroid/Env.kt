package com.example.jddata.shelldroid

class Env : Cloneable {
    var id: String? = null
    var envName: String? = null
    var appName: String? = null
    var pkgName: String? = null
    var active: Boolean = false
    var imei: String? = null
    var createTime: String? = null
    var locationName: String? = null
    var longitude: Double? = null
    var latitude: Double? = null

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): Env {
        return super.clone() as Env
    }

    override fun toString(): String {
        return "Env(id=$id, envName=$envName, appName=$appName, pkgName=$pkgName, active=$active, imei=$imei, createTime=$createTime, locationName=$locationName, longitude=$longitude, latitude=$latitude)"
    }


}
