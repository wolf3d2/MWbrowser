package com.jbak.superbrowser.panels;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import ru.mail.mailnews.st;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jbak.superbrowser.ActArray;
import com.jbak.superbrowser.Action;
import com.jbak.superbrowser.Bookmark;
import com.jbak.superbrowser.BrowserApp;
import com.jbak.superbrowser.IConst;
import com.jbak.superbrowser.ImportExport;
import com.jbak.superbrowser.MainActivity;
import com.jbak.superbrowser.Prefs;
import com.mw.superbrowser.R;
import com.jbak.superbrowser.Tab;
import com.jbak.superbrowser.stat;
import com.jbak.superbrowser.adapters.SettingsAdapter;
import com.jbak.superbrowser.adapters.SettingsBookmark;
import com.jbak.superbrowser.recycleview.BookmarkViewRecyclerAdapter;
import com.jbak.superbrowser.recycleview.PanelButtonRecyclerAdapter;
import com.jbak.superbrowser.recycleview.RecyclerViewEx;
import com.jbak.superbrowser.search.SearchSystem;
import com.jbak.superbrowser.ui.BookmarkView;
import com.jbak.superbrowser.ui.HorizontalPanel;
import com.jbak.superbrowser.ui.PanelButton;
import com.jbak.superbrowser.ui.PanelLayout;
import com.jbak.superbrowser.ui.PanelSetting;
import com.jbak.superbrowser.ui.dialogs.DialogEmptyInterface;
import com.jbak.superbrowser.ui.dialogs.DialogToolsPanelSettings;
import com.jbak.superbrowser.ui.themes.MyTheme;
import com.jbak.ui.UIUtils;
import com.jbak.utils.ObjectKeyValues;
// окно настроек
public class InterfaceSettingsLayout implements OnClickListener,IConst{

	public static final int MODE_INTERFACE_SETTINGS = Action.INTERFACE_SETTINGS;
	public static final int MODE_MAGIC_BUTTON_POS = Action.MAGIC_BUTTON_POS;
	public static final int MODE_MAGIC_BUTTON_ALPHA = Action.MODE_MAGIC_BUTTON_ALPHA;
	public static final int MODE_NAVIGATION_PANEL_POS = Action.NAVIGATION_PANEL_POS;
	public static final int MODE_NAVIGATION_PANEL_ALPHA = Action.MODE_NAVIGATION_PANEL_ALPHA;
//	public static final int MODE_NAVIGATION_PANEL_POS = Action.NAVIGATION_PANEL_POS;
//	public static final int MODE_NAVIGATION_PANEL_ALPHA = Action.MODE_NAVIGATION_PANEL_ALPHA;
	public static final int MODE_QUICK_SETTINGS = Action.QUICK_SETTINGS;
	public static final int MODE_TOOLS_PANEL = Action.MINI_PANEL_SETTINGS;
	public static final int MODE_MAINMENU_SETTINGS = Action.MAINMENU_SETTING;
	public static final int MODE_SETTINGS = Action.MAIN_SETTINGS;
	public static final int MODE_WINDOWS_PANEL_SETTINGS = Action.TAB_PANEL_SETTINGS;
	public static int magic_or_navigation = 0;
	public static final int MODE_LAST_RUNNED = 100000;
	public static final ObjectKeyValues<Integer, Integer> MODES = new ObjectKeyValues<Integer, Integer>
	(
		MODE_INTERFACE_SETTINGS,R.string.act_interface,
		MODE_QUICK_SETTINGS,R.string.act_view_settings,
		MODE_SETTINGS,R.string.act_settings,
		MODE_WINDOWS_PANEL_SETTINGS,R.string.act_tab_panel_settings,
		MODE_MAINMENU_SETTINGS,R.string.main_menu_settings,
		MODE_MAGIC_BUTTON_ALPHA,R.string.act_interface
//		MODE_NAVIGATION_PANEL_ALPHA,R.string.act_interface
//		MODE_TOOLS_PANEL,R.string.panelQuickTools
	);
	public static final ObjectKeyValues<Integer, Integer> PLUS_BUTTON_POS = new ObjectKeyValues<Integer, Integer>
	(
			LEFT,R.string.left,
			RIGHT,R.string.right,
			DISABLED,R.string.disabled
	);
	
	
	public static final int POS_RIGHT_BOTTOM = 0;
	public static final int POS_CENTER_BOTTOM = 1;
	public static final int POS_LEFT_BOTTOM = 2;
	public static final int POS_CENTER_RIGHT = 3;
	public static final int POS_CENTER_LEFT = 4;
	public static final int POS_CENTER_TOP = 5;
	public static final int POS_RIGHT_TOP = 6;
	public static final int POS_LEFT_TOP = 7;
	public static final int POS_MAX = 7;
	int mRectSize;
	RelativeLayout mContent;
	RelativeLayout mParent;
	RecyclerViewEx mPanelSettings;
	HorizontalPanel mActionPanel;
	int mMode = MODE_INTERFACE_SETTINGS;
	public InterfaceSettingsLayout(RelativeLayout parent,int mode) {
		set(parent,mode);

	}
	
