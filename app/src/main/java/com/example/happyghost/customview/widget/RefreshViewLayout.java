package com.example.happyghost.customview.widget;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

/**
 * @author Zhao Chenping
 * @creat 2017/7/22.
 * @description
 */

public class RefreshViewLayout extends ViewGroup implements NestedScrollingParent,NestedScrollingChild{

    private RefreshView mHeaderView;
    private RefreshView mFooterView;
    private NestedScrollingParentHelper mNestedScrollingParentHelper;
    private NestedScrollingChildHelper mNestedScrollingChildHelper;
    private View mTarget;
    private int mHeaderViewIndex=-1;
    private int mFooterViewIndex=-1;
    private int mCurrentTargetOffsetTop =0;
    private boolean targetScrollWithLayout ;
    private boolean mReturningToStart;
    private boolean mRefreshing=false;
    private boolean mLoadMore=false;
    private boolean mIsBeingDragged;

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
        setWillNotDraw(false);
        createHeaderView();
        createFooterView();
        ViewCompat.setChildrenDrawingOrderEnabled(this, true);
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);

        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
//        setNestedScrollingEnabled(true);
//        moveToStart(1.0f);
    }

    private void createHeaderView() {
        View view = setHeaderView();
        addView(view);
    }

    public View setHeaderView() {
        mHeaderView = new RefreshView(getContext());
        mHeaderView.setVisibility(GONE);
        return mHeaderView;
    }

    private void createFooterView() {
        View view = setFooterView();
        addView(view);
    }
    public View setFooterView(){
        mFooterView = new RefreshView(getContext());
        mFooterView.setVisibility(GONE);
        return mFooterView;
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
        mHeaderView.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY));
        mFooterView.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY));
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
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        if(getChildCount()==0){
            return;
        }
        if(mTarget==null){
            ensureTarget();
        }
        if(mTarget==null){
            return;
        }

        int distance = mCurrentTargetOffsetTop + mHeaderView.getHeight();
        if (!targetScrollWithLayout) {
            // 判断标志位，如果目标View不跟随手指的滑动而滑动，将下拉偏移量设置为0
            distance = 0;
        }
        final View child = mTarget;
        final int childLeft = getPaddingLeft();
        final int childTop = getPaddingTop();
//        final  int mTargetTop = getPaddingTop() + distance - pushDistance;// 根据偏移量distance更新
        final  int mTargetTop = getPaddingTop() + distance ;// 根据偏移量distance更新
        final int childWidth = width - getPaddingLeft() - getPaddingRight();
        final int childHeight = height - getPaddingTop() - getPaddingBottom();
        child.layout(childLeft, mTargetTop, childLeft + childWidth, mTargetTop + childHeight);

        int headerWidth = mHeaderView.getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int headerHeight = mHeaderView.getMeasuredHeight()- getPaddingTop() - getPaddingBottom();
        mHeaderView.layout(childLeft , childTop, childLeft + headerWidth, childTop + headerHeight);

        int footererWidth = mHeaderView.getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int footerHeight = mHeaderView.getMeasuredHeight();
        mHeaderView.layout(childLeft , childTop + childHeight-footerHeight, childLeft + footererWidth, childTop + childHeight);


    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        // 将新添加的View,放到最后绘制
        if (mHeaderViewIndex < 0 && mFooterViewIndex < 0) {
            return i;
        }
        if (i == childCount - 2) {
            return mHeaderViewIndex;
        }
        if (i == childCount - 1) {
            return mFooterViewIndex;
        }
        int bigIndex = mFooterViewIndex > mHeaderViewIndex ? mFooterViewIndex
                : mHeaderViewIndex;
        int smallIndex = mFooterViewIndex < mHeaderViewIndex ? mFooterViewIndex
                : mHeaderViewIndex;
        if (i >= smallIndex && i < bigIndex - 1) {
            return i + 1;
        }
        if (i >= bigIndex || (i == bigIndex - 1)) {
            return i + 2;
        }
        return i;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureTarget();

        final int action = MotionEventCompat.getActionMasked(ev);

        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }
        if (!isEnabled() || mReturningToStart || mRefreshing || mLoadMore
                || (isChildScrollToTop() && isChildScrollToBottom())) {
            // 如果子View可以滑动，不拦截事件，交给子View处理-下拉刷新
            // 或者子View没有滑动到底部不拦截事件-上拉加载更多
            return false;
        }
//
//        // 下拉刷新判断
//        switch (action) {
//            case MotionEvent.ACTION_DOWN:
//                setTargetOffsetTopAndBottom(
//                        mOriginalOffsetTop - mHeadViewContainer.getTop(), true);// 恢复HeaderView的初始位置
//                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
//                mIsBeingDragged = false;
//                final float initialMotionY = getMotionEventY(ev, mActivePointerId);
//                if (initialMotionY == -1) {
//                    return false;
//                }
//                mInitialMotionY = initialMotionY;// 记录按下的位置
//
//            case MotionEvent.ACTION_MOVE:
//                if (mActivePointerId == INVALID_POINTER) {
//                    Log.e(LOG_TAG,
//                            "Got ACTION_MOVE event but don't have an active pointer id.");
//                    return false;
//                }
//
//                final float y = getMotionEventY(ev, mActivePointerId);
//                if (y == -1) {
//                    return false;
//                }
//                float yDiff = 0;
//                if (isChildScrollToBottom()) {
//                    yDiff = mInitialMotionY - y;// 计算上拉距离
//                    if (yDiff > mTouchSlop && !mIsBeingDragged) {// 判断是否下拉的距离足够
//                        mIsBeingDragged = true;// 正在上拉
//                    }
//                } else {
//                    yDiff = y - mInitialMotionY;// 计算下拉距离
//                    if (yDiff > mTouchSlop && !mIsBeingDragged) {// 判断是否下拉的距离足够
//                        mIsBeingDragged = true;// 正在下拉
//                    }
//                }
//                break;
//
//            case MotionEventCompat.ACTION_POINTER_UP:
//                onSecondaryPointerUp(ev);
//                break;
//
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_CANCEL:
//                mIsBeingDragged = false;
//                mActivePointerId = INVALID_POINTER;
//                break;
//        }
//
        return mIsBeingDragged;// 如果正在拖动，则拦截子View的事件
    }

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
        return false;
    }

}
