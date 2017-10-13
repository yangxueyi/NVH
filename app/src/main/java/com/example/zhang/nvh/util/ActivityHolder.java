package com.example.zhang.nvh.util;

import android.app.Activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yang
 * Time 2017/8/28.
 *
 *
 * 存储activity的工具类
 */
public class ActivityHolder {

    Map<Integer,Class<?extends Activity>> activityMap ;//将所有的activity存到map集合中
    public ActivityHolder(){
        activityMap = new HashMap<>();
    }

    /**
     * id:点击的按钮或者什么的id
     * 将activity添加到集合中
     * */
    public  void addActivity(Integer id,Class<?extends Activity> activity){
        if(!activityMap.containsKey(id)){//判断map集合中是否含有了这个activity，没有就添加
            activityMap.put(id,activity);
        }
    }
    /**获取activity*/
    public Class<? extends Activity> getActivity(Integer id){
        return activityMap.get(id);
    }

}
