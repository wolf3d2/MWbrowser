package com.jbak.superbrowser.ui;

import java.util.ArrayList;

import ru.mail.mailnews.st;
import ru.mail.mailnews.st.UniObserver;

@SuppressWarnings("serial")
public class LoadBitmapCache extends ArrayList<LoadBitmapInfo> {
	public final void addCache(LoadBitmapInfo li)
	{
		add(li);
		if(size()>50)
			remove(0);
	}
	public final LoadBitmapInfo getCache(Object param)
	{
		for(LoadBitmapInfo li:this)
		{
			if(li==null)
				continue;
			if(li.param==param||li.param.equals(param))
				return li;
		}
		return null;
	}
	public final void clearAndRecycleSync() throws Throwable
	{
		Thread.sleep(3000);
		for(LoadBitmapInfo li:this)
		{
			if(li!=null)
			{
				li.recycleBitmaps();
			}
		}
		clear();
		
	}
	public void clearAndRecycle()
	{
		new st.SyncAsycOper() {
			
			@Override
			public void makeOper(UniObserver obs) throws Throwable {
				clearAndRecycleSync();
			}
		};
	}
}
