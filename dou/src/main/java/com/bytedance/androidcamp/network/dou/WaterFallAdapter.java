package com.bytedance.androidcamp.network.dou;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bytedance.androidcamp.network.dou.model.Video;
import com.bytedance.androidcamp.network.lib.util.ImageHelper;

import java.util.List;

public class WaterFallAdapter extends RecyclerView.Adapter<WaterFallAdapter.MyViewHolder> {

    private Context mContext;
    private List<Video> mData; //定义数据源

    //定义构造方法，默认传入上下文和数据源
    public WaterFallAdapter(Context context, List<Video> data) {
        mContext = context;
        mData = data;
    }

    @Override  //将ItemView渲染进来，创建ViewHolder
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.video_item_view, parent,false);
        return new MyViewHolder(view);
    }

    @Override  //将数据源的数据绑定到相应控件上
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final Video video = mData.get(position);
        holder.bind((Activity)mContext, video);

    }

    @Override
    public int getItemCount() {
            return mData.size();
    }

    //定义自己的ViewHolder，将View的控件引用在成员变量上
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView img;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img);
        }

        public void bind(final Activity activity, final Video video) {
            ImageHelper.displayWebImage(video.getImageUrl(), img);
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VideoActivity.launch(activity, video.getVideoUrl());
                }
            });
        }
    }
}