package com.example.happyghost.customview;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.example.happyghost.customview.recycleherple.RecycleViewAdapter;
import com.example.happyghost.customview.recycleherple.RecycleViewItemDecoration;
import com.example.happyghost.customview.widget.RefreshView;
import com.example.happyghost.customview.widget.RefreshViewLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity  {
    private RecyclerView mRv;
    private List<Integer> mList;
    private ArrayList<Integer> mAddList;
    private ArrayList<Integer> mAddFooterList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            mList.add(i);
        }
        mRv = (RecyclerView) findViewById(R.id.rv);
        RefreshViewLayout rl = (RefreshViewLayout) findViewById(R.id.rl);
        final RecycleViewAdapter<Integer> adapter = new RecycleViewAdapter<>(this, mList);
        mRv.setLayoutManager(new LinearLayoutManager(this));
        mRv.addItemDecoration(new RecycleViewItemDecoration());
        mRv.setAdapter(adapter);
        rl.setOnPullRefreshListener(new RefreshViewLayout.OnPullRefreshListener() {
            @Override
            public void onRefresh() {
                mAddList = new ArrayList<>();
                for (int i = 200; i < 210; i++) {
                    mAddList.add(i);
                }
                mList.addAll(0,mAddList);
                adapter.notifyDataSetChanged();
            }
        });
        rl.setOnPushLoadMoreListener(new RefreshViewLayout.OnPushLoadMoreListener() {
            @Override
            public void onLoadMore() {
                mAddFooterList = new ArrayList<>();
                for (int i = 3000; i < 3010; i++) {
                    mAddFooterList.add(i);
                }
                mList.addAll(mList.size(),mAddFooterList);
                adapter.notifyDataSetChanged();
            }
        });
    }
}
