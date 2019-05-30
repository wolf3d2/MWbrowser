package com.jbak.superbrowser;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.webkit.WebSettings;
import android.webkit.WebSettings.TextSize;
import ru.mail.mailnews.st;

import com.jbak.utils.ObjectKeyValues;
import com.jbak.utils.Utils;
import com.mw.superbrowser.R;

public class Prefs 
{
	/**  ключ, самый первый запуск браузера*/
	public static final String FIRST_START_BROWSER = "first_start_browser";
	/**  ключ, показ панели в две колонки по выбору системы*/
	public static final String SET_TWO_COLUMN = "set_two_column";
	// переменные
	public static String translate_lng = st.STR_NULL;
	public static String NAME_SETTING = "settings";
// ключи для экспорта
	public static final String EXPORT_SAVE_CHANGE= "export_save_change";
	public static final String BACKUP_SETTING_FOLDER= "backup_setting_folder";
// текущая кодировка
	public static final String SHOW_KBD= "show_kbd";
	public static final String SHOW_KBD_DIALOG_EDITOR= "show_kbd_dialog_editor";
// текущая кодировка
	public static final String codepage = IConst.UTF8;
// увеличенная высота вкладок
	public static final String TABS_HEIGTH = "tabs_heigth";
	// наборы расположения кнопок в суперменю
	public static final String SUPERMENU_BUTTON_SET= "supermenu_button_set";
	/** Закрывать ли текущую вкладку, если открытых страниц больше нет в этой вкладке */
	public static final String CLOSE_CURRENT_LAST_TAB= "close__current_last_tab";
	public static final String CLOSE_LAST_PAGE_ON_TAB= "close_last_page_on_tab";
	public static final String GRAYICON = "graycolor_pictogram";
	public static final String FULLSCREEN = "fullscreen";
	public static final String VOLUME_KEYS_STATE = "volume_keys_state";
	public static final String MENU_KEY_CONFIRMED = "menu_key_confirmed";
	public static final String INTERFACE = "interface";
	public static final String MAGIC_BUTTON_VISIBLE = "magic_button_visible";
	public static final String MAGIC_BUTTON_POS = "magic_button_pos";
	public static final String MAGIC_KEY_ALPHA = "magic_key_alpha";
	public static final String NAVIGATION_PANEL_VISIBLE = "navigation_panel_visible";
	public static final String NAVIGATION_PANEL_POS = "navigation_panel_pos";
	public static final String NAVIGATION_PANEL_ALPHA = "navigation_panel_alpha";
	public static final String NAVIGATION_PANEL_SIZE = "navigation_panel_size";
	public static final String NAVIGATION_PANEL_COLOR = "navigation_panel_color";
	public static final String WEBWIEW_BACKGROUND_COLOR = "ww_back_color";
	public static final String LONGCLICK = "longclick";
	public static final String THEME = "theme";
	public static final String START_APP = "startApp";
	public static final String HOMESCREEN_FOLDER = "homescreenFolder";
	public static final String MIN_FONT = "minFont";
	public static final String FONT_SCALE = "fontScale";
	public static final String DOWNLOAD_FOLDER = "downloadFolder";
	public static final String BOOKMARK_STORAGE = "bookmarkStorage";
// выходить сразу или открывать панель выхода	
	public static final String EXIT_PANEL = "exit_panel";
	// сохранять миниатюру страницы для истории
	public static final String HISTORY_MINIATURE = "history_miniature";
	public static final String SEARCH_SYSTEM = "searchSystem";
	public static final String SEARCH_HIDE_KBD = "search_hide_kbd_after_enter";
	public static final String CLEAR_SETTINGS = "clearSettings";
	public static final String SHOW_BOOKMARKS_MAIN_MENU = "showBookmarksMainMenu";
	public static final String CLEAR_EXIT = "clearExit";
	public static final String MAIN_PANEL_LAYOUT = "mainPanelLayout";
	public static final String CACHE_MODE = "cacheMode";
	public static final String CACHE_TYPE = "cacheStor";
	public static final String CACHE_TYPE_APP = "appPath";
	public static final String CACHE_MAX_SIZE = "cacheMaxSize";
	public static final String HOME_PAGE = "homePage";
	public static final String PANEL_SETTINGS = "panelSettings";
	public static final String PANEL_SETTINGS_MAIN_MENU = "panelSettingsMainMenu";
	public static final String PANEL_SETTINGS_START = "panelSettingsStart";
	public static final String SET_NAVIGATION = "setNavigation";
	public static final String EXTENDED_PROGRESS = "extendedProgress";
	public static final String MAGIC_BUTTON_LONG_PRESS_START = "MAGIC_BUTTON_LONG_PRESS_START";
	public static final String EXIT_CONFIRM = "exitConfirm";
	public static final String HISTORY_CONVERTED = "historyConverted";
	public static final String HISTORY_TEST = "historyTest";
	public static final String REAL_PAGES = "realWebPages";

	
	public static final int INTERFACE_NONE = 0;
	public static final int INTERFACE_MAGIC_BUTTON = 1;
	public static final int INTERFACE_PANEL = 2;

