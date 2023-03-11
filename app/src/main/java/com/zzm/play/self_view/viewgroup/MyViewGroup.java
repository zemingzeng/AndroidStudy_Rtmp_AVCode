package com.zzm.play.self_view.viewgroup;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class MyViewGroup extends ViewGroup {
    public MyViewGroup(Context context) {
        super(context);
    }

    public MyViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {

        return new MarginLayoutParams(getContext(), attrs);

    }



    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int childCount = getChildCount();
        int top = t, right = 0, bottom = 0;

        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);

            right = child.getMeasuredWidth();
            bottom += child.getMeasuredHeight();

            com.zzm.play.utils.l.i("left: " + l + " top :" + top + " right: " + right + " bottom:  " + bottom);
            child.layout(l, top, right, bottom);

            top += child.getMeasuredHeight();
        }
    }
}
