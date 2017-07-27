package com.example.happyghost.customview.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;

import com.example.happyghost.customview.R;

/**
 * @author Zhao Chenping
 * @creat 2017/7/20.
 * @description
 */

public class RefreshView extends View{

    public  static final int PULL_STATE_START =0;
    public  static final int PULL_MOST_STATE_CRICLE =1;
    public  static final int RELEASE_STATE_ARC =2;
    public  static final int RELEASE_STATE_END =3;
    public  static final int LOAD_SUCCESS =4;
    public  static final int LOAD_ERROR =5;

    public  int mCurrentState = PULL_MOST_STATE_CRICLE;
    public  int mCurrentLoadState = LOAD_SUCCESS;


    private int mDefaultSize;
    private Paint mCriclePaint;
    private Paint mArcPaint;
    private int mWidth;
    private int mHeight;
    private int mRadius;
    private float mSweepAngle;
    private float mArcSweep;
    private boolean progressReverse;
    private int mProgressStartAngel;
    private Drawable mSuccess;
    private Drawable mError;
    private ObjectAnimator mArcAnimator;
    private ValueAnimator alphAnimation;
    private Animation.AnimationListener mListener;
    private float mStartAngle;

    public RefreshView(Context context) {
        this(context,null);
    }

    public RefreshView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,-1);
    }

    public RefreshView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context,attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        mCriclePaint = new Paint();
        mCriclePaint.setAntiAlias(true);
        mCriclePaint.setColor(Color.parseColor("#00AFFE"));

        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setColor(Color.RED);
        mArcPaint.setStrokeWidth(3);
        mArcPaint.setStyle(Paint.Style.STROKE);

        mSuccess = context.getResources().getDrawable(R.mipmap.yes);
        mError = context.getResources().getDrawable(R.mipmap.no);

        mCurrentState = PULL_STATE_START;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        mDefaultSize = 50;

        if(widthMode==MeasureSpec.AT_MOST&&heightMode==MeasureSpec.AT_MOST){
            setMeasuredDimension(mDefaultSize,mDefaultSize);
        }else if(widthMode==MeasureSpec.AT_MOST){
            setMeasuredDimension(mDefaultSize,heightSize);
        }else if(heightMode==MeasureSpec.AT_MOST){
            setMeasuredDimension(widthSize,mDefaultSize);
        }else{
            setMeasuredDimension(widthSize,heightSize);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        mRadius = Math.min(mWidth,mHeight)/2;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawCricle(canvas);
        if(mCurrentState==PULL_MOST_STATE_CRICLE){
            drawMostArc(canvas);
        }else if(mCurrentState==RELEASE_STATE_ARC){
            drawArc(canvas);
        }else if(mCurrentState==RELEASE_STATE_END){
            drawDrawable(canvas);
        }
    }

    private void drawCricle(Canvas canvas) {
        RectF rectF = new RectF();
        rectF.set(mWidth/2-mRadius,mHeight/2-mRadius,mWidth/2+mRadius,mHeight/2+mRadius );
        canvas.drawArc(rectF,mStartAngle,mSweepAngle,true,mCriclePaint);
    }

    private void drawMostArc(Canvas canvas) {
        RectF progressRect = new RectF();
//        mWidth*3/4-mRadius,mHeight*3/4-mRadius,mWidth/4+mRadius,mHeight/4+mRadius
        progressRect.set(mWidth/2-mRadius/2,mHeight/2-mRadius/2,mWidth/2+mRadius/2,mHeight/2+mRadius/2);
        canvas.drawArc(progressRect,0,270, false,mArcPaint);
    }

    private void drawArc(Canvas canvas) {
        RectF progressRect = new RectF();
        progressRect.set(mWidth/2-mRadius/2,mHeight/2-mRadius/2,mWidth/2+mRadius/2,mHeight/2+mRadius/2);
        if ( mArcSweep != 360 ) {
        mProgressStartAngel = progressReverse ? 270 : (int) (270 + mArcSweep);
            canvas.drawArc(progressRect
                ,mProgressStartAngel,progressReverse ? mArcSweep : (int) (360 - mArcSweep),
                false,mArcPaint);
        }
    }

    private void drawDrawable(Canvas canvas) {
        if(mCurrentLoadState==LOAD_SUCCESS){
            mSuccess.setBounds(mWidth*3/4-mRadius,mHeight*3/4-mRadius,mWidth/4+mRadius,mHeight/4+mRadius);
            mSuccess.draw(canvas);
        }else{
            mError.setBounds(mWidth*3/4-mRadius,mHeight*3/4-mRadius,mWidth/4+mRadius,mHeight/4+mRadius);
            mError.draw(canvas);
        }
    }

    public void startSweepAngleAnimation(float startAngle,float sweepAngle){
        this.mStartAngle = startAngle;
        this.mSweepAngle=sweepAngle;
        float alpha = sweepAngle / 360;
        this.setAlpha(alpha);
        setCurrrentState(0);
        invalidate();
    }
    public void setCurrrentState(int state){
        mCurrentState=state;
        invalidate();
    }
    public void setCurrentLoadState(int state){
        mCurrentLoadState=state;
        invalidate();
    }

    public void startArcAnimation(){
        if(mArcAnimator==null){
            mArcAnimator = ObjectAnimator.ofFloat(this,"mArcSweep",0,359);
        }
        mArcAnimator.setDuration(1000);
        mArcAnimator.setRepeatMode(ValueAnimator.RESTART);
        mArcAnimator.setRepeatCount(ValueAnimator.INFINITE);

        mArcAnimator.removeAllListeners();

        mArcAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
//                Log.d(TAG,"onAnimationRepeat:"+progressReverse);
                progressReverse = !progressReverse;
            }
        });
        mArcAnimator.start();
        mCurrentState = RELEASE_STATE_ARC;
    }
    public void setMArcSweep(float arcSweep){
        this.mArcSweep=arcSweep;
        invalidate();
    }
    public void setAnimationListener(Animation.AnimationListener listener) {
        mListener = listener;
    }

    @Override
    public void onAnimationStart() {
        super.onAnimationStart();
        if (mListener != null) {
            mListener.onAnimationStart(getAnimation());
        }
    }

    @Override
    public void onAnimationEnd() {
        super.onAnimationEnd();
        if (mListener != null) {
            mListener.onAnimationEnd(getAnimation());
        }
    }
}
