package com.jbak.superbrowser;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkChecker extends BroadcastReceiver{
	public static NetworkInfo curNetwork;
	public static boolean inetAvaliable = true;
	static ConnInfo INSTANCE;
	public static void create(Context c)
	{
		INSTANCE = new ConnInfo(c);
	}
	@Override
	public void onReceive(Context c, Intent intent) {
		if(INSTANCE==null)
			return;
		INSTANCE.processIntent(c, intent);
	}
	static class ConnInfo
	{
		ConnectivityManager mCm;
		ConnInfo(Context c) {
			checkNetworkInfo(c);
		}
		void checkNetworkInfo(Context c)
		{
			ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Service.CONNECTIVITY_SERVICE);
			NetworkInfo ni = cm.getActiveNetworkInfo();
			inetAvaliable = ni!=null && ni.getState()==NetworkInfo.State.CONNECTED&&ni.isAvailable();
//			inetAvaliable = true;
			curNetwork = ni;
			BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_NETWORK_CHANGED, ni);
		}
		void processIntent(Context c, Intent intent)
		{
			checkNetworkInfo(c);
		}
	}
}
