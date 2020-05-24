package com.qxtx.idea.ideasvg.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.qxtx.idea.ideasvg.R;
import com.qxtx.idea.ideasvg.entity.SvgParam;
import com.qxtx.idea.ideasvg.parser.SvgParser;
import com.qxtx.idea.ideasvg.tools.SvgLog;

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

        SvgLog.I("耗时：" + (System.currentTimeMillis() - durationMs) + "ms.");

        SvgLog.I("获得的参数：" + mSvgParam.toString());
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
}
