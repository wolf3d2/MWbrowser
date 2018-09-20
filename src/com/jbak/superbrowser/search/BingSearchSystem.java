package com.jbak.superbrowser.search;

import ru.mail.webimage.WebDownload;
import android.net.Uri;

public class BingSearchSystem extends SearchSystem {
	public static final String SEARCH_URL = "http://www.bing.com/";
	public static final String SEARCH_Q =  "search?q=";

	@Override
	public String getSearchLink(int command, String searchEncodedText,
			String url) {
		switch (command) {
		case CMD_SEARCH:
			return SEARCH_URL+SEARCH_Q+searchEncodedText;
		case CMD_I_FEEL_LUCKY:
			return null;
		case CMD_SEARCH_IMAGES:
			return SEARCH_URL+"images/"+SEARCH_Q+searchEncodedText+"&btnI";
		case CMD_SEARCH_VIDEOS:
			return SEARCH_URL+"videos/"+SEARCH_Q+searchEncodedText+"&btnI";
		case CMD_SEARCH_NEWS:
			return SEARCH_URL+"news/"+SEARCH_Q+searchEncodedText+"&btnI";
		case CMD_SEARCH_APPS:
			return null;
		case CMD_CACHED_PAGE:
			return null;
		case CMD_SEARCH_ON_SITE:
			Uri uri = Uri.parse(url);
			String site = uri.getHost();
			return SEARCH_URL+SEARCH_Q+searchEncodedText+" site:"+WebDownload.enc(site);
		}
		return null;
	}

	@Override
	public String getName() {
		return "Bing";
	}

}
