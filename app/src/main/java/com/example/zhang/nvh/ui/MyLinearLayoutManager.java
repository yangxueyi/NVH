package com.example.zhang.nvh.ui;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

/**
 * Created by yang
 * Time 2017/8/31.
 */

/**
 * 设置recycleview不可滑动
 * */
public class MyLinearLayoutManager extends LinearLayoutManager {
    private boolean isScrollEnabled = true;
    public MyLinearLayoutManager(Context context) {
        super(context);
    }

    public void setScrollEnabled(boolean flag){
        this.isScrollEnabled = flag;
    }

    @Override
    public boolean canScrollVertically() {
        return isScrollEnabled && super.canScrollVertically();
    }
}
