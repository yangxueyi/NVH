package com.example.zhang.nvh.modlue.bluetooth.adapter;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.zhang.nvh.R;
import com.example.zhang.nvh.listener.MyItemOnClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by yang
 * Time 2017/8/31.
 */

public class PairAdapter extends RecyclerView.Adapter<PairAdapter.MyViewHolder> {

    List<BluetoothDevice> list;
    private MyItemOnClickListener mMyItemOnClickListener;

    public PairAdapter() {
        this.list = new ArrayList<>();
    }

    //添加蓝牙设备
    public void addDevice(BluetoothDevice device) {
        if(!list.contains(device)) {
            list.add(device);
        }
    }
    //拿到蓝牙设备
    public BluetoothDevice getDevice(int position) {
        return list.get(position);
    }


    //清空列表
    public void clearDevic(){
        list.clear();
    }



    //添加条目点击事件，在Adapter中对外暴露方法
    public void setItemOnClickListener(MyItemOnClickListener listener){
        mMyItemOnClickListener=listener;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blue, parent, false);

//        View view = View.inflate(parent.getContext(), R.layout.item_blue,parent);
        return new MyViewHolder(view,mMyItemOnClickListener);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        //获取蓝牙的类型
        int deviceType = list.get(position).getBluetoothClass().getMajorDeviceClass();
        if(deviceType == 256){//电脑
            holder.iv_icon.setImageResource(R.mipmap.pc);
        }else if(deviceType == 512){//手机
            holder.iv_icon.setImageResource(R.mipmap.phone);
        }else if (deviceType == 0x0700){//可穿戴设备
            holder.iv_icon.setImageResource(R.mipmap.wearable);
        }else{
            holder.iv_icon.setImageResource(R.mipmap.setting_bluetooth);
        }

        holder.tv_name.setText(list.get(position).getName());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends ViewHolder implements View.OnClickListener{

        private final ImageView iv_icon;
        private final TextView tv_name;


        MyItemOnClickListener mListener;

         MyViewHolder(View itemView,MyItemOnClickListener myItemOnClickListener) {
            super(itemView);
            iv_icon = (ImageView) itemView.findViewById(R.id.iv_icon);
            tv_name = (TextView) itemView.findViewById(R.id.tv_name);

             this.mListener = myItemOnClickListener;
             //设置条目的点击事件
             itemView.setOnClickListener(this);
        }

        //设置条目的点击事件
        @Override
        public void onClick(View v) {
            if(mListener!=null){
                try {
                    mListener.onItemOnClick(v,getAdapterPosition(),getItemCount());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
