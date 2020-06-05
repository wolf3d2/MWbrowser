package com.jbak.superbrowser.panels;

import java.io.File;
import java.util.ArrayList;

import ru.mail.mailnews.SiteApp;
import ru.mail.mailnews.st;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.jbak.superbrowser.ActArray;
import com.jbak.superbrowser.Action;
import com.jbak.superbrowser.Bookmark;
import com.jbak.superbrowser.BrowserApp;
import com.jbak.superbrowser.IConst;
import com.jbak.superbrowser.MainActivity;
import com.jbak.superbrowser.Prefs;
import com.mw.superbrowser.R;
import com.jbak.superbrowser.SearchAction;
import com.jbak.superbrowser.stat;
import com.jbak.superbrowser.adapters.SettingsBookmark;
import com.jbak.superbrowser.WebViewEvent;
import com.jbak.superbrowser.Tab;
import com.jbak.superbrowser.pluginapi.Plugin;
import com.jbak.superbrowser.recycleview.RecyclerViewEx;
import com.jbak.superbrowser.search.SearchSystem;
import com.jbak.superbrowser.stat.DownloadOptions;
import com.jbak.superbrowser.ui.HorizontalPanel;
import com.jbak.superbrowser.ui.MyWebView;
import com.jbak.superbrowser.ui.OnAction;
import com.jbak.superbrowser.ui.PanelButton;
import com.jbak.superbrowser.ui.SuggestionsAdapter;
import com.jbak.superbrowser.ui.dialogs.DialogEditor;
import com.jbak.superbrowser.ui.dialogs.ThemedDialog;
import com.jbak.superbrowser.ui.themes.MyTheme;
import com.jbak.ui.ConfirmOper;
import com.jbak.ui.SuggestionEdit;
import com.jbak.ui.CustomDialog.OnUserInput;
import com.jbak.ui.SuggestionEdit.OnAutoCompleteAction;

public class PanelUrlEdit extends LinearLayout implements WebViewEvent {
	
