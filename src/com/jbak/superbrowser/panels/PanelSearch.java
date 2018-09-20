package com.jbak.superbrowser.panels;


import android.content.Context;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.FrameLayout;

import com.jbak.superbrowser.ActArray;
import com.jbak.superbrowser.Action;
import com.jbak.superbrowser.BrowserApp;
import com.jbak.superbrowser.BrowserApp.OnGlobalEventListener;
import com.jbak.superbrowser.stat;
import com.jbak.superbrowser.recycleview.SearchRecyclerAdapter;
import com.jbak.superbrowser.ui.HorizontalPanel;
import com.jbak.superbrowser.ui.MenuPanelButton;
import com.jbak.superbrowser.ui.OnAction;
import com.jbak.superbrowser.ui.PanelButton;
import com.jbak.superbrowser.ui.themes.MyTheme;

public class PanelSearch extends FrameLayout implements OnGlobalEventListener,OnLongClickListener{
	HorizontalPanel mHorizontalPanel;
	public PanelSearch(Context context) {
		super(context);
		init();
	}
	void init()
	{
		BrowserApp.INSTANCE.addGlobalListener(this);
		mHorizontalPanel = new HorizontalPanel(getContext());
		mHorizontalPanel.setButtonsType(PanelButton.TYPE_BUTTON_TEXT_ONLY);
		mHorizontalPanel.setCanAutofill(false);
		mHorizontalPanel.setCheckWidthWhileNotAutoFill(false);
		SearchRecyclerAdapter ad = new SearchRecyclerAdapter(getContext());
		ad.refreshCursor();
		mHorizontalPanel.setItemLongClickListener(this);
		mHorizontalPanel.setRecyclerAdapter(ad);
		addView(mHorizontalPanel);
		MyTheme.get().setView(this, MyTheme.ITEM_HORIZONTAL_PANEL_BACKGROUND);
	}
	public void setOnActionListener(OnAction listener)
	{
		mHorizontalPanel.setOnActionListener(listener);
	}
	@Override
	public void onGlobalEvent(int code, Object param) {
		if(code==BrowserApp.GLOBAL_SEARCH_CHANGED)
		{
			((SearchRecyclerAdapter)mHorizontalPanel.getRealAdapter()).refreshCursor();
			mHorizontalPanel.getRealAdapter().notifyDataSetChanged();
		}
	}
	public final HorizontalPanel getPanel()
	{
		return mHorizontalPanel;
	}
	@Override
	public boolean onLongClick(View v) {
		Action a = (Action) v.getTag();
		final String text = a.getText(getContext());
		ActArray ar = new ActArray(Action.DELETE_ITEM,Action.COPY_TEXT);
		new MenuPanelButton(getContext(), ar, new OnAction() {
			@Override
			public void onAction(Action act) {
				if(act.command==Action.DELETE_ITEM)
					SearchRecyclerAdapter.deleteSearchResult(getContext(), text);
				else if(act.command==Action.COPY_TEXT)
					stat.setClipboardString(getContext(), text);
			}
		}).show();
		return false;
	}
	@Override
	public void setBackgroundColor(int color) {
		super.setBackgroundColor(color);
	}
}
