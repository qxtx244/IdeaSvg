package com.qxtx.idea.ideasvg.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import com.qxtx.idea.ideasvg.R;
import com.qxtx.idea.ideasvg.parser.SvgParser;
import com.qxtx.idea.ideasvg.tools.SvgLog;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author QXTX-WIN
 * @date 2019/12/3 21:39
 * Description: 提供svg支持的控件
 */
public class IdeaSvgView extends View {
    private final String TAG = getClass().getSimpleName();

    /** 控件背景 */
    private int mBgResId;

    /**
     * 控件宽度
     * @see #mHeight
     */
    private int mWidth;

    /**
     * 控件高度
     * @see #mWidth
     */
    private int mHeight;

    /** 此paint不参与svg的绘制，与绘制背景，drawable等元素有关 */
    private Paint mPaint;

    /** svg的参数集 */
    private SvgParam mSvgParam;

    /** svg数据解析器 */
    private SvgParser mSvgParser;

    /**
     * 控件的构造方法，可以在这里获得控件的xml属性值
     * 1、属性获取优先顺序：xml直接定义 >> xml的style属性定义 >> 构造方法里defStyleAttr定义 >> 构造方法里defStyleRes定义 >> theme中直接定义
     */
    public IdeaSvgView(Context context, AttributeSet attrs) {
        super(context, attrs, 0, 0);

        mPaint = new Paint();
        mSvgParam = new SvgParam();
        mSvgParser = new SvgParser();

        //获得各种属性值
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IdeaSvgView);
        try {
            //获取控件宽高，有几种情况：
            //只要有一个为wrap_content或match_parent，
            //1、有具体的宽高值。此时，svgSrc中获得的宽高值被忽略。
            //2、自适应(wrap_content)。svg使用svgSrc中标注的宽高
            //3、铺满(match_parent)。svg使用svgSrc中标注的宽高
            mWidth = a.getLayoutDimension(R.styleable.IdeaSvgView_android_layout_width, ViewGroup.LayoutParams.WRAP_CONTENT);
            mHeight = a.getLayoutDimension(R.styleable.IdeaSvgView_android_layout_height, ViewGroup.LayoutParams.WRAP_CONTENT);

            parseSvgSrc(a);


            mSvgParam.setAlpha(a.getFloat(R.styleable.IdeaSvgView_svgAlpha, mSvgParam.alpha));
        } catch (Exception e) {
            SvgLog.I("读取attr发生异常：" + e.getMessage());
            e.printStackTrace();
        }
        a.recycle();
    }

    /**
     * 开始解析xml
     * 注意如果控件有具体的宽高，则使用控件宽高作为svg宽高，而不是xml中标注的宽高
     */
    private void parseSvgSrc(@NonNull TypedArray a) {
        final int INVALID_ID = Integer.MIN_VALUE;
        int srcId = a.getResourceId(R.styleable.IdeaSvgView_svgSrc, INVALID_ID);
        if (srcId == INVALID_ID) {
            return ;
        }

        try {
            XmlResourceParser parser = getResources().getXml(srcId);
            if (parser.isEmptyElementTag()) {
                return ;
            }

        } catch (Exception e) {
            SvgLog.E("解析xml资源出现异常：" + e.getLocalizedMessage());
            e.printStackTrace();
        }
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
     * svg相关配置信息
     */
    private final static class SvgParam {

        /** 仅供svg使用的画笔属性 */
        private final Paint paint;

        /** 有序路径数据集，每一个键值对为一段路径数据，K为svg锚点符+序号，V为数值数组，用于生成{@link #pathList} */
        private final LinkedHashMap<String, float[]> pathDataMap;

        /** 有序路径对象列表，用于最终绘制路径，由{@link #pathDataMap}生成 */
        private final List<Path> pathList;

        private @Style int style;

        /** 透明度，范围为[0f, 1f] */
        private float alpha;

        /** 线条宽度，单位为px */
        private float strokeWidth;

        /** 线条颜色，顺序对应每一条路径 */
        private int[] strokeColors;

        /** 填充的颜色，顺序对应每一条路径 */
        private int[] fillColors;

        /**
         * svg的实际尺寸，高16位是宽，低16位是高，大端序
         * 如果此宽高数据任一为0，则强制使用虚拟画布宽高值
         * 注意，此宽高数据仅仅是定义了svg在实际画布下的尺寸，与路径的数值大小无关，而{@link #viewportSpec}则给路径提供了一个参考画布尺寸。
         * 可以通过{@link #viewportSpec}和此宽高数据的比值，可以计算出svg在实际画布尺寸下的路径数值，然后在view中绘制出真实尺寸的svg。
         *
         * 宽度：int width = (svgSpec >> 16) & 0xffff
         * 高度：int height = svgSpec & 0xffff
         */
        private int spec;

        /**
         * svg路径参考的尺寸，高16位是宽，低16位是高，大端序
         * 注意，此宽高数据作为路径的参考画布宽高，与svg的实际尺寸无关，而{@link #spec}则定义了svg在实际画布中的尺寸。
         * 简单地说，前者仅仅是为了给路径提供一个参考画布尺寸，后者定义了这个图形在真实画布中的尺寸
         * 可以通过{@link #spec}和此宽高数据的比值，计算出路径在实际画布尺寸下的数值，然后在view中绘制出真实尺寸的svg。
         *
         * 宽度：int width = (pathSpec >> 16) & 0xffff
         * 高度：int height = pathSpec & 0xffff
         */
        private int viewportSpec;

        /** svg绘制风格 */
        @Retention(RetentionPolicy.SOURCE)
        @IntDef({STYLE_UNKNOWN, STYLE_STROKE, STYLE_FILL, STYLE_STROKE_AND_FILL})
        public @interface Style{}
        /** 未知风格 */
        public static final int STYLE_UNKNOWN = 0;
        /** 轮廓风格 */
        public static final int STYLE_STROKE = 0x1;
        /** 填充风格 */
        public static final int STYLE_FILL = STYLE_STROKE << 1;
        /** 轮廓和填充混合风格 */
        public static final int STYLE_STROKE_AND_FILL = STYLE_FILL | STYLE_STROKE;

        private final int DEFAULT_STROKE_WIDTH = 3;
        private final float DEFAULT_ALPHA = 1f;
        private final String DEFAULT_STROKE_COLOR = "#FF0000";
        private final String DEFAULT_FILL_COLOR = "#65C8FF";

        private SvgParam() {
            pathDataMap = new LinkedHashMap<>();
            pathList = new ArrayList<>();
            paint = new Paint();
            reset();
        }

        private void reset() {
            paint.reset();
            pathDataMap.clear();
            pathList.clear();
            style = STYLE_STROKE;
            alpha = DEFAULT_ALPHA;
            strokeWidth = DEFAULT_STROKE_WIDTH;
            strokeColors = new int[] {Color.parseColor(DEFAULT_STROKE_COLOR)};
            fillColors = new int[] {Color.parseColor(DEFAULT_FILL_COLOR)};
            spec = -1;
            viewportSpec = -1;
        }

        public int getWidth() {
            return (spec >> 16) & 0xffff0000;
        }

        public int getHeight() {
            return spec & 0xffff;
        }

        public int getViewportWidth() {
            return (viewportSpec >> 16) & 0xffff0000;
        }

        public int getViewportHeight() {
            return viewportSpec & 0xffff;
        }

        public LinkedHashMap<String, float[]> getPathDataMap() {
            return pathDataMap;
        }

        public List<Path> getPathList() {
            return pathList;
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

        public int getSpec() {
            return spec;
        }

        public void setSpec(int spec) {
            this.spec = spec;
        }

        public int getViewportSpec() {
            return viewportSpec;
        }

        public void setViewportSpec(int viewportSpec) {
            this.viewportSpec = viewportSpec;
        }
    }
}
