package com.jbak.superbrowser.utils;

import java.util.HashMap;
import java.util.Map;

import android.net.Uri;
import android.webkit.CookieManager;
import android.webkit.WebView;
import ru.mail.mailnews.st;

import com.jbak.superbrowser.MainActivity;
import com.jbak.superbrowser.TabList;
import com.jbak.superbrowser.WebViewEvent;
import com.jbak.superbrowser.stat;
import com.jbak.utils.Utils;

public class TempCookieStorage implements WebViewEvent{
	HashMap<String, String> mIncognitoCookies;
	HashMap<String, String> mNormalCookies;
	public static TempCookieStorage INSTANCE = null;
	boolean mIncognito = false;
	public static void onStartIncognito(boolean start)
	{
		if(start)
		{
			INSTANCE = new TempCookieStorage();
		}
		else
		{
//			INSTANCE = null;
		}
			
	}
	private TempCookieStorage() {
		mIncognitoCookies = new HashMap<String, String>();
		mNormalCookies = new HashMap<String, String>();
	}
	private HashMap<String, String> getCurMap()
	{
		return mIncognito?mIncognitoCookies:mNormalCookies;
	}
	private HashMap<String, String> getAnotherMap()
	{
		return mIncognito?mNormalCookies:mIncognitoCookies;
	}
	private void setCookieForUrl(String url,String cookie)
	{
		String domain = getDomain(url);
		if(domain!=null)
			getCurMap().put(domain, cookie);
	}
	private String getCookieForUrl(String url)
	{
		String domain = getDomain(url);
		if(domain==null)
			return null;
		return getCurMap().get(domain);
	}
	public static String getDomain(String url)
	{
		try{
			Uri uri = Uri.parse(url);
			return uri.getHost();
		}
		catch(Throwable e)
		{
			
		}
		return null;
	}
	void checkCookie(String url,boolean start)
	{
		String domain = getDomain(url);
		if(domain==null)
			return;
		CookieManager cm = CookieManager.getInstance();
		String cookie = cm.getCookie(domain);
		String curCookie = INSTANCE.getCookieForUrl(url);
		if(!Utils.isStringsEquals(cookie, curCookie))
		{
			if(start)
			{
				getAnotherMap().put(domain, cookie);
				cm.setCookie(domain, curCookie==null?st.STR_NULL:curCookie);
			}
			else
			{
				INSTANCE.getCurMap().put(domain, curCookie);
			}
		}
	}
	public static void onStartRequest(WebView view, String url)
	{
		if(INSTANCE==null)
			return;
		INSTANCE.checkCookie(url, true);
	}
	public static void onPageFinish(WebView view, String url)
	{
		if(INSTANCE==null)
			return;
		INSTANCE.checkCookie(url, false);
	}
	public static boolean switchIncognito(boolean incognito,TabList tabList)
	{
		CookieManager cm = CookieManager.getInstance();
		HashMap<String, String> map = incognito?INSTANCE.mIncognitoCookies:INSTANCE.mNormalCookies;
		for(Map.Entry<String, String>e:map.entrySet())
		{
			cm.setCookie(e.getKey(), e.getValue());
		}
		return true;
	}
	public static void onMainActivityResume(MainActivity a)
	{
		if(INSTANCE==null||a==null||MainActivity.activeInstance==null||a==MainActivity.activeInstance)
			return; 
		TabList l1 = a.getTabList();
		TabList l2 = MainActivity.activeInstance.getTabList();
		if(l1.isIncognito()==l2.isIncognito())
			return;
		INSTANCE.mIncognito = l1.isIncognito();
		switchIncognito(INSTANCE.mIncognito, l1);
	}
	@Override
	public void onWebViewEvent(int code, EventInfo info) {
		
	}
}
