package com.qxtx.idea.ideasvg.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.graphics.drawable.VectorDrawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.StyleableRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.qxtx.idea.ideasvg.BuildConfig;
import com.qxtx.idea.ideasvg.R;
import com.qxtx.idea.ideasvg.entity.SvgParam;
import com.qxtx.idea.ideasvg.parser.SvgParser;
import com.qxtx.idea.ideasvg.tools.SvgLog;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * @author QXTX-WIN
 * @date 2019/12/3 21:39
 * Description: 提供svg支持的控件，支持导入.svg文件，以及解析资源目录中以vector为根标签的xml
 */
public class IdeaSvgView extends View implements ISvgView {

    private final String TAG = getClass().getSimpleName();

    /** 控件背景 */
    private int mBgResId;

    /** 此paint负责绘制背景等svg之外的元素，不参与svg的绘制 */
    private final Paint mExtraPaint;

    /** svg的参数集 */
    private final SvgParam mSvgParam;

    /** svg数据解析器 */
    private final SvgParser mSvgParser;

    /**
     * 控件的构造方法，可以在这里获得控件的xml属性值
     * 1、属性值获取优先级（低优先级被高优先级覆盖）：xml直接定义 > xml的style属性定义 > 构造方法里defStyleAttr定义 > 构造方法里defStyleRes定义 > theme中直接定义
     */
    public IdeaSvgView(Context context, AttributeSet attrs) {
        super(context, attrs, 0, 0);

        mExtraPaint = new Paint();
        mSvgParam = new SvgParam();
        mSvgParser = new SvgParser(getContext());

        long durationMs = System.currentTimeMillis();

        //获得各种属性值
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IdeaSvgView);
        try {
            //xml中配置svg图形宽高
            //未定义svg宽高，则使用原始数据
            //LYX_TAG 2020/9/14 22:58 这个得到px还是dp数值？
            mSvgParam.setWidth(a.getDimension(R.styleable.IdeaSvgView_svgWidth, 0));
            mSvgParam.setHeight(a.getDimension(R.styleable.IdeaSvgView_svgHeight, 0));

            mSvgParam.setAlpha(a.getFloat(R.styleable.IdeaSvgView_svgAlpha, 1f));

            final int INVALID_ID = Integer.MIN_VALUE;
            int srcId = a.getResourceId(R.styleable.IdeaSvgView_svgSrc, INVALID_ID);
            if (srcId != INVALID_ID) {
                XmlResourceParser parser = getResources().getXml(srcId);
                mSvgParser.parseVectorXml(getResources(), parser, mSvgParam);
            }
        } catch (Exception e) {
            SvgLog.I("读取attr发生异常：" + e.getMessage());
            e.printStackTrace();
        }
        a.recycle();

        SvgLog.I("解析耗时：" + (System.currentTimeMillis() - durationMs) + "ms.\n 获得的参数：" + mSvgParam.toString());
    }

    /** invalidate/postInvalidate会先被执行 */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //如有需要，在此拿到控件的宽高测量模式，但现在暂时不需要
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 可能会出现给定的参数无法得到一个椭圆弧线（计算sqrtValue时会得到Double.NaN，也就是说给的参数无法构成一条椭圆弧），
     *  如果倾斜度为0，则可以处理，缩放长短轴即可；
     *  如果存在倾斜度，目前无法知道这个椭圆需要平移或者缩放甚至两者兼有才能符合要求，因此暂时没有办法处理。
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long time = System.currentTimeMillis();

        //LYX_TAG 2020/8/2 22:14 待处理:测试用
        if (BuildConfig.DEBUG) {
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.parseColor("#00ff00"));
            paint.setStrokeWidth(5f);
            Path path = new Path();

            //圆弧起点坐标x1，y1
            double x1 = 100, y1 = 200;
            //rxHalf长半轴，ryHalf短半轴
            double rxHalf = 100, ryHalf = 70;
            //椭圆x轴与画布x轴的夹角（顺时针夹角）
            double phi = 45;
            //fa大圆弧1小圆弧0，fs顺时针1逆时针0
            int fA = 0, fS = 0;
            //终点坐标x2，y2
            double x2 = 300, y2 = 200;

            mSvgParser.generateEllipticalArcPath(path, x1, y1, rxHalf, ryHalf, phi, fA, fS, x2, y2);

            canvas.drawPath(path, paint);
        }

        //椭圆外接矩形长宽（长轴和短轴）
        SvgLog.i("绘制耗时[" + (System.currentTimeMillis() - time) + "]ms.");
    }
}
