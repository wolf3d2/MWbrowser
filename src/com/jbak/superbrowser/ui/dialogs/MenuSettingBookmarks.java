package com.jbak.superbrowser.ui.dialogs;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;

import com.jbak.superbrowser.Bookmark;
import com.jbak.superbrowser.adapters.SettingsAdapter;
import com.jbak.superbrowser.adapters.SettingsBookmark;
import com.jbak.superbrowser.recycleview.BookmarkViewRecyclerAdapter;
import com.jbak.superbrowser.recycleview.RecyclerViewEx;

public abstract class MenuSettingBookmarks extends ThemedDialog implements android.view.View.OnClickListener{

	ArrayList<Bookmark> mBookmarks;
	RecyclerViewEx mList;
	boolean mCloseMenuOnSelect = true;
	public MenuSettingBookmarks(Context context,String title,ArrayList<Bookmark> bookmarks) {
		this(context, title, bookmarks, false,true);
	}
	public MenuSettingBookmarks(Context context,String title,String ... values) {
		this(context, title, getBookmarksFromStrings(values), false,true);
	}
	public MenuSettingBookmarks(Context context,String title,Integer ... values) {
		this(context, title, getBookmarksFromInts(context,values), false,true);
	}
	public MenuSettingBookmarks(Context context,String title,ArrayList<Bookmark> bookmarks,boolean yesNoButtons,boolean closeMenuOnSelect) {
		super(context);
		mCloseMenuOnSelect = closeMenuOnSelect;
		mBookmarks = bookmarks;
		SettingsAdapter mAdapt = new SettingsAdapter(context,bookmarks);
		mList = new RecyclerViewEx(context,RecyclerViewEx.TYPE_VERTICAL_LIST);
		mList.setWrapContent(true);
		setView(mList);
		BookmarkViewRecyclerAdapter ad = new BookmarkViewRecyclerAdapter(mAdapt); 
		mList.setAdapter(ad);
		setTitleText(title);
		ad.setOnClickListener(this);
	}
	static ArrayList<Bookmark> getBookmarksFromStrings(String ...vals)
	{
		ArrayList<Bookmark> ar = new ArrayList<Bookmark>();
		for(String v:vals)
			ar.add(new SettingsBookmark(v, null));
		return ar;
	}
	static ArrayList<Bookmark> getBookmarksFromInts(Context c,Integer ...vals)
	{
		ArrayList<Bookmark> ar = new ArrayList<Bookmark>();
		for(Integer v:vals)
			ar.add(new SettingsBookmark(c,null,v,null));
		return ar;
	}
	public abstract void onBookmarkSelected(int pos,SettingsBookmark set);
	@Override
	public void onClick(View v) {
		if(mCloseMenuOnSelect)
			dismiss();
		if(v.getTag() instanceof SettingsBookmark)
		{
			SettingsBookmark sa = (SettingsBookmark)v.getTag();
			int pos = mBookmarks.indexOf(sa);
			onBookmarkSelected(pos,sa);
		}
		//super.onClick(v);
	}
}
