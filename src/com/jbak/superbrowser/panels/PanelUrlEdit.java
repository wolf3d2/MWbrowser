package com.jbak.superbrowser.panels;

import java.util.ArrayList;

import ru.mail.mailnews.st;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import com.jbak.superbrowser.ActArray;
import com.jbak.superbrowser.Action;
import com.jbak.superbrowser.BrowserApp;
import com.jbak.superbrowser.MainActivity;
import com.jbak.superbrowser.Prefs;
import com.mw.superbrowser.R;
import com.jbak.superbrowser.SearchAction;
import com.jbak.superbrowser.stat;
import com.jbak.superbrowser.WebViewEvent;
import com.jbak.superbrowser.Tab;
import com.jbak.superbrowser.pluginapi.Plugin;
import com.jbak.superbrowser.search.SearchSystem;
import com.jbak.superbrowser.ui.HorizontalPanel;
import com.jbak.superbrowser.ui.OnAction;
import com.jbak.superbrowser.ui.PanelButton;
import com.jbak.superbrowser.ui.SuggestionsAdapter;
import com.jbak.superbrowser.ui.themes.MyTheme;
import com.jbak.ui.SuggestionEdit;
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
	public PanelUrlEdit(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
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
	HorizontalPanel mToolsPanel;
	String mUrl;
	int mMaxLinesExpanded;
	int mStyles=STYLE_ONE_LINE;
	public static final int STYLE_ONE_LINE = 0x0001;
	public static final int STYLE_VISIBLE_TOOLS = 0x0002;
	int mAddrState = STATE_ADDR_NONE;
	OnAction mActionListener;
	private void init() {
		setOrientation(VERTICAL);
		mEditUrl = (SuggestionEdit) LayoutInflater.from(getContext()).inflate(R.layout.url_edit, null);
		addView(mEditUrl);
		mToolsPanel = new HorizontalPanel(getContext());
		mToolsPanel.setButtonsType(PanelButton.TYPE_BUTTON_MEDIUM_ONE_LINE);
		mToolsPanel.setCheckWidthWhileNotAutoFill(false);
		addView(mToolsPanel);
//		setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);
		int layoutType = Prefs.getPanelLayoutType();
		SuggestionsAdapter sa = new SuggestionsAdapter(getContext(), R.layout.menu_item);
		sa.setStackFromBottom(layoutType==Prefs.TYPE_LAYOUT_BOTTOM);
		mEditUrl.setAdapter(sa);
		mEditUrl.setOnAutoCompleteAction(getEditUrlListener());
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
		if(a.command==Action.CLEAR_TEXT)
		{
			mEditUrl.setText(null);
			if(!mUserEditText)
				st.showEditKeyboard(mEditUrl);
			return;
		}
		else if(a.command==Action.CANCEL)
		{
			onAddrStateChanged(STATE_ADDR_NONE);
			return;
		}
		else if(a.command==Action.PASTE)
		{
			mEditUrl.setText(stat.getClipboardString(getContext()));
			return;
		}
		else if(a.command==Action.VOICE_SEARCH)
		{
			a.param = this;
			((MainActivity)getContext()).runAction(a);
			return;
		}
		else
			a.setParam(mEditUrl.getText().toString());
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
	// создание панели при нажатии на панель адреса
	public void createToolsActions()
	{
		ActArray ar = new ActArray();
		if(mUserEditText)
			ar.add(Action.create(Action.CANCEL));
		String text = mEditUrl.getText().toString();
		if(TextUtils.isEmpty(text))
		{
			if(!TextUtils.isEmpty(stat.getClipboardString(getContext())))
				ar.add(Action.PASTE);
			if(stat.isVoiceSearchExist(getContext()))
				ar.add(Action.create(Action.VOICE_SEARCH,this));
			BrowserApp.pluginServer.getPluginActions(ar, Plugin.WINDOW_ADDR_EMPTY,this);
		}
		else
		{
			ar.add(Action.create(Action.CLEAR_TEXT));
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
}
