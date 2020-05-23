package com.qxtx.idea.ideasvg.tools;

import android.os.Looper;

/**
 * CreateDate 2020/5/23 13:15
 * <p>
 *
 * @author QXTX-WIN
 * Description:
 */
public class ThreadUtil {

    public static boolean isUiThread() {
        return Looper.myLooper() != null && (Looper.myLooper() == Looper.getMainLooper());
    }
}
