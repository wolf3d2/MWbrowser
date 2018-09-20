package com.jbak.superbrowser.ui;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.jbak.superbrowser.BrowserApp;
import com.jbak.superbrowser.BrowserApp.OnGlobalEventListener;
import com.jbak.superbrowser.Db;
import com.jbak.superbrowser.IConst;
import com.jbak.superbrowser.MainActivity;
import com.jbak.superbrowser.MainActivityRef;
import com.jbak.superbrowser.Prefs;
import com.mw.superbrowser.R;
import com.jbak.superbrowser.WebViewEvent;
import com.jbak.superbrowser.panels.InterfaceSettingsLayout;
import com.jbak.superbrowser.panels.PanelQuickTools;
import com.jbak.superbrowser.panels.PanelSettings;
import com.jbak.superbrowser.panels.PanelUrlEdit;
import com.jbak.superbrowser.panels.PanelWindows;
import com.jbak.superbrowser.ui.themes.MyTheme;
import com.jbak.ui.UIUtils;
import com.jbak.utils.ObjectKeyValues;

public class PanelLayout extends MainActivityRef implements WebViewEvent,OnGlobalEventListener{
	public static final int PANEL_TABS = 1;
	public static final int PANEL_BOOKMARKS = 2;
	public static final int PANEL_QUICK_TOOLS = 3;
	public static final int PANEL_URL = 4;
	public static final int PANEL_MAX = 4;
	public static final int PANEL_SEARCH_HISTORY = 5;
	public static final int PANEL_MAINMENU_TOOLS = 6;
	static ObjectKeyValues<Integer, Integer> PANEL_STRINGS = new ObjectKeyValues<Integer, Integer>
			(
				PANEL_TABS,R.string.panelWindows,
				PANEL_BOOKMARKS,R.string.panelBookmarks,
				PANEL_QUICK_TOOLS,R.string.panelQuickTools,
				PANEL_URL,R.string.panelUrl
			);
	@SuppressLint("UseSparseArrays")
	HashMap<Integer, View> mPanels = new HashMap<Integer, View>();
	boolean mVisible = true;
	LinearLayout mTopContainer;
	LinearLayout mBottomContainer;
	private static PanelSettings mPanelSettings;
	public PanelLayout(MainActivity act) {
		super(act);
		mTopContainer = (LinearLayout)getMain().getTopContainer().findViewById(R.id.topPanelContainer);
		mBottomContainer = (LinearLayout)getMain().getTopContainer().findViewById(R.id.bottomPanelContainer);
//		setFromSettings();
//		mTopContainer.forceLayout();
//		mBottomContainer.forceLayout();
		act.addWebViewListener(this);
		BrowserApp.INSTANCE.addGlobalListener(this);
	}
	public final void removePanel(int panelId)
	{
		mPanels.remove(panelId);
	}
	public void createPanel(PanelSetting set)
	{
		View v = null;
		if(set.visible==false)
		{
			v = mPanels.get(set.id);
			if(v!=null&&v.getParent() instanceof LinearLayout)
				((LinearLayout)v.getParent()).removeView(v);
			mPanels.remove(set.id);
			
		}
		else
		{
			v = mPanels.get(set.id);
			if(v!=null)
			{
				ViewGroup parent = (ViewGroup) v.getParent();
				if(parent!=null)
					parent.removeView(v);
			}
			else
			{
				switch (set.id) {
					case PANEL_BOOKMARKS:
						v = new BookmarkHorizontalPanel(getMain());
						break;
					case PANEL_MAINMENU_TOOLS:
					case PANEL_QUICK_TOOLS:
						v = new PanelQuickTools(getMain(),PanelQuickTools.TYPE_TOOLS_MINIPANEL);
						break;
					case PANEL_URL:
						v = new PanelUrlEdit(getMain());
						break;
					case PANEL_TABS:
						v = new PanelWindows(getMain());
//						v = new WindowsHorizontalPanel(mAct);
						break;
				}
			}
			mPanels.put(set.id,v);
			if(v instanceof WebViewEvent)
				getMain().addWebViewListener((WebViewEvent)v);
			if(set.top)
				mTopContainer.addView(v);
			else
				mBottomContainer.addView(v);
		}
			
	}
	public View getPanel(int id)
	{
		return mPanels.get(id);
	}
	public final void setVisibility(boolean visible)
	{
		UIUtils.showViews(visible, mTopContainer,mBottomContainer);
		mVisible = visible;
	}
	public void forceUpdate()
	{
//		HorizontalPanel hp = (HorizontalPanel) getPanel(PANEL_QUICK_TOOLS);
//		if(hp!=null)
//			hp.forceUpdate();
//		for(View v:mPanels.values())
//		{
//			MyTheme.get().setViews(MyTheme.ITEM_HORIZONTAL_PANEL_BACKGROUND, v);
//		}
		mPanels.clear();
		setFromSettings();
	}
	public void onThemeChange(MyTheme t)
	{
		forceUpdate();
		//t.setViews(MyTheme.ITEM_HORIZONTAL_PANEL_BACKGROUND, getAllPanels());
	}
	public static final boolean isPanelVisible(int panelId)
	{
		PanelSetting s = getPanelSetting(panelId);
		return s!=null&&s.visible;
	}
	public static final PanelSetting getPanelSetting(int panelId)
	{
		return getPanelSettings().getPanelSetting(panelId);
	}
	public static PanelSettings getPanelSettings()
	{
		if(mPanelSettings==null)
			mPanelSettings = new PanelSettings(Prefs.PANEL_SETTINGS,PANEL_STRINGS,new PanelSettings.SetDefaultPanelSetting() {
			
			@Override
			public void setDefaultPanelSetting(PanelSetting ps) {
				if(ps.id==PANEL_QUICK_TOOLS&&(Prefs.getInterface()==Prefs.INTERFACE_PANEL||BrowserApp.deviceType==BrowserApp.DEVICE_TYPE_XLARGE))
					ps.visible = true;
				if(ps.id==PANEL_MAINMENU_TOOLS&&(Prefs.getInterface()==Prefs.INTERFACE_PANEL||BrowserApp.deviceType==BrowserApp.DEVICE_TYPE_XLARGE))
					ps.visible = true;
				else if(ps.id==PANEL_URL&&(BrowserApp.deviceType==BrowserApp.DEVICE_TYPE_XLARGE||BrowserApp.deviceType==BrowserApp.DEVICE_TYPE_LARGE))
					ps.visible = true;
				else if(ps.id==PANEL_TABS)
					ps.visible = true;
				else 
					ps.visible = false;
				if(ps.id==PANEL_QUICK_TOOLS||ps.id==PANEL_TABS)
					ps.extraSettings = new JSONObject();
				if(ps.id==PANEL_MAINMENU_TOOLS||ps.id==PANEL_TABS)
					ps.extraSettings = new JSONObject();
			}

			@Override
			public void setDefaultExtraSettings(PanelSetting ps) {
				if(ps.id==PANEL_QUICK_TOOLS)
				{
					ps.extraSettings = new JSONObject();
					String set = Db.getStringTable().get(IConst.STRVAL_MINI_PANEL);
						try {
							if(set!=null)
							{
								ps.extraSettings.put(IConst.STRVAL_MINI_PANEL, set);
								Db.getStringTable().delete(IConst.STRVAL_MINI_PANEL);
							}
						} catch (Throwable e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				}
				if(ps.id==PANEL_MAINMENU_TOOLS)
				{
					ps.extraSettings = new JSONObject();
					String set = Db.getStringTable().get(IConst.STRVAL_MAINMENU_PANEL);
						try {
							if(set!=null)
							{
								ps.extraSettings.put(IConst.STRVAL_MAINMENU_PANEL, set);
								Db.getStringTable().delete(IConst.STRVAL_MAINMENU_PANEL);
							}
						} catch (Throwable e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				}
				if(ps.id==PANEL_TABS)
				{
					ps.extraSettings = new JSONObject();
					try {
						ps.extraSettings.put(IConst.MAX_TAB_ROWS, 1);
						Resources r = BrowserApp.INSTANCE.getResources();
						ps.extraSettings.put(IConst.MIN_TAB_WIDTH, (int)r.getDimension(R.dimen.text_row_min_size));
						ps.extraSettings.put(IConst.MAX_TAB_WIDTH, (int)r.getDimension(R.dimen.text_row_max_size));
					}
					catch(Throwable e)
					{
						
					}
				}
			}
		});
		return mPanelSettings;
	}
	public void setFromSettings()
	{
		mTopContainer.removeAllViews();
		mBottomContainer.removeAllViews();
		if(mPanelSettings==null)
			mPanelSettings = getPanelSettings();
		for(PanelSetting ps:mPanelSettings)
		{
			try{
				createPanel(ps);
			}
			catch(Throwable e)
			{
				e.printStackTrace();
			}
		}
	}
	public ArrayList<View>getAllPanels()
	{
		ArrayList<View> ar = new ArrayList<View>();
		for(int i=0;i<mTopContainer.getChildCount();i++)
			ar.add(mTopContainer.getChildAt(i));
		for(int i=0;i<mBottomContainer.getChildCount();i++)
			ar.add(mBottomContainer.getChildAt(i));
		return ar;
	}
	public void setPanel(PanelSetting set)
	{ 
		mPanelSettings.setPanel(set);
		setFromSettings();
	}
	@Override
	public void onWebViewEvent(int code, EventInfo info) {
		if(code==WWEVENT_SOFT_KEYBOARD_VISIBLE||code==WWEVENT_SOFT_KEYBOARD_HIDDEN)
		{
			if(code==WWEVENT_SOFT_KEYBOARD_VISIBLE)
			{
				PanelUrlEdit pan = (PanelUrlEdit) getPanel(PANEL_URL);
				if((pan==null||!pan.hasInputFocus()) && getMain().getWebView().getInputConnection()!=null)
					setVisibility(false);
			}
			else if(code==WWEVENT_SOFT_KEYBOARD_HIDDEN&&!mVisible&&!getMain().isPanelShown())
				setVisibility(true);
		}
	}
	@Override
	public void onGlobalEvent(int code, Object param) {
		if(code==BrowserApp.GLOBAL_SETTINGS_CHANGED)
		{
			if(Prefs.PANEL_SETTINGS.equals(param))
			{
				setFromSettings();
				InterfaceSettingsLayout.checkMagicKeyCanDisabled(getMain());
			}
			else if(IConst.PANEL_WINDOWS.equals(param))
			{
				removePanel(PANEL_TABS);
				setFromSettings();
				//InterfaceSettingsLayout.checkMagicKeyCanDisabled(mAct);
			}
		}
	}
}
