package com.jbak.superbrowser.ui.dialogs;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
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
import com.jbak.superbrowser.Prefs;
import com.jbak.superbrowser.stat;
import com.mw.superbrowser.R;
import com.jbak.superbrowser.adapters.BookmarkFolderAdapter;
import com.jbak.superbrowser.ui.OnAction;
import com.jbak.superbrowser.ui.themes.MyTheme;

public abstract class DialogBookmark extends ThemedDialog {

	EditText mName;
	TextView mDir;
	ImageView mPreview;
	View mDirImage;
	Uri mDownloadUri;
	EditText mUrl;
	CheckBox cb_show_kbd;
	Bookmark mParentFolder;
	Bitmap mPreviewImage;
	long mId;
	
	public DialogBookmark(Context context,Uri uri, String name,Bookmark parentFolder,Bitmap previewImage)
	{
		this(context, uri, name, parentFolder, -1,previewImage);
	}
	public DialogBookmark(Context context,Uri uri, String name,Bookmark parentFolder,long id,Bitmap previewImage) 
	{
		super(context);
		mPreviewImage = previewImage;
		mDownloadUri = uri;
		mParentFolder = parentFolder;
		mId = id;
		if(mParentFolder==null)
		{
			if(mId<1)
			{
				String sid = Db.getStringTable().get(Db.LAST_BOOKMARK_FOLDER_ID);
				if(!TextUtils.isEmpty(sid)&&TextUtils.isDigitsOnly(sid))
					mParentFolder = BookmarkFolderAdapter.getFolder(context.getContentResolver(), Long.decode(sid));
			}
			if(mParentFolder==null)
				mParentFolder = Bookmark.fromBookmarkFolder(context.getString(R.string.act_bookmarks), 1);
		}
		init(name);
	}
	void init(String name)
	{
		View v =  setView(R.layout.dialog_bookmark);
		setButtons(true);
		setTitleText(mId>0?R.string.act_edit:R.string.act_add_bookmark);
		mName = (EditText)v.findViewById(R.id.name);
		cb_show_kbd = (CheckBox)v.findViewById(R.id.cb_showkbd);
		cb_show_kbd.setOnClickListener(this);
		cb_show_kbd.setChecked(Prefs.getBoolean(Prefs.SHOW_KBD, false));
		if (cb_show_kbd.isChecked())
			st.showEditKeyboard(mName);
		mDir = (TextView)v.findViewById(R.id.dirName);
		mUrl = (EditText)v.findViewById(R.id.bookmarkUrl);
		mPreview = (ImageView)v.findViewById(R.id.preview);
		mPreview.setOnClickListener(this);
		if(mPreviewImage==null)
			mPreview.setVisibility(View.GONE);
		else
		{
			mPreview.setImageBitmap(mPreviewImage);
		}
		mName.setText(name);
		if(mParentFolder!=null)
			mDir.setText(mParentFolder.getTitle());
		mDirImage = findViewById(R.id.dirImage);
		if(mDownloadUri!=null) {
			mUrl.setText(mDownloadUri.toString());
		}else {
			mUrl.setVisibility(View.GONE);
		}
		v.findViewById(R.id.dirSelector).setOnClickListener(this);
		MyTheme.get().setViews(MyTheme.ITEM_DIALOG_TEXT, mDir, cb_show_kbd);
	}
	@Override
	public void onClick(View v) {
		if(v.getId()==R.id.cb_showkbd) {
			Prefs.setBoolean(Prefs.SHOW_KBD, cb_show_kbd.isChecked());
			if (cb_show_kbd.isChecked())
				st.showEditKeyboard(mName);
			else
				st.hideEditKeyboard(mName);
		}
		if(v.getId()==R.id.preview)
			return;
		if(v.getId()==R.id.dirSelector)
		{
			BookmarkActivity.runForBookmarkFolderSelect((Activity)context(), new OnAction() {
				
				@Override
				public void onAction(Action act) {
 					mParentFolder = (Bookmark) act.param;
					mDir.setText(mParentFolder.getTitle());
					String saveid = st.STR_NULL+(Long)mParentFolder.param; 
					Db.getStringTable().save(Db.LAST_BOOKMARK_FOLDER_ID, saveid);
				}
			});
			return;
		}
		super.onClick(v);
	}
	@Override
	protected void onOk(boolean ok) {
		super.onOk(ok);
		if(ok)
		{
			String destDir = mDir.getText().toString();
			File f = new File(destDir);
			if(!f.exists())
				f.mkdirs();
			if(mUrl.getVisibility()!=View.GONE)
			{
				try{
					String text = mUrl.getText().toString();
					Uri uri = Uri.parse(text);
					if(uri.isHierarchical())
						mDownloadUri = uri;
				}
				catch(Throwable e)
				{
					
				}
			}
			doSave(mDownloadUri, mParentFolder,mName.getText().toString(),mId);
		}
	}
	public abstract void doSave(Uri uri,Bookmark parentDir,String name,long id);
}
