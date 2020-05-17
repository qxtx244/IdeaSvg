package com.qxtx.idea.ideasvg.parser;

import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.qxtx.idea.ideasvg.SvgConsts;
import com.qxtx.idea.ideasvg.tools.DeepCopy;
import com.qxtx.idea.ideasvg.tools.SvgLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * @author QXTX-WIN
 * @date 2019/12/3 18:54
 *
 * Description:解析Svg数据。
 *  SVG锚点符：M L Q C H V S T Z A (其实A和L一样的效果)
 *
 *  SVG字符串的使用规则：
 *      ！ 一个锚点符和它的数据集构成一个线段，以下称为[一段路径]；
 *      ！ 路径闭合锚点符[Z/z]称为[闭合符]；
 *      1、数值之间使用[空格]/[,]作为分隔符隔开；
 *      2、必须以锚点符[M/m]开头，代表起点；
 *      3、以锚点符[Z/z]结尾，代表闭合路径；没有Z/z结尾，代表非闭合路径；
 *      4、一段路径结束后，可以不加分隔符，直接跟上下一段路径的锚点符或者闭合符，如"L10,10z"，"L10,10L20,20"；
 *      5、html中好像有连续相同的锚点符可以只保留第一个，中间的可以省去；
 *
 *  示例：
 *      1、"M0 0 L50 0 L50 10 L0 10,z M0 20 L50 20 L50,30 L0 30 Z"
 *      2、"M0,20 L50,20 L50,30 L0 30z"
 *
 *  需求：
 *      1、检查svg字符串的正确性；
 *      2、将svg字符串转化成可解析的HashMap数据集，实现绘制；
 *      3、将svg的HashMap数据集转化成svg字符串，作其他用途；
 */
public class SvgDataParser implements IParser {

    private final int INDEX_ERROR = -1;

    private final float INVALID_VALUE = Float.MIN_VALUE;

    /**
     * 对应{@link SvgConsts#SVG_ANCHOR_ALL}的锚点符的值个数
     */
    private int[] ANCHOR_VALUE_NUM = new int[] {1, 1, 1, 1, 2, 2, 2, 2, 0, 0, 4, 4, 4, 4, 6, 6, 6, 6, 6, 6};

    /** 当前的游标位置 */
    private int mCurIndex;

    private final StringBuilder mTempSB = new StringBuilder();

    /** path数据集 */
    private final ArrayList<Path> mPathList;

    /** svg数据集 */
    private final LinkedHashMap<String, float[]> mSvgMap;

    public SvgDataParser() {
        mPathList = new ArrayList<>();
        mSvgMap = new LinkedHashMap<>();
        mCurIndex = 0;
    }

    @Override
    public LinkedHashMap<String, float[]> svgString2Map(@NonNull final String svgString) {
        if (TextUtils.isEmpty(svgString)) {
            SvgLog.I("错误，不是正确的svg字符串");
            return null;
        }

        String data = svgString.trim();
//        SvgLog.I("格式化后的字符串：" + data);

        char startChar = data.charAt(0);
        if (startChar != SvgConsts.SVG_START_ANCHOR) {
            SvgLog.I("错误，不是以起始符开始的字符串: " + startChar);
            mSvgMap.clear();
            return mSvgMap;
        }

        //真正开始解析字符串
        parseImpl(data, mSvgMap);

        return DeepCopy.svgMap(mSvgMap);
    }

    @Override
    public String svgMap2String(final LinkedHashMap<String, float[]> svgMap) {
        return null;
    }

