package com.jbak.superbrowser;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.jbak.superbrowser.pluginapi.Plugin;
import com.jbak.superbrowser.pluginapi.PluginClient;
import com.jbak.superbrowser.pluginapi.Reflect;
import com.jbak.superbrowser.ui.dialogs.DialogEditor;
import com.jbak.utils.Utils;

public class PluginServer extends PluginClient implements IConst{
	static final String tag = "JbakBrowser_PluginServer";
	PackageManager mPm;
	ArrayList<PluginEntry> mPlugins = new ArrayList<PluginServer.PluginEntry>();
	WeakReference<MainActivity> mAct;
	public PluginServer() {
		mPm = getContext().getPackageManager();
		mServerComponent = new ComponentName(getContext().getPackageName(), PluginServerReceiver.class.getName());
		//readPlugins(c.getPackageName());
	}
	@Override
	public InfoPlugin getPluginInfo() {
		return null;
	}
	public final Context getContext()
	{
		return BrowserApp.INSTANCE;
	}
	public void sendInfoBroadcast(Intent in)
	{
		getContext().sendOrderedBroadcast(in, null, this, null, Activity.RESULT_OK, tag, null);
	}
	public void getPluginInfo(ComponentName cn)
	{
		Intent in = getBroadcastIntent(COMMAND_GET_INFO, COMMAND_GET_INFO);
		in.setComponent(cn);
		in.putExtra(EXTRA_PACKAGE, mServerComponent.getPackageName());
		in.putExtra(EXTRA_COMPONENT, mServerComponent.getClassName());
		sendInfoBroadcast(in);
	}
	public boolean readPlugins(String pkg)
	{
		try{
			Intent in = new Intent(BROADCAST_ACTION);
			in.setPackage(pkg);
			List<ResolveInfo> list =  mPm.queryBroadcastReceivers(in, 0);
			for(ResolveInfo ai:list)
			{
				ComponentName cn = new ComponentName(pkg, ai.activityInfo.name);
				if(cn.equals(mServerComponent))
					continue;
				try{
					Log.d(tag, "Load plugin info:"+ai.toString());
					getPluginInfo(cn);
				}
				catch(Throwable e)
				{
					Log.d(tag, "Bad plugin info:"+cn.toString());
					e.printStackTrace();
					Utils.log(tag, e);
				}
			}
			return true;
		}
		catch(Throwable e)
		{
			
		}
		return false;
	}
	public void runAction(MainActivity ma, Action act)
	{
		mAct = new WeakReference<MainActivity>(ma);
		PluginActionParam pa = (PluginActionParam)act.param2;
		Log.d(tag, "Start command:"+pa.pluginEntry.pluginInfo.title);
		InfoClick clk = new InfoClick();
		String s = ma.getActionString(act);
		if(!TextUtils.isEmpty(s))
			clk.windowText = s;
		else
			clk.windowText = ma.getMainPanel().getUrl();
		clk.window = pa.window;
		if(ma.getTab()!=null)
		{
			if(ma.getTab().currentBookmark!=null)
				clk.title = ma.getTab().currentBookmark.getTitle();
			clk.loadProgress = ma.getTab().getLoadProgress();
		}
		sendOrderedBroadcast(COMMAND_CLICK, pa.pluginEntry.pluginComponent, clk);
	}
	@SuppressWarnings("deprecation")
	@Override
	public void onReceive(Context context, Intent intent) {
		try{
			if(BROADCAST_ACTION.equals(intent.getAction()))
			{
				String cmd = intent.getStringExtra(EXTRA_COMMAND);
				Bundle b = getResultExtras(false);
				if(COMMAND_GET_INFO.equals(cmd))
				{
					if(b!=null)
					{
						InfoPlugin pi = Reflect.fillClass(b, new InfoPlugin());
						PluginEntry pe = new PluginEntry();
						pe.pluginInfo = pi;
						String dat = getResultData();
						String cn[] = dat.split(",");
						pe.pluginComponent = new ComponentName(cn[0], cn[1]);
						Resources res = mPm.getResourcesForApplication(pe.getPackage());
						pe.mainIcon = res.getDrawable(pe.pluginInfo.iconResource);
						mPlugins.add(pe);
					}
				}
			}
		}
		catch(Throwable e)
		{
			Log.d(tag, "Plugin info error:"+intent.getComponent().toString());
			e.printStackTrace();
		}
	}
	public void getPluginActions(ActArray ar,int window,View caller)
	{
		for(PluginEntry pe:mPlugins)
		{
			if((pe.pluginInfo.showInWindows&window)!=0)
			{
				PluginActionParam param = new PluginActionParam(pe, window,caller);
				Action a = new Action(Action.ACTION_PLUGIN, pe.pluginInfo.title, pe.mainIcon, param);
				ar.add(a);
			}
		}
	}
	@Override
	public boolean onMessageReceived(Context c, Intent intent) {
		if(BROADCAST_ACTION.equals(intent.getAction()))
		{
			String cmd = intent.getStringExtra(EXTRA_COMMAND);
			if(COMMAND_S_OPEN_EDITOR.equals(cmd))
			{
				CmdOpen ed = Reflect.fillClass(intent, new CmdOpen());
				String text = ed.text;
				if(mAct!=null&&mAct.get()!=null)
				{
					if(ed.what==OPEN_WHAT_EDITOR)
						new DialogEditor(mAct.get(), ed.title, ed.text).show();
					else if(ed.what==OPEN_WHAT_URL)
						mAct.get().openUrl(ed.text, ed.newWindow?Action.NEW_TAB:0);
				}
			}
		}
		
		return true;
	}
	public static class PluginEntry
	{
		public ComponentName pluginComponent;
		Plugin.InfoPlugin pluginInfo;
		Drawable mainIcon;
		public final String getPackage()
		{
			if(pluginComponent==null)
				return null;
			return pluginComponent.getPackageName();
		}
	}
	public static class PluginActionParam
	{
		public PluginEntry pluginEntry;
		public Integer window;
		View caller;
		public PluginActionParam(PluginEntry pe,int window,View caller)
		{
			pluginEntry = pe;
			this.window = window;
			this.caller = caller;
		}
	}
}