	public PanelUrlEdit(Context c)
	{
		super(c);
		init();
	}
	public PanelUrlEdit(Context c,int styles)
	{
		super(c);
		mStyles = styles;
		init();
	}
	public PanelUrlEdit(Context c, AttributeSet attrs) {
		super(c, attrs);
		init();
	}
	PanelSearch mPanelSearch = null;
	boolean mIncognito = false;
	boolean mUserEditText = false;
	/** Юзер не редактирует текст */
	public static final int STATE_ADDR_NONE = 0;
	/** Юзер редактирует текст и поле для ввода пусто */
	public static final int STATE_ADDR_EMPTY = 1;
	/** Юзер вводит в поле текст для поиска */
	public static final int STATE_ADDR_SEARCH = 2;
	/** Юзер вводит в поле поиска url */
	public static final int STATE_ADDR_URL = 3;
	SuggestionEdit mEditUrl;
	/** ПОКА НЕ ИСПОЛЬЗУЕТСЯ! кнопка для быстрой смены поисковой системы. */
	ImageView mImageSearch;
	HorizontalPanel mToolsPanel;
	String mUrl;
	int mMaxLinesExpanded;
	int mStyles=STYLE_ONE_LINE;
	public static final int STYLE_ONE_LINE = 0x0001;
	public static final int STYLE_VISIBLE_TOOLS = 0x0002;
	int mAddrState = STATE_ADDR_NONE;
	OnAction mActionListener;
	int mToolsPanelHeight = 0;
	private void init() {
		setOrientation(VERTICAL);
//		setOrientation(HORIZONTAL);
//		mImageSearch = new ImageView(getContext());
//		Drawable dr = null;
//		try {
//			dr = getContext().getResources().getDrawable(R.drawable.ic_launcher);
//			dr.setBounds(0, 0, 32, 32);
//			
//		} catch (Throwable e) {
//		}
//		if (dr!=null) {
//			mImageSearch.setImageDrawable(dr);
//			addView(mImageSearch);
//		}
		mEditUrl = (SuggestionEdit) LayoutInflater.from(getContext()).inflate(R.layout.url_edit, null);
		//addView(mEditUrl);
		// пока не используется
		addView(createUrlEditPanel());
		mToolsPanel = new HorizontalPanel(getContext());
		mToolsPanel.setType(RecyclerViewEx.TYPE_GRID);
		//mToolsPanel.setWrapContent(true);
		//mToolsPanel.setButtonsType(PanelButton.TYPE_BUTTON_MEDIUM_TWO_LINE);
		mToolsPanelHeight = PanelButton.getPanelButtonHeight(getContext(), null, PanelButton.TYPE_BUTTON_MEDIUM_TWO_LINE);
		mToolsPanelHeight*=4;
		mToolsPanel.setMaxHeight(mToolsPanelHeight);
		mToolsPanel.setCheckWidthWhileNotAutoFill(false);
		addView(mToolsPanel);
//		setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);
		int layoutType = Prefs.getPanelLayoutType();
		SuggestionsAdapter sa = new SuggestionsAdapter(getContext(), R.layout.menu_item);
		sa.setStackFromBottom(layoutType==Prefs.TYPE_LAYOUT_BOTTOM);
		mEditUrl.setAdapter(sa);
		mEditUrl.setOnAutoCompleteAction(getEditUrlListener());
		// нажимаем enter, когда кликнули на позицию из выпадающего списка
		mEditUrl.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
			{
				runAction(Action.create(Action.GO));
			}
		});
		
		mToolsPanel.setOnActionListener(new OnAction() {
			
			@Override
			public void onAction(Action act) {
				runAction(act);
			}
		});
		setUrlFromWebWindow();
		onAddrStateChanged(STATE_ADDR_NONE);
		mEditUrl.setSelectAllOnFocus(false);
		setEditorStyles();
	}
	final Tab getWebWindow()
	{
		if(getContext() instanceof MainActivity)
			return ((MainActivity)getContext()).getTab();
		return null;
	}
	final void setUrlFromWebWindow()
	{
		Tab ww = getWebWindow();
		if(ww!=null&&ww.getCurBookmark()!=null)
			setUrl(ww.getCurBookmark().getUrl()); 
	}
	public void setStyles(int styles)
	{
		mStyles = styles;
	}
	public void runAction(Action a)
	{
		switch (a.command)
		{
//		case Action.HISTORY_SEARCH:
//			if (mPanelSearch == null)
//				return;
//			return;
		case Action.CREATE_URL_ON_DESKTOP:
			MainActivity ma = ((MainActivity)getContext());
			String url = mEditUrl.getText().toString();
					
					
			Intent i = new Intent();
			i.setAction(Intent.ACTION_VIEW);
			i.setData(Uri.parse(url));
// пока не знаю как добавить свой ярлык			
//			Bitmap btm = (ma).getTab().getFavicon();
//			if (btm == null)
//				btm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

			url = ma.getTab().getWebView().getTitle();
			Intent installer = new Intent();
		    installer.putExtra("android.intent.extra.shortcut.INTENT", i);
		    installer.putExtra("android.intent.extra.shortcut.NAME", url);
		    installer.putExtra("android.intent.extra.shortcut.ICON_RESOURCE", Intent.ShortcutIconResource.fromContext((MainActivity)getContext(), R.drawable.ic_launcher));
		    installer.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		    getContext().sendBroadcast(installer);
		    onAddrStateChanged(STATE_ADDR_NONE);
		    st.toast(R.string.act_created);

//			((MainActivity)getContext()).runAction(a);
			return;
		case Action.CLEAR_TEXT:
			mEditUrl.setText(null);
			if(!mUserEditText)
				st.showEditKeyboard(mEditUrl);
			return;
		case Action.CANCEL:
			onAddrStateChanged(STATE_ADDR_NONE);
			return;
		case Action.TRANSLATE_LINK:
		case Action.TRANSLATE_COPYING:
			((MainActivity)getContext()).runAction(a);
			return;
		case Action.PASTE:
			mEditUrl.setText(stat.getClipboardCharSequence(getContext()));
			return;
		case Action.VOICE_SEARCH:
			a.param = this;
			((MainActivity)getContext()).runAction(a);
			return;
		default:
			a.setParam(mEditUrl.getText().toString());
		}
		if(mActionListener!=null)
		{
			mActionListener.onAction(a);
			return;
		}
		if(getContext() instanceof MainActivity)
		{
			mEditUrl.clearFocus();
			mAddrState = STATE_ADDR_NONE;
			onAddrStateChanged(mAddrState);
			mEditUrl.setText(mUrl);
			((MainActivity)getContext()).hideRoundButton();
			((MainActivity)getContext()).runAction(a);
		}

	}
	void onAddrStateChanged(int newState)
	{
		mAddrState = newState;
		int toolsVis = newState!=STATE_ADDR_NONE?VISIBLE:GONE;
		if((mStyles&STYLE_VISIBLE_TOOLS)!=0)
			toolsVis = VISIBLE;
		mToolsPanel.setVisibility(toolsVis);
		createToolsActions();
		if(mAddrState==STATE_ADDR_NONE)
		{
			//clearFocus();
			st.hideEditKeyboard(mEditUrl);
			if(mUserText==null)
				setUrl(mUrl);
		}
	}
	
	void checkAddrState()
	{
		if(!mUserEditText||!mEditUrl.hasFocus())
		{
			if(mAddrState!=STATE_ADDR_NONE)
				onAddrStateChanged(STATE_ADDR_NONE);
			return;
		}
		int state = STATE_ADDR_EMPTY;
		String text = mEditUrl.getText().toString();
		if(!TextUtils.isEmpty(text))
		{
			boolean addr = stat.isWebAddr(text);
			state = addr?STATE_ADDR_URL:STATE_ADDR_SEARCH;
			mUserText = null;
		}
		if(state!=mAddrState)
		{
			onAddrStateChanged(state);
		}
	}
	public final boolean isToolsVisible()
	{
		return (mStyles&STYLE_VISIBLE_TOOLS)!=0;
	}
	/** создание панели при нажатии на панель адреса */
	public void createToolsActions()
	{
		ActArray ar = new ActArray();
		if(mUserEditText) {
			ar.add(Action.create(Action.CANCEL));
			//ar.add(Action.create(Action.TRANSLATE_LINK));
			if(!TextUtils.isEmpty(stat.getClipboardCharSequence(getContext())))
				ar.add(Action.PASTE);
			ar.add(Action.create(Action.TRANSLATE_COPYING));
		}
		String text = mEditUrl.getText().toString();
		if(TextUtils.isEmpty(text))
		{
// старое положение кнопки paste
//			if(!TextUtils.isEmpty(stat.getClipboardCharSequence(getContext())))
//				ar.add(Action.PASTE);
			if(stat.isVoiceSearchExist(getContext()))
				ar.add(Action.create(Action.VOICE_SEARCH,this));
			BrowserApp.pluginServer.getPluginActions(ar, Plugin.WINDOW_ADDR_EMPTY,this);
		}
		else
		{
			if (text.startsWith(IConst.HTTP)||text.startsWith(IConst.HTTPS))
				ar.add(Action.create(Action.CREATE_URL_ON_DESKTOP));
			ar.add(Action.create(Action.CLEAR_TEXT));
			mPanelSearch = new PanelSearch(getContext());
			if (mPanelSearch.getPanel().getRealAdapter().getItemCount()>0) {
				ar.add(Action.create(Action.HISTORY_SEARCH));
			} else
				mPanelSearch = null;
			if(mAddrState!=STATE_ADDR_SEARCH)
				ar.add(Action.create(Action.COPY_URL_TO_CLIPBOARD));
			if(!TextUtils.isEmpty(text)&&(mAddrState==STATE_ADDR_URL||!mUserEditText&&isToolsVisible()&&stat.isWebAddr(text)))
			{
				if(text.equals(mUrl))
				{
					PanelQuickTools.addRefreshBackForward((MainActivity)getContext(), ar);
				}
				else
					ar.add(Action.create(Action.GO));
				//ar.add(Action.create(Action.GO).setParam(mEditUrl));
			}
			BrowserApp.pluginServer.getPluginActions(ar, Plugin.WINDOW_ADDR_URL,this);
		}
		if(mAddrState==STATE_ADDR_SEARCH)
		{
			ar.add(new SearchAction(SearchSystem.CMD_SEARCH, R.string.act_search, R.drawable.search));
			ar.add(new SearchAction(SearchSystem.CMD_I_FEEL_LUCKY, R.string.act_i_feel_lucky, R.drawable.ilucky));
			ar.add(Action.SEARCH_ON_PAGE);
			ar.add(new SearchAction(SearchSystem.CMD_SEARCH_IMAGES, R.string.act_search_images, R.drawable.images));
			ar.add(new SearchAction(SearchSystem.CMD_SEARCH_VIDEOS, R.string.act_search_videos, R.drawable.search_videos));
			ar.add(new SearchAction(SearchSystem.CMD_SEARCH_NEWS, R.string.act_search_news, R.drawable.search_news));
			ar.add(new SearchAction(SearchSystem.CMD_SEARCH_APPS, R.string.act_search_apps, R.drawable.appsonmarket));
			if(!TextUtils.isEmpty(mUrl))
			{
				String domain = null;
				try{
					domain = Uri.parse(mUrl).getHost();
				}
				catch(Throwable ignor)
				{}
				Action a= new SearchAction(SearchSystem.CMD_SEARCH_ON_SITE, R.string.act_search_site, R.drawable.searchsite);
				if(!TextUtils.isEmpty(domain))
					a.setText(getContext().getString(R.string.search_on)+' '+domain);
				ar.add(a);
				BrowserApp.pluginServer.getPluginActions(ar, Plugin.WINDOW_ADDR_SEARCH,this);
			}
		}
		MyTheme.get().setViews(MyTheme.ITEM_MAIN_PANEL_BACKGROUND, mToolsPanel);
		mToolsPanel.setCanAutofill(ar.size()<6);
		mToolsPanel.setActions(ar);
	}
	public final HorizontalPanel getToolsPanel()
	{
		return mToolsPanel;
	}
	@Override
	public void clearFocus() {
		try{
			mEditUrl.clearFocus();
			mEditUrl.setSelection(0);
			super.clearFocus();
		}
		catch(Throwable e){}
	}
