package com.jbak.utils;

import ru.mail.mailnews.st;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jbak.superbrowser.IConst;

public class DbConverter implements IConst{
	public static final String TAG = "DbConverter";
	@SuppressLint("NewApi")
	public boolean copyTable(Cursor cursor, String projections[],SQLiteDatabase writeDb,String tableName,Integer types[])
	{
		DbWriter writer = new DbWriterDatabase(writeDb, tableName);
		DbIndices indices = new DbIndicesCursor(cursor, writer, projections, types);
		return convert(indices);
	}
	public boolean convert(DbIndices indices)
	{
		return indices.processData();
	}
	public boolean doInsertContentValues(SQLiteDatabase db,String table,ContentValues cv)
	{
		return db!=null;
	}
	public static interface DbWriter
	{
		public boolean writeData(ContentValues cv);
		public void startProcess(boolean start);
		public void onEnd(boolean ok);
	}
	public static abstract class DbIndices
	{
		public String projections[];
		public Integer types[];
		protected Integer indices[];
		DbWriter mWriter;
		public DbIndices(DbWriter writer,String projections[],Integer types[])
		{
			mWriter = writer;
			this.projections = projections;
			this.types = types;
			if(projections!=null)
				indices = new Integer[projections.length];
		}
		public abstract int getIndexByName(String fieldName);
		public abstract boolean writeContentValue(ContentValues cv,int type,int index,String fieldName) throws Throwable;
		public abstract boolean moveFirst();
		public abstract boolean moveNext() throws Throwable;
		public boolean processData()
		{
			boolean ok = false;
			try{
				mWriter.startProcess(true);
				ContentValues cv = new ContentValues();
				if(moveFirst())
				{
					st.LogTime lt = new st.LogTime(TAG);
					int pos = 0;
					do{
						processRow(cv);
						++pos;
						if(pos%30==0)
							lt.log("+30");
	
					}
					while(moveNext());
					lt.log("end");
	
				}
				mWriter.startProcess(false);
				ok = true;
			}
			catch (Throwable e) {
			}
			mWriter.onEnd(ok);
			return ok;
		}
		
		public void processRow(ContentValues cv) throws Throwable
		{
			cv.clear();
			int pos = 0;
			for(String p:projections)
			{
				Integer index = null;
				index = indices[pos];
				if(index==null)
				{
					index = getIndexByName(p);
					indices[pos]=index;
				}
				Integer type = types[pos];
				writeContentValue(cv, type, index, p);
				mWriter.writeData(cv);
				++pos;
			}
		}
		@Override
		public String toString() {
			return projections==null?super.toString():projections.toString();
		}
		
	}
	public static class DbWriterDatabase implements DbWriter
	{
		SQLiteDatabase mDb;
		String mTableName;
		public DbWriterDatabase(SQLiteDatabase db,String tableName) {
			mDb = db;
			mTableName = tableName;
		}
		@Override
		public boolean writeData(ContentValues cv) {
			mDb.insert(mTableName, mTableName, cv);
			return true;
		}

		@Override
		public void startProcess(boolean start) {
			if(start)
				mDb.beginTransaction();
			else
				mDb.setTransactionSuccessful();
		}
		@Override
		public void onEnd(boolean ok) {
			mDb.endTransaction();
		}
		
	}
	public static class DbIndicesCursor extends DbIndices
	{
		Cursor mCursor;
		public DbIndicesCursor(Cursor c, DbWriter writer, String[] projections, Integer[] types) {
			super(writer, projections, types);
			mCursor = c;
		}
		@Override
		public int getIndexByName(String fieldName) {
			return mCursor.getColumnIndex(fieldName);
		}
		@Override
		public boolean writeContentValue(ContentValues cv, int type, int index,String fieldName) {
			switch (type) 
			{
				case Cursor.FIELD_TYPE_STRING: cv.put(fieldName, mCursor.getString(index)); break;
				case Cursor.FIELD_TYPE_FLOAT: cv.put(fieldName, mCursor.getFloat(index)); break;
				case Cursor.FIELD_TYPE_INTEGER: cv.put(fieldName, mCursor.getLong(index)); break;
				case Cursor.FIELD_TYPE_NULL: return false;
				case Cursor.FIELD_TYPE_BLOB: cv.put(fieldName, mCursor.getBlob(index)); break;
			}
			return true;
		}
		@Override
		public boolean moveFirst() {
			return mCursor.moveToFirst();
		}
		@Override
		public boolean moveNext() {
			return mCursor.moveToNext();
		}
		
	}
	public static class EmptyWriter implements DbWriter
	{

		@Override
		public boolean writeData(ContentValues cv) {
			return false;
		}

		@Override
		public void startProcess(boolean start) {
			
		}

		@Override
		public void onEnd(boolean ok) {
			
		}
		
	}
}
