package com.jbak.superbrowser;

import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;

public class MyCookieManager extends CookieManager {
	CookieManager mInstance;
	static MyCookieManager INSTANCE;
	public MyCookieManager()
	{
		mInstance = CookieManager.getInstance();
//		try{
//			Method m = WebView.class.getDeclaredMethod("getFactory", (Class<?>[])null);
//			if(m!=null)
//			{
//				m.setAccessible(true);
//				Object provider = m.invoke(null, (Object[])null);
//				if(provider!=null)
//					Log.d("PROVIDER", provider.toString());
//				Field f =  provider.getClass().getDeclaredField("mCookieManager");
//				if(f!=null)
//				{
//					f.setAccessible(true);
//					CookieManagerAdapter adapt = new CookieManagerAdapter(this);
//					f.set(provider, adapt);
//				}
//			}
//		}
//		catch(Throwable e)
//		{
//			e.printStackTrace();
//		}
	}
	public static MyCookieManager get()
	{
		if(INSTANCE==null)
			INSTANCE = new MyCookieManager();
		return INSTANCE;
	}
	public CookieManager getInstanceCookieManager()
	{
		return mInstance;
	}

	@Override
	public boolean acceptCookie() {
		return mInstance.acceptCookie();
	}

	@Override
	public boolean acceptThirdPartyCookies(WebView webview) {
		return mInstance.acceptThirdPartyCookies(webview);
	}

	@Override
	public void flush() {
		mInstance.flush();
	}

	@Override
	public String getCookie(String url) {
		return mInstance.getCookie(url);
	}

	@Override
	public boolean hasCookies() {
		return hasCookies();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void removeAllCookie() {
		mInstance.removeAllCookie();
	}

	@Override
	public void removeAllCookies(ValueCallback<Boolean> arg0) {
		mInstance.removeAllCookies(arg0);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void removeExpiredCookie() {
		mInstance.removeExpiredCookie();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void removeSessionCookie() {
		mInstance.removeSessionCookie();
		
	}

	@Override
	public void removeSessionCookies(ValueCallback<Boolean> arg0) {
		mInstance.removeSessionCookies(arg0);
	}

	@Override
	public void setAcceptCookie(boolean accept) {
		mInstance.setAcceptCookie(accept);
	}

	@Override
	public void setAcceptThirdPartyCookies(WebView webview, boolean accept) {
		mInstance.setAcceptThirdPartyCookies(webview, accept);
	}

	@Override
	public void setCookie(String url, String value) {
		mInstance.setCookie(url, value);
	}

	@Override
	public void setCookie(String arg0, String arg1, ValueCallback<Boolean> arg2) {
		
	}

	
}
