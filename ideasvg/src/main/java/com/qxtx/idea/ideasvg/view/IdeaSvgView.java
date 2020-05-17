package com.qxtx.idea.ideasvg.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import com.qxtx.idea.ideasvg.R;
import com.qxtx.idea.ideasvg.SvgConsts;
import com.qxtx.idea.ideasvg.animation.ISvgAnim;
import com.qxtx.idea.ideasvg.animation.SvgTrimAnim;
import com.qxtx.idea.ideasvg.listener.AnimListener;
import com.qxtx.idea.ideasvg.listener.SvgDrawListener;
import com.qxtx.idea.ideasvg.parser.SvgDataParser;
import com.qxtx.idea.ideasvg.tools.DeepCopy;
import com.qxtx.idea.ideasvg.tools.SvgLog;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author QXTX-WIN
 * @date 2019/12/3 21:39
 * Description: 提供svg支持的view
 *
 * 1、对svg数值实时变化动画：允许和其他动画同时进行
 * 3、svg手势操作：不允许进行任何其他动画，包括外部触摸监听事件
 * 4、形变动画：允许和其他动画同时进行
 *
 * 备注：svg手势功能仍未完善，很多功能也并未反复测试，不想搞了有空再说
 */
public class IdeaSvgView extends View implements ISvgView, ISvgViewExtend {
    private final String TAG = getClass().getSimpleName();

    private List<SvgAnimation> mSvgAnimationList = null;
    private float mCenterX = SvgConsts.INVAILE_VALUE, mCenterY = SvgConsts.INVAILE_VALUE;

    /** svg缩放的最小值 */
    private final float SCALE_MIN_VALUE = 0.1f;
    /** svg缩放的最大值 */
    private final float SCALE_MAX_VALUE = 25f;

    /**
     * 大端序
     * 大端顺序位字段定义：
     * 0 未定义
     * 0 未定义
     * 0 未定义
     * 0 未定义
     * 0 未定义
     * 0 缩放手势位。[0]当前未使用缩放手势 [1]正在使用缩放手势
     * 0 手势状态位。[0]未处于手势执行状态 [1]正在执行手势
     * 0 手势功能控制位。[0]禁止使用svg手势功能 [1]允许svg手势功能。
     *     并且此位置1时，会立即中断当前的svg动画。默认置0。见{@link #setSvgGestureEnable(boolean)}。
     */
    private byte mGestureStatus;

    private OnTouchListener mTouchListener = null;

    /**
     * 平移量，在替换svg之后，必须重置此值
     * 取值范围(-∞,+∞)
     */
    private float mTranslateX, mTranslateY;

    /**
     * 缩放量，在替换svg/Drawable之后，必须重置此值
     * 取值范围(0f,+∞)
     */
    private float mScale;

    /** 最原始的svg数据集 */
    private LinkedHashMap<String, float[]> mOriMap;

    /** 透明度，在替换svg/Drawable之后，必须重置此值。取值范围[0,255] */
    private int mAlpha;
    
    private final RectF mRectF = new RectF();

    private Drawable mDrawable = null;

    /** Drawable可设置的style见{@link DrawableStyle} */
    private int mDrawableStyle;

    /** xml中设定的宽模式 */
    private int mWidthMode;

    /** xml中设定的高模式 */
    private int mHeightMode;

    /** SVG是否处于动画状态 */
    private volatile boolean mIsSvgAnimRunning;

    private ValueAnimator mValueAnim;

    /** 和{@link #mSvgPathList}互相绑定的，如果此数据集变动，必须清理绑定的对象 */
    private LinkedHashMap<String, float[]> mSvgMap;

    /** 和{@link #mSvgMap}互相绑定 */
    private ArrayList<Path> mSvgPathList;

    private SvgDrawListener mSvgDrawListener;

    /** 绘制模式，见{@link SvgStyle} */
    private int mSvgStyle = SVG_OUTLINE;

    /**
     * svg位置是否居中绘制，默认true
     * @see #setSvgCenter(boolean)
     */
    private boolean mIsForceCenter;

    private float mStokeWidth;

    /** 线条颜色库 */
    private int[] mOutlineColors;

    /** 填充颜色库 */
    private int[] mFillColors;

    private SvgDataParser mParser;

