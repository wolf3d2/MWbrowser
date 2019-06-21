package com.jbak.superbrowser;

import com.mw.superbrowser.R;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import ru.mail.mailnews.st;

import com.jbak.reverseEngine.BrowserContract;
import com.jbak.superbrowser.Action.FileOpenAction;
import com.jbak.superbrowser.BrowserApp.OnGlobalEventListener;
import com.jbak.superbrowser.adapters.BookmarkAdapter;
import com.jbak.superbrowser.adapters.BookmarkAdapter.CursorBookmarkAdapter;
import com.jbak.superbrowser.adapters.BookmarkFolderAdapter;
import com.jbak.superbrowser.adapters.FileAdapter;
import com.jbak.superbrowser.adapters.SettingsAdapter;
import com.jbak.superbrowser.adapters.SettingsBookmark;
import com.jbak.superbrowser.recycleview.BookmarkViewRecyclerAdapter;
import com.jbak.superbrowser.recycleview.RecyclerViewEx;
import com.jbak.superbrowser.ui.HorizontalPanel;
import com.jbak.superbrowser.ui.LoadBitmapInfo;
import com.jbak.superbrowser.ui.MenuPanelButton;
import com.jbak.superbrowser.ui.MenuPanelButton.MenuBookmark;
import com.jbak.superbrowser.ui.MyWebView;
import com.jbak.superbrowser.ui.OnAction;
import com.jbak.superbrowser.ui.PanelButton;
import com.jbak.superbrowser.ui.dialogs.DialogImport;
import com.jbak.superbrowser.ui.dialogs.MenuSettingBookmarks;
import com.jbak.superbrowser.ui.dialogs.ThemedDialog;
import com.jbak.superbrowser.ui.themes.DesktopTheme;
import com.jbak.superbrowser.ui.themes.MyTheme;
import com.jbak.superbrowser.utils.DbClear;
import com.jbak.ui.ConfirmOper;
import com.jbak.ui.CustomPopup;
import com.jbak.utils.DbUtils;
import com.jbak.utils.ObjectKeyValues;

