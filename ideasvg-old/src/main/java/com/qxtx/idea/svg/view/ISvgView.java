package com.qxtx.idea.svg.view;

import com.qxtx.idea.svg.animation.ISvgAnim;
import com.qxtx.idea.svg.listener.AnimListener;

/**
 * @author QXTX-WIN
 * @date 2020/3/21 12:51
 * Description:
 */
public interface ISvgView {

    /** 可以执行一些svg效果动画 */
    boolean startSvgAnim(ISvgAnim anim);

    /**
     * svg切换，从当前svg动画变换为新的svg，目前仅实现两个svg切换，后期将支持多个svg顺序切换
     * @return false]动画未被启动 [true]动画成功启动
     */
    boolean showSvg(String svgData, long durationMs, AnimListener listener);

    /**
     * svg透明度变化
     * @param value 透明度，范围为0~255，如果超出范围，则直接忽略整个指令。
     * @param durationMs 动画持续时长
     * @param listener 动画监听器
     * @return [false]动画未被启动 [true]动画成功启动
     */
    boolean svgAlpha(int value, long durationMs, AnimListener listener);

    /**
     * svg平移
     * 在svg被更新时，重置平移量
     * @param x 目的X坐标
     * @param y 目的Y坐标
     * @return  [false]动画未被启动 [true]动画成功启动
     * @see #showSvg(String)
     * @see #clear()
     */
    boolean svgTranslate(float x, float y, long durationMs, AnimListener listener);

    /**
     * svg中心缩放。在svg被更新时，重置缩放量。缩放值有具体的有效范围，
     * 当执行此方法，若目标缩放值有效，则作为当前缩放值，将svg缩放到这个值。
     * @param value 缩放值，如果为负数，则无缩放效果
     * @param durationMs 大于0时，表示动画持续时长；如果小于等于0，则表示为无动画，直接缩放
     * @param listener 动画监听器
     * @return [false]动画未被启动 [true]动画成功启动
     * @see #showSvg(String)
     * @see #clear()
     */
    boolean svgScale(float value, long durationMs, AnimListener listener);

    /**
     * 填充动画
     * @param colors 填充颜色，可添加多种颜色
     * @param isAnim 是否使用填充动画
     * @return [false]动画未被启动 [true]动画成功启动
     */
    boolean fillSvg(int[] colors, boolean isAnim);

    /** 使当前的svg动画直接完成，从而动画停止  */
    void finishSvgAnim();

    /** 立即终止当前的svg动画 */
    void stopSvgAnim();

    /** 这将会停止可能正在进行的svg动画，并清除所有的SVG和Drawable相关数据 */
    void clear();

    /** 直接显示SVG */
    boolean showSvg(String svgData);
}
