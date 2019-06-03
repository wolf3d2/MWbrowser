package com.jbak.superbrowser;

import com.mw.superbrowser.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import org.json.JSONObject;

import ru.mail.mailnews.st;
import ru.mail.mailnews.st.UniObserver;
import ru.mail.webimage.FileUtils;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.EditorInfo;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.WebIconDatabase;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebSettings.TextSize;
import android.webkit.WebView;
import android.webkit.WebView.FindListener;
import android.webkit.WebViewClient;
import android.webkit.WebViewDatabase;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.jbak.superbrowser.stat.DownloadOptions;
import com.jbak.superbrowser.stat.FileUploadInfo;
import com.jbak.superbrowser.UrlProcess.DownloadFileInfo;
import com.jbak.superbrowser.WebViewEvent.EventInfo;
import com.jbak.superbrowser.noobfuscate.JavaScriptProcessor;
import com.jbak.superbrowser.panels.InterfaceSettingsLayout;
import com.jbak.superbrowser.panels.PanelUrlEdit;
import com.jbak.superbrowser.panels.PanelWindows;
import com.jbak.superbrowser.pluginapi.PluginUtils;
import com.jbak.superbrowser.search.SearchSystem;
import com.jbak.superbrowser.ui.BookmarkView;
import com.jbak.superbrowser.ui.ErrorLayout;
import com.jbak.superbrowser.ui.MainPanel;
import com.jbak.superbrowser.ui.MenuPanelButton.MenuBookmark;
import com.jbak.superbrowser.ui.MyWebView;
import com.jbak.superbrowser.ui.PanelButton;
import com.jbak.superbrowser.ui.PanelLayout;
import com.jbak.superbrowser.ui.TextProgressBar;
import com.jbak.superbrowser.ui.WebViewContextMenu;
import com.jbak.superbrowser.ui.dialogs.DialogEditor;
import com.jbak.superbrowser.ui.dialogs.ThemedDialog;
import com.jbak.superbrowser.ui.dialogs.WindowLayout;
import com.jbak.superbrowser.ui.themes.MyTheme;
import com.jbak.superbrowser.utils.DbClear;
import com.jbak.superbrowser.utils.TempCookieStorage;
import com.jbak.ui.ConfirmOper;
import com.jbak.ui.CustomDialog;
import com.jbak.ui.CustomDialog.OnUserInput;
import com.jbak.ui.CustomPopup;
import com.jbak.ui.UIUtils;
import com.jbak.utils.GlobalHandler;
import com.jbak.utils.IniFile;
import com.jbak.utils.SameThreadTimer;
import com.jbak.utils.StrBuilder;
import com.jbak.utils.Utils;
import com.jbak.utils.WeakRefArray;

@SuppressLint("NewApi")
@SuppressWarnings("deprecation")
public class MainActivity extends Activity implements OnClickListener,OnLongClickListener,OnItemLongClickListener,IConst,BrowserApp.OnGlobalEventListener,GlobalHandler 
{
	// для снижения жора батарейки, обработка ведётся по таймеру
	public static long addOnGlobalLayoutTimer = 0;
	public static long addOnGlobalLayoutTimerTemp = 0;
	// период в мс через, какой промежуток делать повторную обработку
	public static long EVENT_TIME_PERIOD = 200;
	// текущая тема -
	// 0 - светлая
	// 1 - тёмная
	int cur_theme = 0;
	// последние значения для кнопок back и forward панели навигации
	boolean last_tab_back= false;
	boolean last_tab_forward = false;
	public static final int WHAT_UPDATE_PROGRESS = 400;
	public static final int WHAT_RELOAD_FROM_CACHE = 20;
	MyWebView mWebView;
	View mButtonViewType;
	View mMagicButton;
	View mNavigationPanel;
	private TextProgressBar mLoadProgress;
	ImageView mFavIcon;
	public Button round_btn;
	public SeekBar sb_minfont;
	public static boolean bshowSeekbarMinFontSize = false;
	ViewGroup mSearchPage;
	EditText mSearchPageText;
	TextView mSearchPageTitle;
	TabList mTabList;
	ViewGroup mTopContainer;
	FileUploadInfo mUploadInfo;
	ViewGroup mWebViewFrame;
	ViewGroup mTempWebViewFrame;
	MainPanel mPanel;
	public static MainActivity activeInstance;
	WeakRefArray<WebViewEvent> mWebViewListeners = new WeakRefArray<WebViewEvent>();
	RelativeLayout mMagicButtonLayout;
	RelativeLayout mNvigationPanelLayout;
	WebTimer mTimer;
	public static final int UI_WEB = 0;
	public static final int UI_PANEL = 1;
	public static final int UI_SEARCH_ON_PAGE = 2;

	int mUiState = UI_WEB;
	boolean mUrlTitleSingleLine = true;
	Bitmap mIcon;
	ImageView mClearTextButton;
	FrameLayout mCustomLayoutFrame;
	ProgressBar mVideoProgressBar;
	
	JavaScriptProcessor mJsProcessor;
	MyChromeClient mChromeClient = new MyChromeClient(this);
	WebViewClient mClient = new MyWebViewClient(this);
	boolean mIsSoftKeyboradVisible = false;
	boolean mIsFullscreen = false;
	ViewGroup mMainPanelContainer;
	PanelLayout mPanelLayout;
	ViewGroup mErrorLayout;
	static WeakRefArray<MainActivity> mInstances = new WeakRefArray<MainActivity>();
	boolean mTempFocusChange = false;

//    View.OnKeyListener search_onKeyListener = new View.OnKeyListener() {
//		
//		@Override
//		public boolean onKey(View v, int keyCode, KeyEvent event) 
//		{
//    	    if(event.getAction() == KeyEvent.ACTION_DOWN && 
//    	    	(keyCode == KeyEvent.KEYCODE_ENTER))
//       		{
//    			runById(R.id.search_down);
//    			if (Prefs.isSearchHideKeyboard())
//    					st.hideEditKeyboard( (EditText) v);
//    	    	return true;
//   			}
//       		return false;
//		}
//	};

	@SuppressLint({ "NewApi", "InflateParams" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setMainTheme(this);
		super.onCreate(savedInstanceState);
		mInstances.add(this);
		Payment.check();
//		 вызываекм падение чтоб сработал отчёт об ошибке
//		 int bbb = Integer.valueOf("huk");
		st.pref_navigation = Prefs.getNavigationMethod();
		boolean tempSession =  getIntent().getData() instanceof Uri;
		mTimer = new WebTimer(this);
		mTabList = new TabList(this,tempSession?TabList.lastTempSession:0,false);
		addWebViewListener(mTabList);
		mJsProcessor = new JavaScriptProcessor(this);
		WebIconDatabase.getInstance().open(getDir("favicons", MODE_PRIVATE).getPath());
		Db.getWindowTable().clearClosedWindows();
		ViewGroup vg = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.activity_main, null);
		mTopContainer = vg;
		mPanelLayout = new PanelLayout(this);
		setFullscreen(Prefs.getFullscreen(),true);
		setContentView(vg);
		mTempWebViewFrame = (ViewGroup)findViewById(R.id.tempWebViewFrame);
		mMagicButtonLayout = (RelativeLayout) findViewById(R.id.magicButtonLayout);
		mNvigationPanelLayout = (RelativeLayout) findViewById(R.id.navigationLayout);
		round_btn = (Button) findViewById(R.id.round_button);
		round_btn.setVisibility(View.GONE);
		round_btn.setOnClickListener(mMyButtonClk);
		
		sb_minfont = (SeekBar) findViewById(R.id.sb_minfont);
		sb_minfont.setVisibility(View.GONE);
		sb_minfont.setOnSeekBarChangeListener(m_seekbarCngListener);
		bshowSeekbarMinFontSize = false;
		sb_minfont.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent me) {
				switch (me.getAction())
				{
				case MotionEvent.ACTION_DOWN:
					bshowSeekbarMinFontSize = true;
					break;
				case MotionEvent.ACTION_UP:
					bshowSeekbarMinFontSize = false;
					break;
				}
				return false;
			}
		});
		mWebViewFrame = (ViewGroup)findViewById(R.id.webViewFrame);
		mErrorLayout = (ViewGroup)findViewById(R.id.loadErrorFrame);
		mMainPanelContainer = (ViewGroup)findViewById(R.id.main_panel_container);
		mPanel = new MainPanel(this, Prefs.getPanelLayoutType());
		addWebViewListener(mPanel);
		mMainPanelContainer.addView(mPanel);
//		mButtonContainer.setBackgroundResource(R.drawable.background);
		mLoadProgress = (TextProgressBar) findViewById(R.id.progressLoad);
		setLoadProgressColor();
		setProgressType();
		mFavIcon = (ImageView)findViewById(R.id.favicon);
		mSearchPage = (ViewGroup)findViewById(R.id.searchPage);
		mSearchPageText = (EditText)findViewById(R.id.searchText);
		if (cur_theme == 0)
			mSearchPageText.setTextColor(Color.BLACK);
		else{
			mSearchPageText.setTextColor(Color.WHITE);
			mSearchPage.setBackgroundColor(Color.DKGRAY);
		}
//		mSearchPageText.setBackgroundColor(Color.WHITE);
//		mSearchPageText.setTextColor(Color.BLACK);
		mSearchPageText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView tv, int action, KeyEvent tvent) {
				  if (action == EditorInfo.IME_ACTION_SEARCH) {
		    			runById(R.id.search_down);
		    			if (Prefs.isSearchHideKeyboard())
		    					st.hideEditKeyboard( (EditText) tv);
		    	    	return true;
				  }
	       		return false;
			}
		});

		//mSearchPageText.setOnKeyListener(search_onKeyListener);
		mSearchPageTitle = (TextView)findViewById(R.id.searchPage_title);
		mCustomLayoutFrame = (FrameLayout)findViewById(R.id.customLayoutFrame);

		mSearchPageText.addTextChangedListener(mSearchPageTextWatcher);
		mNavigationPanel = findViewById(R.id.navigationLayout);
		setNavigationPanel();
		last_tab_back = true;
		last_tab_forward = true;
