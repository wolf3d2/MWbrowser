package com.jbak.superbrowser.ui;

import ru.mail.mailnews.st;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.Html;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jbak.superbrowser.ActArray;
import com.jbak.superbrowser.Action;
import com.jbak.superbrowser.MainActivity;
import com.mw.superbrowser.R;
import com.jbak.superbrowser.SearchAction;
import com.jbak.superbrowser.Tab;
import com.jbak.superbrowser.recycleview.RecyclerViewEx;
import com.jbak.superbrowser.search.SearchSystem;
import com.jbak.superbrowser.ui.themes.MyTheme;
import com.jbak.utils.StrBuilder;

public class ErrorLayout extends LinearLayout implements OnAction{

	HorizontalPanel mActionPanel;
	TextView mErrorText;
	TextView mTitle;
	public ErrorLayout(Context context,Tab ww) {
		super(context);
		setOrientation(VERTICAL);
		mTitle = (TextView) LayoutInflater.from(context).inflate(R.layout.textview_title, null);
		MyTheme.get().setViews(MyTheme.ITEM_TITLE, mTitle);
		mTitle.setText(R.string.load_error);
		addView(mTitle);
		mErrorText = (TextView) LayoutInflater.from(context).inflate(R.layout.textview_normal, null);
		int pad = st.dp2px(context, 20);
		mErrorText.setPadding(pad, pad, pad, pad);
		mErrorText.setMaxLines(10);
		MyTheme.get().setViews(MyTheme.ITEM_DIALOG_TEXT, mErrorText);
		addView(mErrorText);
		StrBuilder sb = new StrBuilder(context).addTag(StrBuilder.TAG_B, ww.description, null).addBr().addBr().add(ww.failingUrl);
		mErrorText.setText(Html.fromHtml(sb.toString()));
		mActionPanel = new HorizontalPanel(context);
		mActionPanel.setMaxHeight(Integer.MAX_VALUE);
		mActionPanel.setType(RecyclerViewEx.TYPE_GRID);
		mActionPanel.setWrapContent(true);
		ActArray ar = new ActArray(Action.REFRESH);
		ar.add(Action.create(Action.COPY_URL_TO_CLIPBOARD).setParam(ww.failingUrl));
		ar.add(Action.SYSTEM_SETTINGS,Action.SYSTEM_MOBILE_SETTINGS,Action.SYSTEM_WIFI_NETWORKS);
		ar.add(new SearchAction(SearchSystem.CMD_CACHED_PAGE, R.string.act_cached_page, R.drawable.cached_page));
		ar.add(Action.create(Action.SHARE_ELEMENT, ww.failingUrl));
		ar.add(Action.EXIT);
		mActionPanel.setActions(ar);
		addView(mActionPanel);
		mActionPanel.setOnActionListener(this);
		MyTheme.get().setViews(MyTheme.ITEM_DIALOG_BACKGROUND, this);
		MyTheme.get().setViews(MyTheme.ITEM_MAIN_PANEL_BACKGROUND, mActionPanel);
	}
	@Override
	public void onAction(Action act) {
		MainActivity ma = (MainActivity)getContext();
		if(act.command==Action.SETTINGS)
			ma.startActivity(new Intent(Settings.ACTION_SETTINGS));
		else
			ma.runAction(act);
	}
	
}
