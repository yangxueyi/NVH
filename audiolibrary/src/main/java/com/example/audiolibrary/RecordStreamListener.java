package com.example.audiolibrary;

/**
 * Created by YangXueYi
 * Time： 2018/1/5.
 * 获取录音的音频流,用于拓展的处理
 */
public interface RecordStreamListener {
    void recordOfByte(byte[] data, int begin, int end);
}
