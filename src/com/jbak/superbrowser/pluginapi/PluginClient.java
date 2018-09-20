package com.jbak.superbrowser.pluginapi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public abstract class PluginClient extends BroadcastReceiver implements Plugin {
	public abstract InfoPlugin getPluginInfo();
	Context mReceiverContext;
	protected ComponentName mServerComponent;
	public abstract boolean onMessageReceived(Context context, Intent in);
	public void sendOrderedBroadcast(String command, ComponentName cn, Object ... params)
	{
		Intent in = getBroadcastIntent(command, params);
		if(cn!=null)
			in.setComponent(cn);
		in.putExtra(EXTRA_PACKAGE, mServerComponent.getPackageName());
		in.putExtra(EXTRA_COMPONENT, mServerComponent.getClassName());
		getContext().sendOrderedBroadcast(in, null);
		Log.d("JbakBrowser_PluginClient", "sendBroadcast:"+command+" - "+in.toString());
	}
	public Context getContext()
	{
		return mReceiverContext;
	}
	protected Intent getBroadcastIntent(String command, Object ... params)
	{
		Intent in = new Intent(Plugin.BROADCAST_ACTION);
		in.putExtra(EXTRA_PACKAGE, getContext().getPackageName());
		in.putExtra(EXTRA_COMPONENT, this.getClass().getCanonicalName());
		in.putExtra(EXTRA_COMMAND, command);
		if(params!=null)
		{
			for(Object o:params)
			{
				if(o instanceof Plugin)
					Reflect.setIntentParam(in, Reflect.getObjectName(o), Reflect.getBundle(Reflect.getObjectParams(o)));
			}
		}
		return in;
	}
	public void sendBroadcast(String command, Object ... params)
	{
		Intent in = getBroadcastIntent(command,params);
		if(mServerComponent!=null)
			in.setComponent(mServerComponent);
		getContext().sendBroadcast(in);
	}
	public void setAnswer(boolean ok,Object obj)
	{
		String data = getContext().getPackageName()+","+this.getClass().getCanonicalName();
		setResult(ok?Activity.RESULT_OK:Activity.RESULT_CANCELED, data, obj==null?null:Reflect.getObjectBundle(getPluginInfo()));
	}
	@Override
	public void onReceive(Context context, Intent intent) {
		if(BROADCAST_ACTION.equals(intent.getAction()))
		{
			String command = intent.getStringExtra(EXTRA_COMMAND);
			mReceiverContext = context;
			mServerComponent = new ComponentName(intent.getStringExtra(EXTRA_PACKAGE), intent.getStringExtra(EXTRA_COMPONENT));
			if(COMMAND_GET_INFO.equals(command))
			{
				setAnswer(true,getPluginInfo());
				return;
			}
			else if(COMMAND_CLICK.equals(command))
			{
				InfoClick clk = Reflect.fillClass(intent, new InfoClick());
				onClick(clk);
			}
			else
			{
				onMessageReceived(context,intent);
			}
		}
	}
	protected void processIntentByClient(Context context,Intent intent)
	{
		
	}
	public void onClick(InfoClick click)
	{
		setAnswer(false, COMMAND_CLICK);
	}
	public void sendOpen(int openWhat,String text,String title,Boolean newWindow)
	{
		CmdOpen open = new CmdOpen(Integer.valueOf(openWhat), text, title, newWindow);
		sendBroadcast(COMMAND_S_OPEN_EDITOR, open);
	}
}
