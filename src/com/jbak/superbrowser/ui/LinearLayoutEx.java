package com.jbak.superbrowser.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class LinearLayoutEx extends LinearLayout {

private int mMaxWidth = Integer.MAX_VALUE;
private int mMaxHeight = Integer.MAX_VALUE;

public LinearLayoutEx(Context context) {

    super(context);
}

public LinearLayoutEx(Context context, AttributeSet attrs) {
    super(context, attrs);
}
public void setMaxWidth(int maxWidth)
{
	mMaxWidth = maxWidth;
}
public void setMaxHeight(int maxHeight)
{
	mMaxHeight = maxHeight;
}
@Override
protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    //get measured height
    int h = getMeasuredHeight();
    int w = getMeasuredWidth();
    boolean changed = false;
    if(getMeasuredWidth() > mMaxWidth){
    	w = mMaxWidth;
    	changed = true;
    }
    if(getMeasuredHeight() > mMaxHeight){
    	h = mMaxHeight;
    	changed = true;
    }
    if(changed)
    	setMeasuredDimension(w, h);
}

public void setMinAndMaxWidth(int minWidth, int maxWidth) {
	setMaxWidth(maxWidth);
	setMinimumWidth(minWidth);
}
}