//	@Override
//	public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
//		if(direction==FOCUS_DOWN||direction==FOCUS_FORWARD)
//			return false;
//		return super.requestFocus(direction, previouslyFocusedRect);
//	}
	public OnAutoCompleteAction getEditUrlListener()
	{
		return new OnAutoCompleteAction() {
			
			@Override
			public void onTextChanged(SuggestionEdit edit) {
				checkAddrState();
			}
			
			@Override
			public void onFocusChanged(SuggestionEdit edit, boolean focus) {
//				if(edit==mEditUrl)
//				{
//					checkAddrState();
////					if(focus)
////						St.showEditKeyboard(edit);
////					else
////					{
////						InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Service.INPUT_METHOD_SERVICE);
////						imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
////					}
//				}
			}

			@Override
			public void onDropDownShown(boolean show) {
				//mPanelBookmarks.setVisibility(show?View.GONE:View.VISIBLE);
				
			}

			@Override
			public void onEditorAction(int action) {
				runAction(Action.create(Action.GO));
			}
		};
	}
	public final int getAddrState()
	{
		return mAddrState;
	}
	public final void setText(String text,boolean blockQuery)
	{
		if(blockQuery)
			mEditUrl.setBlockFilterString(text);
		mEditUrl.setText(text);

	}
	public final void setText(String text)
	{
		setText(text, true);
	}
	public final String getText()
	{
		return mEditUrl.getText().toString();
	}
	public void setTextAndShowKeyboard(String text)
	{
		mUserText = text;
		setText(text);
		mUserEditText = true;
		st.showEditKeyboard(mEditUrl);
	}
	public void setUrl(String url)
	{
		mUrl = url;
		if(mUserEditText)
			return;
		setText(mUrl);
		mEditUrl.setSelection(0);
	}
	public final String getUrl()
	{
		return mUrl;
	}
	@Override
	public void onWebViewEvent(int code, EventInfo info) {
		switch (code) {
			case WWEVENT_CUR_WINDOW_CHANGES:
				//setUrl(info.getUrl());
				if(!mUserEditText)
					setUrlFromWebWindow();
				break;
			case WWEVENT_SOFT_KEYBOARD_VISIBLE:
			case WWEVENT_SOFT_KEYBOARD_HIDDEN:
				mUserEditText = code==WWEVENT_SOFT_KEYBOARD_VISIBLE;
				checkAddrState();
				setEditorStyles();
				if(mEditUrl.hasFocus())
				{
					if(code==WWEVENT_SOFT_KEYBOARD_VISIBLE)
					{
						mEditUrl.setSelectAllOnFocus(true);
						//mEditUrl.clearFocus();
						mEditUrl.requestFocus();
						mEditUrl.selectAll();
						mToolsPanel.setMaxHeight(mToolsPanelHeight);

					}
					else
					{
						mEditUrl.setSelectAllOnFocus(false);
						mEditUrl.clearFocus();
						mEditUrl.setText(mUrl);
						mEditUrl.setSelection(0);
						if(isToolsVisible())
							onAddrStateChanged(STATE_ADDR_URL);
					}
				}
		}
			
	}
	protected void setEditorStyles()
	{
		if((mStyles&STYLE_ONE_LINE)>0)
		{
			if(mUserEditText)
			{
				mEditUrl.setSingleLine(false);
				mEditUrl.setMaxLines(getResources().getConfiguration().orientation==Configuration.ORIENTATION_PORTRAIT?7:3);
			}
			else
			{
				mEditUrl.setMaxLines(1);
				mEditUrl.setSingleLine(true);
			}
		}
	}
	public void setActionListener(OnAction listener)
	{
		mActionListener = listener;
	}
	String mUserText;
	public void onSuccessVoiceReconition(Intent data)
	{

		ArrayList<String>ar  = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
		if(ar!=null&&!ar.isEmpty())
		{
			mUserText = ar.get(0);
			setTextAndShowKeyboard(mUserText);
		}
	}
	public final void hideKeyboard()
	{
		InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mEditUrl.getWindowToken(), 0);
	}
	public final boolean hasInputFocus()
	{
		return mEditUrl.isFocused();
	}
	public final String getUserText()
	{
		return mUserText;
	}