//		mNavigationPanel.setOnLongClickListener(this);
		int pos = Prefs.getNavigationPanelPos();
		setNavigationPanelPos(pos);
		mMagicButton = findViewById(R.id.magicButton);
		setMagicButtonAlpha();
//		mMagicButton.setAlpha(Prefs.get().getInt(Prefs.MAGIC_KEY_ALPHA, 100)/100f);
		mButtonViewType = findViewById(R.id.viewType);
		BrowserApp.INSTANCE.addGlobalListener(this);
		mMagicButton.setOnLongClickListener(this);
		int pos1 = Prefs.getMagicButtonPos();
		setMagicButtonPos(pos1);
		setOnClickRecurce(vg);
//		showPanel(false);
		mWebView = new MyWebView(this);
		mWebView.setBackgroundColor(Color.TRANSPARENT);
		mWebViewFrame.addView(mWebView);
		// первый запуск браузера
		boolean bool = false;
		if(!openViewIntent(getIntent()))
		{
			bool = firstStartBrowser();
			if (bool) {
				setDefaultSetting();
				Prefs.setBoolean(Prefs.FIRST_START_BROWSER, false);
			}
			tabFirstStart();
		}
		onThemeChanged(MyTheme.get());
		if (bool) {
        	new ThemedDialog(this).setConfirm(this.getString(R.string.first_start_msg), null, new ConfirmOper()
        	{
				
				@Override
				public void onConfirm(Object userParam) {
					BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_ACTION, Action.create(Action.HELP));

				}
			});
		}
		else if (isNewVersion(this)) {
			st.dialogHelp(this, getWhatsNew(), this.getString(R.string.act_whatsnew));
		}

		//addOnGlobalLayoutTimer
		
		// !!!!!! постоянно вызывается и жрёт батарейку
		// чтобы снизить нагрузку, я сделал обработку по таймеру
		mTopContainer.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				addOnGlobalLayoutTimerTemp = new Date().getTime();
				if (addOnGlobalLayoutTimer >
						(addOnGlobalLayoutTimerTemp+EVENT_TIME_PERIOD))
					return;
				addOnGlobalLayoutTimer = new Date().getTime();
				Rect r = new Rect();
				mTopContainer.getWindowVisibleDisplayFrame(r);
				int hd = r.height();
				mTopContainer.getGlobalVisibleRect(r);
				int hr = mTopContainer.getRootView().getHeight();
				if(hr-hd>st.dp2px(MainActivity.this, 40))
					onSoftKeyboardVisible(true);
				else
					onSoftKeyboardVisible(false);
			}
		});
	}
