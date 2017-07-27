package com.example.happyghost.customview.recycleherple;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @author Zhao Chenping
 * @creat 2017/7/24.
 * @description
 */

public class RecycleViewItemDecoration extends RecyclerView.ItemDecoration{
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(0,1,0,1);
    }
}
