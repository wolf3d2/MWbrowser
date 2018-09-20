package com.jbak.superbrowser.adapters;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import ru.mail.mailnews.st.SyncAsycOper;
import ru.mail.mailnews.st.UniObserver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebBackForwardList;
import android.webkit.WebHistoryItem;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.jbak.superbrowser.Action;
import com.jbak.superbrowser.BitmapLoader;
import com.jbak.superbrowser.Bookmark;
import com.jbak.superbrowser.BookmarkActivity;
import com.jbak.superbrowser.BrowserApp;
import com.jbak.superbrowser.Db;
import com.jbak.superbrowser.OnBitmapLoadListener;
import com.jbak.superbrowser.stat;
import com.jbak.superbrowser.Tab;
import com.jbak.superbrowser.recycleview.RecyclerViewEx;
import com.jbak.superbrowser.ui.BookmarkView;
import com.jbak.superbrowser.ui.LoadBitmapCache;
import com.jbak.superbrowser.ui.LoadBitmapInfo;
import com.jbak.superbrowser.ui.PanelButton;
import com.jbak.superbrowser.ui.themes.MyTheme;
import com.jbak.ui.UIUtils;
import com.jbak.utils.GlobalHandler;
import com.jbak.utils.Utils;

public abstract class BookmarkAdapter extends BaseAdapter implements OnBitmapLoadListener,GlobalHandler
{
	public static final int WHAT_UPD_ITEMS = 100;
	private WeakReference<Context> mContext;
	public int mCurrent=-1;
	BitmapLoader mBitmapLoader;
	protected LoadBitmapCache mBitmapCache = new LoadBitmapCache();
	ArrayList<Bookmark> resultList;
	protected boolean mAutoLoadImages = true;
	WeakReference<Adapter<?>> mParentAdapter;
	protected int mBookmarkType = -1;
	public BookmarkAdapter(Context c) {
		setContext(c);
	}
	public BookmarkAdapter setCurrentPos(int currentPos) {
		mCurrent = currentPos;
		return this;
	}
	public final BookmarkAdapter setBookmarkType(int type)
	{
		mBookmarkType = type;
		return this;
	}
	public final Context getContext()
	{
		if(mContext!=null)
			return mContext.get();
		return null;
	}
	@Override
	public int getCount() {
		if(resultList!=null)
			return resultList.size();
		return getItemsCount();
	}
	@Override
	public void onHandlerEvent(int what) {
		if(what==WHAT_UPD_ITEMS)
		{
			notifyDataSetChanged();
		}
	}
	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
	public ArrayList<LoadBitmapInfo> getCache()
	{
		return mBitmapCache;
	}
	public void setCache(LoadBitmapCache cache)
	{
		mBitmapCache = cache;
	}
	public void setParentAdapter(Adapter<?> adapt)
	{
		mParentAdapter = new WeakReference<Adapter<?>>(adapt);
	}
	public final LoadBitmapInfo getBitmapLoadInfo(Object param)
	{
		return mBitmapCache.getCache(param);
	}
	public boolean setSelectedItem(int pos, View v, LoadBitmapInfo info) {
		return false;
	}
	public View processView(int position,View convertView,boolean createBookmarkView)
	{
		Bookmark b = getBookmark(position);
		Object tag = null;
		tag = getItemTag(position, b);
		if(createBookmarkView)
		{
			if (b==null){
//				BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_ACTION, Action.create(Action.CLOSE_ALL_TABS));
 				convertView = new BookmarkView(getContext());
			} else
				convertView = stat.getBookmarkView(getContext(), position, convertView, b,mCurrent==position,mBookmarkType);
		}
		LoadBitmapInfo li = getBitmapLoadInfo(tag);
		boolean loadImages = mAutoLoadImages&&li==null&&b!=null&&b.imageRes==0;
		if(li==null)
		{
			li = new LoadBitmapInfo(b, tag);
			if(loadImages)
				mBitmapCache.addCache(li);
		}
		setLoadInfoToView(position,convertView, li);
		if(loadImages&&li.loadImage)
			startBitmapLoader(position, li);
		return  convertView;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		return processView(position, convertView, true);
	}
	public ViewGroup getViewGroup()
	{
		if(getContext() instanceof BookmarkActivity)
			return ((BookmarkActivity)getContext()).getListView();
		return null;
	}
	protected View getViewByLoadInfo(LoadBitmapInfo info)
	{
		if(info==null||info.param==null)
			return null;
		ViewGroup vg = getViewGroup();
		if(vg==null)
			return null;
		for(int i=vg.getChildCount()-1;i>=0;i--)
		{
			View v = vg.getChildAt(i);
			if(v!=null&&info.param==v.getTag())
				return v;
		}
//		int top = mListView.getFirstVisiblePosition();
//		int sz = mListView.getChildCount();
//		if(info.adapterPos<=top+sz)
//		{
//			View v = mListView.getChildAt(info.adapterPos-top);
//			if(v instanceof BookmarkView)
//			{
//				return (BookmarkView)v;
//			}
//		}
		return null;
	}
	public int getViewPosition(View v, ViewGroup parent)
	{
		try{
		if(parent instanceof RecyclerViewEx)
		{
			RecyclerViewEx rc = (RecyclerViewEx)parent;
			if(rc.getLayoutManager()==null)
				return 0;
			return rc.getLayoutManager().getPosition(v);
		}
		if(parent instanceof ListView)
		{
			ListView rc = (ListView)parent;
			return rc.getPositionForView(v);
		}
		}
		catch(Throwable e)
		{
			Utils.log(e); 
		}
		return 0;
	}
	@Override
	public void notifyDataSetChanged() {
		if(mParentAdapter!=null&&mParentAdapter.get()!=null)
		{
			if(this instanceof CursorBookmarkAdapter)
				super.notifyDataSetChanged();
			mParentAdapter.get().notifyDataSetChanged();
		}
		else
			super.notifyDataSetChanged();
	}
	@Override
	public void notifyDataSetInvalidated() {
		if(mParentAdapter!=null&&mParentAdapter.get()!=null)
			notifyDataSetChanged();
		else
			super.notifyDataSetInvalidated();
	}
	@Override
	public void onBitmapLoad(LoadBitmapInfo lbi) {
		View v = getViewByLoadInfo(lbi);
		if(v!=null)
			setLoadInfoToView(getViewPosition(v, getViewGroup()), v, lbi);
	}
	@Override
	public void onBitmapsLoad() {
//		notifyDataSetInvalidated();
	}
//	@Override
//	public void onBitmapLoad(LoadBitmapInfo info) {
////		//mBitmapCache.addCache(info);
////		//GlobalHandler.command.sendDelayed(WHAT_UPD_ITEMS, this, WHAT_UPD_ITEMS);
////		View v = getViewByLoadInfo(info);
////		int pos = getViewPosition(v, getViewGroup());
////		if(v!=null)
////			setLoadInfoToView(pos, v, info);
//	}
	public void setLoadInfoToView(int pos,View v,LoadBitmapInfo info)
	{
		BookmarkView bm = null;
		PanelButton pb = null;
		if(v instanceof BookmarkView)
		{
			bm = (BookmarkView)v;
			bm.setThumbnail(info.thumbnail);
			bm.setFavIcon(info.favicon);
			setBookmarkView(bm,info.bm);
		}
		else if(v instanceof PanelButton)
		{
 			pb = (PanelButton)v;
			MyTheme.get().setPanelButton(pb, pos, false);
			if(info.param instanceof Action&&((Action)info.param).command==Action.ACTION_BOOKMARK)
				pb.setAction((Action)info.param);
			else if(info.param instanceof Tab&&((Tab)info.param).getCurBookmark()!=null)
				pb.setBookmark(((Tab)info.param).getCurBookmark());
			else
				pb.setBookmark(info.bm);
		}
		boolean sel = setSelectedItem(pos, v, info);
		if(sel&&pb!=null)
		{
			//setSelectedItem(pos, v, info);
		}
		UIUtils.setViewsTag(info.param, v);
	}
	public final Bitmap getThumbnailForView(View v)
	{
		LoadBitmapInfo li = getBitmapLoadInfo(v.getTag());
		if(li!=null)
			return li.bitmap;
		return null;
	}
	public void setBookmarkView(BookmarkView view,Bookmark bm)
	{}
	public void destroy()
	{
		mBitmapCache.clearAndRecycle();
	}
	public void startBitmapLoader(int pos,LoadBitmapInfo li)
	{
//		BitmapLoader bl = new BitmapLoader(mListView.getContext().getContentResolver(), this, li);
//		bl.startAsync();
		if(mBitmapLoader!=null&&mBitmapLoader.started)
			mBitmapLoader.addLoad(li);
		else
		{
			if(mAutoLoadImages)
			{
				mBitmapLoader = createBitmapLoader(pos, li.bm, li);
				mBitmapLoader.startAsync();
			}
		}
	}
	protected BitmapLoader createBitmapLoader(int pos,Bookmark bm,LoadBitmapInfo li)
	{
		return new BitmapLoader(getContext().getContentResolver(), this, li);
	}
	public void showAsyncLoaderProgress(boolean show)
	{
		if(getContext() instanceof BookmarkActivity)
			((BookmarkActivity)getContext()).showProgress(show);
	}
	public boolean doAsync() throws Throwable
	{
		return false;
	}
	public void doAsyncUiThread(boolean result)
	{
		
	}
	protected void startAsyncLoader()
	{
		new AsyncLoader().startAsync();
	}
	public abstract Bookmark getBookmark(int pos);
	public abstract int getItemsCount();