/** записываем настройки по умолчанию */	
	void setDefaultSetting()
	{
		Prefs.setInt(Prefs.START_APP, Prefs.START_APP_HOMEPAGE);
		Prefs.setString(Prefs.HOME_PAGE, IConst.ABOUT_BLANK);
		
	}
	/** самое первое открытие браузера */
	boolean firstStartBrowser()
	{
		boolean ret = Prefs.getBoolean(Prefs.FIRST_START_BROWSER, true);
		return Prefs.getBoolean(Prefs.FIRST_START_BROWSER, true);
	}
	boolean isNewVersion(Context c)
	{
		try {
			boolean newvers = false;
			IniFile ini = new IniFile(c);
			String path = c.getFilesDir().toString()+ st.STR_SLASH;
			ini.setFilename(path + ini.PAR_INI);
			if (!ini.isFileExist()) {
				ini.create(path, ini.PAR_INI);
				newvers = false;
			}
			if (!ini.isFileExist())
				return false;
			String codever = st.getAppVersionCode(c);
			String param = ini.getParamValue(ini.VERSION_CODE);
			if (param == null) {
				ini.setParam(ini.VERSION_CODE, codever);
				return false;
			}
			if (newvers||codever.compareToIgnoreCase(param) != 0) {
				ini.setParam(ini.VERSION_CODE, codever);
				return true;
			} 
		} catch (Throwable e) {
		}

		return false;
	}
    /** читает diary_browser.txt*/
    public String getWhatsNew()
    {
        byte[] buffer = null;
        InputStream is;
        try {
            is = getAssets().open("whatsnew_browser.txt");
            int size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();
        } catch (IOException e) {
//            e.printStackTrace();
        }
        if (buffer!=null) {
            return  new String(buffer);
        }
   		return null;
    }
	void onSoftKeyboardVisible(boolean visible)
	{
		if(mIsSoftKeyboradVisible!=visible)
		{
			mIsSoftKeyboradVisible = visible;
			if(mIsFullscreen&&visible)
				setFullscreen(false, false);
			else if(!mIsFullscreen&&Prefs.getFullscreen()&&!visible)
				setFullscreen(true, false);
			sendWebViewEvent(visible?WebViewEvent.WWEVENT_SOFT_KEYBOARD_VISIBLE:WebViewEvent.WWEVENT_SOFT_KEYBOARD_HIDDEN, mWebView, null, null);
			//mPanel.onSoftKeyboard(visible);
		}
	}
	TextWatcher mSearchPageTextWatcher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,int after) {
			
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			if(mSearchPage.getVisibility()==View.VISIBLE)
			searchOnPage(s.toString());
		}
	};
	@SuppressLint("NewApi")
	void searchOnPage(String s)
	{
		
		if(VERSION.SDK_INT>=16)
			mWebView.findAllAsync(s);
		else
		{
			int f = mWebView.findAll(s);
		}
	}
	public final TextProgressBar getLoadProgress()
	{
		return mLoadProgress;
	}
	void setOnClickRecurce(ViewGroup vg)
	{
		for(int i = vg.getChildCount()-1;i>=0;i--)
		{
			View v = vg.getChildAt(i);
			if(v instanceof ImageView)
				v.setOnClickListener(this);
			else if(v instanceof ViewGroup)
				setOnClickRecurce((ViewGroup)v);
		}
	}
	public void showWindowHistory()
	{
		Intent in = BookmarkActivity.getIntentForWindowHistory(MainActivity.this, getTab());
		BrowserApp.runActivityForResult(MainActivity.this, IConst.CODE_GET_BOOKMARK_POS, in);
	}
	public void showWindowClosedTab()
	{
//		Intent in = BookmarkActivity.getIntentForWindowClosedTab(MainActivity.this, getTab());
//		BrowserApp.runActivityForResult(MainActivity.this, IConst.CODE_GET_BOOKMARK_POS, in);
		runById(Action.SHOW_CLOSED_TABS);
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	public boolean isPanelShown()
	{
		return mPanel.getVisibility()==View.VISIBLE;

	}
	public void showPanel(boolean show)
	{
		if(mUiState==UI_SEARCH_ON_PAGE)
			return;
		mWebViewFrame.setVisibility(show?View.GONE:View.VISIBLE);
		mPanel.show(show);
		UIUtils.showViews(show, mMainPanelContainer);
		if(show)
		{
			mUiState = UI_PANEL;
			hideMagickAndNavigation();
			mPanelLayout.setVisibility(false);
		}
		else
		{
			mUiState = UI_WEB;
			setInterface(0);
			mPanelLayout.setVisibility(true);
		}
	}
	final String getActionString(Action a)
	{
		if(a.param instanceof String)
			return (String)a.param;
		else if(a.param instanceof EditText)
			return ((EditText)a.param).getText().toString();
		return mPanel.getText();
	}
// обработка команд Action	
	public void runAction(Action a)
	{
		boolean closePanel = a.closePanel(); 
		if(closePanel&&(mPanel.getUrl()!=null||a instanceof SearchAction||a.command==Action.GO))
			showPanel(false);
		if(closePanel)
			clearCustomViews();
		if(a instanceof SearchAction)
		{
			((SearchAction)a).doAction(this, getActionString(a),mWebView.getUrl());
			return;
		}
		switch (a.command) {
			case Action.COPY_ALL_OPEN_URL:
				String out = st.STR_NULL;
				String str = st.STR_NULL;
				for (int i=0;i<mTabList.getCount();i++){
					try {
						str = (mTabList.getOpenedTabByPos(i)).getUrl().toString();
						if (str==null)
							continue;
					} catch (Throwable e) 
					{
						str=st.STR_NULL;
					}
					out+= str+"\n";
				}
				stat.setClipboardString(this, out);
				st.toast(R.string.сopied);
				return;
//			case Action.SHOW_CLOSED_TABS:
//				if (BookmarkActivity.inst==null){
//					BrowserApp.sendGlobalEvent(1,BookmarkActivity.class);
//				BookmarkActivity.inst.mShowOpenedWindows = a.command==Action.SHOW_CLOSED_TABS;
//				BookmarkActivity.inst.initListView();
//				BookmarkActivity.runForWindowId(this, getTab().windowId);
//				
//				st.toast("bbb");
//				}
//				return;
			// импорт в Action
//			case Action.BOOKMARK_EXPORT:
//				BookmarkImportExport.saveBookmarks(this,this);
//				return;
			case Action.INSTALL_JBAK2KEYBOARD:
				String link =  "https://play.google.com/store/apps/details?id="+stat.PACKAGE_JBAK2KEYBOARD;
				try {
			        Intent intent = new Intent(Intent.ACTION_VIEW);
			        intent.setData(Uri.parse(link));
			        activeInstance.startActivity(intent);
				} catch (Throwable e) {
				}
				return;
			case Action.INSTALL_MWCOSTS:
				String link1 =  "https://play.google.com/store/apps/details?id="+stat.PACKAGE_MWCOSTS;
				try {
			        Intent intent = new Intent(Intent.ACTION_VIEW);
			        intent.setData(Uri.parse(link1));
			        activeInstance.startActivity(intent);
				} catch (Throwable e) {
				}
				return;
			case Action.INSTALL_MWSHARE2SAVE:
				String link2 =  "https://play.google.com/store/apps/details?id="+stat.PACKAGE_MWSHARE2SAVE;
				try {
			        Intent intent = new Intent(Intent.ACTION_VIEW);
			        intent.setData(Uri.parse(link2));
			        activeInstance.startActivity(intent);
				} catch (Throwable e) {
				}
				return;
			case Action.QUICK_SETTINGS:
				new InterfaceSettingsLayout(mMagicButtonLayout, InterfaceSettingsLayout.MODE_QUICK_SETTINGS);
				return;
			case Action.ACTION_PLUGIN:
				BrowserApp.pluginServer.runAction(this, a);
			return;
			case Action.SHARE_URL:
			case Action.SHARE_ELEMENT:
				share(getActionString(a), mWebView.getTitle());
				break;
			case Action.GO_HOME:
			{
				showPanel(false);
				String url = Prefs.getString(Prefs.HOME_PAGE, null);
				if(url==null)
				{
					new ThemedDialog(this).setInput(getString(R.string.act_startAppHomePage), Prefs.get().getString(Prefs.HOME_PAGE, null),new OnUserInput() {
						
						@Override
						public void onUserInput(boolean ok, String newText) {
							if(ok)
							{
								Prefs.setString(Prefs.HOME_PAGE, newText);
								openUrl(newText, WINDOW_OPEN_SAME);
							}
						}
					}).show();
				}
				else
					openUrl(url, activeInstance.WINDOW_OPEN_SAME);
			}
			return;
			case Action.MAGIC_BUTTON_POS:
				showInterfaceSettings(InterfaceSettingsLayout.MODE_MAGIC_BUTTON_POS);
				break;
			case Action.INTERFACE_SETTINGS:
				showInterfaceSettings(-1);
				break;
			case Action.CLEAR_TEXT:
				mPanel.getUrlPanel().runAction(a);
				break;
			case Action.BACKGROUND_TAB:
			case Action.NEW_TAB:
				if(a.param instanceof Bookmark)
				{
					Bookmark b = (Bookmark)a.param;
					int ws = a.command;
					loadBookmark(b, b.tabMode==0?ws:b.tabMode);
				}
				else
				{
					tabStart(null,null);
					showPanel(true);
				}
				return;
			case Action.VOICE_SEARCH:
				startVoiceSearch(a);
				return;
			case Action.SHOW_MAIN_PANEL:
				showPanel(!isPanelShown());
				return;
			case Action.SEARCH_ON_PAGE:
				startSearchPage(a);
				return;
			case Action.CLOSE_TAB:
				if(a.param instanceof Integer)
					tabClose((Integer)a.param);
				else if(getTab()!=null)
					tabClose(getTab().windowId);
					return;
			case Action.OPEN_TAB:
				if(a.param instanceof Integer)
					tabOpen((Integer)a.param);
				return;
			case Action.SOURCE_CODE:
				mJsProcessor.runJavaScript(JavaScriptProcessor.JS_GET_SOURCE_CODE, null,getWebView());
				return;
			case Action.CLOSE_ALL_TABS:
            	new ThemedDialog(activeInstance).setConfirm(
            			activeInstance.getString(R.string.act_close_windows)+"?", 
            			null, 
            			new ConfirmOper() {
					
					@Override
					public void onConfirm(Object userParam) {
						closeAllWindows();
					}
				});

				return;
			case Action.ADD_BOOKMARK:
				showPanel(false);
				Bookmark parentFolder = null;
				Bitmap preview = null;
				if(getTab()!=null)
				{
					preview = getTab().createBitmap();
				}
				if(a.param instanceof Bookmark)
				{
					parentFolder = (Bookmark)a.param;
				}
				stat.createBookmarkWithDialog(this, parentFolder,preview);
				return;
			case Action.GO:
				String url = getActionString(a);
				if(TextUtils.isEmpty(url))
					return;
				openUrl(url, WINDOW_OPEN_SAME);
				return;
		default:
			break;
		}
		if(a.doAction(this))
		{
			if(a.closePanel())
				showPanel(false);
		}
		else
			runById(a.viewId);
		checkMainPanelStartMode();
	}
	public void closeAllWindows() {
		mTabList.closeAllTabs(true);
	}
// обработка нажатия кнопки magic и другое
	@Override
	public void onClick(View v) {
		hideRoundButton();
		if(v==mTopContainer||v==mPanel)
		{
			showPanel(false);
			return;
		}
		try{
			if(v.getTag() instanceof Action)
			{
				Action a = (Action)v.getTag();
				runAction(a);
			}
			else
				runById(v.getId());
		}
		catch(Throwable e)
		{
			Utils.log("OnClickError",e);
		}
	}
	public void share(String url,String title)
	{
	    Intent share = new Intent(android.content.Intent.ACTION_SEND);
	    share.setType("text/plain");
	    share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
	 
	    // Add data to the intent, the receiving app will decide
	    // what to do with it.
	    if(!TextUtils.isEmpty(title))
	    	share.putExtra(Intent.EXTRA_SUBJECT, title);
	    share.putExtra(Intent.EXTRA_TEXT, url);
	    startActivity(Intent.createChooser(share, getString(R.string.app_name)));
	}
	String mGoFromUser;
	@SuppressLint("DefaultLocale")
	void runById(int id)
	{
		boolean leavePanel = id==R.id.magicButton||id==Action.CLEAR_TEXT;
		if(!leavePanel)
			showPanel(false);
		if(id==R.id.search_down)
			mWebView.findNext(true);
		else if(id==Action.TAB_LIST)
			BookmarkActivity.runForWindowId(this, getTab().windowId);
		else if(id==Action.SHOW_CLOSED_TABS){
			BookmarkActivity.runForClosedTab(this, getTab().windowId);

//			showWindowClosedTab();
//			BookmarkActivity.inst.mShowOpenedWindows = act.command==Action.SHOW_CLOSED_TABS;
//			BookmarkActivity.inst.initListView();

//			BookmarkActivity.runForWindowId(this, getTab().windowId);
		}
		else if(id==R.id.search_up)
			mWebView.findNext(false);
		else if(id==R.id.search_close)
			endSearchPage();
		else if(id==R.id.searchPage)
			startSearchPage(null);
		else if(id==Action.GO_BACK)
			onBackPressed();
		else if(id==R.id.copy_link)
		{
			stat.setClipboardString(this, mWebView.getUrl());
		}
		else if(id==R.id.refresh)
		{
			mWebView.reload();
		}
		else if(id==R.id.pageTop)
		{
			mWebView.pageUp(true);
			//mWebView.scrollTo(0, 0);
		}
		else if(id==R.id.magicButton)
		{
			showPanel(!isPanelShown());
		}
		else if(id==R.id.pageBottom)
		{
			mWebView.pageDown(true);
		}
		else if(id==R.id.history)
		{
			BrowserApp.runActivityForResult(this,BookmarkActivity.class,IConst.CODE_GET_BOOKMARK);
		}
	}
	public final void exit()
	{
		exit(true);
	}
	public void exit(boolean confirm)
	{
		mInstances.remove(this);
		if(confirm&&!getTabList().isTempSession())
		{
			confirm = Prefs.isExitConfirm();
			if(confirm)
			{
				new ThemedDialog(this).setConfirm(getString(R.string.exit_confirm), null, new ConfirmOper() {
					
					@Override
					public void onConfirm(Object userParam) {
						exit(false);
					}
				});
				return;
			}
		}
		if(getTabList().isIncognito())
			TempCookieStorage.onStartIncognito(false);
		if(activeInstance==this)
			activeInstance=null;
		Tab ww = getTab();
		if(ww!=null)
		{
			if(ww.isEmpty()) {
				getTabList().closeCurrent();
			} else {
				getTab().saveNow(false);
			}
		}
		List<MainActivity> liveRefs = mInstances.getLiveRefs();
		boolean doExitActions = liveRefs.size()==0;
		clearWindows();
		if(doExitActions)
		{
			new st.SyncAsycOper() {
				
				@Override
				public void makeOper(UniObserver obs) throws Throwable {
					clearData(Prefs.getJSONObject(Prefs.CLEAR_EXIT),true);
				}
			}.startAsync();
		}
		finish();
	}
	int mWindowsState = MainPanel.STATE_WINDOWS_SHOW;
	void tabFirstStart()
	{
		int fs = Prefs.getStartApp();
		if(fs==Prefs.START_APP_HOME_SCREEN)
		{
			if(getTabList().getCount()>0)
				mWindowsState = MainPanel.STATE_WINDOWS_RESTORE;
			else
				mWindowsState =  MainPanel.STATE_WINDOWS_NONE;
			getTabList().closeAllTabs(true);
		}
		else if(fs==Prefs.START_APP_HOMEPAGE)
		{
			getTabList().closeAllTabs(false);
			openUrl(Prefs.get().getString(Prefs.HOME_PAGE, IConst.ABOUT_BLANK), Action.NEW_TAB);
		}
		else
			tabRestoreLast();
		mMagicButton.postDelayed(new Runnable() {
			@Override
			public void run() {
				BrowserApp.checkCrash(MainActivity.this);
			}
		}, 1000);
	}
	public void tabRestoreLast()
	{
		Tab ww = mTabList.restoreLast();
		mTabList.setCurrent(ww);
		tabStart(getTab(), null);
	}
	public void tabOpen(int windowId) {
		if(getTab().windowId==windowId)
			return;
		Tab ww = getTabList().getTabById(windowId, true);
//		if (ww==null) {
//			ww = new Tab(activeInstance, getTabList().getNewTabId(),getTabList());
//			//act.tabStart(ww, null);
//		}
		if(ww!=null&&ww.isClosed())
			ww.setClosed(false);
		tabStart(ww,null);
	}
	public void tabClose(int windowId)
	{
		mTabList.closeTab(windowId);
	}
	public Tab getOpenedWebWindow(int windowId)
	{
		return mTabList.getOpenedTab(windowId);
	}
	Action mVoiceAction;
	void startVoiceSearch(Action vsAction) {
		mVoiceAction = vsAction;
		Intent in = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
						.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName())
						.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
						.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
						.putExtra(RecognizerIntent.EXTRA_PROMPT, getTitle());
		startActivityForResult(in, CODE_VOICE_RECOGNIZER);
	}
	void tabStart(Tab tab,String url)
	{
		tabStart(tab, url, false);
	}
	void tabStart(Tab tab,String url,boolean background)
	{
		mPanel.setWindowsState(mWindowsState);
		mWindowsState = MainPanel.STATE_WINDOWS_SHOW;
		boolean loadFromBundle = url==null&&tab!=null&&tab.getWebView()==null;
		boolean showPanel = (tab==null||tab.currentBookmark==null)&&TextUtils.isEmpty(url);
		if(tab==null)
			tab = new Tab(this,getTabList().getNewTabId(),getTabList());
		mTabList.addOpenedTab(tab,background);
		MyWebView webView = tab.getWebView();
		if(webView==null)
			webView = new MyWebView(this);
		if(webView.getParent() instanceof ViewGroup)
			((ViewGroup)webView.getParent()).removeView(webView);
		if(!background)
		{
			webView.setVisibility(View.VISIBLE);
			mWebViewFrame.addView(webView);
			mWebView = webView;
		}
		else
		{
			mTempWebViewFrame.addView(webView,new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			
		}
		initWebView(webView);
		tab.setWebView(webView);
		if(showPanel)
			mPanel.setMode(MainPanel.MODE_START_PAGE);
		saveCurrentWindowId();
		if(loadFromBundle&&getTab().savedState!=null)
		{
			//mWebView.loadUrl(LOAD_RESTORE);
			getTab().restoreWebViewState();
			if(getTab().getCurBookmark()==null)
				showPanel = true;
		}
		else if(url!=null)
		{
			if(!IConst.ABOUT_NULL.equals(url))
			{
				tab.setUrl(url);
				tab.getWebView().loadUrl(url);
			}
			else
			{
				if(getTab().getCurBookmark()!=null)
				{
					sendWebViewEvent(WebViewEvent.WWEVENT_TITLE_LOADED, getWebView(), getTab().getCurBookmark().getTitle(), null);
					sendWebViewEvent(WebViewEvent.WWEVENT_PAGE_FINISH, getWebView(), getTab().getCurBookmark().getUrl(), null);
				}
			}
		}
		showPanel(showPanel);
			//Db.getWindowTable().saveWindow(window);
	}
	boolean mRestoreWindow = false;
	@SuppressLint("NewApi")
	public void startSearchPage(Action act) {
		mUiState = UI_SEARCH_ON_PAGE;
		hideMagickAndNavigation();
		mSearchPage.setVisibility(View.VISIBLE);
		mPanelLayout.setVisibility(false);
		if(Build.VERSION.SDK_INT>=16)
		getWebView().setFindListener(new FindListener() {
			
			@Override
			public void onFindResultReceived(int activeMatchOrdinal,int numberOfMatches, boolean isDoneCounting) {
				String title = null;
				String textSearch = mSearchPageText.getText().toString();
				if(numberOfMatches==0)
					title = new StrBuilder(MainActivity.this).add(R.string.searchPage_results).addLong(numberOfMatches).addBrackets(textSearch).toString();
				else
					title = new StrBuilder(MainActivity.this).add(R.string.searchPage_results).addLong(activeMatchOrdinal+1).add('/').addLong(numberOfMatches).addBrackets(textSearch).toString();
				mSearchPageTitle.setText(title);
			}
		});
		boolean withText = act!=null&&act.param instanceof String; 
//		if(withText)
//		{
//			mSearchPageText.setText((String)act.param);
//		}
//		else
//		{
//			St.showEditKeyboard(mSearchPageText);
//		}
		st.showEditKeyboard(mSearchPageText);
		selectAll(mSearchPageText);
		if(mSearchPageText.getText().length()>0)
			searchOnPage(mSearchPageText.getText().toString());
	}
	public final int getUiState()
	{
		return mUiState;
	}
	private void endSearchPage() {
		mUiState = UI_WEB;
		mWebView.clearMatches();
		mSearchPage.setVisibility(View.GONE);
		mPanelLayout.setVisibility(true);
		showPanel(false);
	}
	public void showWindowLayout(Tab ww)
	{
		mCustomLayoutFrame.setBackgroundColor(0xcc000000);
		MyTheme.get().setView(mCustomLayoutFrame, MyTheme.ITEM_DIALOG_SHADOW);
		mCustomLayoutFrame.setVisibility(View.VISIBLE);
		mCustomLayoutFrame.removeAllViews();
		mCustomLayoutFrame.addView(new WindowLayout(this, ww));
	}
	public boolean clearCustomViews()
	{
		if(mSearchPage.getVisibility()==View.VISIBLE)
		{
			endSearchPage();
			return true;
		}
		if(mCustomLayoutFrame.getVisibility()==View.VISIBLE)
		{
			if(mCustomLayoutFrame.getChildAt(0) instanceof WindowLayout)
			{
				mCustomLayoutFrame.removeAllViews();
				mCustomLayoutFrame.setVisibility(View.GONE);
				return true;
			}
	        mChromeClient.onHideCustomView();
			return true;
		}
//		if (mNavigationPanel!=null){
//			mNavigationPanel.setVisibility(View.GONE);
//		}
		if(mMagicButtonLayout.getChildCount()>0)
		{
			boolean ret = false;
			for(int i=mMagicButtonLayout.getChildCount()-1;i>=0;i--)
			{
				if(mMagicButtonLayout.getChildAt(i)!=mMagicButton)
				{
					ret = true;
					mMagicButtonLayout.removeViewAt(i);
				}
			}
			if(ret)
				return true;
		}
//		if(mNvigationPanelLayout.getChildCount()>0)
//		{
//			boolean ret = false;
//			for(int i=mNvigationPanelLayout.getChildCount()-1;i>=0;i--)
//			{
//				if(mNvigationPanelLayout.getChildAt(i)!=mNavigationPanel)
//				{
//					ret = true;
//					mNvigationPanelLayout.removeViewAt(i);
//				}
//			}
//			if(ret)
//				return true;
//		}
		return false;
	}
	@Override
	public void onBackPressed() {
		hideRoundButton();
		if (st.fl_temp_hide_navigationpanel){
			setInterface(2);
			showMagickAndNavigation();
			st.fl_temp_hide_navigationpanel= false;
		}
		int www = mTabList.getCurrent().getWebView().getChildCount();
		if(clearCustomViews())
			return;
		if(isPanelShown()&&mPanel.getMode()!=MainPanel.MODE_START_PAGE)
		{
			onClick(mPanel);
		}
		else if(mWebView.canGoBack())
			mWebView.goBack();
		else if(!Prefs.isCloseCurrentLastTab()
				&&mTabList.getCurrent().getWebView().getChildCount() == 0
				&&!isPanelShown())
		{
			www = mTabList.getCurrent().getWebView().getChildCount();
			st.toast(R.string.close_tab_last_page_toast);
			return;
		}
		else if(mTabList.closeCurrent())
		{
			int tablist = mTabList.getCount();
//			int www = mTabList.getCurrent().getWebView().getChildCount();
			if (mTabList.getCount() > 0) {
				if (!checkMainPanelStartMode())
					return;
			}
			if (Prefs.isExitPanel()) {
				if(isPanelShown()&&!checkMainPanelStartMode())
					showPanel(false);
			} else
				exit();
		}
		else
		{
			exit();
		}
	}
	void initWebView()
	{
		initWebView(getWebView());
	}
	public static final String INCOGNITO_CACHE_FOLDER = "IncognitoCache";
	@SuppressLint({ "NewApi", "SetJavaScriptEnabled" })
	void initWebView(WebView ww)
	{
		int apiLevel = Build.VERSION.SDK_INT; 
		boolean incognito = getTabList().isIncognito();
		CookieManager.getInstance().setAcceptCookie(!mTabList.isIncognito());
		WebSettings ws = ww.getSettings();
		ws.setCacheMode(Prefs.getCacheMode());
		String ct = Prefs.getCacheType();
		ws.setAppCacheEnabled(true);
		if(ct.equals(Prefs.CACHE_TYPE_APP))
		{
			String cacheAppPath = "JbakBrowserCache";
			if(incognito)
				cacheAppPath+='/'+INCOGNITO_CACHE_FOLDER;
			ws.setAppCachePath(cacheAppPath);
		}
		else
		{
			File f = new File(ct);
			f.mkdirs();
			if(f.exists()&&f.isDirectory())
			{
				BrowserApp.cacheDir = f;
				if(incognito)
					f = new File(f,INCOGNITO_CACHE_FOLDER);
				
				ws.setAppCacheEnabled(true);
				ws.setAppCachePath(f.getName());
			}
		}
		
//		String ua = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0";
		//String ua = "ELinks/0.2.3 (textmode; NetBSD 1.6.2 sparc; 132x43)";
//		ws.setUserAgentString(ua);
		ws.setJavaScriptEnabled(true);
		ww.addJavascriptInterface(mJsProcessor, JavaScriptProcessor.INTERFACE_NAME);
        ws.setGeolocationEnabled(true);
		ws.setMinimumFontSize(Prefs.get().getInt(Prefs.MIN_FONT, 8));
		ws.setDatabaseEnabled(!mTabList.isIncognito());
		if(apiLevel<18&&incognito)
			ws.setSavePassword(false);
		ws.setSaveFormData(!incognito);
		if(apiLevel>=14)
			ws.setTextZoom(Prefs.getInt(Prefs.FONT_SCALE, 120));
		else
		{
			TextSize ts = Prefs.OLD_SIZES.getValueByKey(Prefs.getInt(Prefs.FONT_SCALE, 100), TextSize.NORMAL);
			ws.setTextSize(ts);
		}
		try{
			if(apiLevel>=19)
				ws.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
		}
		catch(Throwable ignor)
		{}
		if(apiLevel>=11)
			ws.setEnableSmoothTransition(true);
		if(apiLevel>=11)
		{
			ws.setBuiltInZoomControls(true);
			ws.setDisplayZoomControls(false);
		}
		ws.setJavaScriptCanOpenWindowsAutomatically(true);
		ws.setUseWideViewPort(true); 		// Не трогать! Иначе превьюха ломается 
		ws.setPluginState(PluginState.ON);
		ws.setSupportZoom(true); 
		ws.setSupportMultipleWindows(true); 
		ws.setDomStorageEnabled(true);
		ww.setDrawingCacheBackgroundColor(Color.RED);
		ww.setDownloadListener(new DownloadListener() {
			
			@Override
			public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
				DownloadFileInfo fi = new DownloadFileInfo();
				fi.uri = Uri.parse(url);
				fi.fileSize = contentLength;
				fi.mimeType = mimetype;
				if(contentDisposition!=null)
					fi.filename = UrlProcess.getFileNameFromContentDisposition(contentDisposition);
				closeEmptyWindow();
				//fi.filename = contentDisposition;
				UrlProcess.downloadFileWithDialog(MainActivity.this, fi);
			}
		});
		ww.setWebViewClient(mClient);
		ww.setWebChromeClient(mChromeClient);
//		if(mWebView instanceof MyWebView)
//			((MyWebView)mWebView).setLongClickHandler(this);
		ww.setOnLongClickListener(this);
	}
	public void closeEmptyWindow() {
		Tab ww = getTab();
		if(ww==null)
			return;
		if(getTab().isEmpty())
			mTabList.closeCurrent();
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==CODE_BUY_PRO)
		{
			Payment.processBuyProActivityResult(this,requestCode,resultCode,data);
			return;
		}
		if(resultCode==RESULT_OK)
		{
			switch (requestCode) {
			case CODE_VOICE_RECOGNIZER:
				if(mVoiceAction!=null&&mVoiceAction.param instanceof PanelUrlEdit)
					((PanelUrlEdit)mVoiceAction.param).onSuccessVoiceReconition(data);
				else if(mPanel!=null&&mPanel.getUrlPanel()!=null)
				{
					showPanel(true);
					mPanel.getUrlPanel().onSuccessVoiceReconition(data);
				}
				mVoiceAction = null;
				break;
			case CODE_GET_BOOKMARK:
				Bookmark bm = (Bookmark) data.getSerializableExtra(BookmarkActivity.EXTRA_BOOKMARK);
				loadBookmark(bm,WINDOW_OPEN_SAME);
				break;
			case CODE_WINDOW_ID:
				int windowId = data.getIntExtra(BookmarkActivity.EXTRA_CUR_WINDOW_ID, 1);
				if(data.getBooleanExtra(BookmarkActivity.EXTRA_CLOSE, false))
					tabClose(windowId);
				else
					tabOpen(windowId);
				break;
			case CODE_GET_BOOKMARK_POS:
				int pos = data.getIntExtra(BookmarkActivity.EXTRA_CURPOS, 0);
				int cp = mWebView.copyBackForwardList().getCurrentIndex();
				pos = pos-cp;
				if(pos!=0)
					mWebView.goBackOrForward(pos);
				break;
			case CODE_FILE_UPLOAD_REQUEST:
				Uri uri = data==null?null:data.getData();
				if(mUploadInfo!=null&&mUploadInfo.uploadMsg!=null)
				{
					mUploadInfo.uploadMsg.onReceiveValue(uri);

				}
				break;
			default:
				break;
			}
		}
		else
		{
			if(requestCode==CODE_FILE_UPLOAD_REQUEST&&mUploadInfo!=null&&mUploadInfo.uploadMsg!=null)
				mUploadInfo.uploadMsg.onReceiveValue(null);
		}
	}
	public void loadBookmark(Bookmark bm,int tabMode) {
		String url = TextUtils.isEmpty(bm.originalUrl)?bm.getUrl():bm.originalUrl;
		if(tabMode>WINDOW_OPEN_SAME)
		{
			closeEmptyWindow();
			Tab ww = new Tab(this, getTabList().getNewTabId(),getTabList());
			ww.setCurrentBookmark(bm);
			tabStart(ww,url,tabMode==Action.BACKGROUND_TAB);
		}
		else
		{
			Tab tab = getTab();
			if (tab==null) {
				//tab = new Tab;
				tabStart(null,null);
				showPanel(false);
				tab = getTab();

			}
			tab.setCurrentBookmark(bm);
//			getTab().setCurrentBookmark(bm);
			mWebView.loadUrl(url);
		}
		PanelWindows pw = (PanelWindows) mPanelLayout.getPanel(PanelLayout.PANEL_TABS);
		if(pw!=null)
			pw.getWindowsPanel().scrollToCurrent();
	}
	public void selectAll(final EditText et)
	{
		et.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				et.selectAll();
			}
		}, 500);
	}
	void saveCurrentWindowId()
	{
		if(getTab()!=null)
			Db.getStringTable().save(CUR_WINDOW_ID, st.STR_NULL+getTab().windowId);
	}
	@SuppressLint("NewApi")
	public void clearWindow(Tab w)
	{
		WebView v = w.getWebView(); 
		if(v!=null)
		{
			if(v.getParent() instanceof ViewGroup)
				((ViewGroup)v.getParent()).removeView(v);
			if(mChromeClient!=null)
				mChromeClient.onCloseWindow(v);
			v.setWebChromeClient(null);
			v.setWebViewClient(null);
			if(Build.VERSION.SDK_INT>=11)
				v.removeJavascriptInterface(JavaScriptProcessor.INTERFACE_NAME);
			v.destroy();
			w.setWebView(null);
		}
	}
	public void clearWindows()
	{
		mWebViewFrame.removeAllViews();
		mTempWebViewFrame.removeAllViews();
		mTabList.clearOpened();
	}
	@Override
	protected void onDestroy() {
		BrowserApp.INSTANCE.removeGlobalListener(this);
		mJsProcessor = null;
		clearWindows();
		mChromeClient = null;
		mClient = null;
		super.onDestroy();
	}
