package com.jbak.superbrowser;


import com.mw.superbrowser.R;

import java.io.File;

import org.json.JSONObject;

import ru.mail.mailnews.st;
import ru.mail.webimage.FileUtils;
import ru.mail.webimage.WebDownload;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.widget.EditText;
import android.widget.TextView;

import com.jbak.superbrowser.stat.DownloadOptions;
import com.jbak.superbrowser.UrlProcess.DownloadFileInfo;
import com.jbak.superbrowser.panels.InterfaceSettingsLayout;
import com.jbak.superbrowser.panels.PanelMainMenu;
import com.jbak.superbrowser.panels.PanelQuickTools;
import com.jbak.superbrowser.search.SearchSystem;
import com.jbak.superbrowser.ui.LoadBitmapInfo;
import com.jbak.superbrowser.ui.MenuPanelButton;
import com.jbak.superbrowser.ui.MyWebView;
import com.jbak.superbrowser.ui.OnAction;
import com.jbak.superbrowser.ui.dialogs.DialogAbout;
import com.jbak.superbrowser.ui.dialogs.DialogExport;
import com.jbak.superbrowser.ui.dialogs.DialogImport;
import com.jbak.superbrowser.ui.dialogs.DialogDownloadFile;
import com.jbak.superbrowser.ui.dialogs.DialogEditor;
import com.jbak.superbrowser.ui.dialogs.DialogMainMenuParagraph;
import com.jbak.superbrowser.ui.dialogs.DialogThemeSelector;
import com.jbak.superbrowser.ui.dialogs.DialogToolsPanelSettings;
import com.jbak.ui.CustomPopup;
import com.jbak.utils.Utils;


@SuppressLint("UseSparseArrays")
public class Action {
	public static final int EXIT = 1;
	public static final int GO = 2;
	public static final int CLEAR = 3;
	public static final int GO_BACK = 4;
	public static final int HISTORY = 5;
	public static final int COPY_URL_TO_CLIPBOARD = 6;
	public static final int VIEW_FULL = 7;
	public static final int VIEW_SMARTPHONE= 8;
	public static final int TO_TOP= 9;
	public static final int TO_BOTTOM= 10;
	public static final int REFRESH= 11;
	public static final int SEARCH_ON_PAGE= 12;
	public static final int GO_FORWARD = 13;
	public static final int SEARCH_SITE = 14;
	public static final int BOOKMARKS = 15;
// добавляем закладку
	public static final int ADD_BOOKMARK = 16;
	public static final int SEARCH_ACTION = 17;
	public static final int TAB_LIST = 18;
	public static final int NEW_TAB = 19;
	public static final int TAB_HISTORY = 20;
	public static final int SHARE_ELEMENT= 21;
//	public static final int URL_ENCODER= 22;
	public static final int GIS2_SEARCH= 23;
	public static final int QUICK_SETTINGS= 24;
	public static final int IMAGES_ENABLED= 25;
	public static final int IMAGES_DISABLED= 26;
	public static final int FONT_SCALE= 27;
	public static final int FONT_SCALE_SETTINGS= 28;
	public static final int PREVIEW_ENABLED= 29;
	public static final int PREVIEW_DISABLED= 30;
	public static final int SAVE_IMAGE_TO_GALLERY= 31;
	public static final int ACTION_BOOKMARK= 32;
	public static final int CLOSE_TAB= 33;
	public static final int OPEN_TAB= 34;
	public static final int CLOSE_ALL_TABS= 35;
	public static final int SHOW_CLOSED_TABS= 36;
	public static final int DELETE_BOOKMARK= 37;
	public static final int GO_HOME= 38;
	public static final int GO_UP= 39;
	public static final int NEW_FOLDER= 40;
	public static final int FULLSCCREEN= 41;
	public static final int FULLSCREEN_DISABLED= 42;
	public static final int OPENFILE= 43;
	public static final int SAVEFILE= 44;
	public static final int SELECT_FOLDER= 45;
	public static final int THEMES_SELECTOR= 46;
	public static final int THEME= 47;
	public static final int VOLUME_KEYS_MENU= 48;
	public static final int VOLUME_KEYS_STATE= 49;
	public static final int DELETE_FOLDER= 50;
	public static final int STOP= 51;
	public static final int CLEAR_TEXT= 52;
	public static final int SHOW_MENU= 53;
	public static final int INTERFACE_MENU= 54;
	public static final int INTERFACE= 55;
	public static final int ABOUT= 56;
	public static final int FEEDBACK= 57;
	public static final int APP_MARKET= 58;
	public static final int OTHER_APPS= 59;
	public static final int SOURCE_CODE= 60;
	public static final int SELECT_TEXT= 61;
	public static final int ITEM_TEXT= 62;
	public static final int FOUR_PDA= 65;
	public static final int APPLY_TEXT= 66;
	public static final int START_APP= 67;
	public static final int START_APP_LAST_TAB= 68;
	public static final int START_APP_HOME_SCREEN= 69;
	public static final int EDIT= 70;
	public static final int MIN_FONT_SIZE= 71;
	public static final int MIN_FONT= 72;
	public static final int MAIN_SETTINGS= 73;
	public static final int CLEAR_DATA= 74;
	public static final int OK= 75;
	public static final int CANCEL= 76;
	public static final int YES= 77;
	public static final int NO= 78;
	public static final int DOWNLOAD_LIST= 79;
	public static final int VOICE_SEARCH= 80;
	public static final int COPY_TEXT= 81;
	public static final int PASTE= 82;
	public static final int SHOW_MAIN_PANEL= 83;
	public static final int INTERFACE_SETTINGS= 84;
	public static final int ACTION_PLUGIN= 85;
	public static final int DELETE_ITEM= 86;
	public static final int MAGIC_BUTTON_POS= 87;
	public static final int SYSTEM_SETTINGS= 88;
	public static final int SYSTEM_MOBILE_SETTINGS= 89;
	public static final int SYSTEM_WIFI_NETWORKS= 90;
	public static final int MINI_PANEL_SETTINGS= 91;
	public static final int TAB_PANEL_SETTINGS= 93;
	public static final int BACKGROUND_TAB= 94;
	public static final int HISTORY_VIDEO= 95;
	public static final int HISTORY_SAVED_PAGES= 96;
	public static final int CODEPAGE = 97;
	public static final int HOME = 98;
	public static final int END = 99;
	public static final int PGDN = 100;
	public static final int PGUP = 101;
	public static final int TOSTART = 102;
	public static final int TOEND = 103;
	public static final int INSTALL_JBAK2KEYBOARD = 104;
	public static final int TRANSLATE_LINK = 105;
	public static final int SHARE_URL= 106;
	public static final int INSTALL_MWCOSTS = 107;
	public static final int IMPORT = 108;
	public static final int EXPORT = 109;
	public static final int OPEN_ALL_BOOKMARK= 111;
	public static final int MAINMENU_SETTING= 112;
	public static final int COPY_ALL_OPEN_URL= 113;
	public static final int MODE_MAGIC_BUTTON_ALPHA= 114;
	public static final int HELP= 115;
	// системы перевода урлов
	public static final int TRANSLATE_LINK_GOOGLE= 116;
	public static final int TRANSLATE_LINK_YANDEX= 117;
	public static final int TRANSLATE_LINK_TRANSLATE_RU= 118;

