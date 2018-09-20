package com.jbak.superbrowser.ui.themes;

import com.mw.superbrowser.R;
import com.jbak.superbrowser.ui.BookmarkView;
import com.jbak.superbrowser.ui.PanelButton;
import com.jbak.ui.UIUtils;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;

public class RedTheme extends PastelTheme {
	protected int mTitleTextColor = 0xffffffff;
	@Override
	public ThemeInfo createThemeInfo(Context c) {
		return new ThemeInfo(c.getString(R.string.theme_red), "red_theme");
	}
	int mReversedColors[] = new int[1];
	public void setActive(Context c, boolean activate) {
		if(activate)
		{
			mColorsBookmarks = new int[]{
				0xFFE1D7,
				0xFFEBE5,
				0xFFE9D7,
				0xFFE0CC,
				0xFFE1D7,
				0xFFE3CB,
				0xFFD7D7,
				0xFFCBCB,
				0xFFD8D8
				
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
			mTitleTextColor = 0xffffffff;
			mColorsPressedButton = 0xffff0000;
		}
	}
	public void setBookmarkView(BookmarkView bv,int pos,boolean isCurPos)
	{
		if(bv.getType()==BookmarkView.TYPE_SQUARE)
			return;
		UIUtils.setBackColor(bv, UIUtils.getBackColor(mColorsBookmarks, pos, true), mColorsPressedButton);
		setViewsTextColor(mTextColor, bv.getTextViews());
	}
//	ColorMatrixColorFilter mFilter;
	@Override
	public void setPanelButton(PanelButton pb, int pos, boolean transparent) {
		int colors[] = pb.getButtonType()==PanelButton.TYPE_BUTTON_SMALL?mReversedColors:mColorsBookmarks;
		UIUtils.setBackColor(pb, UIUtils.getBackColor(colors, pos,!transparent), mColorsPressedButton);
		if(pb.getButtonType()==PanelButton.TYPE_BUTTON_BOOKMARK)
			return;
		setViewsTextColor(mTextColor, pb.getTextView());
		//setViewsShadow(0xccffffff, pb.getTextView());

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
			setViewsBackgroundRes(R.drawable.red_background, views);
			//setViewsBackgroundColor(0xffffffff, views);
			return this;
		case ITEM_HORIZONTAL_PANEL_BACKGROUND:
			setViewsBackgroundColor(0xffF5B2B6, views);
			return this;
		case ITEM_TITLE:
			setViewsBackgroundRes(R.drawable.red_title_background, views);	
			setViewsTextColor(mTitleTextColor, views);	
			return this;
		case ITEM_TAB_SELECTED:	
				for(View v:views)
				{
					if(v instanceof PanelButton)
					{
						PanelButton pb = (PanelButton)v;
						pb.setTabDecoration(R.drawable.red_tabsel);
						pb.getTextView().setTypeface(null, Typeface.BOLD);
						pb.getTextView().setTextColor(0xff000000);
						MyTheme.setViewsShadow(0xffffffff, pb.getTextView());
					}
				}
				return this;
		case ITEM_PANELBUTTON_SEL:
			setViewsBackgroundRes(R.drawable.red_tabsel_panelbutton, views);
			return this;
		case ITEM_TAB:
			{
				for(View v:views)
				{
					if(v instanceof PanelButton)
					{
						PanelButton pb = (PanelButton)v;
						pb.setTabDecoration(R.drawable.red_tab);
						pb.getTextView().setTypeface(null, Typeface.NORMAL);
					}
				}
			}
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