/** Создание панели адреса со спиннером быстрой смены
 * поисковой системы (не доделано!) */
	public RelativeLayout createUrlEditPanel()
	{
		RelativeLayout rl = new RelativeLayout(getContext());
		rl.setLayoutParams(new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT, 
				RelativeLayout.LayoutParams.WRAP_CONTENT));
		rl.setBackgroundColor(0xffffffff);
		RelativeLayout.LayoutParams splp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, 
				RelativeLayout.LayoutParams.MATCH_PARENT);
		splp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		splp.addRule(RelativeLayout.CENTER_VERTICAL);
		splp.addRule(RelativeLayout.CENTER_HORIZONTAL);
		//splp.addRule(RelativeLayout.LEFT_OF, 1001);
		String cur_name = SearchSystem.getCurrent().getName();
		/** индекс текущей поисковой системы */
		int cur_ind = 0;
		Integer ar[] = new Integer[SearchSystem.SEARCH_SYSTEMS.length];
		for(int i=0;i<SearchSystem.SEARCH_SYSTEMS.length;i++)//  id:SearchSystem.getSearchSystemsIconId())
		{
			ar[i]= SearchSystem.SEARCH_SYSTEMS[i].getIconId();
			if (cur_name.equals(SearchSystem.SEARCH_SYSTEMS[i].getName()))
				cur_ind = i;
		}
		// поисковая система
		Spinner sp = new Spinner(getContext());
		Adapt adapter = new Adapt(getContext(), 
		        ar);
