package com.jbak.superbrowser.ui.themes;

import android.content.Context;
import android.view.View;

import com.mw.superbrowser.R;
import com.jbak.superbrowser.ui.BookmarkView;
import com.jbak.superbrowser.ui.PanelButton;
import com.jbak.ui.UIUtils;

public class GreenTheme extends PastelTheme {
	protected int mTitleTextColor = 0xffffffff;
	@Override
	public ThemeInfo createThemeInfo(Context c) {
		return new ThemeInfo(c.getString(R.string.theme_green), "green_theme");
	}
	int mReversedColors[] = new int[1];
	public void setActive(Context c, boolean activate) {
		if(activate)
		{
			mColorsBookmarks = new int[]{
				0x2BCE6B,
				0x4AB685,
				0x2FD06C,
				0x33D177,
				0x48AE50,
				0x3AC586,
				0x35C986,
				0x2BAB5A
			};
//			mColorsBookmarks = new int[]{
//				0xFFD3E0,
//				0xFFDEB3,
//				0xA5D5E3,
//				0xA4E6DA,
//				0xFEC7C7,
//				0xDCBAFF,
//				0xEAFFC1,
//				0xEAD5C8,
//				0xEBC9C9,
//				0xFFBFF6,
//				0xA6D5A6,
//				0xBAD2FF,
//				0xEFE8C9,
//				0xA6D5C9,
//				0xFFCAA8,
//				0xD0D5E0,
//			};
			mReversedColors = reverse(mColorsBookmarks);
			mColorsMainPanelBackground = 0xffdddddd;
			mColorsHorizontalPanelBackground = 0xffbbbbbb;
			mColorsContentBackground = 0xffdddddd;
			mColorsTitleBackground = 0xffaaaaaa;
			mTextColor = 0xff000000;
			mColorsPressedButton = 0xff088338;

		}
	}
	public void setBookmarkView(BookmarkView bv,int pos,boolean isCurPos)
	{
		if(bv.getType()==BookmarkView.TYPE_SQUARE)
			return;
		UIUtils.setBackColor(bv, UIUtils.getBackColor(mColorsBookmarks, pos, mTransparency), mColorsPressedButton);
		setViewsTextColor(mTextColor, bv.getTextViews());
		setViewsShadow(0Xffffffff, bv.getTextViews());
	}
	int mTransparency = 0x44000000;
//	ColorMatrixColorFilter mFilter;
	@Override
	public void setPanelButton(PanelButton pb, int pos, boolean transparent) {
		int colors[] = pb.getButtonType()==PanelButton.TYPE_BUTTON_SMALL?mReversedColors:mColorsBookmarks;
		UIUtils.setBackColor(pb, UIUtils.getBackColor(colors, pos, mTransparency), mColorsPressedButton);
		if(pb.getButtonType()==PanelButton.TYPE_BUTTON_BOOKMARK)
			return;
		if(transparent)
		{
			setViewsTextColor(0xffffffff, pb.getTextView());
		}
		else
		{
			setViewsTextColor(mTextColor, pb.getTextView());
			setViewsShadow(0Xffffffff, pb.getTextView());
			
		}
//		if(mFilter==null)
//		{
//		    ColorMatrix matrix = new ColorMatrix();
//		    matrix.setSaturation(0.8f);
//		    mFilter = new ColorMatrixColorFilter(matrix);
//		}
//		pb.getImageView().setColorFilter(mFilter);
	}
	@Override
	public MyTheme setViews(int itemType, View... views) {
		switch (itemType) {
		case ITEM_DIALOG_BACKGROUND:
		case ITEM_ACTIVITY_BACKGROUND:
		case ITEM_MAIN_PANEL_BACKGROUND:
		case ITEM_HORIZONTAL_PANEL_BACKGROUND:	
			//setViewsBackgroundRes(R.drawable.blue_background, views);
			//setViewsBackgroundColor(0xffffffff, views);
			setViewsBackgroundColor(0Xff00FF95, views);
			return this;
		case ITEM_DIALOG_TEXT:
			setViewsTextColor(0Xff000000, views);
			setViewsShadow(0Xffffffff, views);
			return this;
		case ITEM_DIALOG_SHADOW:
			setViewsBackgroundColor(0xcc006336, views);
			return this;
		case ITEM_TITLE:
			setViewsBackgroundColor(0xff178A38, views);	
			setViewsTextColor(mTitleTextColor, views);	
			return this;
		default:
			break;
		}
		return super.setViews(itemType, views);
	}
	@Override
	public void setViews(int item, int pos, View... views) {
		super.setViews(item, pos, views);
		setViewsBackgroundColor(UIUtils.getBackColor(mColorsBookmarks, pos, false), views);	
	}

}
