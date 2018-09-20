package com.jbak.superbrowser.recycleview;

import android.support.v7.widget.RecyclerView.Adapter;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;

import com.jbak.superbrowser.ActArray;
import com.jbak.superbrowser.adapters.BookmarkAdapter;
import com.jbak.superbrowser.ui.PanelButton;
import com.jbak.superbrowser.ui.themes.MyTheme;

public class PanelButtonRecyclerAdapter extends Adapter<PanelButtonHolder> {
	ActArray mActions;
	int mButtonType;
	OnClickListener mClickListener;
	OnLongClickListener mLongClickListener;
	protected BookmarkAdapter mBa;
	int mCurItem;
	boolean mTransparentButtons = false;
	public PanelButtonRecyclerAdapter(ActArray ar,int buttonType)
	{
		mActions = ar;
		mButtonType = buttonType;
	}
	public PanelButtonRecyclerAdapter(BookmarkAdapter ba,int buttonType)
	{
		mActions = null;
		mBa = ba;
		mButtonType = buttonType;
		if(mBa!=null)
			mBa.setParentAdapter(this);
	}
	public void setActions(ActArray ar)
	{
		mActions = ar;
		notifyDataSetChanged();
	}
	@Override
	public int getItemCount() {
		if(mBa!=null)
			return mBa.getItemsCount();
		return mActions.size();
	}

	@Override
	public void onBindViewHolder(PanelButtonHolder vh, int pos) {
		PanelButton pb = (PanelButton)vh.itemView;
		MyTheme.get().setPanelButton(pb,pos,mTransparentButtons);
		if(mBa!=null)
			mBa.processView(pos, pb, false);
		else
			pb.setAction(mActions.get(pos));
		onPostSetItem(pos, pb, pb.getTag());
	}
	public void setTransparentButtons(boolean transparent)
	{
		mTransparentButtons = transparent;
	}
	public void onPostSetItem(int pos,PanelButton pb,Object tag)
	{
		
	}
	@Override
	public PanelButtonHolder onCreateViewHolder(ViewGroup vg, int pos) {
		PanelButton pb = new PanelButton(vg.getContext(), mButtonType);
		pb.setOnClickListener(mClickListener);
		pb.setOnLongClickListener(mLongClickListener);
		return new PanelButtonHolder(pb);
	}
	public final void setOnClickLisener(OnClickListener listener)
	{
		mClickListener = listener;
	}
	public final void setOnLongClickListener(OnLongClickListener listener)
	{
		mLongClickListener = listener;
	}
	public final ActArray getActions()
	{
		return mActions;
	}
}
