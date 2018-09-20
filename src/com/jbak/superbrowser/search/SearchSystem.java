package com.jbak.superbrowser.search;

import com.jbak.superbrowser.Prefs;


public abstract class SearchSystem {
	public static final int CMD_SEARCH = 1;
	public static final int CMD_I_FEEL_LUCKY = 2;
	public static final int CMD_CACHED_PAGE = 3;
	public static final int CMD_SEARCH_ON_SITE = 4;
	public static final int CMD_SEARCH_IMAGES = 5;
	public static final int CMD_SEARCH_VIDEOS = 6;
	public static final int CMD_SEARCH_NEWS = 7;
	public static final int CMD_SEARCH_APPS = 8;
	public static final int CMD_TRANSLATE_URL = 9;
	public static final int CMD_TRANSLATE_TEXT = 10;
	
	public static final String SEARCH_SYSTEM_GOOGLE = "Google";
	static SearchSystem SEARCH_SYSTEMS[] = new SearchSystem[]{
		new GoogleSearchSystem(),
		new BingSearchSystem(),
		new YandexSearchSystem()
	}; 
	static SearchSystem DEFAULT_SEARCH_SYSTEM; 
	static SearchSystem CURRENT_SEARCH_SYSTEM; 
	public abstract String getSearchLink(int command,String searchEncodedText,String url);
	public abstract String getName();
	
	public static SearchSystem getCurrent()
	{
		if(CURRENT_SEARCH_SYSTEM==null)
			return DEFAULT_SEARCH_SYSTEM;
		return CURRENT_SEARCH_SYSTEM;
	}
	public static void init()
	{
		DEFAULT_SEARCH_SYSTEM = SEARCH_SYSTEMS[0];
		CURRENT_SEARCH_SYSTEM = getByName(Prefs.get().getString(Prefs.SEARCH_SYSTEM,null));
	}
	public static void setSearchSystem(String name)
	{
		Prefs.get().edit().putString(Prefs.SEARCH_SYSTEM, name).commit();
		init();
	}
	public static SearchSystem getByName(String name)
	{
		for(SearchSystem ss:SEARCH_SYSTEMS)
		{
			if(ss.getName().equals(name))
				return ss;
		}
		return DEFAULT_SEARCH_SYSTEM;
	}
	public static String[] getSearchSystemsNames()
	{
		String ret[] = new String[SEARCH_SYSTEMS.length];
		for(int i=0;i<SEARCH_SYSTEMS.length;i++)
			ret[i] = SEARCH_SYSTEMS[i].getName();
		return ret;
	}
	public static String getLink(int command,String searchEncodedText,String url)
	{
		if(CURRENT_SEARCH_SYSTEM!=null)
		{
			String ret = CURRENT_SEARCH_SYSTEM.getSearchLink(command, searchEncodedText, url);
			if(ret!=null)
				return ret;
		}
		if(DEFAULT_SEARCH_SYSTEM!=null)
			return DEFAULT_SEARCH_SYSTEM.getSearchLink(command, searchEncodedText, url);
		return null;
	}
}
