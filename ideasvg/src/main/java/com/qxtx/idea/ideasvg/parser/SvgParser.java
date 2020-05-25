package com.qxtx.idea.ideasvg.parser;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.qxtx.idea.ideasvg.entity.PathParam;
import com.qxtx.idea.ideasvg.entity.SvgParam;
import com.qxtx.idea.ideasvg.tools.SvgLog;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * CreateDate 2020/5/20 23:48
 * <p>
 *
 * @author QXTX-WIN
 * Description: svg控件的辅助解析类，负责为svg控件解析xml，.svg等工作
 */
public final class SvgParser implements IParser {

    /** 可复用的字符串组装对象 */
    private static final StringBuilder sb = new StringBuilder();

    public SvgParser() { }

    /**
     * 解析xml中的vector标签数据
     * @param xmlParser 目标xml的解析对象，能方便地获取到xml中的数据
     * @param param svg的配置参数集，解析到的数据保存到此对象中
     */
    public void parseVectorXml(@NonNull Resources resources,  @NonNull XmlResourceParser xmlParser, @NonNull SvgParam param) throws Exception {
        //先清空原有的数据
        param.reset();

        if (xmlParser.isEmptyElementTag()) {
            SvgLog.I("EmptyElementTag！无法解析xml");
            return ;
        }

        if (xmlParser.getEventType() != XmlPullParser.START_DOCUMENT) {
            SvgLog.I("没有以START_DOCUMENT开始，无法解析xml");
            return ;
        }

        String tag;
        int attrCount;

        //仅支持根标签为vector的xml
        while ((xmlParser.next()) != XmlPullParser.END_DOCUMENT) {
            tag = xmlParser.getName();
            if (!TextUtils.isEmpty(tag) && tag.equals(SvgConsts.SUPPORT_ROOT_TAG)) {
                break;
            }
        }

        tag = xmlParser.getName();
        if (!TextUtils.isEmpty(tag) && !SvgConsts.SUPPORT_ROOT_TAG.equals(tag)) {
            SvgLog.I("未找到起始的vector标签");
            return ;
        }

        //读取根标签的属性
        String value;
        int viewportSpec;
        float svgAlpha = 1f;
        attrCount = xmlParser.getAttributeCount();
        for (int i = 0; i < attrCount; i++) {
            String name = xmlParser.getAttributeName(i);
            switch (name) {
                case "width":
                    //解析得到"xxxx.0dip",需要去掉后面的.0dip字符串，读出实际int数值
                    value = xmlParser.getAttributeValue(i);
                    if (TextUtils.isEmpty(value)) {
                        param.setWidth(0);
                    } else {
                        int width = Integer.parseInt(value.substring(0, value.length() - 5));
                        param.setWidth(width);
                    }
                    break;
                case "height":
                    //解析得到"xxxx.0dip",需要去掉后面的.0dip字符串，读出实际整数值
                    value = xmlParser.getAttributeValue(i);
                    if (TextUtils.isEmpty(value)) {
                        param.setHeight(0);
                    } else {
                        int height = Integer.parseInt(value.substring(0, value.length() - 5));
                        param.setHeight(height);
                    }
                    break;
                case "viewportWidth":
                    //解析得到"xxxx.0",需要去掉后面的.0字符串，读出实际整数值
                    value = xmlParser.getAttributeValue(i);
                    int viewportWidth = Integer.parseInt(value.substring(0, value.length() - 2));
                    viewportSpec = param.getViewportSpec() | ((viewportWidth << 16) & 0xffff0000);
                    param.setViewportSpec(viewportSpec);

                    if (param.getWidth() == 0) {
                        param.setWidth(viewportWidth);
                    }
                    break;
                case "viewportHeight":
                    //解析得到"xxxx.0",需要去掉后面的.0字符串，读出实际整数值
                    value = xmlParser.getAttributeValue(i);
                    int viewportHeight = Integer.parseInt(value.substring(0, value.length() - 2));
                    viewportSpec = param.getViewportSpec() | (viewportHeight & 0xffff);
                    param.setViewportSpec(viewportSpec);

                    if (param.getHeight() == 0) {
                        param.setHeight(viewportHeight);
                    }
                    break;
                case "alpha":
                    svgAlpha = xmlParser.getAttributeFloatValue(i, 1f);
                    break;
                case "name":
                    param.setName(xmlParser.getAttributeValue(i));
                    break;
                case "tint":
                    value = xmlParser.getAttributeValue(i);
                    int color = 0;
                    char firstChar = value.charAt(0);
                    if (firstChar == '#') {
                        color = Color.parseColor(value);
                    } else if (firstChar == '@') {
                        color = resources.getColor(xmlParser.getAttributeResourceValue(i, Integer.MIN_VALUE));
                    }
                    param.setTint(color);
                    break;
                case "tintMode":
                    param.setTintMode(xmlParser.getAttributeValue(i));
                    break;
                case "autoMirror":
                    param.setAutoMirrored(xmlParser.getAttributeBooleanValue(i, false));
                    break;
            }
        }

        int eventType;
        while ((eventType = xmlParser.next()) != XmlPullParser.END_DOCUMENT) {
            if (eventType != XmlPullParser.START_TAG) {
                continue;
            }

            tag = xmlParser.getName();
            if (TextUtils.isEmpty(tag) || !tag.equals("path")) {
                continue;
            }

            List<PathParam> pathParamList = param.getPathParamList();
            PathParam pathParam = new PathParam();
            pathParamList.add(pathParam);

            attrCount = xmlParser.getAttributeCount();
            for (int i = 0; i < attrCount; i++) {
                String name = xmlParser.getAttributeName(i);
                switch (name) {
                    case "pathData":
                        String pathData = xmlParser.getAttributeValue(i);
                        pathParam.setPathData(pathData);
                        break;
                    case "strokeWidth":
                        pathParam.setStrokeWidth(xmlParser.getAttributeFloatValue(i, pathParam.getStrokeWidth()));
                        break;
                    case "strokeColor":
                        pathParam.setStrokeColor(xmlParser.getAttributeIntValue(i, pathParam.getStrokeColor()));
                        break;
                    case "strokeAlpha":
                        //需要与全局透明度叠加
                        float strokeAlpha = xmlParser.getAttributeFloatValue(i, pathParam.getStrokeAlpha());
                        pathParam.setStrokeAlpha(strokeAlpha * svgAlpha);
                        break;
                    case "fillColor":
                        pathParam.setFillColor(xmlParser.getAttributeIntValue(i, pathParam.getFillColor()));
                        break;
                    case "fillAlpha":
                        //需要与全局透明度叠加
                        float fillAlpha = xmlParser.getAttributeFloatValue(i, pathParam.getFillAlpha());
                        pathParam.setFillAlpha(fillAlpha * svgAlpha);
                        break;
                    case "name":
                        pathParam.setName(xmlParser.getAttributeValue(i));
                        break;
                    case "strokeLineCap":
                        pathParam.setStrokeLineCap(xmlParser.getAttributeIntValue(i, pathParam.getStrokeLineCap()));
                        break;
                    case "strokeLineJoin":
                        pathParam.setStrokeLineJoin(xmlParser.getAttributeIntValue(i, pathParam.getStrokeLineJoin()));
                        break;
                    case "strokeMiterLimit":
                        pathParam.setStrokeMiterLimit(xmlParser.getAttributeFloatValue(i, pathParam.getStrokeMiterLimit()));
                        break;
                    case "trimPathStart":
                        pathParam.setTrimPathStart(xmlParser.getAttributeFloatValue(i, pathParam.getTrimPathStart()));
                        break;
                    case "trimPathEnd":
                        pathParam.setTrimPathEnd(xmlParser.getAttributeFloatValue(i, pathParam.getTrimPathEnd()));
                        break;
                    case "trimPathOffset":
                        pathParam.setTrimPathOffset(xmlParser.getAttributeFloatValue(i, pathParam.getTrimPathOffset()));
                        break;
                }
            }
        }
    }

