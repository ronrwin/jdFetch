package com.example.jddata.util

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
    }

}
