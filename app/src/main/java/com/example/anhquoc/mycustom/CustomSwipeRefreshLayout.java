package com.example.anhquoc.mycustom;

import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ListViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;

public class CustomSwipeRefreshLayout extends ViewGroup {

    private static final String TAG = CustomSwipeRefreshLayout.class.getSimpleName();

    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;

    private static final int INVALID_POINTER = -1;

    private static final float DRAG_RATE = .5f;

    private static final int SCALE_DOWN_DURATION = 150;

    private static final int ANIMATE_TO_TRIGGER_DURATION = 200;

    private static final int ANIMATE_TO_START_DURATION = 200;

    private static final int REFRESH_ICON_WIDTH = 31;

    private static final int REFRESH_ICON_HEIGHT = 67;

    private final int mMediumAnimationDuration;

    private View mTarget;

    private OnRefreshListener mListener;

    private boolean mRefreshing = false;

    private boolean mNotify = false;

    private int mTouchSlop;

    private float mTotalDragDistance = -1;

    private int mCurrentTargetTop;

    private float mInitialMotionY;

    private float mInitialDownY;

    private boolean mIsBeingDragged;

    private int mActivePointerId = INVALID_POINTER;

    private boolean mScale;

    private boolean mReturningToStart;

    private final DecelerateInterpolator mDecelerateInterpolator;

    private static final int[] LAYOUT_ATTRS = new int[]{
            android.R.attr.enabled
    };

    private SimpleDraweeView mRefreshView;

    private int mImageViewIndex = -1;

    private int mFrom;

    private float mStartingScale;

    private int mOriginalOffsetTop;

    private int mSpinnerOffsetEnd;

    private Uri mUri;

    private Animation mScaleAnimation;

    private final Animation mScaleDownAnimation = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            moveToStart(interpolatedTime);
        }
    };

    private final Animation mAnimateToCorrectPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int targetTop;
            int endTarget;
            if (!mUsingCustomStart) {
                endTarget = mSpinnerOffsetEnd - Math.abs(mOriginalOffsetTop);
            } else {
                endTarget = mSpinnerOffsetEnd;
            }
            targetTop = (mFrom + (int) ((endTarget - mFrom) * interpolatedTime));
            int offset = targetTop - mRefreshView.getTop();
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
                mCurrentTargetTop = mRefreshView.getTop();
            } else {
                reset();
            }
        }
    };

    void reset() {
        mRefreshView.setVisibility(View.GONE);

        if (mScale) {
//            setAnimationProgress(0);
        } else {
            setTargetOffsetTopAndBottom(mOriginalOffsetTop - mCurrentTargetTop);
        }
        mCurrentTargetTop = mRefreshView.getTop();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            reset();
        }
    }

    public CustomSwipeRefreshLayout(Context context) {
        this(context, null);
    }

    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mMediumAnimationDuration = getResources().getInteger(android.R.integer.config_mediumAnimTime);

        setWillNotDraw(false);
        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);

        createRefreshView();
        ViewCompat.setChildrenDrawingOrderEnabled(this, true);

        mOriginalOffsetTop = mCurrentTargetTop;
        moveToStart(1.0f);

        final TypedArray a = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
        setEnabled(a.getBoolean(0, true));
        a.recycle();
    }

