package com.zd.launcher.mylauncher;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class MyWidgetHost extends ViewGroup {
    private int[] cellInfo = new int[2];

    private OnLongClickListener mLongClickListener;

    public MyWidgetHost(Context context) {
        this(context, null);
    }

    public MyWidgetHost(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyWidgetHost(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // @1:当长按的时候触发该动作，记录长按的位置
    public boolean dispatchTouchEvent(MotionEvent event) {
        cellInfo[0] = (int) event.getX();
        cellInfo[1] = (int) event.getY();

        Log.e("event:", cellInfo[0] + "," + cellInfo[1]);
        return false;//super.dispatchTouchEvent(event);
    }

    //当用户选择了某个widget时，触发这个动作，将其所选的widget(child)添加到桌面上
    public void addInScreen(View child, int width, int height) {
        LayoutParams params = new LayoutParams(width, height);
        params.x = cellInfo[0];
        params.y = cellInfo[1];
        // params.width = width
        child.setOnLongClickListener(mLongClickListener);

        Log.e("size", "x,y,width,height" + params.x + "," + params.y + ","
                + params.width + "," + params.height);

        addView(child, params);
    }

    //测量每个孩子的宽度和高度
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int count = getChildCount();
        LayoutParams lp = null;

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            lp = (LayoutParams) child.getLayoutParams();
            Log.e("onMeasure:w,h", lp.width + "," + lp.height);
            child.measure(
                    MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY));
        }

        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec));
    }

    //将每个孩子按照其layoutparams中定义的进行布局
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        LayoutParams lp = null;

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            lp = (LayoutParams) child.getLayoutParams();
            child.layout(lp.x, lp.y, lp.x + lp.width, lp.y + lp.height);
        }
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {

        int x;

        int y;

        public LayoutParams(int width, int height) {
            super(width, height);
            this.width = width;
            this.height = height;
        }

    }

}