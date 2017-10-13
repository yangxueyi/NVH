package com.example.zhang.nvh.modlue.setting.presenter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.widget.Toast;

import com.example.zhang.nvh.R;
import com.example.zhang.nvh.modlue.bluetooth.activity.BluetoothActivtity;
import com.example.zhang.nvh.modlue.datas.DatasActivtity;
import com.example.zhang.nvh.modlue.frequency.FrequencyActivtity;
import com.example.zhang.nvh.modlue.microphone.MicrophoneActivtity;
import com.example.zhang.nvh.modlue.setting.contract.SettingContract;
import com.example.zhang.nvh.modlue.uploading.FileUploadingActivity;
import com.example.zhang.nvh.modlue.wifi.WifiActivtity;
import com.example.zhang.nvh.util.ActivityHolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by yang
 * Time 2017/8/24.
 */

public class SettingPresenter implements SettingContract.Presenter {

    private static ActivityHolder activityHolder;
    static {
        activityHolder = new ActivityHolder();//创建ActivityHolder工具类
        //将自己的activity添加到ActivityHolder中，通过键值对的形式，id是点击的条目或者按钮的id；
        //id就相当于是一个唯一的标识，对应所跳转的activity
        activityHolder.addActivity(R.id.btn_manage, FileUploadingActivity.class);
        activityHolder.addActivity(R.id.lin_layout_bluetooth, BluetoothActivtity.class);
        activityHolder.addActivity(R.id.lin_layout_datas, DatasActivtity.class);
        activityHolder.addActivity(R.id.lin_layout_wifi, WifiActivtity.class);
        activityHolder.addActivity(R.id.lin_layout_frequency, FrequencyActivtity.class);
        activityHolder.addActivity(R.id.lin_layout_microphone, MicrophoneActivtity.class);
    }

    Context context;
    SettingContract.View mView;
    //设置一个变量，记录按钮是开始、暂停、继续

    // 音频文件暂时保存的路径
    private String path = "";
    //音频文件合成后最终保存的路径
    private String path1 = "";
    // 语音文件
    private String fileName = null;
    private boolean isPause = false;// 当前录音是否处于暂停状态
    private ArrayList<String> mList = new ArrayList<>();// 待合成的录音片段
    private MediaRecorder mRecorder;



    public SettingPresenter(Context context,SettingContract.View view){
        this.context = context;
        this.mView = view;
    }

    @Override
    public void onClickStart() {
       mView.startSound();
    }

    @Override
    public void onClickStop() {
        mView.stopSound();
    }

    @Override
    public void onClickManage(int id) {
        //通过点击的按钮或者条目的id取出对应的activity
        Class activity = activityHolder.getActivity(id);
        if (activity!=null){
            context.startActivity(new Intent(context, activity));
        }
    }

    @Override
    public void onClickSyn() {

    }

    @Override
    public void onItemClick(int id ) {
        //通过点击的按钮或者条目的id取出对应的activity
        Class activity = activityHolder.getActivity(id);
        if (activity!=null){
//            context.startActivity(new Intent(context, activity));
            ((Activity)context).startActivityForResult(new Intent(context, activity),0);
        }
    }


    @Override
    public void setPath() {
        //创建音频文件临时保存路径
        path = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/voice";
        //创建合成后的音频文件保存路径
         path1 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyVoice";

    }


    // 开始录音
    @Override
    public void startRecord() {
        if (!isPause) {
            // 新录音清空列表
            mList.clear();
        }
        File file = new File(path);//创建音频文件暂时保存的文件夹
        File file1 = new File(path1);//创建合成后的音频文件保存的文件夹
        if (!file.exists()) {
            file.mkdirs();
        }
        if (!file1.exists()) {
            file1.mkdirs();
        }
        fileName = path + "/" + getTime() + ".amr";
        isPause = false;
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        // 选择amr格式
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
        mRecorder.setOutputFile(fileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mRecorder.prepare();
        } catch (Exception e) {
            // 若录音器启动失败就需要重启应用，屏蔽掉按钮的点击事件。 否则会出现各种异常。
            Toast.makeText(context, "录音器启动失败，请返回重试！", Toast.LENGTH_LONG).show();
            mRecorder.release();
            mRecorder = null;
        }
        if (mRecorder != null) {
            mRecorder.start();
        }
    }

    //暂停录音
    @Override
    public void pauseRecord() {
        mRecorder.stop();
        mRecorder.release();
        isPause = true;
        // 将录音片段加入列表
        mList.add(fileName);
    }

    // 完成录音
    @Override
    public void stopRecord() {

        if(mRecorder == null){

        }else {
            mRecorder.release();
            // 将录音片段加入列表
            mList.add(fileName);
            mRecorder = null;
            isPause = false;
            // 最后合成的音频文件
            fileName = path1 + "/" + getTime() + ".wav";
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(fileName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            FileInputStream fileInputStream = null;
            try {
                for (int i = 0; i < mList.size(); i++) {
                    File file = new File(mList.get(i));
                    // 把因为暂停所录出的多段录音进行读取
                    fileInputStream = new FileInputStream(file);
                    byte[] mByte = new byte[fileInputStream.available()];
                    int length = mByte.length;
                    // 第一个录音文件的前六位是不需要删除的
                    if (i == 0) {
                        while (fileInputStream.read(mByte) != -1) {
                            fileOutputStream.write(mByte, 0, length);
                        }
                    }
                    // 之后的文件，去掉前六位
                    else {
                        while (fileInputStream.read(mByte) != -1) {
                            fileOutputStream.write(mByte, 6, length - 6);
                        }
                    }
                }
            } catch (Exception e) {
                // 这里捕获流的IO异常，万一系统错误需要提示用户
                e.printStackTrace();
                Toast.makeText(context, "录音合成出错，请重试！", Toast.LENGTH_LONG).show();
            } finally {
                try {
                    fileOutputStream.flush();
                    fileInputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 不管合成是否成功、删除录音片段
            for (int i = 0; i < mList.size(); i++) {
                File file = new File(mList.get(i));
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }

    // 获得当前时间yyyy-MM-dd HH:mm
    private String getTime() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        return Long.toString(System.currentTimeMillis());
//        return formatter.format(curDate);
    }

    //来电话的时候暂停录音
    public void callPhone(){
        if (mRecorder != null) {
            // 暂停录音
            try {
                pauseRecord();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
