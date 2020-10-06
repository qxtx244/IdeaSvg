package com.qxtx.idea.ideasvg.xmlEntity;

import android.support.annotation.IdRes;

import java.util.ArrayList;
import java.util.List;

/**
 * CreateDate 2020/5/23 18:04
 * <p>
 *
 * @author QXTX-WIN
 * Description: svg配置实体类，包含了一个svg的具体描述参数，具有的属性如下：
 * [width] 实际画布宽
 * [height] 实际画布高
 * [viewportWidth] 参考画布宽。只为path提供尺寸参考，与最终svg的尺寸单位、尺寸大小无关
 * [viewportHeight] 参考画布高。只为path提供尺寸参考，与最终svg的尺寸单位、尺寸大小无关
 * [alpha] 全局透明度。如果path标签中也存在alpha属性，则与此全局透明度叠加得到最终alpha值
 * [pathParamList] 子path数据列表
 * [tint] 图像着色，必须参考tintMode
 * [tintMode] 全局着色模式，加色，减色等模式，默认是src_in
 * [name] 唯一识别名称
 * [autoMirrored]
 *
 * 备注：具体做法为：将viewportWidth x viewportHeight按比例缩放，直到能包裹width x height的最小尺寸。
 *      这意味着path数值需要做对应的缩放，才能得到svg最终在实际画布中展示的尺寸。path数值从参考画布转换到此实际画布尺寸计算方法：
 *      比如实际画布大小w:100 h:100，参考画布大小w:500 h:300，则取宽高差值较大者，则path数值的缩放比率为：zoom = (500-100) / 500 = 0.8，path中的所有数值乘以这个比率。
 */
public class VectorElement {

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
     * 注意，此宽高数据作为路径的参考画布宽高，与svg的实际尺寸无关，而{@link #width}则描述了svg在实际画布中的宽。
     * 在vector xml资源中，通过vector根标签中的viewportWidth属性取到
     * 在.svg文件中，存在一个viewBox属性，包含有viewportWidth
     */
    private float viewportWidth;

    /**
     * svg路径参考高
     * 注意，此宽高数据作为路径的参考画布宽高，与svg的实际尺寸无关，而{@link #height}则描述了svg在实际画布中的高。
     * 在vector xml资源中，通过vector根标签中的viewportHeight属性取到
     * 在.svg文件中，存在一个viewBox属性，包含有viewportHeight
     */
    private float viewportHeight;

    /**
     * 控件的透明度属性，范围为[0f, 1f]
     * 根标签的属性优先决定svg整体，如果某个path中有自己的这个属性，则数值叠加，
     * 例如：根标签alpha="0.5"，path标签中有strokeAlpha="0.4"，则最终这个path的透明度为0.5x0.4=0.2
     */
    private float alpha;

    /** 唯一识别名称 */
    private String name;

    /** 全局强制着色，默认为null（无全局着色），需要参考{@link #tintMode} */
    private @IdRes int tint;
    /** 着色模式 */
    private String tintMode;

    private boolean autoMirrored;

    /** path标签数据列表 */
    private final List<PathElement> pathElementList;

    /** group标签数据列表 */
    private final List<GroupElement> groupElementList;

    /** clip-path标签数据列表 */
    private final List<ClipPathElement> clipPathElementList;

    public VectorElement() {
        pathElementList = new ArrayList<>(1);
        groupElementList = new ArrayList<>(1);
        clipPathElementList = new ArrayList<>(1);
        reset();
    }

    public void reset() {
        width = 0f;
        height = 0f;
        viewportWidth = 0f;
        viewportHeight = 0f;
        alpha = 1f;
        name = null;
        tint = 0;
        tintMode = null;
        autoMirrored = false;
        pathElementList.clear();
        groupElementList.clear();
        clipPathElementList.clear();
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

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public boolean isAutoMirrored() {
        return autoMirrored;
    }

    public void setAutoMirrored(boolean autoMirrored) {
        this.autoMirrored = autoMirrored;
    }

    public @IdRes int getTint() {
        return tint;
    }

    public void setTint(@IdRes int tint) {
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

    public List<PathElement> getPathElementList() {
        return pathElementList;
    }

    public List<GroupElement> getGroupElementList() {
        return groupElementList;
    }

    public List<ClipPathElement> getClipPathElementList() {
        return clipPathElementList;
    }

    @Override
    public String toString() {
        return "VectorElement{" +
                "width=" + width +
                ", height=" + height +
                ", viewportWidth=" + viewportWidth +
                ", viewportHeight=" + viewportHeight +
                ", alpha=" + alpha +
                ", name='" + name + '\'' +
                ", tint=" + tint +
                ", tintMode='" + tintMode + '\'' +
                ", autoMirrored=" + autoMirrored +
                ", pathElementList size=" + pathElementList.size() +
                ", groupElementList size=" + groupElementList.size() +
                ", clipPathElementList size=" + clipPathElementList.size() +
                '}';
    }
}
