
package com.jbak.superbrowser;

import com.mw.superbrowser.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.crypto.IllegalBlockSizeException;

import org.json.JSONObject;

import ru.mail.mailnews.st;
import ru.mail.mailnews.st.UniObserver;
import ru.mail.webimage.FileUtils;
import ru.mail.webimage.ImageCache;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.os.StatFs;
import android.provider.Browser;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.provider.MediaStore.Images;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.jbak.reverseEngine.BrowserContract;
import com.jbak.superbrowser.Db.TableBookmarks;
import com.jbak.superbrowser.adapters.BookmarkFolderAdapter;
import com.jbak.superbrowser.search.SearchSystem;
import com.jbak.superbrowser.ui.BookmarkView;
import com.jbak.superbrowser.ui.LoadBitmapInfo;
import com.jbak.superbrowser.ui.MyWebView;
import com.jbak.superbrowser.ui.dialogs.DialogBookmark;
import com.jbak.superbrowser.ui.dialogs.ThemedDialog;
import com.jbak.ui.ConfirmOper;
import com.jbak.ui.CustomPopup;
import com.jbak.ui.CustomProgress;
import com.jbak.utils.DbUtils;
import com.jbak.utils.StrBuilder;
import com.jbak.utils.Utils;

public class stat implements IConst {
	// press x, press y
	//public static float pr_x = 0;
	//public static float pr_y = 0;
	// однократное выделение
	public static boolean fl_one_select = false;
	// имя файла создающегося в кеше при смене юзером кодировки страницы
	public static String CODEPAGE_FILENAME = "/codepage.txt";
	// перегрузить страницу в PC моде
	// пока отключил - доделать!
	public static boolean fl_loadPCmode = false;

