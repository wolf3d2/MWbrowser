package com.jbak.superbrowser.search;

import com.mw.superbrowser.R;

import android.net.Uri;
import ru.mail.webimage.WebDownload;

public class DuckDuckGoSearchSystem extends SearchSystem{
	public static final String SEARCH_URL = "https://duckduckgo.com/?q=";

	@Override
	public String getSearchLink(int command, String searchEncodedText,String url) {
		switch (command) {
		case CMD_SEARCH:
			return SEARCH_URL+searchEncodedText;
		case CMD_I_FEEL_LUCKY:
			return null;
		case CMD_SEARCH_BY_PICTURE:
		case CMD_SEARCH_IMAGES:
			return SEARCH_URL+searchEncodedText+"&t=h_&iax=images&ia=images";
		case CMD_SEARCH_VIDEOS:
			return SEARCH_URL+searchEncodedText+"&t=h_&iax=videos&ia=videos";
		case CMD_SEARCH_NEWS:
			return SEARCH_URL+searchEncodedText+"&t=h_&iar=news&ia=news";
//		case CMD_SEARCH_APPS:
//			return SEARCH_URL+searchEncodedText+"&tbm=app";
		case CMD_CACHED_PAGE:
//			return SEARCH_URL+"cache:"+searchEncodedText;
			return null;
		case CMD_SEARCH_ON_SITE:
			Uri uri = Uri.parse(url);
			String site = uri.getHost();
			return SEARCH_URL+searchEncodedText+" site:"+WebDownload.enc(site);
//		case CMD_TRANSLATE_URL:
//			return "https://translate.google.com/translate?u="+WebDownload.enc(url);
//		case CMD_TRANSLATE_TEXT:
//			return "https://translate.google.com/?hl=ru&tab=TT#"+Prefs.translate_lng+"/"+searchEncodedText;
		}
		return null;
	}

	@Override
	public String getName() {
		return "DuckDuckGo";
	}
	public int getIconId() {
		return R.drawable.duckduckgo;
	}

}
