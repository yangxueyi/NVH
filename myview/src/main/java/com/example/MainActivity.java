package com.example;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.myview.R;


/**
 * Created by YangXueYi
 * Time： 2018/1/10.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private MyView vad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vad = (MyView) findViewById(R.id.vad);
        Button btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn :
                //开启定时器
                vad.startTimer();
                break;

        }
    }
}