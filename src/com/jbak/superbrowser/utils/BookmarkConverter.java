package com.jbak.superbrowser.utils;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jbak.superbrowser.Bookmark;
import com.jbak.superbrowser.IConst;
import com.jbak.superbrowser.stat;
import com.jbak.utils.ContextRef;
import com.jbak.utils.DbConverter;
import com.jbak.utils.DbConverter.DbIndices;

public class BookmarkConverter implements IConst{
	public static final String ID = "id";
	public static final String DATE_ADDED = "date_added";
	public static final String ROOTS = "roots";
	public static final String CHILDREN = "roots";
	long mDefaultDate;
	public static String[] BOOKMARK_PROJECTIONS = new String[]{
		ID,TYPE,NAME,URL
	};
	@SuppressLint("InlinedApi")
	public static Integer[] BOOKMARK_TYPES = new Integer[]{
		Cursor.FIELD_TYPE_INTEGER,Cursor.FIELD_TYPE_STRING,Cursor.FIELD_TYPE_STRING,Cursor.FIELD_TYPE_STRING
	};
	public BookmarkConverter() {
		mDefaultDate = System.currentTimeMillis();
	}
	public boolean importBookmarks(Context c,JSONObject bookmarks,long parentFolder)
	{
		try{
			ArrayList<Bookmark> ar = new ArrayList<Bookmark>();
			JSONObjectToArray(bookmarks, ar,0);
			BookmarkData d = new BookmarkData(c, ar, parentFolder);
			d.processData();
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		return false;
	}
	public boolean writeCV(JSONObject jo, ContentValues cv, int type, int index,String fieldName) throws Throwable
	{
		switch (type) {
			case Cursor.FIELD_TYPE_INTEGER: cv.put(fieldName, jo.optLong(fieldName, -1)); return true;
			case Cursor.FIELD_TYPE_STRING: cv.put(fieldName, jo.optString(fieldName, null)); return true;
		}
		return false;
	}
	public void JSONArrayToArray(JSONArray ja, ArrayList<Bookmark>ar,int level) throws Throwable
	{
		int len = ja.length();
		for(int i=0;i<len;i++)
		{
			JSONObject jo = ja.optJSONObject(i);
			if(jo!=null)
				JSONObjectToArray(jo, ar,level+1);
		}
	}
	public void JSONObjectToArray(JSONObject jo, ArrayList<Bookmark>ar,int level) throws Throwable
	{
		JSONArray ja = jo.optJSONArray(CHILDREN);
		if(ja!=null)
		{
			String name = jo.optString(NAME);
			Bookmark bm = Bookmark.fromBookmarkFolder(name, level);
			ar.add(bm);
			JSONArrayToArray(ja, ar,level+1);
		}
		else
		{
			String name = jo.optString(NAME, null);
			String url = jo.optString(URL, null);
			long date = jo.optLong(DATE_ADDED, mDefaultDate);
			Bookmark bm = new Bookmark(url, name, date);
			bm.param = Integer.valueOf(level);
			ar.add(bm);
		}
	}
	public static class BookmarkData extends DbIndices
	{
		ArrayList<Bookmark>mAr;
		Bookmark mCurBookmark;
		int mCurPos = -1;
		long mParentFolder = -1;
		ContextRef mContext;
		ArrayList<Long>mParents = new ArrayList<Long>();
		int mLevel = 0;
		public BookmarkData(Context c,ArrayList<Bookmark>ar,long parentFolder) {
			super(new DbConverter.EmptyWriter(), BOOKMARK_PROJECTIONS, BOOKMARK_TYPES);
			mContext = new ContextRef(c);
			mParentFolder = parentFolder;
			mParents.add(parentFolder);
		}
		@Override
		public int getIndexByName(String fieldName) {
			return -1;
		}
		@Override
		public void processRow(ContentValues cv) throws Throwable {
			cv.put(TITLE, mCurBookmark.getTitle());
			if(mCurBookmark.isBookmarkFolder())
			{
				Bookmark bm = stat.createBookmarkFolder(mContext.getContext(), mCurBookmark.getTitle(), mParentFolder);
				mParentFolder = (Long)bm.param;
			}
			else
			{
				stat.saveHistory(mContext.getContext(), mCurBookmark, null, false, null, mParentFolder);
			}
		}
		@Override
		public boolean writeContentValue(ContentValues cv, int type, int index,String fieldName) {
			return false;
		}
		@Override
		public boolean moveFirst() {
			if(mAr==null||mAr.isEmpty())
				return false;
			mCurPos = 0;
			mCurBookmark = mAr.get(mCurPos);
			return true;
		}
		@Override
		public boolean moveNext() {
			++mCurPos;
			return false;
		}
		
	}
}