//	@Override
//	public boolean dispatchTouchEvent(MotionEvent ev) {
//		boolean dispatch = true;
//		if(ev.getAction()==MotionEvent.ACTION_DOWN)
//		{
//			if(isPanelShown())
//			{
//				dispatch = UIUtils.isTouchEventForViews(ev, null, mInputBlock,mMagicButton,mBookmarksPanel);
//				Log.d(MainActivity.class.getSimpleName(), "dispatch="+dispatch);
//			}
//			else
//				dispatch = UIUtils.isTouchEventForViews(ev, null, mMagicButton);
//		}
//		if(!dispatch&&isPanelShown())
//		{
//			showPanel(false);
//			return true;
//		}
//		return super.dispatchTouchEvent(ev);
//	}

	public ViewGroup getTopContainer()
	{
		return mTopContainer;
	}
	public void showInterfaceSettings(int settingsMode)
	{
		clearCustomViews();
		if(settingsMode<0)
			settingsMode = InterfaceSettingsLayout.MODE_INTERFACE_SETTINGS;
		if(mPanel.getMode()==MainPanel.MODE_START_PAGE)
		{
			openUrl(IConst.ABOUT_BLANK, WINDOW_OPEN_SAME);
		}
		new InterfaceSettingsLayout((RelativeLayout)mMagicButton.getParent(),settingsMode);
	}
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public boolean onLongClick(View v) {
		hideRoundButton();
		if(v==mMagicButton)
		{
			showInterfaceSettings(InterfaceSettingsLayout.MODE_LAST_RUNNED);
			return true;
			//.show();
		}
		if(v instanceof PanelButton)
		{
			PanelButton pb = (PanelButton)v;
			switch(pb.getAction().command)
			{
				case  Action.SHOW_MAIN_PANEL:
					showInterfaceSettings(-1);
					return true;
				case  Action.GO_BACK:
				case  Action.GO_FORWARD:
					showPanel(false);
					showWindowHistory();
					return true;
				case Action.ACTION_BOOKMARK:
					Bookmark bm = null;
					if(v.getTag() instanceof Action)
						bm = (Bookmark) ((Action)v.getTag()).param;
					else if(v.getTag() instanceof Bookmark)
						bm = (Bookmark)v.getTag();
					if(bm!=null)
						new MenuBookmark(v.getContext(), bm, BookmarkActivity.TYPE_BOOKMARKS, null,getTab().mThumbnail).show();
					return true;
				default:
					runAction(pb.getAction());
			}
			
			return true;
		}
		else if(v instanceof BookmarkView)
		{
			Bookmark bm = ((BookmarkView) v).getBookmark();
			if(bm!=null)
				new MenuBookmark(v.getContext(), bm, BookmarkActivity.TYPE_BOOKMARKS, null,null).show();
		}
		if(v==mWebView)
		{
			int lc = Prefs.getLongClick();
			if (stat.fl_one_select) {
				lc = Prefs.LONGCLICK_TEXT_SELECTION;
				stat.fl_one_select=false;
				return false;
			}
			if(lc==Prefs.LONGCLICK_TEXT_SELECTION)
				return false;
			else if(lc==Prefs.LONGCLICK_DEFAULT)
			{
				return new WebViewContextMenu(this).checkLongClick(this);
			}
			else
			{
				mJsProcessor.runJavaScript(JavaScriptProcessor.JS_GET_LONG_CLICK_INFO, null,getWebView() );
				return true;
			}
		}
		return false;
	}
	public void setMagicButtonPos(int pos)
	{
		Prefs.setMagicButtonPos(pos);
		RelativeLayout layout = (RelativeLayout) mMagicButton.getParent();
		layout.removeView(mMagicButton);
		InterfaceSettingsLayout.setViewToRelativeLayout(layout, mMagicButton, pos, LayoutParams.WRAP_CONTENT,getResources().getDimensionPixelSize(R.dimen.magic_button_size));
	}
	public void setNavigationPanelPos(int pos)
	{
		if (st.pref_navigation!=1) {
			if (mNavigationPanel!=null)
				mNavigationPanel.setVisibility(View.GONE);
			return;
		}
		Prefs.setNavigationPanelPos(pos);
		RelativeLayout layout = (RelativeLayout) mNavigationPanel.getParent();
		layout.removeView(mNavigationPanel);
		if (st.pref_navigation!=1)
			return;
		InterfaceSettingsLayout.setViewToRelativeLayout(layout, mNavigationPanel, pos, LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
	}
//	@Override
//	public boolean onPrepareOptionsMenu(Menu menu) {
//		showPanel(!isPanelShown());
//		return true;
//	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_MENU&&event.getRepeatCount()==0)
		{
			if(checkMainPanelStartMode())
				return true;
			showPanel(!isPanelShown());
			return true;
		}
		if(keyCode==KeyEvent.KEYCODE_BACK&&event.getRepeatCount()==1)
		{
			showWindowHistory();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View v, int arg2,long arg3) {
		return onLongClick(v);
	}
	public final MyWebView getWebView()
	{
		Tab t = getTab();
		if(t==null)
			return null;
		return t.getWebView();
	}
	public final Tab getTab()
	{
		return mTabList.getCurrent();
	}
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		openViewIntent(intent);
	}
	public boolean openViewIntent(Intent intent)
	{
		if(Intent.ACTION_VIEW.equals(intent.getAction())&&intent.getData() instanceof Uri)
		{
			Uri uri = intent.getData();
			String scheme = uri.getScheme();
			if(PluginUtils.JBAK_BROWSER_SCHEME.equals(scheme))
			{
				if(PluginUtils.JBAK_BROWSER_INCOGNITO_URL.equals(uri.toString()))
				{
					TempCookieStorage.onStartIncognito(true);
					mTabList.setIncognito(true);
					tabStart(null, null);
					return true;
				}
			}
			String adr = uri.toString();
			//  из-за этих строк, файлы с русскими названиями не открываются и 
			// браузер по кнопке back закрывается нормально,  а вот файлы
			// с английскими названиями постоянно переружаются. 
			// Разобраться!
//			if (adr.startsWith(IConst.STR_FILE)){
//				String url = adr;
//				adr = IConst.STR_FILE.substring(0,IConst.STR_FILE.length())+uri.getPath();
//				if (url.compareToIgnoreCase(adr)!=0){
//					st.toast("Понимаю только английские названия файлов");
//				}
//			} 
			//openUrl(uri.toString(),Action.NEW_TAB);
			openUrl(adr,Action.NEW_TAB);
			return true;
		}
		return false;
	}
	public final void openUrl(String url,int windowState)
	{
		if (url.startsWith("^=") ){
			SearchAction sa = new SearchAction(SearchSystem.CMD_SEARCH,0,0);
			sa.doAction(this, url, null);
			return;
		}
		if(!stat.isWebAddr(url))
		{
			SearchAction sa = new SearchAction(SearchSystem.CMD_SEARCH,0,0);
			sa.doAction(this, url, null);
			return;
		}
		else if(!stat.hasScheme(url))
		{
			mGoFromUser = url;
//			if(!url.startsWith(IConst.STR_FILE)){
//				
//			}
//			else 
			if(!url.startsWith(IConst.HTTP)&&!url.startsWith(IConst.HTTPS))
				url=IConst.HTTP+url;
		}
		loadBookmark(new Bookmark(url, null, System.currentTimeMillis()), windowState);
		setNavigationPanel();
	}
	final boolean checkMainPanelStartMode()
	{
//		setNavigationPanel();
		if(mPanel.getMode()==MainPanel.MODE_START_PAGE&&getTab()!=null&&getTab().getCurBookmark()==null&&!getTab().isLoading())
		{
			showPanel(true);
			return true;
		}
		return false;
	}
	@Override
	protected void onPause() {
		super.onPause();
		mTimer.onActivityPause();
		}
	@Override
	protected void onResume() {
		super.onResume();
		TempCookieStorage.onMainActivityResume(this);
		activeInstance = this;
		boolean incognito = getTabList()!=null&&getTabList().isIncognito();
		CookieManager.getInstance().setAcceptCookie(!incognito);
		checkMainPanelStartMode();
		mTimer.onActivityResume();

	}
	public void onSettingsChanged(String prefKey)
	{
		if(Prefs.CACHE_MODE.equals(prefKey))
		{
			int cache = Prefs.getCacheMode();
			for(Tab ww:getTabList().mOpenedWindows)
			{
				ww.getWebView().getSettings().setCacheMode(cache);
			}
		}
		if(Prefs.CACHE_TYPE.equals(prefKey))
		{
			WebView ww = getWebView();
			ww.clearCache(true);
			clearWindows();
			tabFirstStart();
		}
	}
	@Override
	public void onGlobalEvent(int code, Object param) {
		switch (code) {
		case BrowserApp.GLOBAL_BOOKMARKS_CHANGED:
			sendWebViewEvent(WebViewEvent.WWEVENT_BOOKMARKS_CHANGED, getWebView(), param, null);
			break;
//		case BrowserApp.GLOBAL_MAIN_PANEL_LAYOUT_CHANGED:
//			int pl = Prefs.getPanelLayoutType();
//			if(pl!=mPanel.getLayoutType())
//			{
//				int mod = mPanel.getMode();
//				mMainPanelContainer.removeAllViews();
//				mPanel = new MainPanel(this, pl);
//				mMainPanelContainer.addView(mPanel);
////				mPanel.setMode(mod);
//				onThemeChanged(MyTheme.get());
//			}
//			break;
		case BrowserApp.GLOBAL_SETTINGS_CHANGED:
			onSettingsChanged((String)param);
			break;
		case BrowserApp.GLOBAL_ACTION:
			if(this!=activeInstance)
				return;
			if(param instanceof Action)
			{
				runAction((Action)param);
			}
			break;
//		case BrowserApp.GLOBAL_NETWORK_CHANGED:
//			WebView ww = getWebView();
//			if(ww!=null)
//			{
//				int cm = NetworkChecker.inetAvaliable?WebSettings.LOAD_DEFAULT:WebSettings.LOAD_CACHE_ELSE_NETWORK;
//				ww.getSettings().setCacheMode(cm);
//			}
//			break;
		default:
			break;
		}
	}
	public void uploadFile(FileUploadInfo info)
	{
		mUploadInfo = info;
		Utils.logStack("Upload", info.acceptType+","+info.capture+","+getWebView().getUrl());
        Intent i=new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        startActivityForResult(Intent.createChooser(i, "Select Source"),CODE_FILE_UPLOAD_REQUEST);
	}
	public void setFullscreen(boolean fullscreen,boolean setPref)
	{
		mIsFullscreen = fullscreen;
		if(fullscreen)
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		else
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		if(setPref)
			Prefs.setFullScreen(fullscreen);
	}
	void doFontScale(int size)
	{
		boolean bigger = size>0; 
		String sz = st.STR_SPACE;
		if(Build.VERSION.SDK_INT>=14)
		{
			if(bigger)
				sz = " +";
			runAction(Action.create(Action.FONT_SCALE,size));
		}
		else
		{
			TextSize ts = getWebView().getSettings().getTextSize();
			Integer sizes[] = Prefs.OLD_SIZES.getKeys();
			Integer cur = Prefs.OLD_SIZES.getKeyByValue(ts);
			int index = Arrays.binarySearch(sizes, cur);
			if(index>0&&!bigger)
				cur = sizes[index-1];
			else if(index<sizes.length-1&&bigger)
				cur = sizes[index+1];
			runAction(Action.create(Action.FONT_SCALE,cur));				
			size = cur;
		}
		sz+=size;
		CustomPopup.toast(this,getString(R.string.act_font_scale)+sz);	}
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if(ev.getAction()==MotionEvent.ACTION_DOWN)
			mJsProcessor.resetLastDown();
		return super.dispatchTouchEvent(ev);
	}
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int code = event.getKeyCode();
		if(code==KeyEvent.KEYCODE_VOLUME_UP||code==KeyEvent.KEYCODE_VOLUME_DOWN)
		{
			int st = Prefs.getVolumeKeysState();
			if(st!=Prefs.VOLUME_KEYS_NONE)
			{
				if(event.getAction()==KeyEvent.ACTION_DOWN)
				{
					if(code==KeyEvent.KEYCODE_VOLUME_UP)
					{
						if(st==Prefs.VOLUME_KEYS_SCROLL)
							getWebView().pageUp(false);
						else if(event.getRepeatCount()==0)
							doFontScale(+10);
					}
					else
					{
						if(st==Prefs.VOLUME_KEYS_SCROLL)
							getWebView().pageDown(false);
						else if(event.getRepeatCount()==0)
							doFontScale(-10);
					}
				}
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}
	public void setInterface(int mode)
	{
		if (mode ==0||mode==1)
			UIUtils.showViews(Prefs.isMagicKeyVisible(), mMagicButton);
		if (mode ==0||mode==2){
			boolean visible = Prefs.isNavigationPanelVisible();
			UIUtils.showViews(visible, mNavigationPanel);
			int bbb = mNavigationPanel.getVisibility();
			if (visible&&mNavigationPanel.getVisibility()==View.GONE)
				mNavigationPanel.setVisibility(View.VISIBLE);
		}
		if (st.fl_temp_hide_navigationpanel||mode==3){
			UIUtils.showViews(false, mNavigationPanel);
		}
	}
	public void onJSProcessorEvent(int code,Object param)
	{
		if(code==JavaScriptProcessor.JS_GET_SOURCE_CODE)
		{
			String source = (String)param;
//			source = HtmlUtils.writeBaseUrl(source, mPanel.mUrl);
			new DialogEditor(this, getString(R.string.act_source_code), source).setFileNameForSave(getSaveFilename(null)).show();
		}
	}
	public final String getSaveFilename(String defExt)
	{
		return UrlProcess.filenameFromUrl(mPanel.getUrl(),defExt);
	}
	public JavaScriptProcessor getJavaScriptProcssor()
	{
		return mJsProcessor;
	}
	public final MainPanel getMainPanel()
	{
		return mPanel;
	}
	public void onThemeChanged(MyTheme t)
	{
		t.setActive(this, true);
		MyTheme old = MyTheme.get();
		BrowserApp.setTheme(t);
		t.onCreateActivity(this);
		mPanel.refresh();
		mPanelLayout.onThemeChange(t);
		if(old!=MyTheme.get())
			old.setActive(this, false);
	}
	public void clearData(JSONObject data,boolean exit)
	{
		boolean clearHistory = data.optInt(HISTORY,0)==1;
		boolean clearCache = data.optInt(CACHE,0)==1;
		boolean clearCookies = data.optInt(COOKIES,0)==1;
		boolean closeWindows = data.optInt(CLEAR_CLOSE_WINDOWS,0)==1;
		boolean clearPasswords = data.optInt(CLEAR_SAVED_PASSWORDS)==1;
		boolean killProcess = data.optInt(CLEAR_KILL_PROCESS)==1;
		boolean clearSearchHistory = data.optInt(CLEAR_SEARCH_HISTORY)==1;
		boolean needCloseWindows = false;
		
		if(closeWindows)
		{
			Db.getWindowTable().clear();
			needCloseWindows = true;
		}
		if(clearHistory)
		{
			int clear = DbClear.clearHistory(this,data.optInt(CLEAR_TYPE,DbClear.CLEAR_ALL),new DbClear.ClearDataHistory());
			if(clear<0)
			{
				Utils.log("CLEAR_HISTORY", "Error");
			}
		}
		if(clearSearchHistory)
			stat.clearSearchHistory(BrowserApp.INSTANCE);
		if(clearPasswords)
		{
			WebViewDatabase wd = WebViewDatabase.getInstance(this);
			wd.clearUsernamePassword();
			wd.clearHttpAuthUsernamePassword();
		}
		if(clearCookies)
		{
			  CookieSyncManager cookieSyncMngr=CookieSyncManager.createInstance(this);
			  cookieSyncMngr.startSync();
			  CookieManager cookieManager=CookieManager.getInstance();
			  cookieManager.removeAllCookie();
			  cookieManager.removeSessionCookie();
			  cookieSyncMngr.stopSync();
		}
		if(clearCache)
		{
			//getWebView().clearCache(true);
			FileUtils.delete(getCacheDir());
			FileUtils.delete(getApplicationContext().getCacheDir());
			FileUtils.delete(getExternalCacheDir());
			//closeAllWindows();
			//needCloseWindows = true;
		}
		if(needCloseWindows)
		{
			if(exit)
				Db.getWindowTable().clear();
			else
				closeAllWindows();
		}
		if(exit)
		{
//			try{
//				Db.INSTANCE.getWritableDatabase().execSQL("vacuum");
//			}
//			catch(Throwable e)
//			{
//				
//			}
		}
		if(killProcess)
		{
			if(!exit)
				finish();
			android.os.Process.killProcess(android.os.Process.myPid());
			return;
		}

	}
	public final boolean hasSoftKeyboard()
	{
		return mIsSoftKeyboradVisible;
	}
	public void openWebArchive(File f) {
		String data = st.fileToStr(f);
		if(TextUtils.isEmpty(data))
		{
			//CustomPopup.toast(this, R.string.);
			return;
		}
		String url = Uri.fromFile(f).toString();
		//getWebView().loadDataWithBaseURL(url, data, "text/html", "quoted-printable", url);
        if(android.os.Build.VERSION.SDK_INT == 19){
    		getWebView().loadUrl(url);
        } else {
    		getWebView().loadDataWithBaseURL(url, data, "application/x-webarchive-xml", "UTF-8", url);
        }

	}
	public void showCustomView(View view) {
		if(view==null) //hide
		{
			setFullscreen(Prefs.getFullscreen(), false);
	    	mCustomLayoutFrame.removeAllViews();
	        mCustomLayoutFrame.setVisibility(View.GONE);
	        //mWebView.setVisibility(View.VISIBLE);
		}
		else
		{
			try{
				Bitmap bmp = getTab().mThumbnail;
//				if(bmp==null)
				bmp = stat.createWebViewThumbnail(getWebView());
				Bookmark b = getTab().currentBookmark;
				Db.getExtHistory().insertBookmark(Db.TableExtHistory.VIDEO, b, null, bmp);
			}
			catch(Throwable e)
			{
				Utils.log(e);
			}
			if (view!=null) {
			    if (view instanceof FrameLayout) {
			        final FrameLayout frame = (FrameLayout) view;
			        int iii = frame.getChildCount();
			        if (frame.getChildAt(0) instanceof VideoView) {
			            // get video view

			        	VideoView vv = (VideoView) frame.getFocusedChild();
			        	Uri mUri = null;
			        	try {
			        	    java.lang.reflect.Field mUriField = VideoView.class.getDeclaredField("mUri");
			        	    mUriField.setAccessible(true);
			        	    mUri = (Uri)mUriField.get(vv);
			        	} catch(Exception e) {}
			        }
			    }

			}
			setFullscreen(true, false);
			mCustomLayoutFrame.setBackgroundColor(0xff000000);
	        mCustomLayoutFrame.addView(view);
	        mCustomLayoutFrame.setVisibility(View.VISIBLE);
	        //mWebView.setVisibility(View.INVISIBLE);
		}
	}
	public final TabList getTabList()
	{
		return mTabList;
	}
	public void addWebViewListener(WebViewEvent listener)
	{
		mWebViewListeners.add(listener);
	}
	public void sendWebViewEvent(final int code,final WebView view,Object param,Object param2)
	{
		if(code==WebViewEvent.WWEVENT_WINDOW_LIST_CHANGED)
			updateCurTab();
		EventInfo ei = null;
		if(view instanceof MyWebView)
		{
			MyWebView ww = (MyWebView)view;
			ei = ww.getEventInfo();
			if(ei == null)
			{
				ei = new EventInfo();
				ei.setWebView(ww);
			}
			switch (code) {
			case WebViewEvent.WWEVENT_TITLE_LOADED:
				ei.title = (String)param;
				break;
			case WebViewEvent.WWEVENT_FAVICON_LOADED:
				ei.favicon = (Bitmap)param;
				break;
			case WebViewEvent.WWEVENT_PAGE_START:
				ei.finishedUrl = null;
				ei.startedUrl = (String)param;
				break;
			case WebViewEvent.WWEVENT_PAGE_FINISH:
				String fin = (String)param;
				if(fin==null)
					ei.finishedUrl = ei.startedUrl;
				else
					ei.finishedUrl = fin;
			default:
				break;
			}
		}
		else if(param instanceof Tab)
		{
			Tab ww = (Tab)param;
			if(ww.getWebView()!=null)
				ei = ww.getWebView().getEventInfo();
			else
				ei = new EventInfo();
			Bookmark b = ww.getCurBookmark();
			if(b!=null)
			{
				ei.startedUrl = b.getUrl();
				ei.title = b.getTitle();
			}
		}
		else if(param==null)
		{
			ei = new EventInfo();
			ei.setWebView(getWebView());
		}
		if(Looper.myLooper() == Looper.getMainLooper())
		{
			sendEventInUiThread(code, ei);
			return;
		}
		final EventInfo event = ei;
		mTopContainer.post(new Runnable() {
			
			@Override
			public void run() {
				sendEventInUiThread(code, event);
			}
		});
	}
	public void sendEventInUiThread(int code,EventInfo ei)
	{
		for(WebViewEvent e:mWebViewListeners.getLiveRefs()){
			e.onWebViewEvent(code, ei);
			if (!st.fl_temp_hide_navigationpanel
				&&mWebView!=null
				&&(mWebView.canGoBack()!=last_tab_back||mWebView.canGoForward()!=last_tab_forward)
				){
				setNavigationPanel();
				last_tab_back = mWebView.canGoBack();
				last_tab_forward = mWebView.canGoForward();

			}
			}
	}
	public final PanelLayout getPanelLayout()
	{
		return mPanelLayout;
	}
	public void setProgressType()
	{
		ViewGroup.LayoutParams lp = mLoadProgress.getLayoutParams();
		boolean ext = Prefs.isExtendedProgress(); 
		lp.height = ext?LayoutParams.WRAP_CONTENT:st.dp2px(this, 4);
		mLoadProgress.setTextEnabled(ext);
		mLoadProgress.setLayoutParams(lp);
	}
	public void stopLoading() {
		if(getWebView()!=null)
		{
			Tab t = getTab();
			t.setLoadProgress(100);
			getWebView().stopLoading();
			setProgress(t);
			sendWebViewEvent(WebViewEvent.WWEVENT_PAGE_FINISH, t.getWebView(), null, null);
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		return super.onCreateOptionsMenu(menu);
	}
	public void setProgress(Tab ww)
	{
		if(ww.isError())
			GlobalHandler.command.sendDelayed(WHAT_UPDATE_PROGRESS, this, 1);
	}
	/** обновление прогроесса загрузки */
	public void updateCurTab()
	{
		Tab t = getTab();
		if(t==null)
			return;
		int nv = t.getLoadProgress()<100&&t.getLoadProgress()>-1?View.VISIBLE:View.GONE;
		mLoadProgress.setVisibility(nv);
		mLoadProgress.setProgress(t.getLoadProgress());
		mLoadProgress.setText(t.loadedResource);
		if(t.isError())
			mErrorLayout.addView(new ErrorLayout(this, t));
		else
			mErrorLayout.removeAllViews();
	}
	@Override
	public void onHandlerEvent(int what) {
		if(what==WHAT_UPDATE_PROGRESS)
		{
			updateCurTab();
		}
		if(what==WHAT_RELOAD_FROM_CACHE)
		{
			Tab ww = getTab();
			if(ww!=null&&ww.isError()&&ww.getWebView()!=null)
			{
				int cacheMode = ww.getWebView().getSettings().getCacheMode();
				if(cacheMode!=WebSettings.LOAD_CACHE_ELSE_NETWORK)
				{
					ww.getWebView().getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
					openUrl(ww.failingUrl, WINDOW_OPEN_SAME);
				}
			}
		}
	}
	public void setMagicButtonAlpha()
	{
		if (mMagicButton!=null){
			mMagicButton.setAlpha(Prefs.get().getInt(Prefs.MAGIC_KEY_ALPHA, 100)/100f);
			ImageView iv = (ImageView) mMagicButton;
			st.setImageColor(this, iv, R.drawable.magic_button);
		}
	}
	public void setNavigationPanelButtonHint(TextView tv)
	{
        switch (tv.getId())
        {
        case R.id.np_left:
        	if (mWebView!=null&&mWebView.canGoBack()){
        		tv.setText(IConst.NAVIGATION_BACK);
        		tv.setHint(st.STR_NULL);
        	} else {
        		tv.setText(st.STR_NULL);
        		tv.setHint(IConst.NAVIGATION_BACK);
        	}
       		break;
        case R.id.np_right:
        	if (mWebView!=null&&mWebView.canGoForward()){
        		tv.setText(IConst.NAVIGATION_FORWARD);
        		tv.setHint(st.STR_NULL);
        	} else {
        		tv.setText(st.STR_NULL);
        		tv.setHint(IConst.NAVIGATION_FORWARD);
        	}
       		break;
        }
	}
	TextView m_tv;
	int size;
	float alpha;
	int col;
	public void setNavigationPanel()
	{
		if (st.pref_navigation !=1){
			if (mNavigationPanel!=null)
				mNavigationPanel.setVisibility(View.GONE);
			return;
		}
		if (mNavigationPanel!=null){
			size = Prefs.get().getInt(Prefs.NAVIGATION_PANEL_SIZE, 40);
			alpha = Prefs.get().getInt(Prefs.NAVIGATION_PANEL_ALPHA, 100);
			col = Prefs.get().getInt(Prefs.NAVIGATION_PANEL_COLOR, Color.RED);
			m_tv = (TextView)mNavigationPanel.findViewById(R.id.np_up);
			if (m_tv!=null){
				m_tv.setAlpha(alpha/100f);
				m_tv.setTextSize(size);
				m_tv.setTextColor(col);
				m_tv.setOnClickListener(m_listener_navigation_panel);
				m_tv.setOnLongClickListener(m_longlistener_navigation_panel);
			}
			m_tv = (TextView)mNavigationPanel.findViewById(R.id.np_down);
			if (m_tv!=null){
				m_tv.setAlpha(alpha/100f);
				m_tv.setTextSize(size);
				m_tv.setTextColor(col);
				m_tv.setOnClickListener(m_listener_navigation_panel);
				m_tv.setOnLongClickListener(m_longlistener_navigation_panel);
			}
			m_tv = (TextView)mNavigationPanel.findViewById(R.id.np_left);
			if (m_tv!=null){
				m_tv.setAlpha(alpha/100f);
				m_tv.setTextSize(size);
				m_tv.setTextColor(col);
				m_tv.setOnClickListener(m_listener_navigation_panel);
				m_tv.setOnLongClickListener(m_longlistener_navigation_panel);
				setNavigationPanelButtonHint(m_tv);
			}
			m_tv = (TextView)mNavigationPanel.findViewById(R.id.np_right);
			if (m_tv!=null){
				m_tv.setAlpha(alpha/100f);
				m_tv.setTextSize(size);
				m_tv.setTextColor(col);
				m_tv.setOnClickListener(m_listener_navigation_panel);
				m_tv.setOnLongClickListener(m_longlistener_navigation_panel);
				setNavigationPanelButtonHint(m_tv);
			}
			
		}
	}
    View.OnClickListener m_listener_navigation_panel = new View.OnClickListener()
    {
        
        @Override
        public void onClick(View v)
        {
            if (mWebView!=null){
                switch (v.getId())
                {
                case R.id.np_up:
                	mWebView.pageUp(true);
               		break;
                case R.id.np_down:
                	mWebView.pageDown(true);
               		break;
                case R.id.np_left:
                	mWebView.goBack();
               		break;
                case R.id.np_right:
                	mWebView.goForward();
               		break;
                }
            }
            setNavigationPanelButtonHint((TextView)v);

        }
    };
    View.OnLongClickListener m_longlistener_navigation_panel = new View.OnLongClickListener()
    {
        
        @Override
    	public boolean onLongClick(View v)
        {
           	Action act;
            switch (v.getId())
            {
            case R.id.np_up:
            case R.id.np_down:
              	act = Action.create(Action.NAVIGATION_PANEL_POS);
               	break;
            case R.id.np_left:
            case R.id.np_right:
              	act = Action.create(Action.HISTORY);
            	break;
           	default:
               	act = Action.create(Action.NAVIGATION_PANEL_POS);
            }
            runAction(act);
//            setNavigationPanelButtonHint((TextView)v);
            return true;
        }
    };
    public void hideMagickAndNavigation()
    {
		mMagicButton.setVisibility(View.GONE);
		mNavigationPanel.setVisibility(View.GONE);
    	
    }
    public void showMagickAndNavigation()
    {
    	if (Prefs.isMagicKeyVisible())
    		mMagicButton.setVisibility(View.VISIBLE);
    	if (Prefs.isNavigationPanelVisible())
    		mNavigationPanel.setVisibility(View.VISIBLE);
    }
    // устанавливает  системную схему приложения
    public void setMainTheme(Activity c)
    {
    	switch (Prefs.getWVBackgroundResourse())
    	{
    	case R.color.black_color:
    		c.setTheme(android.R.style.Theme_Holo_NoActionBar);
    		cur_theme = 1;
    		return;
    	}
		c.setTheme(R.style.AppTheme);
		cur_theme = 0;
    }
    View.OnClickListener mMyButtonClk = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId())
			{
				case R.id.round_button:
					Button but = (Button)v;
					String str = but.getText().toString().trim();
					if (str.compareTo("∆") == 0)
			        	BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_ACTION, Action.create(Action.TO_TOP));
			        //inst.pageUp(true);
			        else
			        	BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_ACTION, Action.create(Action.TO_BOTTOM));
			        //inst.pageUp(true);
					round_btn.setVisibility(View.GONE);
					MyWebView.m_tm_round.cancel();
					break;
			}
		}
	};
    int tempint = 0;
    SameThreadTimer m_tm_minfontsize;
    SeekBar.OnSeekBarChangeListener m_seekbarCngListener = new SeekBar.OnSeekBarChangeListener() {
		
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			m_tm_minfontsize = new SameThreadTimer(2000,0)
			{
			    @Override
			    public void onTimer(SameThreadTimer timer)
			    {
			        if (sb_minfont!=null
				        	&&!bshowSeekbarMinFontSize
				           )
			        	sb_minfont.setVisibility(View.GONE);
			        m_tm_minfontsize.cancel();
			    }
			};
			m_tm_minfontsize.start();
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			tempint = sb_minfont.getProgress();
			Prefs.get().edit().putInt(Prefs.MIN_FONT, tempint).commit();
			getWebView().getSettings().setMinimumFontSize(tempint);
			
		}
	};
	// скрывает круглую кнопку для жестов свайп вверх/вниз
    public void hideRoundButton()
    {
		if (round_btn!=null)
			round_btn.setVisibility(View.GONE);
  	}
    public void setLoadProgressColor()
    {
    	if (mLoadProgress!=null) {
    		int col = Prefs.getColorExtendedProgress();
    		Drawable dr = getResources().getDrawable(col, getResources().newTheme());
    		LayerDrawable progressDrawable = (LayerDrawable)mLoadProgress.getProgressDrawable();
    		ClipDrawable cd = new ClipDrawable(dr, Gravity.LEFT,ClipDrawable.HORIZONTAL);
    		progressDrawable.setDrawableByLayerId(android.R.id.progress, cd);
    		// если откоментить, то фон рисуется сразу полный!!!
    		//mLoadProgress.setProgressDrawable(dr);
    		
    		int tcol = Color.WHITE;
    		switch (col)
    		{
    		case R.color.gray_color:
        		tcol = Color.GREEN;
    			break;
    		case R.color.green_color:
        		tcol = Color.MAGENTA;
    			break;
    		case R.color.yellow_color:
        		tcol = Color.MAGENTA;
    			break;
    		}
    		
    		mLoadProgress.mTextPaint.setColor(tcol);
    	}
  	}

}