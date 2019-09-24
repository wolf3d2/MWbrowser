package com.jbak.superbrowser.ads;

import com.jbak.superbrowser.ui.MyWebView;

import ru.mail.mailnews.st;

/** класс блокирования рекламы и элементов сайтов*/
public class AdsBlock 
{
/** список урлов для блокировки */	
    public static class AdBlockList
    {
        public AdBlockList(String address, boolean blockirovat)
        {
        	url = address;
        	block = blockirovat;
        }
        public AdBlockList()
        {
        	url = "";
        	block = false;
        }
        public String id;
        public String url;
        public boolean block;
    }

    /** true - url блокирован */
	public static boolean isBlockUrl(MyWebView webview, String url)
	{
		if (!st.adblock)
			return false;
		if (webview == null)
			return false;
		if (webview.adblock_urls == null)
			return false;
		if (url.contains(".braun"))
			return true;
		
		AdBlockList abl = null;
		for (int i=0;i<webview.adblock_urls.size();i++) {
			abl = webview.adblock_urls.get(i);
			if (abl.url.contains(url))
				if (abl.block)
					return true;
		}
		return false;
	}
	
}
