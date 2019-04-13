package com.jbak.superbrowser.adapters;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Browser;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import ru.mail.mailnews.st;

import com.jbak.reverseEngine.BrowserContract;
import com.jbak.superbrowser.BitmapLoader;
import com.jbak.superbrowser.Bookmark;
import com.jbak.superbrowser.BookmarkActivity;
import com.jbak.superbrowser.BrowserApp;
import com.jbak.superbrowser.Db;
import com.jbak.superbrowser.IConst;
import com.mw.superbrowser.R;
import com.jbak.superbrowser.stat;
import com.jbak.superbrowser.adapters.BookmarkAdapter.CursorBookmarkAdapter;
import com.jbak.superbrowser.ui.LoadBitmapCache;
import com.jbak.superbrowser.ui.LoadBitmapInfo;
import com.jbak.superbrowser.ui.MenuPanelButton.MenuBookmark;
import com.jbak.superbrowser.ui.dialogs.ThemedDialog;
import com.jbak.ui.CustomDialog.OnUserInput;
import com.jbak.ui.CustomPopup;
import com.jbak.utils.DbUtils;

public abstract class BookmarkFolderAdapter extends CursorBookmarkAdapter implements OnClickListener,OnLongClickListener,OnItemClickListener{
	static LoadBitmapCache mCacheInstance;
	Bookmark mCurFolder;
	public BookmarkFolderAdapter(Context context) {
		super(context, getBookmarkCursorWithFolder(context.getContentResolver(), IConst.ROOT_FOLDER_ID));
		initCache();
	}
	public BookmarkFolderAdapter(Context context,long rootFolder) {
		super(context, null);
		initCache();
		Bookmark cur = getFolder(context.getContentResolver(), rootFolder);
		if(cur==null)
			rootFolder = IConst.ROOT_FOLDER_ID;
		else
			mCurFolder = cur;
		setCursor(getBookmarkCursorWithFolder(context.getContentResolver(), rootFolder));
	}
	public BookmarkFolderAdapter(BookmarkFolderAdapter adapt) {
		super(adapt.getContext(),adapt.mCursor);
		initCache();
		mCurFolder = adapt.mCurFolder;
	}
	void initCache()
	{
		if(mCacheInstance==null)
			mCacheInstance = new LoadBitmapCache();
		setCache(mCacheInstance);
	}
	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		return super.getView(position, convertView, arg2);
	}
	@Override
	public int getItemsCount() {
		if(mCurFolder!=null&&mCurFolder.getBookmarkFolderId()!=IConst.ROOT_FOLDER_ID)
			return super.getItemsCount()+1;
		return super.getItemsCount();
	}
	@Override
	public Bookmark getBookmarkFromCursor(Cursor c) {
		return Bookmark.fromManagedCursor(c);
	}
	@Override
	public Bookmark getBookmark(int pos) {
		if(mCurFolder!=null&&mCurFolder.getBookmarkFolderId()!=IConst.ROOT_FOLDER_ID)
		{
			if(pos==0)
			{
				Bookmark bm = new Bookmark(null, mCurFolder.getTitle(), mCurFolder.date).setImageRes(R.drawable.up);
				return bm;
			}
			else
			{
				mCursor.moveToPosition(pos-1);
				return getBookmarkFromCursor(mCursor);
			}
		}
		return super.getBookmark(pos);
	}
	public static Cursor getCursorForBookmarkFolder(Context context, Bookmark folderBookmark)
	{
		long id = 1;
		if(folderBookmark!=null&&folderBookmark.isBookmarkFolder())
			id = folderBookmark.getBookmarkFolderId();
		return getBookmarkCursorWithFolder(context.getContentResolver(), id);
	}
	/** определяет и выдаёт cursor откуда читать - из системмных закладок или из базы */
	public static Cursor getBookmarkCursorWithFolder(ContentResolver cr, long folderId)
	{
		if(BrowserApp.DB_TYPE==BrowserApp.DB_BROWSER_CONTRACT)
			return cr.query(BrowserContract.Bookmarks.CONTENT_URI, null, "parent="+folderId, null, "folder desc, title");
		if(BrowserApp.DB_TYPE==BrowserApp.DB_BROWSER)
			return cr.query(Browser.BOOKMARKS_URI, null, "bookmark = 1", null, DbUtils.getOrder(IConst.TITLE, false));
		else //if(BrowserApp.DB_TYPE==BrowserApp.DB_OWN)
			return Db.getBookmarksTable().getBookmarkCursor(null, folderId);
	}
	private static final String PROJ_CUR[] = new String[]{BrowserContract.Bookmarks.PARENT};
	
	/** читает текущую папку закладок из системных закладок */
	public static Bookmark getFolder(ContentResolver cr, long id)
	{
		Cursor c = cr.query(BrowserContract.Bookmarks.CONTENT_URI, null , "_id="+id, null, null);
		if(c==null)
			return null;
		Bookmark ret = null;
		if(c.moveToFirst())
		{
			ret = Bookmark.fromManagedCursor(c);
			ret.imageRes = R.drawable.folder;
		}
		c.close();
		return ret;
	}
	/** читает текущую папку закладок из системных закладок */
	public static Bookmark getParentFolder(ContentResolver cr, long id)
	{
		// content://com.android.browser/bookmarks
		Uri urii = BrowserContract.Bookmarks.CONTENT_URI;
		Cursor c = cr.query(BrowserContract.Bookmarks.CONTENT_URI, PROJ_CUR , "_id="+id, null, null);
		boolean fl = false;
		
		if(c==null) {
			c = Db.getBookmarksTable().getBookmarkCursorByParentId(id);
			fl=true;
		}
		else if(!c.moveToFirst())
		{
			c.close();
			c = Db.getBookmarksTable().getBookmarkCursorByParentId(id);
			fl=true;
		}
		if(c==null)
			return null;
		long parent = -1;
		if(c.moveToFirst())
		{
			if (fl)
				parent = c.getLong(5);
			else
				parent = c.getLong(0);
		}
		c.close();
		if(parent<0||parent==IConst.ROOT_FOLDER_ID)
			return null;
		c = cr.query(BrowserContract.Bookmarks.CONTENT_URI, null , "_id="+parent, null, null);
		if(c==null) 
			c = Db.getBookmarksTable().getBookmarkCursorByParentId(parent);
		else if(!c.moveToFirst())
		{
			c.close();
			c = Db.getBookmarksTable().getBookmarkCursorByParentId(parent);
		}
		if(c==null)
			return null;
		Bookmark ret = null;
		if(c.moveToFirst())
		{
			ret = Bookmark.fromManagedCursor(c);
			ret.imageRes = R.drawable.folder;
		}
		c.close();
		return ret;
	}
	public abstract void scrollToTop();
	public abstract void onBookmarkClick(Bookmark bm,LoadBitmapInfo info);
	public boolean onBookmarkLongClick(View v,Bookmark bm)
	{
		MenuBookmark mb = new MenuBookmark(getContext(), bm, BookmarkActivity.TYPE_BOOKMARKS, null,getThumbnailForView(v));
		return true;
	}
	public boolean canGoUp()
	{
		if(mCurFolder==null)
			return false;
		return true;
	}
	public void goHome()
	{
		mCurFolder = null;
		onItemChanged(mCurFolder);
		updateAdapter(true);
	}
	public boolean goUp()
	{
		if(!canGoUp())
			return false;
		mCurFolder = getParentFolder(getContext().getContentResolver(), mCurFolder.getBookmarkFolderId());
		updateAdapter(true);
		onItemChanged(mCurFolder);
		return true;
	}
	public boolean onItemClick(Bookmark bm,LoadBitmapInfo info)
	{
		if(bm.isBookmarkFolder())
		{
			Cursor c = getCursorForBookmarkFolder(getContext(), bm);
			setCursor(c);
			mCurFolder = bm;
			onItemChanged(mCurFolder);
			scrollToTop();
			return true;
		}
		else if(bm.getUrl()==null)// уровень вверх
		{
			goUp();
			return true;
		}
		else
		{
			onBookmarkClick(bm,info);
			return false;
		}
	}
	public abstract void onItemChanged(Bookmark curBookmark);
	public Bookmark getCurrentFolder()
	{
		return mCurFolder;
	}
	public long getCurrentFolderId()
	{
		if(mCurFolder==null)
			return IConst.ROOT_FOLDER_ID;
		return mCurFolder.getBookmarkFolderId();
	}
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		onClick(arg1);
	}
	@Override
	public void onClick(View v) {
		Bookmark bm = (Bookmark) v.getTag();
		onItemClick(bm,getBitmapLoadInfo(v.getTag()));
	}
	@Override
	public boolean onLongClick(View v) {
		Bookmark bm = null;
		if(v.getTag()instanceof Bookmark)
			bm = (Bookmark)v.getTag();
		return onBookmarkLongClick(v, bm);
	}

	public void createFolder() {
		new ThemedDialog(getContext()).setInput(getContext().getString(R.string.act_create_folder), st.STR_NULL, new OnUserInput() {
			
			@Override
			public void onUserInput(boolean ok, String newText) {
				if(ok)
				{
					long id = 1;
					if(mCurFolder!=null)
						id = mCurFolder.getBookmarkFolderId();
					Bookmark bm = stat.createBookmarkFolder(getContext(), newText, id);
					if(bm==null)
						CustomPopup.toast(getContext(), R.string.dir_create_error);
					else
						updateAdapter(true);
				}
			}
		}).show();
	}
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}
	public void updateAdapter(boolean scrollTop)
	{
		setCursor(getCursorForBookmarkFolder(getContext(), mCurFolder));
		if(scrollTop)
			scrollToTop();
	}
	@Override
	protected BitmapLoader createBitmapLoader(int pos, Bookmark bm,LoadBitmapInfo li) {
		return new BitmapLoader(getContext().getContentResolver(), this, li)
		{
			@Override
			protected Cursor getBitmapCursor(LoadBitmapInfo info) {
				if(info.getUrl()==null)
					return null;
				Cursor c; 
				if(BrowserApp.DB_TYPE==BrowserApp.DB_OWN)
					c = Db.getBookmarksTable().select().columns(IConst.FAVICON,IConst.THUMBNAIL,IConst.URL).where().eq(IConst.URL, info.getUrl()).orderBy(Db.TYPE, false).select(Db.getBookmarksTable().getDb());
				else
					c = new DbUtils.Select(stat.BITMAP).columns(IConst.FAVICON,IConst.THUMBNAIL,IConst.URL).where().eq(IConst.URL, info.getUrl()).and().whereRaw(stat.isBookmark(true)).select(mCr, Browser.BOOKMARKS_URI);
				return c;
			}
		};
	}
	@Override
	public void destroy() {
	}
}
