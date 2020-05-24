package com.qxtx.idea.ideasvgdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.qxtx.idea.ideasvg.view.IdeaSvgView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private IdeaSvgView viewSvg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewSvg = findViewById(R.id.viewSvg);
    }

    @Override
    public void onClick(View v) {

    }
}