    /**
     * 解析path字符串，得到path数据集，以生成path对象
     * 遍历pathData，每一次循环，得到一条子路径（包含一个锚点符）；
     * 1、寻找起始的锚点符M
     */
    public static LinkedHashMap<String, List<Float>> parsePathData(@NonNull String pathData) {
        if (TextUtils.isEmpty(pathData)) {
            return null;
        }
        SvgLog.i("生成数据集前，字符串=" + pathData);

        int totalLen = pathData.length();

        LinkedHashMap<String, List<Float>> resultMap = new LinkedHashMap<>();

        //找到起始锚点符M/m，并强制大写，丢弃之前的无效字符
        int pos = findFirstAnchor(pathData);
        if (pos < 0) {
            SvgLog.I("找不到svg的起始锚点符。pathData=[" + pathData + "].");
            return null;
        } else {
            if (Character.isLowerCase(pathData.charAt(pos))) {
                pathData = SvgConsts.SVG_START_CHAR_UPPER + pathData.substring(pos + 1);
                SvgLog.i("字符串发生重组：" + pathData);
                //使pos指向起始锚点符位置
                pos = 0;
            }
        }

        //循环会从一个[M]或[m]开始
        //每次循环都会获取到一个子路径数据（也可能具有连续相同锚点符的多条子路径数据拼接）完整数据
        char ch;
        while (pos < totalLen) {
            SvgLog.i("一次子路径循环开始位置：" + pos);
            ch = pathData.charAt(pos);
            //保证当前指向字符是锚点符。
            //如果是[空格]或[逗号]分隔符，则跳过
            if (ch == ' ' || ch == ',') {
                pos++;
                continue;
            }
            //最终指向一个锚点符
            if (!CharUtil.isAnchor(ch)) {
                SvgLog.i("发现非法字符，解析失败：[" + ch + "]. pos[" + pos + "].");
                return null;
            }
            char anchor = ch;

            //如果是结束锚点符Z/z，则可以立即完成本次循环任务，进行下一次循环
            if (CharUtil.toUpper(anchor) == SvgConsts.SVG_END_CHAR_UPPER) {
                SvgLog.i("发现结束锚点符" + anchor + ", 快速完成本次循环任务");
                addSubPath(resultMap, anchor, null);
                pos++;
                continue;
            }

            //开始向右遍历，直到碰到下一个锚点符，或者到达结尾
            //不断获取数值，直到碰到锚点符，完成遍历
            List<Float> valueList = new ArrayList<>();
            sb.delete(0, sb.length());
            boolean decimalPointReady = false;
            boolean negPointReady = false;
            while (++pos < totalLen) {
                //可能的字符：分隔符，锚点符，普通数值
                //一个数值的结尾标志：空格，逗号，负号，小数点，锚点符
                //如果碰到小数点，则记录下来，下一次再碰到小数点，则这个小数点为当前数值的结束标志，并且它是下一个数值的小数点，省略了前面的0字符
                ch = pathData.charAt(pos);

                if (CharUtil.isAnchor(ch)) {
                    //碰到锚点符，本次子路径数据已全部获取到，完成循环
                    if (sb.length() > 0) {
                        if (!addValue(valueList)) {
                            return null;
                        }
                    }
                    SvgLog.i("一个数值获取完成，当前位置：" + pos);
                    break;
                } else if (CharUtil.maybeValueDelimiter(ch)) {
                    //如果碰到的是[,]或[ ]，①说明一个数值的数据获取完成，并且跳过之后连续的[,]或[ ]
                    //如果碰到的是[-]或[.]，如果前面已经保存过一次此类符号，说明当前子符为分隔符而不是数值字符，否则为数值字符
                    if (ch == ',' || ch == ' ') {
                        if (!addValue(valueList)) {
                            return null;
                        }

                        //跳过后面连续的分隔符
                        int checkPos = pos + 1;
                        while (checkPos < totalLen) {
                            char nextChar = pathData.charAt(checkPos);
                            if (nextChar != ',' && nextChar != ' ') {
                                pos = checkPos - 1;
                                break;
                            }
                            checkPos++;
                        }
                        sb.delete(0, sb.length());
                        negPointReady = false;
                        decimalPointReady = false;
                        SvgLog.i("一个数值获取完成，当前位置：" + pos);
                    } else {
                        boolean isNextValueChar = (ch == '-' && negPointReady) || (ch == '.' && decimalPointReady);
                        //碰到的是[-]或[.]
                        if (isNextValueChar) {
                            //碰到了下一个数值的字符，立即完成当前字符的保存
                            if (!addValue(valueList)) {
                                return null;
                            }

                            sb.delete(0, sb.length());
                            //如果是[.]，说明下一个数值起始的[0]被省略了，自动补上
                            if (ch == '.') {
                                sb.append("0.");
                            }
                            SvgLog.i("一个数值获取完成，当前位置：" + pos);
                        } else {
                            sb.append(ch);
                        }

                        negPointReady = (ch == '-') != negPointReady;
                        decimalPointReady = (ch == '.') != decimalPointReady;
                    }
                } else {
                    sb.append(ch);
                }
            }

            addSubPath(resultMap, anchor, valueList);
        }

        return resultMap;
    }

