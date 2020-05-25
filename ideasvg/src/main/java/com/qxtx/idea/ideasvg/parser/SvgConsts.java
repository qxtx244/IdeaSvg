package com.qxtx.idea.ideasvg.parser;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author QXTX-WIN
 * @date 2019/12/11 23:33
 * Description: 保存各种常量
 */
final class SvgConsts {

    static final String SUPPORT_ROOT_TAG = "vector";

    static final char SVG_START_CHAR_UPPER = 'M';

    static final char SVG_END_CHAR_UPPER = 'Z';

    /**
     * 在svg中能被正确解析的锚点符（大写形式）
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
    static final String SVG_ANCHOR_UPPER = "HVLMZQTCSA";

    /** 锚点符需要的数值个数列表，以（大写字母ascii码-'A')为下标，记录以字符为锚点符的子路径需要的数值个数 */
    static int[] ANCHOR_VALUE_ARRAY = new int[] {
            //ABCDEFG
            7, -1, 6, -1, -1, -1, -1,
            //HIJKLMN
            1, -1, -1, -1, 2, 2, -1,
            //OPQRST
            -1, -1, 4, -1, 4, 2,
            //UVWXYZ
            -1, 1, -1, -1, -1, 0
    };

    /** path数值之间的分隔符（大写形式） */
    static final String VALUE_DELIMITER_UPPER = " ,.-";

    /** 锚点符以外的有效字符 */
    static final String SVG_CHAR_OTHER = ".0123456789,- ";

    /** 在svg中的有效字符（大写形式）*/
    static final String SVG_CHAR_UPPER = SVG_ANCHOR_UPPER + SVG_CHAR_OTHER;
}
