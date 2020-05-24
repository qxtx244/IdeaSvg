package com.qxtx.idea.svg.view;

import android.graphics.drawable.Drawable;

import com.qxtx.idea.svg.listener.SvgDrawListener;
import com.qxtx.idea.svg.view.IdeaSvgView;

/**
 * @author QXTX-WIN
 * @date 2020/3/21 12:53
 * Description:
 */
public interface ISvgViewExtend {

    /**
     * 刷新view，可以实现颜色、线条粗细等等绘制属性变化
     */
    void refresh();

    /**
     * 设置svg的绘制监听，建议在svg相关绘制api之前调用，才能监听到完整的svg绘制过程
     */
    void setSvgDrawListener(SvgDrawListener listener);

    void setSvgMode(@IdeaSvgView.SvgStyle int style);

    /**
     * 设置svg的手势支持。如果为true，则允许使用svg手势，否则禁止svg手势功能
     * 目前仅支持双指缩放手势
     */
    void setSvgGestureEnable(boolean gestureEnabled);

    int getSvgAlpha();

    float getSvgScale();

    float[] getSvgTranslate();

    /** 手势功能是否被启用 */
    boolean isGestureEnable();

    /** 是否正在使用手势 */
    boolean isGesturePlaying();

    /**
     * 是否支持手势缩放
     * @param isGestureZoomEnable [true]支持双指缩放 [false]禁用双指缩放
     */
//    void setGestureZoom(boolean isGestureZoomEnable);

    /**
     * 设置Drawable的显示风格。此方法调用后将会立即生效。如果当前没有drawable被显示，则为无效调用
     */
    void setDrawableStyle(@IdeaSvgView.DrawableStyle int style);

    /**
     * 为View设置Drawable
     * drawable和svg绘制互相覆盖。
     * 如果执行了此方法，当前绘制的svg将会被清除，然后绘制此drawable；反之绘制的drawable将会被清除，转而绘制目标svg。
     */
    void setDrawable(Drawable drawable);

    /** 是否强制使svg居中绘制 */
    void setSvgCenter(boolean forceCenter);

    void setStokeColor(int... color);

    void setFillColor(int... color);

    void setStokeWidth(float px);

    /** 是否正在进行动画 */
    boolean isSvgAnimRunning();
}
