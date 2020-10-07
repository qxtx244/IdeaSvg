package com.qxtx.idea.ideasvg.tools;

/**
 * CreateDate 2020/5/24 16:15
 * <p>
 *
 * @author QXTX-WIN
 * Description: 字符处理的工具类
 */
public final class SvgCharUtil {

    /** 是否为起始指令符 */
    public static boolean isStartCommand(char ch) {
        return toUpper(ch) == SvgConsts.SVG_START_CMD_UPPER;
    }


    public static boolean maybeValueDelimiter(char ch) {
        return SvgConsts.VALUE_DELIMITER_UPPER.indexOf(toUpper(ch)) != -1;
    }

    /** 目标字符是否属于svg有效字符 */
    public static boolean isSvgChar(char ch) {
        return SvgConsts.SVG_CHAR_UPPER.indexOf(toUpper(ch)) != -1;
    }

    /**
     * 目标字符是否属于指令符
     * @param ch 目标字符
     */
    public static boolean isSvgCommand(char ch) {
        return SvgConsts.SVG_CMD_UPPER.indexOf(toUpper(ch)) >= 0;
    }

    /**
     * 判断是否为特殊的指令符（简写了数值列表），即T/S/t/s，这类指令符需要转换成普通的指令符（为了补全数值），以方便解析
     * @param ch
     * @return [true]特殊指令符 [false]非特殊指令符，不一定是普通指令符，可能为无效指令符
     */
    public static boolean isSvgSpecCommand(char ch) {
        ch = toUpper(ch);
        return ch == 'T' || ch == 'S';
    }

    public static char toUpper(char c) {
        return Character.toUpperCase(c);
    }

    public static char toLower(char c) {
        return Character.toLowerCase(c);
    }
}
