package com.jbak.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.os.Environment;

public class FileLogger {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================
	// private String path;
	private BufferedOutputStream out;
	File mFile;
	// ===========================================================
	// Constructors
	// ===========================================================

	public FileLogger(String path,boolean createNew) {
		mFile = new File(path);
		if(createNew)
			mFile.delete();
		try {
			out = new BufferedOutputStream(new FileOutputStream(mFile, !createNew), 8192);
		} catch (Throwable e) {
			Utils.log(e);
		}
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	public static File getCleanLogFile(String dirOnSDCard, String filename) {
		if (dirOnSDCard == null) {
			throw new NullPointerException("dirOnSDCard is required!");
		}

		File dir = new File(Environment.getExternalStorageDirectory(), dirOnSDCard);

		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				return null;
			}
		}

		File log = new File(dir, filename);

		if (log.exists()) {
			log.delete();
		}

		return log;
	}

	public boolean write(String text) {
		if (out == null)
			return false;
		try {
			out.write(text.getBytes());
			out.write('\n');
			out.flush();
			return true;
		} catch (Throwable e) {
			Utils.log("FILELOGGER", e);
		}
		return false;
	}

	public void close() {
		if (out != null) {
			try {
				out.flush();
				out.close();
			} catch (Throwable e) {
				Utils.log("FILELOGGER", e);
			}
			out = null;
		}
	}
	public String getLastBytes(long size)
	{
		if(mFile==null||!mFile.exists())
			return null;
		FileInputStream fis = null;
		String ret = null;
		try{
			fis = new FileInputStream(mFile);
			long pos = mFile.length()-size;
			long read = size;
			if(pos<=0)
			{
				read = mFile.length();
				pos = 0;
			}
			if(pos>0)
				fis.skip(pos);
			byte[] buf = new byte[(int) read];
			fis.read(buf);
			int cr = -1;
			for(int i=0;i<buf.length;i++)
			{
				if(buf[i]=='\n'&&i<buf.length-1)
				{
					cr = i;
					break;
				}
			}
			if(cr<0)
				ret =new String(buf);
			else
				ret =new String(buf, cr, buf.length-cr);
		}
		catch(Throwable e)
		{
			Utils.log(e);
		}
		finally
		{
			if(fis!=null)
			{
				try{
				fis.close();
				}
				catch(Throwable e){}
			}
		}
		return ret;
			
	}
}
