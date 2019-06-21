package com.jbak.superbrowser.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Browser;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jbak.reverseEngine.BrowserContract;
import com.jbak.superbrowser.ActArray;
import com.jbak.superbrowser.Action;
import com.jbak.superbrowser.Bookmark;
import com.jbak.superbrowser.BrowserApp;
import com.jbak.superbrowser.Db;
import com.jbak.superbrowser.IConst;
import com.jbak.superbrowser.MainActivity;
import com.jbak.superbrowser.Prefs;
import com.mw.superbrowser.R;
import com.jbak.superbrowser.SearchAction;
import com.jbak.superbrowser.stat;
import com.jbak.superbrowser.WebViewEvent;
import com.jbak.superbrowser.TabList;
import com.jbak.superbrowser.adapters.BookmarkFolderAdapter;
import com.jbak.superbrowser.panels.PanelMainMenu;
import com.jbak.superbrowser.panels.PanelQuickTools;
import com.jbak.superbrowser.panels.PanelSearch;
import com.jbak.superbrowser.panels.PanelSettings;
import com.jbak.superbrowser.panels.PanelTitle;
import com.jbak.superbrowser.panels.PanelUrlEdit;
import com.jbak.superbrowser.recycleview.PanelButtonRecyclerAdapter;
import com.jbak.superbrowser.recycleview.RecyclerViewEx;
import com.jbak.superbrowser.search.SearchSystem;
import com.jbak.superbrowser.ui.themes.MyTheme;
import com.jbak.ui.UIUtils;
import com.jbak.utils.ObjectKeyValues;


public class MainPanel extends LinearLayoutEx implements OnAction,WebViewEvent,OnClickListener,OnLongClickListener, OnItemClickListener,OnItemLongClickListener,BrowserApp.OnGlobalEventListener {

	public static final int MODE_START_PAGE = 0;
	public static final int MODE_NORMAL = 1;

