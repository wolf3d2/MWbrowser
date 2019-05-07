package com.jbak.superbrowser.ui.themes;

import android.content.Context;
import android.view.View;

import com.mw.superbrowser.R;
import com.jbak.superbrowser.ui.BookmarkView;
import com.jbak.superbrowser.ui.PanelButton;
import com.jbak.ui.UIUtils;

public class DarkTheme extends PastelTheme
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
					0x070707,
					0x101010,
					0x171717,
					0x202020,
					0x252525,
					0x303030,
					0x353535,
//					0x404040,
			};
			mReversedColors = reverse(mColorsBookmarks);
			mColorsMainPanelBackground = 0xff555555;
			mColorsHorizontalPanelBackground = 0xf333333;
			mColorsContentBackground = 0xff555555;
			mColorsTitleBackground = 0xff000000;
			mTextColor = 0xffffffff;
//			mReversedColors = reverse(mColorsBookmarks);
//			mColorsMainPanelBackground = 0xffdddddd;
//			mColorsHorizontalPanelBackground = 0xffbbbbbb;
//			mColorsContentBackground = 0xffdddddd;
//			mColorsTitleBackground = 0xffFFFFFF;
//			mTextColor = 0xffffffff;
		}
	}
	@Override
	public ThemeInfo createThemeInfo(Context c) {
		return new ThemeInfo(c.getString(R.string.theme_dark), "dark_theme");
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
			setViewsBackgroundRes(R.drawable.dark_background, views);
			//setViewsBackgroundColor(0xffffffff, views);
			return this;
		case ITEM_TITLE:
			setViewsBackgroundRes(R.drawable.dark_title_background, views);	
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