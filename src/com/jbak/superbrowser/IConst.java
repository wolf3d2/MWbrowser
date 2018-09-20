package com.jbak.superbrowser;

import com.mw.superbrowser.R;

import com.jbak.superbrowser.ui.PanelButton;
import com.jbak.utils.ObjectKeyValues;
import com.jbak.utils.DbUtils.StrConst;

public abstract interface IConst extends StrConst{
	public static final String PACKAGE_MWCOSTS = "com.mwcorp.costs";
	public static final String PACKAGE_MWSHARE2SAVE = "com.mw.share2save";
	public static final String PACKAGE_JBAK2KEYBOARD = "com.jbak2.JbakKeyboard";
	public static final String TEAM_4PDA= "https://4pda.ru/forum/index.php?showtopic=796215";
	public static final String STR_COMMENT = "//";
	public static final String STR_SPACE = " ";
	public static final String STR_NULL = "";
	public static final String STR_CR = "\n";
	public static final String STR_FILE = "file:///";
	public static final String NAVIGATION_BACK = " ◁ ";
	public static final String NAVIGATION_FORWARD = " ▷ ";

//	public static final String APP_NAME = "MWbrowser";
	public static final String UTF8 = "UTF-8";
	public static final String HTTP = "http://";
	public static final String HTTPS = "https://";
	public static final String URL = "url";
	public static final String ORIGINAL_URL = "orig_url";
	public static final String TITLE = "title";
	public static final String HISTORY = "history";
	public static final String FILEPATH = "filepath";
	
	public static final String BITMAP = "bitmap";
	public static final String DATE = "date";
	public static final String MODIFIED = "modified";
	public static final String TYPE = "type";
	public static final String WINDOW_ID = "window_id";
	public static final String CUR_POS = "curPos";
	public static final String VIEW_TYPE = "viewType";
	public static final String THUMBNAIL = "thumbnail";
	public static final String FAVICON = "favicon";
	public static final String VISITS = "visits";
	public static final String EXTRA = "extra";
	public static final String PARENT = "parent";
	public static final String SETTINGS = "settings";
	public static final String SEARCH = "search";
	public static final String SEARCH_ACTION = "searchAction";
	public static final String CURRENT_PAGE = "currentPage";
	public static final String CLOSED_DATE = "closed_date";
	public static final String WEB_VIEW_BUNDLE = "webViewBundle";
	public static final String BOOKMARK = "bookmark";
	public static final String URLS = "urls";
	public static final String NAME = "name";
	public static final String VALUE = "value";
	public static final String MARKET_SCHEME = "market";
	public static final String INTENT_SCHEME = "intent";
	public static final String WS_IMAGES_ENABLED = "images";
	public static final String WS_PREVIEW_ENABLED = "preview";
	public static final String WS_TEXT_SIZE = "textSize";
	public static final String IMAGE = "image";
	public static final int CODE_WINDOW_ID = 1023;
	public static final int CODE_GET_BOOKMARK = 1024;
	public static final int CODE_GET_BOOKMARK_POS = 1025;
	public static final int CODE_FILE_UPLOAD_REQUEST = 1026;
	public static final int CODE_VOICE_RECOGNIZER = 1027;
	public static final int CODE_BUY_PRO = 1028;
	public static final String CUR_WINDOW_ID = "currentWindow";
	public static final String LAST_FILES = "lastFiles";
	public static final String LAST_DIRS = "lastDirs";
	public static final long ROOT_FOLDER_ID = 1;
	public static final String FOLDER = "folder";
	public static final String COOKIES = "cookies";
	public static final String CACHE = "cache";
	public static final String CLEAR_CLOSE_WINDOWS = "closeWindows";
	public static final String CLEAR_SAVED_PASSWORDS = "savedPasswords";
	public static final String CLEAR_KILL_PROCESS = "killProcess";
	public static final String CLEAR_SEARCH_HISTORY = "searchHistory";
	public static final String STRVAL_WINDOWS_IDS = "windowsIds";
	public static final String STRVAL_MINI_PANEL = "toolPanel";
	public static final String STRVAL_MAINMENU_PANEL = "mainmenuPanel";
	public static final String VISIBLE = "visible";
	public static final String TOP = "top";
	public static final String MIN_TAB_WIDTH = "minTabWidth";
	public static final String MAX_TAB_WIDTH = "maxTabWidth";
	public static final String MAX_TAB_ROWS = "maxTabRows";
	public static final String EXTRA_SETTINGS = "extraSettings";
	public static final String PANEL_WINDOWS = "panelWindows";
	public static final String PLUS_BUTTON = "plusButton";
	public static final String CLEAR_TYPE = "clearType";
	public static final String BUTTON_TYPE = "buttonType";
	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	public static final int DISABLED = 2;
	public static final int WINDOW_OPEN_SAME = 0;
	
	public static final long HOUR_MILLIS = 1000*60*60;
	public static final long DAY_MILLIS = HOUR_MILLIS*24;
	public static final long WEEK_MILLIS = 7*DAY_MILLIS;
	public static final long MONTH_MILLIS = 30*DAY_MILLIS;
	
	public static final int BUTTONS_SMALL = PanelButton.TYPE_BUTTON_SMALL;
	public static final int BUTTONS_MEDIUM= PanelButton.TYPE_BUTTON_MEDIUM;
	public static final int BUTTONS_BIG = PanelButton.TYPE_BUTTON_NORMAL;
	public static ObjectKeyValues<Integer, Integer> BUTTON_TYPES = new ObjectKeyValues<Integer, Integer>
	(
			BUTTONS_BIG,R.string.buttons_big,
			BUTTONS_MEDIUM,R.string.buttons_medium,
			BUTTONS_SMALL,R.string.buttons_small
	);
	public static final String HISTORY_PROJECTION[] = new String[]{_ID,TITLE,URL,DATE};


}