	public static final int STATE_WINDOWS_SHOW = 0;
	public static final int STATE_WINDOWS_RESTORE = 1;
	public static final int STATE_WINDOWS_NONE = 2;

	
	int mLayoutType = Prefs.TYPE_LAYOUT_NORMAL;
	PanelQuickTools mPanelNavigation;
	PanelSearch mPanelSearch;
	HorizontalPanel mPanelTools;
	BookmarkHorizontalPanel mPanelBookmarks;
	RecyclerViewEx mGridTools;
	ContentObserver mContentObserver;
	static boolean mPortrait=false;
	int mWindowsState = STATE_WINDOWS_SHOW;
	public String mTitleText;
	BookmarkFolderAdapter mBookmarkPanelAdapter;
	long mHomescreenFolderId = IConst.ROOT_FOLDER_ID;
	int mMode = -1;
	PanelUrlEdit mUrlEdit;
	PanelTitle mPanelTitle;
	PanelSettings mNormalPanelSettings;
	PanelSettings mStartScreenPanelSettings;
	LinearLayout.LayoutParams lpWrap;
	LinearLayout.LayoutParams lpStrecth;
	static ObjectKeyValues<Integer, Integer> PANELS_NAMES = new ObjectKeyValues<Integer, Integer>
	(
			PanelLayout.PANEL_BOOKMARKS,R.string.panelBookmarks,
			PanelLayout.PANEL_SEARCH_HISTORY,R.string.panelSearchHistory
			);
	static ObjectKeyValues<Integer, Integer> PANELS_NAMES_START = new ObjectKeyValues<Integer, Integer>
		(	PanelLayout.PANEL_SEARCH_HISTORY,R.string.panelSearchHistory
			);
	public MainPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context,Prefs.getPanelLayoutType());
	}
	public MainPanel(Context context) {
		super(context);
		init(context,Prefs.getPanelLayoutType());
	}
	public MainPanel(Context context,int layoutType) {
		super(context);
		init(context,layoutType);
	}
	private void init(Context c,int layoutType)
	{
		setPortrait(getResources().getConfiguration());
		lpStrecth = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,0);
		lpStrecth.weight = 1f;
		lpWrap = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		mNormalPanelSettings = loadNormalPanelSettings();
		mStartScreenPanelSettings = loadStartPanelSettings();
		mHomescreenFolderId = Prefs.get().getLong(Prefs.HOMESCREEN_FOLDER, IConst.ROOT_FOLDER_ID);
		setOrientation(VERTICAL);
		mLayoutType = layoutType;
		BrowserApp.INSTANCE.addGlobalListener(this);
		mUrlEdit = new PanelUrlEdit(c);
		mPanelNavigation = new PanelQuickTools(c, PanelQuickTools.TYPE_TOOLS_MAIN);
		mPanelBookmarks = new BookmarkHorizontalPanel(c,mHomescreenFolderId);
		mGridTools = new RecyclerViewEx(c,RecyclerViewEx.TYPE_GRID)
		{
			public int getItemWidth() 
			{
				return PanelButton.getMinWidth(getContext(), PanelButton.TYPE_BUTTON_NORMAL);
			}
		};
		mPanelSearch = new PanelSearch(c);
		mPanelSearch.setOnActionListener(this);
		mPanelTools = new HorizontalPanel(c);
		removeAllViews();
		mPanelTitle = new PanelTitle(getMain());
		mPanelTools.setOnActionListener(this);
		Uri regUri = Browser.BOOKMARKS_URI;
		if(Build.VERSION.SDK_INT>=15)
			regUri = BrowserContract.Bookmarks.CONTENT_URI;

		mContentObserver = new ContentObserver(new Handler()) {
			@Override
			public void onChange(boolean selfChange) {
				onChange(selfChange,null);
			}
			 public void onChange(boolean selfChange, Uri uri) {
				mPanelBookmarks.post(new Runnable() {
					
					@Override
					public void run() {
						updateBookmarks();
					}
				});
			 }
		};
		getMain().getContentResolver().registerContentObserver(regUri, true, mContentObserver);

		mGridTools.setOnUnusedSpaceClickListener(getMain());
		mPanelNavigation.setOnActionListener(this);
		mPanelBookmarks.setOnActionListener(this);
		mPanelBookmarks.setItemLongClickListener(getMain());
		mUrlEdit.getToolsPanel().setItemLongClickListener(getMain());
		mPanelNavigation.setLongClickListener(getMain());
		getMain().addWebViewListener(this);
		setPanelsToLayout();
	}
	
	private void setPanelsToLayout() {
		mLayoutType = Prefs.getPanelLayoutType();
		removeAllViews();
		if(mMode==MODE_START_PAGE)
		{
			mPanelBookmarks.setReverseLayout(mLayoutType!=Prefs.TYPE_LAYOUT_NORMAL);
			mPanelBookmarks.setType(BookmarkHorizontalPanel.TYPE_GRID);
			addView(mPanelBookmarks,lpStrecth);
			addView(mUrlEdit,lpWrap);
			addView(mPanelTools,lpWrap);
		}
		else
		{
			mPanelBookmarks.setReverseLayout(false);
			mGridTools.setMaxHeight(Integer.MAX_VALUE);
			if(mLayoutType==Prefs.TYPE_LAYOUT_NORMAL)
			{
				addView(mPanelTitle,lpWrap);
				addView(mUrlEdit,lpWrap);
				addView(mPanelTools,lpWrap);
				addView(mPanelNavigation,lpWrap);
				addView(mGridTools,lpStrecth);
				addView(mPanelBookmarks,lpWrap);
			}
			else
			{
				setGravity(Gravity.BOTTOM);
				addView(mPanelBookmarks,lpWrap);
				addView(mPanelTitle,lpWrap);
				addView(mGridTools,lpStrecth);
				addView(mPanelTools,lpWrap);
				addView(mUrlEdit,lpWrap);
				addView(mPanelNavigation,lpWrap);
				
			}
			mPanelBookmarks.setType(BookmarkHorizontalPanel.TYPE_HORZ_LINEAR);
			//mGridTools.setStackFromEnd(mLayoutType!=Prefs.TYPE_LAYOUT_NORMAL);
			//mGridTools.setReverseLayout(mLayoutType!=Prefs.TYPE_LAYOUT_NORMAL);
		}
		setPanelFromSettings(mPanelSearch, PanelLayout.PANEL_SEARCH_HISTORY);
		if(mMode==MODE_NORMAL)
			setPanelFromSettings(mPanelBookmarks, PanelLayout.PANEL_BOOKMARKS);
		for(int i=getChildCount()-1;i>=0;i--)
		{
			View v = getChildAt(i);
			if(v instanceof WebViewEvent)
				getMain().addWebViewListener((WebViewEvent)v);
		}
		
		mPanelTitle.setTheme();
		MyTheme.get().setViews(MyTheme.ITEM_MAIN_PANEL_BACKGROUND,this, mPanelBookmarks,mGridTools);
		MyTheme.get().setViews(MyTheme.ITEM_HORIZONTAL_PANEL_BACKGROUND, mPanelNavigation,mPanelSearch,mUrlEdit,mPanelTools);

		requestLayout();
	}
