package com.zzm.play.self_view.recyclerview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.zzm.play.utils.l;

import java.util.ArrayList;

public class RecyclerView extends ViewGroup {


    public RecyclerView(Context context) {
        super(context);
    }

    public RecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }


    //系统滑动最小距离
    private int touchSlop;

    private void init(Context context, AttributeSet attrs) {
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        touchSlop = viewConfiguration.getScaledTouchSlop();
    }

    private Adapter adapter;

    private RecyclerViewPool recyclerViewPool;

    //recyclerview 宽高
    private int width;
    private int height;

    //保存每个item view 的宽高
    private int heights[];

    //屏幕的上可见的第一个item view 的位置
    private int firstRow;

    //保存屏幕可见的item view
    private ArrayList<ViewHolder> viewLists;

    //可见的第一个item　距离屏幕顶端的距离
    private int scrollY;

    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;

        if (null != adapter) {
            recyclerViewPool = new RecyclerViewPool();

            firstRow = 0;
            viewLists = new ArrayList<>();
            scrollY = 0;
            heights = new int[adapter.getItemCount()];
            for (int i = 0; i < adapter.getItemCount(); i++) {
                heights[i] = adapter.getHeight(i);
            }

            //此时有数据了需要新测量布局绘制
            requestLayout();
        }
    }

    private boolean needLayout = true;
    private int rowCount;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed || needLayout) {
            needLayout = false;
            viewLists.clear();
            removeAllViews();
            if (null != adapter) {
                width = r - l;
                height = b - t;
                int left = 0, top = 0, right = width, bottom;
                top = -scrollY;
                rowCount = adapter.getItemCount();
                for (int i = firstRow; i < rowCount && top < height; i++) {
                    bottom = heights[i] + top;
                    ViewHolder viewHolder = makeViewAndPrepare(i, left, top, right, bottom);
                    viewLists.add(viewHolder);
                    top = bottom;
                }
            }
        }
    }

    private ViewHolder makeViewAndPrepare(int row, int left, int top, int right, int bottom) {
        //生成view holder 并布局
        ViewHolder viewHolder = obtainView(row, right - left, bottom - top);
        viewHolder.itemView.layout(left, top, right, bottom);
        return viewHolder;
    }

    private ViewHolder obtainView(int row, int width, int height) {
        int type = adapter.getItemViewType(row);
        //先从回收池里面去拿 如果没有通过adapter生成
        ViewHolder viewHolder = recyclerViewPool.getRecycledView(type);
        if (null == viewHolder) {
            viewHolder = adapter.onCreateViewHolder(this, type);
        }

        //更新内容
        adapter.onBindViewHolder(viewHolder, row);

        viewHolder.setItemViewType(type);

        //测量
        viewHolder.itemView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));

        addView(viewHolder.itemView, 0);
        return viewHolder;
    }


    // 滑动开始的位置Y值
    private int y0;


    //该方法调用取决于：
    //if (actionMasked == MotionEvent.ACTION_DOWN || mFirstTouchTarget != null)
    //ACTION_DOWN来的时候会执行，mFirstTouchTarget是否有子控件消费了事件，当子控件为clickable
    //默认消费此事件
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        l.i("onInterceptTouchEvent: " + event.getAction());

        boolean intercept = false;

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                y0 = (int) event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                int y1 = (int) event.getRawY();
                //如果滑动距离大于最小滑动距离就拦截
                if (Math.abs(y1 - y0) > touchSlop)
                    intercept = true;
                break;

        }
        l.i("拦截 ：" + intercept + "  touchSlop :" + touchSlop + "  Math.abs(y1 - y0): " + Math.abs((int) event.getRawY() - y0));

        return intercept;
    }

    //上面和下面两个重写方法保证能正常处理事件
    //1.当子view消费了事件（clickable && return true）此时要onInterceptTouchEvent拦截下来给到此view处理
    //
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        l.i("onTouchEvent : " + event.getAction());

        int scrollDistance = 0;

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                y0 = (int) event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:

                scrollDistance = (int) (event.getRawY() - y0);
                l.i("滑动距离：" + scrollDistance);

                //如果滑动距离大于最小滑动距离就进行操作
                if (Math.abs(scrollDistance) > touchSlop)
                    scrollBy(0, scrollDistance);
                break;
        }
        return true;
    }


    //原函数是移动和画布
    @Override
    public void scrollBy(int x, int y) {

    }

    interface Adapter<T extends ViewHolder> {

        T onCreateViewHolder(ViewGroup parent, int viewType);

        void onBindViewHolder(T t, int position);

        int getItemViewType(int position);

        int getHeight(int index);

        int getItemCount();

    }

}
