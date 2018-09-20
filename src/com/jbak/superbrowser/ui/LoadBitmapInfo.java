package com.jbak.superbrowser.ui;

import android.graphics.Bitmap;

import com.jbak.superbrowser.Bookmark;

public class LoadBitmapInfo
{
	public LoadBitmapInfo(Bookmark bm, Object param)
	{
		this.bm = bm;
		this.param = param;
	}
	public Object param;
	public Bitmap bitmap;
	public Bitmap thumbnail;
	public Bitmap favicon;
	public Bookmark bm;
	public boolean loadImage = true;
	public boolean destroy()
	{
		return true;
	}
	public final String getUrl()
	{
		if(bm!=null)
			return bm.getUrl();
		return null;
	}
	public final void recycleBitmaps()
	{
		recycleIfCan(bitmap);
		recycleIfCan(favicon);
		recycleIfCan(thumbnail);
	}
	public static final void recycleIfCan(Bitmap bmp)
	{
		if(bmp!=null&&!bmp.isRecycled())
			bmp.recycle();
		
	}
}