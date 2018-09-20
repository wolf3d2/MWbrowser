package com.jbak.superbrowser.panels;

import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.jbak.superbrowser.ActArray;
import com.jbak.superbrowser.Action;
import com.jbak.superbrowser.MainActivity;
import com.mw.superbrowser.R;
import com.jbak.superbrowser.SearchAction;
import com.jbak.superbrowser.WebViewEvent;
import com.jbak.superbrowser.Tab;
import com.jbak.superbrowser.search.SearchSystem;
import com.jbak.superbrowser.ui.MenuPanelButton;
import com.jbak.superbrowser.ui.OnAction;
import com.jbak.superbrowser.ui.dialogs.DialogEditor;
import com.jbak.superbrowser.ui.themes.MyTheme;
import com.jbak.ui.UIUtils;

public class PanelTitle extends FrameLayout implements WebViewEvent,OnAction,OnClickListener{

	String mTitleText;
	TextView mTitle;
	public static final int MAX_LINES_PORTRAIT = 6;
	public static final int MAX_LINES_LANDSCAPE = 3;
	public PanelTitle(Context context) {
		super(context);
		init();
	}

	private void init() {
		mTitle = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.textview_title, null);
		addView(mTitle);
		setFromWebWindow();
		setTheme();
		setOnClickListener(this);
		checkMaxLines(UIUtils.isPortrait(getContext()));
	}
	public final void setTheme()
	{
		MyTheme.get().setViews(MyTheme.ITEM_TITLE, mTitle);
	}
	public PanelTitle setText(String text)
	{
		mTitleText = text;
		mTitle.setText(mTitleText);
		return this;
	}
	@Override
	public void onWebViewEvent(int code, EventInfo info) {
		if(code==WWEVENT_CUR_WINDOW_CHANGES)
			setFromWebWindow();
	}
	public final String getTitleText()
	{
		return mTitleText;
	}
	public final TextView getTitle()
	{
		return mTitle;
	}

	@Override
	public void onAction(Action act) {
		act.param = mTitleText;
		if(getContext() instanceof MainActivity)
		{
			MainActivity ma = (MainActivity)getContext();
			ma.clearCustomViews();
			ma.showPanel(false);
			if(act.command==Action.EDIT)
			{
				DialogEditor ed = new DialogEditor(ma, mTitleText, mTitleText);
				ed.show();
				return;
			}
			ma.runAction(act);
		}
	}

	@Override
	public void onClick(View v) {
		ActArray ar = new ActArray();
		ar.add(Action.COPY_TEXT);
		ar.add(new SearchAction(SearchSystem.CMD_SEARCH, R.string.act_search, R.drawable.search));
		ar.add(Action.EDIT);
		new MenuPanelButton(getContext(), ar, this).show();
	}
	final Tab getWebWindow()
	{
		if(getContext() instanceof MainActivity)
			return ((MainActivity)getContext()).getTab();
		return null;
	}
	final void setFromWebWindow()
	{
		Tab ww = getWebWindow();
		if(ww!=null&&ww.getCurBookmark()!=null)
		{
			setText(ww.getCurBookmark().getTitle());
		}
	}
	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		checkMaxLines(UIUtils.isPortrait(newConfig));
	}
	public void checkMaxLines(boolean portrait)
	{
		mTitle.setMaxLines(portrait?MAX_LINES_PORTRAIT:MAX_LINES_LANDSCAPE);
	}
	
}