    /**
     * 解析svg数据集，生成path数组，每个path都是一条完整路径
     *   每处理一个键值对，都应该计算一次末端坐标值，以便后面的键值对使用；
     * @see #addSubPath(Path, char, float[], float[])
     */
    public ArrayList<Path> createSvgPath(@NonNull final LinkedHashMap<String, float[]> svgMap) {

        //先清空PathList
        mPathList.clear();

        float[] lastCoordinates = new float[2];

        //遇到M/m，就要更新起始坐标
        Path path = new Path();
        int pos = 0;
        for (String k : svgMap.keySet()) {
            char anchor = k.charAt(0);
            float[] values = svgMap.get(k);
            //防止结束符z携带的值数组为null
            values = values == null ? new float[0] : values;

            //将相对位置起始符m转化成绝对位置起始符M
            //并且将起点设为坐标原点
            if (pos == 0) {
                if (Character.isLowerCase(anchor)) {
                    anchor = 'M';
                    values[0] += lastCoordinates[0];
                    values[1] += lastCoordinates[1];
                }
            }

            //这里会在生成path的同时更新终点坐标lastCoordinates
            boolean isSubPathEnd = addSubPath(path, anchor, values, lastCoordinates);

            pos++;

            //表示遇到结束符Z/z，完成一个path
            if (isSubPathEnd) {
                mPathList.add(path);
                path = new Path();
                pos = 0;
            }
        }
        
        if (mPathList.size() == 0) {
            SvgLog.I("未生成任何path对象");
            return null;
        }
//        SvgLog.I("解析到[" + mPathList.size() + "]个path");

        return mPathList;
    }

    /**
     * 生成path对象
     * 备注：不需要检查lastValues，默认符合需要
     *
     * @param path
     * @param key 锚点符
     * @param values 值数组
     * @param lastValues 末端的坐标值
     * @return 如果无法生成path对象，说明出现异常，即返回null
     */
    private boolean addSubPath(Path path, char key, @NonNull final float[] values, @NonNull float[] lastValues) {
        try {
            switch (key) {
                case 'M':
                    path.moveTo(values[0], values[1]);
                    lastValues[0] = values[0];
                    lastValues[1] = values[1];
                    break;
                case 'm':
                    path.rMoveTo(values[0], values[1]);
                    lastValues[0] += values[0];
                    lastValues[1] += values[1];
                    break;
                case 'H':
                    path.lineTo(values[0], lastValues[1]);
                    lastValues[0] = values[0];
                    break;
                case 'h':
                    path.rLineTo(values[0], 0f);
                    lastValues[0] += values[0];
                    break;
                case 'V':
                    path.lineTo(lastValues[0], values[0]);
                    lastValues[1] = values[0];
                    break;
                case 'v':
                    path.rLineTo(0f, values[0]);
                    lastValues[1] += values[0];
                    break;
                case 'L':
                    path.lineTo(values[0], values[1]);
                    lastValues[0] = values[0];
                    lastValues[1] = values[1];
                    break;
                case 'l':
                    path.rLineTo(values[0], values[1]);
                    lastValues[0] += values[0];
                    lastValues[1] += values[1];
                    break;
                case 'Q':
                case 'T':
                    path.quadTo(values[0], values[1], values[2], values[3]);
                    lastValues[0] = values[2];
                    lastValues[1] = values[3];
                    break;
                case 'q':
                case 't':
                    path.rQuadTo(values[0], values[1], values[2], values[3]);
                    lastValues[0] += values[2];
                    lastValues[1] += values[3];
                    break;
                case 'C':
                case 'S':
                    path.cubicTo(values[0], values[1], values[2], values[3], values[4], values[5]);
                    lastValues[0] = values[4];
                    lastValues[1] = values[5];
                    break;
                case 'c':
                case 's':
                    path.rCubicTo(values[0], values[1], values[2], values[3], values[4], values[5]);
                    lastValues[0] += values[4];
                    lastValues[1] += values[5];
                    break;
                case 'A':
                case 'a':
                    //LYX_TAG 2019/12/24 23:07 这里没计算终点坐标
                    RectF rectF = new RectF(values[0], values[1], values[2], values[3]);
                    path.arcTo(rectF, values[4], values[5]);
                    break;
                case 'Z':
                case 'z':
                    path.close();
                    return true;
            }
        } catch (Exception e) {
            SvgLog.I("生成路径对象时发生异常：" + e.getLocalizedMessage());
            e.printStackTrace();
        }

        return false;
    }

    private boolean checkValueNum(int valueNum, int compareNum) {
        if (valueNum < compareNum) {
            SvgLog.I("发现不正确的子路径值数目：需要" + compareNum + "，实际" + valueNum);
            return false;
        }
        return true;
    }

