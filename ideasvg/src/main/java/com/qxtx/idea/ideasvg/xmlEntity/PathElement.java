package com.qxtx.idea.ideasvg.xmlEntity;

import android.graphics.Color;
import android.graphics.Path;
import android.support.annotation.NonNull;

import com.qxtx.idea.ideasvg.parser.VectorXmlParser;
import com.qxtx.idea.ideasvg.tools.SvgLog;

import java.util.ArrayList;
import java.util.List;

/**
 * CreateDate 2020/5/23 18:04
 * <p>
 * @author QXTX-WIN
 * Description: vector xml中的一个path标签包含的属性信息
 */
public final class PathElement {

    /** 由{@link #pathDataList}生成的路径对象 */
    private final Path path;

    /** 由{@link #pathString}生成的路径对象，用于生成{@link #path} */
    private final List<PathData> pathDataList;

    /** 原始的path字符串数据，用于生成{@link #pathDataList} */
    private String pathString;

    /** 线条宽度，默认为0 */
    private float strokeWidth;

    /** 线条颜色，默认为透明（无线条） */
    private int strokeColor;

    /** 在.svg中，这个值可以是transparent，默认透明 */
    private float strokeAlpha;

    /** 填充颜色，默认为透明（无填充） */
    private int  fillColor;

    /** 在.svg中，这个值可能是transparent，默认透明 */
    private float fillAlpha;

    private String fillType;
    private String name;
    private int strokeLineCap;
    private int strokeLineJoin;
    private float strokeMiterLimit;
    private float trimPathStart;
    private float trimPathOffset;
    private float trimPathEnd;

    /**
     * 构造方法
     * 默认为线条宽度0/透明颜色/透明度0，填充透明颜色/透明度0，当线条或填充具有有效值，说明使用此种绘制形式
     */
    public PathElement() {
        path = new Path();
        pathDataList = new ArrayList<>();
        pathString = null;

        strokeWidth = Float.MIN_VALUE;
        strokeColor = Color.parseColor("#00000000");
        strokeAlpha = 1f;

        fillColor = Color.parseColor("#00000000");
        fillAlpha = 1f;

        fillType = null;
        name = null;
        strokeLineCap = Integer.MIN_VALUE;
        strokeLineJoin = Integer.MIN_VALUE;
        strokeMiterLimit = Integer.MIN_VALUE;
        trimPathStart = Float.MIN_VALUE;
        trimPathOffset = Float.MIN_VALUE;
        trimPathEnd = Float.MIN_VALUE;
    }

