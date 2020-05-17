package com.qxtx.idea.ideasvg.tools;

import java.util.Arrays;
import java.util.LinkedHashMap;

/**
 * @author QXTX-WIN
 * @date 2019/12/29 0:11
 * Description: 深拷贝操作
 */
public class DeepCopy {

    /** 深拷贝svg数据集 */
    public static LinkedHashMap<String, float[]> svgMap(LinkedHashMap<String, float[]> map) {
        if (map == null) {
            return null;
        }

        LinkedHashMap<String, float[]> clone = new LinkedHashMap<>();

        if (map.size() != 0) {
            float[] values;
            for (String k : map.keySet()) {
                values = map.get(k);
                clone.put(k, Arrays.copyOf(values, values.length));
            }
        }
        return clone;
    }
}
