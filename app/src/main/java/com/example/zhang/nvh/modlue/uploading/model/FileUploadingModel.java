package com.example.zhang.nvh.modlue.uploading.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.example.zhang.nvh.modlue.uploading.contract.FileUploadingContract;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by yang
 * Time 2017/8/28.
 */

public class FileUploadingModel implements FileUploadingContract.Model {

    private ArrayList<String> list;
    private ArrayList<String> list1;
    private Context context;

    public FileUploadingModel(Context context){
        this.context = context;
        list = new ArrayList<>();
        list1 = new ArrayList<>();
        initList();
        initList1();
    }

    @Override
    public void onGetFileResponse(FileUploadingContract.GetFileCallback getFileCallback) {
        try {
            if(list!=null&&list.size() >0){
                getFileCallback.onSuccess(list,list1);
            }else{
                getFileCallback.onError();
            }
        }catch (Exception e){
            getFileCallback.onNetworkError();
        }
    }


    // 初始化录音列表
    @Override
    public List<String> initList() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyVoice";
        // 判断SD卡是否存在
        if (!Environment.getExternalStorageState().equals( Environment.MEDIA_MOUNTED)) {
            Toast.makeText(context, "SD卡状态异常，无法获取录音列表！", Toast.LENGTH_LONG).show();
        } else {
            // 根据后缀名进行判断、获取文件夹中的音频文件
            File file = new File(path);
            Log.e( "initList: ","file  =  "+file.getAbsolutePath() );
            File files[] = file.listFiles();
            if (files != null) {
                for (File file1 : files) {
                    if (file1.getName().contains(".")) {
                        //获取文件的大小
                        float fileNumber = (float)(Math.round(file1.length())/1024.0);
                        // 只取.amr .mp3
                        // .mp4 文件
                        String fileStr = file1.getName().substring(
                                file1.getName().indexOf("."));
                        if (fileStr.toLowerCase().equals(".wav")
                                || fileStr.toLowerCase().equals(".amr")
                                || fileStr.toLowerCase().equals(".mp4")
                                || fileStr.toLowerCase().equals(".mp3")) {
                            //将文件名与文件大小都存入到集合中

                            String name = file1.getName();
                            String[] split = name.split("\\.");
                            long aLong = Long.parseLong(split[0]);

                            @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date curDate = new Date(aLong);// 获取当前时间
                            String format = formatter.format(curDate);

//                            Log.e( "initList: ", "aLong = "+aLong);

                            list.add(format+".wav" + ";" + fileNumber);
                        }
                    }
                }
            }
        }
        return list;
    }



    // 系统时间列表
    @Override
    public List<String> initList1() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyVoice";
        // 判断SD卡是否存在
        if (!Environment.getExternalStorageState().equals( Environment.MEDIA_MOUNTED)) {
            Toast.makeText(context, "SD卡状态异常，无法获取录音列表！", Toast.LENGTH_LONG).show();
        } else {
            // 根据后缀名进行判断、获取文件夹中的音频文件
            File file = new File(path);
            Log.e( "initList: ","file  =  "+file.getAbsolutePath() );
            File files[] = file.listFiles();
            if (files != null) {
                for (File file1 : files) {
                    if (file1.getName().contains(".")) {
                        //获取文件的大小
                        float fileNumber = (float)(Math.round(file1.length())/1024.0);
                        // 只取.amr .mp3
                        // .mp4 文件
                        String fileStr = file1.getName().substring(
                                file1.getName().indexOf("."));
                        if (fileStr.toLowerCase().equals(".mp3")
                                || fileStr.toLowerCase().equals(".amr")
                                || fileStr.toLowerCase().equals(".mp4")
                                || fileStr.toLowerCase().equals(".wav")) {
                            //将文件名与文件大小都存入到集合中

                            String name = file1.getName();
                            Log.e( "initList: ", "aLong = "+name);

                            list1.add(name + ";" + fileNumber);
                        }
                    }
                }
            }
        }
        return list1;
    }
}
