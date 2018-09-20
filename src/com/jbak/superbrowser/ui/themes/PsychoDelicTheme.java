package com.jbak.superbrowser.ui.themes;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.mw.superbrowser.R;
import com.jbak.superbrowser.ui.BookmarkView;
import com.jbak.superbrowser.ui.PanelButton;
import com.jbak.ui.UIUtils;

public class PsychoDelicTheme extends ColorTheme
{
	@Override
	public ThemeInfo createThemeInfo(Context c) {
		return new ThemeInfo(c.getString(R.string.theme_default), "theme_default");
	}
	@Override
	public void setPanelButton(PanelButton pb, int pos,boolean transparent) {
		UIUtils.setBackColor(pb, UIUtils.getBackColor(mColorsBookmarks, pos, false), mColorsPressedButton);
		if(pb.getButtonType()==PanelButton.TYPE_BUTTON_BOOKMARK)
			return;
		setViewsTextColor(mTextColor, pb.getTextView());
	}
	@Override
	public void setActive(Context c, boolean activate) {
		if(activate)
		{
			mColorsBookmarks = new int[]{
					0xA901DB,
					0xFFA32B,
					0x0489B1,
					0x00B797,
					0xFC6464,
					0x5F04B4,
					0xC48B66,
					0xC66969,
					0xFF00DC,
					0x088A08,
					0x3F82FF,
					0xD3C069,
					0x088A68,
					0xFF7FED,
					0x0431B4,
					0xB40486,
					0xFF83A7,
				};
				mColorsMainPanelBackground = 0xffdddddd;
				mColorsHorizontalPanelBackground = 0xffffffff;
				mColorsTitleBackground = 0xff7F0037;
				mTextColor = 0xffffffff;
		}
		super.setActive(c, activate);
	}
	@Override
	public MyTheme setViews(int itemType, View... views) {
		switch (itemType) {
		case ITEM_ACTIVITY_BACKGROUND:
		case ITEM_DIALOG_BACKGROUND:
		case ITEM_MAIN_PANEL_BACKGROUND:
			setViewsBackgroundRes(R.drawable.dialog_background, views);	
			return this;
		}
		return super.setViews(itemType, views);
	}
	Drawable mDropdownBackColor;
	@Override
	public Drawable getDropdownBackgroundDrawable(Context c) {
		if(mDropdownBackColor==null)
			mDropdownBackColor = new ColorDrawable(Color.WHITE);
		return mDropdownBackColor;
	}
	@Override
	public void setViews(int item, int pos, View... views) {
		super.setViews(item, pos, views);
		setViewsBackgroundColor(UIUtils.getBackColor(mColorsBookmarks, pos, true), views);	
	}
	public void setBookmarkView(BookmarkView bv,int pos,boolean isCurPos)
	{
		if(bv.getType()==BookmarkView.TYPE_SQUARE)
			return;
		UIUtils.setBackColor(bv, UIUtils.getBackColor(mColorsBookmarks, pos, true), mColorsPressedButton);
		setViewsTextColor(mTextColor, bv.getTextViews());
	}

}