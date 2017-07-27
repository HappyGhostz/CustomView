package com.example.happyghost.customview.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import static android.support.v4.widget.ViewDragHelper.INVALID_POINTER;

/**
 * @author Zhao Chenping
 * @creat 2017/7/22.
 * @description
 */

public class RefreshViewLayout extends ViewGroup {

    private static final float DRAG_RATE = .5f;
    private static final float HEADER_VIEW_HEIGHT = 20;
    private static final float DEFAULT_CIRCLE_TARGET = 64;
    private static final float CIRCLE_DIAMETER = 40;
    private static final int ANIMATE_TO_START_DURATION = 200;
    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;
    private NestedScrollingParentHelper mNestedScrollingParentHelper;
    private NestedScrollingChildHelper mNestedScrollingChildHelper;
    private View mTarget;
    private int mHeaderViewIndex=-1;
    private int mFooterViewIndex=-1;
    private int mCurrentTargetOffsetTop ;
    private boolean targetScrollWithLayout=true ;
    private boolean mReturningToStart=true;
    private boolean mRefreshing=false;
    private boolean mLoadMore=false;
    private boolean mIsBeingDragged;
    private RecyclerView.LayoutManager layoutManager;
    private LinearLayoutManager layoutManager1;
    private int mActivePointerId;
    private float mInitialMotionY;
    private int mTouchSlop;
    private int mPushDistance=0;
    private int mHeaderViewWidth;
    private int mFooterViewWidth;
    private int mHeaderViewHeight;
    private int mFooterViewHeight;
    private int mDownY;
    private int mSpinnerOffsetEnd;
    private int mTotalDragDistance=-1;
    private boolean mUsingCustomStart;
    private int mOriginalOffsetTop;
    private float mSpinnerFinalOffset;
    private boolean mOriginalOffsetCalculated=false;
    private int mCircleDiameter;
    private int mFrom;
    private View mShadeView;
    private View mHeaderView;
    private View mFooterView;
    private View mShadeOrFooterView;
    private int offset;
    private boolean mNotify;
    private RefreshView view;
    private boolean mSuccessState=true;
    private int width;
    private int height;
    private DecelerateInterpolator mDecelerateInterpolator;
    private boolean mFootViewSccessState=true;
    private OnPullRefreshListener mListener;
    private OnPushLoadMoreListener mOnPushLoadMoreListener;
    private int mShadeHeaderViewIndex;
    private int mShadeFooterViewIndex;

    public RefreshViewLayout(Context context) {
        this(context,null);
    }

    public RefreshViewLayout(Context context, AttributeSet attrs) {
        this(context, attrs,-1);
    }

    public RefreshViewLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        /**
         * getScaledTouchSlop是一个距离，表示滑动的时候，手的移动要大于这个距离才开始移动控件。如果小于这个距离就不触发移动控件
         */
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setWillNotDraw(false);
        mDecelerateInterpolator = new DecelerateInterpolator(
                DECELERATE_INTERPOLATION_FACTOR);
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        mHeaderViewWidth = (int) display.getWidth();
        mFooterViewWidth = (int) display.getWidth();
        mHeaderViewHeight = (int) (HEADER_VIEW_HEIGHT * metrics.density);
        mFooterViewHeight = (int) (HEADER_VIEW_HEIGHT * metrics.density);
        mCircleDiameter = (int) (CIRCLE_DIAMETER * metrics.density);
        createDefaultView();
        createFooterView();
        createShadeViewOrHeaderView();
        createShadeViewOrFooterView();

//        ViewCompat.setChildrenDrawingOrderEnabled(this, true);
        setChildrenDrawingOrderEnabled(true);
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);

        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        mSpinnerOffsetEnd = (int) (DEFAULT_CIRCLE_TARGET * metrics.density);
        mSpinnerFinalOffset = DEFAULT_CIRCLE_TARGET * metrics.density;
        mTotalDragDistance = mSpinnerOffsetEnd;
