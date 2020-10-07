package com.qxtx.idea.ideasvg.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.qxtx.idea.ideasvg.R;
import com.qxtx.idea.ideasvg.tools.ScreenUtil;
import com.qxtx.idea.ideasvg.xmlEntity.VectorElement;
import com.qxtx.idea.ideasvg.parser.VectorXmlParser;
import com.qxtx.idea.ideasvg.tools.SvgLog;

import java.util.ArrayList;
import java.util.List;

/**
 * @author QXTX-WIN
 * @date 2019/12/3 21:39
 * Description: 提供svg支持的控件，支持导入.svg文件，以及解析控件设置的以vector为根标签的xml
 */
public class IdeaSvgView extends View implements ISvgView {

    private final String TAG = getClass().getSimpleName();

    private float mWidth = 0f;
    private float mHeight = 0f;

    /** svg路径对象列表 */
    private final List<Path> svgPathList;

    /** svg的参数集 */
    private final VectorElement mVectorElement;

    /** svg数据解析器 */
    private final VectorXmlParser mVectorXmlParser;

    /** 控件背景 */
    private int mBgResId;

    /** 此paint负责绘制背景等svg之外的元素，不参与svg的绘制 */
    private final Paint mExtraPaint;

    /**
     * 控件的构造方法，可以在这里获得控件的xml属性值
     * 1、属性值获取优先级（低优先级被高优先级覆盖）：xml直接定义 > xml的style属性定义 > 构造方法里defStyleAttr定义 > 构造方法里defStyleRes定义 > theme中直接定义
     */
    public IdeaSvgView(Context context, AttributeSet attrs) {
        super(context, attrs, 0, 0);

        svgPathList = new ArrayList<>();
        mExtraPaint = new Paint();
        mVectorElement = new VectorElement();
        mVectorXmlParser = new VectorXmlParser(getContext());

        long durationMs = System.currentTimeMillis();

        //获得各种属性值
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IdeaSvgView);
        try {
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
//            mVectorElement.setWidth(a.getDimension(R.styleable.IdeaSvgView_svgWidth, 0));
//            mVectorElement.setHeight(a.getDimension(R.styleable.IdeaSvgView_svgHeight, 0));

            final int INVALID_ID = Integer.MIN_VALUE;
            int srcId = a.getResourceId(R.styleable.IdeaSvgView_svgSrc, INVALID_ID);
            if (srcId != INVALID_ID) {
                XmlResourceParser parser = getResources().getXml(srcId);
                mVectorXmlParser.parseVectorXml(parser, mVectorElement);
            }
        } catch (Exception e) {
            SvgLog.I("读取attr发生异常：" + e.getMessage());
            e.printStackTrace();
        }
        a.recycle();

        Log.e("流程耗时计算", "解析耗时：" + (System.currentTimeMillis() - durationMs) + "ms.\n 获得的参数：" + mVectorElement.toString());
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

        //椭圆外接矩形长宽（长轴和短轴）
        SvgLog.i("绘制耗时[" + (System.currentTimeMillis() - time) + "]ms.");
    }
}