// окно при выходе и входе
	void createToolsPanel()
	{
		//ActArray ar = new ActArray(Action.HISTORY,Action.OPENFILE,Action.SETTINGS,Action.EXIT);
		ActArray ar = new ActArray();
		if(mWindowsState==STATE_WINDOWS_RESTORE)
			ar.add(Action.create(Action.START_APP_LAST_TAB));
//		else if(mWindowsState==STATE_WINDOWS_SHOW)
//			ar.add(Action.create(Action.WINDOWS_LIST));
//		else{}
		ar.add(Action.create(Action.HISTORY));
		ar.add(Action.create(Action.IMPORT));
		ar.add(Action.create(Action.BOOKMARKS));
		if(Prefs.getHome()!=null)
			ar.add(Action.GO_HOME);
		ar.add(Action.create(Action.MAIN_SETTINGS));
		ar.add(Action.create(Action.EXIT));
		ar.add(Action.create(Action.OPENFILE));
		addVoiceSearch(ar);
		mPanelTools.setActions(ar);
	}
	private void updateBookmarks() {
		if(mPanelBookmarks!=null)
		{
			mPanelBookmarks.updateAdapter();
		}
	}
	public void setMode(int mode)
	{
		setMode(mode,false);
	}
	public final void checkShowTitle()
	{
		UIUtils.showViews(!getMain().hasSoftKeyboard()&&!TextUtils.isEmpty(mPanelTitle.getTitleText()), mPanelTitle);
	}
	private void setMode(int mode,boolean forceChange)
	{
		if(mode==MODE_START_PAGE)
		{
			mTitleText = null;
			mPanelTitle.setText(mTitleText);
			mUrlEdit.setUrl(null);
			UIUtils.showViews(true, mPanelTools);
			UIUtils.showViews(false, mPanelNavigation,mGridTools);
		}
		else if(mode==MODE_NORMAL)
		{
			UIUtils.showViews(true, mPanelNavigation,mGridTools);
			UIUtils.showViews(false, mPanelTools);
		}
		checkShowTitle();
		if(mMode==mode&&!forceChange)
			return;
		mMode = mode;
		setPanelsToLayout();
		createActionsToolsGrid();
		mPanelSearch.getPanel().forceUpdate();
		mPanelNavigation.setActions();
		mUrlEdit.createToolsActions();
		mPanelTools.forceUpdate();
	}
	public Action getActionStopOrRefresh()
	{
		if(getMain().getLoadProgress().getVisibility()==View.VISIBLE)
			return Action.create(Action.STOP);
		else
			return Action.create(Action.REFRESH);

	}
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public static int defTextZoom(WebSettings ws)
	{
		try{
		if(Build.VERSION.SDK_INT>=14)
			return ws.getTextZoom();
		else
			return Prefs.OLD_SIZES.getKeyByValue(ws.getTextSize(),100);
		}
		catch(Throwable e)
		{
			
		}
		return -1;
	}
	public final void addVoiceSearch(ActArray ar)
	{
		if(stat.isVoiceSearchExist(getMain()))
			ar.add(Action.create(Action.VOICE_SEARCH,mUrlEdit));
	}
	/** пункты главного меню */
	@SuppressLint("NewApi")
	public void createActionsToolsGrid()
	{
		ActArray ar = new ActArray();
		ar.addAll(PanelMainMenu.getMainMenuPanelActions());
		// определяем количество отображаемых пунктов меню, 
		// и вставляем обязательные в середину списка
		int cnt = 0;
		if (ar!=null)
			cnt = ar.size();
		int pos_default_paragrapf = 0;
		if (cnt == 0)
			ar = getMainMenuActionAlways();
		else
			pos_default_paragrapf = cnt/3;
		ActArray arout = new ActArray();
		for (int i=0;i<ar.size();i++){
			if (i==pos_default_paragrapf){
				arout.addAll(getMainMenuActionAlways());
			}
			switch (ar.get(i).command)
			{
			case Action.FONT_SCALE_SETTINGS:
				if(getMain()!=null&&getMain().getWebView()!=null)
				{
					int z = defTextZoom(getMain().getWebView().getSettings());
					if(z>-1)
						arout.add(Action.create(Action.FONT_SCALE_SETTINGS,z));
				}
				continue;
			case Action.MIN_FONT_SIZE:
				arout.add(Action.MIN_FONT_SIZE);
				continue;
			case Action.TRANSLATE_LINK:
//				arout.add(new SearchAction(SearchSystem.CMD_TRANSLATE_URL, R.string.act_translate, R.drawable.translate));
				arout.add(Action.TRANSLATE_LINK);
				continue;
			case Action.CLEAR_TEXT:
				arout.add(Action.CLEAR_TEXT);
				continue;
			case Action.IMPORT:
				arout.add(Action.IMPORT);
				continue;
			case Action.EXPORT:
				arout.add(Action.EXPORT);
				continue;
			case Action.BOOKMARKS:
				arout.add(Action.BOOKMARKS);
				continue;
			case Action.ADD_BOOKMARK:
				arout.add(Action.ADD_BOOKMARK);
				continue;
			case Action.HISTORY:
				arout.add(Action.HISTORY);
				continue;
			case Action.SHARE_URL:
				arout.add(Action.SHARE_URL);
				continue;
			case Action.CLEAR_DATA:
				arout.add(Action.CLEAR_DATA);
				continue;
			case Action.TAB_LIST:
				arout.add(Action.TAB_LIST);
				continue;
			case Action.CLOSE_TAB:
				arout.add(Action.CLOSE_TAB);
				continue;
			case Action.NEW_TAB:
				arout.add(Action.NEW_TAB);
				continue;
			case Action.DOWNLOAD_LIST:
				arout.add(Action.DOWNLOAD_LIST);
				continue;
			case Action.SOURCE_CODE:
				arout.add(Action.SOURCE_CODE);
				continue;
			case Action.GO_HOME:
				arout.add(Action.GO_HOME);
				continue;
			case Action.SAVEFILE:
				if(Build.VERSION.SDK_INT>=11)
					arout.add(Action.create(Action.SAVEFILE).setText(R.string.act_save_page));
				continue;
			case Action.OPENFILE:
				arout.add(Action.OPENFILE);
				continue;
			case Action.QUICK_SETTINGS:
				arout.add(Action.QUICK_SETTINGS);
				continue;
			case Action.MAIN_SETTINGS:
				arout.add(Action.MAIN_SETTINGS);
				continue;
			case Action.EXIT:
				arout.add(Action.EXIT);
				continue;
			case Action.CODEPAGE:
				arout.add(Action.CODEPAGE);
				continue;
			case Action.VOICE_SEARCH:
				addVoiceSearch(arout);
				continue;
			case Action.SEARCH_ON_PAGE:
				arout.add(Action.SEARCH_ON_PAGE);
				continue;
			case Action.COPY_ALL_OPEN_URL:
				arout.add(Action.COPY_ALL_OPEN_URL);
				continue;
			}
		}
//		BrowserApp.pluginServer.getPluginActions(arout, Plugin.WINDOW_MAIN_MENU,this);
//		arout.add(new SearchAction(SearchSystem.CMD_TRANSLATE_URL, R.string.act_translate, R.drawable.translate));

		
		if(mLayoutType==Prefs.TYPE_LAYOUT_BOTTOM)
			mGridTools.setReverseLayout(true);
		else
			mGridTools.setReverseLayout(false);
		PanelButtonRecyclerAdapter pba = new PanelButtonRecyclerAdapter(arout, PanelButton.TYPE_BUTTON_NORMAL);
		pba.setOnClickLisener(this);
		pba.setOnLongClickListener(this);
			
		mGridTools.setAdapter(pba);
	}
	// пункты главного меню, которые должны выдаваться всегда
	public final ActArray getMainMenuActionAlways()
	{
		ActArray ar = new ActArray();
		ar.add(Action.create(Action.EXIT));
		ar.add(Action.create(Action.MAINMENU_SETTING));
		ar.add(Action.create(Action.ABOUT));
		if (!stat.isAppInstal(getContext(), stat.PACKAGE_MWCOSTS))
			ar.add(Action.create(Action.INSTALL_MWCOSTS));
		if (!stat.isAppInstal(getContext(), stat.PACKAGE_MWSHARE2SAVE))
			ar.add(Action.create(Action.INSTALL_MWSHARE2SAVE));
		if (!stat.isAppInstal(getContext(), stat.PACKAGE_JBAK2KEYBOARD))
			ar.add(Action.create(Action.INSTALL_JBAK2KEYBOARD));
		ar.add(Action.HELP);
		return ar;
	}
	public final MyWebView getWebView()
	{
		return getMain().getWebView();
	}
	@Override
	public void onAction(Action act) {
		if(act.command==Action.START_APP_LAST_TAB)
		{
			show(false);
			Db.getWindowTable().deleteWindow(getMain().getTab().windowId);
			getMain().tabRestoreLast();
			return;
		}
		else if(act.command==Action.ITEM_TEXT)
		{
			String text = act.getText(getMain());
			mUrlEdit.setTextAndShowKeyboard(text);
			return;
		}
		getMain().runAction(act);
	}
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View v, int arg2,long arg3) {
		getMain().onLongClick(v);
		return true;
	}
	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {
		onClick(v);
	}
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		getMain().getContentResolver().unregisterContentObserver(mContentObserver);
		if(mBookmarkPanelAdapter!=null)
			mBookmarkPanelAdapter.destroy();
	}
	public final PanelUrlEdit getUrlPanel()
	{
		return mUrlEdit;
	}
	public static final int EVENT_CHANGE_NAVIGATION = 1;
	
	public void setTitle(String title)
	{
		mPanelTitle.setText(title);
	}
	
	public final int getMode()
	{
		return mMode;
	}
