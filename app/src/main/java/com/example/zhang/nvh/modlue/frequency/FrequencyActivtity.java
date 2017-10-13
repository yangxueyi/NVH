package com.example.zhang.nvh.modlue.frequency;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.zhang.nvh.R;
import com.example.zhang.nvh.base.BaseActivity;

/**
 * Created by yang
 * Time 2017/8/29.
 */

public class FrequencyActivtity extends BaseActivity {


    private Toolbar toolbar;

    @Override
    protected void initLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_frequency);
    }

    @Override
    protected void initView() {
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        //设置toolbar
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.mipmap.back);//返回按钮
        setSupportActionBar(toolbar);
    }
    @Override
    protected void initListener() {
        //返回按钮
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }



}



