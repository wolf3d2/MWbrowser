package com.jbak.superbrowser.recycleview;

import com.jbak.superbrowser.adapters.BookmarkAdapter;
import com.jbak.superbrowser.ui.BookmarkView;

import android.support.v7.widget.RecyclerView.Adapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;

public class BookmarkViewRecyclerAdapter extends Adapter<BookmarkViewHolder> implements OnClickListener,OnLongClickListener {
	BookmarkAdapter mBa;
	OnClickListener mClickListener;
	OnLongClickListener mLongClickListener;
	int mViewType = BookmarkView.TYPE_DEFAULT;
	public BookmarkViewRecyclerAdapter(BookmarkAdapter adapter) {
		mBa = adapter;
		mBa.setParentAdapter(this);
	}
	@Override
	public int getItemCount() {
		return mBa.getCount();
	}
	@Override
	public void onBindViewHolder(BookmarkViewHolder vh, int pos) {
		if (vh!=null)
			mBa.getView(pos, vh.itemView, null);
	}
	@Override
	public BookmarkViewHolder onCreateViewHolder(ViewGroup vg, int pos) {
		BookmarkView bv = (BookmarkView) mBa.getView(pos, null, null);
		bv.setOnClickListener(this);
		bv.setOnLongClickListener(this);
		return new BookmarkViewHolder(bv);
	}
	public final BookmarkAdapter getBookmarkAdapter()
	{
		return mBa;
	}
	@Override
	public boolean onLongClick(View v) {
		if(mLongClickListener!=null)
			return mLongClickListener.onLongClick(v);
		return false;
	}
	@Override
	public void onClick(View v) {	
		if(mClickListener!=null)
			mClickListener.onClick(v);
	}
	public void setOnClickListener(OnClickListener listener)
	{
		mClickListener = listener;
	}
	public void setOnLongClickListener(OnLongClickListener listener)
	{
		mLongClickListener = listener;
	}
}
