package com.example.zhang.nvh.util;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yang
 * Time 2017/9/5.
 */

public class TimerUtils {

    private int i = 0;
    private int TIME = 1000;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                i++;
            }
            super.handleMessage(msg);
        }
    };

    Timer timer = new Timer();
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            // 需要做的事:发送消息
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        }
    };

    public void startTimer(){
        timer.schedule(task, TIME, TIME); // 1s后执行task,经过1s再次执行
    }

    public int getTimer(){
        return  i;
    }


}