	public static final int NAVIGATION_PANEL_POS= 119;
	public static final int NAVIGATION_PANEL_TEXT_COLOR= 120;
	public static final int MODE_NAVIGATION_PANEL_ALPHA= 121;
	public static final int SIZE_BUTTON_NAVIGATION_PANEL = 122;
	public static final int SELECT_WW_BACK_COLOR = 123;
	public static final int SUPERMENU_BUTTON_SET = 124;
	public static final int INSTALL_MWSHARE2SAVE = 125;
	public static final int COPY_TEXT_URL_TO_CLIPBOARD= 126;
	public static final int WHATS_NEW = 127;
	public static final int EXTERNAL_VIDEO_PLAYER = 128;
	/** Копировать ссылку сетевого потока */
	public static final int COPY_NET_STRIMING_URL = 129;

	public static final int MIN_FONT_RANGE[] = new int[]{1,5,6,7,8,9,10,11,12,13,14,16,18,20,22,24,30,32,40,48,60,72};
	
	public int command;
	public int viewId;
	public int textId;
	public Object param;
	public int imageRes;
	public int smallImageRes=0;
	public String itemText;
	public boolean closePanel = true;
	public Object param2;
	public Drawable drw;
	
	public Action(int command,int viewId,int textId,Object param,int imageRes) {
		this(command, viewId, textId, param, imageRes, 0);
	}
	public Action(int command,String text,Drawable drw,Object param2) {
		this.command = command;
		this.itemText = text;
		this.drw = drw;
		this.param2 = param2;
	}
	public Action(int command,int viewId,int textId,Object param,int imageRes,int smallImageRes) {
		this.command = command;
		this.viewId = viewId;
		this.textId = textId;
		this.param = param;
		this.imageRes = imageRes;
		this.smallImageRes = smallImageRes;
	}
	public boolean closePanel()
	{
		boolean leave = command==CLEAR||!closePanel;
		return !leave;
	}
	public Action setClosePanel(boolean close)
	{
		closePanel = close;
		return this;
	}
	public String getText(Context c)
	{
		if(!TextUtils.isEmpty(itemText))
			return itemText;
		return c.getString(textId);
	}
	public Action setText(int res)
	{
		textId = res;
		return this;
	}
	public Action setImageRes(int res)
	{
		imageRes = res;
		return this;
	}
	public Action setText(String text)
	{
		itemText = text;
		return this;
	}
	public Action setParam(Object param)
	{
		this.param = param;
		return this;
	}
	public Action setParam2(Object param2)
	{
		this.param2 = param2;
		return this;
	}
	public boolean doAction(MainActivity act)
	{
		return false;
	}
	public static Action create(int action)
	{
		return create(action,null);
	}
	public static Action create(int action,Object param)
	{
		switch (action) {
		case SUPERMENU_BUTTON_SET:
			return new Action(action,action,R.string.act_translate,param,R.drawable.translate)
			{
				public boolean doAction(final MainActivity activity){
					ActArray ar = new ActArray();
					ar.add(Action.create(TRANSLATE_LINK_TRANSLATE_RU,"Translate.ru").setText("Translate.ru"));
					ar.add(Action.create(TRANSLATE_LINK_YANDEX,"Yandex").setText("Yandex"));
					ar.add(Action.create(TRANSLATE_LINK_GOOGLE,"Google").setText("Google"));
					new MenuPanelButton(activity, ar, new OnAction() 
					{
						@Override
						public void onAction(Action act) 
						{
							translateUrl(activity,act);
						}
					}).show();
					return true;
				};
			};
		case TRANSLATE_LINK:
			return new Action(action,action,R.string.act_translate,param,R.drawable.translate)
			{
				public boolean doAction(final MainActivity activity){
					ActArray ar = new ActArray();
					ar.add(Action.create(TRANSLATE_LINK_TRANSLATE_RU,"Translate.ru").setText("Translate.ru"));
					ar.add(Action.create(TRANSLATE_LINK_YANDEX,"Yandex").setText("Yandex"));
					ar.add(Action.create(TRANSLATE_LINK_GOOGLE,"Google").setText("Google"));
					new MenuPanelButton(activity, ar, new OnAction() 
					{
						@Override
						public void onAction(Action act) 
						{
							translateUrl(activity,act);
						}
					}).show();
					return true;
				};
			};
		case WHATS_NEW:
			return new Action(action,action,R.string.act_whatsnew,param,R.drawable.edit,R.drawable.help_blue_button)
//			return new Action(action, , R.string.act_whatsnew, R.string.act_whatsnew, param,R.drawable.help_blue_button)
			{
				@Override
				public boolean doAction(MainActivity act) {
					// так будет получше
					ActArray ar = new ActArray();
					ar.add(Action.create(Action.ABOUT));
					ar.add(Action.create(Action.HELP));
					ar.add(Action.create(Action.CANCEL));
					//ar.add(Action.create(Action));
					new DialogEditor(act, act.getString(R.string.act_whatsnew), act.getWhatsNew(), ar).show();
					// старый Что нового
					//st.dialogHelp(act, act.getWhatsNew(), act.getString(R.string.act_whatsnew));
					return true;
				}
			};
		
		case ABOUT:
			return new Action(action,action,R.string.act_about,param,R.drawable.about)
			{
				@Override
				public boolean doAction(MainActivity act) {
					ActArray ar = new ActArray();
					ar.add(Action.create(Action.HELP));
					ar.add(Action.create(Action.FOUR_PDA));
					ar.add(Action.create(Action.FEEDBACK));
					ar.add(Action.create(Action.OTHER_APPS));
					ar.add(Action.create(Action.WHATS_NEW));

					new DialogAbout(act, null,
							act.getString(R.string.about_text),
							ar)
					.show();
					return true;
				}
			};
		case HELP:
			return new Action(action,action, R.string.act_help, param,R.drawable.help_blue_button)
			{
				@Override
				public boolean doAction(MainActivity act) {
					String help = st.getTextAssets(act, "_help.htm");
					if (help!=null) {
						Tab ww = new Tab(act.activeInstance, act.getTabList().getNewTabId(),act.getTabList());
						act.tabStart(ww, null);
						
						act.closeEmptyWindow();
						act.tabStart(ww,"helpToBrowser",false);
						ww.getWebView().loadDataWithBaseURL(null, help, "text/html", "utf-8", null);
						//ww.getWebView().loadUrl(help);//.openUrl((String)act.param,act.command);
						//PanelUrlEdit purl = act.getMainPanel().findViewById(id);
						
					}
//					открываем help в окне
//					ActArray ar = new ActArray();
//					ar.add(Action.create(Action.ABOUT));
//					ar.add(Action.create(Action.FOUR_PDA));
//					ar.add(Action.create(Action.FEEDBACK));
//					ar.add(Action.create(Action.OTHER_APPS));
//
//					new DialogAbout(act,
//							act.getString(R.string.act_help),
//							st.getTextAssets(act, "_help.txt"),
//							ar)
//					.show();
					return true;
				}
			};
		case MODE_MAGIC_BUTTON_ALPHA:
			return new Action(action,action,R.string.magic_button_select_alpha,param,R.drawable.magic_button)
			{
				public boolean doAction(final MainActivity activity){
					ActArray ar = new ActArray();
					for (int i=10;i<101;i+=10){
						ar.add(Action.create(MODE_MAGIC_BUTTON_ALPHA,i).setText(st.STR_NULL+i+"%"));
						
					}
					new MenuPanelButton(activity, ar, new OnAction() 
					{
						@Override
						public void onAction(Action act) 
						{
							int proc=100;
							try {
								proc = Integer.parseInt(act.itemText.toString().substring(0,act.itemText.toString().indexOf("%"))); 
							} catch (NumberFormatException e){
								proc = 100;
							}
							Prefs.get().edit().putInt(Prefs.MAGIC_KEY_ALPHA, proc).commit();
							MainActivity.activeInstance.setMagicButtonAlpha();
						}
					}).show();
					return true;
				};
			};
		case MODE_NAVIGATION_PANEL_ALPHA:
			return new Action(action,action,R.string.magic_button_select_alpha,param,R.drawable.navigation)
			{
				public boolean doAction(final MainActivity activity){
					ActArray ar = new ActArray();
					for (int i=10;i<101;i+=10){
						ar.add(Action.create(MODE_NAVIGATION_PANEL_ALPHA,i).setText(st.STR_NULL+i+"%"));
						
					}
					new MenuPanelButton(activity, ar, new OnAction() 
					{
						@Override
						public void onAction(Action act) 
						{
							int proc=100;
							try {
								proc = Integer.parseInt(act.itemText.toString().substring(0,act.itemText.toString().indexOf("%"))); 
							} catch (NumberFormatException e){
								proc = 100;
							}
							Prefs.get().edit().putInt(Prefs.NAVIGATION_PANEL_ALPHA, proc).commit();
							activity.showMagickAndNavigation();
							activity.setNavigationPanel();
							st.fl_temp_hide_navigationpanel=false;
							activity.setInterface(2);
						}
					}).show();
					return true;
				};
			};
		case NAVIGATION_PANEL_TEXT_COLOR:
			return new Action(action,action,R.string.act_color,param,R.drawable.contextmenu)
			{
				public boolean doAction(final MainActivity activity){
					// пока не сделал
					ActArray ar = new ActArray();
					ar.add(Action.create(NAVIGATION_PANEL_TEXT_COLOR).setText(activity.getString(R.string.pn_color_r)));
					ar.add(Action.create(NAVIGATION_PANEL_TEXT_COLOR).setText(activity.getString(R.string.pn_color_g)));
					ar.add(Action.create(NAVIGATION_PANEL_TEXT_COLOR).setText(activity.getString(R.string.pn_color_b)));
					ar.add(Action.create(NAVIGATION_PANEL_TEXT_COLOR).setText(activity.getString(R.string.pn_color_m)));
					ar.add(Action.create(NAVIGATION_PANEL_TEXT_COLOR).setText(activity.getString(R.string.pn_color_dg)));
					ar.add(Action.create(NAVIGATION_PANEL_TEXT_COLOR).setText(activity.getString(R.string.pn_color_bl)));
					
					new MenuPanelButton(activity, ar, new OnAction() 
					{
						@Override
						public void onAction(Action act) 
						{
							int col=0;
							try {
								String str = act.itemText.toString().substring(0,act.itemText.toString().indexOf("."));
								col = Integer.parseInt(str); 
							} catch (NumberFormatException e){
								col = 3;
							}
							switch (col)
							{
							case 1: col=Color.RED;break;
							case 2: col=Color.GREEN;break;
							case 3: col=Color.BLUE;break;
							case 4: col=Color.MAGENTA;break;
							case 5: col=Color.DKGRAY;break;
							case 6: col=Color.BLACK;break;
							default: col=Color.RED;
							}
							Prefs.get().edit().putInt(Prefs.NAVIGATION_PANEL_COLOR, col).commit();
							activity.showMagickAndNavigation();
							activity.setNavigationPanel();
							st.fl_temp_hide_navigationpanel=false;
							activity.setInterface(2);
						}
					}).show();
					return true;
				};
			};
		case SIZE_BUTTON_NAVIGATION_PANEL:
			return new Action(action,action,R.string.navigation_panel_size,param,R.drawable.font_scale)
			{
				public boolean doAction(final MainActivity activity){
					ActArray ar = new ActArray();
					for (int i=10;i<71;i+=2){
						ar.add(Action.create(SIZE_BUTTON_NAVIGATION_PANEL,i).setText(st.STR_NULL+i));
						
					}
					new MenuPanelButton(activity, ar, new OnAction() 
					{
						@Override
						public void onAction(Action act) 
						{
							int size=40;
							try {
								size = Integer.parseInt(act.itemText.toString()); 
							} catch (NumberFormatException e){
								size = 40;
							}
							Prefs.get().edit().putInt(Prefs.NAVIGATION_PANEL_SIZE, size).commit();
							activity.showMagickAndNavigation();
							activity.setNavigationPanel();
							st.fl_temp_hide_navigationpanel=false;;
							activity.setInterface(2);
						}
					}).show();
					return true;
				};
			};
		case COPY_ALL_OPEN_URL:
			return new Action(action,R.id.copy_link, R.string.act_copy_all_url, param,R.drawable.copy,R.drawable.all_url);
		case OPEN_ALL_BOOKMARK:
//			return new Action(action,action,R.string.act_open_all_bookmark,param,R.drawable.bookmarks,R.drawable.all_url);
			return new Action(action,action,R.string.in_developing,param,R.drawable.bookmarks,R.drawable.all_url);
		case EXPORT:
//			return new Action(action,action,R.string.act_bookmark_export,param,R.drawable.bookmarks,R.drawable.back);
//			return new Action(action,action,R.string.in_developing,param,R.drawable.bookmarks,R.drawable.back);
			return new Action(action,action,R.string.act_export,param,R.drawable.ic_launcher,R.drawable.back)
//					return new Action(action,action,R.string.in_developing,param,R.drawable.bookmarks,R.drawable.right)
					{
						public boolean doAction(final MainActivity activity){
							new DialogExport(activity) {
								
								@Override
								public void doSave() 
								{
									st.toast("do save");
								}
							}.show();
							return true;

						};
					};

		case IMPORT:
			return new Action(action,action,R.string.act_import,param,R.drawable.ic_launcher,R.drawable.right)
//			return new Action(action,action,R.string.in_developing,param,R.drawable.bookmarks,R.drawable.right)
			{
				public boolean doAction(final MainActivity activity){
					ActArray ar = new ActArray();
					ar.add(Action.create(IMPORT,"1."+activity.getString(R.string.in_developing)).setText("1."+activity.getString(R.string.in_developing)));
					ar.add(Action.create(IMPORT,"2."+activity.getString(R.string.app_name)).setText("2."
							+ st.STR_NULL+activity.getString(R.string.app_name)));
					new MenuPanelButton(activity, ar, new OnAction() 
					{
						@Override
						public void onAction(Action act) {
							String s = (String)act.param;
							int pos = s.indexOf(".");
							if (pos>-1){
								pos = Integer.parseInt(s.substring(0, pos));
								if (pos == 2) {
									new DialogImport(activity) {
										
										@Override
										public void doSave() 
										{
											//st.toast("do save");
										}
									}.show();

								}
							}
							
						}
					}).show();
					return true;
				};
			};
		case VOICE_SEARCH:
			return new Action(action,action,R.string.act_voiceSearch,param,R.drawable.voicesearch)
			{
				@Override
				public boolean closePanel() {
					return false;
				}
			};
		case YES:
		case OK:
			return new Action(action,action,action==OK?R.string.ok:R.string.yes,param,R.drawable.okey);
		case NO:
		case CANCEL:
			return new Action(action,action,action==NO?R.string.no:R.string.cancel,param,R.drawable.clear);
		case CLEAR_DATA:
			return new Action(action,action,R.string.act_clear_data,param,R.drawable.copy,R.drawable.clear)
			{
				@Override
				public boolean doAction(MainActivity act) {
					if(param instanceof JSONObject)
						act.clearData((JSONObject)param,false);
					else
						BookmarkActivity.runByType(act, BookmarkActivity.TYPE_CLEAR_DATA);
					return true;
				}
				@Override
				public boolean closePanel() {
					return false;
				}
			};
		case PASTE:
			return new Action(action,action,android.R.string.paste,param,R.drawable.paste);
		case TAB_PANEL_SETTINGS:
			return new Action(action,action,R.string.act_tab_panel_settings,param,R.drawable.windows,R.drawable.settings)
			{
				public boolean doAction(MainActivity act) 
				{
					act.showInterfaceSettings(InterfaceSettingsLayout.MODE_WINDOWS_PANEL_SETTINGS);
					return true;
				};
			};
		case OPEN_TAB:
			return new Action(action,action,R.string.act_open_window,param,R.drawable.window,R.drawable.ic_launcher);
		case CLOSE_TAB:
			return new Action(action,action,R.string.act_close_window,param,R.drawable.window,R.drawable.clear);
		case CLOSE_ALL_TABS:
			return new Action(action,action,R.string.act_close_windows,param,R.drawable.windows,R.drawable.clear);
		case DOWNLOAD_LIST:
			return new Action(action,action,R.string.downloads,param,R.drawable.downloads)
			{
				@Override
				public boolean doAction(MainActivity act) {
					try{
						act.startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
					}
					catch(Throwable e)
					{
						CustomPopup.toast(act, R.string.load_error);
					}
					return true;
				}
			};
		case MIN_FONT:
			return new Action(action,action,R.string.act_min_font,param,R.drawable.selecttext);
		case SYSTEM_SETTINGS:
			return new Action(action,action,R.string.act_system_settings,param,R.drawable.fullview)
			{
				public boolean doAction(MainActivity act) 
				{
					act.startActivity(new Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
					return true;
				};
			};
			// запуск настроек панели инструментов
		case MINI_PANEL_SETTINGS:
			return new Action(action,action,R.string.panelQuickTools,param,R.drawable.contextmenu)
			{
				public boolean doAction(MainActivity act) 
				{
					new DialogToolsPanelSettings(act, PanelQuickTools.getMinipanelActions()).show();
					return true;
				};
			};
			// запуск настроек отображения пунктов главного меню
		case MAINMENU_SETTING:
			return new Action(action,action,R.string.mainmenu_settings,param,R.drawable.menu)
			{
				public boolean doAction(MainActivity act) 
				{
					new DialogMainMenuParagraph(act, PanelMainMenu.getMainMenuPanelActions()).show();
					return true;
				};
			};
		case SYSTEM_MOBILE_SETTINGS:
			return new Action(action,action,R.string.act_system_mobile_settings,param,R.drawable.smartphoneview)
			{
				@Override
				public boolean doAction(MainActivity act) 
				{
					act.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
					return true;
				};
			};
		case SYSTEM_WIFI_NETWORKS:
			return new Action(action,action,R.string.act_system_wifi_settings,param,R.drawable.wifi)
			{
				@Override
				public boolean doAction(MainActivity act) 
				{
					act.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
					return true;
				};
			};
		case QUICK_SETTINGS:
			return new Action(action,action,R.string.act_view_settings,param,R.drawable.view);
		case MAIN_SETTINGS:
			return new Action(action,action,R.string.act_settings,param,R.drawable.settings)
			{
				@Override
				public boolean doAction(MainActivity act) {
					act.showInterfaceSettings(InterfaceSettingsLayout.MODE_SETTINGS);
					//BookmarkActivity.runByType(act, BookmarkActivity.TYPE_SETTINGS);
					return true;
				}
			};
		case MIN_FONT_SIZE:
			return new Action(action,action,R.string.act_min_font,param,R.drawable.selecttext)
			{
				public boolean doAction(final MainActivity activity){
					ActArray ar = new ActArray();
					for(int val:MIN_FONT_RANGE)
					{
						ar.add(Action.create(MIN_FONT,val).setText(Integer.toString(val)));
					}
					new MenuPanelButton(activity, ar, new OnAction() {
						
						@Override
						public void onAction(Action act) {
							int sz = (Integer)act.param;
							Prefs.get().edit().putInt(Prefs.MIN_FONT, sz).commit();
							activity.getWebView().getSettings().setMinimumFontSize(sz);
							//activity.getWebView().loadUrl( "javascript:window.location.reload( true )" );
						}
					}).show();
					return true;
				};
			};
		case EDIT:
			return new Action(action,action,R.string.act_edit,param,R.drawable.edit);
		case START_APP_LAST_TAB:
			return new Action(action,action,R.string.act_startAppRestoreWindows,param,R.drawable.windows)
			{
				public boolean doAction(MainActivity act) 
				{
					Prefs.setStartApp(Prefs.START_RESTORE_WINDOWS);
					return true;
				};
			};
		case APPLY_TEXT:
			return new Action(action,action,R.string.act_apply_text,param,R.drawable.okey);
		case FOUR_PDA:
			return new Action(action,action,R.string.four_pda_team,param,R.drawable.four_pda);
		case ITEM_TEXT:
			return new Action(action,action,R.string.act_item_text,param,R.drawable.edit);
		case SELECT_TEXT:
			return new Action(action,action,R.string.act_select,param,R.drawable.selecttext);
//			{
//				@Override
//				public boolean doAction(MainActivity act) {
//					return false;
//				}
//			};
		case SOURCE_CODE:
			return new Action(action,action,R.string.act_source_code,param,R.drawable.edit);
		case FEEDBACK:
			return new Action(action,action,R.string.act_feedback,param,R.drawable.sendmail);
		case APP_MARKET:
			return new Action(action,action,R.string.act_app_market,param,R.drawable.ic_launcher);
		case OTHER_APPS:
			return new Action(action,action,R.string.act_other_apps,param,R.drawable.appsonmarket);
		case INSTALL_JBAK2KEYBOARD:
			return new Action(action,action, R.string.act_jbak2, param,R.drawable.jbak2keyboard);
		case INSTALL_MWCOSTS:
			return new Action(action,action, R.string.act_mwcosts, param,R.drawable.mwcosts);
		case INSTALL_MWSHARE2SAVE:
			return new Action(action,action, R.string.act_mwmwshare2save, param,R.drawable.mwshare2save);
		case SHOW_MENU:
			return new Action(action,action,R.string.act_show_menu,param,R.drawable.ic_launcher)
			{
				@Override
				public boolean closePanel() {
					return false;
				}
				public boolean doAction(MainActivity act) 
				{
					act.showPanel(!act.isPanelShown());
					return true;
				}
			};
		case CLEAR_TEXT:
			return new Action(action,action,R.string.act_clear_text,param,R.drawable.edit_clear)
			{
				public boolean closePanel() 
				{
					return false;
				};
			};
		case MAGIC_BUTTON_POS:
			return new Action(action,action, R.string.act_button_pos, param,R.drawable.ic_launcher)
			{
				public boolean doAction(MainActivity act) 
				{
					act.showInterfaceSettings(InterfaceSettingsLayout.MODE_MAGIC_BUTTON_POS);
					return true;
				};
			};
		case NAVIGATION_PANEL_POS:
			return new Action(action,action, R.string.act_navigation_pos, param,R.drawable.navigation)
			{
				public boolean doAction(MainActivity act) 
				{
					act.showInterfaceSettings(InterfaceSettingsLayout.MODE_NAVIGATION_PANEL_POS);
					return true;
				};
			};
		case STOP:
			return new Action(action,action, R.string.act_stop, param,R.drawable.clear)
			{
				public boolean doAction(MainActivity act) 
				{
					act.stopLoading();
					return true;
				};
			};
		case DELETE_FOLDER:
			return new Action(action,action, R.string.act_delete_folder, param,R.drawable.folder,R.drawable.clear);
		case VOLUME_KEYS_STATE:
			return new Action(action,action,R.string.act_volume_keys_menu,param,R.drawable.themes)
			{
				@Override
				public boolean doAction(MainActivity act) {
					Prefs.setVolumeKeysState((Integer)param);
					return true;
				}
			};
		case COPY_NET_STRIMING_URL:
			return new Action(action,action, R.string.act_copy_striming_url, param,R.drawable.search_videos,R.drawable.copy);
		case EXTERNAL_VIDEO_PLAYER:
			return new Action(action,action,R.string.act_external_player,param,R.drawable.search_videos);
		case HISTORY_VIDEO:
			return new Action(action,action,R.string.act_video_history,param,R.drawable.search_videos,R.drawable.history)
			{
				public boolean doAction(MainActivity act) 
				{
					BookmarkActivity.runByType(act, BookmarkActivity.TYPE_VIDEO_HISTORY);
					return true;
				};
			};
		case HISTORY_SAVED_PAGES:
			return new Action(action,action,R.string.act_saved_pages_history,param,R.drawable.sdcard,R.drawable.history)
			{
				public boolean doAction(MainActivity act) 
				{
					BookmarkActivity.runByType(act, BookmarkActivity.TYPE_SAVED_PAGES);
					return true;
				};
			};
			
		case THEMES_SELECTOR:
			return new Action(action,action,R.string.act_select_theme,param,R.drawable.themes)
			{
				@Override
				public boolean closePanel() {
					return false;
				}
				@Override
				public boolean doAction(MainActivity act) {
//					ArrayList<Action>ar = new ArrayList<Action>();
//					for(MyTheme th:MyTheme.THEMES)
//					{
//						th.init(act);
//						ar.add(create(THEME, th));
//					}
//					new MenuPanelButton(act, ar, new OnAction() {
//						
//						@Override
//						public void onAction(Action act) {
//							BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_ACTION, act);
//						}
//					}).show();
					act.clearCustomViews();
					act.showPanel(true);
					new DialogThemeSelector(act).show();
					return true;
				}
			};
		case SELECT_WW_BACK_COLOR:
			return new Action(action,action,R.string.act_select_theme,param,R.drawable.themes)
			{
// предотвращает закрытие окна настроек				
//				@Override
//				public boolean closePanel() {
//					return false;
//				}
				@Override
				public boolean doAction(MainActivity act) {
					act.setMainTheme(act);
					TabList tlist = act.getTabList();
					MyWebView mww = null;
					for(int i=0;i<tlist.getCount();i++){
						if (i<tlist.getOpenedSize()){
							mww = tlist.getOpenedTabByPos(i).getWebView();
							mww.setBackgroundColor(Prefs.getWWBackgroundColor());
						}
					}
					return true;
				}
			};
		case INTERFACE_SETTINGS:
			return new Action(action,action, R.string.act_interface, param,R.drawable.themes);
		case SAVEFILE:
			return new Action(action,action, R.string.act_savefile, param,R.drawable.sdcard)
			{
				public boolean doAction(final MainActivity activity) 
				{
					Uri uri = null;
					if(param instanceof Uri)
						uri = (Uri)param;
					else if(param instanceof String)
						uri = Uri.parse((String)param);
					else
					{
						DownloadFileInfo fi = new DownloadFileInfo();
						String saveName = activity.getSaveFilename(UrlProcess.MHT_EXT);
						fi.filename = saveName;
						fi.mimeType = UrlProcess.MHT_MIME;
						DialogDownloadFile df = new DialogDownloadFile(activity,new DownloadOptions(saveName),fi) {
							@SuppressLint("NewApi")
							@Override
							public void doSave(final DownloadOptions d) {
								String destName = d.destFile.getName();
								if(!destName.endsWith(UrlProcess.MHT_EXT))
								{
									destName+=UrlProcess.MHT_EXT;
									d.destFile = new File(d.destFile.getParent(), destName);
								}
								activity.getWebView().saveWebArchive(d.destFile.getAbsolutePath(), false, new ValueCallback<String>() {
									@Override
									public void onReceiveValue(String value) {
										if(value!=null)
										{
											Tab t = activity.getTab();
											if(t!=null&&t.currentBookmark!=null)
												Db.getExtHistory().insertBookmark(Db.TableExtHistory.SAVED_PAGE, t.currentBookmark, d.destFile.getAbsolutePath(), t.mThumbnail);
										}
										CustomPopup.toast(activity, value==null?R.string.save_error:R.string.success_oper);
									}
								});
							}
						};
						df.show();
						return true;
					}
					UrlProcess.forceDownload(activity, uri,true);
					return true;
				}
			};
		case SELECT_FOLDER:
			return new Action(action,action, R.string.act_select_folder, param,R.drawable.folder,R.drawable.okey);
			
		case Action.HOME:
			return new Action(action,action, R.string.act_go_home, param,R.drawable.edit);
		case Action.END:
			return new Action(action,action, R.string.act_go_home, param,R.drawable.edit);
		case Action.PGDN:
			return new Action(action,action, R.string.act_go_home, param,R.drawable.edit);
		case Action.PGUP:
			return new Action(action,action, R.string.act_go_home, param,R.drawable.edit);
		case Action.TOSTART:
			return new Action(action,action, R.string.act_go_home, param,R.drawable.edit);
		case Action.TOEND:
			return new Action(action,action, R.string.act_go_home, param,R.drawable.edit);
		case CODEPAGE:
			return new Action(action,action,R.string.act_codepage,param,R.drawable.codepage)
//			return new Action(action,action,R.string.in_developing,param,R.drawable.codepage)
			{
				public boolean doAction(final MainActivity activity){
					ActArray ar = new ActArray();
					ar.add(Action.create(CODEPAGE,IConst.UTF8).setText(IConst.UTF8));
					ar.add(Action.create(CODEPAGE,"cp-1251").setText("cp-1251"));
					ar.add(Action.create(CODEPAGE,"en_US").setText("en_US"));
					new MenuPanelButton(activity, ar, new OnAction() 
					{
						@Override
						public void onAction(Action act) 
						{
							MyWebView ww = activity.getWebView();
							ww.loadUrl(ww.getUrl());
						}
					}).show();
					return true;
				};
			};
		case OPENFILE:
			return new FileOpenAction(action,action, R.string.act_openfile, param,R.drawable.openfile);
		case DELETE_BOOKMARK:
			return new Action(action,action, R.string.act_delete_bookmark, param,R.drawable.clear)
			{
				public boolean doAction(MainActivity act) 
				{
					stat.deleteBookmark(act, (Bookmark)param,BookmarkActivity.TYPE_BOOKMARKS);
					return true;
				}
			};
		case SHOW_CLOSED_TABS:
			return new Action(action,action, R.string.act_closed_windows, param,R.drawable.windows,R.drawable.clear);
		case GO_HOME:
			return new Action(action,action, R.string.act_go_home, param,R.drawable.home);
		case NEW_FOLDER:
			return new Action(action,action, R.string.act_create_folder, param,R.drawable.folder,R.drawable.plus);
		case GO_UP:
			return new Action(action,action, R.string.act_go_up, param,R.drawable.up);
		case ACTION_BOOKMARK:
			return new BookmarkAction(action,action, R.string.act_add_bookmark, param,R.drawable.bookmarks);
		case SAVE_IMAGE_TO_GALLERY:
			return new Action(action,action, R.string.act_save_image, param,R.drawable.images)
			{
				@Override
				public boolean doAction(final MainActivity activity) {
					if(param instanceof LoadBitmapInfo)
					{
						LoadBitmapInfo li = (LoadBitmapInfo)param;
						stat.saveBitmap(activity, li.thumbnail, "SuperBrowser", li.bm.getUrl(),null);
					}
					else
					{
						int type = stat.TYPE_FULL;
						if(param instanceof Integer)
							type = (Integer)type;
						if(type==stat.TYPE_FULL)
						{
							stat.createWebFullPage(activity.getWebView(), new OnBitmapLoadListener() {
								
								@Override
								public void onBitmapsLoad() {
								}

								@Override
								public void onBitmapLoad(LoadBitmapInfo lbi) {
									stat.saveBitmap(activity, lbi.bitmap, "SuperBrowser",null,".jpg");
									
								}
							});
						}
						else
						{
							Bitmap bmp = stat.createWebViewThumbnail(activity.getWebView(),type);
							stat.saveBitmap(activity, bmp, "SuperBrowser",null,".jpg");
						}
					}
					return true;
				}
			};
		case FONT_SCALE_SETTINGS:
			return new Action(action,action, R.string.act_font_scale, param,R.drawable.font_scale)
			{
				@Override
				public boolean doAction(final MainActivity activity) {
					ActArray ar = new ActArray();
					for(Integer val:Prefs.getFontScales())
						ar.add(Action.create(FONT_SCALE,val));
					new MenuPanelButton(activity, ar, new OnAction() {
						
						@Override
						public void onAction(Action act) {
							activity.runAction(act);
						}
					}).show();
					return true;
				}
				@Override
				public String getText(Context c) {
					return super.getText(c) + " - "+(Integer)param+"%";
				}
			};
		case FONT_SCALE:
			return new Action(action,action, R.string.act_font_scale, param,R.drawable.font_scale)
			{
				@SuppressWarnings("deprecation")
				@SuppressLint("NewApi")
				@Override
				public boolean doAction(MainActivity activity) {
					WebSettings ws = activity.getWebView().getSettings();
					int zoom = (Integer)param;
					if(zoom==-10||zoom==10)
						zoom = ws.getTextZoom()+zoom;
					Prefs.setInt(Prefs.FONT_SCALE, zoom);
					if(Build.VERSION.SDK_INT>=14)
					{
						ws.setTextZoom(zoom);
					}
					else
						ws.setTextSize(Prefs.OLD_SIZES.getValueByKey(zoom));
					//activity.getWebView().reload();
					//activity.getWebWindow().refreshSettings(activity.getWebView());
					activity.getMainPanel().createActionsToolsGrid();
					return true;
				}
				@Override
				public String getText(Context c) {
					int zoom = (Integer)param;
					if(zoom==-10)
						return "- 10%";
					if(zoom==10)
						return "+ 10% ";
					
					return st.STR_NULL+(Integer)param+"%";
				}
			};
		case EXIT:
			return new Action(action,action, R.string.act_exit, param,R.drawable.exit)
			{
				@Override
				public boolean doAction(MainActivity act) {
					act.exit();
					return true;
				}
			};
		case ADD_BOOKMARK:
			return new Action(action,action, R.string.act_add_bookmark, param,R.drawable.bookmarks,R.drawable.plus);
		case CLEAR:
			return new Action(action,action, R.string.act_clear, param,R.drawable.clear)
			{
				public boolean doAction(MainActivity act) 
				{
					if(param instanceof EditText)
					{
						((EditText)param).setText(st.STR_NULL);
						return true;
					}
					return false;
				}
				public boolean closePanel() 
				{
					return false;
				};
			};
		case HISTORY:
			return new Action(action,R.id.history, R.string.act_history, param,R.drawable.history);
		case COPY_URL_TO_CLIPBOARD:
			return new Action(action,R.id.copy_link, R.string.act_copy_url, param,R.drawable.copy);
		case COPY_TEXT_URL_TO_CLIPBOARD:
			return new Action(action,action, R.string.act_copy_text, param,R.drawable.copy,R.drawable.txt);
//			return new Action(action,R.id.copy_link, R.string.act_copy_text, param,R.drawable.copy);
		case COPY_TEXT:
			return new Action(action,action, R.string.act_copy_text, param,R.drawable.copy)
			{
				@Override
				public boolean doAction(MainActivity act) {
					if(param instanceof TextView)
						stat.setClipboardString(act, ((TextView)param).getText().toString());
					else if(param instanceof String)
						stat.setClipboardString(act, (String)param);
					return true;
				}
			};
		case GO_BACK:
			return new Action(action,action, R.string.act_back, param,R.drawable.back);
		case SHARE_ELEMENT:
			return new Action(action,action, R.string.act_share_elem, param,R.drawable.share);
		case SHARE_URL:
			return new Action(action,action, R.string.act_share_url, param,R.drawable.share);
		case GO_FORWARD:
			return new Action(action,R.id.forward, R.string.act_forward, param,R.drawable.right)
			{
				@Override
				public boolean doAction(MainActivity act) 
				{
					if(act.getWebView().canGoForward())
						act.getWebView().goForward();
					return true;
				};
			};
		case SEARCH_SITE:
			return new SearchAction(SearchSystem.CMD_SEARCH_ON_SITE, R.string.act_search_site,R.drawable.searchsite)
			{
				@Override
				public boolean doAction(MainActivity ma, String text, String url) {
					return super.doAction(ma, text, url);
				}
			};
		case GO:
			return new Action(action,action, R.string.act_go, param,R.drawable.ic_launcher);
		case SHOW_MAIN_PANEL:
			return new Action(action,action, R.string.act_show_menu, param,R.drawable.ic_launcher);
		case TAB_HISTORY:
			return new Action(action,action, R.string.act_window_history, param,R.drawable.window_history)
			{
				@Override
				public boolean doAction(MainActivity act) {
					act.showWindowHistory();
					return true;
				}
			};
		case TO_TOP:
			return new Action(action,R.id.pageTop, R.string.act_to_top, param,R.drawable.up);
		case TO_BOTTOM:
			return new Action(action,R.id.pageBottom, R.string.act_to_bottom, param,R.drawable.down);
		case BOOKMARKS:
			return new Action(action,R.id.bookmarks, R.string.act_bookmarks, param,R.drawable.bookmarks)
			{
				@Override
				public boolean doAction(MainActivity act) {
					Intent in = BookmarkActivity.getIntent(act, BookmarkActivity.TYPE_BOOKMARKS);
					BrowserApp.runActivityForResult(act,IConst.CODE_GET_BOOKMARK,in);
					return true;
				}
			};
		case NEW_TAB:
			return new Action(action,action, R.string.act_new_window, param,R.drawable.windows,R.drawable.plus);
		case BACKGROUND_TAB:
			return new Action(action,action, R.string.act_background_tab, param,R.drawable.windows,R.drawable.down);
		case TAB_LIST:
			return new Action(action,action, R.string.act_windows, param,R.drawable.windows);
		case REFRESH:
			return new Action(action,R.id.refresh, R.string.act_refresh, param,R.drawable.refresh)
			{
				@Override
				public boolean doAction(MainActivity act) 
				{
					act.setNavigationPanel();
					act.getWebView().reload();
					return true;
				};
			};
		case SEARCH_ON_PAGE:
			return new Action(action,R.id.searchPage, R.string.act_search_page, param,R.drawable.searchpage);
		case DELETE_ITEM:
			return new Action(action,action, R.string.act_delete_item, param,R.drawable.clear);
		default:
			return new Action(action, action, R.string.act_not_def, param, R.drawable.ic_launcher);
		}
	}
	public static class FileOpenAction extends Action
	{
		MainActivityRef mRef;
		String mMime;
		public FileOpenAction(int command, int viewId, int textId,Object param, int imageRes) {
			super(command, viewId, textId, param, imageRes);
		}
		public void setMime(String mime)
		{
			mMime = mime;
		}
		void doOpenFile(File f,MainActivity activity)
		{
			String ext = FileUtils.getFileExt(f);
			String mime = UrlProcess.getMimeFromFile(f);
			if(!TextUtils.isEmpty(mMime))
				mime = mMime;
			if(UrlProcess.canOpenMimeInBrowser(mime))
			{
				if(UrlProcess.MHT_MIME.equals(mime)||ext!=null&&UrlProcess.MHT_EXT.compareToIgnoreCase(ext)==0)
					activity.openWebArchive(f);
				else	
				{
					Uri uri = Uri.fromFile(f);
					activity.openUrl(uri.toString(), Action.NEW_TAB);
				}
			}
			else
			{
				try{
					
					Uri uri = Uri.fromFile(f);
					Intent in = new Intent(Intent.ACTION_VIEW);
					if(!TextUtils.isEmpty(mime))
						in.setDataAndType(uri, mime);
					else
						in.setData(uri);
//
					activity.startActivity(Intent.createChooser(in, activity.getString(R.string.act_openfile)));
				}
				catch(Throwable err)
				{
					Utils.log(err);
				}
			}
		}
		@Override
		public boolean doAction(final MainActivity activity) {
			if(param instanceof File)
			{
				doOpenFile((File)param, activity);
				return true;
			}
			else if(param instanceof Uri)
			{
				Uri uri = (Uri)param;
				doOpenFile(new File(uri.getPath()), activity);
				return true;
			}
			BookmarkActivity.runForFileSelect(activity, new OnAction() {
				@Override
				public void onAction(Action act) {
					Uri uri = Uri.parse(((Bookmark)act.param).getUrl());
					File f = new File(uri.getPath());
					doOpenFile(f, activity);
				}
			}); 
			return true;
			
		}
	}
	
	public static class BookmarkAction extends Action
	{

		public BookmarkAction(int command, int viewId, int textId,Object param, int imageRes) {
			super(command, viewId, textId, param, imageRes);
			checkParam(param);
		}
		final void checkParam(Object param)
		{
			if(param instanceof Bookmark)
				setBookmark((Bookmark)param);
		}
		@Override
		public Action setParam(Object param) {
			super.setParam(param);
			checkParam(param);
			return this;
		}
		final void setBookmark(Bookmark bm)
		{
			if(bm.getUrl()==null)
				imageRes = R.drawable.up;
			if(bm.isBookmarkFolder())
				imageRes = R.drawable.folder;
			if(!TextUtils.isEmpty(bm.getUrl()))
				imageRes = 0;
		}
		@Override
		public boolean doAction(MainActivity activity) {
			if(param instanceof Bookmark)
			{
				activity.loadBookmark(getBookMark(), getBookMark().tabMode); 
			}
			return true;
		}
		public String getText(Context c) 
		{
			if(itemText!=null)
				return itemText;
			if(getBookMark()==null)
				return st.STR_NULL;
			return getBookMark().getTitle();
		};
		Bookmark getBookMark()
		{
			return (Bookmark)param;
		}
		@Override
		public boolean equals(Object o) {
			if(o instanceof BookmarkAction&& param!=null)
				return param.equals(((BookmarkAction)o).param);
			return false;
		}
		
	}
	public void translateUrl(MainActivity activity, Action act)
	{
		MyWebView mwv = activity.getWebView();
		String url = mwv.getUrl();
		switch (act.command)
		{
		case Action.TRANSLATE_LINK_GOOGLE:
			url ="https://translate.google.com/translate?u="+WebDownload.enc(url);
			break;
		case Action.TRANSLATE_LINK_YANDEX:
			url ="https://translate.yandex.ru/web?ui=ru&lang=en-ru&dir=&url="+WebDownload.enc(url);
			break;
		case Action.TRANSLATE_LINK_TRANSLATE_RU:
			url ="http://www.translate.ru/url/translation.aspx?direction=er&template=PersonalCorrespondence&autotranslate=off&transliterate=off&showvariants=off&sourceURL="+WebDownload.enc(url);
			
			break;
		}
// сохраняем в историю поиска		
//		if(!TextUtils.isEmpty(url))
//			SearchAction.saveSearch(activity, url);
		if(!TextUtils.isEmpty(url))
		{
			activity.openUrl(url,Action.NEW_TAB);
		}
		
	}
}
