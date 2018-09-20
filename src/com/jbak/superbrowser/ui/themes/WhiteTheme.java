package com.jbak.superbrowser.ui.themes;

import android.content.Context;

import com.mw.superbrowser.R;

public class WhiteTheme extends ColorTheme
{
	@Override
	public void init(Context c) {
		super.init(c);
		mColorsBookmarks = new int[]{0xffffffff,0xffdddddd};
		mColorsMainPanelBackground = 0xffdddddd;
		mColorsHorizontalPanelBackground = 0xffbbbbbb;
		mColorsContentBackground = 0xffdddddd;
		mColorsTitleBackground = 0xffaaaaaa;
		mColorsYesButtonBackground = 0xffcccccc;
		mColorsNoButtonBackground = 0xff999999;
		mTextColor = 0xff000000;
	}
	@Override
	public ThemeInfo createThemeInfo(Context c) {
		return new ThemeInfo(c.getString(R.string.theme_light), "theme_white");
	}
}