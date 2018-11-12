package com.jbak.superbrowser;

import ru.mail.mailnews.st;
import ru.mail.webimage.WebDownload;
import android.provider.Browser;
import android.text.TextUtils;
import com.jbak.superbrowser.search.SearchSystem;
import com.jbak.utils.Utils;

public class SearchAction extends Action
{
	static String m_url = st.STR_NULL;
	int mSearchCommand;
	public SearchAction(int searchCommand,int textRes, int imageRes) {
		super(SEARCH_ACTION, SEARCH_ACTION, textRes, null, imageRes);
		mSearchCommand = searchCommand;
	}
	static void saveSearch(MainActivity ma, String text)
	{
		try{
			if(text.startsWith(IConst.HTTP)||text.startsWith(IConst.HTTPS))
				return;
			if(BrowserApp.DB_TYPE==BrowserApp.DB_OWN)
				Db.getSearchTable().addSearch(text.toLowerCase());
			else
				Browser.addSearchUrl(ma.getContentResolver(), text.toLowerCase());
			BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_SEARCH_CHANGED, text);
		}
		catch(Throwable e)
		{
			Utils.log(e);
		}
	}
	public boolean doAction(MainActivity ma,String text,String url) {
/////
		Prefs.translate_lng = st.STR_NULL;
		int iii =-1;
		// открытие нескольких урлов, разделённых ентером
		// должна быть перед условия переводов
		if (text.startsWith("^=")){
			text = text.substring(2);
			String[] ar = text.split("\n");
			for (int i=0;i<ar.length;i++){
				if (ar[i].startsWith("//")||ar[i].isEmpty())
					continue;
				ma.openUrl(ar[i],Action.NEW_TAB);
			}
			
			return true;
		}
		// переводы
		else if (text.startsWith("^")){
			iii = text.indexOf("=");
			if (iii >-1){
				Prefs.translate_lng = text.trim().substring(1,iii).toLowerCase();
				text = text.substring(iii+1);
			} else {
				Prefs.translate_lng = "ru/en";
				text = text.substring(1);
			}
			mSearchCommand = 10;
		}
		if (stat.url!=null&&stat.url.length()>0){
			url= stat.url;
			stat.url = st.STR_NULL;
		}
		String link = SearchSystem.getLink(mSearchCommand, WebDownload.enc(text),url);
		if(!TextUtils.isEmpty(text)&&!TextUtils.isEmpty(link))
			saveSearch(ma, text);
		if(!TextUtils.isEmpty(link))
		{
			int ws = mSearchCommand==SearchSystem.CMD_TRANSLATE_URL?Action.NEW_TAB:0;
			ma.openUrl(link,ws);
		}
		return true;
	}
	
}