package com.qxtx.idea.ideasvg.tools;

import android.content.Context;
import android.text.TextUtils;
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

    /**
     * 将xml中的dimen值转换成px
     * @param dimen 带dimen单位的数值字符串
     * @param defValue 默认数值
     */
    public static float parseDimenToPx(Context context, String dimen, float defValue) {
        if (TextUtils.isEmpty(dimen)) {
            return defValue;
        }

        float result = defValue;
        int len = dimen.length();
        float value;
        if (dimen.endsWith("dip")) {
            value = Float.parseFloat(dimen.substring(0, len - 3));
            result = ScreenUtil.dp2Px(context, value);
        } else if (dimen.endsWith("dp")) {
            value = Float.parseFloat(dimen.substring(0, len - 2));
            result = ScreenUtil.dp2Px(context, value);
        } else if (dimen.endsWith("px")) {
            result = Float.parseFloat(dimen.substring(0, len - 2));
        } else {
            result = Float.parseFloat(dimen);
        }
        return Math.max(0f, result);
    }
}
