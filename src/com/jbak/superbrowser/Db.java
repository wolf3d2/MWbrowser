package com.jbak.superbrowser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.Browser;
import android.text.TextUtils;
import ru.mail.mailnews.st;

import com.jbak.utils.DbConverter;
import com.jbak.utils.DbUtils;
import com.jbak.utils.DbUtils.CreateTable;
import com.jbak.utils.DbUtils.Select;
import com.jbak.utils.DbUtils.StrConst;
import com.jbak.utils.DbUtils.Transaction;
import com.jbak.utils.ObjectKeyValues;
import com.jbak.utils.Utils;

public class Db extends SQLiteOpenHelper implements StrConst,IConst{
	
	public static final String LAST_BOOKMARK_FOLDER_ID = "last_bookmark_folder_id";
	public static final String OPEN_WINDOWS_IDS = "open_window_ids";
	public static final int VERSION = 7;
	public static final String TABLE_WINDOWS_HISTORY = "windows_history";
	public static final String TABLE_STRING_VALUES = "string_values";
	public static final String TABLE_BOOKMARKS = "bookmarks";
	public static final String TABLE_SEARCHES = "searches";
	public static final String TABLE_EXT_HISTORY = "extendedHistory";

	public static boolean CONVERTED_HISTORY = false;
//	public static final String OLD_DB_NAME = "sqlite.db"; 
	public static final String DB_NAME = "sqlite.db"; 
	public static String DB_PATH = DB_NAME; 
//	.addRow(URL, TEXT)
//	.addRow(TITLE, TEXT)
//	.addRow(TYPE, INTEGER)
//	.addRow(DATE, INTEGER)
//	.addRow(PARENT, INTEGER)
//	.addRow(VISITS, INTEGER)
//	.addRow(EXTRA, TEXT)
//	.addRow(FAVICON, BLOB)
//	.addRow(THUMBNAIL, BLOB)

	@SuppressLint("InlinedApi")
	public static ObjectKeyValues<Integer, String> HISTORY_EXPORT_PROJECTIONS = new ObjectKeyValues<Integer, String>
	(
		Cursor.FIELD_TYPE_STRING,  URL,
		Cursor.FIELD_TYPE_STRING,  TITLE,
		Cursor.FIELD_TYPE_INTEGER,  DATE,
		Cursor.FIELD_TYPE_BLOB,  THUMBNAIL,
		Cursor.FIELD_TYPE_BLOB,  FAVICON
	);
	
