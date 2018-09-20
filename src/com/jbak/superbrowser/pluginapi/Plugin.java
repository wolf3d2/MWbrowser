package com.jbak.superbrowser.pluginapi;


/** Константы и структуры данных плагина */
public interface Plugin {
	public static final int PLUGIN_API_VERSION = 	1;

	public static final String BROADCAST_ACTION = "com.jbak.superbrowser.pluginapi.BROADCAST";
	public static final int WINDOW_START_SCREEN = 	0x0001;
	public static final int WINDOW_MAIN_MENU = 	0x0002;
	public static final int WINDOW_ADDR_EMPTY = 	0x0004;
	public static final int WINDOW_ADDR_URL = 		0x0008;
	public static final int WINDOW_ADDR_SEARCH = 	0x00010;

	public static final int OPEN_WHAT_URL = 1;
	public static final int OPEN_WHAT_EDITOR = 2;
	public static final int OPEN_WHAT_ALERT = 3;

	public static final String EXTRA_COMMAND = "jbakbrowserPlugin_command";
	public static final String EXTRA_PACKAGE = "jbakbrowserPlugin_package";
	public static final String EXTRA_COMPONENT = "jbakbrowserPlugin_component";
	public static final String COMMAND_GET_INFO = "pluginInfo";
	public static final String COMMAND_CLICK = "click";
	public static final String COMMAND_S_OPEN_EDITOR = "openEditor";
	/** Класс, предоставлющий информацию о плагине */
	public static class InfoPlugin implements Plugin
	{
		public InfoPlugin()
		{}
		public InfoPlugin(String title,Integer showInWindows,Integer iconResource)
		{
			this.title = title;
			this.iconResource = iconResource;
			this.showInWindows = showInWindows;
		}
		/** Версия api */
		public Integer pluginApiVersion = Integer.valueOf(PLUGIN_API_VERSION);
		/** Заголовок плагина*/
		public String title;
		/** Места, где показываются окна. Комбинация флагов WINDOW_*/
		public Integer showInWindows;
		/** Ресурс иконки */
		public Integer iconResource=0;
	}
	public static class InfoBrowser implements Plugin
	{
		public InfoBrowserTab currentTab;
	}
	public static class InfoBrowserTab implements Plugin
	{
		/** Текущая ссылка */
		public String url;
		/** Заголовок страницы */
		public String title;
		/** Текущий прогресс загрузки */
		public Integer loadProgress;
	}
	public static class InfoClick extends InfoBrowserTab implements Plugin
	{
		public Integer window;
		public String windowText;
	}
	public static class CmdOpen implements Plugin
	{
		public CmdOpen()
		{}
		public CmdOpen(Integer what,String text,String title,Boolean newWindow)
		{
			this.what = what;
			this.text = text;
			this.title = title;
			this.newWindow = newWindow;
		}
		public Integer what;
		public String text;
		public String title;
		public Boolean newWindow;
	}
}
