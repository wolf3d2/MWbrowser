package com.jbak.utils;

import java.lang.ref.WeakReference;

import android.content.Context;

public class ContextRef {
	WeakReference<Context> mContextRef;
	public ContextRef()
	{}
	public ContextRef(Context c)
	{
		setContext(c);
	}
	public final Context getContext()
	{
		if(mContextRef==null)
			return null;
		return mContextRef.get();
	}
	public final void setContext(Context c)
	{
		mContextRef = new WeakReference<Context>(c);
	}
}
