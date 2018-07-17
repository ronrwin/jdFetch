package com.example.jddata.shelldroid

class Env : Cloneable {
    var id: String? = null
    var envName: String? = null
    var appName: String? = null
    var pkgName: String? = null
    var active: Boolean = false
    var deviceId: String? = null
    var createTime: String? = null
    var phoneNumber: String? = null
    var networkCountryIso: String? = null
    var networkOperator: String? = null
    var simSerialNumber: String? = null
    var buildBoard: String? = null
    var buildModel: String? = null
    var buildManufacturer: String? = null
    var buildId: String? = null
    var buildDevice: String? = null
    var buildSerial: String? = null
    var buildBrand: String? = null
    var androidId: String? = null
    var location: Location? = null

    override fun toString(): String {
        return "Env{" +
                "id='" + id + '\''.toString() +
                ", envName='" + envName + '\''.toString() +
                ", appName='" + appName + '\''.toString() +
                ", pkgName='" + pkgName + '\''.toString() +
                ", active=" + active +
                ", deviceId='" + deviceId + '\''.toString() +
                ", phoneNumber='" + phoneNumber + '\''.toString() +
                ", networkCountryIso='" + networkCountryIso + '\''.toString() +
                ", networkOperator='" + networkOperator + '\''.toString() +
                ", simSerialNumber='" + simSerialNumber + '\''.toString() +
                ", buildBoard='" + buildBoard + '\''.toString() +
                ", buildModel='" + buildModel + '\''.toString() +
                ", buildManufacturer='" + buildManufacturer + '\''.toString() +
                ", buildId='" + buildId + '\''.toString() +
                ", buildDevice='" + buildDevice + '\''.toString() +
                ", buildSerial='" + buildSerial + '\''.toString() +
                ", buildBrand='" + buildBrand + '\''.toString() +
                ", androidId='" + androidId + '\''.toString() +
                ", location=" + location +
                '}'.toString()
    }

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): Env {
        return super.clone() as Env
    }
}
