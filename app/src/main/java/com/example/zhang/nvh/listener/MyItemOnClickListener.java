package com.example.zhang.nvh.listener;

import android.view.View;

/**
 * Created by yang
 * Time 2017/9/4.
 */

public interface MyItemOnClickListener {
     void onItemOnClick(View view, int postion,int isClick) throws Exception;
     void onItemOnLongClick(View view, int postion) throws Exception;
}
