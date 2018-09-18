package com.example.jddata.util

import android.content.ClipboardManager
import android.content.Context
import android.text.TextUtils
import com.example.jddata.Entity.BrandDetail
import com.example.jddata.GlobalInfo
import com.example.jddata.MainApplication
import java.text.SimpleDateFormat
import java.util.*
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

        @JvmStatic fun tapCommand(x: Int, y: Int): Boolean {
            val cx = x * (GlobalInfo.width/540f)
            val cy = y * (GlobalInfo.height/960f)
            return ExecUtils.handleExecCommand("input tap ${cx} ${cy}")
        }

        @JvmStatic fun fingerScroll(): Boolean {
            return ExecUtils.handleExecCommand("input swipe 250 800 250 250")
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
        @JvmStatic fun <T> filterSingle(origin: ArrayList<T>): ArrayList<T> {
            val finalList = ArrayList<T>()
            // 排重
            val set = HashSet<T>()
            for (single in origin) {
                if (!set.contains(single)) {
                    set.add(single)
                    finalList.add(single)
                }
            }
            return finalList
        }

        @JvmStatic fun getCurrentTimeString(): String {
            return SimpleDateFormat("MM-dd HH:mm:ss:SSS").format(Date(System.currentTimeMillis()))
        }

        @JvmStatic fun getCurrentTimeString(format: SimpleDateFormat): String {
            return format.format(Date(System.currentTimeMillis()))
        }
    }
}
