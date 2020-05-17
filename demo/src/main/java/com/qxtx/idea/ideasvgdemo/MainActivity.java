package com.qxtx.idea.ideasvgdemo;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import com.qxtx.idea.ideasvg.animation.SvgPathMovingAnim;
import com.qxtx.idea.ideasvg.animation.SvgTrimAnim;
import com.qxtx.idea.ideasvg.view.IdeaSvgView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private IdeaSvgView viewSvg;
    private Button btnScale;
    private Button btnTranslate;
    private Button btnTrim;
    private Button btnPathMoving;
    private Button btnGesture;
    private Button btnAlpha;
    private Button btnColorful;
    private Button btnSvgChange;

    private boolean isGestureEnable = false;

    private final float scale = 5f;
    private final int alpha = 0;
    private final float transX = 300, transY = 300;
    private long animDurationMs = 1200;
    private final int[] colors = new int[]{Color.BLUE, Color.GREEN, Color.RED, Color.WHITE, Color.YELLOW};

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewSvg = findViewById(R.id.viewSvg);
        btnScale = findViewById(R.id.btnScale);
        btnScale.setOnClickListener(this);
        btnTranslate = findViewById(R.id.btnTranslate);
        btnTranslate.setOnClickListener(this);
        btnTrim = findViewById(R.id.btnTrim);
        btnTrim.setOnClickListener(this);
        btnPathMoving = findViewById(R.id.btnPathMoving);
        btnPathMoving.setOnClickListener(this);
        btnGesture = findViewById(R.id.btnGesture);
        btnGesture.setOnClickListener(this);
        btnAlpha = findViewById(R.id.btnAlpha);
        btnAlpha.setOnClickListener(this);
        btnColorful = findViewById(R.id.btnColorful);
        btnColorful.setOnClickListener(this);
        btnSvgChange = findViewById(R.id.btnSvgChange);
        btnSvgChange.setOnClickListener(this);

        handler = new Handler();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnScale:
                viewSvg.svgScale(scale, animDurationMs, null);
                revert(() -> viewSvg.svgScale(1f, animDurationMs, null));
                break;
            case R.id.btnTranslate:
                viewSvg.svgTranslate(transX, transY, animDurationMs, null);
                revert(() -> viewSvg.svgTranslate(0, 0, animDurationMs, null));
                break;
            case R.id.btnTrim:
                viewSvg.startSvgAnim(new SvgTrimAnim(animDurationMs));
                break;
            case R.id.btnPathMoving:
                viewSvg.startSvgAnim(new SvgPathMovingAnim(animDurationMs));
                break;
            case R.id.btnGesture:
                isGestureEnable = !isGestureEnable;
                viewSvg.setSvgGestureEnable(isGestureEnable);
                btnGesture.setText(isGestureEnable ? "手势:已启用" : "手势:已禁用");
                viewSvg.svgScale(1f, 0,null);
                break;
            case R.id.btnAlpha:
                viewSvg.svgAlpha(alpha, animDurationMs, null);
                revert(() -> viewSvg.svgAlpha(255, animDurationMs, null));
                break;
            case R.id.btnColorful:
                String svgData = getResources().getString(R.string.svg_heart);
                viewSvg.setStokeColor(colors);
                viewSvg.showSvg(svgData);
                break;
            case R.id.btnSvgChange:
                viewSvg.showSvg(getResources().getString(R.string.svg_x), animDurationMs, null);
                revert(() -> viewSvg.showSvg(getResources().getString(R.string.svg_heart), animDurationMs, null));
                break;
        }
    }

    private void revert(Runnable runnable) {
        handler.postDelayed(runnable, animDurationMs + 200);
    }

}

