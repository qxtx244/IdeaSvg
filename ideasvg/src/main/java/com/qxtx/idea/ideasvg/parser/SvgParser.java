package com.qxtx.idea.ideasvg.parser;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.lolaage.common.util.ScreenSizeUtil;
import com.qxtx.idea.ideasvg.entity.PathParam;
import com.qxtx.idea.ideasvg.entity.SvgParam;
import com.qxtx.idea.ideasvg.tools.SvgLog;

import org.xmlpull.v1.XmlPullParser;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.WeakHashMap;

/**
 * CreateDate 2020/5/20 23:48
 * <p>
 *
 * @author QXTX-WIN
 * Description: svg控件的辅助解析类，负责解析xml，.svg文件等工作
 */
public final class SvgParser implements IParser {

    /** Application cotext */
    private final Context mContext;

    public SvgParser(@NonNull Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * 解析xml中的vector标签数据
     * @param xmlParser 目标xml的解析对象，能方便地获取到xml中的数据
     * @param param svg的配置参数集，解析到的数据保存到此对象中
     */
    public void parseVectorXml(@NonNull Resources resources, @NonNull XmlResourceParser xmlParser, @NonNull SvgParam param) throws Exception {
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

        //解析根标签属性
        String value;
        float svgAlpha = 1f;
        attrCount = xmlParser.getAttributeCount();
        for (int i = 0; i < attrCount; i++) {
            String name = xmlParser.getAttributeName(i);
            switch (name) {
                case "width":
                    //dimen类型数值字符串，如果当前svg的尺寸仍未被确定，则转换成px后使用
                    value = xmlParser.getAttributeValue(i);
                    if (TextUtils.isEmpty(value)) {
                        break;
                    }
                    float width = param.getWidth();
                    if (width == 0 || width == ViewGroup.LayoutParams.WRAP_CONTENT) {
                        param.setWidth(parseDimenToPx(value, width));
                    }
                    break;
                case "height":
                    //dimen类型数值字符串，如果当前svg的尺寸仍未被确定，则转换成px后使用
                    value = xmlParser.getAttributeValue(i);
                    if (TextUtils.isEmpty(value)) {
                        break;
                    }
                    float height = param.getHeight();
                    if (height == 0 || height == ViewGroup.LayoutParams.WRAP_CONTENT) {
                        param.setWidth(parseDimenToPx(value, height));
                    }
                    break;
                case "viewportWidth":
                    //解析可能得到小数或者整数，但肯定是数值
                    param.setViewportWidth(xmlParser.getAttributeFloatValue(i, 0));
                    break;
                case "viewportHeight":
                    //解析可能得到小数或者整数，但肯定是数值
                    param.setViewportHeight(xmlParser.getAttributeFloatValue(i, 0));
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

        List<PathParam> pathParamList = param.getPathParamList();
        //解析子标签
        int eventType;
        while ((eventType = xmlParser.next()) != XmlPullParser.END_DOCUMENT) {
            if (eventType != XmlPullParser.START_TAG) {
                continue;
            }

            tag = xmlParser.getName();
            //只解析path标签
            if (TextUtils.isEmpty(tag) || !tag.equals("path")) {
                continue;
            }

            PathParam pathParam = new PathParam();
            attrCount = xmlParser.getAttributeCount();
            for (int i = 0; i < attrCount; i++) {
                switch (xmlParser.getAttributeName(i)) {
                    case "pathData":
                        pathParam.parsePathData(xmlParser.getAttributeValue(i));
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
                    case "fillAlpha":
                        //需要与全局透明度叠加
                        float fillAlpha = xmlParser.getAttributeFloatValue(i, pathParam.getFillAlpha());
                        pathParam.setFillAlpha(fillAlpha * svgAlpha);
                        break;
                    case "fillColor":
                        pathParam.setFillColor(xmlParser.getAttributeIntValue(i, pathParam.getFillColor()));
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
            pathParamList.add(pathParam);
        }
    }

    /**
     * 解析一条path标签的pathData属性，得到path数据集
     * 遍历pathData，每一次循环，得到一条锚点轨迹（包含一个锚点符）；
     * @param pathData 从svg/xml中得到的path标签的pathData属性字符串
     */
    public static LinkedHashMap<String, List<Float>> parsePathData(@NonNull String pathData) {
        if (TextUtils.isEmpty(pathData)) {
            return null;
        }
        SvgLog.i("生成数据集前，字符串=" + pathData);

        int totalLen = pathData.length();

        LinkedHashMap<String, List<Float>> resultMap = new LinkedHashMap<>();

        //如果起始字符不是锚点符M/m，则不做解析。强制大写m
        char startChar = pathData.charAt(0);
        if (CharUtil.isStartAnchor(startChar)) {
            SvgLog.I(String.format("pathData必须以“%s/%s”开头！pathData=[%s].", SvgConsts.SVG_START_CHAR_UPPER, CharUtil.toLower(SvgConsts.SVG_START_CHAR_UPPER), pathData));
            return null;
        }

        int pos = 0;
        if (Character.isLowerCase(startChar)) {
            pathData = SvgConsts.SVG_START_CHAR_UPPER + pathData.substring(1);
            SvgLog.i("强制大写起始锚点符");
        }

        StringBuilder sb = new StringBuilder();

        //一次循环应该以起始锚点符为开始，以结束锚点符为结束（或者在字符串结尾，Z/z被忽略）
        //每次循环都会获取到一条锚点轨迹数据（也可能是具有连续相同锚点符的多条锚点轨迹数据拼接）完整数据
        char ch;
        while (pos < totalLen) {
            SvgLog.i("一次子路径开始位置：" + pos);
            ch = pathData.charAt(pos);
            //如果是[空格]或[逗号]分隔符，则跳过
            if (ch == ' ' || ch == ',') {
                pos++;
                continue;
            }

            //保证本次循环指向的字符是起始锚点符。
            if (!CharUtil.isAnchor(ch)) {
                SvgLog.i("必须以锚点符为开始。解析失败：起始有效字符[" + ch + "]，pos[" + pos + "].");
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

            //每一次循环，获取一个锚点符的所有数值。
            //svg中连续相同的锚点符段，可以只写一个锚点符，然后不断接上数值。这是时候，获取到的是多段具有相同锚点符的轨迹数据
            //并且，[0.xxx]的小数，还有直接省略整数部分的0，直接[.xxx]接在上一个小数后面的写法，于是就有了[1.234.567]这种写法。转换是1.234和0.567两个数值
            //不断获取数值，直到碰到下一个锚点符，或者到达字符串结尾，完成遍历
            List<Float> valueList = new ArrayList<>();
            sb.delete(0, sb.length());
            //上一个非数值符号是否为小数点
            boolean decimalPointReady = false;
            //上一个非数值符号是否为减号
            boolean negPointReady = false;
            while (++pos < totalLen) {
                //可能的字符：分隔符，锚点符，普通数值。如果碰到空格或者逗号，直接跳过
                //一个数值的结尾标志：分隔符，锚点符
                //如果碰到小数点，则记录下来，下一次再碰到小数点，则这个小数点为当前数值的结束标志，并且它是下一个数值的小数点，省略了前面的0字符
                ch = pathData.charAt(pos);

                //碰到锚点符，本次锚点轨迹数据已全部获取到，完成本次循环
                if (CharUtil.isAnchor(ch)) {
                    if (sb.length() > 0 && !addValue(valueList, sb.toString())) {
                        return null;
                    }
                    SvgLog.i("一个锚点的数值获取完成，当前位置：" + pos);
                    break;
                }

                //如果碰到的是[,]或[ ]，说明一个数值的数据获取完成，并且跳过之后连续的[,]或[ ]
                if (ch == ',' || ch == ' ') {
                    if (!addValue(valueList, sb.toString())) {
                        return null;
                    }

                    //跳过之后连续的[,]和[ ]分隔符
                    int checkPos = pos + 1;
                    while (checkPos < totalLen) {
                        char nextChar = pathData.charAt(checkPos);
                        if (nextChar != ',' && nextChar != ' ') {
                            //外层循环会主动位置右移，因此这里不能做位置右移，需要复位
                            pos = checkPos - 1;
                            break;
                        }
                        checkPos++;
                    }

                    sb.delete(0, sb.length());
                    negPointReady = false;
                    decimalPointReady = false;
                    SvgLog.i("一个数值获取完成，获取下一个数值。当前位置[" + pos + "].");
                }
                //当前字符可能不仅仅为数值的一部分，同时还是分隔符。
                else if (ch == '-') {
                    if (negPointReady || decimalPointReady) {
                        //碰到的是下一个数值的减号，立即完成当前字符的保存
                        if (!addValue(valueList, sb.toString())) {
                            return null;
                        }
                        sb.delete(0, sb.length());
                        pos--;
                    }
                    sb.append(ch);
                    negPointReady = !negPointReady;
                    decimalPointReady = false;
                }
                //当前字符可能不仅仅为数值的一部分，同时还是分隔符。
                else if (ch == '.') {
                    if (decimalPointReady) {
                        //碰到的是下一个数值的逗号，立即完成当前字符的保存
                        if (!addValue(valueList, sb.toString())) {
                            return null;
                        }
                        sb.delete(0, sb.length());
                        sb.append("0");
                        pos--;
                    } else {
                        sb.append(ch);
                    }
                    decimalPointReady = !decimalPointReady;
                    negPointReady = false;
                }
                //普通数字
                else {
                    sb.append(ch);
                }
            }

            addSubPath(resultMap, anchor, valueList);
        }

        return resultMap;
    }

    /**
     * 生成椭圆弧路径，目前只能解析合法的数值，没有自动数值修正
     * @param path svg路径对象
     * @param x1 椭圆弧的起始x坐标
     * @param y1 椭圆弧的起始y坐标
     * @param rxHalf 长半轴
     * @param ryHalf 短半轴
     * @param phi 椭圆弧所在椭圆的自身x轴与画布x轴的夹角，以顺时针方向
     * @param fA 1表示使用大角度圆心角对应的椭圆弧，0表示小角度圆心角对应的椭圆弧
     * @param fS 1表示椭圆弧走向为沿顺时针方向，0为沿逆时针方向
     * @param x2 椭圆弧的终点x坐标
     * @param y2 椭圆弧的终点y坐标
     *
     * 备注：小数计算会有精度问题，可能导致得到的数值变小了
     *       目前可能无法正确解析错误的椭圆弧线数据
     */
    public void generateEllipticalArcPath(@NonNull Path path, double x1, double y1,
                                          double rxHalf, double ryHalf, double phi, double fA, double fS, double x2, double y2) {

        if (rxHalf == ryHalf) {
            phi = 0;
        } else {
            phi %= 360;
        }

        //对于倾斜度为0的椭圆，如果两点之间的距离s大于长轴，则将长轴增大到s，并且短轴比做等比增大。
        double dist = Math.abs(x2 - x1);
        double longAxis = 2 * rxHalf;
        if (phi == 0 && dist > longAxis) {
            rxHalf = dist / 2;
            double zoom = dist / longAxis;
            ryHalf *= zoom;
        }

        double cosPhi = Math.cos(phi);
        double sinPhi = Math.sin(phi);

        double deltaXHalf = (x1 - x2) / 2;
        double deltaYHalf = (y1 - y2) / 2;
        double x11 = cosPhi * deltaXHalf + sinPhi * deltaYHalf;
        double y11 = (-sinPhi) * deltaXHalf + cosPhi * deltaYHalf;

        double rxy11Pow2 = Math.pow(rxHalf * y11, 2);
        double ryx11Pow2 = Math.pow(ryHalf * x11, 2);
        double sqrtValue = Math.sqrt(Math.abs((Math.pow(rxHalf * ryHalf, 2) - rxy11Pow2 - ryx11Pow2) / (rxy11Pow2 + ryx11Pow2)));
        if (Double.isNaN(sqrtValue)) {
            SvgLog.e("发生异常！无法解析的椭圆弧路径");
            path.lineTo((float)x2, (float)y2);
            return ;
        }

        double cxx = sqrtValue * rxHalf * y11 / ryHalf;
        double cyy = sqrtValue * (-ryHalf) * x11 / rxHalf;
        if (fA == fS) {
            cxx *= -1;
            cyy *= -1;
        }

        //得到中心坐标
        double cx = cosPhi * cxx - sinPhi * cyy + ((x1 + x2) / 2);
        double cy = sinPhi * cxx + cosPhi * cyy + ((y1 + y2) / 2);

        SvgLog.i("数值：x11,y11=" + x11 + "," + y11 + "  cxx,cyy=" + cxx + "," + cyy + "  cx,xy=" + cx + "," + cy + ", sqrtValue=" + sqrtValue);

        //通过x1 —> x2的x坐标值递增，得到对应的y坐标值，逐个lineTo，细粒度为1°
        path.moveTo((float)x1, (float)y1);

        double x, y;
        double deltaValue = (x2 - x1) / 180;
        for (x = x1; x <= x2; x += deltaValue) {
            y = cy + Math.sqrt(1 - Math.pow(x - cx, 2) / Math.pow(rxHalf, 2)) * ryHalf;
            path.lineTo((float)x, (float)y);
        }
    }

    /** 添加一个数值到数值列表 */
    private static boolean addValue(List<Float> list, String value) {
        SvgLog.i("得到一个数值：" + value);
        try {
            list.add(Float.parseFloat(value));
        } catch (Exception e) {
            SvgLog.i("解析字符串数值异常。流程应立即终止。cause=" + e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    /**
     * 找到起始的锚点符M
     * @param pathData path字符串数据
     * @return 起始锚点符在字符串中的位置，如果没有，则返回Integer.MIN_VALUE
     */
    private static int findFirstAnchor(@NonNull String pathData) {
        int totalLen = pathData.length();

        int pos = 0;
        while (pos < totalLen) {
            char ch = pathData.charAt(pos);
            pos++;

            if (pos == totalLen) {
                return Integer.MIN_VALUE;
            }
        }
        SvgLog.i("svg字符串起始锚点符[" + pathData.charAt(pos) + "],位置：" + pos);
        return pos;
    }

    /**
     * 添加一条子路径到路径数据集
     * @param map 路径集
     * @param anchor 锚点符
     * @param values 路径数值列表
     */
    private static void addSubPath(@NonNull LinkedHashMap<String, List<Float>> map, char anchor, List<Float> values) {
        map.put(anchor + "" + map.size(), values);
    }

    /**
     * 将xml中的dimen值转换成px
     * @param dimen 带dimen单位的数值字符串
     * @param defValue 默认数值
     * @return
     */
    //LYX_TAG 2020/9/17 22:33 这里需要换成float类型去转换
    private float parseDimenToPx(String dimen, float defValue) {
        float result = defValue;
        int len = dimen.length();
        float v;
        if (dimen.endsWith("dip")) {
            v = Float.parseFloat(dimen.substring(0, len - 4));
            result = ScreenSizeUtil.dp2Px(mContext, (int)v);
        } else if (dimen.endsWith("dp")) {
            v = Float.parseFloat(dimen.substring(0, len - 3));
            result = ScreenSizeUtil.dp2Px(mContext, (int)v);
        } else if (dimen.endsWith("px")) {
            v = Float.parseFloat(dimen.substring(0, len - 3));
            result = v;
        }
        return Math.max(0, result);
    }
}
