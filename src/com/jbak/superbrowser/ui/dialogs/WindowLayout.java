package com.jbak.superbrowser.ui.dialogs;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.jbak.superbrowser.ActArray;
import com.jbak.superbrowser.Action;
import com.jbak.superbrowser.Bookmark;
import com.jbak.superbrowser.MainActivity;
import com.jbak.superbrowser.WebViewEvent;
import com.jbak.superbrowser.Tab;
import com.jbak.superbrowser.panels.PanelTitle;
import com.jbak.superbrowser.panels.PanelUrlEdit;
import com.jbak.superbrowser.recycleview.RecyclerViewEx;
import com.jbak.superbrowser.ui.HorizontalPanel;
import com.jbak.superbrowser.ui.OnAction;
import com.jbak.superbrowser.ui.themes.MyTheme;

public class WindowLayout extends RelativeLayout  implements OnAction,WebViewEvent,OnClickListener{
	PanelUrlEdit mUrlEdit;
	HorizontalPanel mToolsGrid;
	PanelTitle mTitle;
	Tab mWindow;
	private static final int URL_EDIT_ID = 100;
	private static final int TITLE_ID = 101;
	public WindowLayout(MainActivity context,Tab window) {
		super(context);
		mWindow = window;
		init();
	}
	private void init() {
		setOnClickListener(this);
		getMain().addWebViewListener(this);
		RelativeLayout.LayoutParams lp;
		lp = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		mTitle = new PanelTitle(getMain());
		mTitle.setId(TITLE_ID);
		addView(mTitle,lp);
		
		lp = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		mUrlEdit = new PanelUrlEdit(getContext(),PanelUrlEdit.STYLE_ONE_LINE|PanelUrlEdit.STYLE_VISIBLE_TOOLS);
		mUrlEdit.setActionListener(this);
		mUrlEdit.setId(URL_EDIT_ID);
		addView(mUrlEdit,lp);

		
		mToolsGrid = new HorizontalPanel(getContext());
		mToolsGrid.setType(RecyclerViewEx.TYPE_GRID);
		mToolsGrid.setWrapContent(true);
		mToolsGrid.setReverseLayout(true);
		mToolsGrid.setMaxHeight(Integer.MAX_VALUE);
		mToolsGrid.setOnUnusedSpaceClickListener(this);
		mToolsGrid.setOnActionListener(this);
		lp = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		lp.addRule(RelativeLayout.ABOVE,URL_EDIT_ID);
		lp.addRule(RelativeLayout.BELOW,TITLE_ID);
		addView(mToolsGrid, lp);
		//MyTheme.get().setViews(MyTheme.ITEM_MAIN_PANEL_BACKGROUND, mToolsGrid);

		if(mWindow!=null&&mWindow.getCurBookmark()!=null)
			mTitle.setText(mWindow.getCurBookmark().getTitle());
		Bookmark bm = mWindow.getCurBookmark();
		if(bm!=null)
		{
			mTitle.setText(bm.getTitle());
			mUrlEdit.setUrl(bm.getUrl());
		}
		MyTheme.get().setViews(MyTheme.ITEM_TITLE, mTitle);
		((MainActivity)getContext()).addWebViewListener(mUrlEdit);
		setToolsActions();
	}
	public void setToolsActions()
	{
		Tab appMainWindow = getMain().getTab();
		
		ActArray ar = new ActArray();
		ar.add(Action.create(Action.CLOSE_TAB).setParam(mWindow.windowId));
		if(appMainWindow!=null&&appMainWindow.getCurBookmark()!=null)
			ar.add(null,appMainWindow.getCurBookmark());
		ar.add(Action.NEW_TAB);
		ar.add(Action.TAB_LIST);
		ar.add(Action.create(Action.CLOSE_ALL_TABS));
		if(mWindow==getMain().getTab())
			ar.add(Action.TAB_HISTORY);
		else
			ar.add(Action.OPEN_TAB);
		ar.add(Action.TAB_PANEL_SETTINGS);
		mToolsGrid.setActions(ar);
		mToolsGrid.getRealAdapter().setTransparentButtons(true);
	}
	@Override
	public void onAction(Action act) {
		getMain().clearCustomViews();
		if(mWindow!=getMain().getTab())
		{
			getMain().tabOpen(mWindow.windowId);
			if(act.command==Action.OPEN_TAB)
				return;
		}
		getMain().runAction(act);
	}
	@Override
	public void onWebViewEvent(int code, EventInfo info) {	
		if(code==WWEVENT_SOFT_KEYBOARD_VISIBLE&&mUrlEdit.hasInputFocus())
		{
			mTitle.setVisibility(GONE);
			mToolsGrid.setVisibility(GONE);
		}
		if(code==WWEVENT_SOFT_KEYBOARD_HIDDEN&&mTitle.getVisibility()!=VISIBLE)
		{
			mTitle.setVisibility(VISIBLE);
			mToolsGrid.setVisibility(VISIBLE);
		}
	}
	@Override
	public void onClick(View v) {
		if(v.getTag() instanceof Action)
		{
			Action a = (Action) v.getTag();
			onAction(a);
			return;
		}
		mUrlEdit.hideKeyboard();
		((MainActivity)getContext()).clearCustomViews();
	}
	final MainActivity getMain() {
		return (MainActivity) getContext();
	}
}
