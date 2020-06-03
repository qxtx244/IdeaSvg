package com.qxtx.idea.ideasvgdemo;

import android.content.res.XmlResourceParser;
import android.graphics.Path;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.qxtx.idea.ideasvg.view.IdeaSvgView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private IdeaSvgView viewSvg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewSvg = findViewById(R.id.viewSvg);
        viewSvg.invalidate();

//        long durationMs = System.currentTimeMillis();
//        DrawEllipse.EllipseInfo info = DrawEllipse.svgArcToCenterParam(200,200,100,70,0,0,0,225,100);
//        Log.i("IdeaSvg", "写文件耗时：" + (System.currentTimeMillis() - durationMs) + "ms. info=" + info.toString());

    }

    @Override
    public void onClick(View v) {

    }
}

