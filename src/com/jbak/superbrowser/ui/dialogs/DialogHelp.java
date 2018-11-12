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

public class DialogHelp extends ThemedDialog{

	HorizontalPanel mPanel;
	/** диалог с одной кнопкой Ok 
	 * @param title - заголовок окна. Если null, то выводим название приложения и код версии
	 * @param text - выводимый текст
	 * @param arpanel - массив панели кнопок. Если null, то одна кнопка Ok
	 * */
	public DialogHelp(Context context, String title,String text, ActArray arpanel) {
		super(context);
		View v = setView(R.layout.dialog_help);
		if (arpanel == null) {
			arpanel = new ActArray();
			arpanel.add(Action.create(Action.OK));
		}
		setButtons(arpanel, 1);
		//mPanel.setActions(ar);
		TextView tv = (TextView)v.findViewById(R.id.help);
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
// пока не используется - только одна кнопка Ok		
//		switch (act.command) {
//			case Action.HELP:
//				try{
//					BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_ACTION, Action.create(Action.HELP));
//				}
//				catch (Throwable e) {
//				}
//			break;
//		}
	}

}
