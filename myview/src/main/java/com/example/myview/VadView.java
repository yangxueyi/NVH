package com.example.myview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by YangXueYi
 * Time： 2018/1/2.
 */

class MyView extends View {
    private int XPoint = 60;
    private int YPoint = 260;
    private int XScale = 8; // 刻度长度
    private int YScale = 40;
    private int XLength = 380;
    private int YLength = 240;

    private float ratio1 = 2;

    private int MaxDataSize = XLength / XScale;

    private List<Integer> data = new ArrayList<>();

    private String[] YLabel = new String[YLength / YScale];

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0x1234) {
                MyView.this.invalidate();
            }
        }
    };
    private Timer timerTime;
    private MediaRecorder mRecorder;

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        for (int i = 0; i < YLabel.length; i++) {
            YLabel[i] = (i + 1) + "M/s";
        }
    }

    //开启定时器
    public void startTimer(){
        if(timerTime == null) {
            timerTime = new Timer();
        }
        timerTime.schedule(new TimerTask() {
            @Override
            public void run() {
                if (data.size() >= MaxDataSize) {
                    data.remove(0);
                }
                data.add(new Random().nextInt(4) + 1);

                handler.sendEmptyMessage(0x1234);
            }
        },1000,100);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true); // 去锯齿
        paint.setColor(Color.BLUE);

        // 画Y轴
        canvas.drawLine(XPoint, YPoint - YLength, XPoint, YPoint, paint);

        // Y轴箭头
        canvas.drawLine(XPoint, YPoint - YLength, XPoint - 3, YPoint - YLength
                + 6, paint); // 箭头
        canvas.drawLine(XPoint, YPoint - YLength, XPoint + 3, YPoint - YLength
                + 6, paint);

        // 添加刻度和文字
        for (int i = 0; i * YScale < YLength; i++) {
            canvas.drawLine(XPoint, YPoint - i * YScale, XPoint + 5, YPoint - i
                    * YScale, paint); // 刻度

            canvas.drawText(YLabel[i], XPoint - 50, YPoint - i * YScale, paint);// 文字
        }

        // 画X轴
        canvas.drawLine(XPoint, YPoint, XPoint + XLength, YPoint, paint);

        // 绘折线
        /*
         * if(data.size() > 1){ for(int i=1; i<data.size(); i++){
         * canvas.drawLine(XPoint + (i-1) * XScale, YPoint - data.get(i-1) *
         * YScale, XPoint + i * XScale, YPoint - data.get(i) * YScale, paint); }
         * }
         */
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);

        Paint paint2 = new Paint();
        paint2.setColor(Color.BLUE);
        paint2.setStyle(Paint.Style.FILL);
        if (data.size() > 1) {
            //画曲线
             Path path = new Path();
             //画曲线下面的阴影
             Path path4 = new Path();
            path.moveTo(XPoint, YPoint - data.get(0) * YScale);
            path4.moveTo(XPoint, YPoint);
            for (int i = 0; i < data.size(); i++) {
                path.lineTo(XPoint + i * XScale, YPoint - data.get(i) * YScale);
                path4.lineTo(XPoint + i * XScale, YPoint - data.get(i) * YScale);
            }
            path4.lineTo(XPoint + (data.size() - 1) * XScale, YPoint);
            canvas.drawPath(path, paint);
            canvas.drawPath(path4, paint2);
        }
    }
}