	void set(RelativeLayout parent,int mode)
	{
		if(mode == MODE_LAST_RUNNED)
			mode = Prefs.getInt(Prefs.MAGIC_BUTTON_LONG_PRESS_START, MODE_INTERFACE_SETTINGS);
		Prefs.setInt(Prefs.MAGIC_BUTTON_LONG_PRESS_START, mode);
		Context context = parent.getContext();
		mContent = new RelativeLayout(context);
		mParent = parent;


		mMode = mode;
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		parent.addView (mContent,lp);
		switch (mMode)
		{
		case MODE_MAGIC_BUTTON_POS:
			createForMagicButtonPos(context);
			break;
		case MODE_NAVIGATION_PANEL_POS:
			createForNavigationPanelPos(context);
			break;
		default:
			createForSettingsAdapter(context);
			break;
		}
//		if(mMode==MODE_MAGIC_BUTTON_POS)
//			createForMagicButtonPos(context);
//		else
//			createForSettingsAdapter(context);
		mContent.setOnClickListener(this);
		mContent.setBackgroundColor(0xcc000000);
		st.fl_temp_hide_navigationpanel=true;
		getMain().hideMagickAndNavigation();
		getMain().setInterface(3);

	}
	// создаём окно настройки волшебной кнопки
	@SuppressLint("NewApi")
	void createForMagicButtonPos(Context context)
	{
		mRectSize = context.getResources().getDimensionPixelSize(R.dimen.magic_button_size);
		TextView tv = (TextView) LayoutInflater.from(context).inflate(R.layout.textview_title, null);
		tv.setText(R.string.magic_button_select_pos);
		tv.setId(R.id.title);
		MyTheme.get().setViews(MyTheme.ITEM_TITLE, tv);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
		lp.topMargin = mRectSize+10;
		lp.leftMargin = mRectSize+10;
		lp.rightMargin = mRectSize+10;
		RelativeLayout.LayoutParams lpAction = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		lpAction.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		lpAction.bottomMargin = mRectSize+10;
		lpAction.leftMargin = mRectSize+10;
		lpAction.rightMargin = mRectSize+10;
		RelativeLayout.LayoutParams lpSettings = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		lpSettings.addRule(RelativeLayout.BELOW,R.id.title);
		lpSettings.addRule(RelativeLayout.ABOVE,R.id.action_panel);
		lpSettings.topMargin = st.dp2px(context, (int)context.getResources().getDimension(R.dimen.magic_padding));
		lpSettings.leftMargin = mRectSize+10;
		lpSettings.rightMargin = mRectSize+10;
		mContent.addView(tv,lp);
		mActionPanel = createActionPanel();
		mContent.addView(mActionPanel,lpAction);
		for(int pos=0;pos<=POS_MAX;pos++)
		{
			View v = new View(context);
			v.setBackgroundResource(R.drawable.ic_launcher);
			UIUtils.setViewsTag(pos, v);
			v.setOnClickListener(this);
			int sz = st.dp2px(context, 40);
			setViewToRelativeLayout(mContent, v, pos,sz,sz);
		}
		mPanelSettings = new RecyclerViewEx(context, RecyclerViewEx.TYPE_VERTICAL_LIST);
		mPanelSettings.setOnUnusedSpaceClickListener(this);
		mPanelSettings.setListTwoColumnsOnWideScreen(false);
		mPanelSettings.setWrapContent(true);
		mContent.addView(mPanelSettings, lpSettings);
		SettingsAdapter sa = createMagicButtonSettingsAdapter(context);
		BookmarkViewRecyclerAdapter ad = new BookmarkViewRecyclerAdapter(sa);
		mPanelSettings.setAdapter(ad);
		ad.setOnClickListener(sa);
		
	}
	@SuppressLint("NewApi")
	void createForNavigationPanelPos(Context context)
	{
		mRectSize = context.getResources().getDimensionPixelSize(R.dimen.magic_button_size);
		TextView tv = (TextView) LayoutInflater.from(context).inflate(R.layout.textview_title, null);
		tv.setText(R.string.navigation_panel_select_pos);
		tv.setId(R.id.title);
		MyTheme.get().setViews(MyTheme.ITEM_TITLE, tv);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
		lp.topMargin = mRectSize+10;
		lp.leftMargin = mRectSize+10;
		lp.rightMargin = mRectSize+10;
		RelativeLayout.LayoutParams lpAction = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		lpAction.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		lpAction.bottomMargin = mRectSize+10;
		lpAction.leftMargin = mRectSize+10;
		lpAction.rightMargin = mRectSize+10;
		RelativeLayout.LayoutParams lpSettings = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		lpSettings.addRule(RelativeLayout.BELOW,R.id.title);
		lpSettings.addRule(RelativeLayout.ABOVE,R.id.action_panel);
		lpSettings.topMargin = st.dp2px(context, (int)context.getResources().getDimension(R.dimen.magic_padding));
		lpSettings.leftMargin = mRectSize+10;
		lpSettings.rightMargin = mRectSize+10;
		mContent.addView(tv,lp);
		mActionPanel = createActionPanel();
		mContent.addView(mActionPanel,lpAction);
		for(int pos=0;pos<=POS_MAX;pos++)
		{
			//Drawable drawable = new BitmapDrawable(getResources(), st.setImageColor(context, iv, R.drawable););
			View v = new View(context);
			v.setBackgroundResource(R.drawable.navigation);
			UIUtils.setViewsTag(pos, v);
			v.setOnClickListener(this);
			int sz = st.dp2px(context, 40);
			setViewToRelativeLayout(mContent, v, pos,sz,sz);
		}
		mPanelSettings = new RecyclerViewEx(context, RecyclerViewEx.TYPE_VERTICAL_LIST);
		mPanelSettings.setOnUnusedSpaceClickListener(this);
		mPanelSettings.setListTwoColumnsOnWideScreen(false);
		mPanelSettings.setWrapContent(true);
		mContent.addView(mPanelSettings, lpSettings);
		SettingsAdapter sa = createNavigationPanelSettingsAdapter(context);
		BookmarkViewRecyclerAdapter ad = new BookmarkViewRecyclerAdapter(sa);
		mPanelSettings.setAdapter(ad);
		ad.setOnClickListener(sa);
		
	}
	void createForSettingsAdapter(Context context)
	{
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setGravity(Gravity.BOTTOM);
		TextView tv = (TextView) LayoutInflater.from(context).inflate(R.layout.textview_title, null);
		tv.setText(MODES.getValueByKey(mMode));
		MyTheme.get().setViews(MyTheme.ITEM_TITLE, tv);
		layout.addView(tv);
		LinearLayout.LayoutParams lpPanelSetings = new LayoutParams(LayoutParams.MATCH_PARENT,0);
		lpPanelSetings.weight = 1f;
		LinearLayout.LayoutParams lpActionPanel = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams lpAdView = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		mActionPanel = createActionPanel();
		mPanelSettings = new RecyclerViewEx(context,RecyclerViewEx.TYPE_VERTICAL_LIST);
//		mPanelSettings.setWrapContent(true);
		mPanelSettings.setOnUnusedSpaceClickListener(this);
		mPanelSettings.setListTwoColumnsOnWideScreen(true);
//		mPanelSettings.setWrapContent(true);
//		MyTheme.get().setViews(MyTheme.ITEM_DIALOG_BACKGROUND, mPanelSettings);
		layout.addView(mPanelSettings,lpPanelSetings);
		layout.addView(mActionPanel, lpActionPanel);
		int padHorz = st.dp2px(context, (int) mContent.getResources().getDimension(R.dimen.magic_padding));
		int padVert = st.dp2px(context, 2);
		mPanelSettings.setPadding(padHorz, padVert, padHorz, padVert);
		MyTheme.get().setViews(MyTheme.ITEM_DIALOG_BACKGROUND, layout);
		SettingsAdapter sa;
		if(mMode==MODE_INTERFACE_SETTINGS)
			sa = createInterfaceSettingsAdapter(context);
		else if(mMode==MODE_SETTINGS)
			sa = createMainSettingsAdapter(context);
		else if(mMode==MODE_WINDOWS_PANEL_SETTINGS)
			sa = createTabPanelAdapter(context);
		else
			sa = createQuickSettingsAdapter(context);
		BookmarkViewRecyclerAdapter ad = new BookmarkViewRecyclerAdapter(sa);
		mPanelSettings.setAdapter(ad);
		ad.setOnClickListener(sa);
		mContent.addView(layout);
//		getMain().hideMagickAndNavigation();
	}
	public HorizontalPanel createActionPanel()
	{
		HorizontalPanel hp =  new HorizontalPanel(mParent.getContext())
		{
			@Override
			public PanelButtonRecyclerAdapter createRecyclerAdapterForActions(
					ActArray actions, int buttonType) {
				return new PanelButtonRecyclerAdapter(actions, buttonType)
				{
					@Override
					public void onPostSetItem(int pos,PanelButton pb,Object tag)
					{
						int index = MODES.getIndexByKey(mMode);
						if(pos==index)
							MyTheme.get().setView(pb, MyTheme.ITEM_PANELBUTTON_SEL);
					}
				};
			}
		};
		hp.setId(R.id.action_panel);
		hp.setButtonsType(PanelButton.TYPE_BUTTON_MEDIUM);
		ActArray actions = new ActArray(MODES.getKeys());
		if(mMode!=MODE_MAGIC_BUTTON_POS)
		{
// создаём кнопки интерфейса
			if(getMain().getPanelLayout().getPanel(PanelLayout.PANEL_QUICK_TOOLS)!=null)
				actions.add(Action.MINI_PANEL_SETTINGS);
			actions.add(Action.NAVIGATION_PANEL_POS);
			actions.add(Action.MAGIC_BUTTON_POS);
			actions.add(Action.SYSTEM_SETTINGS);
			actions.add(Action.SYSTEM_MOBILE_SETTINGS);
			actions.add(Action.SYSTEM_WIFI_NETWORKS);
			actions.removeAction(Action.MODE_MAGIC_BUTTON_ALPHA);
			actions.removeAction(Action.MODE_NAVIGATION_PANEL_ALPHA);
			actions.removeAction(Action.SIZE_BUTTON_NAVIGATION_PANEL);
		}
		if(mMode==MODE_MAGIC_BUTTON_POS)
		{
			actions.removeAction(Action.MAGIC_BUTTON_POS);
			actions.removeAction(Action.TAB_PANEL_SETTINGS);
			actions.removeAction(Action.MAINMENU_SETTING);
			hp.setButtonsType(PanelButton.TYPE_BUTTON_MEDIUM_BIG_WIDTH);
			Action act = null;
// выставляем процент прозрачности волшебной кнопки
			for (int i=0;i<actions.size();i++){
				act= actions.get(i);
				if (act.command !=Action.MODE_MAGIC_BUTTON_ALPHA)
					continue;
				act.itemText = getMain().getBaseContext().getString(R.string.magic_button_select_alpha)
						+st.STR_SPACE+Prefs.get().getInt(Prefs.MAGIC_KEY_ALPHA, 100)+"%";
				break;
			}
		}
		if(mMode==MODE_NAVIGATION_PANEL_POS)
		{
			actions.clear();
			actions.add(Action.MAIN_SETTINGS);
			actions.add(Action.SIZE_BUTTON_NAVIGATION_PANEL);
			actions.add(Action.create(Action.MODE_NAVIGATION_PANEL_ALPHA));
			// пока не сделал
			actions.add(Action.NAVIGATION_PANEL_TEXT_COLOR);
			hp.setButtonsType(PanelButton.TYPE_BUTTON_MEDIUM_BIG_WIDTH);
			Action act = null;
// выставляем процент прозрачности волшебной кнопки
			for (int i=0;i<actions.size();i++){
				act= actions.get(i);
				if (act.command !=Action.MODE_NAVIGATION_PANEL_ALPHA)
					continue;
				act.itemText = getMain().getBaseContext().getString(R.string.magic_button_select_alpha)
						+st.STR_SPACE+Prefs.get().getInt(Prefs.NAVIGATION_PANEL_ALPHA, 100)+"%";
				break;
			}
		}
		if(!PanelLayout.isPanelVisible(PanelLayout.PANEL_TABS)&&mMode!=MODE_WINDOWS_PANEL_SETTINGS){
			actions.removeAction(Action.TAB_PANEL_SETTINGS);
		}
		hp.setActions(actions);
		hp.post(new Runnable() {
			
			@Override
			public void run() {
				int index = MODES.getIndexByKey(mMode);
				if(index>-1)
					mActionPanel.scrollToPosition(index);
			}
		});
		return hp;
	}
	private SettingsAdapter createMainSettingsAdapter(Context context) {
		return new SettingsAdapter(context, SettingsAdapter.getMainSettings(context));
	}
	public static void checkMagicKeyCanDisabled(MainActivity ma)
	{
		if(Prefs.isMagicKeyVisible()||Prefs.getBoolean(Prefs.MENU_KEY_CONFIRMED, false))
			return;
		View v = ma.getPanelLayout().getPanel(PanelLayout.PANEL_QUICK_TOOLS);
		ActArray ar = PanelQuickTools.getMinipanelActions();
		if(v!=null&&ar.has(Action.SHOW_MAIN_PANEL))
			return;
		new DialogEmptyInterface(ma).show();
	}
	public static void checkNavigationPanelCanDisabled(MainActivity ma)
	{
		if(Prefs.isNavigationPanelVisible())
			return;
// от magic key		
//		View v = ma.getPanelLayout().getPanel(PanelLayout.PANEL_QUICK_TOOLS);
//		ActArray ar = PanelQuickTools.getMinipanelActions();
//		if(v!=null&&ar.has(Action.SHOW_MAIN_PANEL))
//			return;
//		new DialogEmptyInterface(ma).show();
	}
// создаём быстрые настройки
	public SettingsAdapter createQuickSettingsAdapter(Context context)
	{
		final Tab ww = getMain().getTab();
		ArrayList<Bookmark>ar = new ArrayList<Bookmark>();
		ar.add(new SettingsBookmark(context, Prefs.FULLSCREEN, R.string.act_fullscreen, Boolean.valueOf(Prefs.getFullscreen()),R.string.yes,R.string.no, false));
		SettingsBookmark wwcolor =  new SettingsBookmark(context, Prefs.WEBWIEW_BACKGROUND_COLOR, R.string.act_ww_back, 
				Prefs.getWWBackgroundColorName(context)
				);
		ar.add(wwcolor);
		ar.add(new SettingsBookmark(context, IConst.VIEW_TYPE, R.string.act_full_view, Boolean.valueOf(ww.getViewType()==Tab.VIEW_TYPE_FULL),R.string.yes,R.string.no, true));
		ar.add(new SettingsBookmark(context, IConst.WS_IMAGES_ENABLED, R.string.act_images_enabled, Boolean.valueOf(ww.imagesEnabled),R.string.yes,R.string.no, false));
		ar.add(new SettingsBookmark(context, IConst.WS_PREVIEW_ENABLED, R.string.act_preview_enabled, Boolean.valueOf(ww.previewEnabled),R.string.yes,R.string.no, false));

		int longClick = R.string.act_longclick_default;
		int lc = Prefs.getLongClick();
		if(lc==Prefs.LONGCLICK_CONTEXT_MENU)
			longClick = R.string.act_longclick_context_menu;
		if(lc==Prefs.LONGCLICK_TEXT_SELECTION)
			longClick = R.string.act_longclick_text_select;
		ar.add(new SettingsBookmark(context, Prefs.LONGCLICK, R.string.act_longclick, longClick));

		String desc = context.getString(R.string.supermenu_button_def);
		if (Prefs.get().getInt(Prefs.SUPERMENU_BUTTON_SET, 0) == 1)
			desc = context.getString(R.string.supermenu_button_vert8inch);
		SettingsBookmark sb = new SettingsBookmark(context, Prefs.SUPERMENU_BUTTON_SET, R.string.supermenu_button_set, 
				desc
				);
		ar.add(sb);
		int vkRes = R.string.act_volume_keys_none;
		int vol = Prefs.getVolumeKeysState();
		if(vol==Prefs.VOLUME_KEYS_SCROLL)
			vkRes = R.string.act_volume_keys_scroll;
		if(vol==Prefs.VOLUME_KEYS_FONT_RESIZE)
			vkRes = R.string.act_volume_keys_resize;
		ar.add(new SettingsBookmark(context, Prefs.VOLUME_KEYS_STATE, R.string.act_volume_keys_menu, vkRes));
		int minFont = Prefs.get().getInt(Prefs.MIN_FONT, 8);
		String text = st.STR_NULL+minFont;
		ar.add(new SettingsBookmark(context, Prefs.MIN_FONT, R.string.act_min_font, text));
// клик по настройке в быстрых настройках		
		SettingsAdapter sa = new SettingsAdapter(context, ar)
		{
			@Override
			public void onCheckboxChanged(BookmarkView bv, SettingsBookmark sb) {
				super.onCheckboxChanged(bv, sb);
				if(Prefs.FULLSCREEN.equals(sb.prefKey))
					getMain().setFullscreen(sb.checkBox, true);
				else if(IConst.VIEW_TYPE.equals(sb.prefKey))
					ww.setViewType(sb.checkBox?Tab.VIEW_TYPE_FULL:Tab.VIEW_TYPE_SMARTPHONE, true);
				else if(IConst.WS_IMAGES_ENABLED.equals(sb.prefKey))
				{
					WebSettings ws = getMain().getWebView().getSettings();
					ws.setBlockNetworkImage(!sb.checkBox);
					getMain().getTab().refreshSettings();
				}
				else if(IConst.WS_PREVIEW_ENABLED.equals(sb.prefKey))
				{
					WebSettings ws = getMain().getWebView().getSettings();
					ws.setLoadWithOverviewMode(sb.checkBox);
					getMain().getTab().refreshSettings();
				}
			}
			@Override
			public void onSettingClick(SettingsBookmark set) {
				super.onSettingClick(set);
				if (set.tab_load)
					dismiss();
				else if(Prefs.VOLUME_KEYS_STATE.equals(set.prefKey))
					showMenuTextIds(set, new OnMenuItemSelected() {
						
						@Override
						public void onMenuItemSelected(int selectedIndex,SettingsBookmark settingsEdit, SettingsBookmark settingsSelected) {
							settingsEdit.setDesc(settingsSelected.getTitle());
							Prefs.setVolumeKeysState(selectedIndex);
						}
					}, R.string.act_volume_keys_none,R.string.act_volume_keys_scroll,R.string.act_volume_keys_resize);
				else if(Prefs.LONGCLICK.equals(set.prefKey))
				{
					showMenuTextIds(set, new OnMenuItemSelected() {
						
						@Override
						public void onMenuItemSelected(int selectedIndex,SettingsBookmark settingsEdit, SettingsBookmark settingsSelected) {
							settingsEdit.setDesc(settingsSelected.getTitle());
							switch (selectedIndex) {
							case 0:
								Prefs.setLongClick(Prefs.LONGCLICK_DEFAULT);
								break;
							case 1:
								Prefs.setLongClick(Prefs.LONGCLICK_TEXT_SELECTION);
								break;
							case 2:
								Prefs.setLongClick(Prefs.LONGCLICK_CONTEXT_MENU);
								break;
							}
							dismiss();

						}
					}, R.string.act_longclick_default,R.string.act_longclick_text_select,R.string.act_longclick_context_menu);
					
				}
				else if(Prefs.SUPERMENU_BUTTON_SET.equals(set.prefKey))
				{
					showMenuTextIds(set, new OnMenuItemSelected() {
						
						@Override
						public void onMenuItemSelected(int selectedIndex,SettingsBookmark settingsEdit, SettingsBookmark settingsSelected) {
							settingsEdit.setDesc(settingsSelected.getTitle());
							switch (selectedIndex) {
							case 0:
								Prefs.get().edit().putInt(Prefs.SUPERMENU_BUTTON_SET, 0).commit();
								break;
							case 1:
								Prefs.get().edit().putInt(Prefs.SUPERMENU_BUTTON_SET, 1).commit();
								break;
							}
							dismiss();

						}
					}, R.string.supermenu_button_def,R.string.supermenu_button_vert8inch);
					
				}
				else if(Prefs.MIN_FONT.equals(set.prefKey))
				{
					dismiss();
					getMain().runAction(Action.create(Action.MIN_FONT_SIZE));
				}
				}
		};
		return sa;
	}
	public SettingsAdapter createMagicButtonSettingsAdapter(Context context)
	{
		ArrayList<Bookmark>ar = new ArrayList<Bookmark>();
		boolean visible = Prefs.isMagicKeyVisible();
		SettingsBookmark s = new SettingsBookmark(context, Prefs.MAGIC_BUTTON_VISIBLE, R.string.act_interface_magic_button, Boolean.valueOf(visible),R.string.visible,R.string.disabled, false);
		ar.add(s);
		SettingsAdapter sa = new SettingsAdapter(context, ar)
		{
			public void onRightButtonClick(SettingsBookmark sb) 
			{
				dismiss();
				getMain().runAction(Action.create(Action.MINI_PANEL_SETTINGS));
			};
			public void onCheckboxChanged(com.jbak.superbrowser.ui.BookmarkView bv, SettingsBookmark sb) 
			{
				if(Prefs.MAGIC_BUTTON_VISIBLE.equals(sb.prefKey))
				{
					Prefs.setBoolean(sb.prefKey, sb.checkBox);
					((MainActivity)getContext()).setInterface(1);
					checkMagicKeyCanDisabled((MainActivity)getContext());
				}
			};
			@Override
			public void onSettingClick(SettingsBookmark b) {
				if(b.prefKey==Prefs.MAGIC_BUTTON_POS)
				{
					dismiss();
					set(mParent,MODE_MAGIC_BUTTON_POS);
					return;
				}
				super.onSettingClick(b);
			}
		};
		magic_or_navigation = 1;
		return sa;
	}
	public SettingsAdapter createNavigationPanelSettingsAdapter(Context context)
	{
		ArrayList<Bookmark>ar = new ArrayList<Bookmark>();
		boolean visible = Prefs.isNavigationPanelVisible();
		SettingsBookmark s = new SettingsBookmark(context, Prefs.NAVIGATION_PANEL_VISIBLE, R.string.set_navi_page_panel, Boolean.valueOf(visible),R.string.visible,R.string.disabled, false);
		ar.add(s);
		SettingsAdapter sa = new SettingsAdapter(context, ar)
		{
			public void onRightButtonClick(SettingsBookmark sb) 
			{
				dismiss();
				getMain().runAction(Action.create(Action.MINI_PANEL_SETTINGS));
			};
			public void onCheckboxChanged(com.jbak.superbrowser.ui.BookmarkView bv, SettingsBookmark sb) 
			{
				if(Prefs.NAVIGATION_PANEL_VISIBLE.equals(sb.prefKey))
				{
					Prefs.setBoolean(sb.prefKey, sb.checkBox);
					if (sb.checkBox){
						Prefs.setBoolean(Prefs.NAVIGATION_PANEL_VISIBLE, true);
						Prefs.setInt(Prefs.SET_NAVIGATION, 1);
						st.pref_navigation = 1;
					}else{
						Prefs.setBoolean(Prefs.NAVIGATION_PANEL_VISIBLE, false);
						Prefs.setInt(Prefs.SET_NAVIGATION, 2);
						st.pref_navigation = 0;
					}
					((MainActivity)getContext()).setInterface(2);
					checkNavigationPanelCanDisabled((MainActivity)getContext());
				}
			};
			@Override
			public void onSettingClick(SettingsBookmark b) {
				if(b.prefKey==Prefs.NAVIGATION_PANEL_POS)
				{
					dismiss();
					set(mParent,MODE_NAVIGATION_PANEL_POS);
					return;
				}
				super.onSettingClick(b);
			}
		};
		magic_or_navigation = 2;
		return sa;
	}
	public SettingsAdapter createTabPanelAdapter(Context context)
	{
		PanelSetting ps = PanelLayout.getPanelSettings().getPanelSetting(PanelLayout.PANEL_TABS);
		JSONObject tabPanelSettings = ps.extraSettings;
		ArrayList<Bookmark>ar = new ArrayList<Bookmark>();
		SettingsBookmark bookmark=null;
		int rows = tabPanelSettings.optInt(IConst.MAX_TAB_ROWS, 1);
		bookmark = new SettingsBookmark(context, IConst.MAX_TAB_ROWS, R.string.tab_rows, 
				st.STR_NULL+rows);
		ar.add(bookmark.setParam(ps));
		bookmark = new SettingsBookmark(context, IConst.MIN_TAB_WIDTH,rows==1?R.string.tab_min_width:R.string.tab_width, 
				st.STR_NULL+tabPanelSettings.optInt(IConst.MIN_TAB_WIDTH, 0));
		ar.add(bookmark.setParam(ps));
		if(rows==1)
			ar.add(new SettingsBookmark(context, IConst.MAX_TAB_WIDTH, R.string.tab_max_width, 
					st.STR_NULL+tabPanelSettings.optInt(IConst.MAX_TAB_WIDTH,0)).setParam(ps));
		ar.add(new SettingsBookmark(context, Prefs.TABS_HEIGTH, R.string.tabs_big, Prefs.isTabsHeight(),R.string.yes,R.string.no, false));
		int pos = tabPanelSettings.optInt(IConst.PLUS_BUTTON, IConst.RIGHT);
		bookmark = new SettingsBookmark(context, IConst.PLUS_BUTTON,R.string.plus_button, PLUS_BUTTON_POS.getValueByKey(pos));
		ar.add(bookmark.setParam(ps));
		SettingsAdapter sa = new SettingsAdapter(context, ar)
		{
			int mOldRows = 1;
			void updateWindowsPanel(SettingsBookmark settingsEdit,SettingsBookmark settingsSelected)
			{
				try{
					PanelSetting ps = (PanelSetting)settingsEdit.param;
					settingsEdit.setDesc(settingsSelected.getTitle());
					int rows = ps.extraSettings.optInt(MAX_TAB_ROWS, 1);
					int min = ps.extraSettings.optInt(MIN_TAB_WIDTH, 1);
					int max = ps.extraSettings.optInt(MAX_TAB_WIDTH, 1);
					boolean refresh = MAX_TAB_ROWS.equals(settingsEdit.prefKey); 
					if(max<min)
					{
						if(MIN_TAB_WIDTH.equals(settingsEdit.prefKey))
							ps.extraSettings.putOpt(MAX_TAB_WIDTH, min);
						else if(MAX_TAB_WIDTH.equals(settingsEdit.prefKey))
							ps.extraSettings.putOpt(MIN_TAB_WIDTH, max);
						refresh = true;
					}
					if(rows>1&&mOldRows==1)
					{
						min = (min+max)/2;
						ps.extraSettings.put(MIN_TAB_WIDTH, min);
					}
					PanelLayout.getPanelSettings().setPanel(ps);
					PanelLayout.getPanelSettings().reload();
					BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_SETTINGS_CHANGED, IConst.PANEL_WINDOWS);
					if(refresh)
					{
						dismiss();
						getMain().showInterfaceSettings(mMode);
					}
				}
				catch(Throwable e)
				{
					e.printStackTrace();
				}

			}
			final void applyValue(SettingsBookmark settingsEdit, SettingsBookmark settingsSelected)
			{
				PanelSetting ps = (PanelSetting)settingsEdit.param;
				int val = (Integer)settingsSelected.param;
				try {
					if(MAX_TAB_ROWS.equals(settingsEdit.prefKey))
						mOldRows = ps.extraSettings.optInt(settingsEdit.prefKey,1);
					ps.extraSettings.putOpt(settingsEdit.prefKey,val);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				updateWindowsPanel(settingsEdit, settingsSelected);
				//onSettingChanged(settingsEdit);
			}
			OnMenuItemSelected getMenuListener()
			{
				return new OnMenuItemSelected() {
					
					@Override
					public void onMenuItemSelected(int selectedIndex,SettingsBookmark settingsEdit, SettingsBookmark settingsSelected) {
						applyValue(settingsEdit, settingsSelected);
					}
				};
			}
			@Override
			public void onSettingClick(SettingsBookmark b) {
				if(IConst.MAX_TAB_WIDTH.equals(b.prefKey)||IConst.MIN_TAB_WIDTH.equals(b.prefKey))
				{
					PanelSetting ps = (PanelSetting)b.param;
					int val = ps.extraSettings.optInt(b.prefKey);
					showMenu(b, getMenuListener(), getNumberedSettingsBookmarks(val,20,80,600));
					return;
				}
				if(IConst.MAX_TAB_ROWS.equals(b.prefKey))
				{
					ArrayList<Bookmark> ar = new ArrayList<Bookmark>();
					for(int i=1;i<=4;i++)
						ar.add(getBookmarkForInteger(Integer.valueOf(i)));
					showMenu(b, getMenuListener(), ar);
					return;
				}
				if(IConst.PLUS_BUTTON.equals(b.prefKey))
				{
					showMenuTextIds(b, new OnMenuItemSelected() {
						
						@Override
						public void onMenuItemSelected(int selectedIndex,SettingsBookmark settingsEdit, SettingsBookmark settingsSelected) {
							int val = PLUS_BUTTON_POS.getKeyByIndex(selectedIndex);
							PanelSetting ps = (PanelSetting)settingsEdit.param;
							try {
								ps.extraSettings.putOpt(PLUS_BUTTON, val);
							} catch (JSONException e) {
								e.printStackTrace();
							}
							updateWindowsPanel(settingsEdit, settingsSelected);
						}
					}, PLUS_BUTTON_POS.getValues());
				}
			}
		};
		return sa;
	}
// окно настроек интерфейса
	public SettingsAdapter createInterfaceSettingsAdapter(Context context)
	{
		ArrayList<Bookmark>ar = new ArrayList<Bookmark>();
		ar.add(new SettingsBookmark(context, Prefs.EXTENDED_PROGRESS, R.string.loading_indicator, Prefs.isExtendedProgress()?R.string.big:R.string.small));
		SettingsBookmark nav = new SettingsBookmark(context, Prefs.SET_NAVIGATION, R.string.set_navi_page, 
				Prefs.getNavigationName(context)
				);
		ar.add(nav);
		
		SettingsAdapter.createFromPanelSettings(context, ar, PanelLayout.getPanelSettings());
		
		SettingsAdapter sa = new SettingsAdapter(context, ar)
		{
			public void onRightButtonClick(SettingsBookmark sb) 
			{
// закрываем окно настроек
//				dismiss();
				if(sb.getPanelSetting().id==PanelLayout.PANEL_QUICK_TOOLS)
					new DialogToolsPanelSettings(mParent.getContext(), PanelQuickTools.getMinipanelActions()).show();
				else if(sb.getPanelSetting().id==PanelLayout.PANEL_TABS){
					dismiss();
					getMain().showInterfaceSettings(MODE_WINDOWS_PANEL_SETTINGS);
				}
			};
			public void onCheckboxChanged(com.jbak.superbrowser.ui.BookmarkView bv, SettingsBookmark sb) 
			{
				if(Prefs.MAGIC_BUTTON_VISIBLE.equals(sb.prefKey))
				{
					Prefs.setBoolean(Prefs.MAGIC_BUTTON_VISIBLE, sb.checkBox);
					((MainActivity)getContext()).setInterface(1);
					checkMagicKeyCanDisabled((MainActivity)getContext());
				}
				if(Prefs.NAVIGATION_PANEL_VISIBLE.equals(sb.prefKey))
				{
					Prefs.setBoolean(Prefs.NAVIGATION_PANEL_VISIBLE, sb.checkBox);
					((MainActivity)getContext()).setInterface(2);
					checkNavigationPanelCanDisabled((MainActivity)getContext());
				}
			};
			@Override
			public void onSettingClick(SettingsBookmark b) {
				if(b.prefKey==Prefs.EXTENDED_PROGRESS)
				{
					showMenuTextIds(b, new OnMenuItemSelected() {
						@Override
						public void onMenuItemSelected(int selectedIndex,SettingsBookmark settingsEdit, SettingsBookmark settingsSelected) {
							Prefs.setBoolean(Prefs.EXTENDED_PROGRESS, selectedIndex==1);
							if(mParent.getContext() instanceof MainActivity)
								((MainActivity)mParent.getContext()).setProgressType();
							settingsEdit.setDesc(settingsSelected.getTitle());
						}
					}, R.string.small,R.string.big);
					return;
				}
				else if(b.prefKey==Prefs.SET_NAVIGATION)
				{
					showMenuTextIds(b, new OnMenuItemSelected() {
						@Override
						public void onMenuItemSelected(int selectedIndex,SettingsBookmark settingsEdit, SettingsBookmark settingsSelected) {
							Prefs.setInt(Prefs.SET_NAVIGATION, selectedIndex);
							if (selectedIndex ==1)
								Prefs.setBoolean(Prefs.NAVIGATION_PANEL_VISIBLE, true);
							else
								Prefs.setBoolean(Prefs.NAVIGATION_PANEL_VISIBLE, false);
							st.pref_navigation = selectedIndex;
							if(mParent.getContext() instanceof MainActivity)
								((MainActivity)mParent.getContext()).setNavigationPanelPos(Prefs.getNavigationPanelPos());
							settingsEdit.setDesc(settingsSelected.getTitle());
						}
					}, R.string.disabled,R.string.set_navi_page_panel,R.string.set_navi_gest);
					return;
				}
				if(b.prefKey==Prefs.MAGIC_BUTTON_POS)
				{
					dismiss();
					set(mParent,MODE_MAGIC_BUTTON_POS);
					return;
				}
				super.onSettingClick(b);
			}
		};
		return sa;
	}
	public static void setViewToRelativeLayout(RelativeLayout layout,View view,int pos,int width,int height)
	{
		RelativeLayout.LayoutParams lp = getLayoutParamsForPos(layout.getContext(), pos, width, height);
		if (view.getId() == R.id.navigationLayout&&(pos==POS_CENTER_BOTTOM||pos==POS_CENTER_TOP)){
			int margin = layout.getContext().getResources().getDimensionPixelSize(R.dimen.magic_button_margin);
			if (pos == POS_CENTER_BOTTOM)
				lp.bottomMargin = margin;
			if (pos == POS_CENTER_TOP)
				lp.topMargin = margin+20;
			
		}
		layout.addView(view,lp);
	}
	public static RelativeLayout.LayoutParams getLayoutParamsForPos(Context c,int pos,int w,int h)
	{
		int margin = c.getResources().getDimensionPixelSize(R.dimen.magic_button_margin);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(w, h);
		switch (pos) {
		case POS_RIGHT_BOTTOM:
				lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				lp.bottomMargin = margin;
				lp.alignWithParent = true;
			break;
		case POS_LEFT_BOTTOM:
			lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			lp.bottomMargin = margin;
			lp.alignWithParent = true;
		break;
		case POS_CENTER_BOTTOM:
			lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
		break;
		case POS_CENTER_RIGHT:
			lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			lp.addRule(RelativeLayout.CENTER_VERTICAL);
		break;
		case POS_CENTER_LEFT:
			lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			lp.addRule(RelativeLayout.CENTER_VERTICAL);
		break;
		case POS_LEFT_TOP:
			lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			lp.topMargin = margin;
		break;
		case POS_RIGHT_TOP:
			lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			lp.topMargin = margin;
		break;
		case POS_CENTER_TOP:
			lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
			lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		break;
		default:
			break;
		}
		return lp;
		
	}
	public void dismiss()
	{
		st.fl_temp_hide_navigationpanel=false;
		getMain().setInterface(2);
		mParent.removeView(mContent);
		if (Prefs.isUpdateSaveSettingAndBookmark())
			new ImportExport(getMain()).export(null, false);
	}
	public void onPositionSelected(int pos)
	{
		switch (magic_or_navigation)
		{
		case 1:
			getMain().setMagicButtonPos(pos);
			break;
		case 2:
			getMain().setNavigationPanelPos(pos);
			break;
		}
		magic_or_navigation = 0;
	}
	public final MainActivity getMain()
	{
		return (MainActivity)mParent.getContext();
	}
	@Override
	public void onClick(View v) {
		if(v.getTag() instanceof Integer)
		{
			int pos = (Integer)v.getTag();
			dismiss();
			onPositionSelected(pos);
		}
		else
		{
			dismiss();
		}
	}
}
