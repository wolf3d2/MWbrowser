package com.jbak.superbrowser;

import java.lang.ref.WeakReference;

import ru.mail.mailnews.st;
import ru.mail.webimage.WebDownload;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.text.TextUtils;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.jbak.superbrowser.ads.AdsBlock;
import com.jbak.superbrowser.search.GoogleSearchSystem;
import com.jbak.superbrowser.search.SearchSystem;
import com.jbak.superbrowser.ui.dialogs.DialogLoginPassword;
import com.jbak.superbrowser.utils.TempCookieStorage;

public class MyWebViewClient extends WebViewClient {
		/**
		 * 
		 */
		private WeakReference<MainActivity> mMainActivity;
		public static final String TAG = "MyWebView";
		/**
		 * @param mainActivity
		 */
		MyWebViewClient(MainActivity mainActivity) {
			mMainActivity = new WeakReference<MainActivity>(mainActivity);
		}

		public void onScaleChanged(WebView view, float oldScale, float newScale) 
		{
			super.onScaleChanged(view, oldScale, newScale);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if(MainActivity.ABOUT_NULL.equals(url))
				return true;
			if(UrlProcess.overrideUrlLoading(getMainActivity(), url))
				return true;
//			if(NetworkChecker.inetAvaliable)
//				view.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
//			else
//				view.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
			return super.shouldOverrideUrlLoading(view, url);
		}

		public void onReceivedHttpAuthRequest(WebView view, android.webkit.HttpAuthHandler handler, String host, String realm) 
		{
			//if(!handler.useHttpAuthUsernamePassword())
				new DialogLoginPassword(getMainActivity(), handler, realm).show();
			//return;
			//super.onReceivedHttpAuthRequest(view, handler, host, realm);
		}

		public void onPageStarted(WebView webview, String url, Bitmap favicon) 
		{
			//Utils.log(TAG,"started "+url);
			if(!url.equals(getMainActivity().getMainPanel().getUrl())&&URLUtil.isFileUrl(url)&&UrlProcess.checkFileOpen(getMainActivity(), url))
			{
				getMainActivity().sendWebViewEvent(WebViewEvent.WWEVENT_PAGE_FINISH, webview, url, favicon);
				return;
			}
			getMainActivity().sendWebViewEvent(WebViewEvent.WWEVENT_PAGE_START, webview, url, favicon);
			//mMainActivity.mPanel.onWebViewEvent(MainPanel.WWEVENT_TITLE_LOADED, view.getTitle());
			Tab ww = getWebWindow(webview);
			if(ww!=null)
			{
				if(ww.isError()&&(url.equals(ww.failingUrl)||MainActivity.ABOUT_BLANK.equals(url))&&!ww.getWebView().isReload())
				{
					//Log.d(TAG, "Failed page loading");
					if(!MainActivity.ABOUT_BLANK.equals(url))
						webview.stopLoading();
					return;
					
				}
				//ww.getWebView().adblock_urls.clear();;
				ww.setError(Tab.ERROR_SUCCESS, null, null);
				ww.setUrl(url);
				ww.loadedResource = url;
				ww.getWebView().loadUrls = null;
				getMainActivity().setProgress(ww);
			}
			super.onPageStarted(webview, url, favicon);

		}

		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) 
		{
			if(!TextUtils.isEmpty(getMainActivity().mGoFromUser)
					&&(errorCode==WebViewClient.ERROR_HOST_LOOKUP||errorCode==WebViewClient.ERROR_UNSUPPORTED_SCHEME))
			{
				String url = new GoogleSearchSystem().getSearchLink(SearchSystem.CMD_SEARCH, WebDownload.enc(getMainActivity().mGoFromUser), null);
				getMainActivity().mGoFromUser = null;
				getMainActivity().openUrl(url, IConst.WINDOW_OPEN_SAME);
				return;
			}
			BrowserApp.INSTANCE.NetworkChecker();

			Tab ww = getWebWindow(view);
			if(ww.getCurBookmark()!=null&&failingUrl.equals(ww.getCurBookmark().getUrl()))
			{
				ww.getWebView().getEventInfo().isReload = false;
				ww.setLoadProgress(-1);
				ww.setError(errorCode, description, failingUrl);
				getMainActivity().mGoFromUser = null;
				getMainActivity().setProgress(ww);
				//view.loadUrl(MainActivity.ABOUT_BLANK);
			}
			//view.loadUrl("about:blank");
			//GlobalHandler.command.sendDelayed(MainActivity.WHAT_RELOAD_FROM_CACHE, getMainActivity(), MainActivity.WHAT_RELOAD_FROM_CACHE);
		}
		@Override
		public void onPageFinished(WebView view, String url) 
		{
			getMainActivity().mGoFromUser = null;
			TempCookieStorage.onPageFinish(view,url);
			super.onPageFinished(view, url);
			getMainActivity().sendWebViewEvent(WebViewEvent.WWEVENT_PAGE_FINISH, view, url, null);
			//Utils.log(TAG,"finished "+url);
		}
		final Tab getWebWindow(WebView view)
		{
			MainActivity ma = getMainActivity();
			if(ma==null)
				return null;
			return ma.getTabList().getTabByWebView(view);
			
		}
//		WebResourceResponse mNullResponce;
		@SuppressLint("NewApi")
		final WebResourceResponse setResource(WebView view,String url)
		{
			Tab ww = getWebWindow(view);
			if(ww!=null)
			{
				//Log.d(TAG, url);
				ww.loadedResource = url;
				getMainActivity().setProgress(ww);
			}
//			if(url.contains("avatars-fast.yandex.net")||url.contains("an.yandex.ru")||url.contains("mc.yandex.ru"))
//			{
//				if(mNullResponce==null)
//					mNullResponce = new WebResourceResponse(url, "text/plain", null);
//				return mNullResponce;
//			}
			return null;
		}
		@SuppressLint("NewApi")
		public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
			setResource(view, request.getUrl().toString());
			return super.shouldInterceptRequest(view, request);
		}
		@SuppressLint("NewApi")
		@SuppressWarnings("deprecation")
		public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
			setResource(view, url);
			return super.shouldInterceptRequest(view, url);
		}
/** подгрузка всех урлов со страницы, при её загрузке */		
		@Override
		public void onLoadResource(WebView webview, String url) {
			
			MainActivity ma = getMainActivity();
			if(ma==null)
				return;
			Tab ww = ma.getTabList().getTabByWebView(webview);
			if(ww!=null)
			{
				ma.setProgress(ww);
//				if (url.contains(".braun")) {
//					st.toast(url);
//					return;
//				}
				ww.getWebView().loadUrls += url+"\n\n";
				if (AdsBlock.isBlockUrl(ww.getWebView(), url)) {
					// удалить элемент по его id
//					view.loadUrl("javascript:(function() { " +  
//		                    "(elem = document.getElementById('"+url+"')).parentNode.removeChild(elem); " +  
//		                    "})()");
					url = "";
					//return;
				}
			}
			if (url.contains("rs.mail.ru/pixel")) {
				url = "";
			}
			super.onLoadResource(webview, url);
		}

		public MainActivity getMainActivity() {
			if(mMainActivity!=null)
				return mMainActivity.get();
			return null;
		}
		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler,SslError error) {
			handler.proceed();
			//super.onReceivedSslError(view, handler, error);
		}
	}