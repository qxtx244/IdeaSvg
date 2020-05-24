package com.qxtx.idea.ideasvg.entity;

import android.graphics.Color;
import android.graphics.Path;

import com.qxtx.idea.ideasvg.parser.SvgParser;
import com.qxtx.idea.ideasvg.tools.SvgLog;

import java.util.LinkedHashMap;

/**
 * CreateDate 2020/5/23 18:04
 * <p>
 *
 * @author QXTX-WIN
 * Description: vector xml中的一个path标签包含的属性信息
 */
public final class PathParam {

    /** 由{@link #pathDataMap}生成的路径对象 */
    private final Path path;

    /** 由{@link #pathData}生成的路径对象，用于生成{@link #path} */
    private LinkedHashMap<String, float[]> pathDataMap;

    /** 原始的path字符串数据，用于生成{@link #pathDataMap} */
    private String pathData;

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
    public PathParam() {
        path = new Path();
        pathDataMap = null;
        pathData = null;

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

    private void onPathDataUpdate() {
        try {
            //更新pathDataMap
            pathDataMap = SvgParser.parsePathData(pathData);

            //一旦pathDataMap有变动，path将会被同步更新
            onPathDataMapUpdate();
        } catch (Exception e) {
            SvgLog.i("异常！" + e);
            e.printStackTrace();
        }
    }

    private void onPathDataMapUpdate() {
        path.reset();

        //更新Path
    }

    public Path getPath() {
        return path;
    }

    public LinkedHashMap<String, float[]> getPathDataMap() {
        return pathDataMap;
    }

    public String getPathData() {
        return pathData;
    }

    /** 一旦更新这个变量，则与其对应的{@link #pathDataMap}也将会被立即更新 */
    public void setPathData(String pathData) {
        this.pathData = pathData;
        onPathDataUpdate();
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
        return "PathParam{" +
                "path empty? " + path.isEmpty() +
                ", pathDataMap size=" + (pathDataMap == null ? -1 : pathDataMap.size()) +
                ", pathData='" + pathData +
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
