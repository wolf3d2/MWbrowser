package com.jbak.superbrowser.ui.dialogs;

import java.io.File;
import java.util.Locale;

import ru.mail.mailnews.st;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.TextView;

import com.jbak.superbrowser.ActArray;
import com.jbak.superbrowser.Action;
import com.jbak.superbrowser.Bookmark;
import com.jbak.superbrowser.BrowserApp;
import com.jbak.superbrowser.IConst;
import com.jbak.superbrowser.stat;
import com.mw.superbrowser.R;
import com.jbak.superbrowser.ui.HorizontalPanel;
import com.jbak.superbrowser.ui.themes.MyTheme;
import com.jbak.utils.Utils;

public class DialogAbout extends ThemedDialog{

	HorizontalPanel mPanel;
	
	public DialogAbout(Context context, String title,String text, ActArray arpanel) {
		super(context);
		View v = setView(R.layout.dialog_about);
		if (arpanel!=null){
			setButtons(arpanel, 1);
			//mPanel.setActions(ar);
		}
		TextView tv = (TextView)v.findViewById(R.id.description);
		//tv.setText(text);
		tv.setMaxLines(20);
		//tv.setMaxHeight(stat.getSizeHeight(context));
		if (title == null)
			setTitleText(st.getAppNameAndVersion(context));
		else 
			setTitleText(title);
		if (text!=null)
			tv.setText(text); 
		MyTheme.get()
			.setViews(MyTheme.ITEM_DIALOG_TEXT,tv);
		MyTheme.get()
		.setViews(MyTheme.ITEM_HORIZONTAL_PANEL_BACKGROUND,mPanel);
	}
	@Override
	public void onAction(Action act) {
		dismiss();
		switch (act.command) {
			case Action.APP_MARKET:
					goMarket(context(), context().getPackageName());
				break;
			case Action.FEEDBACK:
				sendFeedback(context());
			break;
			case Action.OTHER_APPS:
				goMarketApps(context());
			break;
			case Action.FOUR_PDA:
				try{
					Bookmark bm = new Bookmark(IConst.TEAM_4PDA, null, System.currentTimeMillis());
//					bm.tabMode = Action.NEW_TAB;
					BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_ACTION, Action.create(Action.ACTION_BOOKMARK,bm));
				}
				catch (Throwable e) {
				}
			break;
			case Action.ABOUT:
				try{
					BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_ACTION, Action.create(Action.ABOUT));
				}
				catch (Throwable e) {
				}
			break;
			case Action.HELP:
				try{
					BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_ACTION, Action.create(Action.HELP));
				}
				catch (Throwable e) {
				}
			break;
			case Action.WHATS_NEW:
				try{
					BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_ACTION, Action.create(Action.WHATS_NEW));
				}
				catch (Throwable e) {
				}
			break;
		}
	}
	public static void sendFeedback(Context c) {
		sendFeedback(c, null);
	}
	public static void sendFeedback(Context c,File crash) {
		StringBuilder info = new StringBuilder();
		String delim = ": ";
		// инфо о девайсе
		info.append(String.format(Locale.ENGLISH, "%s%s%s%s\n","Application",
				delim, st.STR_NULL,
				st.getAppNameAndVersion(c)));
		info.append(String.format(Locale.ENGLISH, "%s%s%s%s\n","Device locale",
				delim, st.STR_NULL,
				Locale.getDefault().getLanguage()));
		info.append(String.format(Locale.ENGLISH, "%s%s%s%s\n","Os",
				delim, "Android ",
				Build.VERSION.RELEASE));
		info.append(String.format(Locale.ENGLISH, "%s%s%s%s\n","Manufacture",
				delim, st.STR_NULL,
				Build.MANUFACTURER));
		info.append(String.format(Locale.ENGLISH, "%s%s%s\n",
				"Device", delim, Build.MODEL));
		info.append("\n===\n");
		
		if(crash!=null)
		{
			info.append(c.getString(R.string.app_info));
			info.append("\n===\n\n");
			info.append(st.fileToStr(crash));
			info.append("\n===\n");
		}
		final Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.setType("text/message");
		emailIntent.putExtra(Intent.EXTRA_EMAIL,
				new String[] { "wolf3d@tut.by" });

		String subj = null;
		if(crash==null)
			subj = c.getString(R.string.feedback_subject) + st.getAppNameAndVersion(c);
		else
			subj = "Crash report "+st.getAppNameAndVersion(c);
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, subj);
		emailIntent.putExtra(Intent.EXTRA_TEXT, info.toString());
		c.startActivity(Intent.createChooser(emailIntent, c.getString(R.string.act_feedback)));
	}
	public static void goMarketApps(Context c) {
		String link =  "https://play.google.com/store/apps/developer?id=Михаил+Вязенкин";
		try {
	        Intent intent = new Intent(Intent.ACTION_VIEW);
	        intent.setData(Uri.parse(link));
	        c.startActivity(intent);

//			c.startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(link)));
		} catch (Throwable e) {
		}
		
	}
	public static void goMarket(Context c, String pkg) {
		String link = " линк браузера на маркете";
//		link += pkg;
		try {
			c.startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(link)));
		} catch (Throwable e) {
			Utils.log(e);
		}
	}


}
