package com.example.jddata;

public class ExecUtils {

    public static void handleExecCommand(String command) {
        Process su = null;
        try {
            su = Runtime.getRuntime().exec("su");
            su.getOutputStream().write((command + "\n").getBytes());
            su.getOutputStream().write("exit\n".getBytes());
            su.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (su != null) {
                su.destroy();
            }
        }
    }
}
