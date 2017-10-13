package com.example.zhang.nvh.modlue.uploading.presenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.zhang.nvh.listener.MyItemOnClickListener;
import com.example.zhang.nvh.modlue.bluetooth.adapter.UseableAdapter;
import com.example.zhang.nvh.modlue.uploading.FileUploadingActivity;
import com.example.zhang.nvh.modlue.uploading.adapter.UploadingAdapter;
import com.example.zhang.nvh.modlue.uploading.contract.FileUploadingContract;
import com.example.zhang.nvh.modlue.uploading.model.FileUploadingModel;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.security.auth.login.LoginException;

/**
 * Created by yang
 * Time 2017/8/28.
 */

public class FileUploadingPresenter implements FileUploadingContract.Presenter{

    private Context mContext;
    private FileUploadingContract.View mView;
    private FileUploadingContract.Model mModel;
    private UploadingAdapter mUploadingAdapter;
    private MediaRecorder mMediaRecorder;
    private MediaPlayer mPlayer;
    private String playFileName;
    private SeekBar mProgressBar;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private boolean isChanging =false;//互斥变量，防止定时器与SeekBar拖动时进度冲突
    //    private int currentPosition;//当前音乐播放的进度


    public FileUploadingPresenter(Context context, FileUploadingContract.View view, SeekBar progressBar, FileUploadingModel model, UploadingAdapter uploadingAdapter){
        this.mContext = context;
        this.mView = view;
        this.mProgressBar = progressBar;
        this.mModel = model;
        this.mUploadingAdapter = uploadingAdapter;

        mProgressBar.setOnSeekBarChangeListener(new MySeekbar());



    }

    @Override
    public void onLoadDatas() {

        mModel.onGetFileResponse(new FileUploadingContract.GetFileCallback() {
            @Override
            public void onNetworkError() {
                mView.onShowToast("蓝牙连接不稳定");
            }
            @Override
            public void onSuccess(final List list,final List list1) {
                Handler handler = new Handler(Looper.getMainLooper()) ;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mView.getDataList(list,list1);
                    }
                },1000);
            }
            @Override
            public void onError() {
                mView.onShowToast("没有录音文件");
            }
        });

    }

    @Override
    public void onDeleteFile() {
        mView.deleteFile();
        //停止播放
        stopMediaPlayer();
    }

    @Override
    public void onUploadingFile() {

    }

    //单击执行哪一步
    private int isChick = 1;

    @Override
    public void onItemClick() {
        mUploadingAdapter.setItemOnClickListener(new MyItemOnClickListener() {
            @Override
            public void onItemOnClick(View view, int postion,int isClick) throws Exception {
                if (isClick == 1){
                    mProgressBar.setVisibility(View.VISIBLE);
                    mView.ItemClick( postion);
                }
                if(isClick == 2){
//                    mView.getListPostion(postion);
                    mView.onShowToast("请选择要删除的文件");
                }

            }

            @Override
            public void onItemOnLongClick(View view, int postion) throws Exception {
                mView.ItemLongClick();
            }
        });
    }

    @Override
    public void onDeriveFile() {

    }


    // 播放录音
    @Override
    public void playRecord(int position) {

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyVoice";
        List list = mModel.initList1();

        // 要播放文件的路径
        String name = (String) list.get(position);
        String[] split = name.split(";");

        playFileName = path + "/" + split[0];

        Log.e("playRecord: ","playFileName = "+playFileName );

        // 对按钮的可点击事件的控制是保证不出现空指针的重点！！
        stopMediaPlayer();
        mPlayer = new MediaPlayer();
            try {
                // 播放所选中的录音
                mPlayer.setDataSource(playFileName);
                mPlayer.prepare();
                mPlayer.start();
//            mPlayer.seekTo(mPlayer.getCurrentPosition());
                Log.e("playRecord: ", "play = " + mPlayer.getDuration());

            } catch (Exception e) {
                // 若出现异常被捕获后，同样要释放掉资源
                // 否则程序会不稳定，不适合正式项目上使用
                stopMediaPlayer();
            }
            mProgressBar.setMax(mPlayer.getDuration());//设置进度条最大值
            //----------定时器记录播放进度---------//
            mTimer = new Timer();
            mTimerTask = new TimerTask() {
                @Override
                public void run() {

                    if (isChanging == true) {
                        return;
                    }
                    if (mPlayer != null) {
                        int currentPosition = mPlayer.getCurrentPosition();
                        if (currentPosition >= 0) {
                                mProgressBar.setProgress(currentPosition);
                        } else {
                            mProgressBar.setProgress(0);
                        }
                    }
                }
            };
            mTimer.schedule(mTimerTask, 0, 100);

        // 播放完毕的监听
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                if (!mPlayer.isPlaying()) {//如果不在播放状态，则停止更新//播放器进度条，防止界面报错
                    mProgressBar.setProgress(0);
                    mProgressBar.setVisibility(View.GONE);
                }
                // 播放完毕改变状态，释放资源
                stopMediaPlayer();
                Log.e( "playRecord: ", "结束");

            }
        });
    }
    //释放资源
    public void stopMediaPlayer(){
        if (mPlayer != null) {
            //必须要重置与释放和置为空
            mPlayer.reset();//重置MediaPlayer
            mPlayer.release();//释放MediaPlayer
            mPlayer = null;
            isChick = 1;

            //释放定时器
            if(mTimer!=null){
                mTimer.cancel();
                mTimer.purge();
                mTimer= null;
            }
            if(mTimerTask!=null){
                mTimerTask.cancel();
                mTimerTask = null;
            }
            Log.e( "playRecord: ", "123123123123");
        }else{
            Log.e( "playRecord: ", "456456456456");
        }
    }


    //进度条处理
    class MySeekbar implements SeekBar.OnSeekBarChangeListener {
        public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {

//            Log.e( "playRecord: ", "isChanging = ");
          /*  if(fromUser){
                if(mPlayer != null ){
                    mPlayer.seekTo(progress);
                    mProgressBar.setProgress(progress);
                }
            }*/
        }
        public void onStartTrackingTouch(SeekBar seekBar) {
            isChanging=true;
            Log.e( "playRecord: ", "isChanging = "+isChanging);
        }

        public void onStopTrackingTouch(SeekBar seekBar) {

            Log.e( "playRecord: ", "mProgressBar = "+mProgressBar.getProgress());
            mPlayer.seekTo(mProgressBar.getProgress());
            isChanging=false;


        }

    }
}
