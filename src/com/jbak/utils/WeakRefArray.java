package com.jbak.utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
/** Массив ссылок на объекты 
 */
public class WeakRefArray<T> {
	ArrayList<WeakReference<T>> mRefs = new ArrayList<WeakReference<T>>();
	boolean mUnique = true;
	public void add(T item)
	{
		if(mUnique)
			remove(item);
		mRefs.add(new WeakReference<T>(item));
	}
	public void addWeakRef(WeakReference<T> weakRef)
	{
		if(mUnique&&mRefs.indexOf(weakRef)>-1)
			return;
		mRefs.add(weakRef);
	}
	public WeakReference<T> remove(T item)
	{
		for(int i=mRefs.size()-1;i>=0;i--)
		{
			WeakReference<T> ref = mRefs.get(i);
			if(ref==null)
				continue;
			if(item.equals(ref.get()))
			{
				mRefs.remove(i);
				return ref;
			}
		}
		return null;
	}
	public WeakReference<T> remove(int index)
	{
		return mRefs.remove(index);
	}
	/** Возвращает массив живых объектов, удаляет из массива все мертвые */
	public List<T> getLiveRefs()
	{
		ArrayList<T> ar = new ArrayList<T>();
		for(int i=mRefs.size()-1;i>=0;i--)
		{
			T it = mRefs.get(i).get();
			if(it==null)
				mRefs.remove(i);
			else ar.add(0,it);
		}
		return ar;
	}
	public WeakReference<T> getByObj(T obj)
	{
		for(int i=mRefs.size()-1;i>=0;i--)
		{
			WeakReference<T> ref = mRefs.get(i);
			if(ref!=null&&ref.get()==obj)
				return ref;
		}
		return null;
	}
	public void clear()
	{
		mRefs.clear();
	}
	public int size()
	{
		return mRefs.size();
	}
	public T get(int pos)
	{
		return mRefs.get(pos).get();
	}
}
