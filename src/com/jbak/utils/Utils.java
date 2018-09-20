package com.jbak.utils;

import java.lang.reflect.Field;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

public class Utils {
	public static boolean DEBUG_FILE = false;
	public static boolean DEBUG_LOGCAT = false;
	private static FileLogger mLogger;
	private static final String TAG_TRACE = "trace";
	private static final int MAX_STACK_STRING = 8192;
	private static final String CAUSED_BY = "caused by";
    public static final void logStack(String tag,String msg)
    {
    	if(!DEBUG_FILE)
    		return;
    	Throwable e = new Exception();
    	StackTraceElement st[] = e.getStackTrace();
    	log(tag, "- "+msg);
    	String start = "| ";
    	for(StackTraceElement s:st)
    		log(tag, start+s.toString());
    	log(tag, "-");
    }
    public static final void log(String tag,String msg)
    {
    	Log.d(tag, msg);
    	if(!DEBUG_FILE)
    		return;
    	if(mLogger==null)
    	{
    		String path = Environment.getExternalStorageDirectory()+"/"+"JbakBrowserLog.txt";
    		mLogger = new FileLogger(path,false);
    	}
    	mLogger.write(tag+": "+msg);
    }
    public static final void log(Throwable err)
    {
    	log(TAG_TRACE,err);
    }
    public static final void log(String tag,Throwable err)
    {
		logStack(tag, null, err);
    }
    public static final String getStackString(Throwable e)
    {
    	if(e==null)
    		e = new Exception();
    	StringBuffer msg = new StringBuffer(e.getClass().getName());
    		if(!TextUtils.isEmpty(e.getMessage()))
    			msg.append(' ').append(e.getMessage());
    	msg.append('\n');
    	StackTraceElement st[] = e.getStackTrace();
    	for(StackTraceElement s:st)
    		msg.append(s.toString()).append('\n');
    	Throwable cause = e.getCause();
    	if(cause!=null&&msg.length()<MAX_STACK_STRING)
    		msg.append('\n').append(CAUSED_BY).append('\n').append(getStackString(cause));
    	String ret = msg.toString();
    	return ret;
    }
    public static final void logStack(String tag,String msg,Throwable e)
    {
    	if(e==null)
    		e = new Exception();
    	if(msg==null)
    	{
    		msg = e.getClass().getName();
    		if(!TextUtils.isEmpty(e.getMessage()))
    			msg+=' '+e.getMessage();
    	}
    	StackTraceElement st[] = e.getStackTrace();
    	log(tag, "- "+msg);
    	String start = "| ";
    	for(StackTraceElement s:st)
    		log(tag, start+s.toString());
    	log(tag, "-");
    }
    public static void closeFileLogger()
    {
    	if(mLogger!=null)
    	{
    		mLogger.close();
    		mLogger = null;
    	}
    }
    public static String getLastLogcat()
    {
    	return mLogger.getLastBytes(16000);
    }
    public static Field refGetField(Class<?>clazz,String name)
    {
    	for(Field f:clazz.getDeclaredFields())
    	{
    		if(name.equals(f.getName()))
    			return f;
    	}
    	for(Field f:clazz.getFields())
    	{
    		if(name.equals(f.getName()))
    			return f;
    	}
    	return null;
    }
    public static final boolean isStringsEquals(String s1,String s2)
    {
    	if(s1==null)
    		return s2==null;
    	if(s2==null)
    		return s1==null;
    	return s1.equals(s2);
    }
}
