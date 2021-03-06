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

public abstract class DialogExport extends ThemedDialog {

	TextView tv_dirname;
	CheckBox cb_change;
	Context m_c = null;
	/** папка для сохранения бэкапа */
	String folder = null;
	
	public DialogExport(Context context)
	{
		super(context);
		m_c=context;
		init(null);
	}
	void init(String name)
	{
		View v =  setView(R.layout.dialog_export);
		setButtons(true);
		setTitleText(R.string.act_export);
		cb_change = (CheckBox)v.findViewById(R.id.cb_save_change);
		cb_change.setOnClickListener(this);
		cb_change.setChecked(Prefs.getBoolean(Prefs.EXPORT_SAVE_CHANGE, false));
		folder = Prefs.getString(Prefs.BACKUP_SETTING_FOLDER, 
				Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
				.getAbsolutePath());
		tv_dirname = (TextView)v.findViewById(R.id.dirname);
		tv_dirname.setText(folder);
		tv_dirname.setOnClickListener(this);
		v.findViewById(R.id.dirSelector).setOnClickListener(this);
		TextView tv = (TextView)v.findViewById(R.id.tv_dir_export);
		MyTheme.get().setViews(MyTheme.ITEM_DIALOG_TEXT, tv,cb_change,tv_dirname);
	}
	@Override
	protected void onOk(boolean ok) {
		super.onOk(ok);
		if(ok)
		{
			new ImportExport(m_c).export(folder, true);
//			String destDir = mDir.getText().toString();
//			File f = new File(destDir);
//			if(!f.exists())
//				f.mkdirs();
//			if(mUrl.getVisibility()!=View.GONE)
//			{
//				try{
//					String text = mUrl.getText().toString();
//					Uri uri = Uri.parse(text);
//					if(uri.isHierarchical())
//						mDownloadUri = uri;
//				}
//				catch(Throwable e)
//				{
//					
//				}
//			}
			
//			doSave(mDownloadUri, mParentFolder,mName.getText().toString(),mId);
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
					Prefs.setString(Prefs.BACKUP_SETTING_FOLDER, folder);
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
