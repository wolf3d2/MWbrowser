package com.jbak.superbrowser.ui.themes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class MyBitmapDrawable extends Drawable {
	Bitmap mBmp;
	Drawable mDrw;
	Rect mDest = new Rect();
	Rect mSrc = new Rect();
	Point mSize;
	public MyBitmapDrawable(Context c,Bitmap bmp)
	{
		set(bmp);
	}
	public MyBitmapDrawable(Context c, Drawable drw) {
		set(drw);
	}
	public void set(Bitmap bmp)
	{
		mBmp = bmp;
		mSize = new Point(bmp.getWidth(), bmp.getHeight());
	}
	public void set(Drawable drw)
	{
		if(drw instanceof BitmapDrawable)
		{
			set(((BitmapDrawable)drw).getBitmap());
			return;
		}
		mDrw = drw;
		mSize = new Point(drw.getIntrinsicWidth(), drw.getIntrinsicHeight());
	}
	@Override
	public void draw(Canvas canvas) {
		if(mDrw!=null)
			mDrw.draw(canvas);
		if(mBmp==null||mBmp.isRecycled())
			return;
		canvas.drawBitmap(mBmp, mSrc, mDest, null);
	}
	@Override
	public void setBounds(int left, int top, int right, int bottom) {
		mDest.set(left, top, right, bottom);
		if(!mDest.isEmpty()&&mSize.x>0&&mSize.y>0)
		{
			float dw = ((float)mDest.width())/((float)mSize.x);
			float dh = ((float)mDest.height())/((float)mSize.y);
			int x = 0,y=0;
			if(dw>dh)
			{
				y= (int) (((float)mSize.y)*dh/dw);
				int sy = mSize.y/2-y/2;
				mSrc.set(0, sy, mSize.x, sy+y);
			}
			else
			{
				x = (int) (((float)mSize.x)*dw/dh);
				int sx = mSize.x/2-x/2;
				mSrc.set(sx, 0, sx+x, mSize.y);
			}
		}
		super.setBounds(left, top, right, bottom);
	}
	@Override
	public int getOpacity() {
		return 0;
	}
	@Override
	public void setAlpha(int alpha) {
		
	}
	@Override
	public void setColorFilter(ColorFilter cf) {
		
	}
}
