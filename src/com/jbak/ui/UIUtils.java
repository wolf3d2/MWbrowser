package com.jbak.ui;

import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.jbak.CustomGraphics.CustomButtonDrawable;
import com.jbak.CustomGraphics.GradBack;
import com.mw.superbrowser.R;

public class UIUtils{
	private static final float DX_DY = 0.56f;
	private static final int DELIM_W = 0;
	private static final int MAX_W = 550;
	public static int ITEMS_COUNT = 2;
	public static Point SIZE_VI;
	public static int mMagicPadding = -1;
	public static int mBackColors[] = new int[]{
		0xFFB5CB,
		0xFFC77F,
		0x69B8D0,
		0x66D4C1,
		0xFEA2A2,
		0x9F69D2,
		0xDBB9A3,
		0xDDA5A5,
		0xFF66EA,
		0x6BB96B,
		0x8CB4FF,
		0xE5D9A5,
		0x6BB9A5,
		0xFFB2F4,
		0x6883D2,
		};  
//	public static int mBackColors[] = new int[]{
//		0xA901DB,
//		0xFFA32B,
//		0x0489B1,
//		0x00B797,
//		0xFC6464,
//		0x5F04B4,
//		0xC48B66,
//		0xC66969,
//		0xFF00DC,
//		0x088A08,
//		0x3F82FF,
//		0xD3C069,
//		0x088A68,
//		0xFF7FED,
//		0x0431B4,
//		0xB40486,
//		0xFF83A7,
//		};  
	public static int mRandomColors[] = new int[]{
		0x00ff00,
		0x0000ff,
		0xff00ff,
		0x00ffff,
	};  
	
