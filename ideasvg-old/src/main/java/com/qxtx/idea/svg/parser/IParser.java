package com.qxtx.idea.svg.parser;

import java.util.LinkedHashMap;

/**
 * @author QXTX-WIN
 * @date 2019/12/3 21:38
 * Description:
 */
public interface IParser {

    /**
     * 解析字符串，得到一个可以被使用的数据对集合，里面包含了 指令符-值数组 形式的键值对。
     * 如果解析失败，将会返回null。
     *
     * @param svgData 待解析的字符串
     * @return 若解析成功，则返回解析得到的数据集，否则返回null
     */
    LinkedHashMap<String, float[]> svgString2Map(String svgData);

    /**
     * 反解析svg数据集，得到svg字符串
     * @param svgMap svg数据集
     * @return 可被解析的svg字符串
     */
    String svgMap2String(LinkedHashMap<String, float[]> svgMap);
}
