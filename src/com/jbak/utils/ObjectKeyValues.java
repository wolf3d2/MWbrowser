package com.jbak.utils;

import java.lang.reflect.Array;


@SuppressWarnings("unchecked")
public class ObjectKeyValues <T,K> {
	Object mValues[];
	public ObjectKeyValues(Object ... values)
	{
		mValues = values;
	}
	public final int size()
	{
		return mValues.length/2;
	}
	public final K getValueByKey(T val)
	{
		return getValueByKey(val, null);
	}
	public final K getValueByKey(T val,K fallback)
	{
		for(int i=mValues.length-2;i>=0;i-=2)
		{
			if(mValues[i].equals(val))
			{
				return (K)mValues[i+1];
			}
		}
		return fallback;
	}
	public final int getIndexByKey(T key)
	{
		T[] keys = getKeys();
		int pos = 0;
		for(T k:keys)
		{
			if(k!=null&&k.equals(key))
				return pos;
			++pos;
		}
		return -1;
	}
	public final T getKeyByIndex(int index)
	{
		return (T) mValues[index*2];
	}
	public final K getValueByIndex(int index)
	{
		return (K) mValues[index*2+1];
	}
	public final K[] getValues()
	{
		int sz = mValues.length/2;
		K[] ret = null;
		for(int i=1;i<mValues.length;i+=2)
		{
			K val = (K) mValues[i];
			if(ret==null)
				ret = (K[]) Array.newInstance(val.getClass(), sz);
			ret[i/2]=val;
		}
		return ret;
	}
	public final T[] getKeys()
	{
		int sz = mValues.length/2;
		T[] ret = null;
		for(int i=0;i<mValues.length;i+=2)
		{
			T val = (T) mValues[i];
			if(ret==null)
				ret = (T[]) Array.newInstance(val.getClass(), sz);
			ret[i/2]=val;
		}
		return ret;
	}
	public final T getKeyByValue(K val,T fallback)
	{
		for(int i=mValues.length-1;i>=0;i-=2)
		{
			if(mValues[i].equals(val))
			{
				return (T)mValues[i-1];
			}
		}
		return fallback;
		
	}
	public final T getKeyByValue(K val)
	{
		return getKeyByValue(val, null);
	}
}
