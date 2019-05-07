package com.jbak.superbrowser.ui.themes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.jbak.superbrowser.Prefs;
import com.mw.superbrowser.R;
import com.jbak.superbrowser.ui.BookmarkView;
import com.jbak.superbrowser.ui.PanelButton;
import com.jbak.superbrowser.ui.dialogs.ThemedDialog;
import com.jbak.ui.UIUtils;

public abstract class MyTheme {
	ThemeInfo mInfo;
	public static final int ITEM_TITLE = 1;
	public static final int ITEM_DIALOG_TEXT = 2;
	public static final int ITEM_DIALOG_YES = 3;
	public static final int ITEM_DIALOG_NO = 4;
	public static final int ITEM_DIALOG_BACKGROUND = 5;
	public static final int ITEM_ACTIVITY_BACKGROUND = 6;
	public static final int ITEM_HORIZONTAL_PANEL_BACKGROUND = 7;
	public static final int ITEM_MAIN_PANEL_BACKGROUND = 8;
	public static final int ITEM_PANEL_BUTTON_POS = 9;
	public static final int ITEM_SETTINGS_POS = 10;
	public static final int ITEM_TAB_SELECTED = 11;
	public static final int ITEM_TAB = 12;
	public static final int ITEM_PANELBUTTON_SEL = 13;
	public static final int ITEM_DIALOG_SHADOW = 14;

	public static MyTheme THEMES[] = new MyTheme[]
	{
		new PastelTheme(),
		new PsychoDelicTheme(),
		new GrayscaleTheme(),
		new WhiteTheme(),
		new RedTheme(),
		new GreenTheme(),
		new BlueTheme(),
//		new DefaultThemeTransparent(),
		new DesktopTheme(),
		new DarkTheme(),
	};

	public static final int COLOR_TRANSPARENT = 0x00000000;
	private static MyTheme mCurTheme;
	public static MyTheme setTheme(Context c,String name)
	{
		for(MyTheme th:MyTheme.THEMES)
			th.init(c);
		mCurTheme = MyTheme.THEMES[0];
		String theme = Prefs.getTheme();
		if(!TextUtils.isEmpty(theme))
		{
			for(MyTheme t:MyTheme.THEMES)
			{
				if(theme.equals(t.getThemeInfo().id))
					mCurTheme = t;
			}
		}
		return mCurTheme;
	}
	public static void setCurTheme(MyTheme th)
	{
		mCurTheme = th;
	}
	public void setViews(int item,int pos,View ...views)
	{
	}
	public void setActive(Context c,boolean activate)
	{
		
	}
	public void onCreateThemedDialog(ThemedDialog dlg)
	{
		
	}
	public void init(Context c)
	{
		if(mInfo==null)
			mInfo = createThemeInfo(c);
	}
	public ThemeInfo getThemeInfo()
	{
		return mInfo;
	}
	public MyTheme onCreateActivity(Activity act)
	{
		return this;
	}
	public final MyTheme setView(View v,Integer ... types)
	{
		for(Integer t:types)
			setViews(t, v);
		return this;
	}
	public MyTheme setViews(int itemType,View ... views)
	{
		for(View v:views)
		{
			if(v instanceof PanelButton)
			{
				PanelButton pb = (PanelButton)v;
				switch(itemType)
				{
					case ITEM_TAB_SELECTED:
						pb.setTabDecoration(R.drawable.tabsel);
						pb.getTextView().setTypeface(null, Typeface.BOLD);
						MyTheme.setViewsShadow(0xffffffff, pb.getTextView());
						pb.getTextView().setTextColor(0xff000000);
						break;
					case ITEM_TAB:	
						pb.setTabDecoration(R.drawable.tab);
						pb.getTextView().setTypeface(null, Typeface.NORMAL);
						break;
					case ITEM_PANELBUTTON_SEL:
						pb.setBackgroundResource(R.drawable.tabsel_panelbutton);
						break;
						
				}
			}
		}
		return this;
	}
	public void setPanelButton(PanelButton pb,int pos,boolean transparent)
	{
		if(pb.getImageView()!=null)
			pb.getImageView().setColorFilter(null);
	}
	public void setBookmarkView(BookmarkView bv,int pos,boolean isCurPos)
	{
	}
	public Drawable getDropdownBackgroundDrawable(Context c)
	{
		return null;
	}
	public abstract ThemeInfo createThemeInfo(Context c);
	public static final MyTheme get()
	{
		return mCurTheme;
	}
	public static final void setViewsBackgroundColor(int color,View ...views)
	{
		if(views==null)
			return;
		for(View v:views)
		{
			if(v!=null)
				v.setBackgroundColor(color);
		}
	}
	public static final void setViewsShadow(int color,View ...views)
	{
		if(views==null)
			return;
		for(View v:views)
		{
			if(v instanceof TextView)
				((TextView)v).setShadowLayer(2f, 1f, 1f, color);
		}
	}
	public static final void setViewsBackgroundRes(int res,View ...views)
	{
		if(views==null)
			return;
		for(View v:views)
		{
			if(v!=null)
				v.setBackgroundResource(res);
		}
	}
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public static final void setViewsBackgroundDrawable(Drawable drw,View ...views)
	{
		if(views==null)
			return;
		for(View v:views)
		{
			if(v!=null)
			{
				if(Build.VERSION.SDK_INT>=16)
					v.setBackground(drw);
				else
					v.setBackgroundDrawable(drw);
			}
		}
	}
	public static final void setViewsTextColor(int color,View ...views)
	{
		if(views==null)
			return;
		for(View v:views)
		{
			if(v instanceof TextView)
			{
				TextView tv = (TextView)v;
				tv.setShadowLayer(0, 0, 0, 0);
				tv.setTextColor(color);
			}
		}
	}
	public static class ThemeInfo
	{
		public ThemeInfo(String name,String id)
		{
			this.name = name;
			this.id = id;
		}
		public String name;
		public String id;
	}
	public static class DefaultThemeTransparent extends PsychoDelicTheme
	{
		@Override
		public ThemeInfo createThemeInfo(Context c) {
			return new ThemeInfo(c.getString(R.string.theme_transparent), "theme_transparent");
		}
		@Override
		public void setPanelButton(PanelButton pb, int pos,boolean transparent) {
			UIUtils.setBackColor(pb, pos, true);
		}
	}
}
