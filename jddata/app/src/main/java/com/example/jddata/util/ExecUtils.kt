package com.example.jddata.util

import android.content.ClipboardManager
import android.content.Context
import android.text.TextUtils
import com.example.jddata.Entity.BrandDetail
import com.example.jddata.MainApplication
import java.util.ArrayList
import java.util.HashMap
import kotlin.math.sin

class ExecUtils {

    companion object {
        @JvmStatic fun handleExecCommand(command: String): Boolean {
            var su: Process? = null
            try {
                su = Runtime.getRuntime().exec("su")
                su!!.outputStream.write((command + "\n").toByteArray())
                su.outputStream.write("exit\n".toByteArray())
                su.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                su?.destroy()
            }
            return true
        }

        @JvmStatic fun checkClipBoard(message: String) {
            try {
                val clipboardManager = MainApplication.getContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                if (clipboardManager != null) {

                    var lastClip: String? = null
                    if (clipboardManager.text != null) {
                        lastClip = clipboardManager.text.toString()
                    }
                    if (TextUtils.isEmpty(lastClip) || lastClip != message) {
                        clipboardManager.text = message
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }

        // 排重
        @JvmStatic fun filterSingle(origin: ArrayList<Any>): ArrayList<Any> {
            val finalList = ArrayList<Any>()
            // 排重
            val map = HashMap<Any, Any>()
            for (single in origin) {
                if (!map.containsValue(single)) {
                    finalList.add(single)
                }
            }
            return finalList
        }
    }
}