    /**
     * 解析字符串数据
     *
     * 在调用这个方法之前，已经确保字符串以起始符开始；
     * 每次循环都将寻找子路径数据集；
     * 一次循环可能找到多条连续的同锚点符的子路径；
     */
    private void parseImpl(@NonNull String data, @NonNull final LinkedHashMap<String, float[]> map) {
        if (map.size() > 0) {
            map.clear();
        }

        int maxLen = data.length();
        //每次循环开始，i都应该指向一个锚点符的下一位
        for (int i = 0; i < maxLen; i = ++mCurIndex) {
            mCurIndex = i;
            int lIndex = i;
            char startChar = data.charAt(lIndex);
//            SvgLog.I("当前起始符：" + startChar);

            //非法字符
            //LYX_TAG 2019/12/9 23:24 非法字符是应该忽略掉 还是 直接视为错误？
            if (!isSvgChar(startChar)) {
                SvgLog.I("错误，发现非法字符：" + startChar + ", 位置：" + lIndex);
                map.clear();
                return ;
            }

            //忽略起始的分隔符（逗号）
            if (startChar == SvgConsts.SEPARATOR) {
                continue;
            }

            //解析出正确的字符串，并且此字符串不包含锚点符，且不存在连续的分隔符和空格
            //这个方法已经将当前位置mCurIndex更新为当前子路径的末尾位置；
            String subPathData = getPathData(lIndex + 1, data);
            if (subPathData == null) {
                SvgLog.I("错误，解析失败");
                map.clear();
                return ;
            }

            //和锚点符拼接
            subPathData = startChar + subPathData;
//            SvgLog.I("解析到的子路径数据为：" + subPathData, "PEEK");

            boolean success = saveToMap(subPathData, map);
            if (!success) {
                SvgLog.I("错误，解析失败。当前位置：" + i + ", 字符=" + data.charAt(i));
                map.clear();
                return ;
            }

//            SvgLog.I("一次子路径解析结束，末端位置：" + mCurIndex
//                    + ", 下一个字符：" + ((mCurIndex + 1) >= maxLen ? "OUT_OF_BOUND!" : data.charAt(mCurIndex)));
        }
    }

    /**
     * 获取从起始位置开始，到字符串末端或者第一个锚点符为止 的字符串。
     *
     * @param startIndex 起始位置
     * @return 如果过程中发现异常情况，则返回null；否则返回解析到的字符串，并且此字符串不存在连续的分隔符
     */
    private String getPathData(int startIndex, @NonNull String data) {
        int maxLen = data.length();

        //如果开始位置越界或者为负数，则为异常
        if (startIndex < 0 || startIndex > maxLen) {
            SvgLog.I("错误，发现非法的起始位置");
            return null;
        }

        //到达字符串末端。只有一种情况：①发现结束符Z
        if (startIndex == maxLen) {
//            SvgLog.I("到达字符串末端");
            return "";
        }

        //LYX_TAG 2019/12/10 0:14 是直接返回位置再在外面一次性取数据快，还是在这里使用一个StringBuilder对象来即查即存快？
        mTempSB.delete(0, mTempSB.length());

        //可能有两种情况（都能返回正确位置）：
        // ①找不到，则认为一直到末端都是有效的；（此时可能包含了多条拥有相同锚点符的连续子路径，这是一种发现的svg简略写法）
        // ②找到锚点符，返回锚点符前一个位置。
        boolean isSeparatorBefore = false;
        mCurIndex = maxLen - 1;
        for (int i = startIndex; i < maxLen; i++) {
            char c = data.charAt(i);
            if (!isSvgChar(c)) {
                SvgLog.I("错误，发现非法字符");
                return null;
            }

            //记录上一个是否为分隔字符，如果是，则忽略下一个连续的分隔字符
            boolean isDivider = c == SvgConsts.SEPARATOR || c == ' ';
            if (isSeparatorBefore && isDivider) {
                SvgLog.I("忽略连续的分隔符");
                continue;
            }
            isSeparatorBefore = isDivider;

            //注意将空格也视为分隔字符，并且使用逗号分隔符代替
            if (!isAnchor(c)) {
                mTempSB.append(isDivider ? SvgConsts.SEPARATOR : c);
            } else {
//                SvgLog.I("右端找到锚点符");
                mCurIndex = i - 1;
                break;
            }
        }

        return mTempSB.toString();
    }

