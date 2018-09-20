package com.jbak.superbrowser;

import java.util.ArrayList;

import ru.mail.mailnews.st;
import ru.mail.mailnews.st.UniObserver;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.provider.Browser;

import com.jbak.superbrowser.ui.LoadBitmapInfo;
import com.jbak.utils.DbUtils;
import com.jbak.utils.Utils;

public class BitmapLoader extends st.SyncAsycOper implements IConst
	{
		public boolean started = true;
		ArrayList<LoadBitmapInfo> mLoads = new ArrayList<LoadBitmapInfo>();
		ArrayList<LoadBitmapInfo> mProcessed = new ArrayList<LoadBitmapInfo>();
		OnBitmapLoadListener mLoadListener;
		protected ContentResolver mCr;
		public BitmapLoader(ContentResolver cr,OnBitmapLoadListener listener,LoadBitmapInfo info){
			mLoads.add(info);
			mCr = cr;
			mLoadListener = listener;
		}
		public void addLoad(LoadBitmapInfo info)
		{
			mLoads.add(info);
		}
		protected Cursor getBitmapCursor(LoadBitmapInfo info)
		{
			Cursor c; 
			if(BrowserApp.DB_TYPE==BrowserApp.DB_OWN||Db.CONVERTED_HISTORY)
				c = Db.getBookmarksTable().select().columns(FAVICON,THUMBNAIL).where().eq(URL, info.bm.getUrl()).select(Db.getBookmarksTable().getDb());
			else
				c = new DbUtils.Select(stat.BITMAP).columns(IConst.FAVICON,IConst.THUMBNAIL,IConst.URL).where().eq(Browser.BookmarkColumns.URL, info.bm.getUrl()).select(mCr, Browser.BOOKMARKS_URI);
			return c;
		}
		public boolean loadInfo(LoadBitmapInfo info) throws Throwable
		{
			boolean ret = false;
			Cursor c = getBitmapCursor(info);
			if(c!=null&&c.moveToNext())
			{
				int index = c.getColumnIndex(Browser.BookmarkColumns.FAVICON);
				byte bytes[] = null;
				if(index>-1)
					bytes = c.getBlob(index);
				if(bytes!=null&&bytes.length>0)
					info.favicon = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
				bytes = null;
				index = c.getColumnIndex(IConst.THUMBNAIL);
				if(index>-1)
					bytes = c.getBlob(index);
				if(bytes!=null&&bytes.length>0)
					info.thumbnail = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
				ret = true;
			}
			if(c!=null)
				c.close();
			return ret;
		}
		@Override
		public void makeOper(UniObserver obs) throws Throwable {
			while(mLoads.size()>0)
			{
				try{
					LoadBitmapInfo li = mLoads.remove(0);
					mProcessed.add(li);	
					if(loadInfo(li))
						publishProgress();
				}
				catch(Throwable e)
				{
					Utils.log(e);
				}
			}
			started = false;
		}
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
//		if(mLoadListener!=null)
//			mLoadListener.onBitmapsLoad();
			while (mProcessed.size()>0) {
				LoadBitmapInfo li = mProcessed.remove(0);
				mLoadListener.onBitmapLoad(li);
			}
		}
		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
			
		}
//		public static class FileBitmapLoader extends BitmapLoader
//		{
//			int mSize = 300;
//			public FileBitmapLoader(Context c,OnBitmapLoadListener listener, LoadBitmapInfo info) {
//				super(null, listener, info);
//			}
//			@Override
//			public boolean loadInfo(LoadBitmapInfo info) throws Throwable {
//				File f = (File)info.param;
//				try{
//					info.thumbnail =  ImageDownloader.decodeSampledBitmapFromFile(f.getAbsolutePath(), mSize, mSize);
//					return info.thumbnail!=null;
//				}
//				catch(Throwable e)
//				{
//					e.printStackTrace();
//				}
//				return false;
//			}
//		}
	}