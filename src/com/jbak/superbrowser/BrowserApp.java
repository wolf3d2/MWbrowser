package com.jbak.superbrowser;

import com.mw.superbrowser.R;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;

import ru.mail.mailnews.st;
import ru.mail.mailnews.st.UniObserver;
import ru.mail.webimage.DefaultImageLoaders;
import ru.mail.webimage.FileUtils;
import ru.mail.webimage.DefaultImageLoaders.FileInfo;
import ru.mail.webimage.WidgetImageLoader;
import ru.mail.webimage.WidgetImageLoader.ImagesSettings;
import ru.mail.webimage.widgets.BgImgContainer;
import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.jbak.reverseEngine.BrowserContract;
import com.jbak.superbrowser.search.SearchSystem;
import com.jbak.superbrowser.ui.dialogs.DialogAbout;
import com.jbak.superbrowser.ui.dialogs.ThemedDialog;
import com.jbak.superbrowser.ui.themes.MyTheme;
import com.jbak.superbrowser.utils.TempCookieStorage;
import com.jbak.ui.ConfirmOper;
import com.jbak.utils.DateToString;
import com.jbak.utils.Utils;
import com.jbak.utils.WeakRefArray;

public class BrowserApp extends Application {
	public static final int DB_BROWSER_CONTRACT = 1;
	public static final int DB_BROWSER = 2;
	public static final int DB_OWN = 3;
	
