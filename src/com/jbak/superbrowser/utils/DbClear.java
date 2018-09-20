package com.jbak.superbrowser.utils;

import android.content.Context;
import android.database.Cursor;

import com.jbak.superbrowser.BrowserApp;
import com.jbak.superbrowser.Db;
import com.jbak.superbrowser.IConst;
import com.mw.superbrowser.R;
import com.jbak.utils.ContextRef;
import com.jbak.utils.DbUtils.Select;
import com.jbak.utils.ObjectKeyValues;
import com.jbak.utils.Utils;

public class DbClear extends ContextRef implements IConst{
	public static final int CLEAR_ALL = 0;
	public static final int CLEAR_HOUR= 1;
	public static final int CLEAR_TODAY = 2;
	public static final int CLEAR_LEAVE_10 = 3;
	public static final int CLEAR_LEAVE_50 = 4;
	public static final int CLEAR_LEAVE_100 = 5;
	public static final int CLEAR_LEAVE_TODAY = 6;
	public static final int CLEAR_LEAVE_WEEK = 7;
	public static final int CLEAR_LEAVE_HOUR = 8;
	public static final int CLEAR_LEAVE_150 = 9;
	public static final int CLEAR_LEAVE_200 = 10;
	public static final int CLEAR_LEAVE_MONTH = 11;
	public static final ObjectKeyValues<Integer, Integer> CLEAR_HISTORY_TYPES = new ObjectKeyValues<Integer, Integer>
	(	DbClear.CLEAR_ALL,R.string.clear_all,
		DbClear.CLEAR_HOUR,R.string.clear_hour,
		DbClear.CLEAR_TODAY,R.string.clear_today,
		DbClear.CLEAR_LEAVE_10,R.string.clear_leave_10,
		DbClear.CLEAR_LEAVE_50,R.string.clear_leave_50,
		DbClear.CLEAR_LEAVE_100,R.string.clear_leave_100,
		DbClear.CLEAR_LEAVE_150,R.string.clear_leave_150,
		DbClear.CLEAR_LEAVE_200,R.string.clear_leave_200,
		DbClear.CLEAR_LEAVE_HOUR,R.string.clear_leave_hour,
		DbClear.CLEAR_LEAVE_TODAY,R.string.clear_leave_today,
		DbClear.CLEAR_LEAVE_WEEK,R.string.clear_leave_week,
		DbClear.CLEAR_LEAVE_MONTH,R.string.clear_leave_month
	);
	public static int clearHistory(Context c,int type,ClearData clearData) 
	{		
		int del = -1;
		try{
			if(BrowserApp.DB_TYPE==BrowserApp.DB_OWN||Db.CONVERTED_HISTORY)
			{
				switch (type) {
				case DbClear.CLEAR_ALL:
					return clearData.deleteAll();
				case DbClear.CLEAR_LEAVE_10:	
				case DbClear.CLEAR_LEAVE_50:	
				case DbClear.CLEAR_LEAVE_100:
				case DbClear.CLEAR_LEAVE_150:
				case DbClear.CLEAR_LEAVE_200:
				{
					int count  = 10;
					if(type==DbClear.CLEAR_LEAVE_50)
						count = 50;
					else if(type==DbClear.CLEAR_LEAVE_100)
						count = 100;
					else if(type==DbClear.CLEAR_LEAVE_150)
						count = 150;
					else if(type==DbClear.CLEAR_LEAVE_200)
						count = 200;
					Cursor cursor = clearData.getAllIdsCursor(c);
					if(cursor.moveToPosition(count))
					{
						long id = cursor.getLong(0);
						del = clearData.deleteById(id, true);
					}
					cursor.close();
					return del;
				}
				case DbClear.CLEAR_HOUR:
				case DbClear.CLEAR_TODAY:
				{
					long interval = type==DbClear.CLEAR_HOUR?HOUR_MILLIS:DAY_MILLIS;
					long startDel = System.currentTimeMillis()-interval;
					del = clearData.deleteByDate(startDel, false);
					return del;
				}
				case DbClear.CLEAR_LEAVE_HOUR:
				case DbClear.CLEAR_LEAVE_TODAY:
				case DbClear.CLEAR_LEAVE_WEEK:
				{
					long interval = type==DbClear.CLEAR_LEAVE_TODAY?DAY_MILLIS:
									type==DbClear.CLEAR_LEAVE_HOUR?HOUR_MILLIS:WEEK_MILLIS;
					long startDel = System.currentTimeMillis()-interval;
					del = clearData.deleteByDate(startDel, true);
					return del;
				}
				case DbClear.CLEAR_LEAVE_MONTH:
				{
					long interval = IConst.MONTH_MILLIS;
					long startDel = System.currentTimeMillis()-interval;
					del = clearData.deleteByDate(startDel, true);
					return del;
				}
			}
				
			}
//			else
//				del = c.getContentResolver().delete(BrowserContract.History.CONTENT_URI, null, null);
		}
		catch(Throwable e)
		{
			Utils.log(e);
		}
		return del;
	}
	public static class ClearDataExtHistory implements ClearData
	{
		Integer mType;
		public ClearDataExtHistory(int type) {
			mType = Integer.valueOf(type);
		}
		@Override
		public Cursor getAllIdsCursor(Context context) {
			return Db.getExtHistory().select().columns(_ID).where().eq(TYPE, mType).orderBy(DATE, false).selectOrNull(Db.getExtHistory().getDb());
		}

		@Override
		public int deleteById(long id, boolean lessOrEquals) {
			return Db.getExtHistory().select().where().eq(TYPE, mType).and().lessOrEqual(_ID, id).deleteOpt(Db.getBookmarksTable().getDb());
		}

		@Override
		public int deleteByDate(long startDel, boolean lessOrEquals) {
			Select sel = Db.getExtHistory().select()
					.where()
					.eq(TYPE, mType)
					.and();
			if(lessOrEquals)
				sel.lessOrEqual(DATE, startDel);
			else	
				sel.moreOrEqual(DATE, startDel);
			return sel.deleteOpt(Db.getExtHistory().getDb());
		}
		@Override
		public int deleteAll() {
			return Db.getExtHistory().getSelectByType(mType).deleteOpt(Db.getExtHistory().getDb());
		}
		
	}
	
	public static class ClearDataHistory implements ClearData
	{
		@Override
		public Cursor getAllIdsCursor(Context context) {
			return Db.getBookmarksTable().getHistoryCursor(Db._ID);
		}

		@Override
		public int deleteById(long id, boolean lessOrEquals) {
			return Db.getBookmarksTable().select().where().lessOrEqual(_ID, id).and().eq(TYPE, Db.TableBookmarks.TYPE_HISTORY).deleteOpt(Db.getBookmarksTable().getDb());
		}

		@Override
		public int deleteByDate(long startDel, boolean lessOrEquals) {
			Select sel = Db.getBookmarksTable().select()
					.where()
					.eq(TYPE, Db.TableBookmarks.TYPE_HISTORY)
					.and();
			if(lessOrEquals)
				sel.lessOrEqual(DATE, startDel);
			else	
				sel.moreOrEqual(DATE, startDel);
			return sel.deleteOpt(Db.getBookmarksTable().getDb());
		}

		@Override
		public int deleteAll() {
			return Db.getBookmarksTable().clearHistory();
		}
	}
	
	public static interface ClearData
	{
		public Cursor getAllIdsCursor(Context context);
		public int deleteAll();
		public int deleteById(long id,boolean lessOrEquals);
		public int deleteByDate(long id,boolean lessOrEquals);
		
	}

}
