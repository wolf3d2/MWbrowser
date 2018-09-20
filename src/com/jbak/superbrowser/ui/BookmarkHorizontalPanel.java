package com.jbak.superbrowser.ui;

import ru.mail.mailnews.st;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.jbak.superbrowser.Action;
import com.jbak.superbrowser.Bookmark;
import com.jbak.superbrowser.BookmarkActivity;
import com.jbak.superbrowser.IConst;
import com.jbak.superbrowser.MainActivity;
import com.mw.superbrowser.R;
import com.jbak.superbrowser.WebViewEvent;
import com.jbak.superbrowser.adapters.BookmarkAdapter;
import com.jbak.superbrowser.adapters.BookmarkFolderAdapter;
import com.jbak.superbrowser.recycleview.MyItemDecoration;
import com.jbak.superbrowser.ui.MenuPanelButton.MenuBookmark;
import com.jbak.ui.UIUtils;

public class BookmarkHorizontalPanel extends HorizontalPanel implements WebViewEvent{

	BookmarkAdapter mAdapter;
	int mMaxItems=-1;
	private static final int DEFAULT_FOLDER = -1234;
	long mStartFolderId = IConst.ROOT_FOLDER_ID;
	public BookmarkHorizontalPanel(Context context, AttributeSet ats) {
		super(context, ats);
		init(DEFAULT_FOLDER);
	}
	public BookmarkHorizontalPanel(Context context) {
		super(context);
		init(DEFAULT_FOLDER);
	}
	public BookmarkHorizontalPanel(Context context,long startFolderId) {
		super(context);
		init(startFolderId);
	}
	protected void init(long startFolder) {
		if(startFolder==DEFAULT_FOLDER)
			
		super.init();
		setButtonsType(PanelButton.TYPE_BUTTON_BOOKMARK);
		setBookmarkSource(createBookmarkAdapter());
	}
	public void setBookmarkSource(BookmarkAdapter adapter)
	{
		
//		mMaxItems = maxItems;
		mAdapter = adapter;
		setBookmarks(adapter);
//		int size = adapter.getCount();
//		ActArray ar = new ActArray();
//		if(mAdditionalActions!=null)
//			ar.addAll(mAdditionalActions);
//		for(int i=0;i<size&&i<maxItems;i++)
//		{
//			Bookmark bm = adapter.getBookmark(i);
//			Action a = Action.create(Action.ACTION_BOOKMARK, bm); 
//			if(bm.isBookmarkFolder()||bm.imageRes==R.drawable.up)
//				a.setImageRes(bm.imageRes);
//			else
//				adapter.startBitmapLoader(i, bm, a);
//			ar.add(a);
//		}
//		setActions(ar);
	}
	public final ViewGroup getBookmarksViewGroup()
	{
		return this;
	}
	public String getViewUrl(View v)
	{
		if(v.getTag() instanceof Action)
		{
			Action act = (Action)v.getTag();
			if(act.command==Action.ACTION_BOOKMARK&&act.param instanceof Bookmark)
			{
				return ((Bookmark)act.param).getUrl();
			}
		}
		return null;
	}
	public void setPanelButtonBitmaps(LoadBitmapInfo li,PanelButton pb)
	{
		if(li!=null)
		{
			if(li.favicon!=null)
				pb.setImage(li.favicon);
			if(li.thumbnail!=null)
				pb.setBackgroundImage(li.thumbnail);
			UIUtils.showViews(li.thumbnail==null?INVISIBLE:VISIBLE, pb.getBackgroundImageView());
		}
	}
	@Override
	public void setPanelButton(int pos,Action action, PanelButton pb) {
		if(action.command==Action.ACTION_BOOKMARK)
		{
			Bookmark bm = (Bookmark)action.param;
			LoadBitmapInfo li = mAdapter.getBitmapLoadInfo(bm);
			pb.setBackgroundImage(null);
			setPanelButtonBitmaps(li, pb);
		}
		super.setPanelButton(pos,action, pb);
	}
	@Override
	public void onClick(View v) {
		Action a = (Action)v.getTag();
		if(a.command==Action.ACTION_BOOKMARK)
		{
			Bookmark bm = (Bookmark)a.param;
			if(mAdapter instanceof BookmarkFolderAdapter
					&&((BookmarkFolderAdapter)mAdapter).onItemClick(bm,mAdapter.getBitmapLoadInfo(a)))
				setBookmarkSource(mAdapter);
			return;
		}
		super.onClick(v);
	}
	@Override
	public int getMaxHeight() {
		if(mType==TYPE_GRID)
			return Integer.MAX_VALUE;
		return super.getMaxHeight();
	}
	public void updateAdapter() {
		if(mAdapter==null)
			return;
		if(mAdapter instanceof BookmarkFolderAdapter)
			((BookmarkFolderAdapter)mAdapter).updateAdapter(false);
		getRealAdapter().notifyDataSetChanged();
		setLayoutManager(getFillHorizontal());
	}
	void onBookmark(Bookmark bm,LoadBitmapInfo info)
	{
		if(getContext() instanceof MainActivity)
		{
			Bookmark copy = mAdapter.createBookmarkCopy(bm, info);
			((MainActivity)getContext()).runAction(Action.create(Action.ACTION_BOOKMARK,copy));
		}
	}
	@Override
	protected void onMeasure(int widthSpec, int heightSpec) {
		super.onMeasure(widthSpec, heightSpec);
	}
	public BookmarkFolderAdapter createBookmarkAdapter()
	{
		
		return new BookmarkFolderAdapter(getContext(),mStartFolderId) {
			
			@Override
			public void scrollToTop() {
				//mPanelBookmarks.getScrollView().fullScroll(HorizontalScrollView.FOCUS_LEFT);
			}
			@Override
			public ViewGroup getViewGroup() {
				return getBookmarksViewGroup();
			}
			@Override
			public Object getItemTag(int pos, Bookmark bm) {
				return Action.create(Action.ACTION_BOOKMARK,bm);
			}
			@Override
			public void setLoadInfoToView(int pos,View v, LoadBitmapInfo info) {
				super.setLoadInfoToView(pos, v, info);
				setPanelButtonBitmaps(info, (PanelButton)v);
			}
			@Override
			public void onBookmarkClick(Bookmark bm,LoadBitmapInfo info) {
				onBookmark(bm,info);
			}
			@Override
			public void onItemChanged(Bookmark curBookmark) {
				getRealAdapter().notifyDataSetChanged();
			}
		};

	}
	int mItemWidth=0;
	@Override
	public int getItemWidth() {
		if(mItemWidth>0)
			return mItemWidth;
		return super.getItemWidth();
	}
	@Override
	public void setType(int type) {
		if(type==TYPE_GRID)
			mItemWidth = (int) getResources().getDimension(R.dimen.panelButtonSquareSize);
		else
			mItemWidth = 0;
		super.setType(type);
	}
	@Override
	protected void setLayoutManager(boolean fill) {
		super.setLayoutManager(fill);
		if(getMaxHeight()==Integer.MAX_VALUE)
			setItemDecoration(new MyItemDecoration(st.dp2px(getContext(), 5)));
		else
			setItemDecoration(null);
	}
	@Override
	protected void checkLP(android.support.v7.widget.RecyclerView.LayoutParams lp)
	{
		super.checkLP(lp);
		if(mType==TYPE_GRID)
			lp.height = (int) getResources().getDimension(R.dimen.panelButtonSquareSize);
	}
	@Override
	public boolean onLongClick(View v) {
		Bookmark bm = null; 
		if(v.getTag() instanceof Bookmark)
			bm = (Bookmark)v.getTag();
		else if(v.getTag() instanceof Action)
		{
			Action a = (Action)v.getTag();
			if(a.command==Action.ACTION_BOOKMARK)
				bm = (Bookmark) a.param;
		}
		if(bm==null||bm.param==null)
			return true;
		Bitmap preview = null;
		LoadBitmapInfo lbi = mAdapter.getBitmapLoadInfo(v.getTag());
		if(lbi!=null)
			preview = lbi.thumbnail;
		MenuBookmark mb = new MenuBookmark(getContext(), bm,BookmarkActivity.TYPE_BOOKMARKS, null,preview);
		mb.show();
		return true;
	}
	@Override
	public void onWebViewEvent(int code, EventInfo info) {
		if(code==WebViewEvent.WWEVENT_BOOKMARKS_CHANGED)
			updateAdapter();
		
	}
}