//    @Override
//    protected int getChildDrawingOrder(int childCount, int i) {
//        if (mImageViewIndex < 0) {
//            return i;
//        } else if (i == childCount - 1) {
//            // Draw the selected child last
//            return mImageViewIndex;
//        } else if (i >= mImageViewIndex) {
//            // Move the children after the selected child earlier one
//            return i + 1;
//        } else {
//            // Keep the children before the selected child the same
//            return i;
//        }
//    }

    private void createRefreshView() {
        mUri = new Uri.Builder().scheme(UriUtil.LOCAL_ASSET_SCHEME).path("refreshing_icon.webp").build();

        LayoutParams layoutParams = new LinearLayout.LayoutParams(REFRESH_ICON_WIDTH, REFRESH_ICON_HEIGHT);

        mRefreshView = new SimpleDraweeView(getContext());
        mRefreshView.setLayoutParams(layoutParams);
        mRefreshView.setVisibility(View.GONE);
        addView(mRefreshView);
        autoPlayRefreshAnimation();
    }

    private void autoPlayRefreshAnimation() {
        mRefreshView.setController(
                Fresco.newDraweeControllerBuilder()
                        .setUri(mUri)
                        .setAutoPlayAnimations(true)
                        .build()
        );
    }

    private void startScaleUpAnimation(Animation.AnimationListener listener) {
        mRefreshView.setVisibility(View.VISIBLE);

        mScaleAnimation.setDuration(mMediumAnimationDuration);
        if (listener != null) {
            mScaleAnimation.setAnimationListener(listener);
        }
        mRefreshView.clearAnimation();
        mRefreshView.startAnimation(mScaleAnimation);
    }

    void setAnimationProgress(float progress) {
        mRefreshView.setScaleX(progress);
        mRefreshView.setScaleY(progress);
    }

    private void setRefreshing(boolean refreshing, final boolean notify) {
        if (mRefreshing != refreshing) {
            mNotify = notify;
            ensureTarget();
            mRefreshing = refreshing;
            if (mRefreshing) {
                animateOffsetToCorrectPosition();
            } else {
                startScaleDownAnimation();
            }
        }
    }

    private void startScaleDownAnimation() {
        mScaleDownAnimation.reset();
        mScaleDownAnimation.setDuration(SCALE_DOWN_DURATION);
        mScaleDownAnimation.setInterpolator(mDecelerateInterpolator);
        mScaleDownAnimation.setAnimationListener(mRefreshListener);

        mRefreshView.clearAnimation();
        mRefreshView.startAnimation(mScaleDownAnimation);
    }

    private boolean isAnimationRunning(Animation animation) {
        return animation != null && animation.hasStarted() && !animation.hasEnded();
    }

    private void animateOffsetToCorrectPosition() {
        mFrom = mCurrentTargetTop;
        mAnimateToCorrectPosition.reset();
        mAnimateToCorrectPosition.setDuration(ANIMATE_TO_TRIGGER_DURATION);
        mAnimateToCorrectPosition.setInterpolator(mDecelerateInterpolator);
        mAnimateToCorrectPosition.setAnimationListener(mRefreshListener);

        mRefreshView.clearAnimation();
        mRefreshView.startAnimation(mAnimateToCorrectPosition);
    }

    private void animateOffsetToStartPosition() {
        if (mScale) {
//            startScaleDownReturnToStartAnimation(from, listener);
        } else {
            mFrom = mCurrentTargetTop;
            mAnimateToStartPosition.reset();
            mAnimateToStartPosition.setDuration(ANIMATE_TO_START_DURATION);
            mAnimateToStartPosition.setInterpolator(mDecelerateInterpolator);
            mAnimateToStartPosition.setAnimationListener(mRefreshListener);

            mRefreshView.clearAnimation();
            mRefreshView.startAnimation(mAnimateToStartPosition);
        }
    }

    void setTargetOffsetTopAndBottom(int offset) {
//        mRefreshView.bringToFront();
        mTarget.offsetTopAndBottom(offset);
        mCurrentTargetTop = mRefreshView.getTop();
    }

    private void moveToStart(float interpolatedTime) {
        int targetTop;
        targetTop = (mFrom + (int) ((mOriginalOffsetTop - mFrom) * interpolatedTime));
        int offset = targetTop - mRefreshView.getTop();
        setTargetOffsetTopAndBottom(offset);
    }

