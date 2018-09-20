package ru.mail.webimage;

import java.io.IOException;

import ru.mail.webimage.WebDownload.PostParams;
public class WebUtils
{
	static boolean useUrlConnection = true;
    public static String getUrlCookie(String url,String cookieName)
    {
    	return getUrlCookie(url, cookieName, null);
    }
    public static String getUrlCookie(String url,String cookieName,PostParams postParams)
    {
    	try{
    		WebDownload wd = new WebDownload();
    		wd.setPostParams(postParams);
    		if(useUrlConnection)
    			wd.startGetHttpConnection(url);
    		else
    			wd.startGetDefaultHttpClient(url);
    		String ret = null;
    		if(cookieName==null)
    			ret = WebDownload.streamToString(wd.input);
    		else
    			ret = WebDownload.getCookieByName(cookieName, wd.httpConnection);
    		wd.clear();
    		return ret;
    	}
    	catch(Throwable e)
    	{
    		
    	}
    	return null;
    }
    
    
    /** Загружает url, возвращает контент в виде строки */    
    public static String getUrlContent(String url)
    {
        return getUrlCookie(url, null,null);
    }
    public static boolean saveUrlToFile(String url,String path) throws IOException
    {
    	return new WebDownload().getUrlToFile(url, path, useUrlConnection);
    }
    /** 
     * Переключатель между URLConnection и DefaultHTTPClient
     * @param use true - используется URLConnection, false - DefaultHTTPClient 
     */
    public static void setUseUrlConnection(boolean use)
    {
    	useUrlConnection = use;
    }
    public static boolean getUseUrlConnection()
    {
    	return useUrlConnection;
    }
}
