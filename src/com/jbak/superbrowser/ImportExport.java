package com.jbak.superbrowser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;
import java.util.Vector;

import com.jbak.superbrowser.adapters.BookmarkFolderAdapter;
import com.jbak.superbrowser.stat.DownloadOptions;
import com.jbak.superbrowser.ui.OnAction;
import com.jbak.superbrowser.ui.dialogs.ThemedDialog;
import com.jbak.ui.ConfirmOper;
import com.jbak.ui.CustomDialog;
import com.jbak.ui.CustomPopup;
import com.jbak.utils.DbUtils.StrConst;
import com.mw.superbrowser.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.provider.Browser;
import ru.mail.mailnews.st;

public class ImportExport 
{
	int good = 0;
	// версия сохранённых закладок
	int vers = 0;
	ArrayList<Record> arrec = new ArrayList<Record>();
	public static ImportExport inst = null;
	public static Context m_c= null;
	public static String FILENAME_SETTING = "MWbrowser.setting";
	public static String FILENAME_BOOKMARK = "MWbrowser.bookmark";
	public static String BOOKMARK = "bookmark";
	public static String END_BOOKMARK = "END_BOOKMARK";
	// версия файла закладок
	public static String VERSION = "VERSION";
	public static String VERSION_NUM_BOOKMARK = "1";
	public static String VERSION_NUM_SETTING = "1";
	String folder = null;
	// количество закладок
	int count_bm = 0;
	
	boolean imp_bm = false;
	boolean imp_set = false;
	
	public static int TYPE_MWBROWSER = 0;

	public ImportExport(Context c)
	{
		m_c=c;
	}
	
