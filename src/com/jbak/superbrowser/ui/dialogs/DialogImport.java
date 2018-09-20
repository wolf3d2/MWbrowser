package com.jbak.superbrowser.ui.dialogs;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import ru.mail.mailnews.st;

import com.jbak.superbrowser.Action;
import com.jbak.superbrowser.Bookmark;
import com.jbak.superbrowser.BookmarkActivity;
import com.jbak.superbrowser.Db;
import com.jbak.superbrowser.ImportExport;
import com.jbak.superbrowser.Prefs;
import com.jbak.superbrowser.stat;
import com.mw.superbrowser.R;
import com.jbak.superbrowser.adapters.BookmarkFolderAdapter;
import com.jbak.superbrowser.ui.OnAction;
import com.jbak.superbrowser.ui.themes.MyTheme;
import com.jbak.ui.CustomPopup;

public abstract class DialogImport extends ThemedDialog 
{
	TextView tv_dirname;
	TextView tv_empty;
	CheckBox cb_set;
	CheckBox cb_bm;
	Context m_c = null;
	/** папка для сохранения бэкапа */
	String folder = null;
	
	public DialogImport(Context context)
	{
		super(context);
		m_c=context;
		init(null);
	}
	void init(String name)
	{
		View v =  setView(R.layout.dialog_import);
		setButtons(true);
		setTitleText(R.string.act_import);
		cb_set = (CheckBox)v.findViewById(R.id.cb_import_setting);
		cb_set.setOnClickListener(this);
		cb_bm = (CheckBox)v.findViewById(R.id.cb_import_bookmark);
		cb_bm.setOnClickListener(this);
		tv_empty = (TextView)v.findViewById(R.id.tv_import_empty);
		folder = Prefs.getString(Prefs.BACKUP_SETTING_FOLDER, 
				Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
				.getAbsolutePath());
		tv_dirname = (TextView)v.findViewById(R.id.dirname);
		tv_dirname.setText(folder);
		tv_dirname.setOnClickListener(this);
		setView();

		v.findViewById(R.id.dirSelector).setOnClickListener(this);
		MyTheme.get().setViews(MyTheme.ITEM_DIALOG_TEXT, 1);
	}
	public void setView() {
		int iii = 0;
		File fff = new File(folder+File.separatorChar+ImportExport.FILENAME_SETTING);
		if (fff.isFile()) {
			cb_set.setVisibility(View.VISIBLE);
			iii++;
		} else
			cb_set.setVisibility(View.GONE);
			
		fff = new File(folder+File.separatorChar+ImportExport.FILENAME_BOOKMARK);
		if (fff.isFile()) {
			cb_bm.setVisibility(View.VISIBLE);
			iii++;
		} else
			cb_bm.setVisibility(View.GONE);
		if (iii == 0)
			tv_empty.setVisibility(View.VISIBLE);
		else
			tv_empty.setVisibility(View.GONE);
	}
	@Override
	protected void onOk(boolean ok) {
		super.onOk(ok);
		if(ok)
		{
			new ImportExport(m_c).importGlobal(folder, cb_bm.isChecked(), cb_set.isChecked(), true);
		}
	}
	@Override
	public void onClick(View v) {
		switch (v.getId())
		{
		case R.id.cb_save_change:
			Prefs.setBoolean(Prefs.EXPORT_SAVE_CHANGE, ((CheckBox)v).isChecked());
			return;
		case R.id.dirSelector:
		case R.id.dirname:
			BookmarkActivity.runForFileDirSelect((Activity)m_c, new OnAction() {
				
				@Override
				public void onAction(Action act) {
					 
					folder = ((File)act.param).getAbsolutePath();
					tv_dirname.setText(folder);
					setView();
				}
			});

//			BookmarkActivity.runForBookmarkFolderSelect((Activity)context(), new OnAction() {
//				
//				@Override
//				public void onAction(Action act) {
//// 					mParentFolder = (Bookmark) act.param;
////					mDir.setText(mParentFolder.getTitle());
////					String saveid = stat.STR_NULL+(Long)mParentFolder.param; 
////					Db.getStringTable().save(Db.LAST_BOOKMARK_FOLDER_ID, saveid);
//				}
//			});
			return;
		}
		super.onClick(v);
	}
	public abstract void doSave();
}
