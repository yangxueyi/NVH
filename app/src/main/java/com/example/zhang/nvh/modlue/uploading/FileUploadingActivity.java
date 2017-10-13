package com.example.zhang.nvh.modlue.uploading;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.carlos.voiceline.mylibrary.VoiceLineView;
import com.example.zhang.nvh.R;
import com.example.zhang.nvh.base.BaseActivity;
import com.example.zhang.nvh.modlue.uploading.adapter.UploadingAdapter;
import com.example.zhang.nvh.modlue.uploading.contract.FileUploadingContract;
import com.example.zhang.nvh.modlue.uploading.model.FileUploadingModel;
import com.example.zhang.nvh.modlue.uploading.presenter.FileUploadingPresenter;
import com.example.zhang.nvh.ui.MyItemDecoration;
import com.example.zhang.nvh.ui.MyLinearLayoutManager;
import com.example.zhang.nvh.util.ToastUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by yang
 * Time 2017/8/28.
 */

public class FileUploadingActivity extends BaseActivity implements FileUploadingContract.View,View.OnClickListener{


    private Toolbar toolbar;
    private RecyclerView recycle_uploading;
    private Button btn_delete;
    private Button btn_derive;
    private Button btn_uploading;
    private UploadingAdapter uploadingAdapter;
    private FileUploadingPresenter mFileUploadingPresenter;

    private MediaRecorder mMediaRecorder;
    private boolean isAlive = true;


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(mMediaRecorder==null) return;
            double ratio = (double) mMediaRecorder.getMaxAmplitude() / 100;
            double db = 0;// 分贝
            //默认的最大音量是100,可以修改，但其实默认的，在测试过程中就有不错的表现
            //你可以传自定义的数字进去，但需要在一定的范围内，比如0-200，就需要在xml文件中配置maxVolume
            //同时，也可以配置灵敏度sensibility
            if (ratio > 1)
                db = 20 * Math.log10(ratio);
            //只要有一个线程，不断调用这个方法，就可以使波形变化
            //主要，这个方法必须在ui线程中调用

        }
    };
    private SeekBar seekBar;
    private CheckBox check_box;


    @Override
    protected void initLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_file_uploading);
    }

    @Override
    protected void initView() {

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        recycle_uploading = (RecyclerView) findViewById(R.id.recycle_uploading);
        btn_delete = (Button) findViewById(R.id.btn_delete);
        btn_derive = (Button) findViewById(R.id.btn_derive);
        btn_uploading = (Button) findViewById(R.id.btn_uploading);
        seekBar = (SeekBar) findViewById(R.id.progress_Bar);
        check_box = (CheckBox) findViewById(R.id.check_box);

        //设备布局管理器
        MyLinearLayoutManager linearLayoutManager = new MyLinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//        linearLayoutManager.setScrollEnabled(false);//设置为false为不可滑动;
        recycle_uploading.addItemDecoration(new MyItemDecoration(this, LinearLayoutManager.VERTICAL));
        recycle_uploading.setLayoutManager(linearLayoutManager);

        uploadingAdapter = new UploadingAdapter(this);

        //设置toolbar
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.mipmap.back);//返回按钮
        setSupportActionBar(toolbar);

        mFileUploadingPresenter = new FileUploadingPresenter(this,this,seekBar,new FileUploadingModel(this),uploadingAdapter);

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

        btn_delete.setOnClickListener(this);
        btn_derive.setOnClickListener(this);
        btn_uploading.setOnClickListener(this);

        mFileUploadingPresenter.onLoadDatas();
        //这里可以开始搜索操作
        recycle_uploading.setAdapter(uploadingAdapter);
        mFileUploadingPresenter.onItemClick();

    }

    @Override
    protected void onDestroy() {
        //关闭声音曲线图
        isAlive = false;
        if(mMediaRecorder!=null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }

        mFileUploadingPresenter.stopMediaPlayer();
        super.onDestroy();
    }

    @Override
    public void onShowToast(String str) {
        ToastUtils.toast(this,str);
    }

    @Override
    public void deleteFile() {
        uploadingAdapter.removeItem();

    }

    @Override
    public void uploadingFile() {

    }

    @Override
    public void deriveFile() {
    }

    @Override
    public void getDataList(List<String> datas,List<String> list) {
        uploadingAdapter.setData(datas,list);
    }

    @Override
    public void ItemClick(final int position) {
        mFileUploadingPresenter.playRecord(position);
    }


    @Override
    public void ItemLongClick() {
        uploadingAdapter.setShowBox();
        uploadingAdapter.notifyDataSetChanged();
    }

    @Override
    public void getListPostion(int postion) {
        uploadingAdapter.setListPostion(postion);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_delete:
                mFileUploadingPresenter.onDeleteFile();
                break;
            case R.id.btn_derive:
                break;
            case R.id.btn_uploading:
                break;
        }
    }

    /**开启声音曲线图*/
    private void startMedia() {
        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        File file = new File(Environment.getExternalStorageDirectory(), "hello.log");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        File filePath = new File(file.getPath() + File.separator + "haha.log");
        mMediaRecorder.setOutputFile(file.getAbsolutePath());
        Log.e( "startMedia: ","file = "+ file.getPath());
        mMediaRecorder.setMaxDuration(1000 * 60 * 10);
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaRecorder.start();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isAlive) {
                    handler.sendEmptyMessage(0);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

}
