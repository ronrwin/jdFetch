package com.example.jddata.util

import android.accessibilityservice.AccessibilityService
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.example.jddata.GlobalInfo
import com.example.jddata.MainApplication
import java.text.SimpleDateFormat
import java.util.*

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

        @JvmStatic fun computeX(value: Int): Float {
            return value * (GlobalInfo.width/540f)
        }

        @JvmStatic fun computeY(value: Int): Float {
            return value * (GlobalInfo.height/960f)
        }

        @JvmStatic fun tapCommand(x: Int, y: Int): Boolean {
            val cx = computeX(x)
            val cy = computeY(y)
            return ExecUtils.handleExecCommand("input tap ${cx} ${cy}")
        }

        @JvmStatic fun fingerScroll(): Boolean {
            return ExecUtils.handleExecCommand("input swipe ${computeX(250)} ${computeY(800)} ${computeX(250)} ${computeY(150)}")
        }

        @JvmStatic fun checkClipBoard(message: String) {
            try {
                val clipboardManager = MainApplication.sContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                var lastClip: String? = null
                if (clipboardManager.text != null) {
                    lastClip = clipboardManager.text.toString()
                }
                if (TextUtils.isEmpty(lastClip) || lastClip != message) {
                    clipboardManager.text = message
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

        @JvmStatic fun today(): String {
            return SimpleDateFormat("MM_dd").format(Date(System.currentTimeMillis()))
        }

        @JvmStatic fun getClipBoardText():String {
            val cm = MainApplication.sContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val data = cm.getPrimaryClip()  //  ClipData 里保存了一个ArryList 的 Item 序列， 可以用 getItemCount() 来获取个数
            val item = data.getItemAt(0)
            val text = item.getText().toString()// 注意 item.getText 可能为空
            return text
        }

        @JvmStatic fun canscroll(list: AccessibilityNodeInfo, index: Int): Boolean {
            return (list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) ||
                    ExecUtils.fingerScroll()) && index < GlobalInfo.SCROLL_COUNT
        }
        @JvmStatic fun translate(text: String?):String? {
            return text?.replace("\n", "")?.replace(",", "、")
        }

        @JvmStatic fun commandInput(service: AccessibilityService, className: String, viewId: String, text: String): Boolean {
            val nodes = AccessibilityUtils.findAccessibilityNodeInfosByViewId(service, viewId)
                    ?: return false

            for (node in nodes) {
                if (className == node.className) {
                    if (node.isEnabled && node.isClickable) {
                        val arguments = Bundle()
                        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
                        node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                        return true
                    }
                }
            }
            return false
        }
    }
}
