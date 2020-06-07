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
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.qxtx.idea.ideasvg.R;
import com.qxtx.idea.ideasvg.entity.SvgParam;
import com.qxtx.idea.ideasvg.parser.SvgParser;
import com.qxtx.idea.ideasvg.tools.SvgLog;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * @author QXTX-WIN
 * @date 2019/12/3 21:39
 * Description: 提供svg支持的控件，支持导入.svg文件，以及资源目录中以vector为根标签的xml
 */
public class IdeaSvgView extends View implements ISvgView {
    private final String TAG = getClass().getSimpleName();

    /** 控件背景 */
    private int mBgResId;

    /**
     * 控件宽高，0~14位为宽，15~28位为高, 29~31位标记wrap_content(01),match_content(10),fill_content(11)
     * 当高2位不为0时，表明控件宽高为特殊定义，即wrap_content/match_content/fill_content，此时，宽高字段无效（或者被置0）
     * 获取特殊位：int header = (mSpec >> 30) & 0x3;
     * 获取宽度：int w = mSpec & 0x7fff;
     * 获取高度：int h = (mSpec >> 15) & 0x7fff
     */
    private int mSpec;

    /** 此paint不参与svg的绘制，与绘制背景，drawable等元素有关 */
    private final Paint mPaint;

    /** svg的参数集 */
    private final SvgParam mSvgParam;

    /** svg数据解析器 */
    private final SvgParser mSvgParser;

    /**
     * 控件的构造方法，可以在这里获得控件的xml属性值
     * 1、属性获取优先顺序：xml直接定义 >> xml的style属性定义 >> 构造方法里defStyleAttr定义 >> 构造方法里defStyleRes定义 >> theme中直接定义
     */
    public IdeaSvgView(Context context, AttributeSet attrs) {
        super(context, attrs, 0, 0);

        mPaint = new Paint();
        mSvgParam = new SvgParam();
        mSvgParser = new SvgParser();

        long durationMs = System.currentTimeMillis();

        //获得各种属性值
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IdeaSvgView);
        try {
            //获取控件宽高，有几种情况：
            //1、有具体的宽高值。此时，svgSrc中获得的宽高值被忽略。
            //2、自适应(wrap_content)。svg使用svgSrc中标注的宽高
            //3、铺满(match_parent)。svg使用svgSrc中标注的宽高
            //LYX_TAG 2020/5/23 20:56 这里后面需要重新整理下。如果获取到的宽高值小于0，该怎么处理？目前处理为直接标记mSpec的高2位
            int width = a.getLayoutDimension(R.styleable.IdeaSvgView_android_layout_width, ViewGroup.LayoutParams.WRAP_CONTENT);
            int height = a.getLayoutDimension(R.styleable.IdeaSvgView_android_layout_height, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (width <= 0 || height <= 0) {
                mSpec &= 0xC0000000;
            } else {
                mSpec &= 0x3fffffff;
                mSpec |= (width << 15) & 0x3fff8000;
                mSpec |= (height & 0x7fff);
            }

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
     * LYX_TAG 2020/6/3 23:32 可能会出现给定的参数无法得到一个椭圆（计算sqrtValue时会得到Double.NaN），
     *  如果倾斜度为0，则可以处理，缩放长短轴即可；
     *  如果存在倾斜度，目前无法知道这个椭圆需要平移或者缩放甚至两者兼有才能符合要求，因此暂时没有办法处理。
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.parseColor("#00ff00"));
        paint.setStrokeWidth(5f);
        Path path = new Path();

        long time = System.currentTimeMillis();

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

        generateEllipticalArcPath(path, x1, y1, rxHalf, ryHalf, phi, fA, fS, x2, y2);

        canvas.drawPath(path, paint);

        //椭圆外接矩形长宽（长轴和短轴）
        SvgLog.i("绘制耗时[" + (System.currentTimeMillis() - time) + "]ms.");
    }

    /**
     * @param path svg路径对象
     * @param x1 椭圆弧的起始x坐标
     * @param y1 椭圆弧的起始y坐标
     * @param rxHalf 长半轴
     * @param ryHalf 短半轴
     * @param phi 椭圆弧所在椭圆的自身x轴与画布x轴的夹角，以顺时针方向
     * @param fA 1表示使用大角度圆心角对应的椭圆弧，0表示小角度圆心角对应的椭圆弧
     * @param fS 1表示椭圆弧走向为沿顺时针方向，0为沿逆时针方向
     * @param x2 椭圆弧的终点x坐标
     * @param y2 椭圆弧的终点y坐标
     *
     * 备注：小数计算会有精度问题，可能导致得到的数值变小了
     */
    private void generateEllipticalArcPath(Path path, double x1, double y1,
                                           double rxHalf, double ryHalf, double phi, double fA, double fS, double x2, double y2) {

        if (rxHalf == ryHalf) {
            phi = 0;
        } else {
            phi %= 360;
        }

        //对于倾斜度为0的椭圆，如果两点之间的距离s大于长轴，则将长轴增大到s，并且短轴比做等比增大。
        double dist = Math.abs(x2 - x1);
        double longAxis = 2 * rxHalf;
        if (phi == 0 && dist > longAxis) {
            rxHalf = dist / 2;
            double zoom = dist / longAxis;
            ryHalf *= zoom;
        }

        double cosPhi = Math.cos(phi);
        double sinPhi = Math.sin(phi);

        double deltaXHalf = (x1 - x2) / 2;
        double deltaYHalf = (y1 - y2) / 2;
        double x11 = cosPhi * deltaXHalf + sinPhi * deltaYHalf;
        double y11 = (-sinPhi) * deltaXHalf + cosPhi * deltaYHalf;

        double rxy11Pow2 = Math.pow(rxHalf * y11, 2);
        double ryx11Pow2 = Math.pow(ryHalf * x11, 2);
        double sqrtValue = Math.sqrt(Math.abs((Math.pow(rxHalf * ryHalf, 2) - rxy11Pow2 - ryx11Pow2) / (rxy11Pow2 + ryx11Pow2)));
        if (Double.isNaN(sqrtValue)) {
            SvgLog.i("发生异常！无法解析的A锚点");
            path.lineTo((float)x2, (float)y2);
            return ;
        }

        double cxx = sqrtValue * rxHalf * y11 / ryHalf;
        double cyy = sqrtValue * (-ryHalf) * x11 / rxHalf;
        if (fA == fS) {
            cxx *= -1;
            cyy *= -1;
        }

        //得到中心坐标
        double cx = cosPhi * cxx - sinPhi * cyy + ((x1 + x2) / 2);
        double cy = sinPhi * cxx + cosPhi * cyy + ((y1 + y2) / 2);

        SvgLog.i("数值：x11,y11=" + x11 + "," + y11 + "  cxx,cyy=" + cxx + "," + cyy + "  cx,xy=" + cx + "," + cy + ", sqrtValue=" + sqrtValue);

        //通过x1 —> x2的x坐标值递增，得到对应的y坐标值，逐个lineTo，细粒度为1°
        path.moveTo((float)x1, (float)y1);

        double x, y;
        double deltaValue = (x2 - x1) / 180;
        for (x = x1; x <= x2; x += deltaValue) {
            y = cy + Math.sqrt(1 - Math.pow(x - cx, 2) / Math.pow(rxHalf, 2)) * ryHalf;
            path.lineTo((float)x, (float)y);
        }
    }
}
