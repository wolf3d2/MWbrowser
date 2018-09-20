package com.jbak.superbrowser;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;

import com.jbak.superbrowser.adapters.BookmarkAdapter;
import com.jbak.superbrowser.ui.BookmarkView;
import com.jbak.superbrowser.ui.LoadBitmapInfo;
import com.jbak.superbrowser.ui.BookmarkView.OnCloseListener;

public abstract class WindowsAdapter extends BookmarkAdapter implements OnBitmapLoadListener,OnCloseListener
{
	TabList mWindows;
	public WindowsAdapter(Context с, TabList ww) {
		super(с);
		mWindows = ww;
		mAutoLoadImages = !mWindows.isTempSession();
	}
	public WindowsAdapter(MainActivity act) {
		super(act);
		mWindows = act.getTabList();
		mAutoLoadImages = !mWindows.isTempSession();
	}
	void setImage(View child,Bitmap bmp)
	{
		BookmarkView bv = (BookmarkView)child;
		bv.setThumbnail(bmp);
	}
	public abstract void updateItems();
	@Override
	public void onClose(BookmarkView bw) {
		Tab ww = (Tab)bw.getTag();
//			setResult(RESULT_OK, new Intent().putExtra(EXTRA_CUR_WINDOW_ID, ww.windowId).putExtra(EXTRA_CLOSE, true));
//			onBackPressed();
		//!!!
		BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_ACTION, Action.create(Action.CLOSE_TAB, ww.windowId));
		updateItems();
	}
//	@Override
//	public Bookmark getBookmarkFromCursor(Cursor c) {
//		tempWnd = Db.getWindowTable().loadWindowFromCursor(BrowserApp.INSTANCE, getCursor(),false);
//		Bookmark bm = tempWnd.getCurBookmark();
//		return bm;
//	}
	@Override
	public void setBookmarkView(BookmarkView view, Bookmark bm) {
		super.setBookmarkView(view, bm);
		view.setOnCloseListener(this);
	}
	@Override
	public Object getItemTag(int pos, Bookmark bm) {
		return mWindows.getWindowAt(pos, false);
	}
	@Override
	public Bookmark getBookmark(int pos) {
		Tab ww = mWindows.getWindowAt(pos, false);
		Bookmark bm = ww.getCurBookmark();
		return bm;
	}
	@Override
	public int getItemsCount() {
		return mWindows.getCount();
	}
	public void setWebWindows(TabList ww)
	{
		mWindows = ww;
		notifyDataSetChanged();
	}
	public final TabList getWindows()
	{
		return mWindows;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		return super.getView(position, convertView, arg2);
	}
	@Override
	protected BitmapLoader createBitmapLoader(int pos, Bookmark bm,LoadBitmapInfo li) {
		return new BitmapLoader(getContext().getContentResolver(), this, li)
		{
			@Override
			protected Cursor getBitmapCursor(LoadBitmapInfo info) {
				if(info.param instanceof Tab)
				{
					Tab ww = (Tab)info.param;
					Cursor c =  Db.getWindowTable().select().columns(IConst.FAVICON,IConst.THUMBNAIL).where().eq(IConst.WINDOW_ID, ww.windowId).selectOrNull(Db.getWindowTable().getReadDb());
					return c;
				}
				return null;
//				return super.getBitmapCursor(info);
			}
		};
	}
	@Override
	public void setLoadInfoToView(int pos,View v, LoadBitmapInfo info) {
		Tab ww = mWindows.getWindowAt(pos, false);
		if(info.favicon==null&&ww.getFavicon()!=null)
			info.favicon = ww.getFavicon();
		if(info.favicon!=null&&ww.getFavicon()==null)
			ww.setFavicon(info.favicon);
		if(info.thumbnail!=null&&ww.mThumbnail==null)
			ww.mThumbnail = info.thumbnail;
		if(ww.getFavicon()!=null)
		{
			info.favicon = ww.getFavicon();
		}
		if(ww.mThumbnail!=null)
		{
			info.thumbnail = ww.mThumbnail;
			info.loadImage = false;
		}	
		super.setLoadInfoToView(pos,v, info);
	}
}