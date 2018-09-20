package com.jbak.superbrowser.adapters;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.widget.ImageView;
import ru.mail.mailnews.st;

import com.jbak.superbrowser.Action;
import com.jbak.superbrowser.Bookmark;
import com.jbak.superbrowser.BookmarkActivity;
import com.jbak.superbrowser.BrowserApp;
import com.jbak.superbrowser.IConst;
import com.jbak.superbrowser.MainActivity;
import com.jbak.superbrowser.Payment;
import com.jbak.superbrowser.Prefs;
import com.jbak.superbrowser.stat;
import com.mw.superbrowser.R;
import com.jbak.superbrowser.panels.PanelSettings;
import com.jbak.superbrowser.search.SearchSystem;
import com.jbak.superbrowser.ui.BookmarkView;
import com.jbak.superbrowser.ui.MainPanel;
import com.jbak.superbrowser.ui.OnAction;
import com.jbak.superbrowser.ui.PanelSetting;
import com.jbak.superbrowser.ui.dialogs.DialogDownloadFile;
import com.jbak.superbrowser.ui.dialogs.MenuSettingBookmarks;
import com.jbak.superbrowser.ui.dialogs.ThemedDialog;
import com.jbak.superbrowser.ui.themes.MyTheme;
import com.jbak.superbrowser.utils.DbClear;
import com.jbak.ui.CustomDialog.OnUserInput;
import com.jbak.ui.CustomPopup;
import com.jbak.ui.ConfirmOper;
import com.jbak.ui.UIUtils;
import com.jbak.utils.ObjectKeyValues;
import com.jbak.utils.StrBuilder;
import com.jbak.utils.Utils;
// обработка нажатия на пункте настроек в onSettingClick
public class SettingsAdapter extends BookmarkAdapter.ArrayBookmarkAdapter implements OnClickListener,IConst{

	Context m_c = null;
	
