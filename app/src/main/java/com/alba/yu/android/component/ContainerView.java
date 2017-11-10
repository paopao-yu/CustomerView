package com.alba.yu.android.component;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

/**
 * Created by Alba Yu
 * on 2017/08/22.
 */

public class ContainerView extends LinearLayout {

    private CustomerView mCustomerView;
    private RecyclerView mMoreRecyclerView;
    private View mKnob;
    private GestureDetector mDetector;

    /**
     * 屏幕宽度
     */
    private int mScreenWidth;
    /**
     * 把手目前移动状态
     */
    private int mDrawMoveStatus;
    /**
     * 原点，未移动
     */
    private static int STATUS_ORIGIN = 0;
    /**
     * 移动中
     */
    private static int STATUS_MOVING = 1;
    /**
     * 移动完隐藏一半
     */
    private static int STATUS_INSIDE = 2;

    /**
     * 动画完成时间
     */
    private long ANIMATION_DURATION = 500;

    public ContainerView(Context context) {
        this(context, null);
    }

    public ContainerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClickable(true);
        mDetector = new GestureDetector(getContext(), new YScrollDetector());
        mScreenWidth = getDisplayWidth(context);

    }

    /**
     * 因为分发事件时，需要相关view的滑动能力
     *
     * @param customerView
     * @param loadMoreRecyclerView
     */
    public void setRelevantViews(CustomerView customerView, RecyclerView loadMoreRecyclerView) {
        this.mCustomerView = customerView;
        this.mMoreRecyclerView = loadMoreRecyclerView;
    }

    /**
     * 设置图标，需要随着页面滚动动画
     *
     * @param knob
     */
    public void setKnob(View knob) {
        this.mKnob = knob;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            if (mDrawMoveStatus != STATUS_INSIDE) {
                // 等待500毫秒
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        moveKnobBack();
                    }
                }, ANIMATION_DURATION);
            } else {
                moveKnobBack();
            }
        }

        // 拦截事件
        if (mDetector.onTouchEvent(ev)) {
            ev.setAction(MotionEvent.ACTION_CANCEL);
        }
        return super.dispatchTouchEvent(ev);
    }

    class YScrollDetector extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // 移动
            moveKnobInside();

            if (distanceY > 0) {
                // 往上滑
                boolean consumeFlag = mCustomerView.scroll((int) distanceY);
                if (consumeFlag) {
                    return true;
                }
            } else {
                // 往下滑, 先判断列表是否可滑
                if (null != e1 && e1.getRawY() < getRecyclerViewY()) {
                    // 若开始的手指没有点在列表上,先让mCustomerView处理
                    boolean consumeFlag = mCustomerView.scroll((int) distanceY);
                    if (consumeFlag) {
                        return true;
                    }
                }

                boolean canScrollVertically = mMoreRecyclerView.canScrollVertically(-1);
                if (!canScrollVertically) {
                    // 不可滑，才滑动mCustomerView
                    boolean consumeFlag = mCustomerView.scroll((int) distanceY);
                    if (consumeFlag) {
                        return true;
                    }
                }
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (velocityY < 0) {
                // 往上滑
                mCustomerView.collapse();
            } else {
                // 往下滑, 先判断列表是否可滑
                boolean canScrollVertically = mMoreRecyclerView.canScrollVertically(-1);
                if (!canScrollVertically) {
                    mCustomerView.expand();
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return super.onTouchEvent(event);
    }

    /**
     * 获取RecyclerView　Y坐标
     *
     * @return
     */
    private int getRecyclerViewY() {
        final int[] location = new int[2];
        mMoreRecyclerView.getLocationOnScreen(location);
        return location[1];
    }

    /**
     * 把手平移X的距离，目前是展示40%
     */
    private int getDrawMoveX() {
        if (null == mKnob) {
            return 0;
        }
        int drawMoveX = mScreenWidth - (int) (mKnob.getWidth() * 0.4) - (int) mKnob.getX();
        Log.i("", "drawMoveX " + drawMoveX);
        return drawMoveX;
    }

    /**
     * 移动把手隐藏一大半
     */
    private void moveKnobInside() {
        if (null != mKnob && mDrawMoveStatus == STATUS_ORIGIN) {
            int drawMoveX = getDrawMoveX();
            TranslateAnimation animation = new TranslateAnimation(0, drawMoveX, 0, 0);
            animation.setFillAfter(true);// 移动后不返回
            animation.setDuration(ANIMATION_DURATION);//设置动画持续时间为500毫秒
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    mDrawMoveStatus = STATUS_MOVING;
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mDrawMoveStatus = STATUS_INSIDE;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mKnob.startAnimation(animation);
        }
    }

    /**
     * 移动把手回退到原点
     */
    private void moveKnobBack() {
        if (null != mKnob && mDrawMoveStatus == STATUS_INSIDE) {
            int drawMoveX = getDrawMoveX();
            TranslateAnimation animation = new TranslateAnimation(drawMoveX, 0, 0, 0);
            animation.setFillAfter(true);// 移动后不返回
            animation.setDuration(ANIMATION_DURATION);//设置动画持续时间为500毫秒
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    mDrawMoveStatus = STATUS_MOVING;
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mDrawMoveStatus = STATUS_ORIGIN;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mKnob.startAnimation(animation);
        }
    }

    private int getDisplayWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.widthPixels;
    }

}
