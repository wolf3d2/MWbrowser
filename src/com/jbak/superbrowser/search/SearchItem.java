package com.jbak.superbrowser.search;

public class SearchItem
{
	public static final int SEARCH_TYPE_BOOKMARK = 1;
	public static final int SEARCH_TYPE_WEB = 2;
	public static final int SEARCH_TYPE_RELATED_SEARCHES = 3;

	public SearchItem()
	{
	}
	public SearchItem(String text,int type)
	{
		this.text = text;
		this.type = type;
	}
	@Override
	public String toString() {
		if(url!=null)
			return url;
		return text;
	}
	public String text;
	public int type = SEARCH_TYPE_WEB;
	public String url;
}