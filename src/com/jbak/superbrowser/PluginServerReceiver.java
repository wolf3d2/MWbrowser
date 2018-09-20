package com.jbak.superbrowser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PluginServerReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if(BrowserApp.pluginServer!=null)
			BrowserApp.pluginServer.onMessageReceived(context, intent);
	}

}
