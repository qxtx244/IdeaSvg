package com.qxtx.idea.ideasvg.parser;

/**
 * CreateDate 2020/5/24 16:15
 * <p>
 *
 * @author QXTX-WIN
 * Description: 字符处理的工具类
 */
final class CharUtil {

    /** 是否为起始锚点符 */
    static boolean isStartAnchor(char ch) {
        return ch == SvgConsts.SVG_START_CHAR_UPPER || ch == (SvgConsts.SVG_START_CHAR_UPPER + 32);
    }


    static boolean maybeValueDelimiter(char ch) {
        return SvgConsts.VALUE_DELIMITER_UPPER.indexOf(toUpper(ch)) != -1;
    }

    /** 目标字符是否属于svg有效字符 */
    static boolean isSvgChar(char ch) {
        return SvgConsts.SVG_CHAR_UPPER.indexOf(toUpper(ch)) != -1;
    }

    /**
     * 目标字符是否属于锚点符
     * @param ch 目标字符
     */
    static boolean isAnchor(char ch) {
        return SvgConsts.SVG_ANCHOR_UPPER.indexOf(toUpper(ch)) != -1;
    }

    static char toUpper(char c) {
        return Character.toUpperCase(c);
    }

    static char toLower(char c) {
        return Character.toLowerCase(c);
    }
}