    /**
     * 将一条子路径的数据解析为数据集，添加到svg数据集中
     * 外面已经排除了非法字符，因此这里不再做处理
     *
     * @param pathData 子路径字符串，需要进一步解析成数据集
     * @param map svg数据集
     */
    private boolean saveToMap(String pathData, final HashMap<String, float[]> map) {
        char anchor = pathData.charAt(0);

        int paramsNeed = getAnchorParamNum(anchor);
        if (paramsNeed == INDEX_ERROR) {
            SvgLog.I("无法识别的锚点符：" + anchor);
            map.clear();
            return false;
        }

        float[] paramArray = new float[paramsNeed];

//        SvgLog.I("锚点符：" + anchor + ", 值个数：" + paramsNeed, "PEEK");

        int maxLen = pathData.length();
        //将字符串中的所有值解析出来
        for (int i = 1; i < maxLen; i++) {
            char c = pathData.charAt(i);

            //不允许以分隔符为数值左端
            if (c == SvgConsts.SEPARATOR) {
                continue;
            }

            mTempSB.delete(0, mTempSB.length());

            int rIndex = i;
            //这里只有分隔符和值相关的字符，并且只会以值相关的字符为数值左端
            //找到一个分隔符就转化一个值
            while (c != SvgConsts.SEPARATOR) {
                mTempSB.append(c);

                //到达结尾，退出循环
                if (rIndex == maxLen - 1) {
                    break;
                }

                rIndex++;
                c = pathData.charAt(rIndex);
            }

            //while循环出来后，只需要解析sb的数据就行了。
            float num = parseNum(mTempSB.toString());
//            SvgLog.I("转换字符串得到一个值：" + num, "PEEK");
            if (num == INVALID_VALUE) {
                SvgLog.I("错误，解析值失败");
                map.clear();
                return false;
            }

            //支持连续相同锚点符的子路径的简略写法（省略第一条子路径之后的锚点符）
            //如果值已经够了，但仍未结束，则说明存在连续且相同锚点符的子路径，但字符串中省略了锚点符
            if (paramsNeed == 0) {
                SvgLog.I("已得到足够的值，但仍存在值，说明有相同锚点符的多条子路径");
                map.put(anchor + "" + map.size(), paramArray);
                paramsNeed = paramArray.length;
            }

            paramArray[paramArray.length - paramsNeed] = num;
            paramsNeed--;

            // 此时i为值的左端，rIndex为分隔符位置或字符串末端，c是末端字符
            i = rIndex;
        }

        //如果未能找到足够的值，则视为错误
        if (paramsNeed > 0) {
            SvgLog.I("错误，未能找到足够的值。锚点符：" + anchor + ", found=" + paramsNeed + ", need=" + getAnchorParamNum(anchor));
            map.clear();
            return false;
        }

        map.put(anchor + "" + map.size(), paramArray);
        
        return true;
    }

    /** 解析出一个值 */
    private float parseNum(@NonNull String data) {
        float num;
        try {
            num = Float.parseFloat(data);
        } catch (Exception e) {
            SvgLog.I("解析值的时候出现异常：" + e.getLocalizedMessage());
            e.printStackTrace();
            num = INVALID_VALUE;
        }
        return num;
    }

    /** 获得指定锚点符子路径的值个数 */
    private int getAnchorParamNum(char anchor) {
        int ret = INDEX_ERROR;

        int index = SvgConsts.SVG_ANCHOR_ALL.indexOf(anchor);
        if (index != -1) {
            ret = ANCHOR_VALUE_NUM[index];
        }
        return ret;
    }

    private boolean isSvgChar(char c) {
        return SvgConsts.SVG_CHAR_ALL.indexOf(c) >= 0;
    }

    private boolean isAnchor(char c) {
        return SvgConsts.SVG_ANCHOR_ALL.indexOf(c) >= 0;
    }
}
