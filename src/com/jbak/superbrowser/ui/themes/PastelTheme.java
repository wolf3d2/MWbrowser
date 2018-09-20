package com.jbak.superbrowser.ui.themes;

import android.content.Context;
import android.view.View;

import com.mw.superbrowser.R;
import com.jbak.superbrowser.ui.BookmarkView;
import com.jbak.superbrowser.ui.PanelButton;
import com.jbak.ui.UIUtils;

public class PastelTheme extends PsychoDelicTheme
{
	protected static int[] reverse(int[] array){
	    int[] reversedArray = new int[array.length];
	    for(int i = 0; i < array.length; i++){
	        reversedArray[i] = array[array.length - i - 1];
	    }
	    return reversedArray;
	} 
	int mReversedColors[] = new int[1];
	public void setActive(Context c, boolean activate) {
		if(activate)
		{
			mColorsBookmarks = new int[]{
					0xFFE5ED,
					0xFFEBD1,
					0xC9E5EE,
					0xC8F0E9,
					0xFFDEDE,
					0xEAD5FF,
					0xF3FFDA,
					0xF3E6DE,
					0xF3DFDF,
					0xFFD9FA,
					0xC9E5C9,
					0xD6E4FF,
					0xF5F1DF,
					0xC9E6DE,
					0xFFDFCB,
					0xE3E6EC,
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
			mColorsTitleBackground = 0xffFFFFFF;
			mTextColor = 0xff000000;
		}
	}
	@Override
	public ThemeInfo createThemeInfo(Context c) {
		return new ThemeInfo(c.getString(R.string.theme_pastel), "pastel_theme");
	}
	public void setBookmarkView(BookmarkView bv,int pos,boolean isCurPos)
	{
		if(bv.getType()==BookmarkView.TYPE_SQUARE)
			return;
		UIUtils.setBackColor(bv, UIUtils.getBackColor(mColorsBookmarks, pos, false), mColorsPressedButton);
		setViewsTextColor(mTextColor, bv.getTextViews());
	}
//	ColorMatrixColorFilter mFilter;
	@Override
	public void setPanelButton(PanelButton pb, int pos, boolean transparent) {
		int colors[] = pb.getButtonType()==PanelButton.TYPE_BUTTON_SMALL?mReversedColors:mColorsBookmarks;
		UIUtils.setBackColor(pb, UIUtils.getBackColor(colors, pos, false), mColorsPressedButton);
		if(pb.getButtonType()==PanelButton.TYPE_BUTTON_BOOKMARK)
			return;
		setViewsTextColor(mTextColor, pb.getTextView());
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
			setViewsBackgroundRes(R.drawable.pastel_background, views);
			//setViewsBackgroundColor(0xffffffff, views);
			return this;
		case ITEM_TITLE:
			setViewsBackgroundRes(R.drawable.pastel_title_background, views);	
			setViewsTextColor(mTextColor, views);	
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