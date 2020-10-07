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
import com.qxtx.idea.ideasvg.xmlEntity.VectorElement;
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
     * 解析vector xml中的path标签
     */
    private void parsePathElement(@NonNull XmlResourceParser xmlParser, List<PathElement> elementList) {
        PathElement element = new PathElement();

        int attrCount = xmlParser.getAttributeCount();
        for (int i = 0; i < attrCount; i++) {
            switch (xmlParser.getAttributeName(i)) {
                case "pathData":
                    element.setPathString(xmlParser.getAttributeValue(i));
                    break;
                case "strokeWidth":
                    element.setStrokeWidth(xmlParser.getAttributeFloatValue(i, element.getStrokeWidth()));
                    break;
                case "strokeColor":
                    element.setStrokeColor(xmlParser.getAttributeIntValue(i, element.getStrokeColor()));
                    break;
                case "strokeAlpha":
                    //需要与全局透明度叠加
                    element.setStrokeAlpha(xmlParser.getAttributeFloatValue(i, element.getStrokeAlpha()));
                    break;
                case "fillAlpha":
                    //需要与全局透明度叠加
                    element.setFillAlpha(xmlParser.getAttributeFloatValue(i, element.getFillAlpha()));
                    break;
                case "fillColor":
                    element.setFillColor(xmlParser.getAttributeIntValue(i, element.getFillColor()));
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

    }

    /**
     * 解析vector xml中的group标签
     */
    private void parseGroupElement(@NonNull XmlResourceParser xmlParser, List<GroupElement> element) {

    }

    /**
     * 解析vector标签的根属性
     */
    private void parseVectorAttributes(@NonNull XmlResourceParser xmlParser, @NonNull VectorElement rootElement) {
        String value;
        int attrCount = xmlParser.getAttributeCount();
        for (int i = 0; i < attrCount; i++) {
            switch (xmlParser.getAttributeName(i)) {
                case "width":
                    //dimen类型数值字符串
                    rootElement.setWidth(parseDimenToPx(xmlParser.getAttributeValue(i), rootElement.getWidth()));
                    break;
                case "height":
                    //dimen类型数值字符串
                    rootElement.setWidth(parseDimenToPx(xmlParser.getAttributeValue(i), rootElement.getHeight()));
                    break;
                case "viewportWidth":
                    //解析可能得到小数或者整数，但肯定是float
                    rootElement.setViewportWidth(xmlParser.getAttributeFloatValue(i, 0f));
                    break;
                case "viewportHeight":
                    //解析可能得到小数或者整数，但肯定是float
                    rootElement.setViewportHeight(xmlParser.getAttributeFloatValue(i, 0f));
                    break;
                case "alpha":
                    rootElement.setAlpha(xmlParser.getAttributeFloatValue(i, 1f));
                    break;
                case "name":
                    rootElement.setName(xmlParser.getAttributeValue(i));
                    break;
                case "tint":
                    //color类型，一种是资源id（@），一种是color字符串（#）
                    value = xmlParser.getAttributeValue(i);
                    int tint = rootElement.getTint();
                    char firstChar = value.charAt(0);
                    if (firstChar == '#') {
                        tint = Color.parseColor(value);
                    } else if (firstChar == '@') {
                        tint = mContext.getResources().getColor(xmlParser.getAttributeResourceValue(i, tint));
                    }
                    rootElement.setTint(tint);
                    break;
                case "tintMode":
                    rootElement.setTintMode(xmlParser.getAttributeValue(i));
                    break;
                case "autoMirror":
                    rootElement.setAutoMirrored(xmlParser.getAttributeBooleanValue(i, rootElement.isAutoMirrored()));
                    break;
            }
        }
    }

    /**
     * 解析xml中的vector标签数据
     * @param xmlParser 目标xml的解析对象，能方便地获取到xml中的数据
     * @param vectorElement vector标签的数据集，parser读出的数据保存到此对象中
     */
    public void parseVectorXml(@NonNull XmlResourceParser xmlParser, @NonNull VectorElement vectorElement) throws Exception {
        vectorElement.reset();
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
        parseVectorAttributes(xmlParser, vectorElement);

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
                    parsePathElement(xmlParser, vectorElement.getPathElementList());
                    break;
                case "clip-path":
                    parseClipPathElement(xmlParser, vectorElement.getClipPathElementList());
                    break;
                case "group":
                    parseGroupElement(xmlParser, vectorElement.getGroupElementList());
                    break;
                default:
                    SvgLog.I("不解析标签[" + tag + "].");
                    break;
            }
        }
    }

    /**
     * 解析一条path标签的pathData属性，得到指令符数据集
     * @param pathData 从xml中得到的path标签的pathData属性字符串
     * @param pathDataList 目标数据列表，保存解析结果
     */
    public static boolean parsePathDataAttribute(@NonNull String pathData, @NonNull List<PathData> pathDataList) {
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
            SvgLog.i("一次子路径开始位置：" + pos);
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

            char anchor = ch;
            //如果是结束指令符Z/z，则可以立即完成本次循环任务，进行下一次循环
            if (SvgCharUtil.toUpper(anchor) == SvgConsts.SVG_END_CHAR_UPPER) {
                SvgLog.i("发现结束指令符" + anchor + ", 快速完成本次循环任务");
                addSubPath(pathDataList, anchor, null);
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
                    if (sb.length() > 0 && !addValue(valueList, sb.toString())) {
                        return false;
                    }
                    SvgLog.i("一个指令符的数值获取完成，当前位置：" + pos);
                    break;
                }

                //如果碰到的是[,]或[ ]，说明一个数值的数据获取完成，并且跳过之后连续的[,]或[ ]
                if (ch == ',' || ch == ' ') {
                    if (!addValue(valueList, sb.toString())) {
                        return false;
                    }

                    //跳过之后连续的[,]和[ ]分隔符
                    int checkPos = pos + 1;
                    while (checkPos < dataLen) {
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
                        if (!addValue(valueList, sb.toString())) {
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
                    if (!addValue(valueList, finalStr)) {
                        return false;
                    }
                }
            }

            addSubPath(pathDataList, anchor, valueList);
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

    /**
     * 将一些特殊指令符转换成能被Path表达的指令符。
     * 如T转换成Q后，可使用{@link Path#quadTo(float, float, float, float)}来表达；
     * S转换成C后，可使用{@link Path#cubicTo(float, float, float, float, float, float)}来表达。
     */
    private static boolean addSpecialCommand(@NonNull List<PathData> list, PathData pathData) {
        char newCmd = pathData.getCommand();

        List<Float> valueList = pathData.getValueList();
        int valueCount = valueList.size();
        if (valueCount == 0) {
            SvgLog.I("缺少数值的指令符，忽略它");
            return false;
        }

        float x1, y1, x2, y2, x, y;
        switch (SvgCharUtil.toUpper(newCmd)) {
            //指令Q的简写，规定前一个指令符应该是Q/T/q/t的一种（为T提供参考数据）。否则T的结果将只是一条直线。
            // 这里不可能是T/t，因为T/t总是会被转换成Q
            case 'T':
                for (int i = 0; i < valueCount; i += 2) {
                    PathData lastPathData = list.get(list.size() - 1);
                    x1 = lastPathData.getEndCoordinate()[0];
                    y1 = lastPathData.getEndCoordinate()[1];

                    //需要转换成绝对坐标
                    boolean isRefCoordinate = Character.isLowerCase(newCmd);
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
                    boolean isRefCoordinate = Character.isLowerCase(newCmd);
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
    private static boolean addValue(List<Float> list, String value) {
        SvgLog.i("得到一个数值：" + value);
        try {
            list.add(Float.parseFloat(value));
        } catch (Exception e) {
            SvgLog.i("解析字符串数值异常。流程应立即终止。原因=" + e.getLocalizedMessage());
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

    /**
     * 将xml中的dimen值转换成px
     * @param dimen 带dimen单位的数值字符串
     * @param defValue 默认数值
     */
    private float parseDimenToPx(String dimen, float defValue) {
        if (TextUtils.isEmpty(dimen)) {
            return defValue;
        }

        float result = defValue;
        int len = dimen.length();
        float value;
        if (dimen.endsWith("dip")) {
            value = Float.parseFloat(dimen.substring(0, len - 3));
            result = ScreenUtil.dp2Px(mContext, value);
        } else if (dimen.endsWith("dp")) {
            value = Float.parseFloat(dimen.substring(0, len - 2));
            result = ScreenUtil.dp2Px(mContext, value);
        } else if (dimen.endsWith("px")) {
            result = Float.parseFloat(dimen.substring(0, len - 2));
        }
        return Math.max(0f, result);
    }
}
