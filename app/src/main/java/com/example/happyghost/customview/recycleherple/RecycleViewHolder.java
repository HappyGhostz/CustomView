package com.example.happyghost.customview.recycleherple;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.happyghost.customview.R;

/**
 * @author Zhao Chenping
 * @creat 2017/7/24.
 * @description
 */

public class RecycleViewHolder extends RecyclerView.ViewHolder{
    private final View mConvertView;
    public final TextView mTvItem;

    public RecycleViewHolder(View itemView) {
        super(itemView);
        this.mConvertView = itemView;
        mTvItem = (TextView) itemView.findViewById(R.id.tv_item);
    }
    public View creatView(){

        return mConvertView;
    }

}