// запуск главной панели
	public void show(boolean show)
	{
		UIUtils.showViews(show,this);
		if(show)
		{
			checkShowBookmarksPanel();
			createActionsToolsGrid();
			checkShowTitle();
			if(TextUtils.isEmpty(mUrlEdit.getUserText()))
				mUrlEdit.setText(mUrlEdit.getUrl());
//			ed.clearFocus()
//			ed.setSelection(0);
		}
		else
		{
			mUrlEdit.hideKeyboard();
		}
		
	}
	void setHomescreenFolder()
	{
		mHomescreenFolderId = mBookmarkPanelAdapter.getCurrentFolderId();
		Prefs.get().edit().putLong(Prefs.HOMESCREEN_FOLDER, mHomescreenFolderId).commit();
//!!!		UIUtils.showViews(false,mButtonSetHomescreenFolder);
	}
	public void setWindowsState(int windowsState)
	{
		mWindowsState = windowsState;
		createToolsPanel();
	}
	@Override
	public void onGlobalEvent(int code, Object param) {
		if(code==BrowserApp.GLOBAL_BOOKMARKS_CHANGED)
			updateBookmarks();
		if(code==BrowserApp.GLOBAL_SETTINGS_CHANGED)
		{
			String pref = (String)param;
			if(Prefs.MAIN_PANEL_LAYOUT.equals(pref))
			{
				removeAllViews();
				setPanelsToLayout();
				setPanelFromSettings(mPanelSearch, PanelLayout.PANEL_SEARCH_HISTORY);
				setPanelFromSettings(mPanelBookmarks, PanelLayout.PANEL_BOOKMARKS);
			}
			else if(Prefs.PANEL_SETTINGS_MAIN_MENU.equals(pref))
			{
				mNormalPanelSettings.reload();
				if(mMode==MODE_NORMAL)
					refresh();
			}
			else if(Prefs.PANEL_SETTINGS_START.equals(pref))
			{
				mStartScreenPanelSettings.reload();
				if(mMode==MODE_START_PAGE)
					refresh();
			}
		}
	}
	public void refresh()
	{
		setMode(mMode,true);
	}
	public View[] getPanels()
	{
		return new View[]{mUrlEdit.getToolsPanel(),mPanelNavigation,mPanelBookmarks,mPanelSearch};
	}
	public View[] getGrids()
	{
		return new View[]{mGridTools};
	}
	public final String getUrl() {
		return mUrlEdit.getUrl();
	}
	void checkShowBookmarksPanel()
	{
		boolean showBookmarks = !getMain().hasSoftKeyboard()&&(mMode==MODE_START_PAGE||isPanelVisible(PanelLayout.PANEL_BOOKMARKS));
		UIUtils.showViews(showBookmarks, mPanelBookmarks);
		boolean showSearch = isPanelVisible(PanelLayout.PANEL_SEARCH_HISTORY)&&mPanelSearch.getPanel().getRealAdapter().getItemCount()>0;
		UIUtils.showViews(showSearch, mPanelSearch);
	}
	public final int getLayoutType()
	{
		return mLayoutType;
	}
	boolean mKeyboradShown = false;
	@Override
	public void onWebViewEvent(int code, EventInfo info) {
		boolean curWebWindow = false;
		if(info!=null)
			curWebWindow = info.getWebView()==getMain().getWebView();
		switch (code) {
			case WWEVENT_PAGE_START:
			case WWEVENT_PAGE_FINISH:
				if(curWebWindow)
					setMode(MODE_NORMAL);
				break;
			case WWEVENT_TITLE_LOADED:
				setTitle(info.title);
				break;
			case WWEVENT_SOFT_KEYBOARD_HIDDEN:
			case WWEVENT_SOFT_KEYBOARD_VISIBLE:
				checkShowBookmarksPanel();
				mKeyboradShown = code==WWEVENT_SOFT_KEYBOARD_VISIBLE;
				boolean showViews = !mKeyboradShown;
				if(mMode==MODE_NORMAL)
					UIUtils.showViews(showViews, mPanelTitle,mPanelNavigation,mGridTools);
				else if(mMode==MODE_START_PAGE)
					UIUtils.showViews(!mKeyboradShown, mPanelTools);
				setMainPanelGravity(mKeyboradShown);
				break;
			case WWEVENT_WINDOW_LIST_CHANGED:
				TabList ww =  getMain().getTabList();
				if(ww.getCurrent()==null||ww.getCurrent().getCurBookmark()==null)
				{
					setMode(MODE_START_PAGE);
				}
				else
					setMode(MODE_NORMAL);
				break;
		}
	}
	public void setMainPanelGravity(boolean keyboardShown)
	{
		if(!mPortrait&&keyboardShown)
		{
			int h = getHeight();
			int hChild = UIUtils.getChildsHeight(this);
			if(hChild<h)
			{
				setMaxHeight(h);
//				setGravity(Gravity.BOTTOM);
			}
		}
		else
		{
			setMaxHeight(Integer.MAX_VALUE);
//			setGravity(Gravity.BOTTOM);
		}
	}
	public TextView getTitle() {
		return mPanelTitle.getTitle();
	}
	public final PanelSettings getPanelSettings()
	{
		return mMode==MODE_NORMAL?mNormalPanelSettings:mStartScreenPanelSettings;
	}
	public final boolean isPanelVisible(int panelId)
	{
		return getPanelSettings().isPanelVisible(panelId);
	}
	public static PanelSettings loadNormalPanelSettings()
	{
		return new PanelSettings(Prefs.PANEL_SETTINGS_MAIN_MENU, PANELS_NAMES, new PanelSettings.SetDefaultPanelSetting() {
			
			@Override
			public void setDefaultPanelSetting(PanelSetting ps) {
				if(ps.id==PanelLayout.PANEL_BOOKMARKS)
					ps.visible=Prefs.getInt(Prefs.SHOW_BOOKMARKS_MAIN_MENU, 1)==1;
				else
					ps.visible = true;
				ps.top = true;
			}

			@Override
			public void setDefaultExtraSettings(PanelSetting ps) {
			}
		});
	}
	public void setPanelFromSettings(View panel,int panelId)
	{
		PanelSetting set = getPanelSettings().getPanelSetting(panelId);
		if(set==null)
			return;
		if(panel.getParent() instanceof ViewGroup)
		{
			ViewGroup p = (ViewGroup) panel.getParent();
			p.removeView(panel);
		}
		if(set.top)
			addView(panel,0,lpWrap);
		else
			addView(panel,lpWrap);
	}
	public static PanelSettings loadStartPanelSettings()
	{
		return new PanelSettings(Prefs.PANEL_SETTINGS_START, PANELS_NAMES_START, 
				
				new PanelSettings.SetDefaultPanelSetting() {
			
			@Override
			public void setDefaultPanelSetting(PanelSetting ps) {
				ps.visible = true;
				ps.top = true;
			}
			@Override
			public void setDefaultExtraSettings(PanelSetting ps) {
				
			}
		});
	}
	@Override
	public boolean onLongClick(View v) {
		getMain().onLongClick(v);
		return true;
	}
	@Override
	public void onClick(View v) {
		if(v instanceof PanelButton)
			onAction(((PanelButton)v).getAction());
		else if(v instanceof BookmarkView)
		{
			Action bm = Action.create(Action.ACTION_BOOKMARK, (Bookmark)v.getTag());
			getMain().runAction(bm);
		}
		else
			getMain().onClick(v);
	}
	MainActivity getMain() {
		return (MainActivity)getContext();
	}
	public String getText() {
		return mUrlEdit.getText();
	}
	protected void setPortrait(Configuration config) {
		mPortrait=config.orientation==Configuration.ORIENTATION_PORTRAIT;
	}
	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setPortrait(newConfig);
		setMainPanelGravity(mKeyboradShown);
	}
}
