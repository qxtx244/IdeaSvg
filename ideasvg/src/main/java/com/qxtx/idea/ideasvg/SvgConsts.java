package com.qxtx.idea.ideasvg;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author QXTX-WIN
 * @date 2019/12/11 23:33
 * Description:
 */
public final class SvgConsts {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ANIM_CLIPPING, ANIM_PATH_MOVING})
    public @interface SvgAnimCategory {}
    public static final int ANIM_CLIPPING = 0x1;
    public static final int ANIM_PATH_MOVING = 0x2;

    public static final int INVAILE_VALUE = Integer.MIN_VALUE;

    public static final char SEPARATOR = ',';

    public static final char SVG_START_ANCHOR = 'M';

    public static final char SVG_CLOSED_ANCHOR = 'Z';

    /**
     * 包含了所有在svg中能被正确解析的锚点符。
     * All the valid anchor-character in this for svg.
     *  H = horizontal lineTo(x)
     *  V = vertical lineto(y)
     *  L = lineTo(x,y)
     *  M = moveTo(x,y)
     *  Z = closepath
     *  Q = quadratic Belzier curve(fromX, fromY, toX, toY)
     *  T = smooth quadratic Belzier curveTo（同Q）
     *  C = curveTo(x1, y1, x2, y2, x3, y3)
     *  S = smooth curveTo（同C）
     *  A = elliptical ArcTo(l, t, r, b, startAngle, sweepAngle)
     */
    public static final String SVG_ANCHOR_ALL = "HhVvLlMmZzQqTtCcSsAa";

    /**
     * 包含了所有在svg中能被正确解析的字符。
     * All the valid character in this for svg.
     */
    public static final String SVG_CHAR_ALL = SVG_ANCHOR_ALL + ".0123456789,- ";
}
