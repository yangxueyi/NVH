package com.example.zhang.nvh.modlue.setting.contract;

import com.example.zhang.nvh.base.contract.BaseContract;

import java.util.List;

/**
 * Created by yang
 * Time 2017/8/24.
 */

public interface SettingContract {
    interface Model extends BaseContract.Model {

    }

    interface View extends BaseContract.View{
       void startSound();
       void stopSound();
    }

    interface Presenter  extends BaseContract.Presenter{

        void onClickStart();
        void onClickStop();
        void onClickManage(int id);
        void onClickSyn();
        void onItemClick(int id);
        void startRecord();//开始录音
        void pauseRecord();//暂停录音
        void stopRecord();//结束录音
        void setPath();//创建音频文件临时保存路径

    }
}
