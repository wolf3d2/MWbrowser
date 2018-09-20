package com.jbak.superbrowser;

import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.jbak.superbrowser.stat.FileUploadInfo;

public class MyChromeClient extends WebChromeClient {
	private WeakReference<MainActivity> mainActivity;
	/**
	 * @param mainActivity
	 */
	MyChromeClient(MainActivity mainActivity) {
		this.setMainActivity(mainActivity);
	}
	CustomViewCallback mCallback;

	public void onReceivedIcon(WebView view, Bitmap icon) 
	{
		super.onReceivedIcon(view, icon);
//		if(getMainActivity()!=null)
//		{
//			WebWindow ww = getMainActivity().getWebWindows().getWebWindow(view);
//			ww.setFavicon(icon);
//			getMainActivity().sendWebViewEvent(WebViewEvent.WWEVENT_FAVICON_LOADED, view, icon, null);
//		}
	}

	public boolean onConsoleMessage(android.webkit.ConsoleMessage consoleMessage) 
	{
		if(this.getMainActivity().mJsProcessor!=null)
			this.getMainActivity().mJsProcessor.onConsoleMessage(consoleMessage);
		return super.onConsoleMessage(consoleMessage);
	}

//	public void onReceivedTitle(WebView view, String title) 
//	{
//		getMainActivity().sendWebViewEvent(WebViewEvent.WWEVENT_TITLE_LOADED, view, title, null);
//	}

	public boolean onJsAlert(WebView view, String url, String message, android.webkit.JsResult result) 
	{
		return super.onJsAlert(view, url, message, result);
	}

	public boolean onJsConfirm(WebView view, String url, String message, android.webkit.JsResult result) 
	{
		return super.onJsConfirm(view, url, message, result);
	}

	public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, android.webkit.JsPromptResult result) 
	{
		return super.onJsPrompt(view, url, message, defaultValue, result);
	}

	public void onProgressChanged(WebView view, int newProgress) 
	{
		Log.d("MyChromeProgress", "progress="+newProgress);
		MainActivity ma = getMainActivity();
		if(ma==null)
			return;
		Tab ww = ma.getTabList().getTabByWebView(view);
		if(ww!=null)
		{
			ww.setLoadProgress(newProgress);
			if(newProgress==100)
			{
				ww.loadedResource = null;
				ma.sendWebViewEvent(WebViewEvent.WWEVENT_LOAD_COMPLETE, view, null, null);
			}
			ma.setProgress(ww);
		}
	}

	public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, android.os.Message resultMsg) 
	{
		if(getMainActivity()!=null)
			this.getMainActivity().tabStart(null,MainActivity.ABOUT_NULL);
		WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
        transport.setWebView(this.getMainActivity().getWebView());
        resultMsg.sendToTarget();
		return true;
	}

    public void openFileChooser(ValueCallback<Uri> uploadMsg)
    {
		if(getMainActivity()!=null)
			this.getMainActivity().uploadFile(new FileUploadInfo(uploadMsg, null, null));
    }

    @SuppressWarnings("unused")
	public void openFileChooser(ValueCallback<Uri> uploadMsg,String acceptType)
    {
		if(getMainActivity()!=null)
			this.getMainActivity().uploadFile(new FileUploadInfo(uploadMsg, acceptType, null));
    }

    @SuppressWarnings("unused")
	public void openFileChooser(ValueCallback<Uri> uploadMsg,String acceptType,String capture)
    {
		if(getMainActivity()!=null)
			this.getMainActivity().uploadFile(new FileUploadInfo(uploadMsg, acceptType, capture));
    }

	public void onGeolocationPermissionsShowPrompt(String origin, android.webkit.GeolocationPermissions.Callback callback) 
	{
		callback.invoke(origin, true, false);
	}

	@Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
    	mCallback = callback;
		if(getMainActivity()!=null)
			getMainActivity().showCustomView(view);
    }
	@Override
	public View getVideoLoadingProgressView() {
		ProgressBar pb = new ProgressBar(getMainActivity());
		return pb;
	} 
	@Override
    public void onHideCustomView() {
		if(getMainActivity()!=null)
			getMainActivity().showCustomView(null);
        if(mCallback!=null)
        {
        	mCallback.onCustomViewHidden();
        	mCallback = null;
        }
    }

	private final MainActivity getMainActivity() {
		if(mainActivity!=null)
			return mainActivity.get();
		return null;
	}

	private final void setMainActivity(MainActivity mainActivity) {
		this.mainActivity = new WeakReference<MainActivity>(mainActivity);
	}
	public final boolean isFullscreenVideo()
	{
		return mCallback!=null;
	}
}