	public static final int VOLUME_KEYS_NONE = 0;
	public static final int VOLUME_KEYS_SCROLL = 1;
	public static final int VOLUME_KEYS_FONT_RESIZE = 2;
	
	public static final int LONGCLICK_DEFAULT = 0;
	public static final int LONGCLICK_TEXT_SELECTION = 1;
	public static final int LONGCLICK_CONTEXT_MENU = 3;

	public static final int START_RESTORE_WINDOWS = 0;
	public static final int START_APP_HOME_SCREEN = 1;
	public static final int START_APP_HOMEPAGE = 2;

	public static final int BOOKMARK_STORAGE_UNDEFINED = 0;
	public static final int BOOKMARK_STORAGE_SYSTEM = 1;
	public static final int BOOKMARK_STORAGE_OWN = 2;
	
	public static final int TYPE_LAYOUT_NORMAL = 0;
	public static final int TYPE_LAYOUT_BOTTOM = 1;


	private static Prefs INSTANCE;
	SharedPreferences mPrefs;
	int mVolumeKeysState = VOLUME_KEYS_NONE;
	public static void init(Context c)
	{
		INSTANCE = new Prefs();
		INSTANCE.mPrefs = c.getSharedPreferences(NAME_SETTING, Context.MODE_PRIVATE);
		INSTANCE.mVolumeKeysState = INSTANCE.mPrefs.getInt(VOLUME_KEYS_STATE, VOLUME_KEYS_NONE);
	}
	public static boolean getFullscreen()
	{
		return INSTANCE.mPrefs.getBoolean(FULLSCREEN, false);
	}
	public static void setFullScreen(boolean fullscreen)
	{
		INSTANCE.mPrefs.edit().putBoolean(FULLSCREEN, fullscreen).commit();
	}
	public static boolean getUseColorBackgrounds(boolean use)
	{
		return false;
	}
	public static int getVolumeKeysState()
	{
		return INSTANCE.mVolumeKeysState;
	}
	public static void setVolumeKeysState(int state)
	{
		INSTANCE.mVolumeKeysState = state;
		INSTANCE.mPrefs.edit().putInt(VOLUME_KEYS_STATE, state).commit();
	}
	public static int getInterface()
	{
		return INSTANCE.mPrefs.getInt(INTERFACE, INTERFACE_MAGIC_BUTTON);
	}
	public static void setInterface(int iface)
	{
		INSTANCE.mPrefs.edit().putInt(INTERFACE, iface).commit();
	}
	public static int getMagicButtonPos()
	{
		return INSTANCE.mPrefs.getInt(MAGIC_BUTTON_POS, 0);
	}
	public static void setMagicButtonPos(int pos)
	{
		INSTANCE.mPrefs.edit().putInt(MAGIC_BUTTON_POS, pos).commit();
	}
	public static int getNavigationPanelPos()
	{
		return INSTANCE.mPrefs.getInt(NAVIGATION_PANEL_POS, 2);
	}
	public static int getWWBackgroundColorPref()
	{
		return INSTANCE.mPrefs.getInt(WEBWIEW_BACKGROUND_COLOR, 0);
	}
	public static String getWWBackgroundColorName(Context c)
	{
		int col = getWWBackgroundColorPref();
		String[] ar = c.getResources().getStringArray(R.array.ww_back_color);
		return ar[col];
	}
	/** при изменении не забыть откорректировать метод 
	 * getDefaultTheme в MainActivity (вроде уже не надо) */
	public static int getWWBackgroundColor()
	{
		switch (getWWBackgroundColorPref())
		{
		case 0: return Color.WHITE;
		case 1: return Color.BLACK;
		case 2: return Color.BLUE;
		case 3: return Color.GRAY;
		case 4: return Color.DKGRAY;
		case 5: return Color.GREEN;
		case 6: return 0xff006400;
		case 7: return Color.RED;
		case 8: return 0xff000080;
		case 9: return 0xff800080;
		case 10: return 0xff00FFFF;
		case 11: return 0xffFF8C00;
		case 12: return 0xffFF7F50;
		case 13: return 0xffD3D3D3;
		case 14: return 0xff7FFF00;
		case 15: return 0xff00FA9A;
		case 16: return 0xffFFD700;
		case 17: return 0xff191970;
		case 18: return 0xffBC8F8F;
		case 19: return 0xff8B4513;
		case 20: return 0xff2F4F4F;
		default:return Color.WHITE;
		
		}
	}
	// 0 - светлая тема
	// 1 - тёмная
    public static int getWWDarkWhiteTheme()
    {
    	switch (Prefs.getWWBackgroundColorPref())
    	{
    	case 1:
    	case 3:
    	case 4:
    	case 6:
    	case 7:
    	case 8:
    	case 9:
    		return 1;
    	}
		return 0;
    }
	public static void setNavigationPanelPos(int pos)
	{
		INSTANCE.mPrefs.edit().putInt(NAVIGATION_PANEL_POS, pos).commit();
	}
	public static int getLongClick()
	{
		return INSTANCE.mPrefs.getInt(LONGCLICK, LONGCLICK_DEFAULT);
	}
	public static final void setLongClick(int longclick)
	{
		INSTANCE.mPrefs.edit().putInt(LONGCLICK, longclick).commit();
	}
	public static final int getCacheMode()
	{
		return INSTANCE.mPrefs.getInt(CACHE_MODE, WebSettings.LOAD_DEFAULT);
	}
	public static final void setCacheMode(int cacheMode)
	{
		INSTANCE.mPrefs.edit().putInt(CACHE_MODE, cacheMode).commit();
	}
	public static String getTheme()
	{
		return INSTANCE.mPrefs.getString(THEME, null);
	}
	public static void setTheme(String theme)
	{
		INSTANCE.mPrefs.edit().putString(THEME, theme).commit();
	}
	public static int getStartApp()
	{
// старое значение		
//		return INSTANCE.mPrefs.getInt(START_APP, START_RESTORE_WINDOWS);
		return INSTANCE.mPrefs.getInt(START_APP, START_APP_HOMEPAGE);
	}
	public static void setStartApp(int startApp)
	{
		INSTANCE.mPrefs.edit().putInt(START_APP, startApp).commit();
	}
	public static SharedPreferences get()
	{
		return INSTANCE.mPrefs;
	}
	public static void setInt(String key,int value)
	{
		INSTANCE.mPrefs.edit().putInt(key, value).commit();
	}
	public static void setString(String key,String value)
	{
		INSTANCE.mPrefs.edit().putString(key, value).commit();
	}
	public static void setBoolean(String key,boolean value)
	{
		INSTANCE.mPrefs.edit().putBoolean(key, value).commit();
	}
	public static final int getInt(String key,int fallback)
	{
		return get().getInt(key, fallback);
	}
	public static final String getString(String key,String fallback)
	{
		return get().getString(key, fallback);
	}
	@SuppressWarnings("deprecation")
	public static ObjectKeyValues<Integer, WebSettings.TextSize> OLD_SIZES = new ObjectKeyValues<Integer, WebSettings.TextSize>
				(50,TextSize.SMALLEST,75,TextSize.SMALLER,100,TextSize.NORMAL,150,TextSize.LARGER,200,TextSize.LARGEST);
	public static Integer[] getFontScales()
	{
		if(Build.VERSION.SDK_INT>=14)
			return new Integer[]{-10,+10,50,60,70,80,90,100,110,120,130,140,150,160,170,200};
		else
			return OLD_SIZES.getKeys();
	}
	public static JSONObject getJSONObject(String key)
	{
		try{
			String js = Prefs.get().getString(key, null);
			if(js!=null)
				return new JSONObject(js);
		}
		catch(Throwable e)
		{
			Utils.log(e);
		}
		return new JSONObject();
	}
	public static int getPanelLayoutType()
	{
		return Prefs.getInt(MAIN_PANEL_LAYOUT, TYPE_LAYOUT_BOTTOM);
	}
	public static final String getHome()
	{
		return Prefs.getString(HOME_PAGE, null);
	}
	public static final boolean isExtendedProgress()
	{
		return Prefs.getBoolean(EXTENDED_PROGRESS, false);
	}
	// способ навигации на странице
	public static final int getNavigationMethod()
	{
		return Prefs.getInt(SET_NAVIGATION, 2);
	}
	public static final String getNavigationName(Context c)
	{
		switch (getNavigationMethod())
		{
		case 0: return c.getString(R.string.disabled);
		case 1: return c.getString(R.string.set_navi_page_panel);
		case 2: return c.getString(R.string.set_navi_gest);
		
		}
		return st.STR_NULL;
	}
	public static final boolean getBoolean(String key, boolean def) {
		return get().getBoolean(key, def);
	}
	public static final boolean isExitConfirm()
	{
		return Prefs.getBoolean(Prefs.EXIT_CONFIRM,false);
	}
	public static final boolean isCloseLastPageOnTab()
	{
		return Prefs.getBoolean(Prefs.CLOSE_LAST_PAGE_ON_TAB,true);
	}
	public static final boolean isCloseCurrentLastTab()
	{
		return Prefs.getBoolean(Prefs.CLOSE_CURRENT_LAST_TAB,true);
	}
	public static final boolean isTabsHeight()
	{
		return Prefs.getBoolean(Prefs.TABS_HEIGTH,false);
	}
	public static final boolean isUpdateSaveSettingAndBookmark()
	{
		return Prefs.getBoolean(Prefs.EXPORT_SAVE_CHANGE,false);
	}
	public static final boolean isHistoryMiniature()
	{
		return Prefs.getBoolean(Prefs.HISTORY_MINIATURE,false);
	}
	public static final boolean isExitPanel()
	{
		return Prefs.getBoolean(Prefs.EXIT_PANEL,false);
	}
	public static final boolean isSearchHideKeyboard()
	{
		return Prefs.getBoolean(Prefs.SEARCH_HIDE_KBD,false);
	}
	public static final void setColorPictogram(boolean val)
	{
		Prefs.setBoolean(GRAYICON, val);
	}
	public static final boolean isColorIcon()
	{
		return Prefs.getBoolean(Prefs.GRAYICON,false);
	}
	public static final void setExitConfirm(boolean val)
	{
		Prefs.setBoolean(EXIT_CONFIRM, val);
	}
	public static final boolean isMagicKeyVisible()
	{
		return Prefs.getBoolean(Prefs.MAGIC_BUTTON_VISIBLE,true);
	}
	public static final boolean isNavigationPanelVisible()
	{
		return Prefs.getBoolean(Prefs.NAVIGATION_PANEL_VISIBLE,false);
	}
	public static final String getCacheType()
	{
		return Prefs.getString(CACHE_TYPE, CACHE_TYPE_APP);
	}
	public static final void setCacheType(String cacheType)
	{
		Prefs.setString(CACHE_TYPE, cacheType);
	}
// количество реальных веб страниц	
	public static final int getRealPages()
	{
//		if(!Payment.isPro())
//			return 5;
		return Prefs.getInt(REAL_PAGES, 5);
	}
	public static final boolean getBookmarkSiteLogoIcon(boolean def) {
		return Prefs.getBoolean(HISTORY_MINIATURE, def);
	}
	public static void getBookmarkSiteLogoIcon(String key, boolean val) {
		INSTANCE.mPrefs.edit().putBoolean(HISTORY_MINIATURE, val).commit();
	}
	/** показывать списки в одну колонку или по выбору системы */
	public static final boolean isTwoColumn()
	{
		return Prefs.getBoolean(Prefs.SET_TWO_COLUMN,true);
	}
	public static final void setTwoColumn(boolean val)
	{
		Prefs.setBoolean(SET_TWO_COLUMN, val);
	}
}
