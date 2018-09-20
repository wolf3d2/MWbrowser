package com.jbak.superbrowser.panels;

import org.json.JSONObject;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.widget.FrameLayout;
import com.jbak.superbrowser.ActArray;
import com.jbak.superbrowser.Action;
import com.jbak.superbrowser.BrowserApp;
import com.jbak.superbrowser.BrowserApp.OnGlobalEventListener;
import com.jbak.superbrowser.pluginapi.Plugin;
import com.jbak.superbrowser.search.SearchSystem;
import com.jbak.superbrowser.IConst;
import com.jbak.superbrowser.MainActivity;
import com.jbak.superbrowser.Prefs;
import com.jbak.superbrowser.SearchAction;
import com.jbak.superbrowser.stat;
import com.jbak.superbrowser.WebViewEvent;
import com.jbak.superbrowser.Tab;
import com.jbak.superbrowser.ui.HorizontalPanel;
import com.jbak.superbrowser.ui.MyWebView;
import com.jbak.superbrowser.ui.OnAction;
import com.jbak.superbrowser.ui.PanelButton;
import com.jbak.superbrowser.ui.PanelLayout;
import com.jbak.superbrowser.ui.PanelSetting;
import com.jbak.superbrowser.ui.themes.MyTheme;
import com.mw.superbrowser.R;

public class PanelMainMenu extends FrameLayout implements WebViewEvent,OnGlobalEventListener{
	public static final int TYPE_TOOLS_MAIN = 1;
	public static final int TYPE_TOOLS_MAINMENU_PANEL = 2;
	protected int mType = TYPE_TOOLS_MAIN;
	HorizontalPanel mPanel;
	ActArray mMainmenupanelSettingsAction;
	public PanelMainMenu(Context context,int type) {
		super(context);
		mType = type;
		init();
	}
	public PanelMainMenu(Context context, AttributeSet ats) {
		super(context, ats);
		init();
	}
	protected void init()
	{
		mPanel = new HorizontalPanel(getContext());
		if(getContext() instanceof MainActivity)
			setLongClickListener((MainActivity)getContext());
		//! создание минипанели
		if(mType==TYPE_TOOLS_MAINMENU_PANEL)
		{
			mMainmenupanelSettingsAction = getMainMenuPanelActions();
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
	public static void setMainmenuPanelActions(ActArray ar)
	{
		String out = stat.STR_NULL;
		for (int i=0;i<ar.size();i++){
			out+= stat.STR_NULL+ar.get(i).command+",";
		}
		Prefs.setString(IConst.STRVAL_MAINMENU_PANEL, out);
//		PanelSettings ps = PanelLayout.getPanelSettings();
//		PanelSetting set = ps.getPanelSetting(PanelLayout.PANEL_MAINMENU_TOOLS);
//		if(set!=null)
//		{
//			try{
//				if(set.extraSettings==null)
//					set.extraSettings = new JSONObject();
//				set.extraSettings.put(IConst.STRVAL_MAINMENU_PANEL, ar.getCommaSeparated());
//			}
//			catch(Throwable e)
//			{}
//			ps.setPanel(set);
////			ps.reload();
//			BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_SETTINGS_CHANGED, IConst.STRVAL_MAINMENU_PANEL);
//		}
	}
	static final int getButtonTypeMiniPanel()
	{
		PanelSetting ps = PanelLayout.getPanelSetting(PanelLayout.PANEL_MAINMENU_TOOLS);
		if(ps!=null&&ps.extraSettings!=null)
			return ps.extraSettings.optInt(IConst.BUTTON_TYPE, IConst.BUTTONS_SMALL);
		return IConst.BUTTONS_SMALL;
	}
	// кнопки в левой панели
	public static ActArray getMainMenuPanelActions()
	{
		String actions = Prefs.getString(IConst.STRVAL_MAINMENU_PANEL, stat.STR_NULL);
		if(TextUtils.isEmpty(actions))
		{
			ActArray ar = new ActArray();
			ar.addAll(getDefaultMainMenuPanelActions());
			return ar;
		}
		ActArray ar = new ActArray(actions);
		return ar;
	}
	public static ActArray getDefaultMainMenuPanelActions()
	{
		ActArray ar = new ActArray();
		ar.add(Action.create(Action.FONT_SCALE_SETTINGS));
		ar.add(Action.MIN_FONT_SIZE);
		ar.add(Action.VOICE_SEARCH);
		ar.add(Action.create(Action.SEARCH_ON_PAGE));
		ar.add(Action.create(Action.CLEAR_TEXT));
		ar.add(Action.create(Action.IMPORT));
		ar.add(Action.create(Action.EXPORT));
		ar.add(Action.create(Action.BOOKMARKS));
		ar.add(Action.create(Action.ADD_BOOKMARK));
		ar.add(Action.create(Action.HISTORY));
		ar.add(Action.create(Action.SHARE_URL));
		ar.add(Action.create(Action.CLEAR_DATA));
		ar.add(Action.create(Action.TAB_LIST));
		ar.add(Action.create(Action.CLOSE_TAB));
		ar.add(Action.create(Action.NEW_TAB));
		ar.add(Action.create(Action.DOWNLOAD_LIST));
		ar.add(Action.create(Action.SOURCE_CODE));
		ar.add(Action.create(Action.GO_HOME));
		ar.add(Action.create(Action.COPY_ALL_OPEN_URL));
		if(Build.VERSION.SDK_INT>=11)
			ar.add(Action.create(Action.SAVEFILE).setText(R.string.act_save_page));
		ar.add(Action.create(Action.OPENFILE));
		ar.add(Action.create(Action.QUICK_SETTINGS));
		ar.add(Action.SETTINGS);
//		BrowserApp.pluginServer.getPluginActions(ar, Plugin.WINDOW_MAIN_MENU,this);
		ar.add(Action.TRANSLATE_LINK);
		return ar;
	}
// горизонтальное меню 
	public void setActions()
	{
		MyWebView webView = getWebView();
		ActArray ar = null;
		if(webView==null)
		{
			mPanel.setActions(new ActArray());
			return;
		}
		if(mType==TYPE_TOOLS_MAIN)
			ar = new ActArray(Action.GO_BACK,Action.GO_FORWARD,Action.REFRESH,Action.COPY_URL_TO_CLIPBOARD,Action.TO_TOP,Action.TO_BOTTOM);
		else if(mType==TYPE_TOOLS_MAINMENU_PANEL)
		{
			ar = new ActArray();
			ar.addAll(mMainmenupanelSettingsAction);
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
		if(code==BrowserApp.GLOBAL_SETTINGS_CHANGED&&IConst.STRVAL_MAINMENU_PANEL.equals(param))
		{
			mMainmenupanelSettingsAction = getMainMenuPanelActions();
			setActions();
			mPanel.setButtonsType(getButtonTypeMiniPanel());
		}
		
	}
}
