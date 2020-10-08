package com.qxtx.idea.ideasvg.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.qxtx.idea.ideasvg.R;
import com.qxtx.idea.ideasvg.tools.ScreenUtil;
import com.qxtx.idea.ideasvg.tools.SvgConsts;
import com.qxtx.idea.ideasvg.xmlEntity.PathElement;
import com.qxtx.idea.ideasvg.xmlEntity.VectorXmlInfo;
import com.qxtx.idea.ideasvg.parser.VectorXmlParser;
import com.qxtx.idea.ideasvg.tools.SvgLog;

import java.util.ArrayList;
import java.util.List;

/**
 * @author QXTX-WIN
 * @date 2019/12/3 21:39
 * Description: 提供svg支持的控件，支持导入.svg文件，以及解析控件设置的以vector为根标签的xml
 * 根据官方建议，vector矢量图尺寸应该在200x200以内，以保证较好的性能。
 *
 * 备注：
 *  1、不支持阴影设置：<aapt:attr></aapt:attr>
 *  2、对椭圆弧线支持极差
 */
public class IdeaSvgView extends View implements ISvgView {

    private final String TAG = getClass().getSimpleName();

    /** 保存svg边界数据，完成居中操作 */
    private final RectF svgRectF = new RectF();

    private float mWidth = 0f;
    private float mHeight = 0f;

    /** svg是否居中显示 */
    private boolean mIsSvgCenter;

    /** svg路径对象列表 */
    private final List<Path> mSvgPathList;

    /** 仅负责绘制svg */
    private final Paint mSvgPaint;

    /** vector xml的参数集 */
    private final VectorXmlInfo mVectorXmlInfo;

    /** svg数据解析器 */
    private final VectorXmlParser mVectorXmlParser;

    /** 控件背景 */
    private Drawable mBackground;

    /** 负责绘制背景等svg之外的元素，不参与svg的绘制 */
    private final Paint mExtraPaint;

    /**
     * 控件的构造方法，可以在这里获得控件的xml属性值
     * 1、属性值获取优先级（低优先级被高优先级覆盖）：xml直接定义 > xml的style属性定义 > 构造方法里defStyleAttr定义 > 构造方法里defStyleRes定义 > theme中直接定义
     */
    public IdeaSvgView(Context context, AttributeSet attrs) {
        super(context, attrs, 0, 0);

        mIsSvgCenter = true;
        mSvgPaint = new Paint();
        mSvgPathList = new ArrayList<>();
        mExtraPaint = new Paint();
        mVectorXmlInfo = new VectorXmlInfo();
        mVectorXmlParser = new VectorXmlParser(getContext());

        long durationMs = System.currentTimeMillis();

        //获得各种属性值
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IdeaSvgView);

        try {
            mBackground = a.getDrawable(R.styleable.IdeaSvgView_android_background);

            mIsSvgCenter = a.getBoolean(R.styleable.IdeaSvgView_svgCenter, true);

            //如果xml中使用@dimen这种引用资源id，则TypedArray.getString()得到的直接是一个(double/float?)数值
            String widthAttr = a.getString(R.styleable.IdeaSvgView_android_layout_width);
            String heightAttr = a.getString(R.styleable.IdeaSvgView_android_layout_height);
            if (widthAttr == null || heightAttr == null) {
                throw new IllegalStateException("xml中必须设置控件的宽高！");
            }

            if (!widthAttr.equals(ViewGroup.LayoutParams.WRAP_CONTENT + "")
                    && !widthAttr.equals(ViewGroup.LayoutParams.MATCH_PARENT + "")) {
                mWidth = ScreenUtil.parseDimenToPx(context, widthAttr, mWidth);
            } else {
                mWidth = Integer.parseInt(widthAttr);
            }

            if (!heightAttr.equals(ViewGroup.LayoutParams.WRAP_CONTENT + "")
                    && !heightAttr.equals(ViewGroup.LayoutParams.MATCH_PARENT + "")) {
                mHeight = ScreenUtil.parseDimenToPx(context, heightAttr, mHeight);
            } else {
                mHeight = Integer.parseInt(heightAttr);
            }

            SvgLog.I(String.format("widthAttr=%s, heightAttr=%s，width=%s, height=%s", widthAttr, heightAttr, mWidth, mHeight));

            //xml中配置svg图形宽高
            //未定义svg宽高，则使用原始数据

            final int INVALID_ID = Integer.MIN_VALUE;
            int srcId = a.getResourceId(R.styleable.IdeaSvgView_svgSrc, INVALID_ID);
            if (srcId != INVALID_ID) {
                //默认使用容器的宽高
                mVectorXmlInfo.setWidth(mWidth);
                mVectorXmlInfo.setHeight(mHeight);
                XmlResourceParser parser = getResources().getXml(srcId);
                mVectorXmlParser.parseVectorXml(parser, mVectorXmlInfo);
                invalidate();
            }
        } catch (Exception e) {
            SvgLog.I("读取attr发生异常：" + e.getMessage());
            mVectorXmlInfo.reset();
            e.printStackTrace();
        }
        a.recycle();

