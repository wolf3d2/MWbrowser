package com.jbak.superbrowser.recycleview;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.State;
import android.view.View;

public class MyItemDecoration extends ItemDecoration {
	int mBottom=0;
	boolean mGridLastRowAlign = false;
	public MyItemDecoration(int bottom)
	{
		mBottom = bottom;
	}
	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent,State state) {
		outRect.bottom = mBottom;
	}
	
}