    private Paint mPaint;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DRAWABLE_AUTO, DRAWABLE_FILL, DRAWABLE_FIT_XY})
    public @interface DrawableStyle{}
    public static final int DRAWABLE_AUTO = 0x1;
    public static final int DRAWABLE_FILL = DRAWABLE_AUTO << 1;
    public static final int DRAWABLE_FIT_XY = DRAWABLE_AUTO << 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SVG_FILL, SVG_OUTLINE, SVG_OUTLINE_AND_FILL})
    public @interface SvgStyle{}
    public static final int SVG_OUTLINE = 0x1;
    public static final int SVG_FILL = SVG_OUTLINE << 1;
    public static final int SVG_OUTLINE_AND_FILL = SVG_FILL | SVG_OUTLINE;

    private final int DEFAULT_OUTLINE_WIDTH = 3;
    private final String DEFAULT_OUTLINE_COLOR = "#FF0000";
    private final String DEFAULT_FILL_COLOR = "#FF0000";

    public IdeaSvgView(Context context) {
        this(context, null);
    }

    public IdeaSvgView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IdeaSvgView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public IdeaSvgView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    /**
     * 从attr中获得各种预设的属性值，如果没找到，则使用默认的属性值
     * 1、属性获取优先顺序：xml直接定义 >> xml的style属性定义 >> 构造方法里defStyleAttr定义 >> 构造方法里defStyleRes定义 >> theme中直接定义
     */
    private void init(Context context, AttributeSet attrs) {
        mParser = new SvgDataParser();
        mPaint = new Paint();
        mSvgPathList = new ArrayList<>();

        mGestureStatus = 0x00;

        clear();

        //获得各种属性值
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IdeaSvgView);
        try {
            mWidthMode = a.getLayoutDimension(R.styleable.IdeaSvgView_android_layout_width, ViewGroup.LayoutParams.WRAP_CONTENT);
            mHeightMode = a.getLayoutDimension(R.styleable.IdeaSvgView_android_layout_height, ViewGroup.LayoutParams.WRAP_CONTENT);

            String svgData = a.getString(R.styleable.IdeaSvgView_svgData);
            mSvgMap = TextUtils.isEmpty(svgData) ? new LinkedHashMap<String, float[]>() : mParser.svgString2Map(svgData);
            mOriMap = DeepCopy.svgMap(mSvgMap);
            mIsForceCenter = a.getBoolean(R.styleable.IdeaSvgView_forceCenter, true);
            mSvgStyle = a.getInt(R.styleable.IdeaSvgView_svgStyle, SVG_OUTLINE);
            mDrawableStyle = a.getInt(R.styleable.IdeaSvgView_drawableStyle, DRAWABLE_AUTO);
            mStokeWidth = a.getInt(R.styleable.IdeaSvgView_outlineWidthPx, DEFAULT_OUTLINE_WIDTH);
            int color = a.getColor(R.styleable.IdeaSvgView_outlineColor, Color.parseColor(DEFAULT_OUTLINE_COLOR));
            mOutlineColors = new int[]{color};
            color = a.getColor(R.styleable.IdeaSvgView_outlineColor, Color.parseColor(DEFAULT_FILL_COLOR));
            mFillColors = new int[] {color};

            if (mSvgMap.size() == 0) {
                Drawable src = a.getDrawable(R.styleable.IdeaSvgView_src);
                if (src != null) {
                    setDrawable(src);
                }
            }
        } catch (Exception e) {
            SvgLog.I("读取attr发生异常：" + e.getMessage());
            e.printStackTrace();

            mOutlineColors = new int[] {Color.parseColor(DEFAULT_OUTLINE_COLOR)};
            mFillColors = new int[] {Color.parseColor(DEFAULT_FILL_COLOR)};
        }
        a.recycle();

        mIsSvgAnimRunning = false;

        //默认为绘制线条
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mOutlineColors[0]);
        mPaint.setStrokeWidth(mStokeWidth);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);

        if (hasSvgDraw()) {
            refresh();
        }
    }

    @Override
    public void invalidate() {
        //形变动画不强制刷新path列表
        if (mSvgAnimationList == null || mSvgAnimationList.size() == 0) {
            getPathListBeforeInvalidate();
        }
        fixWidthAndHeight();
        super.invalidate();
    }

    @Override
    public void postInvalidate() {
        if (isUiThread()) {
            invalidate();
            return ;
        }

        //形变动画不强制刷新path列表
        if (mSvgAnimationList == null || mSvgAnimationList.size() == 0) {
            getPathListBeforeInvalidate();
        }
        post(this::fixWidthAndHeight);
        super.postInvalidate();
    }

    /** invalidate/postInvalidate会先被执行 */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //如有需要，在此拿到控件的宽高测量模式，但现在暂时不需要
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        long time = System.currentTimeMillis();

        //svg和drawable不能共存，svg优先级高于drawable；并且在特殊图形绘制完成，需要恢复canvas的方位变换状态
        canvas.save();
        if (mSvgPathList != null && mSvgPathList.size() > 0) {
            onSvgDraw(canvas);
        } else if (mDrawable != null) {
            onDrawableDraw(canvas);
        }
        canvas.restore();