//    private void startScaleDownReturnToStartAnimation(int from,
//                                                      Animation.AnimationListener listener) {
//        mFrom = from;
//        mStartingScale = mRefreshView.getScaleX();
//        mScaleDownToStartAnimation = new Animation() {
//            @Override
//            public void applyTransformation(float interpolatedTime, Transformation t) {
//                float targetScale = (mStartingScale + (-mStartingScale  * interpolatedTime));
//                setAnimationProgress(targetScale);
//                moveToStart(interpolatedTime);
//            }
//        };
//        mScaleDownToStartAnimation.setDuration(SCALE_DOWN_DURATION);
//        if (listener != null) {
//            mRefreshView.setAnimationListener(listener);
//        }
//        mRefreshView.clearAnimation();
//        mRefreshView.startAnimation(mScaleDownToStartAnimation);
//    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = ev.getActionIndex();
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    private void startDragging(float y) {
        float yDiff = y - mInitialDownY;
        if (yDiff > mTouchSlop && !mIsBeingDragged) {
            mInitialMotionY = mInitialDownY + mTouchSlop;
            mIsBeingDragged = true;
        }
    }

    private void ensureTarget() {
        if (mTarget == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (!child.equals(mRefreshView)) {
                    mTarget = child;
                    break;
                }
            }
        }
    }

    private void moveSpinner(float overscrollTop) {
        float originalDragPercent = overscrollTop / mTotalDragDistance;

        float dragPercent = Math.min(1f, Math.abs(originalDragPercent));
        float extraOS = Math.abs(overscrollTop) - mTotalDragDistance;
        float slingshotDist = mUsingCustomStart ? mSpinnerOffsetEnd - mOriginalOffsetTop
                : mSpinnerOffsetEnd;
        float tensionSlingshotPercent = Math.max(0, Math.min(extraOS, slingshotDist * 2)
                / slingshotDist);
        float tensionPercent = (float) ((tensionSlingshotPercent / 4) - Math.pow(
                (tensionSlingshotPercent / 4), 2)) * 2f;
        float extraMove = (slingshotDist) * tensionPercent * 2;

        int targetY = mOriginalOffsetTop + (int) ((slingshotDist * dragPercent) + extraMove);
        // where 1.0f is a full circle
        if (mRefreshView.getVisibility() != View.VISIBLE) {
            mRefreshView.setVisibility(View.VISIBLE);
        }
        if (!mScale) {
            mRefreshView.setScaleX(1f);
            mRefreshView.setScaleY(1f);
        }

        setTargetOffsetTopAndBottom(targetY - mCurrentTargetTop);
    }

    private void finishSpinner(float overscrollTop) {
        if (overscrollTop > mTotalDragDistance) {
            setRefreshing(true, true /* notify */);
        } else {
            // cancel refresh
            mRefreshing = false;
            Animation.AnimationListener listener = null;
            if (!mScale) {
                listener = new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (!mScale) {
                            startScaleDownAnimation();
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                };
            }
            animateOffsetToStartPosition();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        reset();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
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
        final int left = getPaddingLeft();
        final int top = getPaddingTop();
        final int right = left + width - getPaddingRight();
        final int bottom = top + height - getPaddingBottom();

        mTarget.layout(left, top + mTarget.getTop(), right, bottom + mTarget.getTop());

        int refreshWidth = mRefreshView.getMeasuredWidth();
        int refreshHeight = mRefreshView.getMeasuredHeight();
        mRefreshView.layout(width / 2 - refreshWidth / 2, top,
                width / 2 + refreshWidth / 2, top + refreshHeight);
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
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(
                getMeasuredWidth() - getPaddingRight() - getPaddingLeft(), MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(
                getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY);

        mTarget.measure(widthMeasureSpec, heightMeasureSpec);
        mRefreshView.measure(widthMeasureSpec, heightMeasureSpec);
        mImageViewIndex = -1;

        for (int index = 0; index < getChildCount(); index++) {
            if (getChildAt(index) == mRefreshView) {
                mImageViewIndex = index;
                break;
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();
        int pointerIndex;

        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }

        if (!isEnabled() || mReturningToStart || canChildScrollUp() || mRefreshing) {
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                setTargetOffsetTopAndBottom(mOriginalOffsetTop - mRefreshView.getTop());
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
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();
        int pointerIndex;

        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }

        if (!isEnabled() || mReturningToStart || canChildScrollUp() || mRefreshing) {
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
                    return false;
                }

                final float y = ev.getY(pointerIndex);
                startDragging(y);

                if (mIsBeingDragged) {
                    final float overscrollTop = (y - mInitialMotionY) * DRAG_RATE;
                    if (overscrollTop > 0) {
                        moveSpinner(overscrollTop);
                    } else {
                        return false;
                    }
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                pointerIndex = ev.getActionIndex();
                if (pointerIndex < 0) {
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
                    return false;
                }

                if (mIsBeingDragged) {
                    final float y = ev.getY(pointerIndex);
                    final float overscrollTop = (y - mInitialMotionY) * DRAG_RATE;
                    mIsBeingDragged = false;
                    finishSpinner(overscrollTop);
                }
                mActivePointerId = INVALID_POINTER;
                return false;
            }
            case MotionEvent.ACTION_CANCEL:
                return false;
        }
        return true;
    }

    public void setRefreshing(boolean refreshing) {
        if (mRefreshing != refreshing) {
            setRefreshing(refreshing, false);
        }
    }

    public boolean canChildScrollUp() {
        if (mTarget instanceof ListView) {
            return ListViewCompat.canScrollList((ListView) mTarget, -1);
        }
        return mTarget.canScrollVertically(-1);
    }

    public boolean isRefreshing() {
        return mRefreshing;
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        mListener = listener;
    }

    /**
     * {@link OnRefreshListener}
     */
    public interface OnRefreshListener {
        void onRefresh();
    }
}
