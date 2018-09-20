package com.jbak.superbrowser;

import java.util.ArrayList;

public class ActArray extends ArrayList<Action> {
	public ActArray() {
	}
	public ActArray(Integer...vals) {
		for(Integer v:vals)
			add(Action.create(v));
	}
	public ActArray(String commaSeparated) {
		String str[] = commaSeparated.split(",");
		for(String v:str)
		{
			add(Integer.decode(v));
		}
	}
	private static final long serialVersionUID = 1L;
	public final Action removeAction(int command)
	{
		for(int i=size()-1;i>=0;i--)
		{
			Action a = get(i);
			if(a.command==command)
			{
				return remove(i);
			}
		}
		return null;
	}
	public final boolean has(int command)
	{
		return getIndex(command)>-1;
	}
	public final int getIndex(int command)
	{
		for(int i=size()-1;i>=0;i--)
		{
			Action a = get(i);
			if(a.command==command)
				return i;
		}
		return -1;
	}
	public final int insertAction(Action action,int command,boolean before)
	{
		for(int i=size()-1;i>=0;i--)
		{
			Action a = get(i);
			if(a.command==command)
			{
				if(before)
				{
					add(i, action);
					return i;
				}
				else if(i<size()-1)
				{
					add(i+1,action);
					return i+1;
				}
			}
		}
		add(action);
		return size()-1;
	}
	public void add(int ... actions)
	{
		for(int act:actions)
			add(Action.create(act));
	}
	public boolean add(Bookmark parentDir) {
		if(parentDir==null)
			return false;
		this.add(Action.create(Action.ADD_BOOKMARK,parentDir));
		return true;
	}
	public boolean add(Bookmark parentDir,Bookmark curBookmark) {
		if(curBookmark==null)
			return false;
		this.add(Action.create(Action.ADD_BOOKMARK,null).setParam2(curBookmark));
		return true;
	}
	public String getCommaSeparated()
	{
		int sz = size();
		String str = stat.STR_NULL;
		for(int i=0;i<sz;i++)
		{
			str+=get(i).command;
			if(i<sz-1)
				str+=',';
		}
		return str;
	}
}
