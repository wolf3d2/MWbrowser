package com.jbak.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import com.jbak.superbrowser.stat;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class DbUtils {
	public static interface StrConst
	{
		static final String _RANDOM ="RANDOM()";
		public static final String TEXT ="TEXT";
		public static final String INTEGER ="INTEGER";
		public static final String BLOB ="BLOB";
		public static final String COLLATE ="COLLATE";
		public static final String NOCASE ="NOCASE";
		public static final String _ID = "_id";
		public static final String ASC = "asc";
		public static final String DESC = "desc";
		static final String _SPACE = stat.STR_SPACE;
		public static final String _COMMA = ",";
		public static final String AND = "and";
		public static final String OR = "or";
		public static final String IF_NOT_EXISTS= "IF NOT EXISTS"; 
		
		public static final String _AUTOINCREMENT = "PRIMARY KEY AUTOINCREMENT NOT NULL";
	}
	public static class ShuffledCursor extends CursorWrapper
	{
		private ArrayList<Integer> mShuffle;
		int mOrigPos = -1;
		public ShuffledCursor(Cursor cursor) {
			super(cursor);
			int sz = cursor.getCount();
			makeShuffle(sz);
		}
		public ShuffledCursor(Cursor cursor,ArrayList<Integer>shuffle) {
			super(cursor);
			int sz = cursor.getCount();
			if(shuffle.size()==sz)
				mShuffle = shuffle;
			else
				makeShuffle(sz);
		}
		void makeShuffle(int sz)
		{
			mShuffle= new ArrayList<Integer>(sz);
			for(int i=0;i<sz;i++)
				mShuffle.add(Integer.valueOf(i));
			Collections.shuffle(mShuffle,new Random());
		}
		@Override
		public boolean moveToFirst() {
			return moveToPosition(mShuffle.get(0));
		}
		@Override
		public boolean move(int offset) {
			return moveToPosition(mOrigPos+offset);
		}
		@Override
		public final boolean moveToNext() {
			if(mOrigPos+1>=mShuffle.size())
				return false;
			return moveToPosition(mOrigPos+1);
		}
		@Override
		public boolean moveToPosition(int position) {
			boolean ret = super.moveToPosition(mShuffle.get(position));
			if(ret)
				mOrigPos = position;
			return ret;
		}
		public final ArrayList<Integer> getShuffle()
		{
			return mShuffle;
		}
	}
	public static ContentValues makeContentValues(Object ... items)
	{
		ContentValues cv = new ContentValues();
		for(int i=0;i<items.length;i+=2)
		{
			cv.put(items[i].toString(), items[i+1].toString());
		}
		return cv;
	}
	public static class CreateTable implements StrConst
	{
		public static final String CREATE_TABLE = "CREATE TABLE ";
		String mResult;
		boolean firstField = true;
		public CreateTable(String tableName,boolean ifNotExists) {
			mResult = CREATE_TABLE;
			if(ifNotExists)
				mResult+=_SPACE+IF_NOT_EXISTS+_SPACE;
			mResult+=tableName+"(";
		}
		void nextParam()
		{
			if(!firstField)
				mResult+=_COMMA;
			mResult+='\n';
			firstField = false;
		}
		public CreateTable addRow(String colName, String colType) {
			nextParam();
			mResult+=colName+stat.STR_SPACE+colType;
			return this;
		}
		public CreateTable addIdRow() {
			addRow(_ID, INTEGER, _AUTOINCREMENT);
			return this;
		}
		public CreateTable addRow(String colName, String colType, String params) {
			nextParam();
			mResult += colName+_SPACE+colType+_SPACE+params;
			return this;
		}
		public CreateTable addRowCollateNoCase(String colName) {
			return addRow(colName, TEXT, COLLATE+_SPACE+NOCASE);
		}
		public void create(SQLiteDatabase db)
		{
			mResult+=")";
			db.execSQL(toString());
		}
		@Override
		public String toString() {
			return mResult;
		}
	}
	/** Класс для выполнения запросов к БД */
	public static class Select implements StrConst
	{
		static final String ERR_NO_WHERE = "Do you forget call method where() ?";
		String mTable;
		String mWhere;
		String mOrderBy;
		String mLimit;
		ArrayList<String> mSelects;
		String[] mColumns;
		/**  Конструктор
		 * @param table название таблицы
		 */
		public Select(String table)
		{
			mTable = table;
		}
		public Select()
		{
			mTable = stat.STR_NULL;
		}
		public Select(Select sel)
		{
			mTable = sel.mTable;
			mWhere = sel.mWhere;
			mOrderBy = sel.mOrderBy;
			mLimit = sel.mLimit;
			if(sel.mColumns!=null)
			{
				mColumns = new String[sel.mColumns.length];
				System.arraycopy(sel.mColumns, 0, mColumns, 0, sel.mColumns.length);
			}
			if(sel.mSelects!=null)
			{
				mSelects = new ArrayList<String>(sel.mSelects);
			}
		}
		/** Начинает Where */
		public Select where()
		{
			mWhere = stat.STR_NULL;
			mSelects=null;
			return this;
		}
		public Select setWhere(String where,String selects[])
		{
			mWhere = where;
			if(selects!=null)
			{
			mSelects = new ArrayList<String>();
			for(String s:selects)
				mSelects.add(s);
			}
			return this;
		}
		public String getTable()
		{
			return mTable;
		}
		public boolean hasWhere()
		{
			return mWhere!=null;
		}
		private void check()
		{
			if(mWhere==null)
				throw new IllegalStateException(ERR_NO_WHERE);
		}
		private Select addToSelect(Object sel)
		{
			if(mSelects==null)
				mSelects = new ArrayList<String>();
			mSelects.add(sel.toString());
			return this;
		}
		/** Добавляет в where условие col = value
		 * @param col Название столбца
		 * @param value Требуемое значение. В БД ищется значение value.toString()
		 * @return
		 */
		public Select eq(String col,Object value)
		{
			return compare(col, "=", value);
		}
		public Select whereRaw(String value)
		{
			mWhere+=value;
			return this;
		}
		private Select compare(String col,String operand,Object value)
		{
			check();
			if(mWhere.length()>0)
				mWhere+=_SPACE;
			mWhere+=col+_SPACE+operand+_SPACE+'?'+_SPACE;
			return addToSelect(value);
		}
		public Select like(String col,String like)
		{
			return compare(col, "LIKE", like);
		}
		public Select less(String col,Object val)
		{
			return compare(col, "<", val);
		}
		public Select lessOrEqual(String col,Object val)
		{
			return compare(col, "<=", val);
		}
		public Select moreOrEqual(String col,Object val)
		{
			return compare(col, ">=", val);
		}
		public Select more(String col,Object val)
		{
			return compare(col, ">", val);
		}
		public Select notEqual(String col,Object val)
		{
			return compare(col, "!=", val);
		}
		/** Добавляет к Where условие and */
		public Select and()
		{
			check();
			mWhere+=_SPACE+AND+_SPACE;
			return this;
		}
		/** Добавляет к Where условие or */
		public Select or()
		{
			check();
			mWhere+=_SPACE+OR+_SPACE;
			return this;
		}
		/** Выставляет лимит количества выбираемых значений
		 * @param limit Лимит выборки */
		public Select limit(int limit)
		{
			this.mLimit=stat.STR_NULL+limit;
			return this;
		}
		/** Сортировка выборки 
		 * @param col Столбец сортировки
		 * @param asc true - по возрастанию, false - по убыванию
		 * @return
		 */
		public Select orderBy(String col,boolean asc)
		{
			mOrderBy=col+_SPACE+(asc?ASC:DESC);
			return this;
		}
		public Select orderBy(String order)
		{
			mOrderBy=order;
			return this;
		}
		/** Задает столбцы таблицы для выборки 
		 * @param cols Список столбцов 
		 */
		public Select columns(String ...cols)
		{
			mColumns = cols;
			return this;
		}
		public Cursor select(ContentResolver cr,Uri selectUri)
		{
			return  cr.query(selectUri, mColumns, mWhere, getSelectArgs(), mOrderBy);//db.query(mTable, mColumns, mWhere, getSelectArgs(), null, null, mOrderBy,mLimit);
		}
		public int deleteOpt(ContentResolver cr,Uri selectUri)
		{
			try{
				return cr.delete(selectUri, mWhere, getSelectArgs());
			}
			catch(Throwable e)
			{
				
			}
			return -1;
		}
		public Cursor selectOpt(ContentResolver cr,Uri selectUri)
		{
			Cursor c = null;
			try{
				c = select(cr, selectUri);
			}
			catch(Throwable e)
			{
				Utils.log(e);
			}
			return  c;
		}

		private String [] getSelectArgs()
		{
			if(mSelects!=null)
				return mSelects.toArray(new String[mSelects.size()]);
			return null;
		}
		@SuppressWarnings("deprecation")
		@Override
		public String toString() {
			SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
			String str = builder.buildQuery(mColumns, mWhere, getSelectArgs(), null, null, mOrderBy, mLimit);
			if(mSelects!=null)
				str+=" whereArgs:"+mSelects.toString();
			return str;
		}
		/** Выполняет выборку в базе db . В случае неудачи может бросить SQLException */
		public Cursor select(SQLiteDatabase db)
		{
			return db.query(mTable, mColumns, mWhere, getSelectArgs(), null, null, mOrderBy,mLimit);
		}
		public int update(ContentResolver cr,Uri contentUri,ContentValues cv)
		{
			try{
				return cr.update(contentUri, cv, mWhere, getSelectArgs());
			}
			catch(Throwable e)
			{
				Utils.log(e);
			}
			return -1;
		}
		public int update(SQLiteDatabase db,ContentValues cv)
		{
			try{
				return db.update(mTable, cv, mWhere, getSelectArgs());
			}
			catch(Throwable e)
			{
				Utils.log(e);
			}
			return -1;
		}
/** Возвращает количество элементов, соответствующих текущей выборке
 * @param db База данных для выполнения запроса
 * @return Количество жлементов или -1, если произошла ошибка при выборке
 */
		public int getCount(SQLiteDatabase db)
		{
			Cursor c = selectOrNull(db);
			if(c==null)
				return -1;
			int ret = c.getCount();
			c.close();
			return ret;
		}
		/** Возвращает true, если есть хоть один элемент, соответствующий текущей выборке */
		public boolean hasOne(SQLiteDatabase db)
		{
			return getCount(db)>0;
		}
		/** Возвращает курсор выборки или null в случае ошибки */
		public Cursor selectOrNull(SQLiteDatabase db)
		{
			try{
			return db.query(mTable, mColumns, mWhere, getSelectArgs(), null, null, mOrderBy,mLimit);
			}
			catch(Throwable e)
			{
				Utils.log(e);
			}
			return null;
		}
		
		public int delete(SQLiteDatabase db)
		{
			return db.delete(mTable, mWhere, getSelectArgs());
		}
		public int deleteOpt(SQLiteDatabase db)
		{
			try{
				return db.delete(mTable, mWhere, getSelectArgs());
			}
			catch(Throwable e)
			{
				Utils.log(e);
			}
			return -1;
		}
	}
	public static abstract class Transaction
	{
		SQLiteDatabase db;
		boolean ok = true;
		public Transaction(SQLiteDatabase db) {
			this.db = db;
		}
		public abstract void call(SQLiteDatabase db) throws Throwable;
		public boolean execute()
		{
			try{
				db.beginTransaction();
				call(db);
				db.setTransactionSuccessful();
			}
			catch(Throwable e)
			{
				ok = false;
			}
			finally
			{
				db.endTransaction();
			}
			return ok;
		}
	}
	public static String getStringFromCursorAndClose(Cursor c,String fieldName)
	{
		String ret = null;
		if(c==null)
			return ret;
		if(c.moveToFirst())
			ret = c.getString(c.getColumnIndex(fieldName));
		c.close();
		return ret;
	}
	public static String getOrder(String column,boolean asc)
	{
		if(asc)
			return column;
		return column+' '+StrConst.DESC;
	}
}
