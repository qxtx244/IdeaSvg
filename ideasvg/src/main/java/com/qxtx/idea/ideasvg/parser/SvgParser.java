package com.qxtx.idea.ideasvg.parser;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.qxtx.idea.ideasvg.entity.PathParam;
import com.qxtx.idea.ideasvg.entity.SvgParam;
import com.qxtx.idea.ideasvg.tools.SvgLog;

import org.xmlpull.v1.XmlPullParser;

import java.util.List;

/**
 * CreateDate 2020/5/20 23:48
 * <p>
 *
 * @author QXTX-WIN
 * Description: svg控件的辅助解析类，负责为svg控件解析xml，.svg等工作
 */
public class SvgParser implements IParser {

    private final String SUPPORT_ROOT_TAG = "vector";

    public SvgParser() { }

    /**
     * 解析xml中的vector标签数据
     * @param xmlParser 目标xml的解析对象，能方便地获取到xml中的数据
     * @param param svg的配置参数集，解析到的数据保存到此对象中
     */
    public void parseVectorXml(@NonNull Resources resources,  @NonNull XmlResourceParser xmlParser, @NonNull SvgParam param) throws Exception {
        //先清空原有的数据
        param.reset();

        if (xmlParser.isEmptyElementTag()) {
            SvgLog.I("EmptyElementTag！无法解析xml");
            return ;
        }

        if (xmlParser.getEventType() != XmlPullParser.START_DOCUMENT) {
            SvgLog.I("没有以START_DOCUMENT开始，无法解析xml");
            return ;
        }

        String tag;
        int attrCount;

        //仅支持根标签为vector的xml
        while ((xmlParser.next()) != XmlPullParser.END_DOCUMENT) {
            tag = xmlParser.getName();
            if (!TextUtils.isEmpty(tag) && tag.equals(SUPPORT_ROOT_TAG)) {
                break;
            }
        }

        tag = xmlParser.getName();
        if (!TextUtils.isEmpty(tag) && !SUPPORT_ROOT_TAG.equals(tag)) {
            SvgLog.I("未找到起始的vector标签");
            return ;
        }

        int DEF_ATTR_VALUE = 0;
        //读取根标签的属性
        int spec;
        String value;
        int viewportSpec;
        float svgAlpha = 1f;
        attrCount = xmlParser.getAttributeCount();
        for (int i = 0; i < attrCount; i++) {
            String name = xmlParser.getAttributeName(i);
            switch (name) {
                case "width":
                    //解析得到"xxxx.0dip",需要去掉后面的.0dip字符串，读出实际int数值
                    value = xmlParser.getAttributeValue(i);
                    if (TextUtils.isEmpty(value)) {
                        param.setWidth(0);
                    } else {
                        int width = Integer.parseInt(value.substring(0, value.length() - 5));
                        param.setWidth(width);
                    }
                    break;
                case "height":
                    //解析得到"xxxx.0dip",需要去掉后面的.0dip字符串，读出实际整数值
                    value = xmlParser.getAttributeValue(i);
                    if (TextUtils.isEmpty(value)) {
                        param.setHeight(0);
                    } else {
                        int height = Integer.parseInt(value.substring(0, value.length() - 5));
                        param.setHeight(height);
                    }
                    break;
                case "viewportWidth":
                    //解析得到"xxxx.0",需要去掉后面的.0字符串，读出实际整数值
                    value = xmlParser.getAttributeValue(i);
                    int viewportWidth = Integer.parseInt(value.substring(0, value.length() - 2));
                    viewportSpec = param.getViewportSpec() | ((viewportWidth << 16) & 0xffff0000);
                    param.setViewportSpec(viewportSpec);

                    if (param.getWidth() == 0) {
                        param.setWidth(viewportWidth);
                    }
                    break;
                case "viewportHeight":
                    //解析得到"xxxx.0",需要去掉后面的.0字符串，读出实际整数值
                    value = xmlParser.getAttributeValue(i);
                    int viewportHeight = Integer.parseInt(value.substring(0, value.length() - 2));
                    viewportSpec = param.getViewportSpec() | (viewportHeight & 0xffff);
                    param.setViewportSpec(viewportSpec);

                    if (param.getHeight() == 0) {
                        param.setHeight(viewportHeight);
                    }
                    break;
                case "alpha":
                    svgAlpha = xmlParser.getAttributeFloatValue(i, 1f);
                    break;
                case "name":
                    param.setName(xmlParser.getAttributeValue(i));
                    break;
                case "tint":
                    value = xmlParser.getAttributeValue(i);
                    int color = 0;
                    char firstChar = value.charAt(0);
                    if (firstChar == '#') {
                        color = Color.parseColor(value);
                    } else if (firstChar == '@') {
                        color = resources.getColor(xmlParser.getAttributeResourceValue(i, Integer.MIN_VALUE));
                    }
                    param.setTint(color);
                    break;
                case "tintMode":
                    param.setTintMode(xmlParser.getAttributeValue(i));
                    break;
                case "autoMirror":
                    param.setAutoMirrored(xmlParser.getAttributeBooleanValue(i, false));
                    break;
            }
        }

        int eventType;
        while ((eventType = xmlParser.next()) != XmlPullParser.END_DOCUMENT) {
            if (eventType != XmlPullParser.START_TAG) {
                continue;
            }

            tag = xmlParser.getName();
            if (TextUtils.isEmpty(tag) || !tag.equals("path")) {
                continue;
            }

            List<PathParam> pathParamList = param.getPathParamList();
            PathParam pathParam = new PathParam();
            pathParamList.add(pathParam);

            attrCount = xmlParser.getAttributeCount();
            for (int i = 0; i < attrCount; i++) {
                String name = xmlParser.getAttributeName(i);
                switch (name) {
                    case "pathData":
                        String pathData = xmlParser.getAttributeValue(i);
                        pathParam.updatePathData(pathData);
                        break;
                    case "strokeWidth":
                        pathParam.setStrokeWidth(xmlParser.getAttributeFloatValue(i, pathParam.getStrokeWidth()));
                        break;
                    case "strokeColor":
                        pathParam.setStrokeColor(xmlParser.getAttributeIntValue(i, pathParam.getStrokeColor()));
                        break;
                    case "strokeAlpha":
                        //需要与全局透明度叠加
                        float strokeAlpha = xmlParser.getAttributeFloatValue(i, pathParam.getStrokeAlpha());
                        pathParam.setStrokeAlpha(strokeAlpha * svgAlpha);
                        break;
                    case "fillColor":
                        pathParam.setFillColor(xmlParser.getAttributeIntValue(i, pathParam.getFillColor()));
                        break;
                    case "fillAlpha":
                        //需要与全局透明度叠加
                        float fillAlpha = xmlParser.getAttributeFloatValue(i, pathParam.getFillAlpha());
                        pathParam.setFillAlpha(fillAlpha * svgAlpha);
                        break;
                    case "name":
                        pathParam.setName(xmlParser.getAttributeValue(i));
                        break;
                    case "strokeLineCap":
                        pathParam.setStrokeLineCap(xmlParser.getAttributeIntValue(i, pathParam.getStrokeLineCap()));
                        break;
                    case "strokeLineJoin":
                        pathParam.setStrokeLineJoin(xmlParser.getAttributeIntValue(i, pathParam.getStrokeLineJoin()));
                        break;
                    case "strokeMiterLimit":
                        pathParam.setStrokeMiterLimit(xmlParser.getAttributeFloatValue(i, pathParam.getStrokeMiterLimit()));
                        break;
                    case "trimPathStart":
                        pathParam.setTrimPathStart(xmlParser.getAttributeFloatValue(i, pathParam.getTrimPathStart()));
                        break;
                    case "trimPathEnd":
                        pathParam.setTrimPathEnd(xmlParser.getAttributeFloatValue(i, pathParam.getTrimPathEnd()));
                        break;
                    case "trimPathOffset":
                        pathParam.setTrimPathOffset(xmlParser.getAttributeFloatValue(i, pathParam.getTrimPathOffset()));
                        break;
                }
            }
        }
    }
}