	static Db INSTANCE;
	HashMap<String, BaseTable> mMap = new HashMap<String, Db.BaseTable>();
	private Db(Context context) {
		super(context, DB_PATH, null, VERSION);
	}
	public static void create(Context c)
	{
		INSTANCE = new Db(c);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		makeTables(db);
	}
	void makeTables(SQLiteDatabase db)
	{
		new CreateTable(TABLE_WINDOWS_HISTORY,true)
		.addIdRow()
		.addRow(WINDOW_ID, INTEGER)
		.addRow(SETTINGS, TEXT)
		.addRow(CURRENT_PAGE, TEXT)
		.addRow(WEB_VIEW_BUNDLE, BLOB)
		.addRow(THUMBNAIL,BLOB)
		.addRow(FAVICON,BLOB)
		.addRow(CLOSED_DATE,INTEGER)
		.create(db);
		new CreateTable(TABLE_STRING_VALUES,true)
		.addRow(NAME, TEXT)
		.addRow(VALUE, TEXT)
		.create(db);
		// используется для хранения закладок и истории
		new CreateTable(TABLE_BOOKMARKS,true)
		.addIdRow()
		.addRow(URL, TEXT)
		.addRow(TITLE, TEXT)
		// type = 
		// 0 -история
		// 1- закладка
		// 2 - папка закладки
		.addRow(TYPE, INTEGER)
		.addRow(DATE, INTEGER)
		// пусто - строка истории
		// 1 - корневая папка
		// >1 - id верхней папки
		.addRow(PARENT, INTEGER)
		.addRow(VISITS, INTEGER)
		.addRow(EXTRA, TEXT)
		.addRow(FAVICON, BLOB)
		// если нет, то это папка
		.addRow(THUMBNAIL, BLOB)
		.create(db);
		new CreateTable(TABLE_SEARCHES,true)
		.addIdRow()
		.addRow(DATE, TEXT)
		.addRow(SEARCH, TEXT)
		.addRow(EXTRA, TEXT)
		.create(db);
		new CreateTable(TABLE_EXT_HISTORY,true)
		.addIdRow()
		.addRow(URL, TEXT)
		.addRow(TITLE, TEXT)
		.addRow(FILEPATH, TEXT)
		.addRow(TYPE, INTEGER)
		.addRow(DATE, INTEGER)
		.addRow(VISITS, INTEGER)
		.addRow(EXTRA, TEXT)
		.addRow(THUMBNAIL, BLOB)
		.create(db);	
		}
	private static BaseTable createTable(String tableName)
	{
		if(TABLE_WINDOWS_HISTORY.equals(tableName))
			return new TableWindowHistory();
		if(TABLE_STRING_VALUES.equals(tableName))
			return new TableStrValues();
		if(TABLE_BOOKMARKS.equals(tableName))
			return new TableBookmarks(tableName);
		if(TABLE_SEARCHES.equals(tableName))
			return new TableSearch(tableName);
		if(TABLE_EXT_HISTORY.equals(tableName))
			return new TableExtHistory(tableName);
		return null;
	}
	public static TableSearch getSearchTable()
	{
		return (TableSearch) getTable(TABLE_SEARCHES);
	}
	public static TableExtHistory getExtHistory()
	{
		return (TableExtHistory) getTable(TABLE_EXT_HISTORY);
	}
	public static TableBookmarks getBookmarksTable()
	{
		return (TableBookmarks) getTable(TABLE_BOOKMARKS);
	}
	public static TableWindowHistory getWindowTable()
	{
		return (TableWindowHistory) getTable(TABLE_WINDOWS_HISTORY);
	}
	public static TableStrValues getStringTable()
	{
		return (TableStrValues) getTable(TABLE_STRING_VALUES);
	}
	public static BaseTable getTable(String tableName)
	{
		BaseTable bt = Db.INSTANCE.mMap.get(tableName);
		if(bt==null)
		{
			bt = createTable(tableName);
			Db.INSTANCE.mMap.put(tableName, bt);
		}
		return bt;
	}
	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		makeTables(arg0);
	}
	public static abstract class BaseTable
	{
		protected String mTableName;
		public BaseTable(String tableName)
		{
			mTableName = tableName;
		}
		public final String getTableName()
		{
			return mTableName;
		}
		public Cursor selectAll()
		{
			return new DbUtils.Select(mTableName).selectOrNull(getDb());
		}
		public long saveContentValues(ContentValues cv)
		{
			long ins  = getDb().insert(mTableName, mTableName, cv);
			return ins;
		}
		public final SQLiteDatabase getDb()
		{
			return Db.INSTANCE.getWritableDatabase();
		}
		public final SQLiteDatabase getReadDb()
		{
			return Db.INSTANCE.getReadableDatabase();
		}
		public void clear()
		{
			new DbUtils.Select(mTableName).deleteOpt(getDb());
		}
		public Select select()
		{
			return new Select(mTableName);
		}
	}
	public static class TableWindowHistory extends BaseTable
	{

		public TableWindowHistory() {
			super(TABLE_WINDOWS_HISTORY);
		}
		public void saveBookmark(Bookmark bookmark,int windowId)
		{
			ContentValues cv = bookmark.getContentValues();
			cv.put(WINDOW_ID, windowId);
			saveContentValues(cv);
		}
		public void deleteWindow(int windowId)
		{
			select().where().eq(WINDOW_ID, windowId).deleteOpt(getDb());
		}
		public int setWindowClosed(int windowId,boolean close)
		{
			ContentValues cv = new ContentValues();
			cv.put(CLOSED_DATE, close?System.currentTimeMillis():0);
			int upd = 0;
			if(windowId>0)
				upd = select().where().eq(WINDOW_ID, windowId).update(getDb(), cv);
			else
				upd = select().update(getDb(), cv);
			return upd;
		}
		public void setCloseAllWindows(boolean close)
		{
			setWindowClosed(0,close);
		}
		public Tab loadWindowFromCursor(Context context,Cursor c,boolean loadSettings)
		{
			Tab ww = null;
			try{
				ww = new Tab(context, c.getInt(c.getColumnIndex(WINDOW_ID)),null);
				Bookmark bm = null;
				ww.closedTime = c.getLong(c.getColumnIndex(CLOSED_DATE));
				String js = c.getString(c.getColumnIndex(CURRENT_PAGE));
				if(!TextUtils.isEmpty(js))
					bm = Bookmark.fromJSON(new JSONObject(js));
				if(bm==null)
					bm = new Bookmark(st.STR_NULL, "<EMPTY PAGE>",System.currentTimeMillis());
				byte bytes[] = c.getBlob(c.getColumnIndex(Browser.BookmarkColumns.FAVICON));
				if(bytes!=null&&bytes.length>0)
					bm.param = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
				ww.setCurrentBookmark(bm);
				if(loadSettings)
				{
					js = c.getString(c.getColumnIndex(SETTINGS));
					JSONObject obj = new JSONObject(js);
					ww.setJson(obj);
					bytes = c.getBlob(c.getColumnIndex(WEB_VIEW_BUNDLE));
					if(bytes!=null&&bytes.length>0)
						ww.savedState = stat.bytesToBundle(bytes);
						
				}
			}
			catch(Throwable e)
			{
				Utils.log(e);
			}
			return ww;
		}
		ArrayList<Tab> loadAllWindows(Context context,boolean loadSettings)
		{
			ArrayList<Tab> ar = new ArrayList<Tab>();
			Cursor c =  new Select(mTableName).selectOrNull(getDb());
			if(c.moveToFirst())
			{
				do{
					Tab ww = loadWindowFromCursor(context, c,loadSettings);
					if(ww!=null)
						ar.add(ww);
				}
				while(c.moveToNext());
			}
			c.close();
			return ar;

		}
		public ArrayList<Integer> getAllWindowsIds()
		{
			return getAllWindowsIds(false);
		}
		public ArrayList<Integer> getAllWindowsIds(boolean closed)
		{
			ArrayList<Integer>ar = new ArrayList<Integer>();
			Select sel = new Select(mTableName).columns(WINDOW_ID).where();
			if(closed)
				sel.notEqual(CLOSED_DATE, 0);
			else
				sel.eq(CLOSED_DATE, 0);
			Cursor c = sel.selectOrNull(getDb());
			if(c!=null)
			{
				if(c.moveToFirst())
				{
					do
					{
						ar.add(c.getInt(0));
					}
					while(c.moveToNext());
				}
				c.close();
			}
			return ar;
		}
		public Cursor getAllWindowsCursor()
		{
			return new Select(mTableName).where().eq(CLOSED_DATE, 0).selectOrNull(getDb());

		}
		public int clearClosedWindows()
		{
			return select().where().notEqual(CLOSED_DATE, 0).deleteOpt(getDb());
		}
		public Cursor getAllClosedWindowsCursor()
		{
			return new Select(mTableName).where().notEqual(CLOSED_DATE, 0).selectOrNull(getDb());
		}
		public Tab loadWindow(Context context, int windowId,boolean loadSettings)
		{
			Tab ww = null;
			Cursor c =  new Select(mTableName).where().eq(WINDOW_ID, windowId).selectOrNull(getReadDb());
			if(c!=null)
			{
				try{
					if(c.moveToFirst())
						ww = loadWindowFromCursor(context, c,loadSettings);
				}
				catch(Throwable e)
				{
				}
				c.close();
			}
			if(ww==null)
				ww = new Tab(context, windowId,null);
			return ww;
		}
		public void saveWindow(final Tab wnd)
		{
			if(wnd.getWebView()==null)
				return;
			new Transaction(getDb())
			{
				@Override
				public void call(SQLiteDatabase db) throws Throwable {
					ContentValues cv = new ContentValues();
					cv.put(WINDOW_ID, wnd.windowId);
					cv.put(SETTINGS, wnd.toJson().toString());
					cv.put(CLOSED_DATE, wnd.closedTime);
					if(wnd.getFavicon()!=null)
						cv.put(FAVICON, stat.bitmapToByte(wnd.getFavicon()));
					if(wnd.mThumbnail!=null)
						cv.put(THUMBNAIL, stat.bitmapToByte(wnd.mThumbnail));
					if(wnd.savedState!=null)
						cv.put(WEB_VIEW_BUNDLE, stat.bundleToBytes(wnd.savedState));
					Bookmark bm = wnd.getCurBookmark();
					if(bm!=null)
						cv.put(CURRENT_PAGE, bm.getJSON().toString());
					int upd = new Select(mTableName).where().eq(WINDOW_ID, wnd.windowId).update(db, cv);
					if(upd<1)
						saveContentValues(cv);
				}
				
			}.execute();
			BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_WINDOWS_CHANGED, wnd);
		}
		public Cursor getCursorForWindow(int windowId)
		{
			return new DbUtils.Select(mTableName).where().eq(WINDOW_ID, windowId).select(getDb());
		}
		
	}
	public static class TableStrValues extends BaseTable
	{

		public TableStrValues() {
			super(TABLE_STRING_VALUES);
		}
		public void save(String name,String value)
		{
			ContentValues cv = DbUtils.makeContentValues(NAME,name,VALUE,value);
			int upd = new Select(mTableName).where().eq(NAME, name).update(getDb(), cv);
			if(upd<1)
				saveContentValues(cv);
		}
		public void saveList(String name,List<String> list)
		{
			saveList(name, list, 0);
		}
		public ArrayList<Integer> getIntArray(String name)
		{
			ArrayList<Integer>ar = new ArrayList<Integer>();
			String s = get(name);
			if(s==null)
				return null;
			if(TextUtils.isEmpty(s))
				return ar;
			String str[] = s.split(",");
			for(String v:str)
			{
				ar.add(Integer.decode(v));
			}
			return ar;
		}
		public void saveIntArray(String name,List<Integer>ints)
		{
			int sz = ints.size();
			String str = st.STR_NULL;
			for(int i=0;i<sz;i++)
			{
				str+=ints.get(i);
				if(i<sz-1)
					str+=',';
			}
			save(name, str);
		}
		public void saveList(String name,List<String> list,int limit)
		{
			int startPos = 0;
			if(limit>0)
			{
				startPos = list.size()-limit;
				if(startPos<0)
					startPos = 0;
			}
			int pos = 0;
			JSONArray ar = new JSONArray();
			for(String s:list)
			{
				if(pos>=startPos)
					ar.put(s);
				++pos;
			}
			save(name, ar.toString());
		}
		public List<String> loadList(String name)
		{
			String s = get(name);
			ArrayList<String>ar = new ArrayList<String>();
			if(!TextUtils.isEmpty(s))
			{
				try{
					JSONArray jar = new JSONArray(s);
					int sz = jar.length();
					for(int i=0;i<sz;i++)
						ar.add(jar.getString(i));
				}
				catch(Throwable ignor){}
			}
			return ar;
		}
		public String get(String name)
		{
			Cursor c = new Select(mTableName).where().eq(NAME, name).selectOrNull(getDb());
			return DbUtils.getStringFromCursorAndClose(c, VALUE);
		}
		public boolean delete(String name)
		{
			int del = new Select(mTableName).where().eq(NAME, name).deleteOpt(getDb());
			return del>0;
		}
	}
	public static class TableExtHistory extends BaseTable
	{
		public static final int VIDEO = 1;
		public static final int DOWNLOAD = 2;
		public static final int SAVED_PAGE = 3;
		public static final int TYPE_USER_SEARCH = 4;
		public TableExtHistory(String tableName) {
			super(tableName);
		}
		public void insertBookmark(int type,Bookmark bm,String filename,Bitmap thumbnail)
		{
			ContentValues cv = bm.getContentValues(null,thumbnail,-1);
			if(!TextUtils.isEmpty(filename))
				cv.put(FILEPATH, filename);
			cv.put(TYPE, type);
			int upd = select().where().eq(URL, bm.getUrl()).update(getDb(), cv);
			if(upd>0)
				return;
			getDb().insert(mTableName, FILEPATH, cv);
		}
		public static String getSearchJson(String searchText,int searchAction)
		{
			JSONObject obj = new JSONObject();
			try{
				obj.put(IConst.SEARCH, searchText);
				obj.put(IConst.SEARCH_ACTION, searchAction);
				return obj.toString();
			}
			catch(Throwable e)
			{}
			return null;
		}
		public boolean insertUserSearch(String searchText,int searchAction)
		{
			String json = getSearchJson(searchText, searchAction);
			if(json==null)
				return false;
			ContentValues cv = new ContentValues();
			cv.put(TYPE, TYPE_USER_SEARCH);
			cv.put(TITLE, searchText);
			cv.put(EXTRA, json);
			int upd = select().where().eq(TITLE, searchText).update(getDb(), cv);
			if(upd>0)
				return true;
			long v = getDb().insert(mTableName, FILEPATH, cv);
			return v>0;
		}
		public String getFileNameById(long id)
		{
			Cursor c = select().columns(FILEPATH).where().eq(_ID, id).selectOrNull(getDb());
			return DbUtils.getStringFromCursorAndClose(c, FILEPATH);
		}
		public Select getSelectByType(int type)
		{
			return select().where().eq(TYPE, type);
		}
		public Cursor getCursorByType(int type)
		{
			return getSelectByType(type).orderBy(DATE, false).selectOrNull(getDb());
		}
	}
	public static class TableBookmarks extends BaseTable
	{
		public static final int TYPE_HISTORY = 0;
		public static final int TYPE_BOOKMARK = 1;
		public static final int TYPE_FOLDER = 2;
		public TableBookmarks(String tableName) {
			super(tableName);
		}
		public Cursor getHistoryCursor(String ...columns)
		{
			return select().columns(columns).where().eq(TYPE, TYPE_HISTORY).orderBy(DATE, false).selectOrNull(getDb());
		}
		/** возвращает курсор всех записей (и закладок и истории) */
		public Cursor getBookmarkAllCursor(String ...columns)
		{
			return select().columns(columns).selectOrNull(getDb());
		}
		public Cursor getBookmarkCursorByParentId(long parentId)
		{
			return select().columns(null).where().eq(_ID, parentId).selectOrNull(getDb());
		}
		/** возвращает курсор истории */
		public Cursor getBookmarkCursor(String ...columns)
		{
			return select().columns(columns).where().eq(TYPE, TYPE_HISTORY).orderBy(DATE, false).selectOrNull(getDb());
		}
		public Cursor getBookmarkCursor(String []columns,String where,String[] whereArgs)
		{
			return select().columns(columns).setWhere(where, whereArgs).selectOrNull(getDb());
		}
		public Cursor getBookmarkCursor(String []columns,long parentId)
		{
			String orderBy = TYPE+' '+DESC+','+TITLE+' '+ASC;
			Select sel = select().columns(columns).where();
			if(parentId>0)
				sel.eq(TYPE, TYPE_FOLDER).and().eq(PARENT, parentId).or().eq(TYPE, TYPE_BOOKMARK).and().eq(PARENT, parentId);
			else
				sel.eq(TYPE, TYPE_BOOKMARK);
			sel.orderBy(orderBy);
			return sel.selectOrNull(getDb());
		}
		public int deleteFolder(long folderId)
		{
			int ret = select().where().eq(_ID, folderId).or().eq(PARENT, folderId).deleteOpt(getDb());
			BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_BOOKMARKS_CHANGED, folderId);
			return ret;
		}
		public int update(long id,ContentValues cv)
		{
			int ret = getDb().update(getTableName(), cv, "_id=?", new String[]{Long.toString(id)});
			BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_BOOKMARKS_CHANGED, null);
			return ret;
		}
		public static boolean isUnique(int type)
		{
			return type == TYPE_HISTORY;
		}
		public boolean insertBookmark(boolean history,Bookmark bm,Bitmap favicon,Bitmap thumbnail, long parent)
		{
			return insertBookmark(history?TYPE_HISTORY:TYPE_BOOKMARK, bm, favicon, thumbnail, parent)>=0;
		}
		public long insertBookmark(int type,Bookmark bm,Bitmap favicon,Bitmap thumbnail, long parent)
		{
			boolean unique = isUnique(type);
			ContentValues cv = bm.getContentValues(favicon, thumbnail, parent);
			cv.put(TYPE, type);
			if(unique)
			{
				int upd = select().where().eq(URL, bm.getUrl()).and().eq(TYPE, type).update(getDb(), cv);
				if(upd>0)
				{
					Utils.log("INSERT_HISTORY", "Updated:"+bm.toString());
					return 0;
				}
			}
			long ret = getDb().insert(getTableName(), BITMAP, cv);
			Utils.log("INSERT_HISTORY", "Inserted:"+bm.toString());
			if(type==TYPE_BOOKMARK||type==TYPE_FOLDER)
				BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_BOOKMARKS_CHANGED, bm);
			return ret;
		}
		public int delete(long bookmarkId)
		{
			int ret = select().where().eq(_ID, bookmarkId).deleteOpt(getDb());
			BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_BOOKMARKS_CHANGED, null);
			return ret;
		}
		public int deleteAllBookmark()
		{
			int ret = select().columns(new String[] {"*"}).where().more(PARENT, "0").deleteOpt(getDb());
			BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_BOOKMARKS_CHANGED, null);
			return ret;
		}
		public int clearHistory()
		{
			return select().where().eq(TYPE, TYPE_HISTORY).deleteOpt(getDb());
		}
		public int deleteHistoryByUrl(String url)
		{
			return select().where().eq(TYPE, TYPE_HISTORY).and().eq(URL, url).deleteOpt(getDb());
		}
	}
	public static class TableSearch extends BaseTable
	{
		public TableSearch(String tableName) {
			super(tableName);
		}
		public boolean addSearch(String text)
		{
			ContentValues cv = DbUtils.makeContentValues(SEARCH,text,DATE,System.currentTimeMillis());
			select().where().eq(SEARCH, text).deleteOpt(getDb());
			return getDb().insert(getTableName(), SEARCH, cv)>-1;
		}
		public Cursor getCursor(String where)
		{
			if(where!=null)
				return select().setWhere(where, null).orderBy(DATE, false).selectOrNull(getDb());
			else
				return select().orderBy(DATE, false).selectOrNull(getDb());
		}
		
	}
	public static final boolean needImportHistory(Context c)
	{
		if(BrowserApp.DB_TYPE==BrowserApp.DB_OWN)
			return false;
		CONVERTED_HISTORY = Prefs.getBoolean(Prefs.HISTORY_CONVERTED, false);
		if(CONVERTED_HISTORY)
			return false;
		return true;
	}
	public static boolean importHistory(Context c)
	{
		if(!needImportHistory(c))
			return false;
		getBookmarksTable().clearHistory();
		DbConverter conv = new DbConverter()
		{
			@Override
			public boolean doInsertContentValues(SQLiteDatabase db,String table, ContentValues cv) {
				cv.put(TYPE, TableBookmarks.TYPE_HISTORY);
				return true;
			}
		};
		Browser.truncateHistory(c.getContentResolver());
		Cursor cursor = c.getContentResolver().query(Browser.BOOKMARKS_URI, HISTORY_EXPORT_PROJECTIONS.getValues(), "bookmark = 0", null, DbUtils.getOrder(IConst.DATE, false));
		boolean copy =  conv.copyTable(cursor, HISTORY_EXPORT_PROJECTIONS.getValues(), INSTANCE.getWritableDatabase(), TABLE_BOOKMARKS, HISTORY_EXPORT_PROJECTIONS.getKeys());
		Prefs.setBoolean(Prefs.HISTORY_CONVERTED, true);
		CONVERTED_HISTORY = true;
		return copy;
	}

}
