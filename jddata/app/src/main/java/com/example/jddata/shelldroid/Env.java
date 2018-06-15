package com.example.jddata.shelldroid;

public class Env implements Cloneable {
    public String id;
    public String envName;
    public String appName;
    public String pkgName;
    public boolean active;
    public String deviceId;
    public String phoneNumber;
    public String networkCountryIso;
    public String networkOperator;
    public String simSerialNumber;
    public String buildBoard;
    public String buildModel;
    public String buildManufacturer;
    public String buildId;
    public String buildDevice;
    public String buildSerial;
    public String buildBrand;
    public String androidId;
    public Location location;

    @Override
    public String toString() {
        return "Env{" +
                "id='" + id + '\'' +
                ", envName='" + envName + '\'' +
                ", appName='" + appName + '\'' +
                ", pkgName='" + pkgName + '\'' +
                ", active=" + active +
                ", deviceId='" + deviceId + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", networkCountryIso='" + networkCountryIso + '\'' +
                ", networkOperator='" + networkOperator + '\'' +
                ", simSerialNumber='" + simSerialNumber + '\'' +
                ", buildBoard='" + buildBoard + '\'' +
                ", buildModel='" + buildModel + '\'' +
                ", buildManufacturer='" + buildManufacturer + '\'' +
                ", buildId='" + buildId + '\'' +
                ", buildDevice='" + buildDevice + '\'' +
                ", buildSerial='" + buildSerial + '\'' +
                ", buildBrand='" + buildBrand + '\'' +
                ", androidId='" + androidId + '\'' +
                ", location=" + location +
                '}';
    }

    @Override
    protected Env clone() throws CloneNotSupportedException {
        return (Env) super.clone();
    }
}