	public static BrowserApp INSTANCE;
	/** Событие изменения закладок */
	public static final int GLOBAL_BOOKMARKS_CHANGED = 1;
	/** Событие - запуск Action, в param передается Action, который нужно запустить */
	public static final int GLOBAL_ACTION = 2;
	public static final int GLOBAL_WINDOWS_CHANGED = 5;
	public static final int GLOBAL_NETWORK_CHANGED = 6;
	public static final int GLOBAL_SEARCH_CHANGED= 7;
	public static final int GLOBAL_SETTINGS_CHANGED = 8;
	public static final int GLOBAL_BUY_PRO_CHANGED = 9;
	
	
	public static final int DEVICE_TYPE_NORMAL = 0;
	public static final int DEVICE_TYPE_LARGE = 1;
	public static final int DEVICE_TYPE_XLARGE = 2;
	WeakRefArray<OnGlobalEventListener> arGlobals = new WeakRefArray<OnGlobalEventListener>();
	public static int DB_TYPE = DB_OWN;
	public static int deviceType;
	public static File cacheDir;
	public static PluginServer pluginServer;
	public static final String SAVE_CRASH = "save_crash.txt";
	private static boolean mExternalApp;
	static Handler uiHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg) 
		{
			if(msg.obj instanceof GlobalEventParams)
				sendGlobalInUiThread((GlobalEventParams)msg.obj);
		};
	};
	private Thread.UncaughtExceptionHandler androidDefaultUEH;
	@Override
	public void onCreate() {
		super.onCreate();
		mExternalApp = (getApplicationInfo().flags&ApplicationInfo.FLAG_EXTERNAL_STORAGE)!=0;
		//checkMoveDbToFiles();
		//FileUtils.copyOrMove(true, new File("/data/data/com.jbak.superbrowser"), new File(Environment.getExternalStorageDirectory(),"JbakBrowserData"));
		new MyCookieManager();
		//TempCookieStorage.onStartIncognito(true);

		androidDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			
			@Override
			public void uncaughtException(Thread thread, Throwable e) {
				saveCrash(e);
				androidDefaultUEH.uncaughtException(thread, e);
			}
		});
		deviceType = (int) getResources().getDimension(R.dimen.device_type);
		INSTANCE = this;
		NetworkChecker();
		pluginServer = new PluginServer();
		DateToString.create(INSTANCE, getString(R.string.today), getString(R.string.yesterday));
		Db.create(this);
		Prefs.init(this);
		new WidgetImageLoader().init(INSTANCE, "JbakBrowser", new ImagesSettings() {
			@Override
			public boolean isNetworkAvaliable() {
				return true;
			}
			@Override
			public boolean isImagesEnabled() {
				return true;
			}
		});
		WidgetImageLoader.INSTANCE.setSizes(ImageSizes.getSizes());
		WidgetImageLoader.INSTANCE.getImageDownloader().setImageLoaders(DefaultImageLoaders.getLoaders());
		DB_TYPE = checkBookmarks(getContentResolver());
		Utils.log(IConst.BOOKMARK, "Use db:"+DB_TYPE);
		SearchSystem.init();
		MyTheme.setTheme(this, Prefs.getTheme());
		if(Db.needImportHistory(INSTANCE))
			new st.SyncAsycOper() {
				
				@Override
				public void makeOper(UniObserver obs) throws Throwable {
					Db.importHistory(INSTANCE);
				}
			}.startAsync();
		Payment.init();

	}

	public static void runActivity(Class<?> clazz)
	{
		INSTANCE.startActivity(new Intent(INSTANCE, clazz).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

	}
	public static void runActivityForResult(Activity activity,Class<?> clazz,int code)
	{
		activity.startActivityForResult(new Intent(activity,clazz), code);

	}
	public static void runActivityForResult(Activity activity,int code,Intent intent)
	{
		activity.startActivityForResult(intent, code);

	}
	public void addGlobalListener(OnGlobalEventListener listener)
	{
		arGlobals.add(listener);
	}
	public void removeGlobalListener(OnGlobalEventListener listener)
	{
		arGlobals.remove(listener);
	}
	private static void sendGlobalInUiThread(GlobalEventParams gp)
	{
		for(OnGlobalEventListener o:INSTANCE.arGlobals.getLiveRefs())
		{
			o.onGlobalEvent(gp.code, gp.param);
		}
	}
	public static void sendGlobalEvent(int code,Object param)
	{
		GlobalEventParams gp = new GlobalEventParams();
		gp.code = code;
		gp.param = param;
		uiHandler.sendMessage(uiHandler.obtainMessage(1, gp));
	}
	public static void setTheme(MyTheme th)
	{
		MyTheme.setCurTheme(th);
		Prefs.setTheme(th.getThemeInfo().id);
	}
	public static void loadFileImage(BgImgContainer container,FileInfo fileInfo)
	{
		WidgetImageLoader.displayImageIfNeed(Uri.fromFile(fileInfo.file).toString(), container,true,ImageSizes.SIZE_FILEMANAGER_IMAGE, fileInfo);
	}
	public static boolean hasPlayServices()
	{
//		return GooglePlayServicesUtil.isGooglePlayServicesAvailable(INSTANCE)==ConnectionResult.SUCCESS;
		return false;
	}
	/** чекаем использовать общие закладки в браузере или свои */
	public static int checkBookmarks(ContentResolver cr)
	{
		int stor = Prefs.get().getInt(Prefs.BOOKMARK_STORAGE, Prefs.BOOKMARK_STORAGE_UNDEFINED);
		if(stor==Prefs.BOOKMARK_STORAGE_OWN)
			return DB_OWN;
		else if(stor==Prefs.BOOKMARK_STORAGE_SYSTEM)
			return DB_BROWSER_CONTRACT;
		Utils.log(IConst.BOOKMARK, "Bookmarks uri:"+BrowserContract.Bookmarks.CONTENT_URI);
		if(stat.checkUriProvider(cr, BrowserContract.Bookmarks.CONTENT_URI,IConst.FOLDER,IConst.PARENT))
			return DB_BROWSER_CONTRACT;
//		else if(Stat.checkUriProvider(cr, Browser.BOOKMARKS_URI,IConst.FOLDER,IConst.PARENT))
//			return DB_BROWSER;
		return DB_OWN;
	}
	public static class GlobalEventParams 
	{
		int code;
		Object param;
	}
	public static interface OnGlobalEventListener
	{
		public void onGlobalEvent(int code,Object param);
	}
	@Override
	public File getCacheDir() {
		if(cacheDir!=null&&cacheDir.exists()&&cacheDir.isDirectory())
		{
			return cacheDir;
		}
//		if(mExternalApp)
//			return getExternalCacheDir();
		return super.getCacheDir();
	}
	public static boolean checkCrash(final MainActivity act)
	{
		File f = new File(INSTANCE.getFilesDir(), SAVE_CRASH);
		if(!f.exists())
			return false;
		new ThemedDialog(act).setConfirm(act.getString(R.string.do_you_want_send_crash_report), f, new ConfirmOper() {
			
			@Override
			public void onConfirm(Object userParam) {
				File f = (File)userParam;
				DialogAbout.sendFeedback(act, f);
				f.delete();
			}
			@Override
			public void onCancel(Object userParam) {
				super.onCancel(userParam);
				File f = (File)userParam;
				f.delete();
			}
		});
		return true;
	}
	public void saveCrash(Throwable e)
	{
		Log.e("JbakBrowserCrash", Utils.getStackString(e));
		e.printStackTrace();
		st.strToFile(Utils.getStackString(e), new File(getFilesDir(),SAVE_CRASH));
	}
	public void checkMoveDbToFiles()
	{
		boolean external =  (getApplicationInfo().flags&ApplicationInfo.FLAG_EXTERNAL_STORAGE)!=0;
		File filesDir = getFilesDir();
		File extFileDir = getExternalFilesDir(null);
		File dbFile = getDatabasePath(Db.DB_NAME);
		File targetDir=external?extFileDir:filesDir;
		File targetDbFile = new File(targetDir, Db.DB_NAME);
		if(targetDbFile.exists())
		{
			Db.DB_PATH = targetDbFile.getAbsolutePath();
			return;
		}
		if(dbFile!=null&&dbFile.exists())
		{
			File newDbFile = FileUtils.copyOrMoveFile(false, dbFile, targetDir);
			if(newDbFile!=null&&newDbFile.exists())
			{
				Db.DB_PATH = newDbFile.getAbsolutePath();
				return;
			}
		}
		else
		{
			File moveFile = null;
			File f = new File(external?filesDir:extFileDir, Db.DB_NAME);
			if(f.exists())
				moveFile = f;
			if(moveFile!=null)
			{
				File nf = FileUtils.copyOrMoveFile(false, moveFile, targetDir);
				if(nf!=null&&nf.exists())
					Db.DB_PATH = nf.getAbsolutePath();
				return;
			}
		}
		targetDir.mkdirs();
		if(targetDir.exists())
		{
			Db.DB_PATH = new File(targetDir, Db.DB_NAME).getAbsolutePath();
		}
	}
	public void NetworkChecker()
	{
		NetworkChecker.create(this);
	}
}