    /**
     * 由指令符数据集生成Path对象列表
     * @throws Exception 如果数据不正确（如某个指令符携带的数值数量不符合要求），将会导致解析出错，抛出异常
     */
    private void generatePath() throws Exception {
        path.reset();
        if (pathDataMap == null || pathDataMap.size() == 0) {
            return ;
        }

        float lastX = 0f, lastY = 0f;
        String[] keyArray = (String[])pathDataMap.keySet().toArray();
        for (int i = 0; i < keyArray.length; i++) {
            String key = keyArray[i];
            char anchor = key.charAt(0);
            //可能一个指令符携带了n个相同指令符的数据，如[M 1，2，3，4，5，6]这样的数据，实际上是3个M轨迹的数据，只是指令符被省略了
            List<Float> values = pathDataMap.get(key);
            if (anchor != 'Z' && anchor != 'z'
                    && (values == null || values.size() == 0)) {
                throw new IllegalStateException("非法的指令符数据！");
            }

            switch (anchor) {
                case 'M': //2
                    lastX = values.get(values.size() - 2);
                    lastY = values.get(values.size() - 1);
                    path.moveTo(lastX, lastY);
                    break;
                case 'H': //1
                    lastX = values.get(values.size() - 1);
                    path.lineTo(lastX, lastY);
                    break;
                case 'V': //1
                    lastY = values.get(values.size() - 1);
                    path.lineTo(lastX, lastY);
                    break;
                case 'L': //2
                    lastX = values.get(values.size() - 2);
                    lastY = values.get(values.size() - 1);
                    path.lineTo(lastX, lastY);
                    break;
                case 'T': //2，Q（二次贝塞尔）的简写指令符。T前面必须是Q或者T指令符，否则将得到一条直线
                    processAnchorT(values, keyArray, i, lastX, lastY);
                    lastX = values.get(values.size() - 2);
                    lastY = values.get(values.size() - 1);
                    break;
                case 'S': //4，简化的贝塞尔曲线。S前面必须是C或S指令符，否则两个控制点将是同一个
                    break;
                case 'Q': //4
                    lastX = values.get(2);
                    lastY = values.get(3);
                    path.quadTo(values.get(0), values.get(1), lastX, lastY);
                    break;
                case 'C': //6
                    lastX = values.get(4);
                    lastY = values.get(5);
                    path.cubicTo(values.get(0), values.get(1), values.get(2), values.get(3), lastX, lastY);
                    break;
                case 'A': //7
                    break;
                case 'Z':
                case 'z':
                    path.close();
                    break;
                case 'm': //2
                    lastX += values.get(0);
                    lastY += values.get(1);
                    path.moveTo(lastX, lastY);
                    break;
                case 'h': //1
                    lastX += values.get(0);
                    path.lineTo(lastX, lastY);
                    break;
                case 'v': //1
                    lastY += values.get(0);
                    path.lineTo(lastX, lastY);
                    break;
                case 'l': //2
                    lastX += values.get(0);
                    lastY += values.get(1);
                    path.lineTo(lastX, lastY);
                    break;
                case 't': //2
                    break;
                case 's': //4
                    break;
                case 'q': //4
                    lastX += values.get(2);
                    lastY += values.get(3);
                    path.rQuadTo(values.get(0), values.get(1), values.get(2), values.get(3));
                    break;
                case 'c': //6
                    lastX += values.get(4);
                    lastY += values.get(5);
                    path.rCubicTo(values.get(0), values.get(1), values.get(2), values.get(3), values.get(4), values.get(5));
                    break;
                case 'a': //7
                    break;
            }
        }
    }

    /** 处理T指令符轨迹 */
    private void processAnchorT(List<Float> values, String[] keyArray, int keyIndex, float lastX, float lastY) {
        float x1, y1, x2, y2;

        //先计算第一个T，后面的就好算了
        x2 = values.get(0);
        y2 = values.get(1);
        //如果前一个指令符是Q，则直接解析；如果是T，则需要继续往前找；如果两者都不是，则取其终点坐标
        int refKeyIndex = findTRefAnchorIndex(keyArray, keyIndex);
        String refKey = keyArray[refKeyIndex];
        List<Float> refValues = pathDataMap.get(refKey);
        if (refKey.charAt(0) == 'Q') {
            int size = refValues.size();
            x1 = values.get(keyIndex) + refValues.get(size - 2) - refValues.get(size - 4);
            y1 = values.get(keyIndex + 1) + refValues.get(size - 1) - refValues.get(size - 3);
        } else {
            //LYX_TAG 2020/9/19 21:10 暂不支持相对指令符q/t（即小写的指令符）作为参考指令符，此时视其为普通指令符，一律取最后坐标。也不支持T T T这样的连续T，
            // 仅支持[非T指令符X T]这样的格式
            // ！！！需要重新整理，在上一步parsePathData的时候，就将T转换成Q
            SvgLog.i("暂不支持相对指令符（即小写的指令符）作为参考指令符，因此直接以直线轨迹绘制");
            x1 = refValues.get(refValues.size() - 2);
            y1 = refValues.get(refValues.size() - 1);
            //前面可能还隔了n个T，可能是M T T ... T 当前T
            for (int i = refKeyIndex; i < keyIndex; i++) {
            }
        }
        path.quadTo(x1, y1, x2, y2);
    }

