package com.qxtx.idea.svg.tools;

import android.util.Log;

/**
 * @author QXTX-WIN
 * @date 2020/3/17 9:39
 * <p>
 * Description 日志打印封装类
 */
public final class SvgLog {

    private static final String TAG = "SvgLog";

    public static void E(String... msg) {
        log("E", msg);
    }

    public static void I(String... msg) {
        log("I", msg);
    }

    public static void W(String... msg) {
        log("W", msg);
    }

    public static void D(String... msg) {
        log("D", msg);
    }

    public static void WTF(String... msg) {
        log("WTF", msg);
    }

    private static void log(String level, String... msg) {
        String callerLog = "";
        StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        //排除调用栈自己的2个，本方法自己1个， 从第4个开始
        for (int i = 3; i < ste.length; i++) {
            StackTraceElement child = ste[i];
            if (child.getClassName().equals(SvgLog.class.getName())) {
                continue;
            }

            String clsName = child.getClassName();
            callerLog = clsName.substring(clsName.lastIndexOf(".") + 1) + "$" + child.getMethodName()
                    + "(" + child.getFileName() + ":" + child.getLineNumber() + ")\n";
            break;
        }

        StringBuilder sb = new StringBuilder();
        for (String s : msg) {
            sb.append(s);
        }
        String log = sb.toString();

        level = level.toUpperCase();
        switch (level) {
            case "E":
                Log.e(TAG, callerLog + log);
                break;
            case "I":
                Log.i(TAG, callerLog + log);
                break;
            case "D":
                Log.d(TAG, callerLog + log);
                break;
            case "W":
                Log.w(TAG, callerLog + log);
                break;
            case "WTF":
                Log.wtf(TAG, callerLog + log);
                break;
        }

    }
}
