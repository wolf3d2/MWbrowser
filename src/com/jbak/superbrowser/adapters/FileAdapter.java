package com.jbak.superbrowser.adapters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ru.mail.webimage.DefaultImageLoaders;
import ru.mail.webimage.DefaultImageLoaders.FileInfo;
import ru.mail.webimage.FileUtils;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.jbak.superbrowser.Action;
import com.jbak.superbrowser.Bookmark;
import com.jbak.superbrowser.BrowserApp;
import com.jbak.superbrowser.Db;
import com.jbak.superbrowser.IConst;
import com.jbak.superbrowser.stat;
import com.mw.superbrowser.R;
import com.jbak.superbrowser.adapters.BookmarkAdapter.ArrayBookmarkAdapter;
import com.jbak.superbrowser.ui.BookmarkView;
import com.jbak.superbrowser.ui.dialogs.ThemedDialog;
import com.jbak.ui.CustomDialog;
import com.jbak.ui.CustomPopup;

public abstract class FileAdapter extends ArrayBookmarkAdapter implements OnClickListener,IConst
{
	File mDir;
	public static Bookmark createBookmarkFromSdcard(Context c,File f)
	{
		Bookmark bm = new Bookmark(stat.STR_NULL, f.getName(),f.lastModified()).setImageRes(R.drawable.sdcard).setParam(f);
		return bm;
	}
	public static ArrayList<Bookmark> getHomeDir(Context c)
	{
		ArrayList<Bookmark> ar = new ArrayList<Bookmark>();
		addDirIfExists(c, ar, new File("/"), R.string.device_root).setImageRes(R.drawable.smartphoneview);
		File sdcards[] = FileUtils.getStorages();
		if(sdcards!=null&&sdcards.length>0)
		{
			FileUtils.sortFilesByName(sdcards);
			for(File f:sdcards)
				ar.add(createBookmarkFromSdcard(c, f));
		}
		if(ar.isEmpty())
		{
			File f = Environment.getExternalStorageDirectory();
			ar.add(createBookmarkFromSdcard(c, f));
		}
		addDirIfExists(c, ar, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), R.string.downloads);
		addDirIfExists(c, ar, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), R.string.dcim);
		//addDirIfExists(c, ar, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), R.string.camera);
		List<String>lastDirs = Db.getStringTable().loadList(LAST_DIRS);
		for(int i=lastDirs.size()-1;i>=0;i--)
			addDirIfExists(c, ar, new File(lastDirs.get(i)), 0);
		return ar;
	}
	public ArrayList<Action> getActionsForPanel()
	{
		ArrayList<Action>ar = new ArrayList<Action>();
		if(mDir!=null)
		{
			if(canGoUp())
				ar.add(Action.create(Action.NEW_FOLDER));
			if(mDir.getParentFile()!=null)
				ar.add(Action.create(Action.GO_UP));
			ar.add(Action.create(Action.GO_HOME));
		}
		return ar;
	}
	private static final Bookmark addDirIfExists(Context c,ArrayList<Bookmark>ar,File dir,int nameRes)
	{
		if(dir==null||!dir.exists())
			return null;
		for(Bookmark b:ar)
		{
			File f = (File) b.param;
			if(f!=null&&f.equals(dir))
				return null;
		}
		String s = null;
		if(nameRes!=0)
			s = c.getString(nameRes);
		Bookmark bm = createDirBookmark(c, dir, s);
		ar.add(bm);
		return bm;
	}
	public static final Bookmark createDirBookmark(Context c,File f,String name)
	{
		return new Bookmark(stat.STR_NULL, TextUtils.isEmpty(name)?f.getName():name, f.lastModified()).setImageRes(R.drawable.folder).setParam(f);

	}
	public static ArrayList<Bookmark> createBookmarksFromFiles(Context c,File dir)
	{
		if(dir==null)
			return getHomeDir(c);
		ArrayList<Bookmark> ar = new ArrayList<Bookmark>();
		File p = dir.getParentFile();
		if(p!=null)
			ar.add(new Bookmark(stat.STR_NULL, ".. ["+p.getName()+"]", dir.lastModified()).setImageRes(R.drawable.up));
		File files[] = null;
		Log.d("FileAdapter", "Try to read "+dir.getAbsolutePath());
		try{
			files = dir.listFiles();
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		if(files==null)
			return ar;
		FileUtils.sortFilesByName(files);
		for(File f:files)
		{
			if(f.isDirectory())
				ar.add(createDirBookmark(c, f,null));
			else	
				ar.add(new Bookmark(Formatter.formatFileSize(c, f.length()), f.getName(), f.lastModified()).setImageRes(R.drawable.file).setParam(f));
		}
		return ar;
	}

	public FileAdapter(Context c, File dir) {
		super(c, createBookmarksFromFiles(c, dir));
		mAutoLoadImages = false;
		mDir = dir;
	}
	@Override
	public void setArray(List<Bookmark> list) {
		super.setArray(list);
		onDirChanged(mDir);
	}
	ArrayList<Bookmark>mTempArray;
	@Override
	public boolean doAsync() throws Throwable {
		super.doAsync();
		mTempArray = createBookmarksFromFiles(getContext(), mDir);
		return true;
	}
	@Override
	public void doAsyncUiThread(boolean result) {
		super.doAsyncUiThread(result);
		setArray(mTempArray);
		mTempArray = null;
	}
	public void goToDir(File dir)
	{
		mDir = dir;
		startAsyncLoader();
	}
	public void saveFileList(File newFile)
	{
		boolean dir = newFile.isDirectory();
		String key = dir?LAST_DIRS:LAST_FILES;
		List<String> ar = Db.getStringTable().loadList(key);
		String abs = newFile.getAbsolutePath();
		for(int i=ar.size()-1;i>=0;i--)
		{
			String s = ar.get(i);
			if(s.equals(abs))
				ar.remove(i);
		}
		ar.add(abs);
		Db.getStringTable().saveList(key, ar, 3);
	}
	@Override
	public void onClick(View v) {
		Bookmark bm = (Bookmark)v.getTag();
		File f = (File) bm.param;
		if(f==null)
		{
			goUp();
		}
		else if(f.isDirectory())
		{
			goToDir(f);
		}
		else
		{
			onFileSelected(f);
		}
	}
	public void goHome()
	{
		mDir = null;
		setArray(getHomeDir(getContext()));
	}
	public boolean canGoUp()
	{
		return mDir!=null&&mDir.getParentFile()!=null;
	}
	public boolean goUp()
	{
		if(!canGoUp())
			return false;
		mDir = mDir.getParentFile();
		startAsyncLoader();
		return true;
	}
	
	@Override
	public void setBookmarkView(BookmarkView view, Bookmark bm) {
		super.setBookmarkView(view, bm);
		File f = (File)bm.param;
		FileInfo fi = DefaultImageLoaders.getFileInfoIfCanLoadImage(f);
		if(fi!=null)
			BrowserApp.loadFileImage(view.getThumbnailView(), fi);
		view.setType(BookmarkView.TYPE_SMALL);
		view.getThumbnailView().getImageView().setScaleType(ImageView.ScaleType.CENTER_CROP);
		TextView sh = view.getShortTextView();
		if(fi!=null||f==null||f.isDirectory())
			sh.setVisibility(View.GONE);
		else
		{
			String ext = FileUtils.getFileExt(f);
			sh.setText(ext);
			sh.setVisibility(View.VISIBLE);
		}
	}
	public File getCurDir()
	{
		return mDir;
	}
	public abstract void onFileSelected(File file);
	public abstract void onDirChanged(File parentDir);
	public void createNewFolder() {
		if(!canGoUp())
			return;
		new ThemedDialog(getContext()).setInput(getContext().getString(R.string.act_create_folder), null, new CustomDialog.OnUserInput() {
			@Override
			public void onUserInput(boolean ok, String newText) {
				if(!ok)
					return;
				File f = new File(mDir,newText);
				if(f.mkdirs())
					goToDir(f);
				else
					CustomPopup.toast(getContext(), R.string.dir_create_error);
			}
		}).show();
	}
}