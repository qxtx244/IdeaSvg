package com.qxtx.idea.ideasvg.entity;

import android.support.annotation.IdRes;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * CreateDate 2020/5/23 18:04
 * <p>
 *
 * @author QXTX-WIN
 * Description: svg配置实体类，包含了一个svg的具体描述参数，具有的属性如下：
 * [spec] 实际画布宽高值，svg最终展示的尺寸。具体做法为：将viewoportSpec按比例缩放，直到刚好能放到这个实际画布中。
 *          这意味着path数值需要做对应的缩放，才能得到svg最终在实际画布中展示的尺寸。
 *          path数值从参考画布转换到此实际画布尺寸计算方法：比如实际画布大小w:100 h:100，参考画布大小w:500 h:300，
 *          则取宽高差值较大者，则path数值的缩放比率为：zoom = (500-100) / 500 = 0.8，path中的所有数值乘以这个比率。
 * [viewportWidth] 参考画布宽。只为path提供尺寸参考，与最终svg的尺寸单位、尺寸大小无关
 * [viewportHeight] 参考画布高。只为path提供尺寸参考，与最终svg的尺寸单位、尺寸大小无关
 * [alpha] 全局透明度。如果path标签中也存在alpha属性，则与此全局透明度叠加得到最终alpha值
 * [pathParamList] 子path数据列表
 * [tint] 全局着色
 * [tintMode] 全局着色模式，加色，减色等模式
 * [name] 唯一识别名称
 * [autoMirrored] svg镜像（试了好像没效果）
 */
public class SvgParam {

    /**
     * svg的实际宽度，dimen数值
     * 注意，此宽数据仅仅是定义了svg在实际画布下的尺寸，与路径数值无关
     * 此宽高值通过vector根标签中的width属性得到
     */
    private float width;

    /**
     * svg的实际高度，dimen数值
     * 注意，此宽数据仅仅是定义了svg在实际画布下的尺寸，与路径数值无关
     * 此值通过vector根标签中的height属性得到
     */
    private float height;

    /**
     * svg路径参考宽
     * 注意，此宽高数据作为路径的参考画布宽高，与svg的实际尺寸无关，而{@link #width}和{@link #height}则描述了svg在实际画布中的尺寸。
     * 简单地说，前者仅仅是为了给路径提供一个参考画布尺寸，后者定义了这个图形在真实画布中的尺寸
     * 可以通过svg实际宽高和此宽高数据的比值，计算出路径在实际画布尺寸下的数值，然后在view中绘制出真实尺寸的svg。
     * 在vector xml资源中，此宽高值通过vector根标签中的viewportWidth，viewportHeight属性取到
     * 在.svg文件中，存在一个viewBox属性，包含有viewportWidth,viewportHeight
     */
    private float viewportWidth;

    /**
     * svg路径参考高
     * 注意，此宽高数据作为路径的参考画布宽高，与svg的实际尺寸无关，而{@link #width}和{@link #height}则描述了svg在实际画布中的尺寸。
     * 简单地说，前者仅仅是为了给路径提供一个参考画布尺寸，后者定义了这个图形在真实画布中的尺寸
     * 可以通过svg实际宽高和此宽高数据的比值，计算出路径在实际画布尺寸下的数值，然后在view中绘制出真实尺寸的svg。
     * 在vector xml资源中，此宽高值通过vector根标签中的viewportWidth，viewportHeight属性取到
     * 在.svg文件中，存在一个viewBox属性，包含有viewportWidth,viewportHeight
     */
    private float viewportHeight;

    /** 原点X坐标偏移值，默认为0。AS在导入.svg为xml资源时，已经自动将这个偏移值应用到了path数值中 */
    private int offsetX;

    /** 原点Y坐标偏移值，默认为0。AS在导入.svg为xml资源时，已经自动将这个偏移值应用到了path数值中 */
    private int offsetY;

    /**
     * 控件的透明度属性，范围为[0f, 1f]
     * 根标签的属性优先决定svg整体，如果某个path中有自己的这个属性，则数值叠加，
     * 例如：根标签alpha="0.5"，path标签中有strokeAlpha="0.4"，则最终这个path的透明度为0.5x0.4=0.2
     */
    private float alpha;

    /** 一条path的相关数据 */
    private final List<PathParam> pathParamList;

    private String preserveAspectRatio;

    /** 唯一识别名称 */
    private String name;
    /** 全局强制着色，默认为null（无全局着色） */
    private @IdRes int tint;
    /** 着色模式 */
    private String tintMode;
    /** 镜像展示（这个属性经测试，好像没有效果） */
    private boolean autoMirrored;

    /** svg绘制风格 */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STYLE_UNKNOWN, STYLE_STROKE, STYLE_FILL, STYLE_STROKE_AND_FILL})
    public @interface Style{}
    /** 未知风格 */
    public static final int STYLE_UNKNOWN = 0;
    /** 轮廓风格 */
    public static final int STYLE_STROKE = 0x1;
    /** 填充风格 */
    public static final int STYLE_FILL = STYLE_STROKE << 1;
    /** 轮廓和填充混合风格 */
    public static final int STYLE_STROKE_AND_FILL = STYLE_FILL | STYLE_STROKE;

    public SvgParam() {
        pathParamList = new ArrayList<>();
        reset();
    }

    public void reset() {
        width = 0f;
        height = 0f;
        viewportWidth = 0f;
        viewportHeight = 0f;
        offsetX = 0;
        offsetY = 0;
        alpha = 1f;
        pathParamList.clear();
        preserveAspectRatio = null;
        name = null;
        tint = Integer.MIN_VALUE;
        tintMode = null;
        autoMirrored = false;
    }

    public float getWidth() {
        return width;
    }
    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }
    public void setHeight(float height) {
        this.height = height;
    }

    public float getViewportWidth() {
        return viewportWidth;
    }

    public void setViewportWidth(float viewportWidth) {
        this.viewportWidth = viewportWidth;
    }

    public float getViewportHeight() {
        return viewportHeight;
    }

    public void setViewportHeight(float viewportHeight) {
        this.viewportHeight = viewportHeight;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public String getPreserveAspectRatio() {
        return preserveAspectRatio;
    }

    public void setPreserveAspectRatio(String preserveAspectRatio) {
        this.preserveAspectRatio = preserveAspectRatio;
    }

    public List<PathParam> getPathParamList() {
        return pathParamList;
    }

    public boolean isAutoMirrored() {
        return autoMirrored;
    }

    public void setAutoMirrored(boolean autoMirrored) {
        this.autoMirrored = autoMirrored;
    }

    public int getTint() {
        return tint;
    }

    public void setTint(int tint) {
        this.tint = tint;
    }

    public String getTintMode() {
        return tintMode;
    }

    public void setTintMode(String tintMode) {
        this.tintMode = tintMode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
            if (pathParamList != null) {
                for (PathParam item : pathParamList) {
                    sb.append("M").append(item.getPathDataMap().get("M0").get(0)).append("\n\t");
                }
            }

        return "SvgParam{" +
                "width=" + width +
                ", height=" + height +
                ", viewportWidth=" + viewportWidth +
                ", viewportHeight=" + viewportHeight +
                ", offsetX=" + offsetX +
                ", offsetY=" + offsetY +
                ", alpha=" + alpha +
                ", pathParamList size=" + pathParamList.size() +
                ", \npathParamList=" + "\n" + sb.toString() + "\n" +
                ", preserveAspectRatio='" + preserveAspectRatio +
                ", name='" + name +
                ", tint='" + tint  +
                ", tintMode='" + tintMode +
                ", autoMirrored=" + autoMirrored +
                '}';
    }
}