public class BookmarkActivity extends Activity implements IConst, OnAction,OnGlobalEventListener{
	public static String EXTRA_TYPE = "type";
	public static final int TYPE_HISTORY = 1;
	public static final int TYPE_BOOKMARKS = 2;
	public static final int TYPE_WINDOOW_HISTORY = 3;
	public static final int TYPE_WINDOWS = 4;
	public static final int TYPE_SHORTCUTS = 5;
	public static final int TYPE_TEST = 6;
	public static final int TYPE_FILE_SELECT = 7;
	public static final int TYPE_DOWNLOADS = 8;
	public static final int TYPE_DIR_SELECT = 9;
	public static final int TYPE_BOOKMARK_FOLDER_SELECT = 10;
	public static final int TYPE_SETTINGS = 11;
	public static final int TYPE_CLEAR_DATA = 12;
	public static final int TYPE_CLEAR_ON_EXIT = 13;
	public static final int TYPE_VIDEO_HISTORY = 14;
	public static final int TYPE_SAVED_PAGES = 15;
	public static final int TYPE_CLOSED_TAB = 16;
	public static String EXTRA_WINDOW_ID = "windowId";
	public static String EXTRA_CURPOS = "curPos";
	public static String EXTRA_BOOKMARK = "bookmark";
	public static String EXTRA_CUR_WINDOW_ID = "curWindowId";
	public static String EXTRA_CLOSE = "closeWindow";
	public static String EXTRA_CLOSED_TAB = "closedTab";
	public static String EXTRA_TITLE = IConst.TITLE;
	private static OnAction gListener;
	private OnAction mListener;
	View mProgress;
	int mCurWindowId = -1;
	int mCurPos = -1;
	int mType;
	boolean mShowOpenedWindows = true;
	// открываем недавно закрытые 
	static boolean mShowCloseTab = false;
	HorizontalPanel mPanel;
	boolean mSelectFileForResult = false;
	TextView mTitle;
	public static final String ADD_UNIT_ID = "ca-app-pub-5995922978448325/9783622824";
	FrameLayout mAdFrame;
	static Tab gWebWindow;
	static ArrayList<Bookmark> gSetArray;
	static BookmarkActivity inst;
	static MainActivity gMain;
	private WeakReference<MainActivity> mMain;
	RecyclerViewEx mList;
	public static ObjectKeyValues<Integer, Integer> TITLES = new ObjectKeyValues<Integer, Integer>
	(
		TYPE_BOOKMARKS,R.string.act_bookmarks,
		TYPE_HISTORY,R.string.act_history,	
		TYPE_SETTINGS,R.string.act_settings,
		TYPE_VIDEO_HISTORY,R.string.act_video_history,	
		TYPE_CLEAR_ON_EXIT,R.string.clear_on_exit,
		TYPE_WINDOWS,R.string.act_windows,
		TYPE_CLEAR_DATA,R.string.act_clear_data,
		TYPE_FILE_SELECT,R.string.selectFile,
		TYPE_DIR_SELECT,R.string.act_select_folder,
		TYPE_BOOKMARK_FOLDER_SELECT,R.string.act_select_folder,
		TYPE_SAVED_PAGES,R.string.act_saved_pages_history
	);
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		inst=this;
		MyTheme.get().onCreateActivity(this);
		super.onCreate(savedInstanceState);
		View v = getLayoutInflater().inflate(R.layout.activity_bookmarks, null);
		MyTheme.get().setViews(MyTheme.ITEM_ACTIVITY_BACKGROUND, v);
		setContentView(v);
		if(gMain!=null)
		{
			setMain(gMain);
			gMain = null;
		}
		mTitle = (TextView) findViewById(R.id.title);
		mAdFrame = (FrameLayout)findViewById(R.id.adframe);
		mProgress = findViewById(R.id.progressLoad);
		mType = getIntent().getIntExtra(EXTRA_TYPE, TYPE_HISTORY);
		if(Intent.ACTION_GET_CONTENT.equals(getIntent().getAction()))
		{
			mSelectFileForResult = true;
			mType = TYPE_FILE_SELECT;
		}
		mListener = gListener;
		gListener = null;
		ViewGroup vg = (ViewGroup)findViewById(R.id.recyclerContainer);
		mList = (RecyclerViewEx) LayoutInflater.from(this).inflate(R.layout.recycle_scrollbar, null);
		mList.setType(RecyclerViewEx.TYPE_VERTICAL_LIST);
//		mList.setListTwoColumnsOnWideScreen(true);
//		mList.setButtonsType(PanelButton.TYPE_BUTTON_NORMAL);
		vg.addView(mList);
		mList.setOnUnusedSpaceClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
//		LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
//		mList.setLayoutManager(lm);
		if(MyTheme.get() instanceof DesktopTheme)
			mList.setBackgroundColor(0x00000000);
		mList.setListTwoColumnsOnWideScreen(true);
		mPanel = (HorizontalPanel)findViewById(R.id.horizontal_panel);
		mPanel.setButtonsType(PanelButton.TYPE_BUTTON_TEXT_ONELINE);
		//mPanel
		MyTheme.get().setViews(MyTheme.ITEM_TITLE, mTitle);
		MyTheme.get().setViews(MyTheme.ITEM_HORIZONTAL_PANEL_BACKGROUND, mPanel);
		mCurPos = getIntent().getIntExtra(EXTRA_CURPOS, -1);
		BrowserApp.INSTANCE.addGlobalListener(this);
		createAndSetAdapter();
		
	}
	private void createAndSetAdapter() {
		BookmarkAdapter ba = initListView();
		if(ba==null)
			return;
		//text = text+" ("+ba.getCount()+")";
		String text = getIntent().getStringExtra(EXTRA_TITLE);
		int res = TITLES.getValueByKey(mType,R.string.act_history);
		if(TextUtils.isEmpty(text))
			text = getString(res);
		setTitleText(text);
		if(ba instanceof OnClickListener)
			getRecyclerAdapter().setOnClickListener((OnClickListener)ba);
		else if(mType!=TYPE_FILE_SELECT&&mType!=TYPE_DIR_SELECT)
		{
			getRecyclerAdapter().setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent in = new Intent();
					if(mType==TYPE_WINDOOW_HISTORY)
					{
						int pos = (Integer)((Bookmark)v.getTag()).param;
						in.putExtra(EXTRA_CURPOS, pos);
						setResult(RESULT_OK,in);
					}
					else if(v.getTag() instanceof Bookmark)
					{
						Bookmark bm = (Bookmark)v.getTag();
						boolean runBookmark = true;
						if(mType==TYPE_SAVED_PAGES&&bm.param instanceof Long)
						{
							String filename = Db.getExtHistory().getFileNameById((Long)bm.param);
							if(!TextUtils.isEmpty(filename))
							{
								File f = new File(filename);
								if(f.exists())
								{
									Action a = Action.create(Action.OPENFILE, f);
									((FileOpenAction)a).setMime(UrlProcess.MIME_MHT);
									BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_ACTION, a);
									runBookmark = false;
								}
							}
						}
						if(runBookmark)
							BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_ACTION, Action.create(Action.ACTION_BOOKMARK, v.getTag()));
					}
					else if(v.getTag() instanceof Tab) {
						Tab tt = (Tab)v.getTag();
						if (tt!=null) {
							if (mShowCloseTab) {
								// недавно закрытые вкладки - открывает только последний
								// урл закрытой вкладки
								String url = tt.getUrl();
								if (url!=null) {
									Action act = Action.create(Action.NEW_TAB, url);
									getMain().openUrl((String)act.param,act.command);
								}
							} else
								BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_ACTION, Action.create(Action.OPEN_TAB, ((Tab)v.getTag()).windowId));
								
						}
					}
					finish();
				}
			});
		}
		if(mType!=TYPE_FILE_SELECT&&mType!=TYPE_DIR_SELECT&&!(ba instanceof SettingsAdapter))
		{
			getRecyclerAdapter().setOnLongClickListener(new OnLongClickListener() {
	
				@Override
				public boolean onLongClick(View v) {
					if(v.getTag() instanceof Bookmark)
					{
						Bookmark bm = (Bookmark)v.getTag();
						showBookmarkContextMenu(v,bm);
						return true;
					}
					return false;
				}
			});
		}
		
	}
	void setTitleText(String title)
	{
		mTitle.setText(title);
	}
	public void showProgress(boolean show)
	{
		mProgress.setVisibility(show?View.VISIBLE:View.GONE);
	}
	public void showBookmarkContextMenu(View v,Bookmark bm)
	{
		int type = mType;
		if(type==TYPE_BOOKMARK_FOLDER_SELECT)
			type = TYPE_BOOKMARKS;
		new MenuBookmark(this, bm, type, null,getBookmarkAdapter().getThumbnailForView(v))
		{
			public void onActionDone(Action act) 
			{
				if(getListAdapter() instanceof CursorBookmarkAdapter)
				{
					CursorBookmarkAdapter adapt = (CursorBookmarkAdapter) getListAdapter();
					Cursor c = null;
					if(mType==TYPE_BOOKMARKS||mType==TYPE_BOOKMARK_FOLDER_SELECT)
						((BookmarkFolderAdapter)getListAdapter()).updateAdapter(false);
					else
					{
						c = getCursorByType(BookmarkActivity.this, mType);
						adapt.setCursor(c);
					}
				}
			}
			public void onActionSelected(Action a) 
			{
				if(a.command==Action.NEW_TAB||a.command==Action.BACKGROUND_TAB)
					finish();
				super.onActionSelected(a);
			};
		}
		.show();
	}
	final SettingsAdapter createSettingsAdapter(ArrayList<Bookmark> ar)
	{
		return new SettingsAdapter(this,ar);
	}
	public BookmarkAdapter initListView()
	{
		BookmarkAdapter ba = null;
		if(mType==TYPE_HISTORY)
		{
			//Cursor c = new DbUtils.Select(stat.STR_NULL).where().eq(Browser.BookmarkColumns.BOOKMARK, type==TYPE_BOOKMARKS?"1":"0").orderBy(Browser.BookmarkColumns.DATE, false).selectOpt(getContentResolver(), Browser.BOOKMARKS_URI);
			ba = new HistoryAdapter(this).setCurrentPos(mCurPos);
		}
		else if(mType==TYPE_SETTINGS)
		{
			ba = createSettingsAdapter(gSetArray==null?SettingsAdapter.getMainSettings(this):gSetArray);
			gSetArray = null;
		}
		else if(mType==TYPE_CLEAR_DATA)
			ba = new SettingsAdapter(this,SettingsAdapter.getClearData(this,Prefs.CLEAR_SETTINGS));
		else if(mType==TYPE_CLEAR_ON_EXIT)
			ba = new SettingsAdapter(this,SettingsAdapter.getClearData(this,Prefs.CLEAR_EXIT));
		else if(mType==TYPE_BOOKMARKS||mType==TYPE_BOOKMARK_FOLDER_SELECT)
		{
			BookmarkFolderAdapter bf = new BookmarkFolderAdapter(this, 0) {
				
				@Override
				public void scrollToTop() {
					mList.scrollToPosition(0);
				}
				@Override
				public void onBookmarkClick(Bookmark bm,LoadBitmapInfo info) {
					Action act = Action.create(Action.ACTION_BOOKMARK, createBookmarkCopy(bm, info));
					if(mType==TYPE_BOOKMARKS)
					{
						finish();
						if(mListener!=null)
							mListener.onAction(act);
						else
							BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_ACTION, act);
					}
				}
				@Override
				public void onItemChanged(Bookmark curBookmark) {
					createActions();
				}
			};
			ba = bf;
		}
		else if(mType==TYPE_FILE_SELECT||mType==TYPE_DIR_SELECT)
		{
			//Cursor c = new DbUtils.Select(stat.STR_NULL).where().eq(Browser.BookmarkColumns.BOOKMARK, type==TYPE_BOOKMARKS?"1":"0").orderBy(Browser.BookmarkColumns.DATE, false).selectOpt(getContentResolver(), Browser.BOOKMARKS_URI);
			FileAdapter fa = new FileAdapter(this,null) {
				
				@Override
				public void onFileSelected(File file) {
					if(mType==TYPE_DIR_SELECT)
						return;
					saveFileList(file);
					File par = file.getParentFile();
					if(par!=null)
						saveFileList(par);
					if(mSelectFileForResult)
					{
						Intent in = new Intent(Intent.ACTION_GET_CONTENT).setData(Uri.fromFile(file));
						setResult(RESULT_OK, in);
						finish();
					}
					else if(mListener!=null)
					{
						Bookmark bm = new Bookmark(Uri.fromFile(file).toString(), file.getName(), file.lastModified()).setParam(file).setWindowState(WINDOW_OPEN_SAME);
						Action a = Action.create(Action.ACTION_BOOKMARK, bm);
						mListener.onAction(a);
						finish();
					}

				}
				@Override
				public void onDirChanged(File parentDir) {
					mList.scrollToPosition(0);
					if(parentDir==null)
						setTitleText(null);
					else
						setTitleText(parentDir.getAbsolutePath());
					createActions();
				}
			};
			ba = fa;
		}
		else if(mType==TYPE_WINDOWS)
		{
			mCurWindowId = getIntent().getIntExtra(EXTRA_CUR_WINDOW_ID, mCurWindowId);
			int closetab = getIntent().getIntExtra(EXTRA_CLOSED_TAB, 0);
			if (mShowCloseTab&&closetab == 1)
				mShowOpenedWindows = false;
			TabList ww = null;
			if (mShowOpenedWindows)
				ww = getMain().getTabList();
			else
				ww = new TabList(getMain(),0,true);
//			TabList ww = mShowOpenedWindows?getMain().getTabList():new TabList(getMain(),0,true);
			ba = new WindowsAdapter(this, ww)
			{
				@Override
				public void updateItems() {
					initListView();
				}
			};
		}
		else if(mType==TYPE_WINDOOW_HISTORY)
		{
			Tab tab = gWebWindow;
			gWebWindow = null;
			if(tab!=null)
				ba = new BookmarkAdapter.WebViewHistoryAdapter(this,tab.getWebView());
		}
		else if(mType==TYPE_VIDEO_HISTORY||mType==TYPE_SAVED_PAGES)
		{
			int type = mType==TYPE_VIDEO_HISTORY?Db.TableExtHistory.VIDEO:Db.TableExtHistory.SAVED_PAGE;
			ba = new BookmarkAdapter.ExtHistoryAdapter(this, type);
		}
		if(ba==null)
		{
			finish();
			return ba;
		}
		getListView().setAdapter(new BookmarkViewRecyclerAdapter(ba));
		createActions();
		return ba;
	}
	public static Cursor getCursorByType(Context context, int type)
	{
		ContentResolver cr = context.getContentResolver();
		Cursor c = null;
		switch (type) {
		case TYPE_HISTORY:
			if(BrowserApp.DB_TYPE==BrowserApp.DB_OWN||Db.CONVERTED_HISTORY)
				return Db.getBookmarksTable().getHistoryCursor(HISTORY_PROJECTION);
			else 
				return cr.query(Browser.BOOKMARKS_URI, HISTORY_PROJECTION, "bookmark = 0", null, DbUtils.getOrder(IConst.DATE, false));
		case TYPE_BOOKMARKS:
		case TYPE_BOOKMARK_FOLDER_SELECT:	
			c = BookmarkFolderAdapter.getBookmarkCursorWithFolder(cr, 1);
		case TYPE_WINDOWS:
			c = Db.getWindowTable().getAllWindowsCursor();
			break;
		case TYPE_SHORTCUTS:
			c = context.getContentResolver().query(BrowserContract.Bookmarks.CONTENT_URI, null,"folder = 1", null, null);
			break;
		case TYPE_VIDEO_HISTORY:
			c=Db.getExtHistory().getCursorByType(Db.TableExtHistory.VIDEO);
			break;
		case TYPE_TEST:
			c=cr.query(BrowserContract.Bookmarks.CONTENT_URI, null, "type=1 AND parent=1", null, "modified desc");
			break;
		default:
			break;
		}
		return c;
	}
	public void createActions()
	{
		ActArray ar = new ActArray();
		if(mType==TYPE_BOOKMARKS||mType==TYPE_BOOKMARK_FOLDER_SELECT)
		{
			// добавляет горизонтальную менюшку в окно закладок
			ar.add(Action.create(Action.OPEN_ALL_BOOKMARK));
			if(mType==TYPE_BOOKMARK_FOLDER_SELECT)
				ar.add(Action.create(Action.SELECT_FOLDER));
			else
				ar.add(Action.create(Action.ADD_BOOKMARK));
			if(BrowserApp.DB_TYPE!=BrowserApp.DB_BROWSER)
				ar.add(Action.create(Action.NEW_FOLDER));
			if(getListAdapter() instanceof BookmarkFolderAdapter)
			{
				BookmarkFolderAdapter ad = (BookmarkFolderAdapter)getListAdapter();
				if(ad.canGoUp())
				{
					ar.add(Action.create(Action.GO_UP));
					ar.add(Action.create(Action.GO_HOME));
					
				}
			}
		}
		else if(mType==TYPE_CLEAR_DATA)
		{
			ar.add(Action.create(Action.CLEAR_DATA));
			ar.add(Action.create(Action.CLEAR).setText(R.string.cancel));
			
		}
		else if(mType==TYPE_WINDOWS)
		{
			ar.add(Action.create(Action.NEW_TAB));
			ar.add(Action.create(Action.CLEAR).setText(R.string.act_close_windows));
			if(mShowOpenedWindows)
				ar.add(Action.create(Action.SHOW_CLOSED_TABS));
			else
				ar.add(Action.create(Action.TAB_LIST));
		}
		else if(mType==TYPE_HISTORY)
			ar.add(Action.CLEAR_DATA,Action.HISTORY_VIDEO,Action.HISTORY_SAVED_PAGES);
		else if(mType==TYPE_VIDEO_HISTORY)
			ar.add(Action.CLEAR_DATA,Action.HISTORY,Action.HISTORY_SAVED_PAGES);
		else if(mType==TYPE_SAVED_PAGES)
			ar.add(Action.CLEAR_DATA,Action.HISTORY,Action.HISTORY_VIDEO);
		else if(mType==TYPE_FILE_SELECT||mType==TYPE_DIR_SELECT)
		{
			FileAdapter ad = (FileAdapter) getListAdapter();
			if(mType==TYPE_DIR_SELECT)
			{
				File cd = ad.getCurDir();
				if(cd!=null)
				{
					Action a = Action.create(Action.SELECT_FOLDER);
					a.param = cd;
					ar.add(a);
				}
			}
			ar.addAll(ad.getActionsForPanel());
		}
		if(ar.size()==0)
		{
			mPanel.setVisibility(View.GONE);
		}
		else
		{
			mPanel.setVisibility(View.VISIBLE);
			mPanel.setActions(ar);
			mPanel.setOnActionListener(this);
		}
	}
	public final RecyclerView getListView()
	{
		return mList;
	}
	public final BookmarkViewRecyclerAdapter getRecyclerAdapter()
	{
		return (BookmarkViewRecyclerAdapter)mList.getAdapter();
	}
	public final ListAdapter getListAdapter()
	{
		return getRecyclerAdapter().getBookmarkAdapter();
	}
	public final BookmarkAdapter getBookmarkAdapter()
	{
		return getRecyclerAdapter().getBookmarkAdapter();
	}
	@Override
	protected void onDestroy() {
		BrowserApp.INSTANCE.removeGlobalListener(this);
		ListAdapter la = getListAdapter();
		if(mType==TYPE_CLEAR_ON_EXIT)
			((SettingsAdapter)la).getClearSettingsJson(Prefs.CLEAR_EXIT);
		super.onDestroy();
		if(la instanceof BookmarkAdapter)
			((BookmarkAdapter)la).destroy();
		if (Prefs.isUpdateSaveSettingAndBookmark())
			new ImportExport(inst).export(null, false);
		inst = null;
	}
	public static Intent getIntentForWindowHistory(Context c,Tab window)
	{
		Intent in = new Intent(c, BookmarkActivity.class);
		in.putExtra(EXTRA_TYPE, TYPE_WINDOOW_HISTORY);
		in.putExtra(EXTRA_WINDOW_ID, window.windowId);
		gWebWindow = window;
		return in;
	}
	public static Intent getIntentForWindowClosedTab(Context c,Tab window)
	{
		Intent in = new Intent(c, BookmarkActivity.class);
		in.putExtra(EXTRA_TYPE, TYPE_CLOSED_TAB);
		in.putExtra(EXTRA_WINDOW_ID, window.windowId);
		gWebWindow = window;
		return in;
	}
	public static class HistoryAdapter extends BookmarkAdapter.CursorBookmarkAdapter
	{
		public HistoryAdapter(Context context) {
			super(context, null/*getCursorByType(context, TYPE_HISTORY)*/);
			startAsyncLoader();
		}
		Cursor mTempCursor;
		@Override
		public boolean doAsync() throws Throwable {
			mTempCursor = getCursorByType(getContext(), TYPE_HISTORY);
			return true;
		}
		@Override
		public void doAsyncUiThread(boolean result) {
			setCursor(mTempCursor);
		}
		@Override
		public Bookmark getBookmarkFromCursor(Cursor c) {
			return Bookmark.fromManagedCursor(c);
		}
	}
	public static void runSettings(Context c,ArrayList<Bookmark> settingsArray,String title)
	{
		gSetArray = settingsArray;
		Intent in = new Intent(c,BookmarkActivity.class)
			.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			.putExtra(EXTRA_TYPE, TYPE_SETTINGS)
			.putExtra(EXTRA_TITLE, title)
			;
		c.startActivity(in);
	}
	public static void runByType(Context c,int type)
	{
		Intent in = new Intent(c,BookmarkActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra(EXTRA_TYPE, type);
		c.startActivity(in);
	}
	public static void runForWindowId(MainActivity a,int windowId)
	{
		gMain = a;
		Intent in = new Intent(BrowserApp.INSTANCE,BookmarkActivity.class).putExtra(EXTRA_TYPE, TYPE_WINDOWS);
		a.startActivityForResult(in,IConst.CODE_WINDOW_ID);
	}
	public static void runForClosedTab(MainActivity a,int windowId)
	{
		mShowCloseTab = true;

		gMain = a;
		Intent in = new Intent(BrowserApp.INSTANCE,BookmarkActivity.class).putExtra(EXTRA_TYPE, TYPE_WINDOWS);
		in.putExtra(EXTRA_CLOSED_TAB, 1);
		a.startActivityForResult(in,IConst.CODE_WINDOW_ID);
		
	}
	public static void runForFileSelect(Activity a,OnAction listener)
	{
		Intent in = new Intent(BrowserApp.INSTANCE,BookmarkActivity.class).putExtra(EXTRA_TYPE, TYPE_FILE_SELECT);
		gListener = listener;
		a.startActivity(in);
	}
	public static void runForFileDirSelect(Activity a,OnAction listener)
	{
		Intent in = new Intent(BrowserApp.INSTANCE,BookmarkActivity.class).putExtra(EXTRA_TYPE, TYPE_DIR_SELECT);
		gListener = listener;
		a.startActivity(in);
	}
	public static void runForBookmarkFolderSelect(Activity a,OnAction listener)
	{
		Intent in = new Intent(BrowserApp.INSTANCE,BookmarkActivity.class).putExtra(EXTRA_TYPE, TYPE_BOOKMARK_FOLDER_SELECT);
		gListener = listener;
		a.startActivity(in);
	}
	@Override
	public void onAction(Action act) {
		if(getListAdapter() instanceof FileAdapter)
		{
			FileAdapter ad = (FileAdapter) getListAdapter();
			switch (act.command) {
			case Action.GO_HOME:
				ad.goHome();
				break;
			case Action.GO_UP:
				ad.goUp();
				break;
			case Action.NEW_FOLDER:
				ad.createNewFolder();
				break;
			case Action.SELECT_FOLDER:
				if(act.param==null)
					CustomPopup.toast(this, R.string.cant_select_dir);
				else if(mListener!=null)
				{
					File f = (File)act.param;
					f.getAbsolutePath();
					
					ad.saveFileList(f);
					finish();
					mListener.onAction(act);
				}
				break;
			default:
				break;
			}
			return;
		}
		else if(mType==TYPE_CLEAR_DATA)
		{
			if(act.command==Action.CLEAR)
				finish();
			else
			{
				JSONObject jo = ((SettingsAdapter)getListAdapter()).getClearSettingsJson(Prefs.CLEAR_SETTINGS);
				act.param = jo;
				finish();
				BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_ACTION, act);
			}
			return;
		}
		else if(getListAdapter() instanceof BookmarkFolderAdapter)
		{
			final BookmarkFolderAdapter ad = (BookmarkFolderAdapter)getListAdapter();
			switch (act.command) {
			case Action.OPEN_ALL_BOOKMARK:
//				finish();
////				long idfold = ROOT_FOLDER_ID;
////				Bookmark bm1 = ad.getCurrentFolder();
////				if(bm1==null) {
////					idfold = ad.getCurrentFolderId();
////							//bm1 = Bookmark.fromBookmarkFolder(getString(R.string.act_bookmarks), ROOT_FOLDER_ID);
////					
////				}
////				Cursor c = ad.getCursorForBookmarkFolder(getMain(), bm1);
//		        Cursor c = Db.getBookmarksTable().getBookmarkAllCursor(new String[] {"*"});
//		        if (c==null)
//		        	return;
//				String url = stat.STR_NULL;
//				byte[] blob = null;
//		        c.moveToFirst();
//		        do {
//		        	url = c.getString(1);
//		        	blob = c.getBlob(9);
//		        	if (url!=null&blob!=null) {
//						Action act1 = Action.create(Action.SHARE_URL);
//						act1.param = Uri.parse(url);
//						
//						BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_ACTION, act1);
////		        		MainActivity mact = getMain();
////		        		if (mact!=null)
////		        			mact.openUrl(url,Action.NEW_TAB);
//
//		        	}
//		        } while (c.moveToNext());


				return;
			case Action.ADD_BOOKMARK:
				finish();
				act.param = ad.getCurrentFolder();
				BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_ACTION, act);
				return;
			case Action.GO_HOME:
				ad.goHome();
				break;
			case Action.GO_UP:
				ad.goUp();
				break;
			case Action.NEW_FOLDER:
				ad.createFolder();
				break;
			case Action.SELECT_FOLDER:
				Bookmark bm = ad.getCurrentFolder();
				if(bm==null)
					bm = Bookmark.fromBookmarkFolder(getString(R.string.act_bookmarks), ROOT_FOLDER_ID);
				if(mListener!=null)
				{
					finish();
					mListener.onAction(Action.create(Action.ACTION_BOOKMARK, bm));
				}
				return;
			}
			return;
		}
		switch (act.command) {
		case Action.CLEAR:
				if(mType==TYPE_WINDOWS)
				{
					BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_ACTION, Action.create(Action.CLOSE_ALL_TABS));
					finish();
				}
			break;
		case Action.SHOW_CLOSED_TABS:
		case Action.TAB_LIST:
			mShowOpenedWindows = act.command==Action.TAB_LIST;
			mShowCloseTab = !mShowOpenedWindows;
			if (mShowCloseTab) {
				Intent in = getIntent();
				in.putExtra(EXTRA_CLOSED_TAB, 1);
				createAndSetAdapter();
			} else
				initListView();
			break;
		case Action.HISTORY_VIDEO:
			mType=TYPE_VIDEO_HISTORY;
			createAndSetAdapter();
			break;
		case Action.HISTORY_SAVED_PAGES:
			mType=TYPE_SAVED_PAGES;
			createAndSetAdapter();
			break;
		case Action.CLEAR_DATA:
			new MenuSettingBookmarks(this,getString(R.string.act_clear_data),DbClear.CLEAR_HISTORY_TYPES.getValues()) {
				
				@Override
				public void onBookmarkSelected(int pos, SettingsBookmark set) {
					int strId = set.id;
					int ct = DbClear.CLEAR_HISTORY_TYPES.getKeyByValue(strId);
					new ThemedDialog(BookmarkActivity.this).setConfirm(getString(R.string.clear_confirm), ct, new ConfirmOper() {
						
						@Override
						public void onConfirm(Object userParam) {
							Integer ct = (Integer)userParam;
							DbClear.ClearData cd = null;
							if(mType==TYPE_HISTORY)
								cd = new DbClear.ClearDataHistory();
							else if(mType==TYPE_VIDEO_HISTORY)
								cd = new DbClear.ClearDataExtHistory(Db.TableExtHistory.VIDEO);
							else if(mType==TYPE_SAVED_PAGES)
								cd = new DbClear.ClearDataExtHistory(Db.TableExtHistory.SAVED_PAGE);
							int clear = DbClear.clearHistory(context(), ct, cd);
							createAndSetAdapter();
						}
					}.setTitle(strId));
				}
			}.show();
			break;
		case Action.HISTORY:
			mType=TYPE_HISTORY;
			createAndSetAdapter();
			break;
		case Action.NEW_TAB:
			BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_ACTION, act);
			finish();
			break;
		default:
			break;
		}
	}
	public static Intent getIntent(Context c,int type)
	{
		return new Intent(c, BookmarkActivity.class).putExtra(BookmarkActivity.EXTRA_TYPE, type);
	}
	@Override
	public void onGlobalEvent(int code, Object param) {
		if (code==TYPE_BOOKMARKS&&mType==4){
			int cnt = getRecyclerAdapter().getItemCount();
			if (cnt==1){
				finish();

			}
		}
		else if(code==BrowserApp.GLOBAL_BOOKMARKS_CHANGED&&mType==TYPE_BOOKMARKS)
		{
			getRecyclerAdapter().notifyDataSetChanged();
		}
	}
	MainActivity getMain() {
		if(mMain==null)
			return null;
		return mMain.get();
	}
	void setMain(MainActivity main) {
		this.mMain = new WeakReference<MainActivity>(main);
	}
	
}
