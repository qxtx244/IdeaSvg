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

        handler = new Handler();
    }

    @Override
    public void onClick(View view) {
    }

    private void revert(Runnable runnable) {
        handler.postDelayed(runnable, animDurationMs + 200);
    }

}

