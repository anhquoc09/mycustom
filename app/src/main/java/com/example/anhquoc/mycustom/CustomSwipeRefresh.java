package com.example.anhquoc.mycustom;

import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ListViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;

public class CustomSwipeRefresh extends ViewGroup implements NestedScrollingParent, NestedScrollingChild {

    private static final String TAG = CustomSwipeRefresh.class.getSimpleName();

    private static final float DRAG_RATE = 0.5f;

    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;

    private static final int REFRESH_ICON_HEIGHT = 67;

    private static final int REFRESH_ICON_WIDTH = 31;

    private static final int INVALID_POINTER = -1;

    private static final int SCALE_DOWN_DURATION = 200;

    private static final int ANIMATE_TO_TRIGGER_DURATION = 200;

    private static final int ANIMATE_TO_START_DURATION = 200;

    private static final int DEFAULT_ICON_OFFSET_END = 70;

    private static final int[] LAYOUT_ATTRS = new int[]{
            android.R.attr.enabled
    };

    private Uri mUri;

    private View mTarget;

    private SimpleDraweeView mDraweeView;

    private OnRefreshListener mListener;

    private OnChildScrollUpCallback mChildScrollUpCallback;

    private int mFrom;

    private int mOriginalOffsetTop;

    private int mIconOffsetEnd;

    private int mTouchSlop;

    private int mMediumAnimationDuration;

    private int mCurrentOffsetTop;

    private int mActivePointerId = INVALID_POINTER;

    private float mTotalDragDistance;

    private float mTotalUnconsumed;

    private float mInitialMotionY;

    private float mInitialDownY;

    private boolean mNestedScrollInProgress;

    private boolean mRefreshing = false;

    private boolean mIsBeingDragged;

    private boolean mNotify;

    private boolean mReturningToStart;

    private final DecelerateInterpolator mDecelerateInterpolator;

    private final NestedScrollingParentHelper mNestedScrollingParentHelper;

    private final NestedScrollingChildHelper mNestedScrollingChildHelper;

    private final int[] mParentScrollConsumed = new int[2];

    private final int[] mParentOffsetInWindow = new int[2];

