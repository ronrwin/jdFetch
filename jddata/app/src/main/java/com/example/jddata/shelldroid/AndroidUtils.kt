package com.example.jddata.shelldroid

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.example.jddata.AppInfo

import com.example.jddata.MainApplication
import com.example.jddata.R

import java.util.ArrayList

class AndroidUtils {
    companion object {
        val pm = MainApplication.sContext.packageManager
        @JvmStatic val installedAppInfo: ArrayList<AppInfo>
            get() {
                val infos = ArrayList<AppInfo>()
                val pkgs = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                for (pkg in pkgs) {
                    if ((pkg.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) == 0) {
                        val appName = pm.getApplicationLabel(pkg).toString()
                        infos.add(AppInfo(appName, pkg.packageName, getIcon(pkg.packageName)))
                    }
                }
                return infos
            }


        @JvmStatic fun getIcon(pkgName: String): Drawable? {
            try {
                return pm?.getApplicationIcon(pkgName)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return MainApplication.sContext.getDrawable(R.drawable.ic_launcher_background)
        }

        @JvmStatic fun getDataDir(pkgName: String): String? {
            try {
                val info = pm.getApplicationInfo(pkgName, 0)
                if (info != null) {
                    return info.dataDir
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }
    }
}
