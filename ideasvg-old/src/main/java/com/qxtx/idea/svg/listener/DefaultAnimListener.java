package com.qxtx.idea.svg.listener;

/**
 * @author QXTX-WIN
 * @date 2020/1/6 23:29
 * Description: 默认的动画监听器，本身不实现任何功能
 */
public class DefaultAnimListener implements AnimListener {
    @Override
    public void onAnimStart() {
    }

    @Override
    public void onAnimEnd() {
    }

    @Override
    public void onAnimCancel() {
    }

    @Override
    public boolean onAnimProgress(float fraction) {
        return true;
    }
}
