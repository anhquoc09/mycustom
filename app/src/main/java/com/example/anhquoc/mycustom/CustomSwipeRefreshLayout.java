package com.example.anhquoc.mycustom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ListViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.OverScroller;
import android.widget.ScrollView;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;

public class CustomSwipeRefreshLayout extends ViewGroup
        implements NestedScrollingParent, NestedScrollingChild {

    private static final String TAG = CustomSwipeRefreshLayout.class.getSimpleName();

    private final NestedScrollingParentHelper parentHelper;
    private final NestedScrollingChildHelper childHelper;
    private final int[] parentScrollConsumed = new int[2];
    final int[] parentOffsetInWindow = new int[2];

    ImageView headerView;
    ImageView footerView;
    View targetView;
    private View pullContentLayout;

    int refreshTriggerDistance = -1;
    int loadTriggerDistance = -1;
    private int pullDownMaxDistance = -1;
    private int pullUpMaxDistance = -1;
    private int refreshAnimationDuring = 180;
    private int resetAnimationDuring = 400;
    private int topOverScrollMaxTriggerOffset = 60;
    private int bottomOverScrollMaxTriggerOffset = 60;
    private int overScrollMinDuring = 65;
    private int targetViewId = -1;
    private float dragDampingRatio = 0.6F;
    private float overScrollAdjustValue = 1F;
    private float overScrollDampingRatio = 0.35F;

    boolean isMoveWithContent = true;

    private int progressState = 0;

    private int lastScrollY = 0;
    private int overScrollState = 0;
    int moveDistance = 0;
    private float finalScrollDistance = 0;

    private boolean pullStateControl = true;
    private boolean isHoldingTrigger = false;
    private boolean isHoldingFinishTrigger = false;
    private boolean isResetTrigger = false;
    private boolean isOverScrollTrigger = false;

    private boolean refreshWithAction = true;
    private boolean isScrollAbleViewBackScroll = false;

    private boolean isTargetNested = false;
    private boolean isAttachWindow = false;

    private static final int[] LAYOUT_ATTRS = new int[]{
            android.R.attr.enabled
    };

    private OnRefreshListener onRefreshListener;
    private OverScroller scroller;

    private Interpolator scrollInterpolator;

    private ValueAnimator startRefreshAnimator;
    private ValueAnimator resetHeaderAnimator;
    private ValueAnimator startLoadMoreAnimator;
    private ValueAnimator resetFooterAnimator;

    private ValueAnimator overScrollAnimator;
    private Interpolator animationMainInterpolator;

    private Interpolator animationOverScrollInterpolator;

    private Runnable delayHandleActionRunnable;

    //    PullHelper
    private final int minimumFlingVelocity;
    private final int maximumVelocity;
    private final int touchSlop;

    boolean isDragVertical;
    boolean isDragHorizontal;
    boolean isDragMoveTrendDown;
    boolean isLayoutDragMoved;
    boolean isDisallowIntercept;
    private boolean lastDisallowIntercept;
    private boolean isReDispatchMoveEvent;
    private boolean isDispatchTouchCancel;
    int dragState;
    private int actionDownPointX;
    private int actionDownPointY;
    private int lastMoveDistance;
    private final int[] childConsumed = new int[2];
    private int lastChildConsumedY;
    private int activePointerId;
    private int lastDragEventY;
    private VelocityTracker velocityTracker;

    private AnimationDrawable mAnimationDrawable;

    public CustomSwipeRefreshLayout(Context context) {
        this(context, null);
    }

    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        ViewConfiguration configuration = ViewConfiguration.get(context);
        minimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        maximumVelocity = configuration.getScaledMaximumFlingVelocity();
        touchSlop = configuration.getScaledTouchSlop();

        initProgressView();

        parentHelper = new NestedScrollingParentHelper(this);
        childHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);

        TypedArray ta = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
        setEnabled(ta.getBoolean(0, true));
        ta.recycle();
    }

    private void initProgressView() {
        mAnimationDrawable = (AnimationDrawable) getResources().getDrawable(R.drawable.live_refresh_anim);

        headerView = new ImageView(getContext());
        headerView.setImageDrawable(mAnimationDrawable);

        footerView = new ImageView(getContext());
        footerView.setImageDrawable(mAnimationDrawable);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initContentView();
        dellNestedScrollCheck();
        readyScroller();
    }

    private void initContentView() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child != headerView && child != footerView) {
                pullContentLayout = child;
                break;
            }
        }
        if (pullContentLayout == null) {
            throw new RuntimeException("PullRefreshLayout should have a child");
        }

        if (targetViewId != -1) {
            targetView = findViewById(targetViewId);
        }
        if (targetView == null) {
            targetView = pullContentLayout;
        }

        setHeaderView(headerView);
        setFooterView(footerView);
    }

    public boolean dispatchSuperTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (super.dispatchTouchEvent(ev)) {
            return true;
        }

        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                activePointerId = ev.getPointerId(0);
                actionDownPointX = (int) (ev.getX() + 0.5f);
                lastDragEventY = actionDownPointY = (int) (ev.getY() + 0.5f);

                isLayoutDragMoved = false;
                isDisallowIntercept = false;
                lastDisallowIntercept = false;

                onStartScroll();
                dispatchSuperTouchEvent(ev);
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!isDisallowIntercept) {
                    final int pointerIndex = ev.findPointerIndex(activePointerId);
                    if (ev.findPointerIndex(activePointerId) == -1) {
                        break;
                    }
                    int tempY = (int) (ev.getY(pointerIndex) + 0.5f);
                    if (lastDisallowIntercept) {
                        lastDragEventY = tempY;
                    }
                    int deltaY = lastDragEventY - tempY;
                    lastDragEventY = tempY;

                    if (!isDragVertical || !isTargetNestedScrollingEnabled()
                            || (!isMoveWithContent && moveDistance != 0)) {
                        dellDirection(deltaY);
                    }

                    int movingX = (int) (ev.getX(pointerIndex) + 0.5f) - actionDownPointX;
                    int movingY = (int) (ev.getY(pointerIndex) + 0.5f) - actionDownPointY;
                    if (!isDragVertical && Math.abs(movingY) > touchSlop && Math.abs(movingY) > Math.abs(
                            movingX)) {
                        final ViewParent parent = getParent();
                        if (parent != null) {
                            parent.requestDisallowInterceptTouchEvent(true);
                        }

                        isDragVertical = true;
                        reDispatchMoveEventDrag(ev, deltaY);
                        lastDragEventY = (int) ev.getY(pointerIndex);
                    } else if (!isDragVertical
                            && !isDragHorizontal
                            && Math.abs(movingX) > touchSlop
                            && Math.abs(movingX) > Math.abs(movingY)) {
                        isDragHorizontal = true;
                    }

                    if (isDragVertical) {
                        // ---------- | make sure that the pullRefreshLayout is moved|----------
                        if (lastMoveDistance == 0) {
                            lastMoveDistance = moveDistance;
                        }
                        if (lastMoveDistance != moveDistance) {
                            isLayoutDragMoved = true;
                        }
                        lastMoveDistance = moveDistance;

                        reDispatchMoveEventDragging(ev, deltaY);

                        // make sure that can nested to work or the targetView is move with content
                        // dell the touch logic
                        if (!isTargetNestedScrollingEnabled() || !isMoveWithContent) {
                            if (!isMoveWithContent && isTargetNestedScrollingEnabled()) {
                                // when nested scroll the nested event is delay than this logic
                                // so we need adjust the deltaY
                                deltaY = (isDragMoveTrendDown ? -1 : 1) * Math.abs(deltaY);
                            }
                            onPreScroll(deltaY, childConsumed);
                            deltaY = parentOffsetInWindow[1] >= Math.abs(deltaY) ? 0 : deltaY;
                            onScroll(deltaY - (childConsumed[1] - lastChildConsumedY));
                            lastChildConsumedY = childConsumed[1];

                            if (!isMoveWithContent) {
                                ev.offsetLocation(0, childConsumed[1]);
                            }
                        }
                    }
                }
                lastDisallowIntercept = isDisallowIntercept;
                break;

            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = ev.getActionIndex();
                lastDragEventY = (int) ev.getY(index);
                activePointerId = ev.getPointerId(index);
                reDispatchPointDownEvent();
                break;
            }
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                lastDragEventY = (int) ev.getY(ev.findPointerIndex(activePointerId));
                reDispatchPointUpEvent(ev);
                break;

            case MotionEvent.ACTION_UP:
                dragState = 0;

                velocityTracker.computeCurrentVelocity(1000, maximumVelocity);
                float velocityY = (isDragMoveTrendDown ? 1 : -1) * Math.abs(
                        velocityTracker.getYVelocity(activePointerId));
                if (!isTargetNestedScrollingEnabled() && isDragVertical && (Math.abs(velocityY) > minimumFlingVelocity)) {
                    onPreFling(-(int) velocityY);
                }
                recycleVelocityTracker();

                reDispatchUpEvent(ev);
            case MotionEvent.ACTION_CANCEL:
                onStopScroll();

                isReDispatchMoveEvent = false;
                isDispatchTouchCancel = false;
                isDragHorizontal = false;
                isDragVertical = false;

                lastMoveDistance = 0;
                lastChildConsumedY = 0;
                childConsumed[1] = 0;
                activePointerId = -1;
                dragState = 0;
                break;
            default:
        }
        if (velocityTracker != null) {
            velocityTracker.addMovement(ev);
        }
        return dispatchSuperTouchEvent(ev);
    }

    void dellDirection(int offsetY) {
        if (offsetY < 0) {
            dragState = 1;
            isDragMoveTrendDown = true;
        } else if (offsetY > 0) {
            dragState = -1;
            isDragMoveTrendDown = false;
        }
    }

    private void recycleVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    private void reDispatchPointDownEvent() {
        if (!isMoveWithContent && isLayoutDragMoved && moveDistance == 0) {
            childConsumed[1] = 0;
            lastChildConsumedY = 0;
        }
    }

    private void reDispatchPointUpEvent(MotionEvent event) {
        if (!isMoveWithContent
                && isLayoutDragMoved
                && moveDistance == 0
                && childConsumed[1] != 0) {
            dispatchSuperTouchEvent(getReEvent(event, MotionEvent.ACTION_CANCEL));
        }
    }

    private void reDispatchMoveEventDrag(MotionEvent event, int movingY) {
        if ((!isTargetNestedScrollingEnabled() || !isMoveWithContent)
                && (movingY > 0 && moveDistance > 0 || movingY < 0 && moveDistance < 0
                || (isDragHorizontal && (moveDistance != 0
                || !isTargetAbleScrollUp() && movingY < 0
                || !isTargetAbleScrollDown() && movingY > 0)))) {
            isDispatchTouchCancel = true;
            dispatchSuperTouchEvent(getReEvent(event, MotionEvent.ACTION_CANCEL));
        }
    }

    private void reDispatchMoveEventDragging(MotionEvent event, int movingY) {
        if ((!isTargetNestedScrollingEnabled() || !isMoveWithContent)
                && isDispatchTouchCancel
                && !isReDispatchMoveEvent
                && ((movingY > 0 && moveDistance > 0 && moveDistance - movingY < 0)
                || (movingY < 0 && moveDistance < 0 && moveDistance - movingY > 0))) {
            isReDispatchMoveEvent = true;
            dispatchSuperTouchEvent(getReEvent(event, MotionEvent.ACTION_DOWN));
        }
    }

    private void reDispatchUpEvent(MotionEvent event) {
        if ((!isTargetNestedScrollingEnabled() || !isMoveWithContent)
                && isDragVertical
                && isLayoutDragMoved) {
            if (!isTargetAbleScrollDown() && !isTargetAbleScrollUp()) {
                dispatchSuperTouchEvent(getReEvent(event, MotionEvent.ACTION_CANCEL));
            } else if (targetView instanceof ViewGroup) {
                ViewGroup vp = (ViewGroup) targetView;
                for (int i = 0; i < vp.getChildCount(); i++) {
                    vp.getChildAt(i).dispatchTouchEvent(getReEvent(event, MotionEvent.ACTION_CANCEL));
                }
            }
        }
    }

    private MotionEvent getReEvent(MotionEvent event, int action) {
        MotionEvent reEvent = MotionEvent.obtain(event);
        reEvent.setAction(action);
        return reEvent;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int actionIndex = ev.getActionIndex();
        if (ev.getPointerId(actionIndex) == activePointerId) {
            final int newPointerIndex = actionIndex == 0 ? 1 : 0;
            lastDragEventY = (int) ev.getY(newPointerIndex);
            activePointerId = ev.getPointerId(newPointerIndex);

            if (velocityTracker != null) {
                velocityTracker.clear();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        for (int i = 0; i < getChildCount(); i++) {
            measureChildWithMargins(getChildAt(i), widthMeasureSpec, 0, heightMeasureSpec, 0);
        }

        if (headerView != null && refreshTriggerDistance == -1) {
            refreshTriggerDistance = headerView.getMeasuredHeight();
        }
        if (footerView != null && loadTriggerDistance == -1) {
            loadTriggerDistance = footerView.getMeasuredHeight();
        }

        if (pullDownMaxDistance == -1) {
            pullDownMaxDistance = getMeasuredHeight();
        }
        if (pullUpMaxDistance == -1) {
            pullUpMaxDistance = getMeasuredHeight();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        layoutProgress();
        layoutContentView();
    }

    public void layoutProgress() {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        if (headerView != null) {
            int headerWidth = headerView.getMeasuredWidth();
            int headerHeight = headerView.getMeasuredHeight();

            headerView.layout(width / 2 - headerWidth / 2
                    , paddingTop - headerHeight
                    , width / 2 + headerWidth / 2
                    , paddingTop);
        }

        if (footerView != null) {
            int footerWidth = footerView.getMeasuredWidth();
            int footerHeight = footerView.getMeasuredHeight();

            footerView.layout(width / 2 - footerWidth / 2
                    , paddingBottom
                    , width / 2 + footerWidth / 2
                    , paddingBottom + footerHeight);

        }
    }

    private void layoutContentView() {
        pullContentLayout.layout(getPaddingLeft()
                , getPaddingTop()
                , getPaddingLeft() + pullContentLayout.getMeasuredWidth()
                , getPaddingTop() + pullContentLayout.getMeasuredHeight());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttachWindow = true;
        handleAction();
    }

    @Override
    protected void onDetachedFromWindow() {
        isAttachWindow = false;

        cancelAllAnimation();
        abortScroller();

        startRefreshAnimator = null;
        resetHeaderAnimator = null;
        startLoadMoreAnimator = null;
        resetFooterAnimator = null;
        overScrollAnimator = null;

        delayHandleActionRunnable = null;
        super.onDetachedFromWindow();
    }

    @Override
    public void computeScroll() {
        boolean isFinish = scroller == null || !scroller.computeScrollOffset() || scroller.isFinished();
        if (!isFinish) {
            int currY = scroller.getCurrY();
            int currScrollOffset = currY - lastScrollY;
            lastScrollY = currY;

            if (scrollOver(currScrollOffset)) {
                return;
            } else if (isScrollAbleViewBackScroll && (targetView instanceof ListView)) {
                ListViewCompat.scrollListBy((ListView) targetView, currScrollOffset);
            }

            if (!isOverScrollTrigger
                    && !isTargetAbleScrollUp()
                    && currScrollOffset < 0
                    && moveDistance >= 0) {
                overScrollDell(1, currScrollOffset);
            } else if (!isOverScrollTrigger
                    && !isTargetAbleScrollDown()
                    && currScrollOffset > 0
                    && moveDistance <= 0) {
                overScrollDell(2, currScrollOffset);
            }

            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private boolean scrollOver(int currScrollOffset) {
        int flingState = overScrollFlingState();
        return (flingState == 1 || flingState == 2) && overScrollBackDell(flingState, currScrollOffset);
    }

    private boolean overScrollBackDell(int type, int tempDistance) {
        if ((type == 1 && finalScrollDistance > moveDistance * 2)
                || (type == 2 && finalScrollDistance < moveDistance * 2)) {
            cancelAllAnimation();
            if ((type == 1 && moveDistance <= tempDistance)
                    || (type == 2 && moveDistance >= tempDistance)) {
                dellScroll(-moveDistance);
                return kindsOfViewsToNormalDell(type, tempDistance);
            }
            dellScroll(-tempDistance);
            return false;
        } else {
            abortScroller();
            handleAction();
            return true;
        }
    }

    private boolean kindsOfViewsToNormalDell(int type, int tempDistance) {
        final int sign = type == 1 ? 1 : -1;
        int velocity = (int) (sign * Math.abs(scroller.getCurrVelocity()));

        if (targetView instanceof ScrollView && !isScrollAbleViewBackScroll) {
            ((ScrollView) targetView).fling(velocity);
        } else if (targetView instanceof WebView && !isScrollAbleViewBackScroll) {
            ((WebView) targetView).flingScroll(0, velocity);
        } else if (targetView instanceof RecyclerView
                && !isTargetNestedScrollingEnabled()
                && !isScrollAbleViewBackScroll) {
            ((RecyclerView) targetView).fling(0, velocity);
        } else if (targetView instanceof NestedScrollView
                && !isTargetNestedScrollingEnabled()
                && !isScrollAbleViewBackScroll) {
            ((NestedScrollView) targetView).fling(velocity);
        } else if (!canChildScrollUp(targetView)
                && !canChildScrollDown(targetView)
                || (targetView instanceof ListView && !isScrollAbleViewBackScroll)
                || targetView instanceof RecyclerView
                || targetView instanceof NestedScrollView) {
            // this case just dell overScroll normal,without any operation
        } else {
            // the target is able to scrollUp or scrollDown but have not the fling method
            // ,so dell the view just like normal view
            overScrollDell(type, tempDistance);
            return true;
        }
        isScrollAbleViewBackScroll = true;
        return false;
    }

    public boolean canChildScrollUp(View targetView) {
        if (targetView instanceof ListView) {
            return ListViewCompat.canScrollList((ListView) targetView, -1);
        }
        return targetView.canScrollVertically(-1);
    }

    public boolean canChildScrollDown(View targetView) {
        if (targetView instanceof ListView) {
            return ListViewCompat.canScrollList((ListView) targetView, 1);
        }
        return targetView.canScrollVertically(1);
    }

    public int getWindowHeight(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        }
        return displayMetrics.heightPixels;
    }

    public int dipToPx(Context context, float value) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, metrics);
    }

    private void startOverScrollAnimation(int type, int distanceMove) {
        distanceMove = type == 1 ? Math.max(-topOverScrollMaxTriggerOffset, distanceMove)
                : Math.min(bottomOverScrollMaxTriggerOffset, distanceMove);

        int finalDistance = scroller.getFinalY() - scroller.getCurrY();
        abortScroller();
        cancelAllAnimation();

        if (overScrollAnimator == null) {
            if (animationOverScrollInterpolator == null) {
                animationOverScrollInterpolator = new LinearInterpolator();
            }
            overScrollAnimator =
                    getAnimator(distanceMove, 0, overScrollAnimatorUpdate, overScrollAnimatorListener,
                            animationOverScrollInterpolator);
        } else {
            overScrollAnimator.setIntValues(distanceMove, 0);
        }
        overScrollAnimator.setDuration(getOverScrollTime(finalDistance));
        overScrollAnimator.start();
    }

    private void onTopOverScroll() {
        overScrollState = 1;
    }

    private void onBottomOverScroll() {
        overScrollState = 2;
        autoLoadingTrigger();
    }

    private void autoLoadingTrigger() {
        if (!isHoldingTrigger && onRefreshListener != null) {
            isHoldingTrigger = true;
            loadingStartAnimationListener.onAnimationEnd(null);
        }
    }

    private void readyScroller() {
        if (scroller == null) {
            if (targetView instanceof RecyclerView) {
                scroller = new OverScroller(getContext(),
                        scrollInterpolator == null ? scrollInterpolator = getRecyclerDefaultInterpolator()
                                : scrollInterpolator);
                return;
            }
            scroller = new OverScroller(getContext());
        }
    }

    private Interpolator readyMainInterpolator() {
        if (animationMainInterpolator == null) {
            animationMainInterpolator = new DecelerateInterpolator(2f);
        }
        return animationMainInterpolator;
    }

    private Interpolator getRecyclerDefaultInterpolator() {
        return new Interpolator() {
            @Override
            public float getInterpolation(float t) {
                t -= 1.0f;
                return t * t * t * t * t + 1.0f;
            }
        };
    }

    private void dellScroll(float distanceY) {
        if (distanceY == 0) {
            return;
        }
        int tempDistance = (int) (moveDistance + distanceY);
        tempDistance = Math.min(tempDistance, pullDownMaxDistance);
        tempDistance = Math.max(tempDistance, -pullUpMaxDistance);

        if (((isRefreshing() && tempDistance < 0)
                || (isLoading() && tempDistance > 0))) {
            if (moveDistance == 0) {
                return;
            }
            tempDistance = 0;
        }

        moveChildren(tempDistance);

        if (moveDistance >= 0 && headerView != null) {
            onHeaderPullChange();
            if (!isHoldingTrigger && moveDistance >= refreshTriggerDistance) {
                if (pullStateControl) {
                    pullStateControl = false;
                    onHeaderPullHoldTrigger();
                }
                return;
            }
            if (!isHoldingTrigger && !pullStateControl) {
                pullStateControl = true;
                onHeaderPullHoldUnTrigger();
            }
            return;
        }
        if (footerView == null) {
            return;
        }
        onFooterPullChange();
        if (!isHoldingTrigger && moveDistance <= -loadTriggerDistance) {
            if (pullStateControl) {
                pullStateControl = false;
                onFooterPullHoldTrigger();
            }
            return;
        }
        if (!isHoldingTrigger && !pullStateControl) {
            pullStateControl = true;
            onFooterPullHoldUnTrigger();
        }
    }

    private void overScrollDell(int type, int offset) {
        if (parentOffsetInWindow[1] != 0
                && ((!isTargetAbleScrollUp() && isTargetAbleScrollDown()) && moveDistance < 0
                || (isTargetAbleScrollUp() && !isTargetAbleScrollDown()) && moveDistance > 0)) {
            return;
        }

        if (type == 1) {
            onTopOverScroll();
        } else {
            onBottomOverScroll();
        }

        isOverScrollTrigger = true;
        startOverScrollAnimation(type, offset);
    }

    /**
     * decide on the action refresh or loadMore
     */
    private void handleAction() {
        if (headerView != null
                && !isLoading()
                && !isResetTrigger
                && moveDistance >= refreshTriggerDistance) {
            startRefresh(moveDistance, -1, true);
        } else if (footerView != null
                && !isDragMoveTrendDown
                && !isRefreshing()
                && !isResetTrigger
                && moveDistance <= -loadTriggerDistance) {
            startLoadMore(moveDistance, -1, true);
        } else if ((!isHoldingTrigger && moveDistance > 0)
                || (isRefreshing() && (moveDistance < 0 || isResetTrigger))) {
            resetHeaderView(moveDistance);
        } else if ((!isHoldingTrigger && moveDistance < 0)
                || (isLoading() && (moveDistance > 0 || isResetTrigger))) {
            resetFootView(moveDistance);
        }
    }

    private void startRefresh(int headerCurrentDistance, int toRefreshDistance,
                              final boolean withAction) {
        if (refreshTriggerDistance == -1) {
            return;
        }

        cancelAllAnimation();
        if (!isHoldingTrigger && onHeaderPullHolding()) {
            isHoldingTrigger = true;
        }
        final int refreshTriggerHeight =
                (toRefreshDistance != -1 ? toRefreshDistance : refreshTriggerDistance);
        if (headerCurrentDistance == refreshTriggerHeight) {
            refreshStartAnimationListener.onAnimationEnd(null);
            return;
        }
        if (startRefreshAnimator == null) {
            startRefreshAnimator =
                    getAnimator(headerCurrentDistance, refreshTriggerHeight, headerAnimationUpdate,
                            refreshStartAnimationListener, readyMainInterpolator());
        } else {
            startRefreshAnimator.setIntValues(headerCurrentDistance, refreshTriggerHeight);
        }
        refreshWithAction = withAction;
        startRefreshAnimator.setDuration(refreshAnimationDuring);
        startRefreshAnimator.start();
    }

    private void resetHeaderView(int headerViewHeight) {
        cancelAllAnimation();
        if (headerViewHeight == 0) {
            resetHeaderAnimationListener.onAnimationStart(null);
            resetHeaderAnimationListener.onAnimationEnd(null);
            return;
        }
        if (resetHeaderAnimator == null) {
            resetHeaderAnimator =
                    getAnimator(headerViewHeight, 0, headerAnimationUpdate, resetHeaderAnimationListener,
                            readyMainInterpolator());
        } else {
            resetHeaderAnimator.setIntValues(headerViewHeight, 0);
        }
        resetHeaderAnimator.setDuration(resetAnimationDuring);
        resetHeaderAnimator.start();
    }

    private void resetRefreshState() {
        if (isHoldingFinishTrigger) {
            onHeaderPullReset();
        }
        if (footerView != null) {
            footerView.setVisibility(VISIBLE);
        }
        resetState();
    }

    private void startLoadMore(int loadCurrentDistance, int toLoadDistance, boolean withAction) {
        if (loadTriggerDistance == -1) {
            return;
        }
        cancelAllAnimation();
        if (!isHoldingTrigger && onFooterPullHolding()) {
            isHoldingTrigger = true;
        }
        final int loadTriggerHeight = toLoadDistance != -1 ? toLoadDistance : loadTriggerDistance;
        if (loadCurrentDistance == -loadTriggerHeight) {
            loadingStartAnimationListener.onAnimationEnd(null);
            return;
        }
        if (startLoadMoreAnimator == null) {
            startLoadMoreAnimator =
                    getAnimator(loadCurrentDistance, -loadTriggerHeight, footerAnimationUpdate,
                            loadingStartAnimationListener, readyMainInterpolator());
        } else {
            startLoadMoreAnimator.setIntValues(loadCurrentDistance, -loadTriggerHeight);
        }
        refreshWithAction = withAction;
        startLoadMoreAnimator.setDuration(refreshAnimationDuring);
        startLoadMoreAnimator.start();
    }

    private void resetFootView(int loadMoreViewHeight) {
        cancelAllAnimation();
        if (loadMoreViewHeight == 0) {
            resetFooterAnimationListener.onAnimationStart(null);
            resetFooterAnimationListener.onAnimationEnd(null);
            return;
        }
        if (resetFooterAnimator == null) {
            resetFooterAnimator =
                    getAnimator(loadMoreViewHeight, 0, footerAnimationUpdate, resetFooterAnimationListener,
                            readyMainInterpolator());
        } else {
            resetFooterAnimator.setIntValues(loadMoreViewHeight, 0);
        }
        resetFooterAnimator.setDuration(resetAnimationDuring);
        resetFooterAnimator.start();
    }

    private void resetLoadMoreState() {
        if (isHoldingFinishTrigger) {
            onFooterPullReset();
        }
        if (headerView != null) {
            headerView.setVisibility(VISIBLE);
        }
        resetState();
    }

    private void resetState() {
        isHoldingFinishTrigger = false;
        isHoldingTrigger = false;
        pullStateControl = true;
        isResetTrigger = false;
        progressState = 0;
    }

    private ValueAnimator getAnimator(int firstValue, int secondValue,
                                      ValueAnimator.AnimatorUpdateListener updateListener,
                                      Animator.AnimatorListener animatorListener, Interpolator interpolator) {
        ValueAnimator animator = ValueAnimator.ofInt(firstValue, secondValue);
        animator.addUpdateListener(updateListener);
        animator.addListener(animatorListener);
        animator.setInterpolator(interpolator);
        return animator;
    }

    private void abortScroller() {
        if (scroller != null && !scroller.isFinished()) {
            scroller.abortAnimation();
        }
    }

    private void cancelAnimation(ValueAnimator animator) {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
    }

    private long getOverScrollTime(int distance) {
        float ratio = Math.abs((float) distance / getWindowHeight(getContext()));
        return Math.max(overScrollMinDuring,
                (long) ((Math.pow(2000 * ratio, 0.44)) * overScrollAdjustValue));
    }

    private void dellNestedScrollCheck() {
        View target = targetView;
        while (target != pullContentLayout) {
            if (!(target instanceof NestedScrollingChild)) {
                isTargetNested = false;
                return;
            }
            target = (View) target.getParent();
        }
        isTargetNested = (target instanceof NestedScrollingChild);
    }

    private void removeDelayRunnable() {
        if (delayHandleActionRunnable != null) {
            removeCallbacks(delayHandleActionRunnable);
        }
    }

    private Runnable getDelayHandleActionRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                if ((scroller != null
                        && scroller.isFinished()
                        && overScrollState == 0)) {
                    CustomSwipeRefreshLayout.this.handleAction();
                }
            }
        };
    }

    private void setViewFront(View secondView) {
        bringViewToFront(pullContentLayout);

        bringViewToFront(secondView);
    }

    private void bringViewToFront(View view) {
        if (view != null) {
            view.bringToFront();
        }
    }

    private boolean dellDetachComplete() {
        if (isAttachWindow) {
            return true;
        }

        isResetTrigger = true;
        isHoldingFinishTrigger = true;
        return false;
    }

    private boolean nestedAble(View target) {
        return isTargetNestedScrollingEnabled() || !(target instanceof NestedScrollingChild);
    }

    /**
     * - use by generalHelper to dell touch logic
     */
    boolean isTargetNestedScrollingEnabled() {
        return isTargetNested && ViewCompat.isNestedScrollingEnabled(targetView);
    }

    private int overScrollFlingState() {
        if (moveDistance == 0) {
            return 0;
        }
        if (!isDragMoveTrendDown) {
            return moveDistance > 0 ? 1 : -1;
        } else {
            return moveDistance < 0 ? 2 : -1;
        }
    }

    private View getRefreshView(View v) {
        LayoutParams lp = v.getLayoutParams();
        if (v.getParent() != null) {
            ((ViewGroup) v.getParent()).removeView(v);
        }
        if (lp == null) {
            lp = new LayoutParams(-1, -2);
            v.setLayoutParams(lp);
        }
        return v;
    }

    public boolean isTargetAbleScrollUp() {
        return canChildScrollUp(targetView);
    }

    public boolean isTargetAbleScrollDown() {
        return canChildScrollDown(targetView);
    }

    /**
     * state animation
     */
    private final PullAnimatorListenerAdapter resetHeaderAnimationListener =
            new PullAnimatorListenerAdapter() {
                protected void animationStart() {
                    if (isResetTrigger && isRefreshing() && !isHoldingFinishTrigger && onHeaderPullFinish(
                            isFlag())) {
                        isHoldingFinishTrigger = true;
                    }
                }

                protected void animationEnd() {
                    if (isResetTrigger) {
                        resetRefreshState();
                    }
                }
            };

    private final PullAnimatorListenerAdapter resetFooterAnimationListener =
            new PullAnimatorListenerAdapter() {
                protected void animationStart() {
                    if (isResetTrigger && isLoading() && !isHoldingFinishTrigger && onFooterPullFinish(
                            isFlag())) {
                        isHoldingFinishTrigger = true;
                    }
                }

                protected void animationEnd() {
                    if (isResetTrigger) {
                        resetLoadMoreState();
                    }
                }
            };

    private final AnimatorListenerAdapter refreshStartAnimationListener =
            new PullAnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (progressState == 0) {
                        progressState = 1;
                        if (footerView != null) {
                            footerView.setVisibility(GONE);
                        }
                        if (onRefreshListener != null && refreshWithAction) {
                            onRefreshListener.onRefresh();
                        }
                    }
                }
            };

    private final AnimatorListenerAdapter loadingStartAnimationListener =
            new PullAnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (progressState == 0) {
                        progressState = 2;
                        if (headerView != null) {
                            headerView.setVisibility(GONE);
                        }
                        if (onRefreshListener != null && refreshWithAction) {
//                            onRefreshListener.onLoading();
                        }
                    }
                }
            };

    private final AnimatorListenerAdapter overScrollAnimatorListener =
            new PullAnimatorListenerAdapter() {
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    onNestedScrollAccepted(null, null, 2);
                }

                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    handleAction();
                    onStopNestedScroll(null);
                    overScrollState = 0;
                    isOverScrollTrigger = false;
                }
            };

    /**
     * animator update listener
     */
    private final ValueAnimator.AnimatorUpdateListener headerAnimationUpdate =
            new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    CustomSwipeRefreshLayout.this.moveChildren((Integer) animation.getAnimatedValue());
                    onHeaderPullChange();
                }
            };

    private final ValueAnimator.AnimatorUpdateListener footerAnimationUpdate =
            new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    CustomSwipeRefreshLayout.this.moveChildren((Integer) animation.getAnimatedValue());
                    onFooterPullChange();
                }
            };

    private final ValueAnimator.AnimatorUpdateListener overScrollAnimatorUpdate =
            new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int offsetY = (int) ((Integer) animation.getAnimatedValue() * overScrollDampingRatio);

                    CustomSwipeRefreshLayout.this.onScrollAny(offsetY + parentOffsetInWindow[1]);
                }
            };

    void onStartScroll() {
        abortScroller();
        cancelAllAnimation();
        isScrollAbleViewBackScroll = false;
    }

    void onPreScroll(int dy, int[] consumed) {
        if (dy > 0 && moveDistance > 0) {
            if (dy > moveDistance) {
                consumed[1] += moveDistance;
                dellScroll(-moveDistance);
                return;
            }
            consumed[1] += dy;
            dellScroll(-dy);
        } else if (dy < 0 && moveDistance < 0) {
            if (dy < moveDistance) {
                consumed[1] += moveDistance;
                dellScroll(-moveDistance);
                return;
            }
            consumed[1] += dy;
            dellScroll(-dy);
        }
    }

    void onScroll(int dy) {
        if ((isDragMoveTrendDown && !isTargetAbleScrollUp())
                || (!isDragMoveTrendDown && !isTargetAbleScrollDown())) {
            onScrollAny(dy);
        }
    }

    private void onScrollAny(int dy) {
        if (dy < 0
                && dragDampingRatio < 1
                && pullDownMaxDistance > 0
                && moveDistance - dy > pullDownMaxDistance * dragDampingRatio) {
            dy = (int) (dy * (1 - (moveDistance / (float) pullDownMaxDistance)));
        } else if (dy > 0
                && dragDampingRatio < 1
                && pullUpMaxDistance > 0
                && -moveDistance + dy > pullUpMaxDistance * dragDampingRatio) {
            dy = (int) (dy * (1 - (-moveDistance / (float) pullUpMaxDistance)));
        } else {
            dy = (int) (dy * dragDampingRatio);
        }
        dellScroll(-dy);
    }

    void onStopScroll() {
        removeDelayRunnable();
        if ((overScrollFlingState() == 1 || overScrollFlingState() == 2) && !isOverScrollTrigger) {
            if (delayHandleActionRunnable == null) {
                delayHandleActionRunnable = getDelayHandleActionRunnable();
            }
            postDelayed(delayHandleActionRunnable, 50);
        } else if (scroller != null && scroller.isFinished()) {
            handleAction();
        }

        if (isLayoutDragMoved) {
            if (isRefreshing() || moveDistance > 0) {
                onHeaderPullChange();
            } else if (isLoading() || moveDistance < 0) {
                onFooterPullChange();
            }
        }
    }

    void onPreFling(float velocityY) {
        if (overScrollFlingState() != -1) {
            readyScroller();
            lastScrollY = 0;
            scroller.fling(0, 0, 0, (int) velocityY, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
            finalScrollDistance = scroller.getFinalY() - scroller.getCurrY();
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        parentHelper.onNestedScrollAccepted(child, target, axes);
        startNestedScroll(axes & ViewCompat.SCROLL_AXIS_VERTICAL);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        if (nestedAble(target)) {
            dellDirection(dy);
            if (isMoveWithContent) {
                onPreScroll(dy, consumed);
            }
            final int[] parentConsumed = parentScrollConsumed;
            if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
                consumed[0] += parentConsumed[0];
                consumed[1] += parentConsumed[1];
            }
        }
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed,
                               int dyUnconsumed) {
        if (nestedAble(target)) {
            dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                    parentOffsetInWindow);
            if (isMoveWithContent) {
                onScroll(dyUnconsumed + parentOffsetInWindow[1]);
            }
        }
    }

    @Override
    public void onStopNestedScroll(View child) {
        parentHelper.onStopNestedScroll(child);
        stopNestedScroll();
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        if (nestedAble(target)) {
            onPreFling(velocityY);
        }
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public int getNestedScrollAxes() {
        return parentHelper.getNestedScrollAxes();
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        childHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return childHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return childHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        childHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return childHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, int[] offsetInWindow) {
        return childHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return childHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return childHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    public interface OnRefreshListener {
        void onRefresh();
    }

    private class PullAnimatorListenerAdapter extends AnimatorListenerAdapter {
        private boolean flag = true;
        private boolean isCancel;

        public boolean isFlag() {
            return flag;
        }

        public void setFlag(boolean flag) {
            this.flag = flag;
        }

        public void onAnimationStart(Animator animation) {
            if (!isAttachWindow) {
                return;
            }
            animationStart();
        }

        public void onAnimationCancel(Animator animation) {
            isCancel = true;
        }

        public void onAnimationEnd(Animator animation) {
            if (!isAttachWindow) {
                return;
            }
            if (!isCancel) {
                animationEnd();
            }
            isCancel = false;
        }

        protected void animationStart() {
        }

        protected void animationEnd() {
        }
    }

    public final void moveChildren(int distance) {
        moveDistance = distance;
        if (moveDistance <= 0 && !isTargetAbleScrollDown()) {
            autoLoadingTrigger();
        }

        dellFooterMoving(moveDistance);

        dellHeaderMoving(moveDistance);

        if (isMoveWithContent) {
            pullContentLayout.setTranslationY(moveDistance);
        }
    }

    private void dellHeaderMoving(int moveDistance) {
        if (headerView != null && moveDistance >= 0) {
            headerView.setTranslationY(moveDistance <= refreshTriggerDistance ? moveDistance : refreshTriggerDistance);
        }
    }

    private void dellFooterMoving(int moveDistance) {
        if (footerView != null && moveDistance <= 0) {
            footerView.setTranslationY(moveDistance >= -loadTriggerDistance ? moveDistance : -loadTriggerDistance)
        }
    }

    public void refreshComplete(boolean flag) {
        if (dellDetachComplete() && !isLoading()) {
            isResetTrigger = true;
            resetHeaderAnimationListener.setFlag(flag);
            if (resetHeaderAnimator != null && resetHeaderAnimator.isRunning()) {
                resetHeaderAnimationListener.onAnimationStart(null);
                return;
            }
            resetHeaderView(moveDistance);
        }
    }

    public void loadMoreComplete(boolean flag) {
        if (dellDetachComplete() && !isRefreshing()) {
            isResetTrigger = true;
            resetFooterAnimationListener.setFlag(flag);
            if (resetFooterAnimator != null && resetFooterAnimator.isRunning()) {
                resetFooterAnimationListener.onAnimationStart(null);
                return;
            }
            resetFootView(moveDistance);
        }
    }


    public void autoLoading(boolean withAction, int toLoadDistance) {
        if (isHoldingTrigger) {
            return;
        }
        startLoadMore(moveDistance, toLoadDistance, withAction);
    }

    public void autoRefresh(boolean withAction, int toRefreshDistance) {
        if (!isLoading() && headerView != null) {
            cancelAllAnimation();
            resetState();
            startRefresh(moveDistance, toRefreshDistance, withAction);
        }
    }

    public void setHeaderView(ImageView header) {
        if (headerView != null && headerView != header) {
            removeView(headerView);
        }
        headerView = header;
        if (header == null) {
            return;
        }
        addView(getRefreshView(header));
        headerView.bringToFront();
    }

    public void setFooterView(ImageView footer) {
        if (footerView != null && footerView != footer) {
            removeView(footerView);
        }
        footerView = footer;
        if (footer == null) {
            return;
        }
        addView(getRefreshView(footer));
        footerView.bringToFront();
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    public boolean isRefreshing() {
        return (progressState == 0 && startRefreshAnimator != null
                && startRefreshAnimator.isRunning())
                || progressState == 1;
    }

    public boolean isLoading() {
        return (progressState == 0 && startLoadMoreAnimator != null
                && startLoadMoreAnimator.isRunning())
                || progressState == 2;
    }

    public final void cancelAllAnimation() {
        cancelAnimation(overScrollAnimator);
        cancelAnimation(startRefreshAnimator);
        cancelAnimation(resetHeaderAnimator);
        cancelAnimation(startLoadMoreAnimator);
        cancelAnimation(resetFooterAnimator);
        removeDelayRunnable();
    }
}