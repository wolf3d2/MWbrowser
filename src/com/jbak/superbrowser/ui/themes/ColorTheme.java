package com.jbak.superbrowser.ui.themes;

import android.app.Activity;
import android.view.View;

import com.jbak.superbrowser.MainActivity;
import com.jbak.superbrowser.ui.BookmarkView;
import com.jbak.superbrowser.ui.PanelButton;
import com.jbak.ui.UIUtils;

public abstract class ColorTheme extends MyTheme
{
	int mColorsBookmarks[] = new int[]{0xff666666,0xff6c6c6c};
	int mColorsMainPanelBackground = 0xff666666;
	int mColorsHorizontalPanelBackground = 0xff888888;
	int mColorsContentBackground = 0xff666666;
	int mColorsTitleBackground = 0xff333333;
	int mColorsYesButtonBackground = 0xff888888;
	int mColorsNoButtonBackground = 0xffaaaaaa;
	int mColorsPressedButton = 0xff999999;
	int mColorsActivityBackground = 0xff;
	int mTextColor = 0xffffffff;
	//new int[]{0xff000000,0xff111111,0xff222222,0xff333333,0xff444444,0xff555555,0xff666666,0xff777777,0xff888888};
	@Override
	public MyTheme setViews(int itemType,View... views) {
		super.setViews(itemType, views);
		switch (itemType) {
		case ITEM_TITLE:
			setViewsBackgroundColor(mColorsTitleBackground, views);
			setViewsTextColor(mTextColor, views);
			break;
		case ITEM_DIALOG_TEXT:
			setViewsTextColor(mTextColor, views);
			break;
		case ITEM_ACTIVITY_BACKGROUND:
		case ITEM_DIALOG_BACKGROUND:
			MyTheme.setViewsBackgroundColor(mColorsContentBackground, views);
			break;
		case ITEM_DIALOG_YES:
			MyTheme.setViewsBackgroundColor(mColorsYesButtonBackground, views);
			break;
		case ITEM_DIALOG_NO:
			MyTheme.setViewsBackgroundColor(mColorsNoButtonBackground, views);
			break;
		case ITEM_MAIN_PANEL_BACKGROUND:
			MyTheme.setViewsBackgroundColor(mColorsMainPanelBackground, views);
			break;
		case ITEM_HORIZONTAL_PANEL_BACKGROUND:
			MyTheme.setViewsBackgroundColor(mColorsHorizontalPanelBackground, views);
			break;
		default:
			break;
		}
		return this;
	}
	@Override
	public MyTheme onCreateActivity(Activity act) {
		if(act instanceof MainActivity)
		{
			((MainActivity)act).getMainPanel().setBackgroundColor(mColorsMainPanelBackground);
		}
		return this;
	}
	@Override
	public void setBookmarkView(BookmarkView bv, int pos, boolean isCurPos) {
		if(bv.getType()==BookmarkView.TYPE_SQUARE)
			return;
		int color = UIUtils.getBackColor(mColorsBookmarks, pos, false);
		UIUtils.setBackColor(bv, color, mColorsPressedButton);
		setViewsTextColor(mTextColor, bv.getTextViews());
	}
	
	@Override
	public void setPanelButton(PanelButton pb, int pos,boolean transparent) {
		super.setPanelButton(pb, pos,transparent);
		//int color = UIUtils.getBackColor(mColors, pos, false);
		int color = transparent?mColorsHorizontalPanelBackground:mColorsMainPanelBackground;
		UIUtils.setBackColor(pb, color, mColorsPressedButton);
		if(pb.getButtonType()!=PanelButton.TYPE_BUTTON_BOOKMARK)
			setViewsTextColor(mTextColor, pb.getTextView());
	}
	@Override
	public void setViews(int item, int pos, View... views) {
		super.setViews(item, pos, views);
		if(item==ITEM_PANEL_BUTTON_POS)
			setViewsBackgroundColor(UIUtils.getBackColor(mColorsBookmarks, pos, false), views);
	}
}