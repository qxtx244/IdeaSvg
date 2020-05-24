package com.qxtx.idea.svg.animation;
import com.qxtx.idea.svg.listener.AnimListener;

public interface ISvgAnim {
    int getCategory();
    long getDurationMs();
    AnimListener getListener();
}