        Log.i("SvgLog", "解析耗时：" + (System.currentTimeMillis() - durationMs) + "ms.\n 获得的参数：" + mVectorXmlInfo.toString());
    }

    /** invalidate/postInvalidate会先被执行 */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void invalidate() {
        updatePathList();

        if (isUiThread()) {
            super.invalidate();
        } else {
            postInvalidate();
        }
    }

    @Override
    public void postInvalidate() {
        updatePathList();

        super.postInvalidate();
    }

    /**
     * 1、背景已经在{@link View#draw(Canvas)}中被绘制，这里可以不管，也可以重新绘制
     * 2、绘制svg（是否默认居中绘制？）
     * 3、绘制额外的drawable
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long time = System.currentTimeMillis();

        //重新绘制背景
        reDrawBackground(canvas);

        //LYX_TAG 2020/10/8 12:48 目前只解析vector xml中的<path标签
        //绘制svg
        drawSvg(canvas);

        //绘制其他drawable
        drawOther(canvas);

        //椭圆外接矩形长宽（长轴和短轴）
        SvgLog.i("绘制耗时[" + (System.currentTimeMillis() - time) + "]ms.");
    }

    private void reDrawBackground(@NonNull Canvas canvas) {
    }

    private void drawSvg(@NonNull Canvas canvas) {
        maybeFixSvgPosition(canvas);

        List<PathElement> elementList = mVectorXmlInfo.getPathElementList();
        for (PathElement element : elementList) {
            if (element == null) {
                continue;
            }

            if (element.getStrokeColor() != SvgConsts.INVALID_INT) {
                drawSvgStroke(canvas, element);
            }
            if (element.getFillColor() != SvgConsts.INVALID_INT) {
                drawSvgFill(canvas, element);
            }
        }
    }

    private void drawOther(@NonNull Canvas canvas) {

    }

    private void configSvgPaint(@NonNull PathElement element) {
        mSvgPaint.reset();
    }

    private void drawSvgStroke(@NonNull Canvas canvas, @NonNull PathElement element) {
        mSvgPaint.reset();
        float alpha = mVectorXmlInfo.getAlpha() * element.getStrokeAlpha();
        //setColor()也包含alpha设置，因此可能会覆盖setAlpha()的效果，应该在setAlpha之前配置
        mSvgPaint.setStyle(Paint.Style.STROKE);
        mSvgPaint.setColor(element.getStrokeColor());
        mSvgPaint.setAlpha((int)(255 * alpha));
        mSvgPaint.setStrokeWidth(element.getStrokeWidth());

        canvas.drawPath(element.getPath(), mSvgPaint);
    }

    private void drawSvgFill(@NonNull Canvas canvas, @NonNull PathElement element) {
        mSvgPaint.reset();
        mSvgPaint.setStyle(Paint.Style.FILL);
        float alpha = mVectorXmlInfo.getAlpha() * element.getFillAlpha();
        //setColor()也包含alpha设置，因此可能会覆盖setAlpha()的效果，应该在setAlpha之前配置
        mSvgPaint.setColor(element.getFillColor());
        mSvgPaint.setAlpha((int)(255 * alpha));

        canvas.drawPath(element.getPath(), mSvgPaint);
    }

    private void maybeFixSvgPosition(@NonNull Canvas canvas) {
        float l = Float.MAX_VALUE, t = Float.MAX_VALUE, r = Float.MIN_VALUE, b = Float.MIN_VALUE;
        //检查svg位置
        for (Path p : mSvgPathList) {
            svgRectF.setEmpty();
            p.computeBounds(svgRectF, true);
            l = Math.min(svgRectF.left, l);
            t = Math.min(svgRectF.top, t);
            r = Math.max(svgRectF.right, r);
            b = Math.max(svgRectF.bottom, b);
        }

        if (mIsSvgCenter) {
            forceSvgCenter(canvas, l, t, r, b);
        } else {
            fixSvgPadding(canvas, l, t);
        }
    }

    /** 计算得到所有path的边界坐标最值，实现svg强制居中 */
    private void forceSvgCenter(@NonNull Canvas canvas, float l, float t, float r, float b) {
        float translateX = ((getWidth() - r) + l) / 2f - l;
        float translateY = ((getHeight() - b) + t) / 2f - t;
        canvas.translate(translateX, translateY);
    }

    /**
     * 手动实现控件的padding，总是优先满足右移和下移
     * 如果经过左移，绘制内容到控件左端距离仍然大于右移值，则左移也生效；否则不再偏移;
     * 如果经过上移，绘制内容到控件顶端距离仍然大于下移值，则上移也生效；否则不再偏移
     */
    private void fixSvgPadding(Canvas canvas, float l, float t) {
        canvas.translate(getPaddingLeft(), getPaddingTop());

        if (l >= getPaddingRight() && getPaddingRight() != 0f) {
            canvas.translate(-getPaddingRight(), 0f);
        }

        if (t >= getPaddingBottom() && getPaddingBottom() != 0f) {
            canvas.translate(0f, -getPaddingBottom());
        }
    }

    /**
     * 更新待绘制的Path列表
     */
    private void updatePathList() {
        mSvgPathList.clear();
        List<PathElement> pathElementList = mVectorXmlInfo.getPathElementList();
        for (PathElement element : pathElementList) {
            if (element == null) {
                continue;
            }
            Path path = element.getPath();
            if (path != null) {
                mSvgPathList.add(path);
            }
        }
    }

    private boolean isUiThread() {
        return Looper.myLooper() != null && Looper.myLooper() == Looper.getMainLooper();
    }
}
