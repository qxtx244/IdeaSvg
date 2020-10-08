package com.qxtx.idea.ideasvg.parser;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.qxtx.idea.ideasvg.tools.ScreenUtil;
import com.qxtx.idea.ideasvg.tools.SvgCharUtil;
import com.qxtx.idea.ideasvg.tools.SvgConsts;
import com.qxtx.idea.ideasvg.xmlEntity.ClipPathElement;
import com.qxtx.idea.ideasvg.xmlEntity.GroupElement;
import com.qxtx.idea.ideasvg.xmlEntity.PathData;
import com.qxtx.idea.ideasvg.xmlEntity.PathElement;
import com.qxtx.idea.ideasvg.xmlEntity.VectorXmlInfo;
import com.qxtx.idea.ideasvg.tools.SvgLog;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * CreateDate 2020/5/20 23:48
 * <p>
 *
 * @author QXTX-WIN
 * Description: svg控件的辅助解析类，负责解析xml，.svg文件等工作
 */
public final class VectorXmlParser {

    /** Application cotext */
    private final Context mContext;

    public VectorXmlParser(@NonNull Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * 解析xml中的vector标签数据
     * @param xmlParser 目标xml的解析对象，能方便地获取到xml中的数据
     * @param vectorXmlInfo vector标签的数据集，parser读出的数据保存到此对象中
     */
    public void parseVectorXml(@NonNull XmlResourceParser xmlParser, @NonNull VectorXmlInfo vectorXmlInfo) throws Exception {
        vectorXmlInfo.reset();
        if (xmlParser.isEmptyElementTag()) {
            SvgLog.I("EmptyElementTag！无法解析xml");
            return ;
        }
        if (xmlParser.getEventType() != XmlPullParser.START_DOCUMENT) {
            SvgLog.I("没有以START_DOCUMENT开始，无法解析xml");
            return ;
        }

        String tag;
        //仅支持解析根标签为vector的xml
        while ((xmlParser.next()) != XmlPullParser.END_DOCUMENT) {
            tag = xmlParser.getName();
            if (!TextUtils.isEmpty(tag) && tag.equals(SvgConsts.SUPPORT_ROOT_TAG)) {
                break;
            }
        }
        tag = xmlParser.getName();
        if (!TextUtils.isEmpty(tag) && !SvgConsts.SUPPORT_ROOT_TAG.equals(tag)) {
            SvgLog.I("未找到" + SvgConsts.SUPPORT_ROOT_TAG + "标签");
            return ;
        }
        //解析根标签vector的属性
        parseVectorAttributes(xmlParser, vectorXmlInfo);

        int eventType;
        while ((eventType = xmlParser.next()) != XmlPullParser.END_DOCUMENT) {
            if (eventType != XmlPullParser.START_TAG) {
                continue;
            }

            tag = xmlParser.getName();
            if (TextUtils.isEmpty(tag)) {
                throw new IllegalStateException("空的元素节点！");
            }

            switch (tag) {
                case "path":
                    parsePathElement(xmlParser, vectorXmlInfo);
                    break;
                case "clip-path":
                    parseClipPathElement(xmlParser, vectorXmlInfo.getClipPathElementList());
                    break;
                case "group":
                    parseGroupElement(xmlParser, vectorXmlInfo.getGroupElementList());
                    break;
                default:
                    SvgLog.I("不解析标签[" + tag + "].");
                    break;
            }
        }
    }

    /**
     * 解析一条path标签的pathData属性，得到指令符数据集
     * @param scale 数值的缩放值
     * @param pathData 从xml中得到的path标签的pathData属性字符串
     * @param pathDataList 目标数据列表，保存解析结果
     */
    public static boolean parsePathDataAttribute(float scale, @NonNull String pathData, @NonNull List<PathData> pathDataList) {
        pathDataList.clear();
        if (TextUtils.isEmpty(pathData)) {
            return false;
        }

        SvgLog.I("生成数据集前，字符串=" + pathData);
        pathData = pathData.trim();

        int dataLen = pathData.length();

        //如果起始字符不是指令符M/m，则自动添加一个[M0,0]数据。强制起始指令符大写。
        char startChar = pathData.charAt(0);
        if (!SvgCharUtil.isStartCommand(startChar)) {
            SvgLog.I("自动添加缺少的起始指令符数据。原数据[" + pathData + "].");
            addSubPath(pathDataList, SvgConsts.SVG_START_CMD_UPPER, Arrays.asList(0f, 0f));
        } else {
            if (Character.isLowerCase(startChar)) {
                SvgLog.i("强制起始指令符大写");
                pathData = SvgConsts.SVG_START_CMD_UPPER + pathData.substring(1);
            }
        }

        int pos = 0;
        StringBuilder sb = new StringBuilder();

        //一次循环应该以起始指令符为开始，以结束指令符为结束（或者在字符串结尾，Z/z被忽略）
        //每次循环都会获取到一条指令符轨迹数据（也可能是具有连续相同指令符的多条指令符轨迹数据拼接）完整数据
        while (pos < dataLen) {
            SvgLog.i("一个指令符数据开始位置：" + pos);
            char ch = pathData.charAt(pos);
            //如果是[空格]或[逗号]分隔符，则跳过
            if (ch == ' ' || ch == ',') {
                pos++;
                continue;
            }

            //保证本次循环指向的字符是起始指令符。
            if (!SvgCharUtil.isSvgCommand(ch)) {
                SvgLog.i("数据解析失败：必须以指令符为开始字符。起始有效字符[" + ch + "]，位置[" + pos + "].");
                return false;
            }

            char cmd = ch;
            //如果是结束指令符Z/z，则可以立即完成本次循环任务，进行下一次循环
            if (SvgCharUtil.toUpper(cmd) == SvgConsts.SVG_END_CHAR_UPPER) {
                SvgLog.i("发现结束指令符" + cmd + ", 快速完成本次循环任务");
                addSubPath(pathDataList, cmd, null);
                pos++;
                continue;
            }

            List<Float> valueList = new ArrayList<>();
            sb.delete(0, sb.length());
            //上一个非数值符号是否为小数点
            boolean decimalPointReady = false;
            //上一个非数值符号是否为减号
            boolean negPointReady = false;
            //每一次循环，获取一个指令符的所有数值
            //svg中连续相同的指令符段，可以只写一个指令符，然后不断接上数值。这是时候，获取到的是多段具有相同指令符的轨迹数据
            //并且，[0.xxx]的小数，还有直接省略整数部分的0，直接[.xxx]接在上一个小数后面的写法，于是就有了[1.234.567]这种写法。转换是1.234和0.567两个数值
            //不断获取数值，直到碰到下一个指令符，或者到达字符串结尾，完成遍历
            while (++pos < dataLen) {
                //可能的字符：分隔符，指令符，普通数值。如果碰到空格或者逗号，直接跳过
                //一个数值的结尾标志：分隔符，指令符
                //如果碰到小数点，则记录下来，下一次再碰到小数点，则这个小数点为当前数值的结束标志，并且它是下一个数值的小数点，省略了前面的0字符
                ch = pathData.charAt(pos);

                //碰到指令符，本次指令符轨迹数据已全部获取到，完成本次循环
                if (SvgCharUtil.isSvgCommand(ch)) {
                    SvgLog.I("碰到指令符[" + ch + "]，位置[" + pos + "]. 此时未处理数据[" + sb.toString() + "].");
                    if (sb.length() > 0 && !addValue(valueList, sb.toString(), scale)) {
                        return false;
                    }
                    SvgLog.i("一个指令符的数值获取完成，当前位置：" + pos);
                    break;
                }

                //如果碰到的是[,]或[ ]，此时字符收集器存在数据，则说明一个数值的数据获取完成，并且跳过之后连续的[,]或[ ]
                if (ch == ',' || ch == ' ') {
                    //如果字符收集器没有任何数据，说明当前的分隔符号需要跳过
                    if (sb.length() == 0) {
                        continue;
                    }

                    if (!addValue(valueList, sb.toString(), scale)) {
                        SvgLog.I("解析指令符的数值失败！指令[" + cmd + "]，当前解析位置[" + pos + "].");
                        return false;
                    }

                    SvgLog.i("数值[" + sb.toString() + "]获取完成，获取下一个数值。当前位置[" + pos + "].");
                    sb.delete(0, sb.length());
                    negPointReady = false;
                    decimalPointReady = false;
                }
                //当前字符可能不仅仅为数值的一部分，同时还是分隔符。
                else if (ch == '-') {
                    if (negPointReady || decimalPointReady) {
                        //碰到的是下一个数值的减号，立即完成当前字符的保存
                        if (!addValue(valueList, sb.toString(), scale)) {
                            return false;
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
                        if (!addValue(valueList, sb.toString(), scale)) {
                            return false;
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

                //如果到达结尾，处理当前收集的字符
                if (pos + 1 == dataLen && sb.length() > 0) {
                    String finalStr = sb.toString();
                    SvgLog.i("到达结尾，整个字符串遍历完成，处理当前收集的字符串[" + finalStr + "].");
                    if (!addValue(valueList, finalStr, scale)) {
                        return false;
                    }
                }
            }

            addSubPath(pathDataList, cmd, valueList);
        }
        return true;
    }

    /**
     * 生成Path对象列表
     * @throws Exception 如果数据不正确（如某个指令符携带的数值数量不符合要求），将会导致解析出错，抛出异常
     */
    public static boolean generatePath(@NonNull List<PathData> list, @NonNull Path path) throws Exception {
        path.reset();
        if (list.size() == 0) {
            return false;
        }

        for (int index = 0; index < list.size(); index++) {
            PathData pathData = list.get(index);
            if (pathData == null) {
                continue;
            }

            char cmd = pathData.getCommand();
            if (SvgCharUtil.toUpper(cmd) == SvgConsts.SVG_END_CHAR_UPPER) {
                path.close();
                continue;
            }

            float[] endCoordinate = pathData.getEndCoordinate();
            //可能一个指令符携带了n个相同指令符的数据，如[M 1，2，3，4，5，6]
            List<Float> values = pathData.getValueList();
            int valueCount = values.size();
            if (SvgCharUtil.toUpper(cmd) != SvgConsts.SVG_END_CHAR_UPPER && values.size() == 0) {
                SvgLog.I("非法的指令符数据！");
                continue;
            }

            switch (SvgCharUtil.toUpper(cmd)) {
                case 'M': //2
                    path.moveTo(endCoordinate[0], endCoordinate[1]);
                    break;
                case 'H': //1
                case 'V': //1
                case 'L': //2
                    path.lineTo(endCoordinate[0], endCoordinate[1]);
                    break;
                case 'Q': //4
                    for (int i = 0; i < valueCount; i += 4) {
                        if (i + 3 >= valueCount) {
                            SvgLog.I("无法生成路径：未获取到足够的数值。cmd[" + cmd + "]. values" + Arrays.toString(values.toArray(new Float[0])));
                            return false;
                        }

                        boolean isRefCommand = Character.isLowerCase(cmd);
                        if (isRefCommand) {
                            path.rQuadTo(values.get(i), values.get(i + 1), values.get(i + 2), values.get(i + 3));
                        } else {
                            path.quadTo(values.get(i), values.get(i + 1), values.get(i + 2), values.get(i + 3));
                        }
                    }
                    break;
                case 'C': //6
                    for (int i = 0; i < valueCount; i += 6) {
                        if (i + 5 >= valueCount) {
                            SvgLog.I("无法生成路径：未获取到足够的数值。cmd[" + cmd + "]. values" + Arrays.toString(values.toArray(new Float[0])));
                            return false;
                        }

                        boolean isRefCommand = Character.isLowerCase(cmd);
                        if (isRefCommand) {
                            path.rCubicTo(values.get(i), values.get(i + 1), values.get(i + 2), values.get(i + 3), values.get(i + 4), values.get(i + 5));
                        } else {
                            path.cubicTo(values.get(i), values.get(i + 1), values.get(i + 2), values.get(i + 3), values.get(i + 4), values.get(i + 5));
                        }
                    }
                    break;
                case 'A': //7
                    for (int i = 0; i < valueCount; i += 7) {
                        if (i + 6 >= valueCount) {
                            SvgLog.I("无法生成路径：未获取到足够的数值。cmd[" + cmd + "]. values" + Arrays.toString(values.toArray(new Float[0])));
                            return false;
                        }

                        PathData lastPathData = list.get(index - 1);
                        float x1 = lastPathData.getEndCoordinate()[0];
                        float y1 = lastPathData.getEndCoordinate()[1];
                        double rxHalf = values.get(i);
                        double ryHalf = values.get(i + 1);
                        double phi = values.get(i + 2);
                        double fA = values.get(i + 3);
                        double fS = values.get(i + 4);
                        boolean isRefCommand = Character.isLowerCase(cmd);
                        double x2 = values.get(i + 5) + (isRefCommand ? x1 : 0);
                        double y2 = values.get(i + 6) + (isRefCommand ? y1 : 0);
                        generateEllipticalArcPath(path, x1, y1, rxHalf, ryHalf, phi, fA, fS, x2, y2);
                    }
                    break;
                case 'T': //2
                case 'S': //4
                default:
                    //这里不会出现T/S/t/s指令符，前面已经进行转换了
                    SvgLog.I("无法处理的指令符[" + cmd + "], value" + Arrays.toString(values.toArray(new Float[0])));
                    return false;
            }
        }
        return true;
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
     * 备注：
     *  1、小数计算会有精度问题，可能导致得到的数值变小了。并且，目前可能无法正确解析错误的椭圆弧线数据
     *  2、可能会出现给定的参数无法得到一个椭圆弧线（计算sqrtValue时会得到Double.NaN，也就是说给的参数无法构成一条椭圆弧），
     *      如果倾斜度为0，则可以处理，缩放长短轴即可；
     *      如果存在倾斜度，目前无法知道这个椭圆需要平移或者缩放甚至两者兼有才能符合要求，因此暂时没有办法处理。
     */
    private static void generateEllipticalArcPath(@NonNull Path path, double x1, double y1,
                                          double rxHalf, double ryHalf, double phi, double fA, double fS, double x2, double y2) {

        SvgLog.I(String.format("生成椭圆前坐标(%s, %s), 7个参数rxHalf=%s, ryHalf=%s, phi=%s, fA=%s, fS=%s, x2=%s, y2=%s",
                x1, y1, rxHalf, ryHalf, phi, fA, fS, x2, y2));

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

    /**
     * 解析vector xml中的path标签
     */
    private void parsePathElement(@NonNull XmlResourceParser xmlParser, @NonNull VectorXmlInfo info) {
        List<PathElement> elementList = info.getPathElementList();

        PathElement element = new PathElement();
        int attrCount = xmlParser.getAttributeCount();
        for (int i = 0; i < attrCount; i++) {
            switch (xmlParser.getAttributeName(i)) {
                case "pathData":
                    //需要计算实际的尺寸，即比较width&height 和 viewportWidth&viewportHeight
                    float width = info.getWidth();
                    float height = info.getHeight();
                    float viewportWidth = info.getViewportWidth();
                    float viewportHeight = info.getViewportHeight();
                    if (width <= 0f || height <= 0f) {
                        throw new IllegalStateException("宽高值必须大于0！");
                    }

                    float scale = Math.max(width / viewportWidth, height / viewportHeight);
                    SvgLog.I(String.format("width&height=%s&%s，viewportWidth&viewportHeight=%s&%s,尺寸缩放值%s", width, height, viewportWidth, viewportHeight, scale));
                    element.savePathString(scale, xmlParser.getAttributeValue(i));
                    break;
                case "strokeWidth":
                    float strokeWidth = xmlParser.getAttributeFloatValue(i, element.getStrokeWidth());
                    element.setStrokeWidth(Math.max(0f, strokeWidth));
                    break;
                case "strokeColor":
                    //0为透明
                    int strokeColor = xmlParser.getAttributeIntValue(i, element.getStrokeColor());
                    element.setStrokeColor(strokeColor == 0 ? SvgConsts.INVALID_INT : strokeColor);
                    break;
                case "strokeAlpha":
                    float strokeAlpha = xmlParser.getAttributeFloatValue(i, element.getStrokeAlpha());
                    strokeAlpha = Math.max(0f, strokeAlpha);
                    strokeAlpha = Math.min(1f, strokeAlpha);
                    element.setStrokeAlpha(strokeAlpha);
                    break;
                case "fillAlpha":
                    float fillAlpha = xmlParser.getAttributeFloatValue(i, element.getFillAlpha());
                    fillAlpha = Math.max(0f, fillAlpha);
                    fillAlpha = Math.min(1f, fillAlpha);
                    element.setFillAlpha(fillAlpha);
                    break;
                case "fillColor":
                    //0为透明
                    int fillColor = xmlParser.getAttributeIntValue(i, element.getFillColor());
                    element.setFillColor(fillColor == 0 ? SvgConsts.INVALID_INT : fillColor);
                    break;
                case "name":
                    element.setName(xmlParser.getAttributeValue(i));
                    break;
                case "strokeLineCap":
                    element.setStrokeLineCap(xmlParser.getAttributeIntValue(i, element.getStrokeLineCap()));
                    break;
                case "strokeLineJoin":
                    element.setStrokeLineJoin(xmlParser.getAttributeIntValue(i, element.getStrokeLineJoin()));
                    break;
                case "strokeMiterLimit":
                    element.setStrokeMiterLimit(xmlParser.getAttributeFloatValue(i, element.getStrokeMiterLimit()));
                    break;
                case "trimPathStart":
                    element.setTrimPathStart(xmlParser.getAttributeFloatValue(i, element.getTrimPathStart()));
                    break;
                case "trimPathEnd":
                    element.setTrimPathEnd(xmlParser.getAttributeFloatValue(i, element.getTrimPathEnd()));
                    break;
                case "trimPathOffset":
                    element.setTrimPathOffset(xmlParser.getAttributeFloatValue(i, element.getTrimPathOffset()));
                    break;
            }
        }
        elementList.add(element);
    }

    /**
     * 解析vector xml中的clip-path标签
     */
    private void parseClipPathElement(@NonNull XmlResourceParser xmlParser, List<ClipPathElement> elementList) {
        SvgLog.I("暂不支持解析clip-path标签");
    }

    /**
     * 解析vector xml中的group标签
     */
    private void parseGroupElement(@NonNull XmlResourceParser xmlParser, List<GroupElement> element) {
        SvgLog.I("暂不支持解析group标签");
    }

    /**
     * 解析vector标签的根属性
     */
    private void parseVectorAttributes(@NonNull XmlResourceParser xmlParser, @NonNull VectorXmlInfo info) {
        String value;
        int attrCount = xmlParser.getAttributeCount();
        for (int i = 0; i < attrCount; i++) {
            switch (xmlParser.getAttributeName(i)) {
                case "width":
                    float defaultWidth = info.getWidth();
                    //dimen类型数值字符串
                    float width = ScreenUtil.parseDimenToPx(mContext, xmlParser.getAttributeValue(i), defaultWidth);
                    //控件可能有确定的宽度
                    if (defaultWidth > 0) {
                        width = Math.min(width, defaultWidth);
                    }
                    info.setWidth(Math.max(0f, width));
                    break;
                case "height":
                    float defaultHeight = info.getHeight();
                    //dimen类型数值字符串
                    float height = ScreenUtil.parseDimenToPx(mContext, xmlParser.getAttributeValue(i), defaultHeight);
                    //控件可能有确定的高度
                    if (defaultHeight > 0) {
                        height = Math.min(height, defaultHeight);
                    }
                    info.setHeight(Math.max(0f, height));
                    break;
                case "viewportWidth":
                    //解析可能得到小数或者整数，但肯定是float
                    info.setViewportWidth(xmlParser.getAttributeFloatValue(i, 0f));
                    break;
                case "viewportHeight":
                    //解析可能得到小数或者整数，但肯定是float
                    info.setViewportHeight(xmlParser.getAttributeFloatValue(i, 0f));
                    break;
                case "alpha":
                    info.setAlpha(xmlParser.getAttributeFloatValue(i, 1f));
                    break;
                case "name":
                    info.setName(xmlParser.getAttributeValue(i));
                    break;
                case "tint":
                    //color类型，一种是资源id（@），一种是color字符串（#）。如果是@null，将会被解析成@0（id=0，非法）。或许可以容错一下
                    value = xmlParser.getAttributeValue(i);
                    int tint = info.getTint();
                    char firstChar = value.charAt(0);
                    if (firstChar == '#') {
                        tint = Color.parseColor(value);
                    } else if (firstChar == '@') {
                        int id = Integer.parseInt(value.substring(1));
                        if (id == 0) {
                            tint = Color.TRANSPARENT;
                        } else {
                            tint = mContext.getResources().getColor(id);
                        }
                    }
                    info.setTint(tint);
                    break;
                case "tintMode":
                    info.setTintMode(xmlParser.getAttributeValue(i));
                    break;
                case "autoMirror":
                    info.setAutoMirrored(xmlParser.getAttributeBooleanValue(i, info.isAutoMirrored()));
                    break;
            }
        }
    }

    /**
     * 将一些特殊指令符转换成能被Path表达的指令符。
     * 如T转换成Q后，可使用{@link Path#quadTo(float, float, float, float)}来表达；
     * S转换成C后，可使用{@link Path#cubicTo(float, float, float, float, float, float)}来表达。
     */
    private static boolean addSpecialCommand(@NonNull List<PathData> list, PathData pathData) {
        char cmd = pathData.getCommand();

        List<Float> valueList = pathData.getValueList();
        int valueCount = valueList.size();
        if (valueCount == 0) {
            SvgLog.I("缺少数值的指令符，忽略它");
            return false;
        }

        float x1, y1, x2, y2, x, y;
        switch (SvgCharUtil.toUpper(cmd)) {
            //指令Q的简写，规定前一个指令符应该是Q/T/q/t的一种（为T提供参考数据）。否则T的结果将只是一条直线。
            // 这里不可能是T/t，因为T/t总是会被转换成Q
            case 'T':
                for (int i = 0; i < valueCount; i += 2) {
                    PathData lastPathData = list.get(list.size() - 1);
                    x1 = lastPathData.getEndCoordinate()[0];
                    y1 = lastPathData.getEndCoordinate()[1];

                    //需要转换成绝对坐标
                    boolean isRefCoordinate = Character.isLowerCase(cmd);
                    x = valueList.get(i) + (isRefCoordinate ? x1 : 0);
                    y = valueList.get(i + 1) + (isRefCoordinate ? y1 : 0);

                    char lastCommand = lastPathData.getCommand();
                    if (SvgCharUtil.toUpper(lastCommand) == 'Q') {
                        List<Float> lastValueList = lastPathData.getValueList();
                        int lastValueCount = lastValueList.size();
                        x1 += lastValueList.get(lastValueCount - 2) - lastValueList.get(lastValueCount - 4);
                        y1 += lastValueList.get(lastValueCount - 1) - lastValueList.get(lastValueCount - 3);
                    }

                    list.add(new PathData('Q', Arrays.asList(x1, y1, x, y), x, y));
                }
                break;
            //指令C的简写，规定前一个指令符应该是C/c/S/s的一种（为T提供参考数据），否则S的第一个控制点将认为和前一个指令符的终点坐标相同。
            // 这里不可能是S/s，因为S/s总是会先被转换成C
            case 'S':
                for (int i = 0; i < valueCount; i += 4) {
                    PathData lastPathData = list.get(list.size() - 1);
                    x1 = lastPathData.getEndCoordinate()[0];
                    y1 = lastPathData.getEndCoordinate()[1];

                    //需要转换成绝对坐标
                    boolean isRefCoordinate = Character.isLowerCase(cmd);
                    x2 = valueList.get(i) + (isRefCoordinate ? x1 : 0);
                    y2 = valueList.get(i + 1) + (isRefCoordinate ? y1 : 0);
                    x = valueList.get(i + 2) + (isRefCoordinate ? x1 : 0);
                    y = valueList.get(i + 3) + (isRefCoordinate ? y1 : 0);

                    //前一个指令符不可能是特殊指令符S/s
                    char lastCommand = lastPathData.getCommand();
                    if (SvgCharUtil.toUpper(lastCommand) == 'C') {
                        List<Float> lastValueList = lastPathData.getValueList();
                        int lastValueCount = lastValueList.size();
                        x1 += lastValueList.get(lastValueCount - 2) - lastValueList.get(lastValueCount - 4);
                        y1 += lastValueList.get(lastValueCount - 1) - lastValueList.get(lastValueCount - 3);
                    }

                    list.add(new PathData('C', Arrays.asList(x1, y1, x2, y2, x, y), x, y));
                }
                break;
        }
        return true;
    }

    /** 添加一个数值到数值列表 */
    private static boolean addValue(List<Float> list, String value, float scale) {
//        SvgLog.i("得到一个数值[" + value + "].");
        if (TextUtils.isEmpty(value)) {
            return false;
        }
        try {
            list.add(Float.parseFloat(value) * scale);
        } catch (Exception e) {
            SvgLog.i("解析字符串数值[" + value + "]异常。流程应立即终止。原因=" + e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    /**
     * 添加一个路径指令数据到路径数据列表
     * @param list 路径数据列表
     * @param cmd 指令符。如果为闭合指令符，则数值列表应该为null
     * @param valueList 数值列表。闭合指令符携带的数值列表应该为null
     */
    private static void addSubPath(@NonNull List<PathData> list, char cmd, @Nullable List<Float> valueList) {
        float endX = 0f, endY = 0f;

        //如果是闭合指令符，则找到最近一个起始指令路径数据，以它的终点坐标作为终点坐标
        if (valueList == null) {
            if (SvgCharUtil.toUpper(cmd) == SvgConsts.SVG_END_CHAR_UPPER) {
                SvgLog.I("闭合指令符，寻找终点坐标...");
                for (int i = list.size() - 1; i >= 0; i--) {
                    PathData pathData = list.get(i);
                    if (pathData == null) {
                        continue;
                    }
                    if (SvgCharUtil.toUpper(pathData.getCommand()) == SvgConsts.SVG_START_CMD_UPPER) {
                        float[] lastCoordinate = pathData.getEndCoordinate();
                        endX = lastCoordinate[0];
                        endY = lastCoordinate[1];
                        break;
                    }
                }
                SvgLog.I("闭合指令符终点坐标为(" + endX + ", " + endY + ").");
            } else {
                SvgLog.I("指令符[" + cmd + "]无任何数值，自动丢弃");
                return ;
            }
        } else if (list.size() == 0) {
            if (valueList.size() >= 2) {
                //如果是第一次添加，则肯定是起始指令符数据
                endX = valueList.get(valueList.size() - 2);
                endY = valueList.get(valueList.size() - 1);
            }
        } else {
            PathData lastData = list.get(list.size() - 1);
            float[] lastCoordinate = lastData.getEndCoordinate();
            endX = lastCoordinate[0];
            endY = lastCoordinate[1];

            int valueCount = valueList.size();
            switch (cmd) {
                case 'H': //1
                    endX = valueList.get(valueCount - 1);
                    break;
                case 'V': //1
                    endY = valueList.get(valueCount - 1);
                    break;
                case 'h': //1
                    endX += valueList.get(valueCount - 1);
                    break;
                case 'v': //1
                    endY += valueList.get(valueCount - 1);
                    break;
                case 'M':
                case 'L':
                case 'Q':
                case 'S':
                case 'C':
                case 'T':
                case 'A':
                    endX = valueList.get(valueCount - 2);
                    endY = valueList.get(valueCount - 1);
                    break;
                case 'm': //2
                case 'l':
                case 't':
                    for (int i = 0; i < valueCount; i += 2) {
                        endX += valueList.get(i);
                        endY += valueList.get(i + 1);
                    }
                    break;
                case 's': //4
                case 'q':
                    for (int i = 0; i < valueCount; i += 4) {
                        endX += valueList.get(i + 2);
                        endY += valueList.get(i + 3);
                    }
                    break;
                case 'c': //6
                    for (int i = 0; i < valueCount; i += 6) {
                        endX += valueList.get(i + 4);
                        endY += valueList.get(i + 5);
                    }
                    break;
                case 'a': //7
                    for (int i = 0; i < valueCount; i += 7) {
                        endX += valueList.get(i + 5);
                        endY += valueList.get(i + 6);
                    }
                    break;
                default:
                    SvgLog.I("无法解析的指令符[" + cmd + "].");
                    return ;
            }
        }

        PathData pathData = new PathData(cmd, valueList, endX, endY);

        //如果是特殊的指令，先转换成普通指令，再保存数据
        boolean ret;
        if (SvgCharUtil.isSvgSpecCommand(cmd)) {
            ret = addSpecialCommand(list, pathData);
        } else {
            ret = list.add(pathData);
        }

        if (!ret) {
            SvgLog.I("添加路径指令数据失败了！数据=" + pathData.toString());
        }
    }
}
