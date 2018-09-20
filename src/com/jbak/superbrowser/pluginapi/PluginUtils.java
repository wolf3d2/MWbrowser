package com.jbak.superbrowser.pluginapi;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

public class PluginUtils {
	public static final String JBAK_BROWSER_PACKAGE = "com.jbak.superbrowser";
	public static final String JBAK_BROWSER_SCHEME = "jbakbrowser";
	public static final String JBAK_BROWSER_INCOGNITO_URL = JBAK_BROWSER_SCHEME+":incognito";
	public static final int getBrowserIntVersion(Context c)
	{
		try{
			PackageManager pm = c.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(JBAK_BROWSER_PACKAGE, 0);
			if(pi!=null)
				return pi.versionCode;
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		return -1;
	}
	public static final boolean runBrowser(Context c,Uri uri)
	{
		try{
			Intent in = new Intent(Intent.ACTION_VIEW);
			in.setPackage(JBAK_BROWSER_PACKAGE);
			in.setData(uri);
			c.startActivity(in);
			return true;
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		return false;
	}
}
