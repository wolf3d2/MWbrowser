package com.jbak.superbrowser.ui.themes;

import android.app.Activity;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.jbak.superbrowser.MainActivity;
import com.mw.superbrowser.R;
import com.jbak.superbrowser.ui.BookmarkView;
import com.jbak.superbrowser.ui.HorizontalPanel;
import com.jbak.superbrowser.ui.PanelButton;
import com.jbak.superbrowser.ui.dialogs.ThemedDialog;
import com.jbak.ui.UIUtils;

public class DesktopTheme extends MyTheme
{
	MyBitmapDrawable mDrw;
	BroadcastReceiver mOnThemeChangedReceiver;
	static final int COLOR_SHADOW = 0xff000000;
	int mColorsSettings[] = new int[]{
			0x11ffffff,
			0x11000000,
			0x22ffffff,
			0x22000000,
	};
	WallpaperInfo mLastWallpaper;
		@Override
		public void setActive(Context c, boolean activate) {
			super.setActive(c, activate);
			if(activate)
			{
				mOnThemeChangedReceiver = new BroadcastReceiver() {
					@Override
					public void onReceive(Context context, Intent intent) {
						createDrawable(context,true);
					}
				};
				@SuppressWarnings("deprecation")
				IntentFilter filt = new IntentFilter(Intent.ACTION_WALLPAPER_CHANGED);
				c.registerReceiver(mOnThemeChangedReceiver, filt);
				createDrawable(c, true);
			}
		}
		private void createDrawable(Context c,boolean force)
		{
			WallpaperManager wm = WallpaperManager.getInstance(c);
			WallpaperInfo wi = wm.getWallpaperInfo();
			if(!force&&mLastWallpaper!=null&&wi!=null&&mLastWallpaper.equals(wi)&&mDrw!=null) // Юзается живая обоина, и она такая же, как и в прошлом вызове createDrawable
				return;
			if(!force&&mDrw!=null&&mLastWallpaper==null&&wi==null) // Обоина статическая, и вызов не из ресивера mOnThemeChangedReceiver и не из setActive
				return;
			mLastWallpaper = wi;
			Drawable drw = null;
			if(wi!=null)
				drw = wi.loadThumbnail(c.getPackageManager());
			else
				drw = wm.getDrawable();
			if(mDrw!=null)
				mDrw.set(drw);
			else
				mDrw = new MyBitmapDrawable(c, drw);
		}
		@Override
		public Drawable getDropdownBackgroundDrawable(Context c) {
			createDrawable(c,false);
			return mDrw;
		}
		
		@Override
		public MyTheme setViews(int itemType,View... views) {
//			if(mWhiteTheme!=null)
//				mWhiteTheme.setTextViews(views);
			switch (itemType) {
			case ITEM_ACTIVITY_BACKGROUND:
				setViewsBackgroundColor(COLOR_TRANSPARENT, views);
				break;
			case ITEM_DIALOG_BACKGROUND:
				setViewsBackgroundDrawable(mDrw, views);
				break;
			case ITEM_TITLE:
				setViewsBackgroundColor(0x99000000, views);
				setViewsTextColor(Color.WHITE, views);
				setViewsShadow(COLOR_SHADOW, views);
				break;
			case ITEM_DIALOG_TEXT:
				//setViewsBackgroundColor(COLOR_TRANSPARENT, views);
				setViewsShadow(COLOR_SHADOW, views);
				break;
			case ITEM_MAIN_PANEL_BACKGROUND:
				MyTheme.setViewsBackgroundColor(COLOR_TRANSPARENT, views);
				break;
			case ITEM_HORIZONTAL_PANEL_BACKGROUND:
				MyTheme.setViewsBackgroundColor(0x66000000, views);
				break;
			default:
				super.setViews(itemType,views);
				break;
			}
			return this;
		}
		@Override
		public void setViews(int item, int pos, View... views) {
			super.setViews(item, pos, views);
			if(item==ITEM_SETTINGS_POS)
				setViewsBackgroundColor(UIUtils.getBackColor(mColorsSettings, pos, false), views);
		}
		@Override
		public MyTheme onCreateActivity(Activity act) {
			act.setTheme(android.R.style.Theme_Wallpaper_NoTitleBar);
			if(act instanceof MainActivity)
			{
				MainActivity ma = (MainActivity)act;
				((MainActivity)act).getMainPanel().setBackgroundColor(0x00000000);
				for(View v:ma.getMainPanel().getPanels())
				{
					if(v instanceof HorizontalPanel)
						((HorizontalPanel)v).forceUpdate();
					v.setBackgroundColor(0x00000000);
				}
			}
			return this;
		}
		@Override
		public void setBookmarkView(BookmarkView bv, int pos, boolean isCurPos) {
			super.setBookmarkView(bv, pos, isCurPos);
			setViewsTextColor(0xffffffff,bv.getTextViews());
			setViewsShadow(COLOR_SHADOW, bv.getTextViews());
		}
		@Override
		public ThemeInfo createThemeInfo(Context c) {
			return new ThemeInfo(c.getString(R.string.theme_desktop), "theme_desktop");
		}
		@Override
		public void setPanelButton(PanelButton pb, int pos,boolean transparent) {
			super.setPanelButton(pb, pos,transparent);
			setViewsBackgroundColor(COLOR_TRANSPARENT, pb);
			setViewsTextColor(0xffffffff,pb.getTextView());
			setViewsShadow(COLOR_SHADOW, pb.getTextView());
		}
		@Override
		public void onCreateThemedDialog(ThemedDialog dlg) {
			super.onCreateThemedDialog(dlg);
			createDrawable(dlg.context(),false);
		}
}