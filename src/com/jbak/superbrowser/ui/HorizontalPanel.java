package com.jbak.superbrowser.ui;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.jbak.superbrowser.ActArray;
import com.jbak.superbrowser.Action;
import com.jbak.superbrowser.MainActivity;
import com.mw.superbrowser.R;
import com.jbak.superbrowser.adapters.BookmarkAdapter;
import com.jbak.superbrowser.recycleview.PanelButtonRecyclerAdapter;
import com.jbak.superbrowser.recycleview.RecyclerViewEx;

public class HorizontalPanel extends RecyclerViewEx implements OnClickListener,OnLongClickListener 
{
	private boolean mCheckWidthWhileNotAutoFill = true;
	ActArray mActions;
	protected LinearLayout mContainer;
	HorizontalScrollView mScrollView;
	OnAction mOnActionListener;
	OnLongClickListener mLongClickListener;
	int mButtonType = PanelButton.TYPE_BUTTON_NORMAL;
	LayoutManager mLayoutManager;
	PanelButtonRecyclerAdapter mAdapter;
	/** Подстраивать ширину кнопок в горизонтальном режиме, если не используется автораспределение по ширине */
	public HorizontalPanel(Context context) {
		super(context,TYPE_HORZ_LINEAR);
	}
	public HorizontalPanel(Context context,AttributeSet ats) {
		super(context,ats);
	}
	@Override
	protected void init()
	{
		super.init();
		setButtonsType(mButtonType);
		
	}
	@Override
	public int getItemWidth() {
		return PanelButton.getMinWidth(getContext(), mButtonType);
	}
	public void setPanelButton(int pos,Action action,PanelButton pb)
	{
		
	}
	public void setButtonsType(int type)
	{
		mButtonType = type;
		setMaxHeight(PanelButton.getPanelButtonHeight(getContext(), null, type));
	}
	public PanelButtonRecyclerAdapter createPanelButtonAdapter(BookmarkAdapter ba,int buttonType)
	{
		PanelButtonRecyclerAdapter pba = new PanelButtonRecyclerAdapter(ba, buttonType);
		return pba;
	}
	public PanelButtonRecyclerAdapter setRecyclerAdapter(PanelButtonRecyclerAdapter adapt)
	{
		mAdapter = adapt;
		adapt.setOnClickLisener(this);
		adapt.setOnLongClickListener(this);
		swapAdapter(adapt, true);
		//setAdapter(adapt);
		setLayoutManager(getFillHorizontal());
		return adapt;
	}
	public PanelButtonRecyclerAdapter setBookmarks(BookmarkAdapter ba)
	{
		PanelButtonRecyclerAdapter adapt = createPanelButtonAdapter(ba, mButtonType);
		return setRecyclerAdapter(adapt);
	}
	public PanelButtonRecyclerAdapter createRecyclerAdapterForActions(ActArray actions,int buttonType)
	{
		return new PanelButtonRecyclerAdapter(actions, mButtonType);
	}
	public void setActions(ActArray actions)
	{
		PanelButtonRecyclerAdapter adapt = createRecyclerAdapterForActions(actions, mButtonType);
		mAdapter = adapt;
		adapt.setOnLongClickListener(this);
		adapt.setOnClickLisener(this);
		setAdapter(adapt);
		setLayoutManager(getFillHorizontal());
	}
	@Override
	public void onClick(View v) {
		Action a = ((PanelButton)v).getAction();
		if(mOnActionListener!=null)
		{
			mOnActionListener.onAction(a);
		}
		else if(getContext() instanceof MainActivity)
		{
			((MainActivity)getContext()).hideRoundButton();
			((MainActivity)getContext()).runAction(a);
		}
	}
	public void setOnActionListener(OnAction listener)
	{
		mOnActionListener = listener;
	}
	public void setItemLongClickListener(OnLongClickListener listener)
	{
		mLongClickListener = listener;
	}
	int getVisibleChildCount()
	{
		int count = 0;
		for(int i=mContainer.getChildCount()-1;i>=0;i--)
		{
			View v = mContainer.getChildAt(i);
			if(v!=null&&v.getVisibility()==VISIBLE)
				count++;
		}
		return count;
	}
	public void forceUpdate()
	{
		if(getAdapter()!=null)
			getAdapter().notifyDataSetChanged();
	}
	public PanelButtonRecyclerAdapter getRealAdapter()
	{
		return mAdapter;
	}
	public MainActivity getMainActivity()
	{
		return (MainActivity)getContext();
	}
	@Override
	public boolean onLongClick(View v) {
		if(mLongClickListener!=null)
			return mLongClickListener.onLongClick(v);
		return false;
	}
	public void setCheckWidthWhileNotAutoFill(boolean set)
	{
		mCheckWidthWhileNotAutoFill = set;
	}
	protected void checkLP(android.support.v7.widget.RecyclerView.LayoutParams lp)
	{
		if(mType==TYPE_GRID)
			lp.height = (int) getResources().getDimension(R.dimen.panelButtonSize);
		else
		{
			int h = getMaxHeight();
			if(h>0&&h<Integer.MAX_VALUE)
				lp.height = h;
			if(!mFill&&mCheckWidthWhileNotAutoFill)
			{
				int w = getItemWidth();
				if(w>0)
					lp.width = getItemWidth();
			}
		}
	}
	@Override
	protected GridLayoutManager createGridLayoutManager(int spans, boolean fill) {
		return new GridLayoutManager(getContext(), spans,VERTICAL, mReverseLayout)
		{
			@Override
			public boolean checkLayoutParams(
					android.support.v7.widget.RecyclerView.LayoutParams lp) {
				checkLP(lp);
				return super.checkLayoutParams(lp);
			}
		};
	}
	@Override
	protected LinearLayoutManager createLinearLayoutManager(int orientation,boolean fill) {
		return new LinearLayoutManager(getContext(), orientation, mReverseLayout)
		{
			@Override
			public boolean checkLayoutParams(LayoutParams lp) {
				checkLP(lp);
				return super.checkLayoutParams(lp);
			}
		};
	}

}