    /**
     * 找到前面能够为T指令符提供参考的指令符索引。
     * @param keyArray 指令符索引数组
     * @param keyIndex 当前T指令符索引
     * @return 参考指令符的索引
     */
    private int findTRefAnchorIndex(@NonNull String[] keyArray, int keyIndex) {
        int ret = 0;
        for (int i = keyIndex - 1; i >= 0; i--) {
            char anchor = keyArray[i].charAt(i);
            if (anchor != 'T') {
                //LYX_TAG 2020/9/19 23:12 t/q，暂时当作普通指令符来处理
                ret = i;
                break;
            }
        }
        return ret;
    }

    public Path getPath() {
        return path;
    }

    public String getPathString() {
        return pathString;
    }

    /**
     * 保存路径原始数据，并生成{@link #pathDataList}
     */
    public void setPathString(String pathString) {
        this.pathString = pathString;

        try {
            //更新到路径数据列表
            if (!VectorXmlParser.parsePathDataAttribute(pathString, pathDataList)) {
                pathDataList.clear();
            }
        } catch (Exception e) {
            SvgLog.i("解析pathData发生异常！" + e);
            e.printStackTrace();
        }
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
    }

    public float getStrokeAlpha() {
        return strokeAlpha;
    }

    public void setStrokeAlpha(float strokeAlpha) {
        this.strokeAlpha = strokeAlpha;
    }

    public int getFillColor() {
        return fillColor;
    }

    public void setFillColor(int fillColor) {
        this.fillColor = fillColor;
    }

    public float getFillAlpha() {
        return fillAlpha;
    }

    public void setFillAlpha(float fillAlpha) {
        this.fillAlpha = fillAlpha;
    }

    public String getFillType() {
        return fillType;
    }

    public void setFillType(String fillType) {
        this.fillType = fillType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStrokeLineCap() {
        return strokeLineCap;
    }

    public void setStrokeLineCap(int strokeLineCap) {
        this.strokeLineCap = strokeLineCap;
    }

    public int getStrokeLineJoin() {
        return strokeLineJoin;
    }

    public void setStrokeLineJoin(int strokeLineJoin) {
        this.strokeLineJoin = strokeLineJoin;
    }

    public float getStrokeMiterLimit() {
        return strokeMiterLimit;
    }

    public void setStrokeMiterLimit(float strokeMiterLimit) {
        this.strokeMiterLimit = strokeMiterLimit;
    }

    public float getTrimPathStart() {
        return trimPathStart;
    }

    public void setTrimPathStart(float trimPathStart) {
        this.trimPathStart = trimPathStart;
    }

    public float getTrimPathOffset() {
        return trimPathOffset;
    }

    public void setTrimPathOffset(float trimPathOffset) {
        this.trimPathOffset = trimPathOffset;
    }

    public float getTrimPathEnd() {
        return trimPathEnd;
    }

    public void setTrimPathEnd(float trimPathEnd) {
        this.trimPathEnd = trimPathEnd;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (pathDataList != null) {
            for (PathData pathData : pathDataList) {
                sb.append(pathData.toString()).append("\n");
            }
        }

        return "PathParam{" +
                "path= " + (path.isEmpty() ? "[无]" : "[有数据]") +
                ", pathDataList=[" + sb.toString() + "]" +
                ", pathData='" + pathString +
                ", strokeWidth=" + strokeWidth +
                ", strokeColor=" + strokeColor +
                ", strokeAlpha=" + strokeAlpha +
                ", fillColor=" + fillColor +
                ", fillAlpha=" + fillAlpha +
                ", fillType='" + fillType +
                ", name='" + name +
                ", strokeLineCap=" + strokeLineCap +
                ", strokeLineJoin=" + strokeLineJoin +
                ", strokeMiterLimit=" + strokeMiterLimit +
                ", trimPathStart=" + trimPathStart +
                ", trimPathOffset=" + trimPathOffset +
                ", trimPathEnd=" + trimPathEnd +
                '}';
    }
}
