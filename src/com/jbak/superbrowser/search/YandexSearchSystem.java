package com.jbak.superbrowser.search;

import ru.mail.webimage.WebDownload;

import com.mw.superbrowser.R;

import android.net.Uri;

public class YandexSearchSystem extends SearchSystem {
	public static final String SEARCH_URL = "http://yandex.ru/yandsearch?text=";

	@Override
	public String getSearchLink(int command, String searchEncodedText, String url) {
		switch (command) {
		case CMD_SEARCH:
			return SEARCH_URL+searchEncodedText;
		case CMD_I_FEEL_LUCKY:
			return null;
		case CMD_SEARCH_IMAGES:
			return "http://yandex.ru/images/search?text="+searchEncodedText;
		case CMD_SEARCH_BY_PICTURE:
			return "https://yandex.ru/images/search?url="
			+searchEncodedText
			+"&rpt=imageview";
		case CMD_SEARCH_VIDEOS:
			return "http://yandex.ru/video/search?text="+searchEncodedText;
		case CMD_SEARCH_NEWS:
			return "http://news.yandex.ru/yandsearch?rpt=nnews2&text="+searchEncodedText;
		case CMD_SEARCH_APPS:
			return "http://yandex.ru/yandsearch?filter=mobile_apps&text="+searchEncodedText;
		case CMD_CACHED_PAGE:
			return null;
		case CMD_SEARCH_ON_SITE:
			Uri uri = Uri.parse(url);
			String site = uri.getHost();
			return SEARCH_URL+searchEncodedText+WebDownload.enc(" site:"+site);
//		case CMD_TRANSLATE_URL:
//			return "https://translate.yandex.ru/web?ui=ru&lang=en-ru&dir=&url="+WebDownload.enc(url);
		}
		
		return null;
	}

	@Override
	public String getName() {
		return "Yandex";
	}
	public int getIconId() {
		return R.drawable.yandex;
	}

}
