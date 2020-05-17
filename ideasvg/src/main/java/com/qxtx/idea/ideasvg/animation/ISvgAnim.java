package com.qxtx.idea.ideasvg.animation;

import com.qxtx.idea.ideasvg.listener.AnimListener;

public interface ISvgAnim {
    int getCategory();
    long getDurationMs();
    AnimListener getListener();
}
