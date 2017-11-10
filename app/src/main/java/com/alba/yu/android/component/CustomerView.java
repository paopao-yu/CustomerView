package com.alba.yu.android.component;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Alba Yu
 * on 2017/07/27.
 */
public class CustomerView extends ScrollView {

    public static final String UP = "UP";
    public static final String DOWN = "DOWN";

    private ViewGroup mTopContainers;

    private ViewGroup mTopBigContainers;

    private ViewGroup mTopMinContainers;

    private LinearLayout mEntryContainers;

    private LinearLayout mMiddleContainer;

    private OnScrollListener mCollapseListener;

    /**
     * 标志，是否已经测量过一些初始化Ｙ信息
     */
    private boolean mMeasureYFlag;

    /**
     * 滑动所在最小Y
     */
    private float mTopY;
    /**
     * 中间view所在的Y
     */
    private float mMidY;
    /**
     * 顶部原始高度
     */
    private int mTopHeight;
    /**
     * 入口view所在的Y
     */
    private float mBottomY;
    /**
     * 变换过程滑动的高度
     */
    private float mAlphaHeight;

    public CustomerView(@NonNull Context context) {
        this(context, null);
    }

    public CustomerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setClickable(true);
        LayoutInflater.from(getContext()).inflate(R.layout.item_customer_view, this);
        mTopContainers = findViewById(R.id.top_container);
        mTopBigContainers = findViewById(R.id.top_container_big);
        mTopMinContainers = findViewById(R.id.top_container_min);
        mEntryContainers = findViewById(R.id.entry_layout);
        mMiddleContainer = findViewById(R.id.middle_container);
    }

    /**
     * 测量一些折叠时需要的Ｙ坐标, 只在加载时测量一次
     */
    private void measureY() {
        if (!mMeasureYFlag) {
            mMeasureYFlag = true;

            mTopHeight = mTopContainers.getHeight();
            mMidY = mMiddleContainer.getY();
            mBottomY = mEntryContainers.getY();

            // 滑动过程最高Y，mTopBigContainers - mTopMinContainers
            mTopY = mMidY - dipToPixel(getContext(), 188 - 60);
            // 变换过程滑动的高度
            mAlphaHeight = mBottomY - mTopY;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        measureY();
    }

    /**
     * TODO 增加动画？
     * 展开
     */
    public boolean expand() {
        if (checksScrollable(DOWN)) {
            float curY = mEntryContainers.getY();
            float dy = curY - mBottomY;
            scroll((int) dy);
            return true;
        }
        return false;
    }

    /**
     * 折叠
     */
    public boolean collapse() {
        if (checksScrollable(UP)) {
            float curY = mEntryContainers.getY();
            float dy = curY - mTopY;
            scroll((int) dy);
            return true;
        }
        return false;
    }

    /**
     * 滚动
     *
     * @param dy dy　负值向下，正值向上
     * @return true 需要折叠　false无法折叠
     */
    public synchronized boolean scroll(int dy) {
        float curY = mEntryContainers.getY();
        String direction = (dy < 0) ? DOWN : UP;
        if (!checksScrollable(direction)) {
            return false;
        }

        // 根据上下边界，获取可真实移动距离
        dy = getRealY(curY, dy);
        // 移动底部
        LayoutParams layoutParams = (LayoutParams) mEntryContainers.getLayoutParams();
        layoutParams.topMargin = mEntryContainers.getTop() - dy;
        mEntryContainers.setLayoutParams(layoutParams);


        // 获取移动底部后的底部Y坐标
        curY -= dy;

        // 根据底部Y坐标，变动其他children
        scrollOtherChildren(curY);

        if (null != mCollapseListener) {
            mCollapseListener.onScroll(dy);
        }
        return true;
    }

    /**
     * 检测是否可折叠
     *
     * @param direction 方向
     * @return
     */
    private boolean checksScrollable(@DIRECTION final String direction) {
        float curY = mEntryContainers.getY();

        // 已经移到顶部或底部，不可滚动
        switch (direction) {
            case UP:
                return curY > mTopY;
            case DOWN:
                return curY < mBottomY;
            default:
                break;
        }
        return false;
    }

    /**
     * 移动底部，控制其移动范围，不能超过mTopY～mBottomY
     *
     * @param curY
     * @param dy
     */
    private int getRealY(float curY, int dy) {
        float tempY = curY - dy;
        if (tempY > mBottomY) {
            dy = (int) (curY - mBottomY);
        } else if (tempY < mTopY) {
            dy = (int) (curY - mTopY);
        }

        return dy;
    }

    /**
     * 根据底部Ｙ轴，变动top和mid
     *
     * @param curY
     */
    private void scrollOtherChildren(float curY) {
        if (curY <= mMidY) {
            mMiddleContainer.setVisibility(GONE);

            // mTopContainers跟着移动
            LayoutParams layoutParams = (LayoutParams) mTopContainers.getLayoutParams();
            layoutParams.height = (int) curY;
            mTopContainers.setLayoutParams(layoutParams);
        } else {
            // Reset
            mMiddleContainer.setVisibility(VISIBLE);

            LayoutParams layoutParams = (LayoutParams) mTopContainers.getLayoutParams();
            layoutParams.height = mTopHeight;
            mTopContainers.setLayoutParams(layoutParams);
        }

        // 整体高度调整
        ViewGroup.LayoutParams mainLayoutParams = getLayoutParams();
        mainLayoutParams.height = mEntryContainers.getHeight() + (int) curY;
        this.setLayoutParams(mainLayoutParams);

        // 变换顶部 Alpha 0~1
        float alpha = (curY - mTopY) / mAlphaHeight;
        mTopMinContainers.setAlpha(1 - alpha);
        mTopBigContainers.setAlpha(alpha);
    }

    public void setOnCollapseListener(OnScrollListener clickListener) {
        this.mCollapseListener = clickListener;
    }

    public interface OnScrollListener {

        void onScroll(int dy);
    }

    @StringDef({UP, DOWN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DIRECTION {
    }

    //将dip(dp)转换成pixel
    private int dipToPixel(Context context, float dipValue) {
        if (context == null) {
            return 0;
        }
        float density = context.getResources().getDisplayMetrics().density;
        int pixelValue = (int) (dipValue * density + 0.5f);
        return pixelValue;
    }

}
