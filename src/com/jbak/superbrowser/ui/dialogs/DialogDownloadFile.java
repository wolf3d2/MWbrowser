package com.jbak.superbrowser.ui.dialogs;

import java.io.File;
import java.util.ArrayList;

import ru.mail.webimage.FileUtils;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.jbak.superbrowser.Action;
import com.jbak.superbrowser.Bookmark;
import com.jbak.superbrowser.BookmarkActivity;
import com.jbak.superbrowser.Prefs;
import com.mw.superbrowser.R;
import com.jbak.superbrowser.stat.DownloadOptions;
import com.jbak.superbrowser.UrlProcess.DownloadFileInfo;
import com.jbak.superbrowser.stat;
import com.jbak.superbrowser.adapters.SettingsAdapter;
import com.jbak.superbrowser.adapters.SettingsAdapter.OnMenuItemSelected;
import com.jbak.superbrowser.adapters.SettingsBookmark;
import com.jbak.superbrowser.ui.OnAction;
import com.jbak.superbrowser.ui.themes.MyTheme;
import com.jbak.utils.ObjectKeyValues;
import com.jbak.utils.StrBuilder;

public abstract class DialogDownloadFile extends ThemedDialog {

	EditText mName;
	TextView mDownloadDir;
	View mDirImage;
	Uri mDownloadUri;
	DownloadOptions mDownloadOptions;
	DownloadFileInfo mDownloadInfo;
	View mNetworkSelector;
	TextView mNetworkType;
	CompoundButton mShowDownloader;
	@SuppressLint("InlinedApi")
	ObjectKeyValues<Integer, Integer> mNetworks = new ObjectKeyValues<Integer, Integer>(
			R.string.downloadNetworkAll,DownloadManager.Request.NETWORK_WIFI|DownloadManager.Request.NETWORK_MOBILE,
			R.string.downloadNetworkWiFi,DownloadManager.Request.NETWORK_WIFI,
			R.string.downloadNetworkMobile,DownloadManager.Request.NETWORK_MOBILE
			);
	public DialogDownloadFile(Context context,DownloadOptions options,DownloadFileInfo fi) 
	{
		super(context);
		mDownloadInfo = fi;
		if(options==null)
			options = new DownloadOptions(fi.filename);
		if(TextUtils.isEmpty(options.destDir))
			options.destDir = getDefaultDir();
		mDownloadOptions = options;
		initDialog();
	}
	void initDialog()
	{
		View v =  setView(R.layout.dialog_download_file);
		setButtons(true);
		String tt = getString(R.string.act_savefile);
		String inf = stat.STR_NULL;
		if(mDownloadInfo.fileSize>0)
			inf+=Formatter.formatFileSize(context(), mDownloadInfo.fileSize);
		if(!TextUtils.isEmpty(mDownloadInfo.mimeType))
		{
			if(!TextUtils.isEmpty(inf))
				inf+=" - ";
			inf+=mDownloadInfo.mimeType;
		}
		if(!TextUtils.isEmpty(inf))
			tt = new StrBuilder(context()).add(tt).addBrackets(inf).toString();
		setTitleText(tt);
		mNetworkSelector = findViewById(R.id.networkSelector);
		mName = (EditText)v.findViewById(R.id.name);
		mDownloadDir = (TextView)v.findViewById(R.id.dirName);
		mName.setText(mDownloadOptions.destFileName);
		mDownloadDir.setText(mDownloadOptions.destDir);
		mDirImage = findViewById(R.id.dirImage);
		mNetworkType = (TextView) findViewById(R.id.downloadNetwork);
		mShowDownloader = (CompoundButton)findViewById(R.id.showDownloader);
		mShowDownloader.setChecked(mDownloadOptions.showDownloader);
		if(mDownloadOptions.showNetworkSelector)
			mNetworkSelector.setOnClickListener(this);
		else
		{
			mNetworkSelector.setVisibility(View.GONE);
			mShowDownloader.setVisibility(View.GONE);
		}	
		mNetworkType.setText(mNetworks.getKeyByValue(mDownloadOptions.downloadNetworks));
		v.findViewById(R.id.dirSelector).setOnClickListener(this);
		MyTheme.get().setViews(MyTheme.ITEM_DIALOG_TEXT, mDownloadDir,mNetworkType,mShowDownloader,findViewById(R.id.downloadNetworkTitle));
	}
	public static String getDefaultDir()
	{
		String def = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
		return Prefs.get().getString(Prefs.DOWNLOAD_FOLDER, def);
	}
	final boolean checkCanWebDownload()
	{
		File f = FileUtils.getFileStorageDir(getDestDir());
		return f==null||f.equals(FileUtils.getFileStorageDir(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)))||f.equals(Environment.getExternalStorageDirectory());
	}
	final File getDestDir()
	{
		String destDir = mDownloadDir.getText().toString();
		return new File(destDir);
	}
	final boolean isWebDownload()
	{
		return mDownloadOptions.showNetworkSelector;
	}
	void showSdcardError()
	{
		ArrayList<Bookmark>ar = new ArrayList<Bookmark>();
		SettingsAdapter sa = new SettingsAdapter(context(), ar);
		SettingsBookmark sb = new SettingsBookmark(getString(R.string.sdcard_not_default), null);
		sa.showMenuTextIds(sb, new OnMenuItemSelected() {
			
			@Override
			public void onMenuItemSelected(int selectedIndex,
					SettingsBookmark settingsEdit, SettingsBookmark settingsSelected) {
				if(selectedIndex==0)
					context().startActivity(new Intent(Settings.ACTION_MEMORY_CARD_SETTINGS));
					
			}
		}, R.string.change_sdcard);
	}
	@Override
	protected void onOk(boolean ok) {
		File mnt = FileUtils.getFilePathFromMnt(getDestDir());
//		if(ok&&isWebDownload()&&!checkCanWebDownload())
//		{
//			File f = getDestDir();
//			f = FileUtils.setStorageToExternalDir(f);
//			mDownloadDir.setText(f.getAbsolutePath());
//			showSdcardError();
//			return;
//		}
		super.onOk(ok);
		if(ok)
		{
			File f = getDestDir();
			if(!f.exists())
				f.mkdirs();
			mDownloadOptions.destDir = f.getAbsolutePath();
			mDownloadOptions.destFileName = mName.getText().toString();
			File destFile = new File(f, mDownloadOptions.destFileName);
			mDownloadOptions.destFile = destFile;
			mDownloadOptions.showDownloader = mShowDownloader.isChecked();
			doSave(mDownloadOptions);
		}
	}
	@Override
	public void onClick(View v) {
		if(v.getId()==R.id.dirSelector)
		{
			BookmarkActivity.runForFileDirSelect((Activity)context(), new OnAction() {
				
				@Override
				public void onAction(final Action act) {
					mDownloadDir.post(new Runnable() {
						
						@Override
						public void run() {
							File f = (File) act.param;
							mDownloadDir.setText(FileUtils.fileToCanonical(f).getAbsolutePath());
						}
					});
				}
			});
			return;
		}
		else if(v.getId()==R.id.networkSelector)
		{
			new MenuSettingBookmarks(context(),getString(R.string.downloadNetwork),R.string.downloadNetworkAll,R.string.downloadNetworkWiFi,R.string.downloadNetworkMobile) 
			{
				@Override
				public void onBookmarkSelected(int pos, SettingsBookmark set) {
					mDownloadOptions.downloadNetworks = mNetworks.getValueByKey(set.id);
					mNetworkType.setText(set.getTitle());
				}
			}.show();
		}
		super.onClick(v);
	}
	public abstract void doSave(DownloadOptions options);
}