	public Object getItemTag(int pos,Bookmark bm)
	{
		return bm;
	}
		public final Bookmark createBookmarkCopy(Bookmark bm,LoadBitmapInfo info)
		{
			Bookmark copy = new Bookmark(bm);
			if(info.favicon!=null)
			{
				copy.param = info.favicon;
				info.favicon = null;
			}
			return copy;
		}
	protected void setContext(Context mContext) {
			this.mContext = new WeakReference<Context>(mContext);
		}
	public class AsyncLoader extends SyncAsycOper
	{
		boolean mOk = false;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showAsyncLoaderProgress(true);
		}
		@Override
		public void makeOper(UniObserver obs) throws Throwable {
			try{
				mOk = doAsync();
			}
			catch(Throwable e)
			{
				Utils.log(e);
			}
		}
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			doAsyncUiThread(mOk);
			showAsyncLoaderProgress(false);
		}
		
	};
	
	public static class ArrayBookmarkAdapter extends BookmarkAdapter
	{
		List<Bookmark> mBookmarks;

		public ArrayBookmarkAdapter(Context c,List<Bookmark>list) {
			super(c);
			mBookmarks = list;
		}

		@Override
		public void setBookmarkView(BookmarkView view, Bookmark bm) {
		}

		@Override
		public Bookmark getBookmark(int pos) {
			return mBookmarks.get(pos);
		}

		@Override
		public int getItemsCount() {
			return mBookmarks.size();
		}
		public void setArray(List<Bookmark>list)
		{
			mBookmarks = list;
			notifyDataSetChanged();
		}
	};
	
	public static class WebViewHistoryAdapter extends BookmarkAdapter
	{

		WebBackForwardList mList;
		public WebViewHistoryAdapter(Context c, WebView ww) {
			super(c);
			mAutoLoadImages = true;
			mList = ww.copyBackForwardList();
			int ci = mList.getSize()-1-mList.getCurrentIndex();
			setCurrentPos(ci);
		}

		@Override
		public Bookmark getBookmark(int pos) {
			pos = mList.getSize()-1-pos;
			WebHistoryItem item = mList.getItemAtIndex(pos);
			Bookmark bm = Bookmark.fromWebHistoryItem(item);
			bm.param = Integer.valueOf(pos);
			return bm;
		}

		@Override
		public int getItemsCount() {
			return mList.getSize();
		}
		
	}
	public static abstract class CursorBookmarkAdapter extends BookmarkAdapter
	{
		Cursor mCursor;
		public CursorBookmarkAdapter(Context context,Cursor cursor) {
			super(context);
			mCursor = cursor;
		}
		@Override
		public void destroy() {
			super.destroy();
			if(mCursor!=null&&!mCursor.isClosed())
				mCursor.close();
		}
		public void setCursor(Cursor c)
		{
			Cursor old = mCursor;
			mCursor = c;
			notifyDataSetChanged();
			if(old!=null&&!mCursor.isClosed())
				old.close();
		}
		@Override
		public void setBookmarkView(BookmarkView view, Bookmark bm) {
		}
		public abstract Bookmark getBookmarkFromCursor(Cursor c);
		@Override
		public Bookmark getBookmark(int pos) {
			mCursor.moveToPosition(pos);
			return getBookmarkFromCursor(mCursor);
		}
		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
		}
		@Override
		public int getItemsCount() {
			if(mCursor!=null)
				return mCursor.getCount();
			return 0;
		}
		public final Cursor getCursor()
		{
			return mCursor;
		}
	}
	public static class ExtHistoryAdapter extends CursorBookmarkAdapter
	{
		int mType;
		public ExtHistoryAdapter(Context context, int type) {
			super(context, Db.getExtHistory().getCursorByType(type));
			mType = type;
		}
		@Override
		public Bookmark getBookmarkFromCursor(Cursor c) {
			return Bookmark.fromManagedCursor(c);
		}
		@Override
		protected BitmapLoader createBitmapLoader(int pos, Bookmark bm,LoadBitmapInfo li) {
			return new BitmapLoader(getContext().getContentResolver(), this, li)
			{
				@Override
				protected Cursor getBitmapCursor(LoadBitmapInfo info) {
					return Db.getExtHistory().select().columns(THUMBNAIL).where().eq(URL, info.bm.getUrl()).and().eq(TYPE, mType).select(Db.getBookmarksTable().getDb());
				}
			};
		}
	}
}