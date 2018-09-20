package com.jbak.superbrowser;

import java.io.File;

import org.json.JSONObject;

import ru.mail.mailnews.st;
import ru.mail.mailnews.st.UniObserver;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.WebBackForwardList;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.jbak.superbrowser.WebViewEvent.EventInfo;
import com.jbak.superbrowser.ui.MyWebView;
import com.jbak.ui.CustomPopup;
import com.jbak.utils.GlobalHandler;
import com.jbak.utils.Utils;

public class Tab implements IConst,GlobalHandler{
	public static final int ERROR_SUCCESS = 0;
	public int windowId;
	public int viewType;
	public Bookmark currentBookmark;
	public static final int VIEW_TYPE_FULL = 1;
	public static final int VIEW_TYPE_SMARTPHONE = 0;
	public static final int MSG_SAVE = 300;
	public static final int MSG_SAVE_DELAYED = 5000;
	private MyWebView mWebView;
	static Context m_c = null;
	Bitmap mThumbnail;
	private Bitmap mFavicon;
	public Bundle savedState;
	public boolean previewEnabled = true;
	public boolean imagesEnabled = true;
	long closedTime=0;
	private int loadProgress = -1;
	public String loadedResource;
	public int errorCode = ERROR_SUCCESS;
	public String description;
	public String failingUrl;
	boolean mIncognito = false;
	private boolean tempSession = false;
	public boolean lastStateIsLoad=false;
	public Tab(Context c, int id,TabList tabList)
	{
		m_c = c;
		windowId = id;
		if(tabList!=null)
		{
			this.tempSession = tabList.isTempSession();
			this.mIncognito = tabList.isIncognito();
		}
	}
	public Bookmark getCurBookmark()
	{
		return currentBookmark;
	}
	public final boolean isError()
	{
		return errorCode!=ERROR_SUCCESS;
	}
	public final String getUrl()
	{
		if(currentBookmark==null)
			return null;
		return currentBookmark.getUrl();
	}
	public final boolean isLoading()
	{
		return getLoadProgress()>=0&&getLoadProgress()<100;
	}
	public void setJson(JSONObject obj)
	{
		try{
			windowId = obj.optInt(WINDOW_ID);
			viewType = obj.optInt(VIEW_TYPE,VIEW_TYPE_SMARTPHONE);
			imagesEnabled = obj.optBoolean(WS_IMAGES_ENABLED,imagesEnabled);
			previewEnabled = obj.optBoolean(WS_PREVIEW_ENABLED,previewEnabled);
		}
		catch(Throwable e)
		{
		}
	}
	public String toJson()
	{
		JSONObject obj = new JSONObject();
		try {
			obj.putOpt(WINDOW_ID, windowId);
			obj.putOpt(VIEW_TYPE, viewType);
			obj.putOpt(WS_PREVIEW_ENABLED, previewEnabled);
			obj.putOpt(WS_IMAGES_ENABLED, imagesEnabled);
		} catch (Throwable e) {
			Utils.log(e);
		}
		return obj.toString();
	}
	public void refreshSettings()
	{
		refreshSettings(mWebView);
	}
	@SuppressLint("NewApi")
	public void refreshSettings(WebView ww)
	{
		WebSettings ws = ww.getSettings();
		imagesEnabled = !ws.getBlockNetworkImage();
		previewEnabled = ws.getLoadWithOverviewMode();
		save();
	}
	public boolean restoreWebViewState()
	{
		if(savedState!=null)
		{
			try{
			getWebView().checkCacheMode();
			WebBackForwardList wb = getWebView().restoreState(savedState);
			if(wb!=null&&wb.getSize()>0)
			{
				getWebView().invalidate();
				return true;
			}
			}
			catch(Throwable ignor)
			{
			}
		}
		if(currentBookmark!=null)
		{
			String url = currentBookmark.originalUrl;
			if(!TextUtils.isEmpty(url))
				url = currentBookmark.getUrl();
			if(!TextUtils.isEmpty(url))
				getWebView().loadUrl(url);
		}
		return false;
	}
	@SuppressLint("NewApi")
	public boolean setWebView(MyWebView ww)
	{
		mWebView = ww;
		if(ww==null)
			return false;
		WebSettings ws = ww.getSettings();
		ws.setBlockNetworkImage(!imagesEnabled);
		ws.setLoadWithOverviewMode(previewEnabled);
		setViewType(viewType, false);
		return true;
	}
	public void save()
	{
		if(getWebView()==null||currentBookmark==null||isError()||mIncognito)
			return;
		GlobalHandler.command.sendDelayed(MSG_SAVE, this, MSG_SAVE);
	}
	public Bitmap createBitmap()
	{
		if(getWebView()!=null)
			mThumbnail = stat.createWebViewThumbnail(getWebView());
		return mThumbnail;
	}
	public void saveNow(boolean delayed)
	{
		if(getWebView()==null||currentBookmark==null||isError())
			return;
		if(getUrl()==null||!getUrl().equals(getWebView().getUrl()))
			return;
//		if(getFavicon()==null)
//			setFavicon(getWebView().getFavicon());
//		if(mThumbnail!=null&&!mThumbnail.isRecycled())
//			mThumbnail.recycle();
		mThumbnail = null;
		mThumbnail = stat.createWebViewThumbnail(getWebView());
		if(tempSession)
			return;
		if(savedState==null)
			savedState = new Bundle();
		savedState.clear();
		getWebView().saveState(savedState);
		new Saver().startAsync();
	}
	public String trimMobileFromUrl(String url)
	{
		Uri uri = Uri.parse(url);
		String host = uri.getAuthority();
		if (host==null){
			if (m_c!=null)
				st.toast(m_c,"Сперва загрузите страницу");
			return stat.STR_NULL;
		}
		String parts[] = host.split("\\.");
		String nhost = stat.STR_NULL;
		int pos = 0;
		for(String p:parts)
		{
			if(p.equals("m")&&pos<parts.length-1)
			{
			}
			else
			{
				nhost+=p;
				if(pos<parts.length-1)
					nhost+='.';
			}
			++pos;
		}
		if(!host.equals(nhost))
		{
			return uri.buildUpon().authority(nhost).build().toString();
		}
		return url;
	}
	private static String mExistUserAgent;
	public void setViewType(int viewType,boolean reload)
	{
		if(getWebView()==null)
			return;
		this.viewType = viewType;
		WebSettings ws  =  getWebView().getSettings();
		String ua = null;
		if(mExistUserAgent==null)
			mExistUserAgent = ws.getUserAgentString();
		if(viewType==Tab.VIEW_TYPE_FULL)
			ua = "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2224.3 Safari/537.36";
		else
			ua = mExistUserAgent+" Jbak Browser";
			//"Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0";
		ws.setUserAgentString(ua);
		if(reload)
		{
			String url = getWebView().getUrl();
			String origUrl = getWebView().getOriginalUrl();
			String loadUrl = null;
			if(origUrl!=null&&!(origUrl.equals(url)))
			{
				loadUrl = origUrl;
			}
			else if(!TextUtils.isEmpty(url)&&viewType==VIEW_TYPE_FULL){
				// Попробуем выбить букву m из домена
				String nurl = trimMobileFromUrl(url);
				if (nurl.length()==0){
					stat.fl_loadPCmode=false;
					return;
				} else
					stat.fl_loadPCmode=true;
//				if(!nurl.equals(url)&&Stat.fl_loadPCmode)
				if(!nurl.equals(url))
				{
					loadUrl = nurl;
				}
			}
			if(loadUrl!=null)
			{
				getWebView().stopLoading();
				ws.setUserAgentString(ua);
				//mWebView.loadUrl(loadUrl);
				CustomPopup.toast(getWebView().getContext(), loadUrl);
			}
			else
			{
				//mWebView.reload();
				CustomPopup.toast(getWebView().getContext(), getWebView().getUrl());
			}
			final String u = loadUrl;
			getWebView().post(new Runnable() {
				
				@Override
				public void run() {
					if(u!=null)
						getWebView().loadUrl(u);
					else
						getWebView().reload();
				}
			});
		}
	}
	public final void setUrl(String url)
	{
		if(TextUtils.isEmpty(url)||MainActivity.ABOUT_BLANK.equals(url)&&isError())
			return;
		if(currentBookmark==null||currentBookmark.getUrl()==null||!currentBookmark.getUrl().equals(url))
		{
			currentBookmark = new Bookmark(url, null, System.currentTimeMillis());
			setFavicon(null);
		}
	}
	public void onWebViewEvent(int code,EventInfo info)
	{
		switch (code) {
//			case WebViewEvent.WWEVENT_PAGE_START:
//				String u = getUrl();
//				if(u!=null&&!u.equals(info.startedUrl))
//					setFavicon(null);
//				break;
//			case WebViewEvent.WWEVENT_PAGE_FINISH:
//					if(currentBookmark!=null)
//					{
//						if(!MainActivity.ABOUT_BLANK.equals(info.getUrl()))
//							currentBookmark.setUrl(info.getUrl());
//						currentBookmark.date = System.currentTimeMillis();
//						save();
//					}
//				break;	
//			case WebViewEvent.WWEVENT_TITLE_LOADED:
//				if(currentBookmark!=null)
//					currentBookmark.setTitle(info.title);
//				break;
//			case WebViewEvent.WWEVENT_FAVICON_LOADED:
//			case WebViewEvent.WWEVENT_LOAD_COMPLETE:
//				save();
//				break;
		}
	}
	public int getViewType()
	{
		return viewType;
	}
	public void setCurrentBookmark(Bookmark bm)
	{
		currentBookmark = bm;
		if(bm!=null&&bm.param instanceof Bitmap)
			setFavicon((Bitmap)bm.param);
		else
			setFavicon(null);
	}
	@Override
	public void onHandlerEvent(int what) {
		saveNow(what==MSG_SAVE);
		if(what==MSG_SAVE)
			GlobalHandler.command.sendDelayed(MSG_SAVE_DELAYED, this, MSG_SAVE_DELAYED);
	}
	public class Saver extends st.SyncAsycOper
	{
		Context mContext;
		public Saver()
		{
			mContext = getWebView().getContext();
		}
		@Override
		public void makeOper(UniObserver obs) throws Throwable {
			if(mIncognito)
				return;
			if(!tempSession)
				Db.getWindowTable().saveWindow(Tab.this);
			stat.saveHistory(mContext, currentBookmark, getFavicon(), true,mThumbnail);
		}
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_WINDOWS_CHANGED, Tab.this);
		}
	}
	public void setClosed(boolean closed)
	{
		closedTime = closed?System.currentTimeMillis():0;
		Db.getWindowTable().setWindowClosed(windowId, closed);
	}
	public boolean isClosed()
	{
		return closedTime>0;
	}
	public void runTestJavascript()
	{
		runJavascript("alert(1);");
	}
	public void runJavascript(String js)
	{
		File f = new File(getWebView().getContext().getFilesDir(),"run.js");
		st.strToFile(js, f);
		runJavascript(f);
	}
	public void runJavascript(File jsFile)
	{
//		String html = "<script src='"+jsFile.getName()+"'></script>";
//		File save = new File(jsFile.getParent(), "enc.html");
//		St.strToFile(html, save);
//
//		html = "javascript:"+WebDownload.enc(html);
//		mWebView.loadUrl(html);
		//String html = "javascript: var%20s%20=%20document.createElement('script');s.type='text/javascript';document.body.appendChild(s);s.src='http://rajter-zoli.narod.ru/monomize/spaceship.js';void(0);";
		getWebView().loadUrl("javascript:alert(document.activeElement.text);");
//		Bookmark bm = new Bookmark("http://open.adaptedstudio.com/html5/many-lines/index.html", stat.STR_NULL, 0).setOpenNewWindow(true);
//		BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_ACTION, Action.create(Action.ACTION_BOOKMARK, bm));
	}
	public final boolean isEmpty()
	{
		return getCurBookmark()==null||TextUtils.isEmpty(getCurBookmark().getUrl());
	}
	public final Bitmap getFavicon()
	{
		return mFavicon;
	}
	public final MyWebView getWebView() {
		if(mWebView==null)
			return null;
		return mWebView;
	}
	public void setError(int errorCode,String description,String failedUrl)
	{
		this.errorCode = errorCode;
		this.description = description;
		this.failingUrl = failedUrl;
		if(errorCode==ERROR_SUCCESS)
			setLoadProgress(5);
		else
			setLoadProgress(-1);
	}
	void setFavicon(Bitmap favicon) {
		this.mFavicon = favicon;
	}
	public final int getLoadProgress() {
		return loadProgress;
	}
	public final void setLoadProgress(int loadProgress) {
		if(loadProgress>=0&&loadProgress<5)
			this.loadProgress = 5;
		this.loadProgress = loadProgress;
	}
	public boolean checkWebView()
	{
		boolean changed = false;
		if(mWebView==null)
			return changed;
		try{
			boolean wload = isLoading();
			String url = mWebView.getUrl();
			String title = mWebView.getTitle();
			Bitmap favicon = mWebView.getFavicon();
			if(currentBookmark==null
				||url!=null&&!Utils.isStringsEquals(url, currentBookmark.getUrl())
				||title!=null&&!Utils.isStringsEquals(title, currentBookmark.getTitle())
				||favicon!=getFavicon()
				||wload!=lastStateIsLoad
				)
			{
				if(currentBookmark==null&&url==null)
					changed = false;
				else
				{
					changed = true;
					if(currentBookmark==null)
						currentBookmark = new Bookmark(url, title, System.currentTimeMillis());
					else if(url!=null)
						currentBookmark.set(url, title, System.currentTimeMillis());
					setFavicon(favicon);
					if(mWebView.getContentHeight()>0)
						save();
				}
			}
			lastStateIsLoad = wload;
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		return changed;
	}
}