	public static final int TRANSPARENCY = 0x99000000;
	public static final int getMagicPadding()
	{
		return mMagicPadding;
	}
	public static final int getActivityBackground()
	{
		//return R.drawable.pattern_carbon_fibre;
		return 0;
	}
	static Point getDisplayRealSize(Context c)
	{
		WindowManager wm = (WindowManager) c.getSystemService(Service.WINDOW_SERVICE);
		Display d = wm.getDefaultDisplay();
		DisplayMetrics dm =  new DisplayMetrics();
		d.getMetrics(dm);
		//c.getResources().getDisplayMetrics();
		return new Point(dm.widthPixels,dm.heightPixels);
	}
	public static int calcItemWidth(int sz,int preferedItemsCount,boolean portrait)
	{
		int portW = 0;
		int cnt = preferedItemsCount;
		do
		{
			int delims= (cnt+1)*DELIM_W;
			portW = (sz-delims)/cnt;
			ITEMS_COUNT = cnt;
			cnt++;
		}
		while(portW>MAX_W);
		return portW;
	}
	public static void init(Context c)
	{
		try{
		mMagicPadding = c.getResources().getDimensionPixelSize(R.dimen.magic_padding);
		}
		catch(Throwable e)
		{
			mMagicPadding = 4;
		}
	}
	static final int heightByWidth(int w)
	{
		return (int)((float)w*DX_DY);
	}
    public static final boolean isPortrait(Context c)
    {
    	return c.getResources().getConfiguration().orientation==Configuration.ORIENTATION_PORTRAIT;
    }
    public static final boolean isPortrait(Configuration config)
    {
    	return config.orientation==Configuration.ORIENTATION_PORTRAIT;
    }
    public static TextView newTextView(Context c,int style,int text)
    {
//    	XmlPullParser pars = c.getResources().getXml(style);
//    	AttributeSet att =  Xml.asAttributeSet(pars);
    	TextView tv = new TextView(c);
    	tv.setTextAppearance(c, style);
    	
    	tv.setText("bb "+text);
    	return tv;
    }
    private static final GradBack getGradBack(int color)
    {
    	return new GradBack(color, GradBack.DEFAULT_COLOR).setUseCache(false).setGap(0).setCorners(0, 0);
    	
    }
    @SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public static void setViewBackground(View v,Drawable drw)
    {
    	if(v==null)
    		return;
    	int left = v.getPaddingLeft();
    	int top = v.getPaddingTop();
    	int right = v.getPaddingRight();
    	int bottom = v.getPaddingBottom();
    	if(Build.VERSION.SDK_INT>=VERSION_CODES.JELLY_BEAN)
    		v.setBackground(drw);
    	else
    		v.setBackgroundDrawable(drw);
    	v.setPadding(left, top, right, bottom);
    }
    public static GradBack getGradBack(int color,int pressedColor)
    {
    	return  getGradBack(color)
				.setPressedGradBack(getGradBack(pressedColor))
				.setDisabledGradBack(getGradBack(0x99999999));
    	
    }
    public static void setBackColor(View v,int color,int pressedColor)
    {
    	if(v==null)
    		return;
    	if(v.getBackground() instanceof CustomButtonDrawable)
    	{
    		CustomButtonDrawable cb = (CustomButtonDrawable)v.getBackground();
    		if(cb.getGradBack()!=null)
    		{
    			GradBack gb = cb.getGradBack();
    			gb.set(color, GradBack.DEFAULT_COLOR);
    			if(gb.getPressedGradBack()!=null)
    				gb.getPressedGradBack().set(pressedColor, GradBack.DEFAULT_COLOR);
    		}
    	}
    	GradBack gb = getGradBack(color,pressedColor);
    	CustomButtonDrawable drw = new CustomButtonDrawable(gb);
    	setViewBackground(v, drw);
//    	if(v!=null)
//    		v.setBackgroundColor(color);
    }
    public static void setBackColor(View v,int position,boolean transparent)
    {
    	setBackColor(v, getBackColor(position, transparent),getBackColor(position+3, transparent));
    }
    public static final int getBackColor(int position,boolean transparent)
    {
    	return getBackColor(mBackColors, position, transparent);
    }
    public static final int getBackColor(int colors[], int position,boolean transparent)
    {
    	return getBackColor(colors, position, transparent?TRANSPARENCY:0);
    }
    public static final int getBackColor(int colors[], int position,int transparency)
    {
    	int arrayPos = position/colors.length;
    	arrayPos = position-arrayPos*colors.length;
    	int color = colors[arrayPos];
    	if(transparency!=0)
    		return transparency+color;
    	return 0xff000000+color;
    }
    public static final Point getClickCoords(MotionEvent evt)
    {
    	return new Point((int) evt.getRawX(),(int) evt.getRawY());

    }
    public static final Rect getViewGlobRect(View v,Rect reusedRect)
    {
    	if(reusedRect==null)
    		reusedRect = new Rect();
    	else
    		reusedRect.set(0, 0, 0, 0);
		if(v.getVisibility()==View.VISIBLE)
		{
    		v.getGlobalVisibleRect(reusedRect);
		}
		return reusedRect;
    }
    public static final boolean isTouchEventForViews(MotionEvent evt, Rect reusedRect, View ...views)
    {
    	if(reusedRect==null)
    		reusedRect = new Rect();
    	Point pt = getClickCoords(evt);
    	for(View v:views)
    	{
    		reusedRect = getViewGlobRect(v, reusedRect);
    		if(reusedRect.contains(pt.x,pt.y))
    			return true;
    	}
    	return false;
    }
    public static int getRandomColor(boolean transparent)
    {
    	int pos = new Random().nextInt(mRandomColors.length-1);
    	int color = mRandomColors[pos];
    	if(transparent)
    		return TRANSPARENCY+color;
    	return 0xff000000+color;
    }
	public static final void showViews(boolean show,View ... views)
	{
		showViews(show?View.VISIBLE:View.GONE, views);
	}
	public static final void setViewsTag(Object tag,View ... views)
	{
		for(View v:views)
		{
			if(v!=null)
				v.setTag(tag);
		}
	}
	public static final void showViews(int visibility,View ... views)
	{
		for(View v:views)
		{
			if(v!=null)
				v.setVisibility(visibility);
		}
	}
	public static final int getChildsWidth(ViewGroup vg)
	{
		int w = 0;
		for(int i=vg.getChildCount()-1;i>=0;i--)
		{
			View v = vg.getChildAt(i);
			if(v!=null)
				w+=v.getWidth();
		}
		return w;
	}
	public static final int getChildsHeight(ViewGroup vg)
	{
		int cc = vg.getChildCount();
		if(cc<1)
			return 0;
		int firstIndex = 0;
		View vFirst = vg.getChildAt(firstIndex);
		while (vFirst.getVisibility()==View.GONE&&firstIndex<cc-1) {
			++firstIndex;
			vFirst = vg.getChildAt(firstIndex);
			if(vFirst==null)
				return 0;
		}
		if(vFirst==null||vFirst.getVisibility()==View.GONE)
			return 0;
		int lastIndex = cc-1;
		View vLast = vg.getChildAt(lastIndex);
		if(vLast==null)
			return 0;
		while (vFirst.getVisibility()==View.GONE&&lastIndex>0) {
			--lastIndex;
			vLast = vg.getChildAt(lastIndex);
		}
		
		if(vFirst!=null&&vLast!=null)
		{
			int start = Math.min(vLast.getTop(), vFirst.getTop());
			int end = Math.max(vLast.getBottom(), vFirst.getBottom());
			return Math.abs(end-start);
		}
		return 0;
	}
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public static final Drawable getDrawable(Context c, int resId)
	{
		if(c==null)
			return null;
		if(Build.VERSION.SDK_INT>=21)
			return c.getDrawable(resId);
		return c.getResources().getDrawable(resId);
	}
}
