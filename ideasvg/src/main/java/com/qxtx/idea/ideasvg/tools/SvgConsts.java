package com.qxtx.idea.ideasvg.tools;

/**
 * @author QXTX-WIN
 * @date 2019/12/11 23:33
 * Description: 各种常量
 */
public final class SvgConsts {

    public static final String SUPPORT_ROOT_TAG = "vector";

    public static final char SVG_START_CMD_UPPER = 'M';

    public static final char SVG_END_CHAR_UPPER = 'Z';

    /**
     * 在svg中能被正确解析的指令符（大写形式）
     *  (1)H = horizontal lineTo(x)
     *  (1)V = vertical lineto(y)
     *  (2)L = lineTo(x,y)
     *  (2)M = moveTo(x,y)
     *  (0)Z = closepath
     *  (4)Q = quadratic Belzier curve(fromX, fromY, toX, toY)
     *  (2)T = smooth quadratic Belzier 等价于（curve(toX - fromX, toY - fromY, toX, toY)）
     *  (6)C = curveTo(x1, y1, x2, y2, x3, y3) svg中6个参数
     *  (4)S = smooth curveTo（同C） svg中4个参数
     *  (7)A = elliptical ArcTo(l, t, r, b, startAngle, sweepAngle) svg中7个参数
     */
    public static final String SVG_CMD_UPPER = "HVLMZQTCSA";

    /** path数值之间的分隔符（大写形式） */
    public static final String VALUE_DELIMITER_UPPER = " ,.-";

    /** 指令符以外的有效字符 */
    public static final String SVG_CHAR_OTHER = ".0123456789,- ";

    /** 在svg中的有效字符（大写形式）*/
    public static final String SVG_CHAR_UPPER = SVG_CMD_UPPER + SVG_CHAR_OTHER;
}