	public void dismiss()
	{
		inst = null;
	}
	public void export(String destFolder, boolean toast)
	{
		inst = this;
		if (destFolder == null)
			folder = Prefs.getString(Prefs.BACKUP_SETTING_FOLDER, Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
		else
			folder = destFolder;
		if (exportBookmark(toast))
			if (exportSetting())
				return;
		if (!toast)
			st.toast("Ошибка сохранения");
		dismiss();
	}
	
	public boolean exportBookmark(boolean toast)
	{
		if (folder==null)
			return false;
        //Cursor c = Db.getBookmarksTable().getBookmarkAllCursor(new String[] {"*"});
        Cursor c = Db.getBookmarksTable().getBookmarkAllCursor(new String[] {"*"});
        if (c==null)
        	return false;
		long parent = 0;
		long type = 0;
		byte[] blob = null;
		boolean fl = false;
		count_bm = 0;
		try {
			FileWriter fw = new FileWriter(folder+File.separatorChar+FILENAME_BOOKMARK, false);
			String out = null;
	        if (c.moveToFirst()) {
		        out = stat.STR_COMMENT+stat.STR_SPACE+CustomDialog.getAppName(m_c)
	        		+stat.STR_SPACE+BOOKMARK+stat.STR_SPACE+stat.STR_CR;
		        out+=VERSION+"="+VERSION_NUM_BOOKMARK+stat.STR_CR+stat.STR_CR;
	        	fw.write(out);
	        	fl=true;
	        }
	        do {
	        	if (!fl&toast) {
	        		st.toast(R.string.act_import_not_bm);
	        		return false;
	        	}
	        	type = c.getLong(3);
	        	parent = c.getLong(5);
	        	if (type!=0&parent!=0) {
			        out = BOOKMARK.toUpperCase()+stat.STR_CR;
			        out+=c.getColumnName(0)+"="+c.getLong(0)+stat.STR_CR;
			        out+=c.getColumnName(1)+"="+c.getString(1)+stat.STR_CR;
			        out+=c.getColumnName(2)+"="+c.getString(2)+stat.STR_CR;
			        out+=c.getColumnName(3)+"="+type+stat.STR_CR;
			        out+=c.getColumnName(4)+"="+c.getLong(4)+stat.STR_CR;
			        out+=c.getColumnName(5)+"="+parent+stat.STR_CR;
			        out+=END_BOOKMARK+stat.STR_CR+stat.STR_CR;
			        fw.write(out);
		        	count_bm++;
	        	}
	        	} while (c.moveToNext());
        	c.close();
        	
	        fw.flush();
	        fw.close();
        	if (toast) 
        		st.toast(m_c.getString(R.string.act_export_bm_complete)+stat.STR_SPACE+(count_bm-1));
			return true;
        }catch (Throwable e) 
		{
//			e.printStackTrace();
        	return false;
        }
	}
	public boolean exportSetting()
	{
		if (folder==null)
			return false;
		String pth = m_c.getFilesDir().getParent()+"/shared_prefs/"+Prefs.NAME_SETTING+".xml";
        File fin = new File(pth);
        File fout = new File(folder+File.separatorChar+FILENAME_SETTING);
        FileInputStream in;
        FileOutputStream out;
        try {
        	in = new FileInputStream(fin);
			out = new FileOutputStream(fout);
            byte b[] = new byte[in.available()];
            in.read(b);
            out.write(b);
            out.flush();
            in.close();
            out.close();
			
		} catch (Throwable e) {
			return false;
		}
		return true;
	}
	public boolean importSetting()
	{
		if (folder==null)
			return false;
        File fout = new File(m_c.getFilesDir().getParent()+"/shared_prefs/"+Prefs.NAME_SETTING+".xml");
        File fin = new File(folder+File.separatorChar+FILENAME_SETTING);
        FileInputStream in;
        FileOutputStream out;
        try {
        	in = new FileInputStream(fin);
			out = new FileOutputStream(fout);
            byte b[] = new byte[in.available()];
            in.read(b);
            out.write(b);
            out.flush();
            in.close();
            out.close();
			
		} catch (Throwable e) {
			return false;
		}
		return true;
	}
	/** Основная функция импорта в MWbrowser 
	 * просто нельзя использовать слово import */
	public void importGlobal(String importFolder,boolean bimp_bm, boolean bimp_set, boolean toast)
	{
		imp_bm = bimp_bm;
		imp_set = bimp_set;
		folder = importFolder;
		good = 0;
		if (!imp_bm&!imp_set)
			good = -1;
		else {
			if (imp_bm)
				good++;
			if (imp_set)
				good++;
		}
		if (imp_bm&!imp_set) {
			if (importMWbrowserBookmark()) {
				good--;
			}
		}
		else if (imp_bm&imp_set) {
			if (importMWbrowserBookmark())
				good--;
				good--;
		}
		else if (!imp_bm&imp_set) {
			if (importSetting()) {
				good--;
				importSettingRestart();
			}
		}
		if (toast) {
			switch (good)
			{
			case -1:
				st.toast("Ничего не выбрано");
				break;
			case 1:
				st.toast("Не всё импортировано");
				break;
			case 2:
				st.toast("Ошибка импорта");
				break;
			}
//			if (good == -1)
//				st.toast("Ничего не выбрано");
//			else if (good == 1)
//				st.toast("Не всё импортировано");
//			else if (good == 2)
//				st.toast("Ошибка импорта");
		}
		dismiss();
	}
	public boolean importMWbrowserBookmark()
	{
		if (folder==null)
			return false;
// импорт закладок		
		if (imp_bm) {
			String par = stat.STR_NULL;
			String val = stat.STR_NULL;
			String parr = stat.STR_NULL;
			int indcom = -1;
			FileReader fr;
			int ind = -1;
			Record rec = null;
			
			String path = folder+File.separatorChar+FILENAME_BOOKMARK;
			File fff = new File(path);
			if (!fff.exists()&!fff.isFile())
				return false;
			try {
				fr = new FileReader(path);
				Scanner sc = new Scanner(fr);
				sc.useLocale(Locale.US);
				if (arrec.size()>0)
					arrec.clear();
				while (sc.hasNext()) {
					if (sc.hasNextLine()) {
						parr = sc.nextLine();
					}
					// если строкам начинается с //, то это коммент
					if (parr.startsWith(stat.STR_COMMENT))
						continue;
					parr = parr.trim();
					if (parr.length() > 0) {
						if (BOOKMARK.toUpperCase().compareTo(parr)==0) {
							rec = new Record();
							continue;
						}
						else if (END_BOOKMARK.compareTo(parr)==0) {
							arrec.add(rec);
							continue;
						}
						else if (VERSION.compareTo(parr)==0) {
							try {
								vers=Integer.parseInt(val);
							} catch (NumberFormatException e){
								vers = 0;
							}
						}
						
						ind = parr.indexOf("=");
						if (ind>-1){
							par = parr.substring(0, ind);
							val = parr.substring(ind + 1);
							if (par.compareTo(IConst.URL)==0) {
								rec.url=val;
							}
							else if (par.compareTo(IConst.TITLE)==0) {
								rec.title=val;
							}
							else if (par.compareTo(IConst._ID)==0) {
								try {
									rec.id=Long.parseLong(val);
								} catch (NumberFormatException e){
									return false;
								}
							}
							else if (par.compareTo(IConst.TYPE)==0) {
								try {
									rec.type=Long.parseLong(val);
								} catch (NumberFormatException e){
									return false;
								}
							}
							else if (par.compareTo(IConst.DATE)==0) {
								try {
									rec.data=Long.parseLong(val);
								} catch (NumberFormatException e){
									return false;
								}
							}
							else if (par.compareTo(IConst.PARENT)==0) {
								try {
									rec.oldparent=Long.parseLong(val);
								} catch (NumberFormatException e){
									return false;
								}
							}
						}
					}
				}
			} catch (FileNotFoundException e) {
				return false;
			}
        	new ThemedDialog(m_c).setConfirm(m_c.getString(R.string.act_import_delete_bookmark), null, new ConfirmOper() {
				
				@Override
				public void onConfirm(Object userParam) {
					Db.getBookmarksTable().deleteAllBookmark();
					BitmapDrawable bd = (BitmapDrawable) m_c.getResources().getDrawable(R.drawable.not_miniature);
					Bitmap thumbnail = bd.getBitmap();
					createBookmarFolder();
					Bookmark bm=null;
					for (Record rc:arrec) {
						if(rc.type!=2) {
							bm = new Bookmark(rc.url, rc.title, rc.data);
							bm.setImageRes(R.drawable.not_miniature);
							if (rc.oldparent == 1)
								Db.getBookmarksTable().insertBookmark(false, bm, null, thumbnail, 1);
							else
								Db.getBookmarksTable().insertBookmark(false, bm, null, thumbnail, getNewParent(rc.oldparent));

						}
					}
		        	new ThemedDialog(m_c).setConfirmOk(m_c.getString(R.string.act_import_cb_bm),m_c.getString(R.string.act_import_complete)+stat.STR_SPACE+arrec.size(), null, new ConfirmOper() {
						
						@Override
						public void onConfirm(Object userParam) {
							if (imp_set) {
								if (importSetting()) {
									importSettingRestart();
								} else
									st.toast("Ошибка импорта настроек");
							}
						}
					});

//					st.dialogHelp(m_c , m_c.getString(R.string.act_import_complete)+stat.STR_SPACE+arrec.size(), m_c.getString(R.string.act_import));
					

				}
			});

		}
// импорт настроек		
		if (imp_set) {
			
		}
		return true;
	}
	public void createBookmarFolder() {
		Vector<Long> ar = new Vector<Long>();
		long lv = 0;
		
		for (Record rc:arrec) {
			if (rc.type == 2&rc.oldparent>lv) {
				lv= rc.oldparent;
				ar.add(lv);
			}
		}
		long[] arr = new long[ar.size()];
		for (int i=0; i<ar.size();i++) {
			arr[i] = ar.get(i);
		}
		Arrays.sort(arr);
		createBookmarkFolder(1,arr,0);
	}
	/** создаём папки в закладках с самого мелкого_id (lev),
	 * до самого большого.
	 * Для ускорения номера уровней lev перечислены в arlev */
	public void createBookmarkFolder(int lev, long[] arlev,int levpos) {
		for (Record rc:arrec) {
			if (rc.type == 2&rc.oldparent == lev) {
				if (lev == 1) {
					rc.newparent =(Long) stat.createBookmarkFolder(m_c, rc.title, 1).param;
				} else {
					rc.newparent =(Long) stat.createBookmarkFolder(m_c, rc.title, getNewParent(rc.oldparent)).param;
				}
			}
		}
		levpos++;
		if (levpos <arlev.length)
			createBookmarkFolder((int)arlev[levpos],arlev,levpos);
		
		
	}
	public long getNewParent(long oldParent) {
		for (Record rc:arrec) {
			if (rc.type==2)
				if (rc.id == oldParent)
					return rc.newparent;
		}
		return 1;
	}
	public class Record
    {
		long id = 0;
		String url = stat.STR_NULL;
		String title = stat.STR_NULL;
		long type = 0;
		long data = 0;
		long oldparent = 0;
		long newparent = 0;
		
        public Record()
        {
        	id = 0;
    		url = stat.STR_NULL;
    		title = stat.STR_NULL;
    		type = 0;
    		data = 0;
    		oldparent = 0;
    		newparent = 0;
        }
    }
	public void importSettingRestart() {
    	new ThemedDialog(m_c).setConfirmOk(m_c.getString(R.string.act_import_cb_set),m_c.getString(R.string.act_import_set_complete), null, new ConfirmOper() {
			
			@Override
			public void onConfirm(Object userParam) {
				System.exit(0);
			}
		});
		
	}

}