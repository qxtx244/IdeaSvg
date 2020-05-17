package com.qxtx.idea.ideasvg.animation;

import com.qxtx.idea.ideasvg.SvgConsts;
import com.qxtx.idea.ideasvg.listener.AnimListener;

public final class SvgPathMovingAnim implements ISvgAnim {
    private long durationMs;
    private int destLen;
    private int destColor;
    private AnimListener listener;

    public SvgPathMovingAnim(long durationMs) {
        this(durationMs, SvgConsts.INVAILE_VALUE, SvgConsts.INVAILE_VALUE, null);
    }

    public SvgPathMovingAnim(long durationMs, int destLen, int destColor) {
        this(durationMs, destLen, destColor, null);
    }

    public SvgPathMovingAnim(long durationMs, int destLen, int destColor, AnimListener listener) {
        this.durationMs = durationMs;
        this.destLen = destLen;
        this.destColor = destColor;
        this.listener = listener;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public int getDestLen() {
        return destLen;
    }

    public void setDestLen(int destLen) {
        this.destLen = destLen;
    }

    public int getDestColor() {
        return destColor;
    }

    public void setDestColor(int destColor) {
        this.destColor = destColor;
    }

    public AnimListener getListener() {
        return listener;
    }

    public void setListener(AnimListener listener) {
        this.listener = listener;
    }

    @Override
    public int getCategory() {
        return SvgConsts.ANIM_PATH_MOVING;
    }
}