	public static String url =st.STR_NULL;
	public static final String START_URL = "https://google.com";
	public static byte[] bitmapToByte(Bitmap bitmap)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos); //bm is the bitmap object   
        byte[] b = baos.toByteArray();
        return b;
    }
	public static void saveHistory(Context c,Bookmark bm,Bitmap favicon,boolean history,Bitmap thumbnail)
	{
		saveHistory(c, bm, favicon, history, thumbnail, -1);
	}
	public static void saveHistory(Context c,Bookmark bm,Bitmap favicon,boolean history,Bitmap thumbnail,long parentDir)
	{
		Utils.log("SAVE_HISTORY","bm="+bm.toString());
		if (!Prefs.isHistoryMiniature()) {
			Resources res = c.getResources();
			BitmapDrawable bd = (BitmapDrawable) res.getDrawable(R.drawable.not_miniature);
			thumbnail = bd.getBitmap();
		}
		if(BrowserApp.DB_TYPE==BrowserApp.DB_OWN||history&&Db.CONVERTED_HISTORY)
		{
			Db.getBookmarksTable().insertBookmark(history, bm, favicon, thumbnail, parentDir);
			return;
		}
		ContentValues cv = bm.getManagedContentValues(history,favicon,thumbnail,parentDir);
		String select = " url=? ";
		if(history)
		{
			int upd = 0;
			if(BrowserApp.DB_TYPE==BrowserApp.DB_BROWSER_CONTRACT)
			{
				cv.remove(BOOKMARK);
				upd = new DbUtils.Select().where().like(URL, bm.getUrl()).update(c.getContentResolver(), BrowserContract.History.CONTENT_URI, cv);
//				upd = c.getContentResolver().update(BrowserContract.History.CONTENT_URI, cv, select, new String[]{bm.getUrl()});
			}
			if(upd<=0)
			{
//				Browser.updateVisitedHistory(c.getContentResolver(), bm.getUrl(), true);
//				Cursor cursor = new DbUtils.Select().where().eq(Browser.BookmarkColumns.URL, bm.getUrl()).select(c.getContentResolver(),Browser.BOOKMARKS_URI );
//				if(cursor!=null)
//				{
//					cursor.moveToFirst();
//					String[] cols = cursor.getColumnNames();
//					Utils.log("DB_COLUMNS", cols.toString());
//				}
				upd = new DbUtils.Select().where().eq(Browser.BookmarkColumns.URL, bm.getUrl()).and().eq(BOOKMARK, 0).update(c.getContentResolver(),Browser.BOOKMARKS_URI, cv);
			}
			if(upd>0)
				return;
			
		}
		Uri contentUri = Browser.BOOKMARKS_URI;
		if(BrowserApp.DB_TYPE==BrowserApp.DB_BROWSER_CONTRACT)
		{
			if(history)
				contentUri = BrowserContract.History.CONTENT_URI;
			else
				contentUri = BrowserContract.Bookmarks.CONTENT_URI;
		}
		try{
			c.getContentResolver().delete(contentUri, select, new String[]{bm.getUrl()});
			c.getContentResolver().insert(contentUri, cv);
		}
		catch(Throwable e)
		{
			Utils.log(e);
		}
	}
	public static boolean hasScheme(String url)
	{
		int index = url.indexOf(':');
		if(index>-1&&index<30)
		{
			String s = url.substring(0,index);
			boolean isEng = true;
			for(int i=0;i<s.length();i++)
			{
				char c = s.charAt(i);
				if(!('a'<=c&&c<='z'||'A'<=c&&c<='Z'))
				{
					isEng = false;
					break;
				}
				if(isEng)
					return true;
			}
		}
		return false;
	}
	public static boolean isWebAddr(String text)
	{
		if(TextUtils.isEmpty(text))
			return false;
		if(hasScheme(text))
			return  true;
		return text.indexOf(' ')<0&&text.indexOf('.')>-1;
	}
	public static BookmarkView getBookmarkView(Context c, int position, View convertView, Bookmark bm,boolean isCurPos) {
		return getBookmarkView(c, position, convertView, bm, isCurPos, -1);
	}
	public static BookmarkView getBookmarkView(Context c, int position, View convertView, Bookmark bm,boolean isCurPos,int viewType) {
 		if(convertView==null)
 		{
 			if(viewType>-1)
 				convertView = new BookmarkView(c,viewType);
 			else
 				convertView = new BookmarkView(c);
 		}
		BookmarkView bv = (BookmarkView)convertView; 
		bv.setBookmark(bm,position,isCurPos);
		return bv;
	}
	public static Bitmap createWebViewThumbnail(WebView webView)
	{
		return createWebViewThumbnail(webView,TYPE_THUMBNAIL);
	}
	public static final int TYPE_THUMBNAIL = 1;
	public static final int TYPE_FULL = 2;
	public static final int TYPE_SCRINSHOT = 2;
	static void makeWebViewForScreenshot(MyWebView ww)
	{
		ww.pageDown(true);
	}
	
	public static final void createWebFullPage(final WebView ww,final OnBitmapLoadListener listener)
	{
		ww.pageDown(true);
		ww.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				ww.scrollBy(1000, 0);
				ww.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						int sy = ww.getScrollY();
						sy+=ww.getHeight();
						int ch = ww.getContentHeight();
						int height = Math.max(sy, ch);
						int width = ww.getWidth();
						width = ww.getScrollX()+ww.getWidth();
						Bitmap bm = createViewThumbnail(ww, width, height, 1f);
						Bookmark b = new Bookmark(ORIGINAL_URL, TITLE, 0);
						LoadBitmapInfo lbi = new LoadBitmapInfo(b, b);
						lbi.bitmap = bm;
						if(listener!=null)
							listener.onBitmapsLoad();
						
					}
				}, 300);
			}
		}, 1000);
	}
	public static Bitmap drawableToBitmap (Drawable drawable) {
	    if (drawable instanceof BitmapDrawable) {
	        return ((BitmapDrawable)drawable).getBitmap();
	    }

	    Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
	    Canvas canvas = new Canvas(bitmap); 
	    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
	    drawable.draw(canvas);

	    return bitmap;
	}
	public static final Bitmap createViewThumbnail(View view,int width,int height,float scale)
	{
		try{
        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        c.scale(scale, scale);
        view.draw(c);
        return bm;
		}
		catch(Throwable e)
		{
			Utils.log(e);
		}
		return null;
	}
	public static final Bitmap createWebViewThumbnail(View view,int type)
	{
		int dimen = (int)view.getResources().getDimension(R.dimen.panelButtonSquareSize);
		int width = st.dp2px(view.getContext(), dimen);
		int height = st.dp2px(view.getContext(), dimen);
		int w = view.getWidth();
		int h = view.getHeight();
		float min = Math.min(w, h);
		float scale = ((float)width)/min;
		switch (type) {
			case TYPE_THUMBNAIL:
				break;
			case TYPE_FULL:
				if(view instanceof WebView)
					height = ((WebView)view).getContentHeight();
				else
					height = view.getHeight();
				scale = 1f;
				break;
		default:
			break;
		}
		return createViewThumbnail(view, width, height, scale);
	}
	
	public static void saveWebWindowThumbnail(Tab ww,Bitmap bmp)
	{
        OutputStream stream = null;
        try {
            stream = new FileOutputStream(Environment.getExternalStorageDirectory() +"/page.jpg");
            bmp.compress(CompressFormat.JPEG, 80, stream);
            if (stream != null) stream.close();              
        } catch (IOException e) {
        } finally {
        } 
        
	}
	public static ArrayList<Action> createActionsArray(int ... actionIds)
	{
		ArrayList<Action>ar = new ArrayList<Action>();
		for(int act:actionIds)
			ar.add(Action.create(act));
		return ar;
	}
	public static Bundle bundleFromJSONObject(JSONObject obj)
	{
		try{
			Bundle b = new Bundle();	
			Iterator<String>keys = obj.keys();
			while (keys.hasNext()) {
		        String key = keys.next();
		        Object o = obj.get(key);
		        if(o instanceof Boolean)
		        	b.putBoolean(key, (Boolean)o);
		        else if(o instanceof String)
		        	b.putString(key, (String)o);
			}
			return b;
		}
		catch(Throwable e)
		{
			Utils.log(e);
		}
		return null;
	}
	public static JSONObject bundleToJSONObject(Bundle bundle)
	{
		try{
			JSONObject obj = new JSONObject();
			Set<String> keySet = bundle.keySet();
		    Iterator<String> it = keySet.iterator();
		    while (it.hasNext()){
		        String key = it.next();
		        Object o = bundle.get(key);
		        if (o instanceof Bundle)
		        {
		            obj.put(key, bundleToJSONObject((Bundle)o));
		        }
		        else
		        {
		        	obj.put(key, o);
		        }
		    }
		    return obj;
		}
		catch(Throwable e)
		{
			Utils.log(e);
		}
		return null;
	}
	public static byte[] bundleToBytes(Bundle bundle)
	{
//		JSONObject jo = bundleToJSONObject(bundle);
//		if(jo==null)
//			return null;
//		return jo.toString().getBytes();
		Parcel p = Parcel.obtain();
		p.setDataSize(0);
		bundle.writeToParcel(p, 0);
		byte ret[] =  p.marshall();
		p.recycle();
		return ret;
	}
	public static Bundle bytesToBundle(byte bytes[])
	{
		Parcel p = Parcel.obtain();
		p.setDataSize(0);
		p.unmarshall(bytes, 0, bytes.length);
		p.setDataPosition(0);
		Bundle b = new Bundle();
		b.readFromParcel(p);
		p.recycle();
		return b;
	}
	public static Uri addImageToGallery(Context context, String filepath, String title, String description) {    
	    ContentValues values = new ContentValues();
	    values.put(Media.TITLE, title);
	    values.put(Media.DISPLAY_NAME, description); 
	    values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis());
	    values.put(Images.Media.MIME_TYPE, "image/jpeg");
	    values.put(MediaStore.MediaColumns.DATA, filepath);

	    return context.getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
	}
	@SuppressWarnings("deprecation")
	public static long getFreeSpace(String path) {
		StatFs sf = new StatFs(path);
		return (long) sf.getAvailableBlocks() * (long) sf.getBlockSize();
	}
	public static void sendNewFileToMediaScanner(Context c,File f)
	{
		Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		c.sendBroadcast(mediaScanIntent);

	}
	public static void saveBitmap(final Activity a,final Bitmap bitmap,final String outFileNameWithExtension) {
		saveBitmap(a, bitmap, outFileNameWithExtension,null,".jpg");
	}
	public static void saveBitmap(final Activity a,final Bitmap bitmap,final String outFileName, final String url,final String extension) {
		final CustomProgress cp = new CustomProgress(a);
		cp.show();
		st.UniObserver obs = new st.UniObserver() {
			@Override
			public int OnObserver(Object param1, Object param2) {
				cp.dismiss();
				int msg = R.string.save_error;
				if (param1 != null
						&& param1 instanceof IllegalBlockSizeException)
					msg = R.string.save_photo_nospace;
				if (param1 != null && param1 instanceof IllegalStateException)
					msg = R.string.save_photo_nosdcard;
				else if (param1 instanceof Throwable)
				{
					st.toast(a, param1.toString());
					return 0;
				}
				else if (param1 != null)
					msg = R.string.save_photo_success;
				st.toast(a, a.getString(msg));
				return 0;
			}
		};
		st.SyncAsycOper op = new st.SyncAsycOper(obs) {

			@TargetApi(8)
			@Override
			public void makeOper(UniObserver obs) {
				try {

					File out;
					if (Build.VERSION.SDK_INT >= 8)
						out = Environment
								.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
					else
						out = Environment.getExternalStorageDirectory();
					out.mkdirs();
					if (!out.exists()) {
						obs.m_param1 = new IllegalStateException();
						return;
					}
					long fs = getFreeSpace(out.getAbsolutePath());
					if (fs < 1024 * 1024) {
						obs.m_param1 = new IllegalBlockSizeException();
						return;
					}
					String savepath = st.STR_NULL;
					try {
						String ext = extension;
						if(url!=null)
						{
							int pos = url.lastIndexOf('.');
							ext = url.substring(pos);
						}
						String outName = outFileName;
						if (outName.length() > 20)
							outName = outName.substring(0, 19);
						outName = outName.replace(':', '_');
						outName = outName.replace('?', '_');
						outName = outName.replace('/', '_');
						outName = outName.replace('\\', '_');
						savepath = out.getAbsolutePath() + '/' + outName;
						if(ext==null)
							ext = ".jpg";
						savepath+=ext;
						int num = 1;
						while (new File(savepath).exists()) {
							savepath = out.getAbsolutePath() + '/' + outName
									+ "(" + num + ")"+ext;
							num++;
						}
					} catch (Throwable e) {
						Utils.log(e);
					}
					if (TextUtils.isEmpty(savepath))
						out = new File(out, URLEncoder.encode(url,
								ImageCache.UTF8_NAME));
					else
						out = new File(savepath);
					boolean compress = bitmap.compress(CompressFormat.JPEG, 100,
							new FileOutputStream(out));
					sendNewFileToMediaScanner(a, out);
					obs.m_param1 = savepath;
				} catch (Throwable e) {
					Utils.log(e);
				}
			}
		};
		op.execute();
	}
	public static void createBookmarkWithDialog(final MainActivity a,Bookmark parentDir,Bitmap previewImage)
	{
		String url = a.getMainPanel().getUrl();
		if(TextUtils.isEmpty(url))
			return;
		Uri uri = Uri.parse(url);
		new DialogBookmark(a,uri,a.getWebView().getTitle(),parentDir,previewImage) {
			
			@Override
			public void doSave(Uri uri, Bookmark parentDir, String name,long id) {
				if(id<0)
				{
					if(parentDir!=null)
						Db.getStringTable().save(Db.LAST_BOOKMARK_FOLDER_ID, st.STR_NULL+parentDir.getBookmarkFolderId());
					createBookmark(a.getWebView(), name, parentDir);
				}
			}
		}.show();
	}
	public static void createBookmark(WebView mWebView,String name,Bookmark parentDir)
	{
		createBookmark(mWebView, name,parentDir.getBookmarkFolderId());
	}
	public static void createBookmark(WebView mWebView,String name,long parentDir)
	{
		try{
			MainActivity c = (MainActivity) mWebView.getContext();
			if(TextUtils.isEmpty(name))
				name = mWebView.getTitle();
			Bookmark bm = new Bookmark(mWebView.getUrl(), name, System.currentTimeMillis());
			Utils.log(BOOKMARK, "Add:"+bm.getJSON().toString()+st.STR_SPACE+parentDir);
			Bitmap preview = null;
// запись закладки. Создание лого сайта
			if(c.getTab().mThumbnail!=null)
				preview = c.getTab().mThumbnail;
			else
				preview = createWebViewThumbnail(mWebView);
// создание миниатюры закладки			
// если превью истории отключено
// закомментировано мной			
//			if (!Prefs.isHistoryMiniature()){
//				Resources res = c.getResources();
//				BitmapDrawable bd = (BitmapDrawable) res.getDrawable(R.drawable.close);
//				preview = null;
//				preview = bd.getBitmap();
//			}
			stat.saveHistory(c, bm, mWebView.getFavicon(),false,preview,parentDir);
		}
		catch(Throwable e)
		{
			Utils.log(e);
		}
	}
	public static void createBookmark(MainActivity con, String url,String name,long parentDir, Bitmap preview)
	{
		try{
//			MainActivity c = (MainActivity) mWebView.getContext();
//			if(TextUtils.isEmpty(name))
//				name = mWebView.getTitle();
			WebView mww = (WebView)con.getWebView();
			Bookmark bm = new Bookmark(url, name, System.currentTimeMillis());
			Utils.log(BOOKMARK, "Add:"+bm.getJSON().toString()+st.STR_SPACE+parentDir);
//			Bitmap preview = null;
//// запись закладки. Создание лого сайта
//			if(c.getTab().mThumbnail!=null)
//				preview = c.getTab().mThumbnail;
//			else
//				preview = createWebViewThumbnail(mWebView);

// создание миниатюры закладки			
// если превью истории отключено
// закомментировано мной			
//			if (!Prefs.isHistoryMiniature()){
//				Resources res = c.getResources();
//				BitmapDrawable bd = (BitmapDrawable) res.getDrawable(R.drawable.close);
//				preview = null;
//				preview = bd.getBitmap();
//			}
			
			stat.saveHistory(con, bm, mww.getFavicon(),false,preview,parentDir);
		}
		catch(Throwable e)
		{
			Utils.log(e);
		}
	}
	public static class FileUploadInfo
	{
		public FileUploadInfo(ValueCallback<Uri> uploadMsg,String acceptType,String capture)
		{
			this.uploadMsg = uploadMsg;
			this.acceptType = acceptType;
			this.capture = capture;
		}
		public ValueCallback<Uri> uploadMsg;
		public String acceptType;
		public String capture;
	}
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public static class DownloadOptions
	{
		public String destDir;
		public String destFileName;
		public int downloadNetworks = DownloadManager.Request.NETWORK_WIFI|DownloadManager.Request.NETWORK_MOBILE;
		public boolean showDownloader = false;
		public File destFile;
		Uri downloadUri;
		public boolean showNetworkSelector = false;
		public DownloadOptions(){}
		public DownloadOptions(String name)
		{
			destFileName = name;
		}
		public DownloadOptions(String name,String dir)
		{
			destDir = dir;
			destFileName = name;
		}
		public DownloadOptions(Uri uri,String name)
		{
			showNetworkSelector = true;
			downloadUri = uri;
			destFileName = name;
		}
	}
	public static void setClipboardString(Context c,String text)
	{
		ClipboardManager cm = (ClipboardManager)c.getSystemService(Service.CLIPBOARD_SERVICE);
		cm.setText(text);
	}
	public static CharSequence getClipboardCharSequence(Context c)
	{
     	ClipboardManager cm = (ClipboardManager)c.getSystemService(c.CLIPBOARD_SERVICE);
        ClipData clip = cm.getPrimaryClip();
        if (clip==null)
        	return null;
        if (clip.getItemCount()<1)
        	return null;
     	CharSequence str = clip.getItemAt(0).getText();
     	return str;
// старый код - ClipboardManager из android.text
//		ClipboardManager cm = (ClipboardManager)c.getSystemService(Service.CLIPBOARD_SERVICE);
//		return cm.getText();
	}
	static void editBookmark(Context c, Uri uri,long id,String name,Bookmark parentName)
	{
		ContentValues cv = new ContentValues();
		cv.put(TITLE, name);
		cv.put(PARENT, parentName.getBookmarkFolderId());
		if(uri!=null)
		cv.put(URL, uri.toString());
		try{
			if(BrowserApp.DB_TYPE==BrowserApp.DB_OWN)
				Db.getBookmarksTable().update(id, cv);
			else
			{
				int upd = 0;
				Cursor cursor = c.getContentResolver().query(BrowserContract.Bookmarks.CONTENT_URI, null, "_id=?", new String[]{Long.valueOf(id).toString()},null);
				if(cursor.moveToFirst())
				{
					int sz = cursor.getCount();
					cv.put(THUMBNAIL, cursor.getBlob(cursor.getColumnIndex(THUMBNAIL)));
					cv.put(FAVICON, cursor.getBlob(cursor.getColumnIndex(FAVICON)));
					//cv.put(DATE, cursor.getLong(cursor.getColumnIndex(DATE)));
				}
				upd = c.getContentResolver().update(BrowserContract.Bookmarks.CONTENT_URI, cv, "_id=?", new String[]{Long.valueOf(id).toString()});
				cursor.close();
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}
	public static boolean editBookmarkWithDialog(Context c,Bookmark bm,Bookmark parentDir,final st.UniObserver observer,Bitmap previewImage)
	{
		if(bm==null)
			return false;
		new DialogBookmark(c,bm.getUrl()==null?null:Uri.parse(bm.getUrl()),bm.getTitle(),parentDir,(Long)bm.param,previewImage) {
			
			@Override
			public void doSave(Uri uri, Bookmark parentDir, String name, long id) {
				editBookmark(context(), uri,id, name, parentDir);
				observer.Observ();
			}
		}.show();
		return true;
	}
	/** добавил я - удаляем все закладки */
	public static void deleteAllBookmark(Context con) {
		ContentResolver cr = con.getContentResolver();
		Cursor c = BookmarkFolderAdapter.getBookmarkCursorWithFolder(cr, 0); 
//				getCursorByType(inst, TYPE_BOOKMARKS);
		int cnt = 0;
		c.moveToFirst();
		while (c.moveToNext()){
			CustomPopup.toast(con, st.STR_NULL+cnt);
			cnt++;
		}
//		Bookmark bm = Bookmark.fromManagedCursor(c);
//		
//		BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_BOOKMARKS_CHANGED, bm);
		
	}
	public static boolean deleteBookmark(Context c,Bookmark bm,int type)
	{
		try{
	//		c = context.getContentResolver().query(Browser.BOOKMARKS_URI, null, type==TYPE_BOOKMARKS?"bookmark = 1":"bookmark = 0", null, DbUtils.getOrder(DATE, false));
			if(bm.param instanceof Long)
			{
				if(BrowserApp.DB_TYPE==BrowserApp.DB_OWN)
				{
					return Db.getBookmarksTable().delete((Long)bm.param)>-1;
				}
				Uri uri = type==BookmarkActivity.TYPE_BOOKMARKS?BrowserContract.Bookmarks.CONTENT_URI:BrowserContract.History.CONTENT_URI;
				String where = "_id = "+(Long)bm.param;
				int del = c.getContentResolver().delete(uri, "_id=?", new String[]{bm.param.toString()});
				return del>0;
			}
			if(BrowserApp.DB_TYPE==BrowserApp.DB_OWN)
			{
				return Db.getBookmarksTable().deleteHistoryByUrl(bm.getUrl())>-1;
			}
			String where = type==BookmarkActivity.TYPE_BOOKMARKS?"bookmark = 1 ":"bookmark = 0 ";
			where+=AND+' '+URL+" = \""+bm.getUrl()+"\"";
			int del = c.getContentResolver().delete(Browser.BOOKMARKS_URI, where,null);
			return del>0;
		}
		catch(Throwable e)
		{
			Utils.log(e);
		}
		return false;
	}
	@SuppressLint("NewApi")
	public static boolean downloadFile(final Context c,DownloadOptions opt)
	{
//		File f = opt.destFile;
//		opt.destFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), f.getName());
		opt.destFile = opt.destFile.getAbsoluteFile();
        File fstor = Environment.getExternalStorageDirectory();
        File curStor = FileUtils.getFileStorageDir(opt.destFile);
        if(curStor!=null&&!curStor.getAbsolutePath().equals(fstor.getAbsolutePath()));
        	setStorageDirectory(curStor);
        curStor = Environment.getExternalStorageDirectory();	
		if (Build.VERSION.SDK_INT >= 9) {
            List<String>path = opt.downloadUri.getPathSegments();
            if(path==null||path.size()<1)
            	return false;
            String fn = path.get(path.size()-1);
            CustomPopup.toast(c, c.getString(R.string.file_load)+' '+fn);
			DownloadManager.Request request = new DownloadManager.Request(opt.downloadUri);
	        String cookies = CookieManager.getInstance().getCookie(opt.downloadUri.toString());
	        request.addRequestHeader("cookie", cookies);

            // appears the same in Notification bar while downloading
            request.setDescription(opt.downloadUri.toString());
            request.setDestinationUri(Uri.fromFile(opt.destFile));
            request.setTitle(opt.destFileName);
            request.setAllowedNetworkTypes(opt.downloadNetworks);
            if(Build.VERSION.SDK_INT>=11)
            {
	            request.allowScanningByMediaScanner();
	            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            }
            DownloadManager manager = (DownloadManager) c.getSystemService(Context.DOWNLOAD_SERVICE);
//            File fstor = Environment.getExternalStorageDirectory();
//            File curStor = FileUtils.getFileStorageDir(opt.destFile);
//            if(curStor!=null&&!curStor.getAbsolutePath().equals(fstor.getAbsolutePath()));
//            	setStorageDirectory(curStor);
//            curStor = Environment.getExternalStorageDirectory();	
            try{
            	manager.enqueue(request);
            }
            catch(Throwable e)
            {
// сохранение файла
            	new ThemedDialog(c).setConfirm(c.getString(R.string.downloadFolderError), opt, new ConfirmOper()
            	{
					
					@Override
					public void onConfirm(Object userParam) {
						DownloadOptions opt = (DownloadOptions)userParam;
						File f = opt.destFile;
						opt.destFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), f.getName());
						downloadFile(c, opt);
					}
				});
            	return true;
            }
            if(opt.showDownloader)
            	c.startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

//            	File cur = setStorageDirectory(new File("/storage/sdcard1"));
//            	if(cur!=null)
//            		setStorageDirectory(cur);
        }
		return true;
	}
	public static File setStorageDirectory(File file)
	{
		try{
			Field f = Utils.refGetField(Environment.class, "EXTERNAL_STORAGE_DIRECTORY");
			f.setAccessible(true);
			File cur = (File) f.get(null);
			f.set(null, file);
			return cur;
//			Field f = Utils.refGetField(Environment.class, "sCurrentUser");
//			Object o = f.get(null);
//			Field stor = Utils.refGetField(o.getClass(), "mExternalStorage");
//			stor.setAccessible(true);
//			File exist = (File) stor.get(o);
			//Object o = f.get(Environment.class);
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getDeleteConfirm(Context c,String name)
	{
		return new StrBuilder(c).add(R.string.do_you_want_to_delete).addQuoted(name).add('?').toString();
	}
	// создаём папку в закладках
	public static Bookmark createBookmarkFolder(Context c,String name,long parentDir)
	{
		if(BrowserApp.DB_TYPE==BrowserApp.DB_BROWSER)
		{
			return null;
		}
		Bookmark bm = Bookmark.fromBookmarkFolder(name,1);
		try{
			
			if(BrowserApp.DB_TYPE==BrowserApp.DB_OWN)
			{
				bm.param = Db.getBookmarksTable().insertBookmark(TableBookmarks.TYPE_FOLDER, bm, null, null, parentDir);
				return bm;
			}
			ContentValues cv = bm.getFolderContentValues(parentDir);
			Uri row = c.getContentResolver().insert(BrowserContract.Bookmarks.CONTENT_URI, cv);
			List<String> path = row.getPathSegments();
			if(path.size()>0&&TextUtils.isDigitsOnly(path.get(path.size()-1)))
				bm.param = Long.decode(path.get(path.size()-1));
			return bm;
		}
		catch(Throwable e)
		{
			Utils.log(e);
		}
		return null;
	}
	public static int deleteBookmarkFolder(Context c,Bookmark dir)
	{
		try{
			long dirId = dir.getBookmarkFolderId();
			if(BrowserApp.DB_TYPE==BrowserApp.DB_OWN)
			{
				return Db.getBookmarksTable().deleteFolder(dirId);
			}
			String p = st.STR_NULL+dirId;
			int del = c.getContentResolver().delete(BrowserContract.Bookmarks.CONTENT_URI, "_id = ? OR parent = ?", new String[]{p,p});
			return del;
		}
		catch(Throwable e)
		{
			Utils.log(e);
		}
		return 0;
	}
	public static boolean checkUriProvider(ContentResolver cr,Uri uri,String ... existColumns)
	{
		String tag = "checkUriProvider";
		try{
			Utils.log(tag,"check:"+uri.toString());
			Cursor cursor = cr.query(uri, null, null, null, null);
			if(cursor==null)
			{
				Utils.log(tag,"null cursor");
				return false;
			}
			Utils.log(tag,"Cursor columns:"+Arrays.toString(cursor.getColumnNames()));
			boolean ret = true;
			if(existColumns!=null)
			{
				for(String col:existColumns)
				{
					if(cursor.getColumnIndex(col)<0)
					{
						ret = false;
						Utils.log(tag,"Column missed:"+col);
						break;
					}
				}
			}
			cursor.close();
			return ret;
		}
		catch(Throwable e)
		{
			Utils.log(tag, e);
		}
		return false;
	}
	public static void clearSearchHistory(Context c) 
	{		
		try{
			if(BrowserApp.DB_TYPE==BrowserApp.DB_OWN)
				Db.getSearchTable().clear();
			else
				c.getContentResolver().delete(Browser.SEARCHES_URI, null, null);
			BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_SEARCH_CHANGED, null);
		}
		catch(Throwable e)
		{
			Utils.log(e);
		}
	}
	public static boolean isVoiceSearchExist(Context c)
	{
		try{
			Intent in =  RecognizerIntent.getVoiceDetailsIntent(c);
			if(in!=null)
				return true;
		}
		catch(Throwable e)
		{
			
		}
		return false;
	}
	public static final String isBookmark(boolean isBookmark)
	{
		return Browser.BookmarkColumns.BOOKMARK+(isBookmark?"=1 ":"=0 ");
	}
	public static String decode(String url)
	{
		try{
			return URLDecoder.decode(url,UTF8);
		}
		catch(Throwable e)
		{ e.printStackTrace();}
		return url;
	}
	@SuppressWarnings("deprecation")
	public static int getSizeHeight(Context c)
	{
		WindowManager wm = (WindowManager) c.getSystemService(Service.WINDOW_SERVICE);
		int size = wm.getDefaultDisplay().getHeight();
		if (size<= 240)
			size = 100;
		else if (size>240&&size<=320)
			size = 180;
		else if (size>320&&size<=480)
			size = 280;
		else if (size>480&&size<=640)
			size = 400;
		else if (size>640)
			size = 400;

		return size;
	}
    public static boolean isAppInstal(Context c, String packageName)
    {
    	try {
			PackageInfo pi = c.getPackageManager().getPackageInfo(packageName, 0);
	    	if (pi!= null)
	    		return true;
		} catch (NameNotFoundException e)
		{}   
    	return false;
   	}

}