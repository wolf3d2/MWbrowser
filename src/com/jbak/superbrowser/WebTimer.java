package com.jbak.superbrowser;

import java.lang.ref.WeakReference;

import android.view.ViewGroup;
import android.webkit.WebView;

import com.jbak.utils.GlobalHandler;

public class WebTimer extends MainActivityRef implements GlobalHandler{
	public static final int WHAT_INTERVAL = 300;
	WeakReference<MainActivity>mMain;
	WeakReference<Tab>mCurTab;
	boolean mPaused = true;
	public WebTimer(MainActivity main)
	{
		super(main);
	}
	public void onActivityResume()
	{
		mPaused = false;
		GlobalHandler.command.sendDelayed(WHAT_INTERVAL, this, WHAT_INTERVAL);
	}
	public void onActivityPause()
	{
		mPaused = true;
	}
	@Override
	public void onHandlerEvent(int what) {
		MainActivity ma = getMain();
		if(ma==null)
			return;
		TabList windows = ma.getTabList();
		if(windows==null)
			return;
		boolean load = false;
		try{
			boolean windowsChanged = false;
			boolean curChanged = false;
			for(Tab webWindow:windows.getOpenedTabs())
			{
				boolean cur = webWindow==windows.getCurrent();
				if(webWindow.isLoading())
					load = true;
				else
				{
					if(!cur) // Фоновую вкладку убираем из временного контейнера по окончании загрузки
					{
						WebView v = webWindow.getWebView();
						if(v.getParent() instanceof ViewGroup)
							((ViewGroup)v.getParent()).removeView(v);
					}
				}
				if(webWindow.checkWebView())
				{
					if(cur)
						curChanged = true;
					load = true;
					windowsChanged = true;
				}
				if(cur&&(mCurTab==null||webWindow!=mCurTab.get()))
				{
					mCurTab = new WeakReference<Tab>(webWindow);
					curChanged = true;
				}
			}
			ma.updateCurTab();
//			if(windowsChanged)
//				ma.sendEventInUiThread(WebViewEvent.WWEVENT_WINDOW_LIST_CHANGED, null);
//			else
			if(curChanged)
				ma.sendEventInUiThread(WebViewEvent.WWEVENT_CUR_WINDOW_CHANGES, null);
			if(windowsChanged||load)
				ma.sendEventInUiThread(WebViewEvent.WWEVENT_WINDOWS_INVALIDATE, null);
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		if(load||!mPaused)
			GlobalHandler.command.sendDelayed(WHAT_INTERVAL, this, WHAT_INTERVAL);
	}
}
