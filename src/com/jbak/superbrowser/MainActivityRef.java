package com.jbak.superbrowser;

import java.lang.ref.WeakReference;

public class MainActivityRef {
	public WeakReference<MainActivity> mMain;
	public MainActivityRef()
	{
		
	}
	public MainActivityRef(MainActivity main)
	{
		setMain(main);
	}
	public final MainActivity getMain()
	{
		if(mMain==null)
			return null;
		return mMain.get();
	}
	public final void setMain(MainActivity main)
	{
		mMain = new WeakReference<MainActivity>(main);
	}
}