//        SvgLog.I("onDraw() time=" + (System.currentTimeMillis() - time) + "ms");
    }

    @Override
    public boolean showSvg(@NonNull String svgData) {
        LinkedHashMap<String, float[]> svgMap = mParser.svgString2Map(svgData);
        if (svgMap == null || svgMap.size() == 0) {
            SvgLog.I("错误的svg数据");
            return false;
        }

        showSvgImpl(svgMap, SvgConsts.INVAILE_VALUE, null);

        return true;
    }

    @Override
    public boolean showSvg(@NonNull String svgData, long durationMs, AnimListener listener) {
        LinkedHashMap<String, float[]> svgMap = mParser.svgString2Map(svgData);
        if (svgMap == null || svgMap.size() == 0) {
            SvgLog.I("非法的svg数据");
            return false;
        }

        if (!hasSvgDraw()) {
            SvgLog.I("没有svg被绘制，忽略svg变换");
            return false;
        }

        if (isGesturePlaying()) {
            SvgLog.I("正在使用svg手势，svg变换动画被忽略");
            return false;
        }

        boolean isInvalidDuration = durationMs <= 0;
        if (isInvalidDuration) {
            SvgLog.I("过短的动画时间，无动画切换到新的svg");
            showSvgImpl(svgMap, SvgConsts.INVAILE_VALUE, null);
            return true;
        }

        //当动画切换的条件已经满足，但不是互为同型svg，视为无动画切换
        boolean isSimilarSvg = mSvgMap.keySet().equals(svgMap.keySet());
        if (!isSimilarSvg) {
            SvgLog.I("不是同型svg，不是用动画，而是直接替换svg");
            showSvgImpl(svgMap, SvgConsts.INVAILE_VALUE, null);
            return true;
        }

        if (isSvgAnimRunning()) {
            SvgLog.I("当前正在进行svg动画！不推荐同时执行多个svg动画");
//            return false;
            stopSvgAnim();
        }

        //更新svg之前，清除所有的drawable和svg（包括svg动画）
        resetSvgParams();

        showSvgImpl(svgMap, durationMs, listener);

        return true;
    }
    
    private void showSvgImpl(LinkedHashMap<String, float[]> destSvgMap, long durationMs, AnimListener listener) {
        if (durationMs <= 0) {
            //更新svg之前，清除所有的drawable和svg（包括svg动画）
            clear();

            //深拷贝，变成私有数据，java的clone()方法无法实现深拷贝！
//        long durationMs = System.nanoTime();
            mSvgMap = DeepCopy.svgMap(destSvgMap);
            mOriMap = DeepCopy.svgMap(destSvgMap);
//        SvgLog.I("深拷贝数据。耗时=" + (System.nanoTime() - durationMs) + "ns");

            refresh();
            return ;
        }

        clearOldData();

        //备份切换前的svgMap
        LinkedHashMap<String, float[]> oldSvgMap = DeepCopy.svgMap(mSvgMap);

        ValueAnimator animator = ValueAnimator.ofFloat(1f, 10f);
        animator.setDuration(durationMs).setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();

            Iterator<float[]> curIterator = mSvgMap.values().iterator();
            Iterator<float[]> oriIterator = oldSvgMap.values().iterator();
            Iterator<float[]> destIterator = destSvgMap.values().iterator();
            while (curIterator.hasNext()) {
                float[] curValues = curIterator.next();
                float[] oriValues = oriIterator.next();
                float[] destValues = destIterator.next();
                if (curValues == null || destValues == null) {
                    SvgLog.I("发现异常数据，svg变换失败了，复原svg");
                    mSvgMap = oldSvgMap;
                    return ;
                }

                for (int i = 0; i < curValues.length; i++) {
                    curValues[i] = oriValues[i] + (destValues[i] - oriValues[i]) * fraction;
                }
            }

            if (listener != null) {
                if (!listener.onAnimProgress(fraction)) {
                    animation.cancel();
                }
            }

            refresh();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);

                if (listener != null) {
                    listener.onAnimCancel();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                //svg替换完成，备份原始数据
                mOriMap = DeepCopy.svgMap(mSvgMap);

                if (listener != null) {
                    listener.onAnimEnd();
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);

                if (listener != null) {
                    listener.onAnimStart();
                }
            }
        });
        animator.start();
    }

    /** 与svg数值动画互斥 */
    @Override
    public boolean startSvgAnim(ISvgAnim anim) {
        if (anim == null) {
            return false;
        }

        if (!hasSvgDraw()) {
            SvgLog.I("未发现svg绘制，忽略svg动画请求");
            return false;
        }

        if (isGesturePlaying()) {
            SvgLog.I("正在使用svg手势，忽略svg动画请求");
            return false;
        }

        if (isSvgAnimRunning()) {
            SvgLog.I("正在执行svg动画，不建议同时进行形变动画");
            stopSvgAnim();
        }

        if (mSvgAnimationList == null) {
            mSvgAnimationList = new ArrayList<>();
        }

        SvgAnimation svgAnimation = new SvgAnimation(anim);
        mSvgAnimationList.add(svgAnimation);
        svgAnimation.start();

        return false;
    }

    /**
     * svg手势功能和view触摸监听功能互相冲突。当设置了其中一个，另一个将会暂时失效；
     *  当取消其中一个，被屏蔽的另一个将会尽可能地被恢复。
     */
    @Override
    public void setOnTouchListener(OnTouchListener l) {
        if (l == null) {
            mTouchListener = null;
            //当外部需要清除触摸监听时，判断是否需要恢复svg手势功能
            if ((mGestureStatus & 0x1) != 0) {
                l = new SvgGestureListener();
            }
        }

        super.setOnTouchListener(l);
    }

    @Override
    public void setSvgGestureEnable(boolean gestureEnabled) {
        if (gestureEnabled) {
            mGestureStatus = (byte)(mGestureStatus | 0x1);
            //正在使用svg手势时，禁止变更svg手势启用状态
            if (!isGesturePlaying()) {
                setOnTouchListener(new SvgGestureListener());
            }
        } else {
            mGestureStatus = 0x0;
            //如果存在外部的触摸监听器，恢复这个监听
            if (mTouchListener != null) {
                setOnTouchListener(mTouchListener);
            }
        }
    }

    @Override
    public void setSvgCenter(boolean forceCenter) {
        this.mIsForceCenter = forceCenter;
    }

    @Override
    public void setStokeColor(int... color) {
        if (color == null || color.length == 0) {
            return ;
        }

        mOutlineColors = color;
    }

    @Override
    public void setFillColor(int... color) {
        if (color == null || color.length == 0) {
            return ;
        }

        mFillColors = color;
    }

    @Override
    public void setStokeWidth(float px) {
        if (px <= 0f) {
            return ;
        }
        mStokeWidth = px;
    }

    @Override
    public boolean isGesturePlaying() {
        return (mGestureStatus & 0x3) == 0x3;
    }

    @Override
    public boolean isSvgAnimRunning() {
        return mIsSvgAnimRunning;
    }

    @Override
    public boolean isGestureEnable() {
        return (mGestureStatus & 0x1) != 0;
    }

    @Override
    public void setSvgMode(@SvgStyle int style) {
        this.mSvgStyle = style;
    }

    @Override
    public float getSvgScale() {
        return hasSvgDraw() ? SvgConsts.INVAILE_VALUE : mScale;
    }

    @Override
    public float[] getSvgTranslate() {
        return hasSvgDraw() ? new float[] {SvgConsts.INVAILE_VALUE, SvgConsts.INVAILE_VALUE} : new float[] {mTranslateX, mTranslateY};
    }

    @Override
    public int getSvgAlpha() {
        return hasSvgDraw() ? SvgConsts.INVAILE_VALUE : mAlpha;
    }

    @Override
    public void setDrawableStyle(@DrawableStyle int style) {
        mDrawableStyle = style;
        setDrawable(mDrawable);
    }

    @Override
    public void refresh() {
        if (isUiThread()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    @Override
    public void setSvgDrawListener(@NonNull SvgDrawListener svgDrawListener) {
        this.mSvgDrawListener = svgDrawListener;
    }

    @Override
    public boolean svgAlpha(int value, long durationMs, AnimListener listener) {
        if (isGesturePlaying()) {
            SvgLog.I(")正在使用svg手势，缩放动画被忽略");
            return false;
        }

        if (value < 0 || value > 255) {
            SvgLog.I("非法的透明度");
            return false;
        }

        if (value == mAlpha) {
            SvgLog.I("透明度未发生变化");
            return false;
        }

        if (!hasSvgDraw()) {
            SvgLog.I("没有svg被绘制，忽略透明度变化");
            return false;
        }

        if (isSvgAnimRunning()) {
            SvgLog.I("当前正在进行svg动画！不推荐同时执行多个svg动画");
//            return false;
        }

        int oldAlpha = mAlpha;
        mAlpha = value;

        if (durationMs <= 0) {
            alphaWithoutAnim();
        } else {
            alphaWithAnim(oldAlpha, durationMs, listener);
        }

        return true;
    }

    @Override
    public boolean svgTranslate(float x, float y, long durationMs, @Nullable AnimListener listener) {
        if (isGesturePlaying()) {
            SvgLog.I("正在使用svg手势，缩放动画被忽略");
            return false;
        }

        if (mTranslateX == x && mTranslateY == y) {
            SvgLog.I("未发生平移");
            return false;
        }

        if (!hasSvgDraw()) {
            SvgLog.I("没有svg被绘制，忽略平移");
            return false;
        }

        if (isSvgAnimRunning()) {
            SvgLog.I("当前正在进行svg动画！不推荐同时执行多个svg动画");
//            return false;
        }

        mTranslateX = x;
        mTranslateY = y;

        if (durationMs <= 0) {
            translateWithoutAnim();
        } else {
            translateWithAnim(durationMs, listener);
        }

        return true;
    }

    @Override
    public boolean svgScale(float scale, long durationMs, @Nullable AnimListener listener) {
        if (isGesturePlaying()) {
            SvgLog.I("正在使用svg手势，缩放动画被忽略");
            return false;
        }

        if (scale <= 0f || mScale == scale) {
            SvgLog.I("无效的缩放值");
            return false;
        }

        if (!hasSvgDraw()) {
            SvgLog.I("没有svg被绘制，忽略缩放");
            return false;
        }

        if (isSvgAnimRunning()) {
            SvgLog.I("当前正在进行svg动画！不推荐同时执行多个svg动画");
//            return false;
        }

        if (scale > SCALE_MAX_VALUE) {
            SvgLog.I("超出了允许的缩放范围");
            scale = SCALE_MAX_VALUE;
        } else if (scale < SCALE_MIN_VALUE) {
            SvgLog.I("超出了允许的缩放范围");
            scale = SCALE_MIN_VALUE;
        }

        //本次具体缩放值
        final float curScale = scale;
        if (isUiThread()) {
            scaleImpl(curScale, durationMs, listener);
        } else {
            post(() -> {
                scaleImpl(curScale, durationMs, listener);
            });
        }

        return true;
    }

    @Override
    public boolean fillSvg(int[] colors, boolean isAnim) {
        return false;
    }

    @Override
    public void setDrawable(Drawable drawable) {
        if (mDrawable == drawable) {
            SvgLog.I("不绘制相同的Drawable");
            return ;
        }

        clear();

        mDrawable = drawable;

        int drawableW = drawable.getIntrinsicWidth();
        int drawableH = drawable.getIntrinsicHeight();
        //必须为drawable设置bounds才会显示，否则bounds中ltrb全为0
        //LYX_TAG 2020/1/16 0:46 在init()中拿到xml属性参数时，这里过早拿到的控件w和h都为0。暂时不知道怎么处理，可以参考一下ImageView
        int vWidth = getWidth() == 0 ? drawableW : getWidth();
        int vHeight = getHeight() == 0 ? drawableH : getHeight();
        int l = 0, t = 0, r = drawableW, b = drawableH;
        switch (mDrawableStyle) {
            case DRAWABLE_AUTO:
                break;
            case DRAWABLE_FILL:
                if (drawableW > vWidth) {
                    r = vWidth;
                }
                if (drawableH > vHeight) {
                    b = vHeight;
                }
                break;
            case DRAWABLE_FIT_XY:
                r = vWidth == 0 ? drawableW : vWidth;
                b = vHeight == 0 ? drawableH : vHeight;
                break;
        }
        mDrawable.setBounds(new Rect(l, t, r, b));

        refresh();
    }

    @Override
    public void finishSvgAnim() {
        if (mValueAnim != null) {
            mValueAnim.end();
        }
        //即使动画的监听回调中做了，这里也做，以积极地接受下一个可能的动画指令
        mIsSvgAnimRunning = false;
    }

    @Override
    public void stopSvgAnim() {
        if (mValueAnim != null && mValueAnim.isStarted()) {
            mValueAnim.cancel();
            mValueAnim = null;
        }

        if (mSvgAnimationList != null && mSvgAnimationList.size() > 0) {
            for (SvgAnimation anim : mSvgAnimationList) {
                anim.cancel();
            }
            mSvgAnimationList.clear();
            mSvgAnimationList = null;
        }

        //即使动画的监听回调中有处理，这里也置一次false，以积极地接受下一个可能的动画指令
        mIsSvgAnimRunning = false;
    }

    @Override
    public void clear() {
        clearOldData();

        if (hasSvgDraw()) {
            mSvgMap.clear();
        }
    }

    /** 不清除当前的svgMap数据，因为需要用来做svg变换动画 */
    private void clearOldData() {
        clearDrawable();

        stopSvgAnim();
        clearSvgData();

        mCenterX = SvgConsts.INVAILE_VALUE;
        mCenterY = SvgConsts.INVAILE_VALUE;
        mScale = 1f;
        mTranslateX = 0f;
        mTranslateY = 0f;
        mAlpha = 255;
    }

    private boolean isUiThread() {
        return Looper.myLooper() != null && (Looper.myLooper() == Looper.getMainLooper());
    }

    private void onSvgDraw(Canvas canvas) {
        float l = Float.MAX_VALUE, t = Float.MAX_VALUE, r = Float.MIN_VALUE, b = Float.MIN_VALUE;
        //检查svg位置
        for (Path p : mSvgPathList) {
            p.computeBounds(mRectF, true);
            l = mRectF.left < l ? mRectF.left : l;
            t = mRectF.top < t ? mRectF.top : t;
            r = mRectF.right > r ? mRectF.right : r;
            b = mRectF.bottom > b ? mRectF.bottom : b;
        }

        if (mIsForceCenter) {
            forceCenter(l, t, r, b, canvas);
        } else {
            fixPadding(l, t, canvas);
        }

        //处理平移，因为是平移canvas的参照原点
        if (mTranslateX != 0f || mTranslateY != 0f) {
            canvas.translate(mTranslateX, mTranslateY);
        }

        for (int i = 0; i < mSvgPathList.size(); i++) {
            drawPath(canvas, i, mSvgPathList.get(i));
        }
    }

    private void onDrawableDraw(Canvas canvas) {
        SvgLog.I("开始绘制drawable");
        //检查drawable的位置
        Rect rect = mDrawable.getBounds();
        if (mIsForceCenter) {
            forceCenter(rect.left, rect.top, rect.right, rect.bottom, canvas);
        } else {
            fixPadding(rect.left, rect.top, canvas);
        }

        //LYX_TAG 2020/1/18 12:26 仅仅允许特定的svg使用平移，对设置的drawable无效
//            //处理drawable平移
//            canvas.translate(mTranslateX, mTranslateY);

        mDrawable.draw(canvas);
    }

    private boolean hasSvgDraw() {
        return mSvgMap != null && mSvgMap.size() > 0;
    }

    private void resetSvgParams() {
        mTranslateX = 0f;
        mTranslateY = 0f;
        mScale = 1f;
        mAlpha = 255;

//        setOnTouchListener(new SvgGestureListener());
    }

    /** 清除svg数据，用于需要绘制Drawable的时候 */
    private void clearSvgData() {
        if (hasSvgDraw()) {
            mOriMap.clear();
        }
        if (mSvgPathList != null && mSvgPathList.size() > 0) {
            mSvgPathList.clear();
        }

        resetSvgParams();
    }

    /** 清除Drawable数据，用于需要绘制SVG的时候 */
    private void clearDrawable() {
        mDrawable = null;
    }

    /** 在{@link #invalidate()}或者{@link #postInvalidate()}开始时，立即将svg数据集转换成svg路径集 */
    private void getPathListBeforeInvalidate() {
        if (hasSvgDraw()) {
            mSvgPathList = mParser.createSvgPath(mSvgMap);
            if (mSvgPathList == null) {
                SvgLog.I("无法生成完整的svg路径集");
            }
        }
    }

    private void alphaWithoutAnim() {
        SvgLog.I("直接做透明度改变，无动画");
        refresh();
    }

    private void alphaWithAnim(int oldAlpha, long durationMs, AnimListener listener) {
        SvgLog.I("存在动画时长，使用动画透明度变化：" + oldAlpha + ">" + mAlpha);
//        stopSvgAnim();

        mIsSvgAnimRunning = true;

        int destAlpha = mAlpha;
        mValueAnim = ValueAnimator.ofInt(oldAlpha, destAlpha).setDuration(durationMs);
        mValueAnim.setInterpolator(new LinearInterpolator());
        mValueAnim.addUpdateListener(animation -> {
            if (!isSvgAnimRunning()) {
                return ;
            }

            float fraction = animation.getAnimatedFraction();
            int curValue = (int)animation.getAnimatedValue();

            mAlpha = curValue;

            if (listener != null) {
                if (!listener.onAnimProgress(fraction)) {
                    animation.cancel();
                }
            }

            refresh();
        });

        //给外部回调
        mValueAnim.addListener(new SvgAnimListener(listener));

        mValueAnim.start();
    }

    /** 直接平移 */
    private void translateWithoutAnim() {
        SvgLog.I("直接做平移，无动画");
        refresh();
    }

    /** 动画平移 */
    private void translateWithAnim(long durationMs, AnimListener listener) {
        SvgLog.I("存在动画时长，使用动画平移");
//        stopSvgAnim();

        mIsSvgAnimRunning = true;

        final float destX = mTranslateX;
        final float destY = mTranslateY;
        mValueAnim = ValueAnimator.ofFloat(0f, 1f).setDuration(durationMs);
        mValueAnim.setInterpolator(new LinearInterpolator());
        mValueAnim.addUpdateListener(animation -> {
            if (!isSvgAnimRunning()) {
                return ;
            }

            float fraction = animation.getAnimatedFraction();

            mTranslateX = fraction * destX;
            mTranslateY = fraction * destY;

            if (listener != null) {
                if (!listener.onAnimProgress(fraction)) {
                    animation.cancel();
                }
            }

            refresh();
        });

        //给外部回调
        mValueAnim.addListener(new SvgAnimListener(listener));

        mValueAnim.start();
    }

    private void scaleImpl(float scale, long durationMs, AnimListener listener) {
        stopSvgAnim();

        //无动画时长，不做动画，直接显示
        if (durationMs <= 0) {
            scaleWithoutAnim(scale);
        } else {
            scaleWithAnim(scale, durationMs, listener);
        }
    }

    /** 直接缩放 */
    private void scaleWithoutAnim(float scale) {
        for (String k : mSvgMap.keySet()) {
            float[] values = mSvgMap.get(k);
            float[] oldValues = mOriMap.get(k);
            if (values == null) {
                SvgLog.I("发现非法数据");
                //恢复原有的svg数据集
                mSvgMap = DeepCopy.svgMap(mOriMap);
                return ;
            }
            for (int i = 0; i < values.length; i++) {
                values[i] = oldValues[i] * scale;
            }
        }

        mScale = scale;

        refresh();
    }

    /** 使用动画缩放 */
    private void scaleWithAnim(float scale, long durationMs, AnimListener listener) {
        mIsSvgAnimRunning = true;

        final float oldScale = mScale;

        mScale = scale;

        mValueAnim = ValueAnimator.ofFloat(oldScale, scale).setDuration(durationMs);
        mValueAnim.setInterpolator(new LinearInterpolator());
        mValueAnim.addUpdateListener(animation -> {
            float curScale = (float)animation.getAnimatedValue();

            if (!mIsSvgAnimRunning) {
                SvgLog.I("动画已经结束，停止ValueAnimator");
                animation.cancel();
                return ;
            }

            for (String k : mSvgMap.keySet()) {
                float[] values = mSvgMap.get(k);
                if (values == null) {
                    SvgLog.I("缩放过程中发现错误数值，中断缩放，恢复原有svg");
                    animation.cancel();
                    mScale = oldScale;
                    mSvgMap = DeepCopy.svgMap(mOriMap);
                    return ;
                }

                float[] oldValues = mOriMap.get(k);
                if (oldValues != null) {
                    for (int i = 0; i < values.length; i++) {
                        values[i] = oldValues[i] * curScale;
                    }
                }
            }

            if (listener != null) {
                if (!listener.onAnimProgress(animation.getAnimatedFraction())) {
                    animation.cancel();
                }
            }

            refresh();
        });

        mValueAnim.addListener(new SvgAnimListener(listener));

        mValueAnim.start();
    }

    /**
     * 绘制一个Path
     * @param canvas 目标canvas
     * @param index path在整个svg中的绘制顺序
     * @param p 目标path
     *
     * @see #onDraw(Canvas)
     */
    private void drawPath(Canvas canvas, int index, Path p) {
        int color;
        switch (mSvgStyle) {
            case SVG_OUTLINE:
                color = mOutlineColors.length > index ? mOutlineColors[index] : mOutlineColors[0];
                drawPathImpl(canvas, index, p, Paint.Style.STROKE, color);
                break;
            case SVG_FILL:
                color = mFillColors.length > index ? mFillColors[index] : mFillColors[0];
                drawPathImpl(canvas, index, p, Paint.Style.FILL, color);
                break;
            case SVG_OUTLINE_AND_FILL:
                color = mOutlineColors.length > index ? mOutlineColors[index] : mOutlineColors[0];
                drawPathImpl(canvas, index, p, Paint.Style.STROKE, color);

                color = mFillColors.length > index ? mFillColors[index] : mFillColors[0];
                drawPathImpl(canvas, index, p, Paint.Style.FILL, color);
                break;
            default:
                break;
        }
    }

    /**
     * 在执行动画时，不为外部提供回调，需要靠动画api传入监听器得到回调
     * @see #drawPath(Canvas, int, Path)
     */
    private void drawPathImpl(Canvas canvas, int index, Path path, Paint.Style style, int color) {
        mPaint.setStyle(style);
        mPaint.setColor(color);
        //setColor()也包含alpha值，因此会覆盖setAlpha()的效果，因此setAlpha()必须在setColor()之后调用
        mPaint.setAlpha(mAlpha);

        if (mSvgDrawListener != null) {
            if (!isSvgAnimRunning()) {
                mSvgDrawListener.onPathStart(index, mPaint);
            }
        }

        canvas.drawPath(path, mPaint);

        if (mSvgDrawListener != null) {
            if (!isSvgAnimRunning()) {
                mSvgDrawListener.onPathEnd(index);
            }
        }
    }

    /**
     * 修正设置为wrap_content的控件宽高，改为svg/drawable的尺寸 + 绘制修正值（Drawable的这个值仅为假设值）
     */
    private void fixWidthAndHeight() {
        boolean isWidthWrapContent = mWidthMode == ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean isHeightWrapContent = mHeightMode == ViewGroup.LayoutParams.WRAP_CONTENT;

        if (!isWidthWrapContent && !isHeightWrapContent) {
//            SvgLog.I("取最大值或者指定值，不需要参考svg的尺寸");
            return ;
        }

        //处理等级：SVG > Drawable
        if (mSvgPathList != null && mSvgPathList.size() > 0) {
            float l = 0f, t = 0f, r = 0f, b = 0f;
            RectF bounds = new RectF();
            for (Path p : mSvgPathList) {
                p.computeBounds(bounds, true);
                l = bounds.left < l ? bounds.left : l;
                t = bounds.top < t ? bounds.top : t;
                r = bounds.right > r ? bounds.right : r;
                b = bounds.bottom > b ? bounds.bottom : b;
            }

            //视觉优化：额外加上{@link #mStokeWidth}的大小是因为发现如果刚好占满控件尺寸的话，边缘弧线会超出一点点控件空间
            //先乘再除，表示精确值取到小数点后一位
            if (isWidthWrapContent) {
                getLayoutParams().width = (int) (r - l) + (int) (mStokeWidth * 10) / 10 + Math.max(getPaddingLeft(),getPaddingRight());
            }
            if (isHeightWrapContent) {
                getLayoutParams().height = (int) (b - t) + (int) (mStokeWidth * 10) / 10 + Math.max(getPaddingTop(),getPaddingBottom());
            }
        } else {
        }

        if (isWidthWrapContent || isHeightWrapContent) {
            requestLayout();
        }
    }

    /**
     * 手动实现控件的padding，总是优先满足右移和下移
     * 如果经过左移，绘制内容到控件左端距离仍然大于右移值，则左移也生效；否则不再偏移;
     * 如果经过上移，绘制内容到控件顶端距离仍然大于下移值，则上移也生效；否则不再偏移
     */
    private void fixPadding(float l, float t, Canvas canvas) {
        canvas.translate(getPaddingLeft(), getPaddingTop());

        if (l >= getPaddingRight() && getPaddingRight() != 0f) {
            canvas.translate(-getPaddingRight(), 0f);
        }

        if (t >= getPaddingBottom() && getPaddingBottom() != 0f) {
            canvas.translate(0f, -getPaddingBottom());
        }
    }

    /** 计算得到所有path的边界坐标最值，实现绘制内容强制居中 */
    private void forceCenter(float l, float t, float r, float b, Canvas canvas) {
        //允许强制居中条件：不处于形变动画状态
        if ((mSvgAnimationList == null || mSvgAnimationList.size() == 0 || !mSvgAnimationList.get(0).isRunning())) {
            float translateX = ((getWidth() - r) + l) / 2f - l;
            float translateY = ((getHeight() - b) + t) / 2f - t;
            mCenterX = translateX;
            mCenterY = translateY;
        }

        canvas.translate(mCenterX, mCenterY);
    }

    /** 恢复外部触摸监听 */
    private void restoreTouchEventOutside() {
        setOnTouchListener(mTouchListener);
    }

    /** svg动画监听 */
    private class SvgAnimListener extends AnimatorListenerAdapter {
        private AnimListener listener;

        private SvgAnimListener(AnimListener listener) {
            this.listener = listener;
        }

        @Override
        public void onAnimationStart(Animator animation) {
            super.onAnimationStart(animation);
            SvgLog.I("svg anim start");
            mIsSvgAnimRunning = true;

            if (listener != null) {
                listener.onAnimStart();
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            SvgLog.I("svg anim cancel");
            mIsSvgAnimRunning = false;

            if (listener != null) {
                listener.onAnimCancel();
            }
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            SvgLog.I("anim end");
            mIsSvgAnimRunning = false;

            if (listener != null) {
                listener.onAnimEnd();
            }
        }
    }

    /**
     * SVG手势监听
     * down事件：在处理事件之前就已经更新触摸点个数了
     * up事件：在处理事件之后才更新触摸事件产生的后果
     */
    private class SvgGestureListener implements OnTouchListener {

        private float scaleOnce;

        /** 当此值为true时，在触摸结束的时候，将会复原svg */
        private boolean shouldBeRestore = false;

        /** 开始缩放的两手势点的最小相对距离，当小于此值，不做缩放 */
        private final int SVG_SCALE_START_DISTANCE = 100;
        /** svg手势缩放的触摸点个数 */
        private final int GESTURE_SVG_SCALE = 2;
        /** 防触摸点抖动，单次缩放最小有效倍率 */
        private final float SCALE_TOO_SMALL = 0.005f;

        //双指移动前后的相对距离数组
        private final double[] distCompare;

        //双指缩放绑定的两个触摸点id
        private final int[] pointerId;

        private SvgGestureListener() {
            distCompare = new double[2];
            pointerId = new int[] {SvgConsts.INVAILE_VALUE, SvgConsts.INVAILE_VALUE};
            scaleOnce = mScale;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!hasSvgDraw()) {
                SvgLog.I("没有svg被绘制，忽略缩放");
                performClick();
                return false;
            }

            if (!isGestureEnable()) {
                performClick();
                return false;
            }

            int actionMasked = event.getActionMasked();

            //当被允许使用svg手势，且有手势标记，svg动画将会立即停止
            if (isSvgAnimRunning() && ((mGestureStatus & 0x2) != 0)) {
                stopSvgAnim();
            }

            switch (actionMasked) {
                case MotionEvent.ACTION_DOWN:
                    onFirstPointerAdd();
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    onPointerAdd(event);
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    onPointerRemove(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!isGesturePlaying()) {
                        break;
                    }

                    if ((mGestureStatus & 0x7) == 0x7) {
                        maybeSvgScale(event);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    setPointerId(SvgConsts.INVAILE_VALUE, SvgConsts.INVAILE_VALUE);
                    break;
            }

            return true;
        }

        private void onFirstPointerAdd() {
            setPointerId(0, pointerId[1]);
        }

        private void onPointerAdd(MotionEvent event) {
            int pointerCount = event.getPointerCount();

            //此时触摸点个数总是 >= 2；当触摸点不少于2，则记录触摸点
            mGestureStatus |= 0x2;

            if (pointerCount == GESTURE_SVG_SCALE) {
                //单指触摸 -> 双指触摸。记录第二个触摸点的id
                setPointerId(pointerId[0], event.getPointerId(1));

                //标记双指svg缩放手势
                mGestureStatus |= 0x4;

                //记录原始的双指距离
                distCompare[0] = getPointersDistance(event);
//                SvgLog.I("起始相对距离：" + distCompare[0]);
            } else {
                //不是svg缩放手势，但不清除记录的触摸点id
                mGestureStatus &= 0xF3;
            }
        }

        private void onPointerRemove(MotionEvent event) {
            int pointerCount = event.getPointerCount();

            //此时获取到的触摸点个数总是 >= 2
            if (pointerCount == GESTURE_SVG_SCALE) {
                //只剩下一个触摸点，取消所有手势标记
                mGestureStatus &= 0x1;

                setPointerId(event.getPointerId(0), SvgConsts.INVAILE_VALUE); //之后两点初始相对距离需要重新计算
                distCompare[0] = SvgConsts.INVAILE_VALUE;

                //只有一只手指，退出svg手势
                exitSvgScaleGesture();
            } else {
                //触摸点仍然不少于2，但可能已经将某个已记录id的触摸点移除，此时检查所有的触摸点信息
                boolean firstPointerExist = event.findPointerIndex(pointerId[0]) >= 0;
                boolean secondPointerExist = event.findPointerIndex(pointerId[1]) >= 0;
                int newPointerId = event.getPointerId(pointerCount - 2);
                if (!firstPointerExist) {
//                    SvgLog.I("第0个触摸点已经丢失");
                    pointerId[0] = newPointerId;
                    distCompare[0] = getPointersDistance(event);
                } else if (!secondPointerExist) {
//                    SvgLog.I("第1个触摸点已经丢失");
                    pointerId[1] = newPointerId;
                    distCompare[0] = getPointersDistance(event);
                } else {
                    //丢失的不是记录的两个手势点，不做处理
                }
            }
        }

        /** 可能产生了svg缩放手势 */
        private void maybeSvgScale(MotionEvent event) {

            if (event.getPointerCount() != GESTURE_SVG_SCALE) {
                //不处理缩放之外的手势
                return ;
            }

            if (distCompare[0] == SvgConsts.INVAILE_VALUE) {
                SvgLog.I("两点的初始相对距离异常！不做缩放");
                return ;
            }
            if (distCompare[0] < SVG_SCALE_START_DISTANCE) {
                SvgLog.I("初始相对距离较小，不做缩放");
                distCompare[0] = getPointersDistance(event);
                return ;
            }

            if ((mGestureStatus & 0x7) == 0 || shouldBeRestore) {
                SvgLog.I("本次手势已经被提前终止，可能是超过了缩放范围");
                return ;
            }

            //获取新的两个触摸点的相对距离
            distCompare[1] = getPointersDistance(event);

            double deltaDist = distCompare[1] - distCompare[0];

            //位移 ：缩放率 = 5000px : 1x
            scaleOnce = (float)(deltaDist / 5000f + mScale);

            SvgLog.I("相对距离增加了" + deltaDist + "px, 当前缩放值：" + scaleOnce + ", 上一次缩放值：" + mScale);

            //禁止超出缩放阈值
            boolean refuseNarrow = scaleOnce < SCALE_MIN_VALUE && (scaleOnce < 1f);
            boolean refuseAmplify = scaleOnce > SCALE_MAX_VALUE && (scaleOnce > 1f);
            if (refuseAmplify || refuseNarrow) {
                SvgLog.I("超出缩放范围：" + scaleOnce);
                //直接标记本次手势已经失效
                shouldBeRestore = true;
                mGestureStatus &= 0xF9;

                scaleOnce = refuseAmplify ? SCALE_MAX_VALUE : SCALE_MIN_VALUE;
            }

            if (Math.abs(deltaDist) < 10.0) {
                //位移过小不做缩放
                return ;
            }
            if (scaleOnce < SCALE_TOO_SMALL) {
                SvgLog.I("倍率变化太小，不做缩放");
                return ;
            }

            //&￥%@#&$*^，这里搞得我调了四个多小时，从2020年1月20日16:00到2020年1月20日19:42！！！
            // 不光一加7pro要调坏，人都要被玩坏了

            if (isUiThread()) {
                scaleImpl(scaleOnce, 0, null);
//                scaleWithoutAnim(oriSvgMap, scale);
            } else {

                post(() -> {
                    scaleImpl(scaleOnce, 0, null);
//                    scaleWithoutAnim(oriSvgMap, scale);
                });
            }
        }

        private void exitSvgScaleGesture() {
            if (shouldBeRestore) {
                //取消这个功能
                SvgLog.I("需要还原svg缩放");
                shouldBeRestore = false;
                scaleOnce = 1f;
                svgScale(scaleOnce, 300, null);
            }

            mScale = scaleOnce;
            mGestureStatus &= 0x1;
        }

        /** 计算两个触摸点之间的相对距离 */
        private double getPointersDistance(MotionEvent event) {
            int pointerCount = event.getPointerCount();
            if (pointerCount < GESTURE_SVG_SCALE) {
                return SvgConsts.INVAILE_VALUE;
            }

            float x1 = event.getX(event.findPointerIndex(pointerId[0]));
            float y1 = event.getY(event.findPointerIndex(pointerId[0]));
            float x2 = event.getX(event.findPointerIndex(pointerId[1]));
            float y2 = event.getY(event.findPointerIndex(pointerId[1]));
            return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        }

        private void setPointerId(int id1, int id2) {
            pointerId[0] = id1;
            pointerId[1] = id2;
        }
    }

    private final class SvgAnimation {
        private final ISvgAnim anim;
        private AtomicBoolean isRunning;
        private ValueAnimator animator;
        private final int MSG_START_ANIM_DELAY = 1;

        private Handler mainHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_START_ANIM_DELAY) {
                    if (mCenterX == SvgConsts.INVAILE_VALUE || mCenterY == SvgConsts.INVAILE_VALUE) {
                        SvgLog.I("等待svg第一次绘制完成");
                        removeMessages(MSG_START_ANIM_DELAY);
                        sendEmptyMessageDelayed(MSG_START_ANIM_DELAY, 20);
                        return ;
                    }

                    if (animator != null) {
                        animator.start();
                    }
                }
            }
        };

        private SvgAnimation(@NonNull ISvgAnim anim) {
            this.anim = anim;
            isRunning = new AtomicBoolean(false);
            animator = new ValueAnimator();
            animator.setInterpolator(new LinearInterpolator());
        }

        private void start() {
            int category = anim.getCategory();
            switch (category) {
                case SvgConsts.ANIM_CLIPPING:
                    SvgLog.I("执行裁剪动画");
                    startTrimAnim();
                    break;
                case SvgConsts.ANIM_PATH_MOVING:
                    SvgLog.I("执行路径运动动画");
                    startPathMovingAnim();
                    break;
            }
        }

        private void cancel() {
            isRunning.set(false);
            mainHandler = null;
        }

        private boolean isRunning() {
            return isRunning.get();
        }

        private void startTrimAnim() {
            animator.cancel();

            if (mSvgPathList.size() > 1) {
                SvgLog.I("暂不支持复合路径的裁剪动画");
                return ;
            }

            final AnimListener listener = anim.getListener();

            animator.setDuration(anim.getDurationMs());

            final PathMeasure pm = new PathMeasure();
            pm.setPath(mSvgPathList.get(0), false);
            final Path path = new Path();
            float pathLen = pm.getLength();
            if (pathLen == 0f) {
                SvgLog.I("svg路径长度错误，不进行svg裁剪动画");
                return ;
            }

            final boolean isReverse = ((SvgTrimAnim)anim).isReverse();

            final float startDst = isReverse ? pathLen : 0f;
            final float endDst = isReverse ? 0f : pathLen;
            animator.setFloatValues(startDst, endDst);

            animator.addUpdateListener(animation -> {
                float fraction = animation.getAnimatedFraction();

                if (!isRunning()) {
                    SvgLog.I("形变动画被中止");
                    refresh();
                    animation.cancel();
                }

                if (mSvgPathList == null || mSvgPathList.size() == 0) {
                    SvgLog.I("形变动画中发生了异常");
                    refresh();
                    animation.cancel();
                    return ;
                }

                path.reset();
                float curLen = (float)animation.getAnimatedValue();
                pm.getSegment(startDst, curLen, path, true);
                mSvgPathList.set(0, path);

                if (listener != null) {
                    if (!listener.onAnimProgress(fraction)) {
                        animation.cancel();
                    }
                }

                refresh();
            });
            
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    if (mSvgAnimationList.size() > 0) {
                        mSvgAnimationList.remove(0);
                    }

                    cancel();

                    if (listener != null) {
                        listener.onAnimCancel();
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mSvgAnimationList.size() > 0) {
                        mSvgAnimationList.remove(0);
                    }

                    cancel();

                    if (listener != null) {
                        listener.onAnimEnd();
                    }
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    SvgLog.I("SvgAnimation start.");
                    isRunning.set(true);

                    if (listener != null) {
                        listener.onAnimStart();
                    }
                }
            });

            if (mCenterX == SvgConsts.INVAILE_VALUE || mCenterY == SvgConsts.INVAILE_VALUE) {
                SvgLog.I("发现未完成第一次svg绘制");
                mainHandler.sendEmptyMessage(MSG_START_ANIM_DELAY);
            } else {
                animator.start();
            }
        }

        //LYX_TAG 2020/3/21 18:05 直接复制裁剪动画那里的代码，有空再另写
        private void startPathMovingAnim() {
            animator.cancel();

            if (mSvgPathList.size() > 1) {
                SvgLog.I("暂不支持复合路径的裁剪动画");
                return ;
            }

            final AnimListener listener = anim.getListener();

            animator.setDuration(anim.getDurationMs());

            final PathMeasure pm = new PathMeasure();
            pm.setPath(mSvgPathList.get(0), false);
            final Path path = new Path();
            float pathLen = pm.getLength();
            if (pathLen == 0f) {
                SvgLog.I("svg路径长度错误，不进行svg裁剪动画");
                return ;
            }

            final float startDst = 0f;
            final float endDst = pathLen;
            animator.setFloatValues(startDst, endDst);

            animator.addUpdateListener(animation -> {
                float fraction = animation.getAnimatedFraction();

                if (!isRunning()) {
                    SvgLog.I("形变动画被中止");
                    refresh();
                    animation.cancel();
                }

                if (mSvgPathList == null || mSvgPathList.size() == 0) {
                    SvgLog.I("形变动画中发生了异常");
                    refresh();
                    animation.cancel();
                    return ;
                }

                path.reset();
                float curLen = (float)animation.getAnimatedValue();
                pm.getSegment(curLen - pathLen / 10, curLen, path, true);
                mSvgPathList.set(0, path);

                if (listener != null) {
                    if (!listener.onAnimProgress(fraction)) {
                        animation.cancel();
                    }
                }

                refresh();
            });

            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    if (mSvgAnimationList.size() > 0) {
                        mSvgAnimationList.remove(0);
                    }

                    cancel();

                    if (listener != null) {
                        listener.onAnimCancel();
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mSvgAnimationList.size() > 0) {
                        mSvgAnimationList.remove(0);
                    }

                    cancel();

                    if (listener != null) {
                        listener.onAnimEnd();
                    }

                    refresh();
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    SvgLog.I("SvgAnimation start.");
                    isRunning.set(true);

                    if (listener != null) {
                        listener.onAnimStart();
                    }
                }
            });

            if (mCenterX == SvgConsts.INVAILE_VALUE || mCenterY == SvgConsts.INVAILE_VALUE) {
                SvgLog.I("发现未完成第一次svg绘制");
                mainHandler.sendEmptyMessage(MSG_START_ANIM_DELAY);
            } else {
                animator.start();
            }
        }
    }
}
