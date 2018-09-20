package com.jbak.superbrowser.plugins;

import ru.mail.webimage.WebDownload;
import android.content.Context;
import android.content.Intent;

import com.mw.superbrowser.R;
import com.jbak.superbrowser.stat;
import com.jbak.superbrowser.pluginapi.PluginClient;

public class PluginShortener extends PluginClient{

	@Override
	public InfoPlugin getPluginInfo() {
		InfoPlugin info = new InfoPlugin("Tiny url", WINDOW_MAIN_MENU|WINDOW_ADDR_URL, R.drawable.appsonmarket);
		return info;
	}

	@Override
	public boolean onMessageReceived(Context context, Intent in) {
		return false;
	}
	@Override
	public void onClick(InfoClick click) {
		String url = "http://tinyurl.com/create.php?url="+WebDownload.enc(click.windowText);
		sendOpen(OPEN_WHAT_URL, url, stat.STR_NULL, true);
	}
}	
