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
    public static LinkedHashMap<String, float[]> parsePathData(@NonNull String pathData) {
        if (TextUtils.isEmpty(pathData)) {
            return null;
        }

        SvgLog.i("生成数据集前，字符串=" + pathData);

        int totalLen = pathData.length();

        LinkedHashMap<String, float[]> resultMap = new LinkedHashMap<>();

        //寻找起始锚点符M/m
        int pos = 0;
        while (pos < totalLen) {
            char ch = pathData.charAt(pos);
            if (CharUtil.toUpper(ch) == SvgConsts.SVG_START_CHAR_UPPER) {
                //将第一个锚点符强制转换为大写，并丢弃之前的无效字符
                if (!Character.isUpperCase(ch)) {
                    SvgLog.i("发生事件：强制将起始锚点符转换成大写，并丢弃它之前的无效字符");
                    pathData = SvgConsts.SVG_START_CHAR_UPPER + pathData.substring(pos + 1);
                }
                break;
            }

            pos++;
            if (pos == totalLen) {
                SvgLog.I("无法解析的path字符串！pathData=[" + pathData + "].");
                return null;
            }
        }

        //使pos指向起始锚点符位置
        pos = 0;

        //循环会从一个[M]或[m]开始
        //每一次循环的开始，都会指向一个锚点符
        char lastAnchor;
        while (pos < totalLen) {
            char ch = pathData.charAt(pos);

            if (!CharUtil.isSvgChar(ch)) {
                return null;
            }

            //保证当前指向字符是锚点符，如果是空格或逗号分隔符，则跳过，如果是数值字符，则认为锚点符被省略了，自动添加锚点符
            if (CharUtil.maybeValueDelimiter(ch)) {
                if (ch == ' ' || ch == ',') {
                    pos++;
                    continue;
                } else {

                }
            }

            char anchor = ch;

            //如果是结束锚点符，则可以立即完成本次循环任务，进行下一次循环
            if (CharUtil.toUpper(anchor) == SvgConsts.SVG_END_CHAR_UPPER) {
                SvgLog.i("发现结束锚点符" + anchor + ", 快速完成本次循环任务");
                addSubPath(resultMap, anchor, null);
                pos++;
                continue;
            }

            //获取锚点符所需数值的个数
            if (valuesNeed < 0) {
                SvgLog.i("path字符串中存在非法锚点符！锚点符[" + anchor + "].");
                return null;
            }

            //开始向右遍历，取出足够个数的值
            //一个数值的结尾标志：空格，逗号，负号，小数点，锚点符
            //如果碰到小数点，则记录下来，下一次再碰到小数点，则这个小数点为当前数值的结束标志，并且它是下一个数值的小数点，省略了前面的0字符
            //每一次循环，都会得到一个数值，并且循环开始时游标指向一个分隔符。如果发现是小数点，则先添加[0.]，再继续逻辑
            float[] values = new float[valuesNeed];
            for (int i = 0; i < valuesNeed; i++) {
                //直到获取一个完整的数值
                sb.delete(0, sb.length());
                boolean decimalPointReady = false;
                boolean minusSignReady = false;

                if (pathData.charAt(pos) == '.') {
                    sb.append("0.");
                }

                //分隔符下一个不能是 空格或逗号分隔符
                while (pathData.charAt(pos + 1) == ' ' || pathData.charAt(pos + 1) == ',') {
                    pos++;
                }

                while (pos < totalLen) {
                    if (pos + 1 == totalLen) {
                        SvgLog.i("path字符串不完整！pathData=" + pathData);
                        return null;
                    }
                    char ch = pathData.charAt(++pos);

                    //可能碰到分隔符，则认为一个数值的字符串已经获取完成
                    //小数点可能是分隔符（如果前面已经存在小数点），也可能不是。
                    if (CharUtil.maybeValueDelimiter(ch)) {
                        //特殊情况1，小数点不属于数值分隔符
                        if (ch == '.' && !decimalPointReady) {
                            decimalPointReady = true;
                            sb.append(ch);
                            continue;
                        } else if (ch == '-' && !minusSignReady) {
                            //特殊情况2：负数符号不属于数值分隔符
                            minusSignReady = true;
                            sb.append(ch);
                            continue;
                        }

                        String valueStr = sb.toString();
                        SvgLog.i("数值：" + valueStr);
                        if (valueStr.charAt(0) == '.') {
                            valueStr = "0" + valueStr;
                        }
                        try {
                            values[i] = Float.parseFloat(valueStr);
                        } catch (Exception e) {
                            SvgLog.i("解析数值异常。cause=" + e.getLocalizedMessage());
                            return null;
                        }

                        break;
                    } else {
                        sb.append(ch);
                    }
                }
            }

            addSubPath(resultMap, anchor, values);

            lastAnchor = anchor;
        }

        return resultMap;
    }


    private static void addSubPath(@NonNull LinkedHashMap<String, float[]> map, char anchor, float[] values) {
        map.put(anchor + "" + map.size(), values);
    }
}
