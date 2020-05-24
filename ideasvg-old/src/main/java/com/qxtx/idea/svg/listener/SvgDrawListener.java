package com.qxtx.idea.svg.listener;

import android.graphics.Paint;

/**
 * @author QXTX-WIN
 * @date 2019/12/25 23:39
 * Description: 每条子路径的绘制，将会在开始绘制和结束绘制时触发回调
 */
public interface SvgDrawListener {
    void onPathStart(int sequence, Paint paint);

    void onPathEnd(int pathSequence);
}
