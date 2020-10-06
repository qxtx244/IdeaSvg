package com.qxtx.idea.ideasvg.tools;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Create date 2020/10/6 15:31
 * @author QXTX-WIN
 * Description 屏幕相关参数工具类
 */
public class ScreenUtil {
    private static DisplayMetrics dm = null;

    public ScreenUtil() {
    }

    public static float getStatusBarHeight(Context context) {
        int resId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return (float)context.getResources().getDimensionPixelSize(resId);
    }

    public static float getScaleSp(Context context, float sp) {
        if (dm == null) {
            dm = context.getResources().getDisplayMetrics();
        }

        return sp * dm.scaledDensity;
    }

    public static float px2Dp(Context context, int px) {
        if (dm == null) {
            dm = context.getResources().getDisplayMetrics();
        }

        return (float)px / dm.density;
    }

    public static float dp2Px(Context context, float dp) {
        if (dm == null) {
            dm = context.getResources().getDisplayMetrics();
        }

        return dp * dm.density;
    }

    public static float getScaleDensity(Context context) {
        if (dm == null) {
            dm = context.getResources().getDisplayMetrics();
        }

        return dm.scaledDensity;
    }

    public static float getDensity(Context context) {
        if (dm == null) {
            dm = context.getResources().getDisplayMetrics();
        }

        return dm.density;
    }

    public static int getDensityDpi(Context context) {
        if (dm == null) {
            dm = context.getResources().getDisplayMetrics();
        }

        return dm.densityDpi;
    }

    public static int getScreenWidthPx(Context context) {
        if (dm == null) {
            dm = context.getResources().getDisplayMetrics();
        }

        return dm.widthPixels;
    }

    public static int getScreenHeightPx(Context context) {
        if (dm == null) {
            dm = context.getResources().getDisplayMetrics();
        }

        return dm.heightPixels;
    }
}
