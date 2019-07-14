package com.example.jddata.shelldroid

import com.example.jddata.Entity.EnvActions
import java.io.Serializable

class Env : Cloneable, Serializable {
    private val serialVersionUID = 1L

    var id: String? = null
    var envName: String? = null
    var appName: String? = null
    var pkgName: String? = null
    var active: Boolean = false
    var imei: String? = null
    var createTime: String? = null
    var locationNo: String? = null
    var locationName: String? = null
    var longitude: Double? = null
    var latitude: Double? = null
    var observation: String? = null
    var day9: String? = null
    var envActions: EnvActions? = null
    var move: String? = null

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): Env {
        return super.clone() as Env
    }

    override fun toString(): String {
        return "Env(id=$id, envName=$envName, appName=$appName, pkgName=$pkgName, active=$active, imei=$imei, createTime=$createTime, locationNo=$locationNo, locationName=$locationName, longitude=$longitude, latitude=$latitude, observation=$observation)"
    }

}
