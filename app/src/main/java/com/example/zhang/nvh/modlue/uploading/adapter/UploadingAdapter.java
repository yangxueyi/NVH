package com.example.zhang.nvh.modlue.uploading.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.zhang.nvh.R;
import com.example.zhang.nvh.listener.MyItemOnClickListener;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by yang
 * Time 2017/9/11.
 */

public class UploadingAdapter extends Adapter<UploadingAdapter.MyUploadingHolder>{

    private Context context;
    private List<String> list;
    private List<String> list1;
    private List<String> deleteList;
    private List<String> deleteList1;
    private List<Integer> listCb = new ArrayList<>();//用于记录位置
    private MyItemOnClickListener mMyItemOnClickListener;
    //是否显示单选框,默认false
    private boolean isshowBox = false;

    private int isClick = 1;


    public UploadingAdapter(Context context){
        this.context = context;
        list = new ArrayList<>();//列表集合
        list1 = new ArrayList<>();//文件集合
        deleteList = new ArrayList<>();//要删除本地文件集合
        deleteList1 = new ArrayList<>();//要删除列表集合
    }

    /**填充adapter*/
    public void setData( List<String> datas,List<String> listDatas ){
        if(datas!=null&&datas.size()>0){
            list.clear();
            list.addAll(datas);
            list1.addAll(listDatas);
            notifyDataSetChanged();
        }

        Log.e("setData: ", "list1 = "+list1.size());
        Log.e("setData: ", "list = "+list.size());
    }

    //
    public void setListPostion(int postion){
        listCb.add(postion);
        notifyDataSetChanged();
    }

    /**删除所选中的条目*/
    public void removeItem(){

        //删除列表中的条目
        list.removeAll(deleteList1);
        //删除的同时，清空记录的位置list
        listCb.clear();
        //设置CheckBox隐藏
        isshowBox = false;
        //删除本地文件
        if(deleteList!=null&&deleteList.size()>0){
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyVoice";
            // 判断SD卡是否存在
            // 根据后缀名进行判断、获取文件夹中的音频文件
            File file = new File(path);
            File files[] = file.listFiles();
            if (files != null) {
                for (File file1 : files) {
                    String name = file1.getName();
                    if (deleteList.contains(name)){
                        File file2 = new File(path+"/"+name);
                        if (file2.exists()) {
                            file2.delete();
                            Log.e( "removeItem: ","name = "+name );
                        }
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    //添加条目点击事件，在Adapter中对外暴露方法
    public void setItemOnClickListener(MyItemOnClickListener listener){
        mMyItemOnClickListener=listener;
    }

    @Override
    public MyUploadingHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_uploading, parent, false);
        return new MyUploadingHolder(view,mMyItemOnClickListener);
    }

    @Override
    public void onBindViewHolder(final MyUploadingHolder holder, @SuppressLint("RecyclerView") final int position) {

        //强制关闭复用
//        holder.setIsRecyclable(false);
        String str = list.get(position);
        String name = list1.get(position);
        final String[] split = str.split(";");
        final String[] split1 = name.split(";");
        holder.tv_uploading.setText(split[0]);

        String s = split[1];
        float num = Float.parseFloat(s);

        //判断文件大小
        if (num  > 1024f){
            float num1 = (float)(Math.round(num)/1024.0);
            BigDecimal d = new BigDecimal(num);//设置保留两位小数，并四舍五入
            double fileNumber = d.setScale(2, RoundingMode.HALF_UP).doubleValue();
            holder.tv_size.setText("文件大小:"+fileNumber + "MB");

        }else{
            BigDecimal d = new BigDecimal(num);//设置保留两位小数，并四舍五入
            double fileNumber = d.setScale(2, RoundingMode.HALF_UP).doubleValue();
            holder.tv_size.setText("文件大小:"+fileNumber + "KB");
        }

        //长按显示/隐藏
        if (isshowBox) {
            holder.check_box.setVisibility(View.VISIBLE);
            isClick = 2;
        } else {
            holder.check_box.setVisibility(View.INVISIBLE);
            isClick = 1;
        }

        //在初始化CheckBox状态和设置状态变化监听事件之前，先把状态变化监听事件设置为null
        holder.check_box.setOnCheckedChangeListener(null);
        //根据记录的位置设置CheckBox状态
        if (listCb != null) {
            holder.check_box.setChecked(listCb.contains(position));
        } else {
            holder.check_box.setChecked(false);
        }
        //checkbox点击时的变化
        holder.check_box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!listCb.contains(position)) {
                        listCb.add(position);//选中时添加
                    }
                    deleteList.add(split1[0]);
                    deleteList1.add(list.get(position));

                } else {
                    if(listCb != null && listCb.size() > 0) {
                        if(listCb.contains(position)){
                            int i = listCb.indexOf(position);
                            listCb.remove(i);//没选中时移除
                            deleteList.remove(i);//没选中时移除
                            deleteList1.remove(i);//没选中时移除
                        }
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    //设置是否显示CheckBox
    public void setShowBox() {
        //取反
        isshowBox = !isshowBox;
    }

    class MyUploadingHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {

        private final TextView tv_uploading;
        private final TextView tv_size;
        private final CheckBox check_box;
        MyItemOnClickListener mListener;

        MyUploadingHolder(View itemView,MyItemOnClickListener myItemOnClickListener) {
            super(itemView);
            tv_uploading = (TextView) itemView.findViewById(R.id.tv_uploading);
            tv_size = (TextView) itemView.findViewById(R.id.tv_size);
            check_box = (CheckBox) itemView.findViewById(R.id.check_box);

            this.mListener = myItemOnClickListener;
            //设置条目的点击事件
            itemView.setOnClickListener(this);
            //设置条目的长按事件
            itemView.setOnLongClickListener(this);
        }

        //设置条目的点击事件
        @Override
        public void onClick(View v) {
            if(mListener!=null){
                try {
                    mListener.onItemOnClick(v,getAdapterPosition(),isClick);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if(mListener!=null){
                try {
                    mListener.onItemOnLongClick(v,getAdapterPosition());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
    }
}
