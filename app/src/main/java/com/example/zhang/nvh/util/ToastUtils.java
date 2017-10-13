package com.example.zhang.nvh.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by yang
 * Time 2017/8/24.
 */

public class ToastUtils {

    public static void toast (Context context,String str){
        Toast.makeText(context,str,Toast.LENGTH_SHORT).show();
    }
}
