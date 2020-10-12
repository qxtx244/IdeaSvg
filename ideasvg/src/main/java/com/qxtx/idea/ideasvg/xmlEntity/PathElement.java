package com.qxtx.idea.ideasvg.xmlEntity;

import android.graphics.Color;
import android.graphics.Path;
import android.support.annotation.NonNull;

import com.qxtx.idea.ideasvg.parser.VectorXmlParser;
import com.qxtx.idea.ideasvg.tools.SvgCharUtil;
import com.qxtx.idea.ideasvg.tools.SvgConsts;
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
    private int fillColor;

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

        strokeWidth = SvgConsts.INVALID_FLOAT;
        strokeColor = SvgConsts.INVALID_INT;
        strokeAlpha = 1f;

        fillColor = SvgConsts.INVALID_INT;
        fillAlpha = 1f;

        fillType = null;
        name = null;
        strokeLineCap = SvgConsts.INVALID_INT;
        strokeLineJoin = SvgConsts.INVALID_INT;
        strokeMiterLimit = SvgConsts.INVALID_INT;
        trimPathStart = SvgConsts.INVALID_FLOAT;
        trimPathOffset = SvgConsts.INVALID_FLOAT;
        trimPathEnd = SvgConsts.INVALID_FLOAT;
    }

    public Path getPath() {
        return path;
    }

    public String getPathString() {
        return pathString;
    }

    /**
     * 保存原始路径数据字符串，并生成{@link #pathDataList}
     * @param scaleW 宽的缩放值
     * @param scaleH 高的缩放值
     * @param pathString 原始路径数据字符串
     */
    public void savePathString(float scaleW, float scaleH, String pathString) {
        this.pathString = pathString;

        try {
            //更新到路径数据列表
            if (!VectorXmlParser.parsePathDataAttribute(pathString, pathDataList)) {
                pathDataList.clear();
                path.reset();
                return ;
            }

            if (!VectorXmlParser.generatePath(scaleW, scaleH, pathDataList, path)) {
                pathDataList.clear();
                path.reset();
                return ;
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
        for (PathData pathData : pathDataList) {
            sb.append(pathData.toString()).append("\n");
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
