package com.qxtx.idea.svg.animation;

import com.qxtx.idea.svg.SvgConsts;
import com.qxtx.idea.svg.listener.AnimListener;

/**
 * svg裁剪动画
 */
public final class SvgTrimAnim implements ISvgAnim {
    private final long durationMs;
    private final AnimListener listener;
    private final boolean isReverse;

    public SvgTrimAnim(long durationMs) {
        this(durationMs, null, false);
    }

    public SvgTrimAnim(long durationMs, AnimListener listener, boolean isReverse) {
        this.durationMs = Math.max(durationMs, 100);
        this.listener = listener;
        this.isReverse = isReverse;
    }

    public AnimListener getListener() {
        return listener;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public boolean isReverse() {
        return isReverse;
    }

    @Override
    public int getCategory() {
        return SvgConsts.ANIM_CLIPPING;
    }
}
