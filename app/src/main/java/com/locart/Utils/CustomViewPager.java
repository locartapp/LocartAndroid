package com.locart.Utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import androidx.viewpager.widget.ViewPager;

import java.lang.reflect.Field;

public class CustomViewPager extends ViewPager {
    private boolean swipeAble = false;

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /* Call this method in your motion events when you want to disable or enable
        It should work as desired */
    public void setSwipeAble(boolean swipeAble) {
        this.swipeAble = swipeAble;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        return (this.swipeAble) && super.onInterceptTouchEvent(arg0);
    }

}
