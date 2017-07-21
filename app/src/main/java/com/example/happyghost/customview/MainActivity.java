package com.example.happyghost.customview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.example.happyghost.customview.widget.RefreshView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SeekBar mSeekBar;
    private Button mBtMost;
    private Button mBtarc;
    private Button mBtSuccess;
    private Button mBtErroe;
    private RefreshView mRv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initView() {
        mRv = (RefreshView) findViewById(R.id.rv);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mBtMost = (Button) findViewById(R.id.button);
        mBtarc = (Button) findViewById(R.id.button2);
        mBtSuccess = (Button) findViewById(R.id.button3);
        mBtErroe = (Button) findViewById(R.id.button4);

        mBtMost.setOnClickListener(this);
        mBtarc.setOnClickListener(this);
        mBtSuccess.setOnClickListener(this);
        mBtErroe.setOnClickListener(this);
    }

    private void initData() {
        mSeekBar.setMax(360);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRv.startSweepAngleAnimation(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button:
                mRv.setCurrrentState(1);
                break;
            case R.id.button2:
                mRv.setCurrrentState(2);
                mRv.startArcAnimation();
                break;
            case R.id.button3:
                mRv.setCurrrentState(3);
                mRv.setCurrentLoadState(4);
                break;
            case R.id.button4:
                mRv.setCurrrentState(3);
                mRv.setCurrentLoadState(5);
                break;

        }
    }
}
