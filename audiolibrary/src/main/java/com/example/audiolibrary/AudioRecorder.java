package com.example.audiolibrary;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by YangXueYi
 * Time： 2018/1/5.
 */

public class AudioRecorder {
    //音频输入-麦克风
    private final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;
    //采用频率
    //44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    //采样频率一般共分为22.05KHz、44.1KHz、48KHz三个等级
    private final static int AUDIO_SAMPLE_RATE = 16000;
    //声道 单声道
    private final static int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    //编码
    private final static int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    // 缓冲区字节大小
    private int bufferSizeInBytes = 0;

    //录音对象
    private AudioRecord audioRecord;

    //录音状态
    private Status status = Status.STATUS_NO_READY;

    //文件名
    private String fileName;

    //录音文件
    private List<String> filesName = new ArrayList<>();

    //线程池
    private ExecutorService mExecutorService;

    //录音监听
    private RecordStreamListener listener;
    private Timer timerTime;
    private final Context context;

/*

    @SuppressLint("HandlerLeak")
    private Handler myHandler = new Handler(){

        // 2.重写消息处理函数
        public void handleMessage(Message msg){
            switch (msg.what) {
                case 2:{
                    //获取当前时间   精确到毫秒yyyyMMdd--HH:mm:ss:SSS
                    @SuppressLint
                            ("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd--HH:mm:ss");
//                    float timeF = System.currentTimeMillis();
                    Date curDate = new Date(System.currentTimeMillis());//获取当前时间
                    String time = formatter.format(curDate);


                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            short[] buffer = new short[bufferSizeInBytes];
                            int r = audioRecord.read(buffer, 0, bufferSizeInBytes);
                            long v = 0;
                            for(int i = 0;i < buffer.length; i++){
                                v += buffer[i] * buffer[i];
                            }
                            double mean = v / (double)r;
                            double volume = 10 * Math.log10(mean);
                            Log.e("AudioRecorder", "分贝值: "+volume );

                        }
                    }).start();


                }
            }
            super.handleMessage(msg);
        }
    };
*/



    public AudioRecorder(Context context) {
        mExecutorService = Executors.newCachedThreadPool();
        this.context = context;
    }

