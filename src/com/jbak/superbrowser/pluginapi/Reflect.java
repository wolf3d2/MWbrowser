package com.jbak.superbrowser.pluginapi;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

public class Reflect {
	public static Bundle getBundle(ArrayList<Object> params)
	{
		Bundle in = new Bundle();
		int len = params.size();
		for(int i=0;i<len;i+=2)
		{
			String name = (String)params.get(i);
			Object val = (Object)params.get(i+1);
			if(val instanceof Boolean)
				in.putBoolean(name, (Boolean)val);
			else if(val instanceof String)
				in.putString(name, (String)val);
			else if(val instanceof Integer)
				in.putInt(name, (Integer)val);
			else if(val instanceof Long)
				in.putLong(name, (Long)val);
			else if(val instanceof Parcelable)
				in.putParcelable(name, (Parcelable)val);
			else
				throw new IllegalArgumentException("Bad param: "+name);
		}
		return in;
	}
	public static Bundle getObjectBundle(Object obj)
	{
		return getBundle(getObjectParams(obj));
	}
	public static String getName(Class<?>clazz)
	{
		return clazz.getSimpleName();
	}
	public static String getObjectName(Object obj)
	{
		return getName(obj.getClass());
	}
	public static <T> T fillClass(Bundle b,T obj)
	{
		try
		{
			Field[] fields = obj.getClass().getFields();
			for(Field f:fields)
			{
				Object val = b.get(f.getName());
				if(val!=null)
				{
					f.setAccessible(true);
					f.set(obj, val);
				}
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		return obj;
	}
	public static <T> T fillClass(Intent in,T obj)
	{
		try
		{
			Bundle b = in.getBundleExtra(getObjectName(obj));
			return fillClass(b, obj);
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		return obj;
	}
	public static void setIntentParam(Intent in,String extraName, Bundle b)
	{
		in.putExtra(extraName, b);
	}
	public static void setIntentParams(Intent in,String extraName, Object ... params)
	{
		in.putExtra(extraName, params);
	}
	public static ArrayList<Object> getObjectParams(Object obj)
	{
		try{
			Field[] fields = obj.getClass().getDeclaredFields();
			ArrayList<Object> params = new ArrayList<Object>();
			for(Field f:fields)
			{
				Object v = f.get(obj);
				if(v==null)
					continue;
				params.add(f.getName());
				params.add(f.get(obj));
			}
			return params;
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		return null;
	}

}