    /** 添加一个数值到数值列表 */
    private static boolean addValue(List<Float> list) {
        String value = sb.toString();
        SvgLog.i("得到一个数值：" + value);
        try {
            list.add(Float.parseFloat(value));
        } catch (Exception e) {
            SvgLog.i("解析数值异常。cause=" + e.getLocalizedMessage());
            return false;
        }

        return true;
    }

    /**
     * 找到起始的锚点符
     * @param pathData path字符串数据
     * @return 起始锚点符在字符串中的位置，如果没有，则返回Integer.MIN_VALUE
     */
    private static int findFirstAnchor(@NonNull String pathData) {
        int totalLen = pathData.length();

        int pos = 0;
        while (pos < totalLen) {
            char ch = pathData.charAt(pos);
            if (CharUtil.toUpper(ch) == SvgConsts.SVG_START_CHAR_UPPER) {
                break;
            }

            pos++;

            if (pos == totalLen) {
                return Integer.MIN_VALUE;
            }
        }
        SvgLog.i("svg字符串起始锚点符[" + pathData.charAt(pos) + "],位置：" + pos);
        return pos;
    }

    private static void addSubPath(@NonNull LinkedHashMap<String, List<Float>> map, char anchor, List<Float> values) {
        map.put(anchor + "" + map.size(), values);
    }
}
