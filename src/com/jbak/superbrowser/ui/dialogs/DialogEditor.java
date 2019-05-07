package com.jbak.superbrowser.ui.dialogs;

import java.io.FileOutputStream;

import android.content.Context;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Scroller;
import ru.mail.mailnews.st;

import com.jbak.superbrowser.ActArray;
import com.jbak.superbrowser.Action;
import com.jbak.superbrowser.BrowserApp;
import com.jbak.superbrowser.Prefs;
import com.mw.superbrowser.R;
import com.jbak.superbrowser.stat;
import com.jbak.superbrowser.stat.DownloadOptions;
import com.jbak.superbrowser.UrlProcess.DownloadFileInfo;
import com.jbak.superbrowser.ui.OnAction;
import com.jbak.superbrowser.ui.themes.MyTheme;
import com.jbak.utils.Utils;

public class DialogEditor extends ThemedDialog{

	protected EditText mEdit;
	protected ActArray arAct = null;
	String mFileNameForSave;
	public DialogEditor(Context context,String title,String text)
	{
		this(context, title, text, true);
		View v =  setView(R.layout.dialog_editor);
		mEdit = (EditText) v.findViewById(R.id.editText);
		mEdit.setMaxLines(20);
		mEdit.setText(text);
		mEdit.setSelection(0);
		// время в мс, сколько показывать скролбар
		//mEdit.setScrollBarFadeDuration(5000);
		
		if (cb_showKbd!=null) {
			cb_showKbd.setVisibility(View.VISIBLE);
			cb_showKbd.setChecked(Prefs.getBoolean(Prefs.SHOW_KBD_DIALOG_EDITOR, false));
			if (cb_showKbd.isChecked())
				st.showEditKeyboard(mEdit);
			else
				st.hideEditKeyboard(mEdit);
			cb_showKbd.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					Prefs.setBoolean(Prefs.SHOW_KBD_DIALOG_EDITOR, cb_showKbd.isChecked());
					if (cb_showKbd.isChecked())
						st.showEditKeyboard(mEdit);
					else
						st.hideEditKeyboard(mEdit);

				}
			});
		}
		// показываем клавиатуру, иначе курсор не виден
		//st.showEditKeyboard(mEdit);
	}
	public DialogEditor(Context context,String title,String text,boolean hideInput) {
		super(context,hideInput,R.style.DialogEditText);
		init(context, title, text);
	}
	public DialogEditor(Context context,String title,String text,ActArray act) {
		this(context, title, text, true);
		arAct = act;
		init(context, title, text);
	}
	public void init(Context context,String title,String text)
	{
		setDialogMaxWidth(0);
		mDefPaddingDp = 0;
		mContentFrame.setPadding(0, 0, 0, 0);
		View v =  setView(R.layout.dialog_editor);
		setTitleText(title);
		mEdit = (EditText) v.findViewById(R.id.editText);
		//mEdit.setMaxHeight(stat.getSizeHeight(context));
		mEdit.setMaxLines(20);
		mEdit.setText(text);
//		LinearLayout.LayoutParams lp =  (android.widget.LinearLayout.LayoutParams) mContentFrame.getLayoutParams();
//		lp.height = 0;
//		lp.weight = 1f;
//		lp.leftMargin = lp.rightMargin = 0;
		if (arAct==null) {
			arAct = new ActArray();
			createDefaultActions(arAct);
		}
		setButtons(arAct, 1);
		mContentFrame.setBackgroundColor(0xffff0000);
		mContentFrame.setPadding(0, 0, 0, 0);
		if (cb_showKbd!=null) {
			MyTheme.get().setViews(MyTheme.ITEM_DIALOG_TEXT, cb_showKbd);
			cb_showKbd.setVisibility(View.VISIBLE);
			cb_showKbd.setChecked(Prefs.getBoolean(Prefs.SHOW_KBD_DIALOG_EDITOR, false));
			if (cb_showKbd.isChecked())
				st.showEditKeyboard(mEdit);
			else
				st.hideEditKeyboard(mEdit);
			cb_showKbd.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					Prefs.setBoolean(Prefs.SHOW_KBD_DIALOG_EDITOR, cb_showKbd.isChecked());
					if (cb_showKbd.isChecked())
						st.showEditKeyboard(mEdit);
					else
						st.hideEditKeyboard(mEdit);

				}
			});
		}
	}
	public void setOnActionListener(OnAction listener)
	{
		mActionPanel.setOnActionListener(listener);
	}
	// меню, долгое нажатие на элементе сайта (не ссылке)
	public void createDefaultActions(ActArray act)
	{
		act.add(Action.create(Action.COPY_URL_TO_CLIPBOARD).setText(R.string.act_copy_text));
		act.add(Action.create(Action.SAVEFILE));
		act.add(Action.create(Action.STOP).setText(R.string.cancel));
		act.add(Action.create(Action.HOME).setText("Home"));
		act.add(Action.create(Action.END).setText("End"));
		act.add(Action.create(Action.PGDN).setText("PgDn"));
		act.add(Action.create(Action.PGUP).setText("PgUp"));
		act.add(Action.create(Action.TOSTART).setText("to Start"));
		act.add(Action.create(Action.TOEND).setText("to End"));
	}
// листенер, долгое нажатие на элементе сайта (не ссылке)
	@Override
	public void onAction(Action act) {
		final String text = mEdit.getText().toString();
		if (act.command != Action.HOME
				&&act.command != Action.END
				&&act.command != Action.PGDN
				&&act.command != Action.PGUP
				&&act.command != Action.TOSTART
				&&act.command != Action.TOEND
				)
			dismiss();
		switch (act.command) {
		case Action.SAVEFILE:
			String saveName = "textfile.txt";
			if(!TextUtils.isEmpty(mFileNameForSave))
				saveName = mFileNameForSave;
			DownloadFileInfo fi = new DownloadFileInfo();
			fi.filename = saveName;
			fi.mimeType = "text/plain";
			fi.fileSize = text.getBytes().length;
			DownloadOptions d = new DownloadOptions(saveName);
			new DialogDownloadFile(context(),d,fi) {
				
				@Override
				public void doSave(DownloadOptions d) {
					try{
						FileOutputStream fos = new FileOutputStream(d.destFile);
						fos.write(text.getBytes());
						fos.close();
					}
					catch(Throwable e)
					{
						Utils.log(e);
					}
				}
			}.show();
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
		case Action.COPY_URL_TO_CLIPBOARD:
			stat.setClipboardString(context(), text);
			break;
		case Action.HOME:
			mEdit.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_MULTIPLE,
            		KeyEvent.KEYCODE_MOVE_HOME));
			break;
		case Action.END:
			mEdit.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_MULTIPLE,
            		KeyEvent.KEYCODE_MOVE_END));
			break;
		case Action.PGDN:
			mEdit.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_MULTIPLE,
            		KeyEvent.KEYCODE_PAGE_DOWN));
			break;
		case Action.PGUP:
			mEdit.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_MULTIPLE,
            		KeyEvent.KEYCODE_PAGE_UP));
			break;
		case Action.TOSTART:
			mEdit.setSelection(0);
			break;
		case Action.TOEND:
			mEdit.setSelection(text.length());
			break;
		default:
			break;
		}
	}
	public DialogEditor setFileNameForSave(String fileName)
	{
		mFileNameForSave = fileName;
		return this;
	}
	public final EditText getEdit()
	{
		return mEdit;
	}
	public final String getText()
	{
		return mEdit.getText().toString();
	}
	@Override
	public void setContentMargins(boolean bigMargins) {
	}
}
