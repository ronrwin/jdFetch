package com.example.jddata.shelldroid

class Env : Cloneable {
    var id: String? = null
    var envName: String? = null
    var appName: String? = null
    var pkgName: String? = null
    var active: Boolean = false
    var deviceId: String? = null
    var createTime: String? = null
    var location: Location? = null

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): Env {
        return super.clone() as Env
    }

    override fun toString(): String {
        return "Env(id=$id, envName=$envName, appName=$appName, pkgName=$pkgName, active=$active, deviceId=$deviceId, createTime=$createTime, location=$location)"
    }
}