//        setNestedScrollingEnabled(true);
//        moveToStart(1.0f);
    }

    private void createShadeViewOrFooterView() {
        mShadeOrFooterView = setShadeOrFooterView();
        addView(mShadeOrFooterView);
    }

    private View setShadeOrFooterView() {
        RelativeLayout mShadeRlView = new RelativeLayout(getContext());
        mShadeRlView.setBackgroundColor(Color.RED);
        return mShadeRlView;
    }

    private void createShadeViewOrHeaderView() {
        mShadeView = setShadeOrHeaderView();
        addView(mShadeView);
    }

    private View setShadeOrHeaderView() {
        RelativeLayout mShadeRlView = new RelativeLayout(getContext());
        mShadeRlView.setBackgroundColor(Color.RED);
        return mShadeRlView;
    }

    private void createDefaultView() {
        mHeaderView = setHeaderView();
        addView(mHeaderView);
    }

    public View setHeaderView() {
        view = new RefreshView(getContext());
        view.setVisibility(VISIBLE);
//        view.startSweepAngleAnimation(360);
        return view;
    }

    private void createFooterView() {
        mFooterView = setFooterView();
        addView(mFooterView);
    }
    public View setFooterView(){
        RefreshView view = new RefreshView(getContext());
        view.setVisibility(VISIBLE);
//        view.startSweepAngleAnimation(360);
//        view.setCurrrentState(2);
//        view.startArcAnimation();
//        view.startArcAnimation();
        return view;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mTarget == null) {
            ensureTarget();
        }
        if (mTarget == null) {
            return;
        }
        mTarget.measure(MeasureSpec.makeMeasureSpec(
                getMeasuredWidth() - getPaddingLeft() - getPaddingRight(),
                MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
                getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY));
        mHeaderView.measure(MeasureSpec.makeMeasureSpec(mHeaderViewWidth, MeasureSpec.EXACTLY), MeasureSpec
                .makeMeasureSpec(3 * mHeaderViewHeight, MeasureSpec.EXACTLY));
        mFooterView.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(3 * mHeaderViewHeight, MeasureSpec.EXACTLY));
        mShadeOrFooterView.measure(MeasureSpec.makeMeasureSpec(mHeaderViewWidth, MeasureSpec.EXACTLY), MeasureSpec
                .makeMeasureSpec(3 * mHeaderViewHeight, MeasureSpec.EXACTLY));
        mShadeView.measure(MeasureSpec.makeMeasureSpec(mHeaderViewWidth, MeasureSpec.EXACTLY), MeasureSpec
                .makeMeasureSpec(3 * mHeaderViewHeight, MeasureSpec.EXACTLY));

        if (!mUsingCustomStart && !mOriginalOffsetCalculated) {
            mOriginalOffsetCalculated = true;
            int measuredHeight = mShadeView.getMeasuredHeight();
            mCurrentTargetOffsetTop = mOriginalOffsetTop = -mShadeView.getMeasuredHeight();
//            updateListenerCallBack();
        }

        mHeaderViewIndex = -1;
        // 得到头部Index
        for (int index = 0; index < getChildCount(); index++) {
            if (getChildAt(index) == mHeaderView) {
                mHeaderViewIndex = index;
                break;
            }
        }
        // 得到尾部Index
        mFooterViewIndex =-1;
        for (int index = 0; index < getChildCount(); index++) {
            if (getChildAt(index) == mFooterView) {
                mFooterViewIndex = index;
                break;
            }
        }
        mShadeHeaderViewIndex = -1;
        // 得到头部Index
        for (int index = 0; index < getChildCount(); index++) {
            if (getChildAt(index) == mShadeView) {
                mShadeHeaderViewIndex = index;
                break;
            }
        }
        // 得到尾部Index
        mShadeFooterViewIndex =-1;
        for (int index = 0; index < getChildCount(); index++) {
            if (getChildAt(index) == mShadeOrFooterView) {
                mShadeFooterViewIndex = index;
                break;
            }
        }
    }

    private void ensureTarget() {
        if(mTarget==null){
            for (int i=0;i<getChildCount();i++){
                View childAt = getChildAt(i);
                if(!childAt.equals(mFooterView)&&!childAt.equals(mHeaderView)){
                    mTarget=childAt;
                }
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        if(getChildCount()==0){
            return;
        }
        if(mTarget==null){
            ensureTarget();
        }
        if(mTarget==null){
            return;
        }

        int distance = mCurrentTargetOffsetTop +mShadeView.getMeasuredHeight();
        if (!targetScrollWithLayout) {
            // 判断标志位，如果目标View不跟随手指的滑动而滑动，将下拉偏移量设置为0
            distance = 0;
        }
        final View child = mTarget;
        final int childLeft = getPaddingLeft();
        final int mHeaderTop = getPaddingTop();
        final  int childTop = getPaddingTop() + distance - mPushDistance;// 根据偏移量distance更新
        // 根据偏移量distance更新
        final int childWidth = width - getPaddingLeft() - getPaddingRight();
        final int childHeight = height - getPaddingTop() - getPaddingBottom();
        // 更新目标View的位置
        child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        int headViewWidth = mHeaderView.getMeasuredWidth();
        int headViewHeight = mHeaderView.getMeasuredHeight();
        // 更新头布局的位置
        if(mCurrentTargetOffsetTop<=5){
            mHeaderView.layout(childLeft, mCurrentTargetOffsetTop, childLeft+headViewWidth, mCurrentTargetOffsetTop + headViewHeight);
        }
        int footViewWidth = mFooterView.getMeasuredWidth();
        int footViewHeight = mFooterView.getMeasuredHeight();
        if(mPushDistance<=footViewHeight+2){
            mFooterView.layout(childLeft, height - mPushDistance, childLeft+footViewWidth, height + footViewHeight- mPushDistance);
        }


        mShadeView.layout(childLeft, mCurrentTargetOffsetTop, childLeft+headViewWidth, mCurrentTargetOffsetTop + headViewHeight);
        mShadeOrFooterView.layout(childLeft, height - mPushDistance, childLeft+footViewWidth, height + footViewHeight - mPushDistance);
    }

//    @Override
//    protected int getChildDrawingOrder(int childCount, int i) {
//        // 将新添加的View,放到最后绘制
//        if (mHeaderViewIndex < 0 && mFooterViewIndex < 0) {
//            return i;
//        }
//        if (i == childCount - 4) {
//            return mHeaderViewIndex;
//        }
//        if (i == childCount - 3) {
//            return mFooterViewIndex;
//        }
//        if(i==childCount-2){
//            return mShadeHeaderViewIndex;
//        }
//        if(i==childCount-1){
//            return mShadeFooterViewIndex;
//        }
//        int bigIndex = mShadeFooterViewIndex > mShadeHeaderViewIndex ? mShadeFooterViewIndex
//                : mShadeHeaderViewIndex;
//        int smallIndex = mShadeFooterViewIndex < mShadeHeaderViewIndex ? mShadeFooterViewIndex
//                : mShadeHeaderViewIndex;
//        if (i >= smallIndex && i < bigIndex - 1) {
//            return i + 4;
//        }
//        if (i >= bigIndex || (i == bigIndex - 1)) {
//            return i + 3;
//        }
//        return i;
//    }

    private boolean isChildScrollToTop() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(mTarget, -1) || mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, -1);
        }
    }

    private boolean isChildScrollToBottom() {
        if(!isChildScrollToTop()){
            return false;
        }
        if(mTarget instanceof  AbsListView){
            AbsListView absListView = (AbsListView) this.mTarget;
            int absListViewCount = absListView.getAdapter().getCount();
            int firstVisiblePosition = absListView.getFirstVisiblePosition();
            if(firstVisiblePosition==0&&absListView.getChildAt(0).getTop()>=getPaddingTop()){
                return false;
            }
            int lastVisiblePosition = absListView.getLastVisiblePosition();
            if(lastVisiblePosition>0&&absListViewCount>0&&lastVisiblePosition==absListViewCount-1){
                return true;
            }
        }else if(mTarget instanceof ScrollView){
            ScrollView scrollView = (ScrollView) this.mTarget;
            View lastView = scrollView.getChildAt(scrollView.getChildCount() - 1);
            if(lastView!=null){
                int distance = lastView.getBottom() - (scrollView.getHeight() + scrollView.getScrollY());
                if(distance==0){
                    return true;
                }
            }
        }else if(mTarget instanceof RecyclerView){
            RecyclerView recyclerView = (RecyclerView) this.mTarget;
            layoutManager = recyclerView.getLayoutManager();
            int itemCount = recyclerView.getAdapter().getItemCount();
            if (layoutManager instanceof LinearLayoutManager&&itemCount>0){
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                if(linearLayoutManager.findLastCompletelyVisibleItemPosition()==itemCount-1){
                    return true;
                }
            }else if (layoutManager instanceof StaggeredGridLayoutManager){
                StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager)layoutManager;
                int[] items = new int[2];
                int[] itemPositions = staggeredGridLayoutManager.findLastCompletelyVisibleItemPositions(items);
                int lastItem = Math.max(itemPositions[0], itemPositions[1]);
                if(lastItem==itemCount-1){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureTarget();

        final int action = MotionEventCompat.getActionMasked(ev);

        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }
        if (!isEnabled()  || (isChildScrollToTop() && !isChildScrollToBottom())) {
            // 如果子View可以滑动，不拦截事件，交给子View处理-下拉刷新
            // 或者子View没有滑动到底部不拦截事件-上拉加载更多|| mReturningToStart || mRefreshing || mLoadMore
            return false;
        }

        // 下拉刷新判断
        switch (action) {
            case MotionEvent.ACTION_DOWN:
//                setTargetOffsetTop(mOriginalOffsetTop - mShadeView.getTop(), true);// 恢复mTarget的初始位置
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragged = false;
                final float initialMotionY = getMotionEventY(ev, mActivePointerId);
                if (initialMotionY == -1) {
                    return false;
                }
                mInitialMotionY = initialMotionY;// 记录按下的位置

            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER) {
                    return false;
                }

                final float y = getMotionEventY(ev, mActivePointerId);
                if (y == -1) {
                    return false;
                }
                float yDiff = 0;
                if (isChildScrollToBottom()) {
                    yDiff = mInitialMotionY - y;// 计算上拉距离
                    if (yDiff > mTouchSlop && !mIsBeingDragged) {// 判断是否下拉的距离足够
                        mIsBeingDragged = true;// 正在上拉
                    }
                } else {
                    yDiff = y - mInitialMotionY;// 计算下拉距离
                    if (yDiff > mTouchSlop && !mIsBeingDragged) {// 判断是否下拉的距离足够
                        mIsBeingDragged = true;// 正在下拉
                    }
                }
                break;

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
        }

        return mIsBeingDragged;// 如果正在拖动，则拦截子View的事件
    }
    private float getMotionEventY(MotionEvent ev, int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(ev,
                activePointerId);
        if (index < 0) {
            return -1;
        }
        return MotionEventCompat.getY(ev, index);
    }
    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = MotionEventCompat.getPointerId(ev,
                    newPointerIndex);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (!isEnabled() || (isChildScrollToTop() && !isChildScrollToBottom())) {
            // 如果子View可以滑动，不拦截事件，交给子View处理
            return false;
        }

        if (isChildScrollToBottom()) {// 上拉加载更多
            return handlerPushTouchEvent(event, action);
        } else {// 下拉刷新
            return handlerPullTouchEvent(event, action);
        }
    }

    private boolean handlerPullTouchEvent(MotionEvent event, int action) {


        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = event.getPointerId(0);
                mIsBeingDragged = false;
                break;
            case MotionEvent.ACTION_MOVE:
                final int pointerIndex = MotionEventCompat.findPointerIndex(event,
                        mActivePointerId);
                if (pointerIndex < 0) {

                    return false;
                }

                final float y = MotionEventCompat.getY(event, pointerIndex);
                final float overscrollTop = (y - mInitialMotionY) * DRAG_RATE;
                if (mIsBeingDragged) {
                    float originalDragPercent = overscrollTop / mTotalDragDistance;
                    if (originalDragPercent < 0) {
                        return false;
                    }
                    float dragPercent = Math.min(1f, Math.abs(originalDragPercent));
                    float extraOS = Math.abs(overscrollTop) - mTotalDragDistance;
                    float slingshotDist = mUsingCustomStart ? mSpinnerFinalOffset
                            - mOriginalOffsetTop : mSpinnerFinalOffset;
                    float tensionSlingshotPercent = Math.max(0,
                            Math.min(extraOS, slingshotDist * 2) / slingshotDist);
                    float tensionPercent = (float) ((tensionSlingshotPercent / 4) - Math
                            .pow((tensionSlingshotPercent / 4), 2)) * 2f;
                    float extraMove = (slingshotDist) * tensionPercent * 2;

                    int targetY = mOriginalOffsetTop
                            + (int) ((slingshotDist * dragPercent) + extraMove);
                    offset = targetY - mCurrentTargetOffsetTop;
                    if(mShadeView.getBottom()>=mHeaderView.getMeasuredHeight()+5){
                        if(overscrollTop<mTotalDragDistance*2){
                            float arc = (360*overscrollTop / mTotalDragDistance)/2+10;
                            System.out.println("arc="+arc);
                            if(arc>360){
                                view.startSweepAngleAnimation(-90,360);
                                view.setCurrrentState(1);
                            }else{
                                ((RefreshView)mHeaderView).startSweepAngleAnimation(-90,arc);
                            }
                        }
                    }
                    setTargetOffsetTopAndBottom(offset, true);
                }
                break;
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int index = MotionEventCompat.getActionIndex(event);
                mActivePointerId = MotionEventCompat.getPointerId(event, index);
                break;
            }

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (mActivePointerId == INVALID_POINTER) {
                    if (action == MotionEvent.ACTION_UP) {
                        return false;
                    }
                }
                final int pointerIndexUp = MotionEventCompat.findPointerIndex(event,
                        mActivePointerId);
                final float yup = MotionEventCompat.getY(event, pointerIndexUp);
                final float overscrollTopUp = (yup - mInitialMotionY) * DRAG_RATE;
                mIsBeingDragged = false;
                if (overscrollTopUp > mTotalDragDistance*2) {
                    setRefreshing(true, true /* notify */);
                } else {
                    mRefreshing = false;
                    animateOffsetToStartPosition(mCurrentTargetOffsetTop);
                }
                mActivePointerId = INVALID_POINTER;
                return false;
            }
        }
        return true;
    }

    private void setRefreshing(boolean refreshing, final boolean notify) {
            ensureTarget();
            mRefreshing = refreshing;
            if (mRefreshing) {
                ((RefreshView)mHeaderView).startArcAnimation();
                resetTargetLayoutDelay(2000);
            }
    }
    public void setSuccessState(boolean state){
        this.mSuccessState =state;
    }
    /**
     * 重置Target位置
     *
     * @param delay
     */
    public void resetTargetLayoutDelay(int delay) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mSuccessState){
                    resetTargetLayout();
                }else{
                    Toast.makeText(getContext(),"刷新出错-o-",Toast.LENGTH_SHORT).show();
                    resetTargetLayout();
                }
                if(mListener!=null){
                    mListener.onRefresh();
                }
            }
        }, delay);
    }
    /**
     * 重置Target的位置
     */
    public void resetTargetLayout() {
        animateOffsetToStartPosition(mFrom);
    }


    private void animateOffsetToStartPosition(int from) {
        mFrom = from;
        Animation mScaleDownToStartAnimation = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {
                moveToStart(interpolatedTime);
            }
        };
        mScaleDownToStartAnimation.setDuration(ANIMATE_TO_START_DURATION);
        mHeaderView.clearAnimation();
        mShadeView.clearAnimation();
        mHeaderView.startAnimation(mScaleDownToStartAnimation);
        mShadeView.startAnimation(mScaleDownToStartAnimation);
    }

    private void moveToStart(float interpolatedTime) {
        int targetTop = 0;
        targetTop = (mFrom + (int) ((mOriginalOffsetTop - mFrom) * interpolatedTime));
        int offset = targetTop - mShadeView.getTop();
        setTargetOffsetTopAndBottom(offset, false /* requires update */);
    }

    private void  setTargetOffsetTopAndBottom(int offset, boolean requiresUpdate) {
        mShadeView.bringToFront();
        ViewCompat.offsetTopAndBottom(mShadeView, offset);
        mCurrentTargetOffsetTop = mShadeView.getTop();
        if (requiresUpdate && android.os.Build.VERSION.SDK_INT < 11) {
            invalidate();
        }
    }


    private boolean handlerPushTouchEvent(MotionEvent event, int action) {
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(event, 0);
                mIsBeingDragged = false;
                break;
            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = MotionEventCompat.findPointerIndex(event,
                        mActivePointerId);
                if (pointerIndex < 0) {
//                    Log.e(LOG_TAG,
//                            "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }
                final float y = MotionEventCompat.getY(event, pointerIndex);
                final float overscrollBottom = (mInitialMotionY - y) * DRAG_RATE;
                if (mIsBeingDragged) {
                    mPushDistance = (int)overscrollBottom;
                    if(mPushDistance>2*mShadeView.getMeasuredHeight()){
                        mPushDistance=2*mShadeView.getMeasuredHeight()+10;
                    }
                    if(mShadeOrFooterView.getTop()<=height-mShadeView.getHeight()){
                        float arc = 360*mPushDistance / mShadeView.getHeight()/2;
                        System.out.println("arc"+arc);
                        if(arc>=360){
                            ((RefreshView)mFooterView).startSweepAngleAnimation(90,360);
                            ((RefreshView)mFooterView).setCurrrentState(1);
                        }else {
                            ((RefreshView)mFooterView).startSweepAngleAnimation(90,arc);
                        }
                    }

                    setTargetOffsetBootom();
                }
                break;
            }
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int index = MotionEventCompat.getActionIndex(event);
                mActivePointerId = MotionEventCompat.getPointerId(event, index);
                break;
            }

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (mActivePointerId == INVALID_POINTER) {
                    if (action == MotionEvent.ACTION_UP) {
//                        Log.e(LOG_TAG,
//                                "Got ACTION_UP event but don't have an active pointer id.");
                    }
                    return false;
                }
                final int pointerIndex = MotionEventCompat.findPointerIndex(event,
                        mActivePointerId);
                final float y = MotionEventCompat.getY(event, pointerIndex);
                final float overscrollBottom = (mInitialMotionY - y) * DRAG_RATE;// 松手是下拉的距离
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                if (overscrollBottom < mFooterViewHeight*2+10) {// 直接取消
                    mPushDistance = 0;
                } else {// 下拉到mFooterViewHeight
                    mPushDistance = mFooterViewHeight*2+10;
                    setFootRefreshing((int)overscrollBottom,mPushDistance);
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    setTargetOffsetBootom();
                }
                return false;
            }
        }
        return true;
    }

    private void setFootRefreshing(final int  start, final int end) {
        ((RefreshView)mFooterView).setCurrrentState(2);
        ((RefreshView)mFooterView).startArcAnimation();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                animatorFooterToBottom(start,mPushDistance);
            }
        }, 2000);
    }

    private void animatorFooterToBottom(int start, int end) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(start, end);
        valueAnimator.setDuration(200);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                // update
                mPushDistance = (Integer) valueAnimator.getAnimatedValue();
                setTargetOffsetBootom();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if(mFootViewSccessState){
                    mPushDistance=0;
                    setTargetOffsetBootom();
                }else {
                    Toast.makeText(getContext(),"上拉加载出错-0-",Toast.LENGTH_SHORT).show();
                    setTargetOffsetBootom();
                }
                if(mOnPushLoadMoreListener!=null){
                    mOnPushLoadMoreListener.onLoadMore();
                }
            }
        });
        valueAnimator.setInterpolator(mDecelerateInterpolator);
        valueAnimator.start();
    }
    public void setFooterSuccessState(boolean state){
        this.mFootViewSccessState=state;
    }

    public void setTargetOffsetBootom(){
        mShadeView.setVisibility(View.VISIBLE);
        mShadeView.bringToFront();
        //针对4.4及之前版本的兼容
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            mShadeView.getParent().requestLayout();
        }
        mShadeView.offsetTopAndBottom(-mPushDistance);
    }
    /**
     * 设置下拉刷新回调
     *
     * @param listener
     */
    public void setOnPullRefreshListener(OnPullRefreshListener listener) {
        mListener = listener;
    }
    /**
     * 下拉刷新回调
     */
    public interface OnPullRefreshListener {
        void onRefresh();
    }
    /**
     * 设置上拉加载更多的接口
     *
     * @param onPushLoadMoreListener
     */
    public void setOnPushLoadMoreListener(
            OnPushLoadMoreListener onPushLoadMoreListener) {
        this.mOnPushLoadMoreListener = onPushLoadMoreListener;
    }
    /**
     * 上拉加载更多
     */
    public interface OnPushLoadMoreListener {
        void onLoadMore();
    }


}
