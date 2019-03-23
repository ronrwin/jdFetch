package com.example.jddata.util

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.MainApplication.sContext
import com.example.jddata.MainApplication.sExecutor
import java.io.File


class JdUtils {
    companion object {

        @JvmStatic fun dmpclick(service: AccessibilityService): Boolean {
            val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(service, "com.jingdong.app.mall:id/h8")
            if (AccessibilityUtils.isNodesAvalibale(nodes)) {
                val scroll = nodes!![0]
                if (scroll != null && scroll.childCount > 0) {
                    val child = scroll.getChild(0)
                    if (child != null) {
                        return child.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                }
            }
            return false
        }

        @JvmStatic fun copyPic(filename: String) {
            sExecutor.execute {
                val path = File(Environment.getExternalStorageDirectory().toString() + "/Pictures/" + filename)
                if (path.exists()) {
                    path.delete()
                }

                if (!path.exists()) {
                    try {
                        FileUtils.copyAssets(sContext, filename, path.getAbsolutePath())
                        // 最后通知图库更新
                        sContext.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://$path")))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            }
        }
    }



}