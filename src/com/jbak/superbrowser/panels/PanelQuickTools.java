package com.jbak.superbrowser.panels;

import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.widget.FrameLayout;
import com.jbak.superbrowser.ActArray;
import com.jbak.superbrowser.Action;
import com.jbak.superbrowser.BrowserApp;
import com.jbak.superbrowser.BrowserApp.OnGlobalEventListener;
import com.jbak.superbrowser.IConst;
import com.jbak.superbrowser.MainActivity;
import com.jbak.superbrowser.WebViewEvent;
import com.jbak.superbrowser.Tab;
import com.jbak.superbrowser.ui.HorizontalPanel;
import com.jbak.superbrowser.ui.MyWebView;
import com.jbak.superbrowser.ui.OnAction;
import com.jbak.superbrowser.ui.PanelButton;
import com.jbak.superbrowser.ui.PanelLayout;
import com.jbak.superbrowser.ui.PanelSetting;
import com.jbak.superbrowser.ui.themes.MyTheme;

public class PanelQuickTools extends FrameLayout implements WebViewEvent,OnGlobalEventListener{
	public static final int TYPE_TOOLS_MAIN = 1;
	public static final int TYPE_TOOLS_MINIPANEL = 2;
	protected int mType = TYPE_TOOLS_MAIN;
	HorizontalPanel mPanel;
	ActArray mMinipanelSettingsAction;
	public PanelQuickTools(Context context,int type) {
		super(context);
		mType = type;
		init();
	}
	public PanelQuickTools(Context context, AttributeSet ats) {
		super(context, ats);
		init();
	}
	protected void init()
	{
		mPanel = new HorizontalPanel(getContext());
		if(getContext() instanceof MainActivity)
			setLongClickListener((MainActivity)getContext());
		//! создание минипанели
		if(mType==TYPE_TOOLS_MINIPANEL)
		{
			mMinipanelSettingsAction = getMinipanelActions();
			BrowserApp.INSTANCE.addGlobalListener(this);
			mPanel.setButtonsType(getButtonTypeMiniPanel());
		}
		MyTheme.get().setView(this, MyTheme.ITEM_ACTIVITY_BACKGROUND);
		setActions();
		addView(mPanel);
	}
	@Override
	public void onWebViewEvent(int code, EventInfo info) {
		if(code==WWEVENT_CUR_WINDOW_CHANGES)
			setActions();
	}
	MyWebView getWebView()
	{
		if(getContext() instanceof MainActivity)
		{
			return ((MainActivity)getContext()).getWebView();
		}
		return null;
	}
	public static void addRefreshBackForward(MainActivity act,ActArray ar)
	{
		Action a = getActionStopOrRefresh(act);
		if(a!=null)
			ar.add(a);
		WebView webView = act.getWebView();
		if(webView==null)
			return;
		if(webView.canGoBack())
			ar.add(Action.create(Action.GO_BACK));
		if(webView.canGoForward())
			ar.add(Action.create(Action.GO_FORWARD));

	}
	public static void setMinipanelActions(ActArray ar)
	{
		PanelSettings ps = PanelLayout.getPanelSettings();
		PanelSetting set = ps.getPanelSetting(PanelLayout.PANEL_QUICK_TOOLS);
		if(set!=null)
		{
			try{
				if(set.extraSettings==null)
					set.extraSettings = new JSONObject();
				set.extraSettings.put(IConst.STRVAL_MINI_PANEL, ar.getCommaSeparated());
			}
			catch(Throwable e)
			{}
			ps.setPanel(set);
//			ps.reload();
			BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_SETTINGS_CHANGED, IConst.STRVAL_MINI_PANEL);
		}
//		Db.getStringTable().saveIntArray(IConst.STRVAL_MINI_PANEL, ids);
	}
	static final int getButtonTypeMiniPanel()
	{
		PanelSetting ps = PanelLayout.getPanelSetting(PanelLayout.PANEL_QUICK_TOOLS);
		if(ps!=null&&ps.extraSettings!=null)
			return ps.extraSettings.optInt(IConst.BUTTON_TYPE, IConst.BUTTONS_MEDIUM);
		return IConst.BUTTONS_MEDIUM;
	}
	public static ActArray getMinipanelActions()
	{
//		ArrayList<Integer> ids = Db.getStringTable().getIntArray(IConst.STRVAL_MINI_PANEL);
		PanelSetting ps = PanelLayout.getPanelSettings().getPanelSetting(PanelLayout.PANEL_QUICK_TOOLS);
		String actions = ps.extraSettings.optString(IConst.STRVAL_MINI_PANEL);
		if(TextUtils.isEmpty(actions))
		{
			return new ActArray(
					Action.MINI_PANEL_SETTINGS,
					Action.QUICK_SETTINGS,
					Action.EXIT,
					Action.HISTORY,
					Action.REFRESH,
					Action.CLOSE_TAB,
					Action.NEW_TAB,
					Action.BOOKMARKS,
					Action.SEARCH_ON_PAGE
					);
		}
		ActArray ar = new ActArray(actions);
		return ar;
	}
	public void setActions()
	{
		MyWebView webView = getWebView();
		ActArray ar = null;
		if(webView==null)
		{
			mPanel.setActions(new ActArray());
			return;
		}
		if(mType==TYPE_TOOLS_MAIN){
			ar = new ActArray();
			ar.add(Action.GO_BACK);
			ar.add(Action.GO_FORWARD);
			ar.add(Action.REFRESH);
			ar.add(Action.COPY_URL_TO_CLIPBOARD);
			// !!!
			ar.add(Action.SHOW_CLOSED_TABS);
			ar.add(Action.TO_TOP);
			ar.add(Action.TO_BOTTOM);
		}
		else if(mType==TYPE_TOOLS_MINIPANEL)
		{
			ar = new ActArray();
			ar.addAll(mMinipanelSettingsAction);
		}
		for(int i=ar.size()-1;i>=0;i--)
		{
			Action a = ar.get(i);
			switch (a.command) {
			case Action.REFRESH:
				MainActivity ma = (MainActivity)getContext();
				if(ma.getWebView()!=null&&ma.getTab()!=null&&ma.getTab().isLoading())
					ar.set(i, Action.create(Action.STOP));
				break;
			case Action.GO_BACK:
				if(!webView.canGoBack())
					ar.remove(i);
				break;
			case Action.GO_FORWARD:
				if(!webView.canGoForward())
					ar.remove(i);
				break;
			default:
				break;
			}
			//((MainActivity)getContext()).setNavigationPanel();
		}
		mPanel.setActions(ar);
	}
	public static Action getActionStopOrRefresh(MainActivity ma)
	{
		Tab ww = ma.getTab();
		if(ww!=null&&ww.isLoading())
			return Action.create(Action.STOP);
		else
			return Action.create(Action.REFRESH);
	}
	public final void setOnActionListener(OnAction listener)
	{
		mPanel.setOnActionListener(listener);
	}
	public final void setLongClickListener(OnLongClickListener listener)
	{
		mPanel.setItemLongClickListener(listener);
	}
	@Override
	public void onGlobalEvent(int code, Object param) {
		if(code==BrowserApp.GLOBAL_SETTINGS_CHANGED&&IConst.STRVAL_MINI_PANEL.equals(param))
		{
			mMinipanelSettingsAction = getMinipanelActions();
			setActions();
			mPanel.setButtonsType(getButtonTypeMiniPanel());
		}
		
	}
}