	public SettingsAdapter(Context c,ArrayList<Bookmark>settingsBookmarks) {
		super(c, settingsBookmarks);
		m_c = c;
		mAutoLoadImages = false;
	}
	private void selectDownloadDir(final SettingsBookmark bm)
	{
		BookmarkActivity.runForFileDirSelect((Activity)getContext(), new OnAction() {
			
			@Override
			public void onAction(Action act) {
				File f = (File) act.param;
				bm.setDesc(f.getAbsolutePath()).setPref(f.getAbsolutePath());
				onSettingChanged(bm);
			}
		});
	}
	public static final SettingsBookmark getBookmarkForInteger(Integer val)
	{
		return new SettingsBookmark(val.toString(), null).setParam(val);
	}
	public static ArrayList<Bookmark> getNumberedSettingsBookmarks(int currentValue,int step,int min,int max)
	{
		ArrayList<Bookmark> ar = new ArrayList<Bookmark>();
		boolean valAdded = false;
		for(int i=min;i<=max;i+=step)
		{
			Integer p = Integer.valueOf(i);
			if(i==currentValue)
				valAdded = true;
			else if(currentValue<i&&!valAdded)
			{
				ar.add(getBookmarkForInteger(currentValue));
				valAdded = true;
			}
			ar.add(getBookmarkForInteger(p));
		}
		return ar;
	}
	public final void setCheckbox(BookmarkView bv,SettingsBookmark sb)
	{
		ImageView cb = bv.getFaviconView();
		if(sb.checkBox==null)
			cb.setVisibility(View.GONE);
		else
		{
			cb.setVisibility(View.VISIBLE);
			cb.setImageResource(sb.checkBox?android.R.drawable.checkbox_on_background:android.R.drawable.checkbox_off_background);
			if(sb.checkBoxAndMenuSeparate)
			{
				bv.getNormalTextView().setText(sb.getUrl());
			}
			else
			{
				int res = sb.checkBox?sb.checkBoxTrueRes:sb.checkBoxFalseRes;
				bv.getNormalTextView().setText(res);
			}
			if(sb.checkBoxAndMenuSeparate)
			{
				cb.setOnClickListener(this);
				cb.setTag(sb);
			}
		}
	}
	public void onCheckboxChanged(BookmarkView bv,SettingsBookmark sb)
	{
		if(!TextUtils.isEmpty(sb.prefKey)&&sb.checkBox!=null){
			Prefs.setBoolean(sb.prefKey, sb.checkBox);
			if (sb.prefKey.compareToIgnoreCase(Prefs.TABS_HEIGTH)==0){
				BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_SETTINGS_CHANGED, IConst.PANEL_WINDOWS);
			}
		}
		setCheckbox(bv, sb);
		notifyDataSetChanged();
	}
	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		
		BookmarkView.BookmarkViewType = BookmarkView.TYPE_SETTINGS;
		BookmarkView bv = (BookmarkView) super.getView(position, convertView, arg2);
		Bookmark b = getBookmark(position);
		if(b instanceof SettingsBookmark)
		{
			SettingsBookmark sb = (SettingsBookmark)b;
			ImageView close = bv.getClose();
			setCheckbox(bv, sb);
			if(sb.rightButtonImage!=0)
			{
				close.setImageResource(sb.rightButtonImage);
				close.setVisibility(View.VISIBLE);
				UIUtils.setViewsTag(sb, close);
				close.setOnClickListener(this);
			}
			else
			{
				close.setVisibility(View.GONE);
			}
		}
		BookmarkView.BookmarkViewType = BookmarkView.TYPE_DEFAULT;
		MyTheme.get().setViews(MyTheme.ITEM_SETTINGS_POS, position, bv);
		return bv;
	}
	public JSONObject getClearSettingsJson(String prefKey)
	{
		JSONObject jo = new JSONObject();
		try{
			for(Bookmark b:mBookmarks)
			{
				SettingsBookmark sb = (SettingsBookmark)b;
				jo.put(sb.prefKey, sb.checkBox?1:0);
				if(HISTORY.equals(sb.prefKey)&&sb.param instanceof Integer)
				{
					jo.put(CLEAR_TYPE, ((Integer)sb.param).intValue());
				}
				
			}
			Prefs.setString(prefKey, jo.toString());
		}
		catch(Throwable e)
		{
			Utils.log(e);
		}
		return jo;
	}
	private static SettingsBookmark getClearSettingsBookmark(Context c,JSONObject obj,String key,int title,int defVal)
	{
		int val = obj.optInt(key, defVal);
		SettingsBookmark sb = new SettingsBookmark(c, key, title, Boolean.valueOf(val!=0), R.string.yes, R.string.no, false);
		if(HISTORY.equals(key))
		{
			sb.checkBoxAndMenuSeparate = true;
			int ct = obj.optInt(CLEAR_TYPE, DbClear.CLEAR_ALL);
			sb.setDesc(c.getString(DbClear.CLEAR_HISTORY_TYPES.getValueByKey(ct)));
			sb.setParam(Integer.valueOf(ct));
		}
		return sb;
	}
	public static SettingsBookmark createSettingsBookmark(Context c,PanelSetting ps)
	{
		int desc = R.string.disabled;
		if(ps.visible)
			desc = ps.top?R.string.showTop:R.string.showBottom;
		SettingsBookmark sb = new SettingsBookmark(c, String.valueOf(ps.id), ps.nameRes, desc);
		sb.id = ps.id;
		sb.param = ps;
		return sb;
	}
	public static ArrayList<Bookmark> createFromPanelSettings(Context c,ArrayList<Bookmark> ar,PanelSettings ps)
	{
		for(PanelSetting set:ps)
		{
			SettingsBookmark sb = createSettingsBookmark(c, set);
			sb.panelSettings = ps;
			if(set.extraSettings!=null)
				sb.rightButtonImage = R.drawable.settings;
			ar.add(sb);
		}
		return ar;
	}
	public static ArrayList<Bookmark> getClearData(Context c,String clearPref)
	{
		ArrayList<Bookmark> ar = new ArrayList<Bookmark>();
		JSONObject jo = Prefs.getJSONObject(clearPref);
		ar.add(getClearSettingsBookmark(c,jo, IConst.CLEAR_CLOSE_WINDOWS, R.string.act_close_windows,0));
		ar.add(getClearSettingsBookmark(c,jo, IConst.CACHE, R.string.clear_data_cache, 0));
		ar.add(getClearSettingsBookmark(c,jo, IConst.HISTORY, R.string.clear_data_history, 0));
		ar.add(getClearSettingsBookmark(c,jo, IConst.COOKIES, R.string.clear_data_cookies, 0));
		ar.add(getClearSettingsBookmark(c,jo, IConst.CLEAR_SAVED_PASSWORDS, R.string.clear_passwords, 0));
		ar.add(getClearSettingsBookmark(c,jo, IConst.CLEAR_KILL_PROCESS, R.string.clear_kill_process, 0));
		ar.add(getClearSettingsBookmark(c,jo, IConst.CLEAR_SEARCH_HISTORY, R.string.clear_search_history, 0));
		return ar;		
	}
	// общие настройки
	public static ArrayList<Bookmark> getSettings(Context c)
	{
		ArrayList<Bookmark> ar = new ArrayList<Bookmark>();
		ar.add(new SettingsBookmark(c, null, R.string.act_select_theme,null));
// перенёс уже я в быстрые настройки и закомментил предотвращение закрытие настроек 
// в action, чтобы изменения видны были сразу
//		SettingsBookmark wwcolor =  new SettingsBookmark(c, Prefs.WEBWIEW_BACKGROUND_COLOR, R.string.act_ww_back, 
//				Prefs.getWWBackgroundColorName(c)
//				);
//		ar.add(wwcolor);
		ar.add(new SettingsBookmark(c, Prefs.GRAYICON, R.string.act_gray_pictogram, Boolean.valueOf(Prefs.isColorIcon()),R.string.yes,R.string.no, false));
		if(Payment.canBuyPro())
			ar.add(new SettingsBookmark(c, null, R.string.act_buy_pro,null));
//		ar.add(new SettingsBookmark(c, null, R.string.act_interface,null));
		ar.add(new SettingsBookmark(c, Prefs.DOWNLOAD_FOLDER, R.string.set_download_folder, DialogDownloadFile.getDefaultDir()));
		SettingsBookmark searchSystem =  new SettingsBookmark(c, Prefs.SEARCH_SYSTEM, R.string.set_search_system, 
				SearchSystem.getCurrent().getName()
				);
		ar.add(searchSystem);
		ar.add(new SettingsBookmark(c, Prefs.SEARCH_HIDE_KBD, R.string.set_search_search_hide_keyboard, Prefs.isSearchHideKeyboard(),R.string.yes,R.string.no, false));
		ObjectKeyValues<Integer, Integer> vals = new ObjectKeyValues<Integer, Integer>(Prefs.START_RESTORE_WINDOWS,R.string.act_startAppRestoreWindows,Prefs.START_APP_HOME_SCREEN,R.string.act_startAppHomeScreen,Prefs.START_APP_HOMEPAGE,R.string.act_startAppHomePage);
		ar.add(new SettingsBookmark(c, Prefs.START_APP, R.string.act_startApp,vals.getValueByKey(Prefs.getInt(Prefs.START_APP, Prefs.START_RESTORE_WINDOWS))));
		ar.add(new SettingsBookmark(c, null, R.string.main_menu_settings,null));
		ar.add(new SettingsBookmark(c, null, R.string.set_homescreen,null));
// закладки в гланом меню
//		ar.add(new SettingsBookmark(c, Prefs.SHOW_BOOKMARKS_MAIN_MENU, R.string.set_bookmarksMainMenu,Prefs.getInt(Prefs.SHOW_BOOKMARKS_MAIN_MENU, 1)==1?R.string.yes:R.string.no));
		ar.add(new SettingsBookmark(c, null, R.string.clear_on_exit,null));
		ar.add(new SettingsBookmark(c, Prefs.EXIT_CONFIRM, R.string.set_exit_confirm, Prefs.isExitConfirm(),R.string.yes,R.string.no, false));
		//ar.add(new SettingsBookmark(c, Prefs.CLOSE_LAST_PAGE_ON_TAB, R.string.close_last_tab, Prefs.isCloseLastPageOnTab(),R.string.yes,R.string.no, true));
		ar.add(new SettingsBookmark(c, Prefs.EXIT_PANEL, R.string.exit_panel, Prefs.isExitPanel(),R.string.yes,R.string.no, false));
		ar.add(new SettingsBookmark(c, Prefs.REAL_PAGES, R.string.set_real_webpages, realPagesDesc(c)));
		ar.add(new SettingsBookmark(c, Prefs.HISTORY_MINIATURE, R.string.set_history_miniature, Prefs.isHistoryMiniature(),R.string.yes,R.string.no, false));
		ar.add(new SettingsBookmark(c, null, R.string.set_cache_settings,null));
		return ar;		
	}
	public void showCacheTypeMenu(SettingsBookmark set)
	{
		showMenuTextIds(set, new OnMenuItemSelected() {
			
			@Override
			public void onMenuItemSelected(int selectedIndex, final SettingsBookmark settingsEdit, SettingsBookmark settingsSelected) {
				if(selectedIndex==1)
				{
					BookmarkActivity.runForFileDirSelect((Activity)getContext(), new OnAction() {
						
						@Override
						public void onAction(Action act) {
							File f = (File) act.param;
							settingsEdit.setTitle(f.getAbsolutePath());
							onSettingChanged(settingsEdit);
						}
					});
					return;
				}
				settingsEdit.setDesc(settingsSelected.getTitle());
				Prefs.setCacheType(Prefs.CACHE_TYPE_APP);
				onSettingChanged(settingsEdit);
			}
		}, R.string.set_cache_settings_type_app_data,R.string.set_cache_settings_path);
	}
	public static interface OnMenuItemSelected
	{
		public void onMenuItemSelected(int selectedIndex,SettingsBookmark settingsEdit,SettingsBookmark settingsSelected);
	}
	private void showCacheSettings(SettingsBookmark sb)
	{
		ArrayList<Bookmark>ar = new ArrayList<Bookmark>();
		String def;
		String ct = Prefs.getCacheType();
		if(Prefs.CACHE_TYPE_APP.equals(ct))
			def = getContext().getString(R.string.set_cache_settings_type_app_data);
		else
			def = ct;
		ar.add(new SettingsBookmark(getContext(), Prefs.CACHE_TYPE, R.string.set_cache_settings_type, def));
		//ar.add(new SettingsBookmark(mContext, Prefs.CACHE_MAX_SIZE, R.string.set_cache_settings_max_size, "1mb"));
		ar.add(new SettingsBookmark(getContext(), Prefs.CACHE_MODE, R.string.set_cache, R.string.set_cache_default));
		//ar.add(new SettingsBookmark(mContext, Prefs.CACHE_PATH, R.string.set_cache_settings_path, "1mb"));
		BookmarkActivity.runSettings(getContext(), ar,sb.getTitle());
	}
	protected void showMenu(final SettingsBookmark b,final OnMenuItemSelected listener,ArrayList<Bookmark>settingsBookmark)
	{
		showMenu(b, listener, settingsBookmark, true);
	}
	
	private void showMenu(final SettingsBookmark b,final OnMenuItemSelected listener,ArrayList<Bookmark>settingsBookmark,boolean closeMenu)
	{
		new MenuSettingBookmarks(getContext(),b.getTitle(),settingsBookmark,false,closeMenu) {
			@Override
			public void onBookmarkSelected(int pos, SettingsBookmark set) {
				if(listener!=null)
					listener.onMenuItemSelected(pos, b, set);
				onSettingChanged(set);
			}
		}.show();
	}
	public void showMenuTextIds(final SettingsBookmark b,final OnMenuItemSelected listener,Integer ... options)
	{
		ArrayList<Bookmark>ar = new ArrayList<Bookmark>();
		for(int o:options)
		{
			SettingsBookmark sb = new SettingsBookmark(getContext(), null, o, null); 
			ar.add(sb);
		}
		showMenuTextIds(b, listener, ar);
	}
	private void showMenuTextIds(final SettingsBookmark b,final OnMenuItemSelected listener,ArrayList<Bookmark> options)
	{
		showMenu(b, listener, options);
	}
	void showMainMenuSettings(SettingsBookmark b)
	{
		ArrayList<Bookmark>ar = new ArrayList<Bookmark>();
		int desc;
		desc = Prefs.getPanelLayoutType()==Prefs.TYPE_LAYOUT_NORMAL?R.string.set_main_menu_layout_normal:R.string.set_main_menu_layout_reversed;
		ar.add(new SettingsBookmark(getContext(), Prefs.SHOW_BOOKMARKS_MAIN_MENU, R.string.set_main_menu_layout,desc));
		createFromPanelSettings(getContext(), ar, MainPanel.loadNormalPanelSettings());
		BookmarkActivity.runSettings(getContext(), ar,b.getTitle());
	}
	void showStartScreenSettings(SettingsBookmark b)
	{
		ArrayList<Bookmark>ar = new ArrayList<Bookmark>();
		createFromPanelSettings(getContext(), ar, MainPanel.loadStartPanelSettings());
		BookmarkActivity.runSettings(getContext(), ar,b.getTitle());
	}
	void showPanelSettingMenu(SettingsBookmark b)
	{
		showMenuTextIds(b, new OnMenuItemSelected() {
			
			@Override
			public void onMenuItemSelected(int selectedIndex,SettingsBookmark settingsEdit, SettingsBookmark settingsSelected) {
				if(settingsSelected==null)
					return;
				PanelSetting ps = settingsEdit.getPanelSetting();
				ps.visible = selectedIndex<2;
				ps.top = selectedIndex==0;
				settingsEdit.setDesc(settingsSelected.getTitle());
				if(settingsEdit.panelSettings!=null)
					settingsEdit.panelSettings.setPanel(ps);
				onSettingChanged(settingsEdit);
			}
		}, R.string.showTop,R.string.showBottom,R.string.disabled);
	}
	static String realPagesDesc(Context c)
	{
		return new StrBuilder(c).addBrackets(stat.STR_NULL+Prefs.getRealPages()).add('\n').add(R.string.set_real_webpages_desc).toString();

	}
	public static void dialogPayVersion(Context c,boolean fromFeature)
	{
		new ThemedDialog(c).setConfirm(c.getString(R.string.act_buy_pro_desc), c, new ConfirmOper() {
			@Override
			public void onConfirm(Object userParam) {
				Payment.buyPro((MainActivity)userParam);
			}
		}.setTitle(fromFeature?R.string.pay_version_only:R.string.act_buy_pro));
	}
	
	public void onSettingClick(final SettingsBookmark b){
		if(b.param instanceof PanelSetting)
			showPanelSettingMenu(b);
		else if(HISTORY.equals(b.prefKey))
		{
			showMenuTextIds(b, new OnMenuItemSelected() {
				@Override
				public void onMenuItemSelected(int selectedIndex,SettingsBookmark settingsEdit, SettingsBookmark settingsSelected) {
					settingsEdit.setDesc(settingsSelected.getTitle());
					settingsEdit.setParam(DbClear.CLEAR_HISTORY_TYPES.getKeyByIndex(selectedIndex));
					settingsEdit.checkBox = Boolean.valueOf(true);
				}
			}, DbClear.CLEAR_HISTORY_TYPES.getValues());
		}
		else if(Prefs.CACHE_TYPE.equals(b.prefKey))
			showCacheTypeMenu(b);
		else if(Prefs.CACHE_MODE.equals(b.prefKey))
			showCacheModeMenu(b);
		else if(Prefs.REAL_PAGES.equals(b.prefKey))
		{
			if(Payment.canBuyPro())
				dialogPayVersion(getContext(), true);
			else
			{
				showMenu(b, new OnMenuItemSelected() {
					
					@Override
					public void onMenuItemSelected(int selectedIndex,SettingsBookmark settingsEdit, SettingsBookmark settingsSelected) {
						Prefs.setInt(Prefs.REAL_PAGES, (Integer)settingsSelected.param);
						settingsEdit.setDesc(realPagesDesc(getContext()));
						onSettingChanged(settingsEdit);
					}
				}, getNumberedSettingsBookmarks(Prefs.getRealPages(), 1, 2, 20));
			}
		}
		else if(b.id==R.string.act_select_theme)
		{
			if(getContext() instanceof BookmarkActivity)
				((BookmarkActivity)getContext()).finish();
			BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_ACTION, Action.create(Action.THEMES_SELECTOR));
		}
		else if(b.id==R.string.act_gray_pictogram)
		{
			//BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_ACTION, Action.create(Action.THEMES_SELECTOR));
			BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_SETTINGS_CHANGED, STRVAL_MINI_PANEL);
			if (m_c!=null)
				CustomPopup.toast(m_c, R.string.restart_app);
		}
		else if(b.id==R.string.act_buy_pro)
			dialogPayVersion(getContext(), false);
		else if(b.id==R.string.clear_on_exit)
			BookmarkActivity.runByType(getContext(), BookmarkActivity.TYPE_CLEAR_ON_EXIT);
		else if(b.id==R.string.main_menu_settings)
		{
			showMainMenuSettings(b);
		}
		else if(b.id==R.string.set_main_menu_layout)
		{
			showMenuTextIds(b, new OnMenuItemSelected() {
				
				@Override
				public void onMenuItemSelected(int selectedIndex,SettingsBookmark settingsEdit, SettingsBookmark settingsSelected) {
					settingsEdit.setDesc(settingsSelected.getTitle());
					Prefs.setInt(Prefs.MAIN_PANEL_LAYOUT, selectedIndex==0?Prefs.TYPE_LAYOUT_NORMAL:Prefs.TYPE_LAYOUT_BOTTOM);
					onSettingChanged(settingsEdit);
				}
			}, R.string.set_main_menu_layout_normal,R.string.set_main_menu_layout_reversed);
		}
		else if(b.id==R.string.set_homescreen)
		{
			showStartScreenSettings(b);
		}
		else if(b.id==R.string.act_select_theme)
		{
			if(getContext() instanceof BookmarkActivity)
			{
				((BookmarkActivity)getContext()).finish();
				BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_ACTION, Action.create(Action.THEMES_SELECTOR));
			}
		}
		else if(b.id==R.string.act_interface)
		{
			if(getContext() instanceof BookmarkActivity)
			{
				((BookmarkActivity)getContext()).finish();
				BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_ACTION, Action.create(Action.INTERFACE_SETTINGS));
			}
		}
		else if(b.id==R.string.act_startApp)
		{
			ArrayList<Bookmark>ar = new ArrayList<Bookmark>();
			ar.add(new SettingsBookmark(getContext(), null, R.string.act_startAppRestoreWindows, null).setParam(Prefs.START_RESTORE_WINDOWS));
			ar.add(new SettingsBookmark(getContext(), null, R.string.act_startAppHomeScreen, null).setParam(Prefs.START_APP_HOME_SCREEN));
			if (Prefs.get().getString(Prefs.HOME_PAGE, null)==null){
				Prefs.setString(Prefs.HOME_PAGE, MainActivity.ABOUT_BLANK);
			}

			ar.add(new SettingsBookmark(getContext(), null, R.string.act_startAppHomePage, Prefs.get().getString(Prefs.HOME_PAGE, null)).setParam(Prefs.START_APP_HOMEPAGE));
			showMenuTextIds(b, new OnMenuItemSelected() {
				
				@Override
				public void onMenuItemSelected(int selectedIndex,final SettingsBookmark settingsEdit, final SettingsBookmark settingsSelected) {
					int val = (Integer)settingsSelected.param;
					if(val==Prefs.START_APP_HOMEPAGE)
					{
						new ThemedDialog(getContext()).setInput(getContext().getString(R.string.act_startAppHomePage), Prefs.get().getString(Prefs.HOME_PAGE, null),new OnUserInput() {
							
							@Override
							public void onUserInput(boolean ok, String newText) {
								if(ok)
								{
									Prefs.setInt(Prefs.START_APP, Prefs.START_APP_HOMEPAGE);
									Prefs.setString(Prefs.HOME_PAGE, newText);
									settingsEdit.setDesc(settingsSelected.getTitle());
									onSettingChanged(settingsEdit);
								}
							}
						}).show();
					}
					else
					{
						Prefs.setInt(Prefs.START_APP, val);
						settingsEdit.setDesc(settingsSelected.getTitle());
						onSettingChanged(settingsEdit);
					}
				}
			}, ar);
		}
		else if(b.id==R.string.set_cache_settings)
			showCacheSettings(b);
		else if(b.id==R.string.set_navi_page_panel)
			st.pref_navigation=2;
		else if(b.id==R.string.set_download_folder)
			selectDownloadDir(b);
		else if(b.id==R.string.clear_data_cache
				||b.id==R.string.clear_data_history
				||b.id==R.string.clear_data_cookies
				||b.id==R.string.act_close_windows
				||b.id==R.string.set_bookmarksMainMenu
				||b.id==R.string.clear_passwords
				||b.id==R.string.clear_kill_process
				)
		{
			showMenuTextIds(b, new OnMenuItemSelected() {
				
				@Override
				public void onMenuItemSelected(int selectedIndex,SettingsBookmark settingsEdit, SettingsBookmark settingsSelected) {
					int p = selectedIndex==1?0:1;
					settingsEdit.setParam(p);
					settingsEdit.setDesc(settingsSelected.getTitle());
					if(settingsEdit.id==R.string.set_bookmarksMainMenu)
						Prefs.setInt(Prefs.SHOW_BOOKMARKS_MAIN_MENU, p);
					onSettingChanged(settingsEdit);
				}
			}, R.string.yes,R.string.no);
		}
		else if(b.id==R.string.set_search_system)
		{
			ArrayList<Bookmark>ar = new ArrayList<Bookmark>();
			for(String name:SearchSystem.getSearchSystemsNames())
			{
				ar.add(new SettingsBookmark(name, null));
			}
			MenuSettingBookmarks menu = new MenuSettingBookmarks(getContext(),b.getTitle(),ar) {
				
				@Override
				public void onBookmarkSelected(int pos,SettingsBookmark set) {
					b.setDesc(set.getTitle());
					SearchSystem.setSearchSystem(set.getTitle());
					onSettingChanged(set);
				}
			};
			menu.show();
		}
		else if(b.id==R.string.act_ww_back)
		{
			ArrayList<Bookmark>ar = new ArrayList<Bookmark>();
			String[] arname = m_c.getResources().getStringArray(R.array.ww_back_color); 
			for(String name:arname)
			{
				ar.add(new SettingsBookmark(name, null));
			}
			MenuSettingBookmarks menu = new MenuSettingBookmarks(getContext(),b.getTitle(),ar) {
				
				@Override
				public void onBookmarkSelected(int pos,SettingsBookmark set) {
					b.setDesc(set.getTitle());
					Prefs.setInt(Prefs.WEBWIEW_BACKGROUND_COLOR, pos);
					onSettingChanged(set);
					BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_ACTION, Action.create(Action.SELECT_WW_BACK_COLOR));

				}
			};
			menu.show();
		}
	}
	public void showCacheModeMenu(SettingsBookmark set)
	{
		showMenuTextIds(set, new OnMenuItemSelected() {
			
			@Override
			public void onMenuItemSelected(int selectedIndex,SettingsBookmark settingsEdit, SettingsBookmark settingsSelected) {
				settingsEdit.setDesc(settingsSelected.getTitle());
				switch (selectedIndex) {
				case 0:
					Prefs.setCacheMode(WebSettings.LOAD_DEFAULT);
					break;
				case 1:
					Prefs.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
					break;
				case 2:
					Prefs.setCacheMode(WebSettings.LOAD_CACHE_ONLY);
					break;
				}
				onSettingChanged(settingsEdit);
			}
		}, R.string.set_cache_default,R.string.set_cache_cache_then_network,R.string.set_cache_cache_only);
	}
	public void onSettingChanged(SettingsBookmark set)
	{
		if(set.id==R.string.set_main_menu_layout)
			BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_SETTINGS_CHANGED, Prefs.MAIN_PANEL_LAYOUT);
		else if(set.panelSettings !=null)
		{
			set.panelSettings.reload();
			BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_SETTINGS_CHANGED, set.panelSettings.getPrefName());
		}
		else if(!TextUtils.isEmpty(set.prefKey))
			BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_SETTINGS_CHANGED, set.prefKey);
		notifyDataSetChanged();
	}
	public void onRightButtonClick(SettingsBookmark sb)
	{
		
	}
	@Override
	public void onClick(View v) {
		if(v.getId()==R.id.favicon&&v.getTag() instanceof SettingsBookmark)
		{
			View parent = (View) v.getParent();
			while (!(parent instanceof BookmarkView)) {
				parent = (View) parent.getParent();
			}
			onCheckboxChanged((BookmarkView)parent, (SettingsBookmark)v.getTag());
		}
		if(v.getId()==R.id.close&&v.getTag() instanceof SettingsBookmark)
			onRightButtonClick((SettingsBookmark)v.getTag());
		else
		{
			SettingsBookmark sb = (SettingsBookmark)v.getTag();
			if(sb.checkBox!=null&&!sb.checkBoxAndMenuSeparate)
			{
				sb.checkBox = Boolean.valueOf(!sb.checkBox);
				setCheckbox((BookmarkView)v, sb);
				onCheckboxChanged((BookmarkView)v, sb);
				
				if (sb.tab_load){
					if (!stat.fl_loadPCmode){
						sb.checkBox=stat.fl_loadPCmode;
						onCheckboxChanged((BookmarkView)v, sb);
					}
				}
				onSettingClick(sb);
			}
			else
				onSettingClick(sb);
		}
	}
}
