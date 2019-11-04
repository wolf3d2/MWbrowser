package ru.mail.webimage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import ru.mail.mailnews.st;

public class FileUtils
{
	public static int BUF_SIZE = 16384;
    /** Удаляет файл или папку. Папка удаляется рекурсивно
    *@param file Папка или файл для удаления
    *@return true - в случае успеха, false - в случае неудачи
     */
    public static boolean delete(File file)
    {
        if(!file.isDirectory())
        {
            return file.delete();
        }
        File[] children = file.listFiles();
        for (File f:children) 
        {
           if(f.isDirectory())
           {
               if(!delete(f))
                   return false;
           }
           else
           {
               if(!f.delete())
                   return false;
           }
        }
        file.delete();
        return true;
    }
    public static File copyOrMove(boolean copy,File src,File destDir)
    {
    	if(!src.exists())
    		return null;
    	if(!src.isDirectory())
    		return copyOrMoveFile(copy, src, destDir);
    	File files[] = src.listFiles();
    	for(File f:files)
    	{
    		if(f.isDirectory())
    		{
    			File dest = new File(destDir, f.getName());
    			dest.mkdirs();
    			if(copyOrMove(copy, f, dest)==null)
    				return null;
    			if(!copy)
    				f.delete();
    		}
    		else
    		{
    			if(copyOrMoveFile(copy, f, destDir)==null)
    				return null;
    		}
    	}
    	return destDir;
    }
    public static File copyOrMoveFile(boolean copy,File srcFile,File destDir)
    {
    	File newFile = null;
    	boolean destExist = destDir.exists();
    	if(destExist&&!destDir.isDirectory())
    		return newFile;
    	if(!destExist)
    		destDir.mkdirs();
    	if(!destDir.exists())
    		return newFile;
    	FileInputStream in=null;
    	FileOutputStream out=null;
    	try{
    		File f = new File(destDir,srcFile.getName());
    		byte chunk[] = new byte[BUF_SIZE];
    		in = new FileInputStream(srcFile);
    		out = new FileOutputStream(f);
    		int read = -1;
    		while ((read=in.read(chunk))>-1) {
    			out.write(chunk, 0, read);
			}
    		if(!copy)
    			srcFile.delete();
    		newFile = f;
    	}
    	catch(Throwable e)
    	{
    		e.printStackTrace();
    	}
    	try{
    		if(in!=null)
    			in.close();
    		if(out!=null)
    			out.close();
    	}
    	catch(Throwable ignor)
    	{
    		
    	}
    	return newFile;
    }
    /** Возвращает размер файлов в массиве ar*/    
    public static long getFilesSize(File[] files)
    {
        long sz = 0;
        if(files!=null)
        {
            for(File f:files)
            {
                if(!f.isDirectory())
                    sz+=f.length();
            }
        }
        return sz;
    }
    public static class FileProgressInputStream extends FileInputStream
    {
    	int mSize = 0;
    	long mPos = 0;
    	File mFile;
		public FileProgressInputStream(File file) throws FileNotFoundException {
			super(file);
			mFile = file;
		}
		@Override
		public int read(byte[] buffer, int byteOffset, int byteCount)throws IOException {
			int read = super.read(buffer, byteOffset, byteCount);
			mPos += read;
			return read;
		}
		public long getReadSize()
		{
			return mPos;
		}
		public long getTotalSize()
		{
			return mFile.length();
		}
		public float getReadPercent()
		{
			float total = getTotalSize();
			if(total==0)
				return 100f;
			float read = getReadSize();
			float p = read*100f/total;
			return p;
		}
    }
	public static void sortFilesByName(File files[])
	{
		Arrays.sort(files, new Comparator<File>() {

			@Override
			public int compare(File lhs, File rhs) {
				if(lhs.isDirectory()&&!rhs.isDirectory())
					return -1;
				if(!lhs.isDirectory()&&rhs.isDirectory())
					return 1;
				return lhs.getName().compareToIgnoreCase(rhs.getName());
			}
		});
	}
	public static String filenameFromUri(Uri uri,String mime)
	{
		List<String> path = uri.getPathSegments();
		if(path!=null&&path.size()>0) {
			String fn = path.get(path.size()-1);
			String ext = null;
			try {
				ext = st.STR_POINT+mime.substring(mime.indexOf("/")+1);
			} catch (Exception e) {
				ext = null;
			}
			if (!fn.contains(st.STR_POINT))
				if (ext!=null)
					fn += ext;
			return fn;
		}
		if(!TextUtils.isEmpty(mime))
		{
			 return getDateFileName(mime);
		}
		return null;
	}
	public static final String getFileExt(File f)
	{
		if(f==null)
			return null;
		String fn = f.getName();
		int pos = fn.lastIndexOf('.');
		if(pos<0||pos>=fn.length())
			return null;
		return fn.substring(pos+1);
	}
	@SuppressLint("SimpleDateFormat")
	public static String getDateFileName(String mime)
	{
		MimeTypeMap map = MimeTypeMap.getSingleton();
		String ext = map.getExtensionFromMimeType(mime);
//		DateFormat df = SimpleDateFormat.getDateTimeInstance();
//		return df.format(new Date(System.currentTimeMillis()))+'.'+ext;
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-hhmmss-S");
		return df.format(new Date(System.currentTimeMillis()))+'.'+ext;
	}
	public static File fileToCanonical(File f)
	{
        try {
			return f.getCanonicalFile();
		} catch (Throwable e1) {
			e1.printStackTrace();
		}
        return f;
	}
	public static String fileToCanonicalPath(File f)
	{
        try {
			return f.getCanonicalFile().toString();
		} catch (Throwable e1) {
			e1.printStackTrace();
		}
        return f.toString();
	}
	public static File setStorageToExternalDir(File f)
	{
		File canon = fileToCanonical(f);
		File stor = getFileStorageDir(canon);
		if(stor==null)
			return f;
		File ext = Environment.getExternalStorageDirectory();
		String abs = canon.getAbsolutePath();
		abs = abs.substring(stor.getAbsolutePath().length());
		abs = ext+abs;
		return new File(abs);
	}
	public static File getFileStorageDir(File f)
	{
		File stor[] = getStorages();
		if(stor==null)
			return null;
		File cannon = fileToCanonical(f);
		for(File s:stor)
		{
			if(cannon.getAbsolutePath().startsWith(s.getAbsolutePath()))
				return s;
		}
		return null;
	}
	public static File getFilePathFromMnt(File file)
	{
		File stor = getFileStorageDir(file);
		if(stor==null)
			return file;
		File mnt = new File("/mnt");
		File mntFiles[] = mnt.listFiles();
		File selMnt = null;
		for(File f:mntFiles)
		{
			try {
				if(f.getCanonicalFile().equals(stor))
					selMnt = f;
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		if(selMnt!=null)
		{
			String path = fileToCanonical(file).getAbsolutePath();
			path = path.substring(stor.getAbsolutePath().length());
			path = selMnt.getAbsolutePath()+path;
			return new File(path);
		}
		return file;
	}
	public static File[] getStorages()
	{
		File storages = new File("/storage");
		if(storages.exists())
		{
			File sdcards[] = storages.listFiles();
			return sdcards;
		}
		return null;
	}
}
