package com.example.zhang.nvh.modlue.uploading.contract;

import com.example.zhang.nvh.base.contract.BaseContract;

import java.util.List;

/**
 * Created by yang
 * Time 2017/8/28.
 */

public interface FileUploadingContract {


    interface GetFileCallback{
        void onNetworkError();
        void onSuccess(List<?> list,List<?> list1);
        void onError();

    }

    interface Model {
        void onGetFileResponse(GetFileCallback getFileCallback);//显示已存在的录音文件
        List initList();
        List initList1();
    }

    interface View extends BaseContract.View{
        void deleteFile();
        void uploadingFile();
        void deriveFile();
        void getDataList(List<String> datas,List<String> list);
        void ItemClick(int postion);
        void ItemLongClick();
        void getListPostion(int postion);
    }

    interface Presenter {
        void onDeleteFile();
        void onUploadingFile();
        void onDeriveFile();
        void onItemClick();
        void onLoadDatas();
        void playRecord(int postion);//播放录音
    }
}
