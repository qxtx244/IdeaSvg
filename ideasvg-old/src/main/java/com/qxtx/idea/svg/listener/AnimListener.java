package com.qxtx.idea.svg.listener;

/**
 * @author QXTX-WIN
 * @date 2020/1/5 14:28
 * Description:
 */
public interface AnimListener {

    /**
     * 动画开始
     */
    void onAnimStart();

    /**
     * 动画完成
     */
    void onAnimEnd();

    /**
     * 动画被关闭，可能属于非自然结束，而是在动画过程中被强制关闭
     */
    void onAnimCancel();

    /**
     * 动画进度
     * @param fraction 数值范围为 0f~1f
     * @return [false] 将会使本次动画截止   [true] 动画继续进行
     */
    boolean onAnimProgress(float fraction);
}
