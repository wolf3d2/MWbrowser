package com.jbak.superbrowser;

import com.mw.superbrowser.R;

import java.util.ArrayList;

import org.json.JSONObject;

import com.jbak.reverseEngine.BrowserContract;
import com.jbak.superbrowser.Db.TableBookmarks;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.Browser;
import android.text.TextUtils;
import android.webkit.WebBackForwardList;
import android.webkit.WebHistoryItem;

public class Bookmark implements IConst
	{
		private String url;
		public String originalUrl;
		private String title;
		public long date;
		public int imageRes=0;
		public Object param;
		public int tabMode = WINDOW_OPEN_SAME;
//		public Bitmap thumbnail;
//		public Bitmap bitmap;

		public Bookmark() {
			
		}
		public Bookmark(String url,String title,long date) {
			set(url, title, date);
		}
		public final void set(String url,String title,long date)
		{
			this.setUrl(url);
			this.setTitle(title);
			this.date = date;
		}
		public Bookmark(Bookmark bm)
		{
			setUrl(bm.getUrl());
			date = bm.date;
			title = bm.title;
			imageRes = bm.imageRes;
			param = bm.param;
			tabMode = bm.tabMode;
		}
		public Bookmark setImageRes(int imageRes)
		{
			this.imageRes = imageRes;
			return this;
		}
		public Bookmark setParam(Object param)
		{
			this.param = param;
			return this;
		}
		public Bookmark setWindowState(int windowState)
		{
			this.tabMode = windowState;
			return this;
		}
		public static Bookmark fromWebHistoryItem(WebHistoryItem item)
		{
			if(item==null)
				return null;
			Bookmark bm = new Bookmark();
			bm.setUrl(item.getUrl());
			bm.setTitle(item.getTitle());
			bm.originalUrl = item.getOriginalUrl();
			bm.date = System.currentTimeMillis();
			return bm;
		}
		public static Bookmark fromBookmarkFolder(String title,long id)
		{
			Bookmark bm = new Bookmark(null, title, 0);
			bm.imageRes = R.drawable.folder;
			bm.date = System.currentTimeMillis();
			bm.param = id;
			return bm;
		}
		public final boolean isBookmarkFolder()
		{
			return imageRes==R.drawable.folder&&param instanceof Long;
		}
		public final long getBookmarkFolderId()
		{
			return (Long)param;
		}
		/** возвращает bookmark из текущей позиции курсора */
		public static Bookmark fromManagedCursor(Cursor c)
		{
			// тип курсора - или 0, или 1 для истории поискаж
			int type_cursor = 0; 
			int index = 0;
			String[] ar =  c.getColumnNames();
			for (String curs:ar)
			{
				if (curs.compareToIgnoreCase(Db.SEARCH)==0) {
					type_cursor = 1;
					break;
				}
			}
			//[_id, url, visits, date, bookmark, title, favicon, thumbnail, touch_icon, user_entered]
			Bookmark bm = new Bookmark();
			switch (type_cursor)
			{
			case 1:
				//bm.setUrl(c.getString(c.getColumnIndex(Browser.BookmarkColumns.URL)));
				index = c.getColumnIndex(Browser.BookmarkColumns.DATE);
				if(index<0)
					index = -1;
				if(index>=0)
					bm.date = c.getLong(index);
				
//				if(BrowserApp.DB_TYPE==BrowserApp.DB_OWN)
//					index = c.getColumnIndex(TYPE);
//				else
//					index = c.getColumnIndex(BrowserContract.Bookmarks.IS_FOLDER);
//				if(index>=0)
//				{
//					int type = c.getInt(index);
//					boolean folder = BrowserApp.DB_TYPE==BrowserApp.DB_OWN&&type==TableBookmarks.TYPE_FOLDER||BrowserApp.DB_TYPE!=BrowserApp.DB_OWN&&type>0;
//					if(folder)
//						bm.setImageRes(R.drawable.folder);
//				}
				
				index = c.getColumnIndex(_ID);
				if(index>=0)
					bm.param = c.getLong(index);
				index = c.getColumnIndex(Db.SEARCH);
				if(index>=0) {
					bm.setTitle(c.getString(index));
				}
				bm.originalUrl = bm.getUrl();
				break;
			default:
				bm.setUrl(c.getString(c.getColumnIndex(Browser.BookmarkColumns.URL)));
				index = c.getColumnIndex(Browser.BookmarkColumns.DATE);
				if(index<0)
					index = c.getColumnIndex(MODIFIED);
				if(index>=0)
					bm.date = c.getLong(index);
				
				if(BrowserApp.DB_TYPE==BrowserApp.DB_OWN)
					index = c.getColumnIndex(TYPE);
				else
					index = c.getColumnIndex(BrowserContract.Bookmarks.IS_FOLDER);
				if(index>=0)
				{
					int type = c.getInt(index);
					boolean folder = BrowserApp.DB_TYPE==BrowserApp.DB_OWN&&type==TableBookmarks.TYPE_FOLDER||BrowserApp.DB_TYPE!=BrowserApp.DB_OWN&&type>0;
					if(folder)
						bm.setImageRes(R.drawable.folder);
				}
				
				index = c.getColumnIndex(_ID);
				if(index>=0)
					bm.param = c.getLong(index);
				index = c.getColumnIndex(Browser.BookmarkColumns.TITLE);
				if(index>=0)
					bm.setTitle(c.getString(index));
				bm.originalUrl = bm.getUrl();
				break;
			}
			return bm;
			
		}
		public static Bookmark currentFromHistoryList(WebBackForwardList list)
		{
			WebHistoryItem item =  list.getCurrentItem();
			return fromWebHistoryItem(item);
		}
		public static Bookmark byIndexFromHistoryList(WebBackForwardList list,int index)
		{
			if(index>=list.getSize())
				return null;
			WebHistoryItem item = list.getItemAtIndex(index);
			return fromWebHistoryItem(item);
		}
		public ContentValues getContentValues(ContentValues cv)
		{
			cv.clear();
			cv.put(stat.TITLE, getTitle());
			cv.put(stat.URL, getUrl());
			cv.put(stat.DATE, date);
			return cv;
		}
		public JSONObject getJSON()
		{
			JSONObject obj = new JSONObject();
			try{
				obj.putOpt(stat.TITLE, getTitle());
				obj.putOpt(stat.URL, getUrl());
				obj.putOpt(stat.ORIGINAL_URL, originalUrl);
				obj.putOpt(stat.DATE, date);
			}
			catch(Throwable e){}
			return obj;
		}
		public static Bookmark fromJSON(JSONObject obj)
		{
			Bookmark bm = new Bookmark();
			bm.setUrl(obj.optString(stat.URL));
			bm.originalUrl = obj.optString(stat.ORIGINAL_URL);
			bm.setTitle(obj.optString(TITLE));
			if(TextUtils.isEmpty(bm.getTitle())&&obj.has(HISTORY))
				bm.setTitle(obj.optString(HISTORY));
			bm.date = obj.optLong(stat.DATE);
			return bm;
		}
		public ContentValues getContentValues(Bitmap favicon,Bitmap thumbnail,long parentDir)
		{
			ContentValues cv = getContentValues(new ContentValues());
			if(parentDir>=0)
				cv.put(PARENT, parentDir);
			if(favicon!=null)
				cv.put(FAVICON, stat.bitmapToByte(favicon));
			if(thumbnail!=null)
				cv.put(THUMBNAIL, stat.bitmapToByte(thumbnail));
			return cv;
		}
		public ContentValues getContentValues()
		{
			return getContentValues(new ContentValues());
		}
		@Override
		public boolean equals(Object o) {
			if(this==o)
				return true;
			if(getUrl()==null||getTitle()==null)
				return false;
			if(o instanceof Bookmark)
			{
				Bookmark b = (Bookmark)o;
				if(b.getUrl()==null||b.getTitle()==null)
					return false;
				return getUrl().contentEquals(b.getUrl())&&getTitle().contentEquals(b.getTitle());
			}
			return false;
		}
		public static ArrayList<Bookmark> createList(WebBackForwardList list)
		{
			ArrayList<Bookmark> ar = new ArrayList<Bookmark>();
			int sz = list.getSize();
			for(int i=0;i<sz;i++)
			{
				ar.add(Bookmark.fromWebHistoryItem(list.getItemAtIndex(i)));
			}
			return ar;
		}
		public ContentValues getManagedContentValues(boolean history,Bitmap favicon, Bitmap thumbnail,long parentDir)
		{
			ContentValues cv = new ContentValues();
			cv.put(Browser.BookmarkColumns.TITLE, getTitle());
			cv.put(Browser.BookmarkColumns.CREATED, date);
			cv.put(Browser.BookmarkColumns.URL, getUrl());
			if(favicon!=null)
				cv.put(Browser.BookmarkColumns.FAVICON, stat.bitmapToByte(favicon));
			if(thumbnail!=null)
				cv.put(THUMBNAIL, stat.bitmapToByte(thumbnail));
			if(parentDir>0)
			{
				cv.put(PARENT, parentDir);
				cv.put(BrowserContract.Bookmarks.IS_FOLDER, 0);
				cv.put(MODIFIED, date);
			}
			else
			{
				cv.put(Browser.BookmarkColumns.DATE, date);
				cv.put(Browser.BookmarkColumns.BOOKMARK, history?0:1); 
			}
			return cv;
		}
		public ContentValues getFolderContentValues(long parentDir)
		{
			ContentValues cv = new ContentValues();
			cv.put(BrowserContract.Bookmarks.IS_FOLDER, 1); 
			cv.put(Browser.BookmarkColumns.TITLE, getTitle());
			cv.put(BrowserContract.Bookmarks.DATE_MODIFIED, date);
			cv.put(Browser.BookmarkColumns.CREATED, date);
			cv.put("parent", parentDir);
			return cv;
		}
		public final String getTitle()
		{
			if(!TextUtils.isEmpty(title))
				return title;
			if(TextUtils.isEmpty(getUrl()))
				return title;
			String text = stat.decode(url);
			int pos = text.indexOf("//");
			if(pos>0)
				return text.substring(pos+2);
			return text;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public final String getUrl() {
			return url;
		}
		public final void setUrl(String url) {
			this.url = url;
		}
		@Override
		public String toString() {
			return new StringBuffer().append(title).append(':').append(url).toString();
		}
	}