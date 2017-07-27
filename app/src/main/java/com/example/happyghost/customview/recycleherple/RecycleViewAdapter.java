package com.example.happyghost.customview.recycleherple;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.happyghost.customview.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhao Chenping
 * @creat 2017/7/24.
 * @description
 */

public class RecycleViewAdapter<T> extends RecyclerView.Adapter<RecycleViewHolder>{
    private final Context mContext;
    private List<T> mList;

    public RecycleViewAdapter(Context context, List<T> list) {
        this.mContext = context;
        if(mList==null){
            mList=new ArrayList<>();
        }
        this.mList = list;
    }

    @Override
    public RecycleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_recycle_view, parent, false);
        return new RecycleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecycleViewHolder holder, int position) {
        holder.mTvItem.setText("小小攻城狮"+mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