    /**
     * 创建录音对象
     */
    public void createAudio(String fileName, int audioSource, int sampleRateInHz, int channelConfig, int audioFormat) {
        // 获得缓冲区字节大小
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
                channelConfig, channelConfig);
        audioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
        this.fileName = fileName;
    }

    /**
     * 创建默认的录音对象
     *
     * @param fileName 文件名
     */
    public void createDefaultAudio(String fileName) {
        // 获得缓冲区字节大小
        bufferSizeInBytes = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE,
                AUDIO_CHANNEL, AUDIO_ENCODING);
        audioRecord = new AudioRecord(AUDIO_INPUT, AUDIO_SAMPLE_RATE, AUDIO_CHANNEL, AUDIO_ENCODING, bufferSizeInBytes);
        this.fileName = fileName;
        status = Status.STATUS_READY;
    }


    Object mLock = new Object();

    /**
     * 开始录音
     *
     */
    public void startRecord() {

        if (status == Status.STATUS_NO_READY||audioRecord==null) {
//            throw new IllegalStateException("录音尚未初始化,请检查是否禁止了录音权限~");
            Log.d("AudioRecorder", "录音尚未初始化,请检查是否禁止了录音权限~" );
        }
        if (status == Status.STATUS_START) {
            throw new IllegalStateException("正在录音");
        }
        Log.d("AudioRecorder", "===startRecord===" + audioRecord.getState());
        audioRecord.startRecording();

        String currentFileName = fileName;
        if (status == Status.STATUS_PAUSE) {
            //假如是暂停录音 将文件名后面加个数字,防止重名文件内容被覆盖
            currentFileName += filesName.size();

        }
        filesName.add(currentFileName);

        final String finalFileName=currentFileName;
        //将录音状态设置成正在录音状态
        status = Status.STATUS_START;

        //使用线程池管理线程
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                writeDataTOFile(finalFileName);
            }
        });
    }

    /**
     * 暂停录音
     */
    public void pauseRecord() {
        Log.d("AudioRecorder", "===pauseRecord===");
        if (status != Status.STATUS_START) {
            throw new IllegalStateException("没有在录音");
        } else {
            audioRecord.stop();
            status = Status.STATUS_PAUSE;
        }
    }

    /**
     * 停止录音
     */
    public void stopRecord() {
        Log.d("AudioRecorder", "===stopRecord===");
        if (status == Status.STATUS_NO_READY || status == Status.STATUS_READY) {
            Toast.makeText(context,"录音尚未开始",Toast.LENGTH_SHORT).show();
        } else {
            audioRecord.stop();
            status = Status.STATUS_STOP;
            release();
        }
    }

    /**
     * 释放资源
     */
    private void release() {
        Log.d("AudioRecorder", "===release===");
        //假如有暂停录音
        try {
            if (filesName.size() > 0) {
                List<String> filePaths = new ArrayList<>();
                for (String fileName : filesName) {
                    filePaths.add(FileUtils.getPcmFileAbsolutePath(fileName));
                }
                //清除
                filesName.clear();
                //将多个pcm文件转化为wav文件
                mergePCMFilesToWAVFile(filePaths);

            } else {
                //这里由于只要录音过filesName.size都会大于0,没录音时fileName为null
                //会报空指针 NullPointerException
                // 将单个pcm文件转化为wav文件
                //Log.d("AudioRecorder", "=====makePCMFileToWAVFile======");
                //makePCMFileToWAVFile();
            }
        } catch (IllegalStateException e) {
            throw new IllegalStateException(e.getMessage());
        }

        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }

        status = Status.STATUS_NO_READY;
    }

    /**
     *来电停止录音
     */
    public void callPhone(){
        pauseRecord();
    }

    /**
     * 取消录音
     */
    public void canel() {
        filesName.clear();
        fileName = null;
        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }

        status = Status.STATUS_NO_READY;
    }


    /**
     * 将音频信息写入文件
     *
     */
    private void writeDataTOFile(String currentFileName) {
        // new一个byte数组用来存一些字节数据，大小为缓冲区大小
        byte[] audiodata = new byte[bufferSizeInBytes];

        FileOutputStream fos = null;
        int readsize = 0;
        try {
            File file = new File(FileUtils.getPcmFileAbsolutePath(currentFileName));
            if (file.exists()) {
                file.delete();
            }
            fos = new FileOutputStream(file);// 建立一个可存取字节的文件
        } catch (IllegalStateException e) {
            Log.e("AudioRecorder", e.getMessage());
            throw new IllegalStateException(e.getMessage());
        } catch (FileNotFoundException e) {
            Log.e("AudioRecorder", e.getMessage());

        }
        while (status == Status.STATUS_START) {
            readsize = audioRecord.read(audiodata, 0, bufferSizeInBytes);
            if (AudioRecord.ERROR_INVALID_OPERATION != readsize && fos != null) {
                try {
                    fos.write(audiodata);
                    if (listener != null) {
                        //用于拓展业务
                        listener.recordOfByte(audiodata, 0, audiodata.length);
                    }
                } catch (IOException e) {
                    Log.e("AudioRecorder", e.getMessage());
                }
            }
        }
//        if (listener != null) {
//            listener.finishRecord();
//        }
        try {
            if (fos != null) {
                fos.close();// 关闭写入流
            }
        } catch (IOException e) {
            Log.e("AudioRecorder", e.getMessage());
        }
    }

    /**
     * 将pcm合并成wav
     *
     */
    private void mergePCMFilesToWAVFile(final List<String> filePaths) {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                if (PcmToWav.mergePCMFilesToWAVFile(filePaths, FileUtils.getWavFileAbsolutePath(fileName))) {
                    //操作成功
                    Log.e("AudioRecorder", "成功");
                } else {
                    //操作失败
                    Log.e("AudioRecorder", "mergePCMFilesToWAVFile fail");
                    throw new IllegalStateException("mergePCMFilesToWAVFile fail");
                }
            }
        });
    }

    /**
     * 将单个pcm文件转化为wav文件
     */
    private void makePCMFileToWAVFile() {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                if (PcmToWav.makePCMFileToWAVFile(FileUtils.getPcmFileAbsolutePath(fileName), FileUtils.getWavFileAbsolutePath(fileName), true)) {
                    //操作成功
                } else {
                    //操作失败
                    Log.e("AudioRecorder", "makePCMFileToWAVFile fail");
                    throw new IllegalStateException("makePCMFileToWAVFile fail");
                }
            }
        });
    }


    /**
     * 录音对象的状态
     */
    public enum Status {
        //未开始
        STATUS_NO_READY,
        //预备
        STATUS_READY,
        //录音
        STATUS_START,
        //暂停
        STATUS_PAUSE,
        //停止
        STATUS_STOP
    }

    /**
     * 获取录音对象的状态
     *
     */
    public Status getStatus() {
        return status;
    }

    /**
     * 获取本次录音文件的个数
     *
     */
    public int getPcmFilesCount() {
        return filesName.size();
    }


    public RecordStreamListener getListener() {
        return listener;
    }

    public void setListener(RecordStreamListener listener) {
        this.listener = listener;
    }

}
