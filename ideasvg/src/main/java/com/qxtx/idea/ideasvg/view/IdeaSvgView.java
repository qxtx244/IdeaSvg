package com.qxtx.idea.ideasvg.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.qxtx.idea.ideasvg.R;
import com.qxtx.idea.ideasvg.SvgConsts;
import com.qxtx.idea.ideasvg.parser.SvgDataParser;
import com.qxtx.idea.ideasvg.tools.DeepCopy;
import com.qxtx.idea.ideasvg.tools.SvgLog;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * @author QXTX-WIN
 * @date 2019/12/3 21:39
 * Description: 提供svg支持的控件
 */
public class IdeaSvgView extends View {

    private final String TAG = getClass().getSimpleName();

    private SvgDataParser mParser;

    private Paint mPaint;

    private IdeaSvgView(Context context) {
        super(context);
    }

    public IdeaSvgView(Context context, AttributeSet attrs) {
        super(context, attrs, 0, 0);
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

    }

    @Override
    public void invalidate() {
        super.invalidate();
    }

    @Override
    public void postInvalidate() {
        if (isUiThread()) {
            invalidate();
            return ;
        }

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

//        SvgLog.I("onDraw() time=" + (System.currentTimeMillis() - time) + "ms");
    }

    private boolean isUiThread() {
        return Looper.myLooper() != null && (Looper.myLooper() == Looper.getMainLooper());
    }

    /**
     * svg相关配置参数:
     * svg路径
     * 绘制风格
     * 透明度
     * 线条宽度
     * 线条颜色
     * 填充颜色
     */
    private final static class SvgParam {

        private String path;

        private @Style int style;

        private float alpha;

        private float strokeWidth;

        private int[] strokeColors;

        private int[] fillColors;

        /** svg实际尺寸，高16位是宽，低16位是高，大端序 */
        private int svgSpec;

        /** svg路径参考尺寸，高16位是宽，低16位是高 */
        private int viewportSpec;

        @Retention(RetentionPolicy.SOURCE)
        @IntDef({STYLE_STROKE, STYLE_FILL, STYLE_STROKE_AND_FILL})
        public @interface Style{}
        public static final int STYLE_STROKE = 0x1;
        public static final int STYLE_FILL = STYLE_STROKE << 1;
        public static final int STYLE_STROKE_AND_FILL = STYLE_FILL | STYLE_STROKE;

        private final int DEFAULT_STROKE_WIDTH = 3;
        private final String DEFAULT_STROKE_COLOR = "#FF0000";
        private final String DEFAULT_FILL_COLOR = "#65C8FF";

        private SvgParam() {
            path = null;
            style = STYLE_STROKE;
            alpha = 1f;
            strokeWidth = DEFAULT_STROKE_WIDTH;
            strokeColors = new int[] {Color.parseColor(DEFAULT_STROKE_COLOR)};
            fillColors = new int[] {Color.parseColor(DEFAULT_FILL_COLOR)};
            svgSpec = 0;
            viewportSpec = 0;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public int getStyle() {
            return style;
        }

        public void setStyle(int style) {
            this.style = style;
        }

        public float getAlpha() {
            return alpha;
        }

        public void setAlpha(float alpha) {
            this.alpha = alpha;
        }

        public float getStrokeWidth() {
            return strokeWidth;
        }

        public void setStrokeWidth(float strokeWidth) {
            this.strokeWidth = strokeWidth;
        }

        public int[] getStrokeColors() {
            return strokeColors;
        }

        public void setStrokeColors(int[] strokeColors) {
            this.strokeColors = strokeColors;
        }

        public int[] getFillColors() {
            return fillColors;
        }

        public void setFillColors(int[] fillColors) {
            this.fillColors = fillColors;
        }

        public int getSvgSpec() {
            return svgSpec;
        }

        public void setSvgSpec(int svgSpec) {
            this.svgSpec = svgSpec;
        }

        public int getViewportSpec() {
            return viewportSpec;
        }

        public void setViewportSpec(int viewportSpec) {
            this.viewportSpec = viewportSpec;
        }
    }
}
