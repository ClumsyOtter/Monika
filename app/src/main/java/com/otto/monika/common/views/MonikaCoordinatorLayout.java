package com.otto.monika.common.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

public class MonikaCoordinatorLayout extends CoordinatorLayout {
    private OnInterceptTouchListener mListener;

    public void setOnInterceptTouchListener(OnInterceptTouchListener listener) {
        mListener = listener;
    }

    public MonikaCoordinatorLayout(Context context) {
        super(context);
    }

    public MonikaCoordinatorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MonikaCoordinatorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mListener != null) {
            mListener.onIntercept(ev);
        }
        return super.onInterceptTouchEvent(ev);
    }


    public interface OnInterceptTouchListener {
        void onIntercept(MotionEvent ev);
    }
}