//		ArrayAdapter<?> adapter = 
//		ArrayAdapter.createFromResource(getContext(), R.array.ww_back_color, android.R.layout.simple_spinner_item);
//		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mEditUrl.measure(0, 0);
		int hh = mEditUrl.getMeasuredHeight();
		splp.height = hh;
		splp.width = hh;
		sp.setLayoutParams(splp);
		sp.setBackgroundColor(0xffffffff);
		sp.setAdapter(adapter);
		sp.setId(1000);
		sp.setOnItemSelectedListener(m_itemSelection);
		
		sp.setSelection(cur_ind);
		rl.addView(sp);
		
		// блокировщик рекламы
		RelativeLayout.LayoutParams adslp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, 
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		adslp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		adslp.addRule(RelativeLayout.CENTER_VERTICAL);
		adslp.addRule(RelativeLayout.CENTER_HORIZONTAL);

		TextView ads = new TextView(getContext());
		ads.setText("Ad");
		ads.setTextSize(12);
		ads.setTextColor(Color.BLACK);
		ads.setPadding(5, 0, 5, 0);
		//MyTheme.get().setViews(MyTheme.ITEM_DIALOG_TEXT, ads);
		//MyTheme.get().setViewsTextColor(MyTheme.ITEM_TITLE, ads);
		
		setAdsIndicator(ads,Prefs.isAdsABlock());
		ads.setId(1002);
		ads.setLayoutParams(adslp);
		ads.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View arg0) {
				if (MainActivity.inst==null)
					return false;
				Tab ww = getWebWindow();
				String sss = ww.getWebView().loadUrls;
				try {
					new DialogEditor(MainActivity.inst, "Загруженные урлы", 
							getWebWindow().getWebView().loadUrls)
					.show();
					
				} catch (Exception e) {
					return false;
				}

				return true;
			}
		});
		ads.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				 
				if (st.adblock) 
					setAdsIndicator((TextView)v, false);
				else
					setAdsIndicator((TextView)v, true);
				//st.adblock = !st.adblock;
				Prefs.setAdsABlock(!st.adblock);
			}
		});
		rl.addView(ads);
		ads.setVisibility(View.GONE);
		// инкогнито
		RelativeLayout.LayoutParams ilp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, 
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		ilp.addRule(RelativeLayout.LEFT_OF, ads.getId());

		ImageView incognito = new ImageView(getContext());
		incognito.setVisibility(View.GONE);
		incognito.setBackgroundResource(R.drawable.mask_32);
		incognito.setId(1001);
		incognito.setLayoutParams(ilp);
		mIncognito = false;
		incognito.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (MainActivity.inst!=null) {
					
					ImageView iv = (ImageView) v;
					if (mIncognito) {
						iv.setBackgroundResource(R.drawable.mask_32);
						MainActivity.inst.setIncognito(false);
					} else {
						iv.setBackgroundResource(R.drawable.mask_purple_32);
						MainActivity.inst.setIncognito(true);
					}
					mIncognito = !mIncognito;
				}
			}
		});
		rl.addView(incognito);
		
		RelativeLayout.LayoutParams ulp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, 
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		ulp.addRule(RelativeLayout.CENTER_VERTICAL);
		ulp.addRule(RelativeLayout.CENTER_HORIZONTAL);
		if (ads.getVisibility() == View.GONE)
			ulp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		else
			ulp.addRule(RelativeLayout.LEFT_OF, ads.getId());

		TextView upd = new TextView(getContext());
		upd.setId(1003);
		upd.setText("Upd");
		upd.setTextSize(12);
		upd.setBackgroundColor(Color.RED);
		upd.setTextColor(Color.WHITE);
		upd.setLayoutParams(ulp);
		upd.setPadding(5, 0, 5, 0);
		upd.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (MainActivity.inst!=null) {
	            	new ThemedDialog(getContext()).setConfirm(
	            			getContext().getString(R.string.upd_new_ver), 
	            			null, 
	            			new ConfirmOper() {
						
						@Override
						public void onConfirm(Object userParam) {
							try {
								mEditUrl.setText(SiteApp.SITE_APP+SiteApp.PAGE_DOWNLOAD);
								runAction(Action.create(Action.GO));
//								MainActivity.inst.openUrl(SiteApp.SITE_APP+SiteApp.PAGE_DOWNLOAD, 
//										Action.NEW_TAB);	
								
							} catch (Throwable e) {
								// TODO: handle exception
							}
						}
					});

	            }
					
			}
		});
		rl.addView(upd);

		upd.setVisibility(View.GONE);
		if (MainActivity.ini!=null)
			if (SiteApp.checkVersion(getContext(), MainActivity.ini))
				upd.setVisibility(View.VISIBLE);

		RelativeLayout.LayoutParams etlp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT, 
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		etlp.addRule(RelativeLayout.RIGHT_OF, sp.getId());
		etlp.addRule(RelativeLayout.LEFT_OF, upd.getId());
		mEditUrl.setLayoutParams(etlp);

		rl.addView(mEditUrl);
		return rl;
	}
	Drawable img_ads_block_indicator = null;
	
	public void setAdsIndicator(TextView tv, boolean flag)
	{
		if (flag)
			img_ads_block_indicator = getContext().getResources().getDrawable(R.drawable.bullet_red);
		else
			img_ads_block_indicator = getContext().getResources().getDrawable(R.drawable.bullet_black);
		img_ads_block_indicator.setBounds( 0, 0, 25, 25 );
		tv.setCompoundDrawables( img_ads_block_indicator, null, null, null );
		tv.setCompoundDrawablePadding(0);
		
	}
	public class Adapt extends ArrayAdapter<Integer> {
		private Integer[] images;
		private ImageView imageView;
		private Drawable dr;
		
		public Adapt(Context context, Integer[] images) {
		    super(context, android.R.layout.simple_spinner_item, images);
		    this.images = images;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			parent.setBackgroundColor(0xffffffff);
		    return getImageForPosition(convertView, position, true);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
		    return getImageForPosition(convertView, position, true);
		}

		private View getImageForPosition(View v,int position, boolean bigImageSize) {
			if (v==null) {
				imageView = new ImageView(getContext());
		        imageView.setLayoutParams(new AbsListView.LayoutParams(
		        		ViewGroup.LayoutParams.WRAP_CONTENT, 
		        		ViewGroup.LayoutParams.MATCH_PARENT));
			} else 
				imageView = (ImageView)v;
			if (bigImageSize) {
				imageView.setMinimumHeight(75);
				imageView.setMinimumWidth(75);
			}
	        imageView.setBackgroundResource(images[position]);
	        return imageView;
		}
	}
    AdapterView.OnItemSelectedListener m_itemSelection = new AdapterView.OnItemSelectedListener()
    {
    	public void onItemSelected(AdapterView<?> parent,
    			View itemSelected, int selectedItemPosition, long selectedId) 
    	{
    		String ss = SearchSystem.SEARCH_SYSTEMS[selectedItemPosition].getName();
    		SearchSystem.setSearchSystem(ss);
    		if (st.last_search!=null) {
				if (MainActivity.inst!=null) {
						MainActivity.inst.openUrl(st.last_search, MainActivity.WINDOW_OPEN_SAME);
					}

    		}
    	}
    	public void onNothingSelected(AdapterView<?> parent) {
    	}
    ;};		    

}
