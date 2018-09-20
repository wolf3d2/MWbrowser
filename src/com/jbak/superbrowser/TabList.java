package com.jbak.superbrowser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.webkit.WebView;
/** Класс для работы с окнами (вкладками) 
 * 	Хранит id всех открытых окон, отправляет оконные события всем подписчикам
 * */
public class TabList extends MainActivityRef implements IConst,WebViewEvent{
	public static int lastTempSession = 1;
	@SuppressLint("UseSparseArrays")
	HashMap<Integer, Tab> mWindows = new HashMap<Integer, Tab>();
	CopyOnWriteArrayList<Tab> mOpenedWindows = new CopyOnWriteArrayList<Tab>();
	Tab mCurWindow;
	ActArray mActArray;
	ArrayList<Integer> mIds = new ArrayList<Integer>();
	int mLastSessId = 0;
	int mTempSession = 0;
	boolean mIncognito = false;
	public TabList(MainActivity act)
	{
		this(act, 0,false);
	}
	public TabList(MainActivity act,int tempSessionId, boolean closed)
	{
		super(act);
		mTempSession = tempSessionId;
		if(mTempSession>0)
			lastTempSession = mTempSession+1;
		setMain(act);
		if(!isTempSession())
			mIds = loadWindowIds(closed);
		if(!closed&&!isTempSession())
		{
			String lastId = Db.getStringTable().get(CUR_WINDOW_ID);
			if(!TextUtils.isEmpty(lastId)&&TextUtils.isDigitsOnly(lastId))
				mLastSessId = Integer.decode(lastId);
		}
	}
	public final Tab getCurrent()
	{
		return mCurWindow;
	}
	public void setCurrent(Tab ww)
	{
		mCurWindow = ww;
		sendEvent(ww,WebViewEvent.WWEVENT_WINDOW_LIST_CHANGED);
	}
	public final int getCurPos()
	{
		if(mCurWindow==null)
			return -1;
		return getPosById(mCurWindow.windowId);
	}
	public final int getNewTabId()
	{
		if(mIds==null)
			return 1;
		int id = 0;
		for(Integer i:mIds)
		{
			if(i>id)
				id = i;
		}
		return id+1;
	}
	public void addOpenedTab(Tab tab,boolean background)
	{
		if(mOpenedWindows.size()>0&&mOpenedWindows.get(mOpenedWindows.size()-1)==tab)
			return;
		int oldPos = getCurPos();
		if(!background)
			mCurWindow = tab;
		for(int i=mOpenedWindows.size()-1;i>=0;i--)
		{
			if(mOpenedWindows.get(i).windowId==tab.windowId)
				mOpenedWindows.remove(i);
		}
		int realPages = Prefs.getRealPages();
		while(mOpenedWindows.size()>=realPages)
		{
			Tab w = mOpenedWindows.remove(0);
			getMain().clearWindow(w);
		}
		if(background&&mOpenedWindows.size()>0)
			mOpenedWindows.add(mOpenedWindows.size()-1,tab);
		else
			mOpenedWindows.add(tab);
		getMain().setProgress(tab);
		mWindows.remove(tab.windowId);
		int pos = -1;
		if(!mIds.contains(tab.windowId))
		{
			if(oldPos>=mIds.size()-1)
			{
				mIds.add(Integer.valueOf(tab.windowId));
				pos = getCount()-1;
			}
			else
			{
				pos = oldPos+1;
				mIds.add(pos, Integer.valueOf(tab.windowId));
			}
			saveWindowsIds();
		}
		else
			pos = getPosById(pos);
		sendEvent(tab,WebViewEvent.WWEVENT_WINDOW_LIST_CHANGED);
	}
	public final int getCount()
	{
		return mIds.size();
	}
	public final int getPos(Tab ww)
	{
		return getPosById(ww.windowId);
	}
	public final int getPos(WebView view)
	{
		if(view==null)
			return -1;
		Tab ww = getTabByWebView(view);
		if(ww!=null)
			return getPos(ww);
		return -1;
	}
	public final Tab getTabByWebView(WebView ww)
	{
		if(ww==null)
			return null;
		for(Tab wnd:mOpenedWindows)
		{
			if(wnd.getWebView()==ww)
				return wnd;
		}
		return null;
	}
	public final int getIdAt(int pos)
	{
		return mIds.get(pos);
	}
	public final int getPosById(int id)
	{
		for(int i=mIds.size()-1;i>=0;i--)
		{
			if(mIds.get(i).intValue()==id)
				return i;
		}
		return -1;
	}
	public final Tab getTabById(int windowId,boolean loadSettings)
	{
		int pos = getPosById(windowId);
		if(pos>-1)
			return getWindowAt(pos, loadSettings);
		return null;
	}
	public final Tab getWindowAt(int pos,boolean loadSettings)
	{
		int id = getIdAt(pos);
		if(mCurWindow!=null&&mCurWindow.windowId==id)
			return mCurWindow;
		Tab ww = getTabFromList(mOpenedWindows, id);
		if(ww!=null)
			return ww;
		ww = mWindows.get(id);
		if(ww!=null&&(!loadSettings||loadSettings&&ww.savedState!=null))
			return ww;
		ww = Db.getWindowTable().loadWindow(getMain(), id, loadSettings);
		mWindows.put(ww.windowId, ww);
		return ww;
	}
	public final int getOpenedSize()
	{
		return mOpenedWindows.size();
	}
	public Tab removeOpenedWindow(int pos)
	{
		if(pos>=mOpenedWindows.size())
			return null;
		return mOpenedWindows.remove(pos);
	}
	public void closeTab(int windowId)
	{
		closeTab(windowId, true);
	}
	public void closeTab(int windowId,boolean startNew)
	{
		int closePos = getPosById(windowId);
		Tab close = getWindowAt(closePos, false);
		boolean exit = close!=null&&close.currentBookmark==null||isTempSession();
		int newPos = -1;
		Tab cur = null;
		if(closePos==getCurPos()&&mOpenedWindows.size()>1) // Если закрываем активное окно и есть другие открытые окна - перемещаемся к предыдущему открытому окну
		{
			cur = mOpenedWindows.get(mOpenedWindows.size()-2);
			newPos = getPosById(cur.windowId);
		}
		else if(getCount()>1)
		{
			newPos = closePos>0?newPos = closePos-1:closePos+1;
			cur = getWindowAt(newPos, true);
		}
		removeOpenedTab(close);
		close.setClosed(true);
		getMain().clearWindow(close);
		if(close.isEmpty())
			Db.getWindowTable().deleteWindow(windowId);
		else
			Db.getWindowTable().setWindowClosed(windowId, true);
		mIds.remove(Integer.valueOf(windowId));
		saveWindowsIds();
		if(cur!=null)
		{
			mCurWindow = cur;
			getMain().tabStart(cur, null);
		}
		else
		{
			if(exit)
				getMain().exit();
			else
				getMain().tabStart(null, null);
		}
		sendEvent(mCurWindow,WebViewEvent.WWEVENT_WINDOW_LIST_CHANGED);
	}
	public void removeOpenedTab(Tab ww)
	{
		mOpenedWindows.remove(ww);
	}
	public Tab getOpenedTabByPos(int pos)
	{
		return mOpenedWindows.get(pos);
	}
	public Tab getOpenedTab(int windowId)
	{
		return getTabFromList(mOpenedWindows, windowId);
	}
	public static final Tab getTabFromList(List<Tab> list,int windowId)
	{
		for(Tab ww:list)
		{
			if(ww.windowId==windowId)
				return ww;
		}
		return null;
	}
	public boolean closeCurrent()
	{
		if(mCurWindow==null||mOpenedWindows.size()==0)
			return false;
		closeTab(mCurWindow.windowId,false);
		return true;
	}
	public void clearOpened()
	{
		for(Tab ww:mOpenedWindows)
			getMain().clearWindow(ww);
		mOpenedWindows.clear();
	}
	public Tab restoreLast()
	{
		Db.getWindowTable().setCloseAllWindows(false);
		mIds = Db.getWindowTable().getAllWindowsIds(false);
		int pos = getPosById(mLastSessId);
		if(pos<0&&mIds.size()>0)
			pos = 0;
		if(pos<0)
			return null;
		return getWindowAt(pos, true);
	}
	public void sendEvent(Tab wnd,int code)
	{
		if(wnd==null)
			getMain().sendWebViewEvent(code, null, wnd, null);
		else
			getMain().sendWebViewEvent(code, wnd.getWebView(), wnd, null);
	}
	@Override
	public void onWebViewEvent(int code, EventInfo info) {
		if(info!=null&&info.getWebView()!=null&&code>=WebViewEvent.WWEVENT_PAGE_START&&code<=WebViewEvent.WWEVENT_FAVICON_LOADED)
		{
			Tab ww = getTabByWebView(info.getWebView());
			if(ww==null)
				return;
			ww.onWebViewEvent(code, info);
		}
	}
	public void saveWindowsIds()
	{
		Db.getStringTable().saveIntArray(STRVAL_WINDOWS_IDS, mIds);
	}
	public ArrayList<Integer> loadWindowIds(boolean closed)
	{
		if(!closed)
		{
			ArrayList<Integer> ar = Db.getStringTable().getIntArray(STRVAL_WINDOWS_IDS);
			if(ar!=null&&!ar.isEmpty())
				return ar;
		}
		return Db.getWindowTable().getAllWindowsIds(closed);
				
	}

	public void closeAllTabs(boolean startNew) 
	{
		getMain().clearWindows();
		Db.getWindowTable().setCloseAllWindows(true);
		mIds.clear();
		saveWindowsIds();
		if(startNew)
			getMain().tabStart(null,null);
	}
	public final boolean isTempSession()
	{
		return mTempSession>0;
	}
	public void setTempSession(int sess)
	{
		mTempSession = sess;
	}
	public final CopyOnWriteArrayList<Tab> getOpenedTabs()
	{
		return mOpenedWindows;
	}
	public void setIncognito(boolean incognito)
	{
		mIncognito = incognito;
	}
	public final boolean isIncognito()
	{
		return mIncognito;
	}
}
