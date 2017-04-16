package com.cocosw.swipehelper;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.cocosw.undobar.R;

public class Container
        extends LinearLayout
        implements SwipeHelper.Callback {

    private SwipeHelper mSwipeHelper;

    public Container(Context context) {
        super(context);

        init(context);
    }

    public Container(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    private void init(Context context) {
        float densityScale = getResources().getDisplayMetrics().density;
        float pagingTouchSlop = ViewConfiguration.get(context).getScaledPagingTouchSlop();
        mSwipeHelper = new SwipeHelper(SwipeHelper.X, this, densityScale, pagingTouchSlop);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mSwipeHelper.onInterceptTouchEvent(ev) ||
                super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mSwipeHelper.onTouchEvent(ev) ||
                super.onTouchEvent(ev);
    }

    @Override
    public View getChildAtPosition(MotionEvent ev) {
        return findViewById(R.id._undobar);
    }

    @Override
    public View getChildContentView(View v) {
        return v;
    }

    @Override
    public boolean canChildBeDismissed(View v) {
        return true;
    }

    @Override
    public void onBeginDrag(View v) {
        requestDisallowInterceptTouchEvent(true);
    }

    @Override
    public void onChildDismissed(View v) {
        ViewGroup grandParent = (ViewGroup) v.getParent().getParent();
        grandParent.removeView((View) v.getParent()); // workaround. not perfect
    }

    @Override
    public void onDragCancelled(View v) {
    }
}