    private final Animation mScaleUpAnimation = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            setIconScale(interpolatedTime);
        }
    };

    private final Animation mScaleDownToStartAnimation = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            setIconScale(1 - interpolatedTime);
            mFrom = mCurrentOffsetTop;
            moveToStart(interpolatedTime);
        }
    };

    private final Animation mAnimateToCorrectPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int endTarget = mIconOffsetEnd - Math.abs(mOriginalOffsetTop);
            int targetTop = (mFrom + (int) ((endTarget - mFrom) * interpolatedTime));
            int offset = targetTop - mDraweeView.getTop();
            setTargetOffsetTopAndBottom(offset);
        }
    };

    private final Animation mAnimateToStartPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            moveToStart(interpolatedTime);
        }
    };

    private final Animation.AnimationListener mRefreshListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (mRefreshing) {
                if (mNotify) {
                    if (mListener != null) {
                        mListener.onRefresh();
                    }
                }
                mCurrentOffsetTop = mDraweeView.getTop();
            } else {
                reset();
            }
        }
    };

    void reset() {
        mDraweeView.clearAnimation();
        mDraweeView.setVisibility(View.GONE);

        setTargetOffsetTopAndBottom(mOriginalOffsetTop - mCurrentOffsetTop);
        mCurrentOffsetTop = mDraweeView.getTop();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            reset();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        reset();
    }

    public CustomSwipeRefresh(Context context) {
        this(context, null);
    }

    public CustomSwipeRefresh(Context context, AttributeSet attrs) {
        super(context, attrs);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mMediumAnimationDuration = getResources().getInteger(android.R.integer.config_mediumAnimTime);

        setWillNotDraw(false);
        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);

        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        int iconHeight = (int) (REFRESH_ICON_HEIGHT * metrics.density);

        createProgressView();
        mIconOffsetEnd = (int) (DEFAULT_ICON_OFFSET_END * metrics.density);
        mTotalDragDistance = mIconOffsetEnd;

        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);

        mOriginalOffsetTop = mCurrentOffsetTop = -iconHeight;
        moveToStart(1.0f);

        final TypedArray a = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
        setEnabled(a.getBoolean(0, true));
        a.recycle();
    }

    private void createProgressView() {
        mUri = new Uri.Builder().scheme(UriUtil.LOCAL_ASSET_SCHEME).path("refreshing_icon.webp").build();

        mDraweeView = new SimpleDraweeView(getContext());
        mDraweeView.setLayoutParams(new LinearLayout.LayoutParams(31, 67));

        mDraweeView.setVisibility(View.GONE);
        addView(mDraweeView);
        autoPlayRefreshAnimation();
    }

    private void autoPlayRefreshAnimation() {
        mDraweeView.setController(
                Fresco.newDraweeControllerBuilder()
                        .setUri(mUri)
                        .setAutoPlayAnimations(true)
                        .build()
        );
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        mListener = listener;
    }

    public void setRefreshing(boolean refreshing) {
        if (refreshing && mRefreshing != refreshing) {
            mRefreshing = refreshing;
            int endTarget = mIconOffsetEnd + mOriginalOffsetTop;
            setTargetOffsetTopAndBottom(endTarget - mCurrentOffsetTop);
            mNotify = false;
            startScaleUpAnimation(mRefreshListener);
        } else {
            setRefreshing(refreshing, false);
        }
    }

    public boolean isRefreshing() {
        return mRefreshing;
    }

    public boolean canChildScrollUp() {
        if (mChildScrollUpCallback != null) {
            return mChildScrollUpCallback.canChildScrollUp(this, mTarget);
        }
        if (mTarget instanceof ListView) {
            return ListViewCompat.canScrollList((ListView) mTarget, -1);
        }
        return mTarget.canScrollVertically(-1);
    }

    public void setOnChildScrollUpCallback(@Nullable OnChildScrollUpCallback callback) {
        mChildScrollUpCallback = callback;
    }

    public void setDistanceToTriggerSync(int distance) {
        mTotalDragDistance = distance;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        if (getChildCount() == 0) {
            return;
        }
        if (mTarget == null) {
            ensureTarget();
        }
        if (mTarget == null) {
            return;
        }

        int iconWidth = mDraweeView.getMeasuredWidth();
        int iconHeight = mDraweeView.getMeasuredHeight();
        int iconTop = getPaddingTop() + mCurrentOffsetTop;
        int iconBottom = iconTop + iconHeight;
        mDraweeView.layout((width / 2 - iconWidth / 2), iconTop, (width / 2 + iconWidth / 2), iconBottom);

        final int childLeft = getPaddingLeft();
        final int childTop = mCurrentOffsetTop + iconHeight;
        final int childWidth = width - getPaddingLeft() - getPaddingRight();
        final int childHeight = height - getPaddingTop() - getPaddingBottom();
        mTarget.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mTarget == null) {
            ensureTarget();
        }
        if (mTarget == null) {
            return;
        }
        mTarget.measure(MeasureSpec.makeMeasureSpec(
                getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(
                        getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY));

        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        mDraweeView.measure(MeasureSpec.makeMeasureSpec((int) (REFRESH_ICON_WIDTH * metrics.density), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec((int) (REFRESH_ICON_HEIGHT * metrics.density), MeasureSpec.EXACTLY));
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureTarget();

        final int action = ev.getActionMasked();
        int pointerIndex;

        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }

        if (!isEnabled() || mReturningToStart || canChildScrollUp() || mRefreshing || mNestedScrollInProgress) {
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                setTargetOffsetTopAndBottom(mOriginalOffsetTop - mDraweeView.getTop());
                mActivePointerId = ev.getPointerId(0);
                mIsBeingDragged = false;

                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                mInitialDownY = ev.getY(pointerIndex);
                break;

            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER) {
                    Log.e(TAG, "Got ACTION_MOVE event but don't have an active pointer id.");
                    return false;
                }

                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                final float y = ev.getY(pointerIndex);
                startDragging(y);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
        }

        return mIsBeingDragged;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        if ((mTarget instanceof AbsListView)
                || (mTarget != null && !ViewCompat.isNestedScrollingEnabled(mTarget))) {
            //Nope.
        } else {
            super.requestDisallowInterceptTouchEvent(b);
        }
    }

    // NestedScrollingParent

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int nestedScrollAxes) {
        return isEnabled() && !mReturningToStart && !mRefreshing
                && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes) {
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
        startNestedScroll(axes & ViewCompat.SCROLL_AXIS_VERTICAL);
        mTotalUnconsumed = 0;
        mNestedScrollInProgress = true;
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed) {
        if (dy > 0 && mTotalUnconsumed > 0) {
            if (dy > mTotalUnconsumed) {
                consumed[1] = dy - (int) mTotalUnconsumed;
                mTotalUnconsumed = 0;
            } else {
                mTotalUnconsumed -= dy;
                consumed[1] = dy;
            }
            moveIcon(mTotalUnconsumed);
        }

        final int[] parentConsumed = mParentScrollConsumed;
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
            consumed[0] += parentConsumed[0];
            consumed[1] += parentConsumed[1];
        }
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    @Override
    public void onStopNestedScroll(@NonNull View target) {
        mNestedScrollingParentHelper.onStopNestedScroll(target);
        mNestedScrollInProgress = false;

        if (mTotalUnconsumed > 0) {
            finishMoveIcon(mTotalUnconsumed);
            mTotalUnconsumed = 0;
        }
        stopNestedScroll();
    }

    @Override
    public void onNestedScroll(final @NonNull View target, final int dxConsumed, final int dyConsumed,
                               final int dxUnconsumed, final int dyUnconsumed) {
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                mParentOffsetInWindow);

        final int dy = dyUnconsumed + mParentOffsetInWindow[1];
        if (dy < 0 && !canChildScrollUp()) {
            mTotalUnconsumed += Math.abs(dy);
            moveIcon(mTotalUnconsumed);
        }
    }

    // NestedScrollingChild

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mNestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mNestedScrollingChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mNestedScrollingChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed,
                dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(
                dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean onNestedPreFling(@NonNull View target, float velocityX, float velocityY) {
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean onNestedFling(@NonNull View target, float velocityX, float velocityY, boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();
        int pointerIndex;

        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }

        if (!isEnabled() || mReturningToStart || canChildScrollUp() || mRefreshing || mNestedScrollInProgress) {
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);
                mIsBeingDragged = false;
                break;

            case MotionEvent.ACTION_MOVE: {
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }

                final float y = ev.getY(pointerIndex);
                startDragging(y);

                if (mIsBeingDragged) {
                    final float overScrollTop = (y - mInitialMotionY) * DRAG_RATE;
                    if (overScrollTop > 0) {
                        moveIcon(overScrollTop);
                    } else {
                        return false;
                    }
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                pointerIndex = ev.getActionIndex();
                if (pointerIndex < 0) {
                    Log.e(TAG, "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                    return false;
                }
                mActivePointerId = ev.getPointerId(pointerIndex);
                break;
            }

            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP: {
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(TAG, "Got ACTION_UP event but don't have an active pointer id.");
                    return false;
                }

                if (mIsBeingDragged) {
                    final float y = ev.getY(pointerIndex);
                    final float overScrollTop = (y - mInitialMotionY) * DRAG_RATE;
                    mIsBeingDragged = false;
                    finishMoveIcon(overScrollTop);
                }
                mActivePointerId = INVALID_POINTER;
                return false;
            }
            case MotionEvent.ACTION_CANCEL:
                return false;
        }

        return true;
    }

    private void ensureTarget() {
        if (mTarget == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (!child.equals(mDraweeView)) {
                    mTarget = child;
                    break;
                }
            }
        }
    }

    private void moveIcon(float overScrollTop) {
        float originalDragPercent = overScrollTop / mTotalDragDistance;

        float dragPercent = Math.min(1f, Math.abs(originalDragPercent));
        float extraOS = Math.abs(overScrollTop) - mTotalDragDistance;
        float tensionSlingshotPercent = Math.max(0, Math.min(extraOS, mIconOffsetEnd * 2) / mIconOffsetEnd);
        float tensionPercent = (float) ((tensionSlingshotPercent / 4) - Math.pow((tensionSlingshotPercent / 4), 2)) * 2f;
        float extraMove = (mIconOffsetEnd) * tensionPercent * 2;

        int targetY = mOriginalOffsetTop + (int) ((mIconOffsetEnd * dragPercent) + extraMove);
        if (mDraweeView.getVisibility() != View.VISIBLE) {
            mDraweeView.setVisibility(View.VISIBLE);
        }
        float scale = Math.min(Math.abs((mTotalDragDistance + mCurrentOffsetTop) / mTotalDragDistance), 1);
        setIconScale(scale);

        setTargetOffsetTopAndBottom(targetY - mCurrentOffsetTop);

    }

    private void finishMoveIcon(float overScrollTop) {
        if (overScrollTop > mTotalDragDistance) {
            setRefreshing(true, true);
        } else {
            mRefreshing = false;
            Animation.AnimationListener listener = new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    startScaleDownToStartAnimation(null);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            };
            animateOffsetToStartPosition(mCurrentOffsetTop, listener);
        }
    }

    private void startDragging(float y) {
        final float yDiff = y - mInitialDownY;
        if (yDiff > mTouchSlop && !mIsBeingDragged) {
            mInitialMotionY = mInitialDownY + mTouchSlop;
            mIsBeingDragged = true;
        }
    }

    private void setIconScale(float scale) {
        mDraweeView.setScaleX(scale);
        mDraweeView.setScaleY(scale);
    }

    private void setRefreshing(boolean refreshing, final boolean notify) {
        if (mRefreshing != refreshing) {
            mNotify = notify;
            ensureTarget();
            mRefreshing = refreshing;
            if (mRefreshing) {
                animateOffsetToCorrectPosition(mCurrentOffsetTop, mRefreshListener);
            } else {
                startScaleDownToStartAnimation(mRefreshListener);
            }
        }
    }

    private void moveToStart(float interpolatedTime) {
        int targetTop = (mFrom + (int) ((mOriginalOffsetTop - mFrom) * interpolatedTime));
        int offset = targetTop - mDraweeView.getTop();
        setTargetOffsetTopAndBottom(offset);
    }

    private void setTargetOffsetTopAndBottom(int offset) {
        mDraweeView.bringToFront();
        ViewCompat.offsetTopAndBottom(mDraweeView, offset);
        mCurrentOffsetTop = mDraweeView.getTop();
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        int pointerIndex = ev.getActionIndex();
        int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    private void startScaleUpAnimation(Animation.AnimationListener listener) {
        mScaleUpAnimation.reset();
        mScaleUpAnimation.setDuration(mMediumAnimationDuration);
        mScaleUpAnimation.setAnimationListener(listener);

        mDraweeView.setVisibility(View.VISIBLE);
        mDraweeView.clearAnimation();
        mDraweeView.startAnimation(mScaleUpAnimation);
    }

    private void startScaleDownToStartAnimation(Animation.AnimationListener listener) {
        mScaleDownToStartAnimation.reset();
        mScaleDownToStartAnimation.setDuration(SCALE_DOWN_DURATION);
        mScaleDownToStartAnimation.setAnimationListener(listener);

        mDraweeView.clearAnimation();
        mDraweeView.startAnimation(mScaleDownToStartAnimation);
    }

    private void animateOffsetToCorrectPosition(int from, Animation.AnimationListener listener) {
        mFrom = from;
        mAnimateToCorrectPosition.reset();
        mAnimateToCorrectPosition.setDuration(ANIMATE_TO_TRIGGER_DURATION);
        mAnimateToCorrectPosition.setInterpolator(mDecelerateInterpolator);
        mAnimateToCorrectPosition.setAnimationListener(listener);

        mDraweeView.clearAnimation();
        mDraweeView.startAnimation(mAnimateToCorrectPosition);
    }

    private void animateOffsetToStartPosition(int from, Animation.AnimationListener listener) {
        mFrom = from;
        mAnimateToStartPosition.reset();
        mAnimateToStartPosition.setDuration(ANIMATE_TO_START_DURATION);
        mAnimateToStartPosition.setInterpolator(mDecelerateInterpolator);
        mAnimateToStartPosition.setAnimationListener(listener);

        mDraweeView.clearAnimation();
        mDraweeView.startAnimation(mAnimateToStartPosition);
    }

    /**
     * {@link OnRefreshListener}
     */
    public interface OnRefreshListener {
        void onRefresh();
    }

    /**
     * {@link OnChildScrollUpCallback}
     */
    public interface OnChildScrollUpCallback {
        boolean canChildScrollUp(CustomSwipeRefresh parent, @Nullable View child);
    }
}