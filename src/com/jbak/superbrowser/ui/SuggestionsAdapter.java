package com.jbak.superbrowser.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;

import ru.mail.mailnews.st;
import ru.mail.webimage.WebDownload;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.provider.Browser;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.jbak.superbrowser.BrowserApp;
import com.jbak.superbrowser.Db;
import com.jbak.superbrowser.IConst;
import com.jbak.superbrowser.NetworkChecker;
import com.jbak.superbrowser.stat;
import com.mw.superbrowser.R;
import com.jbak.superbrowser.search.SearchItem;
import com.jbak.superbrowser.ui.themes.MyTheme;
import com.jbak.ui.UIUtils;
import com.jbak.utils.DbUtils.Select;
import com.jbak.utils.Utils;

@SuppressLint("DefaultLocale")
public class SuggestionsAdapter extends ArrayAdapter<SearchItem> implements Filterable,IConst{
	static final int MAX_SIZE = 15;
	private ArrayList<SearchItem> resultList = new ArrayList<SearchItem>();
	String mLastSearch;
	boolean mStackFromBottom = true;
	WeakReference<AbsListView>mListViewRef;
	public SuggestionsAdapter(Context context, int resource) {
		super(context, resource);
	}
	
	@Override
	public int getCount() {
		return resultList.size();
	}
	@Override
	public SearchItem getItem(int position) {
		if(position>=resultList.size()||position<0)
			return new SearchItem();
		return resultList.get(position);
	}
	Runnable mMoveBottom = new Runnable() {
		
		@Override
		public void run() {
			if(mListViewRef!=null&&mListViewRef.get()!=null)
				mListViewRef.get().smoothScrollToPosition(getCount()-1);
		}
	};
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		if(mStackFromBottom&&getCount()>0&&mListViewRef!=null&&mListViewRef.get()!=null)
		{
			mListViewRef.get().post(mMoveBottom);
		}
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(parent  instanceof AbsListView)
		{
			AbsListView lv = (AbsListView)parent;
			if(mListViewRef==null||mListViewRef.get()==null||mListViewRef.get()!=lv)
			{
				mListViewRef = new WeakReference<AbsListView>(lv);
				((AbsListView)parent).setStackFromBottom(mStackFromBottom);
			}
		}
		if(convertView==null)
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.autocomplete_url_item, null);
		//UIUtils.setBackColor(convertView, position, false);
		TextView tv = (TextView) convertView.findViewById(R.id.title);
		MyTheme.get().setViews(MyTheme.ITEM_PANEL_BUTTON_POS, position,null,convertView);
		MyTheme.get().setViews(MyTheme.ITEM_DIALOG_TEXT, tv);
		ImageView iv = (ImageView) convertView.findViewById(R.id.searchType);
		SearchItem si = getItem(position);
		tv.setText(si.text);
		switch(si.type)
		{
			case SearchItem.SEARCH_TYPE_WEB:
				iv.setImageResource(R.drawable.search);
				break;
			case SearchItem.SEARCH_TYPE_BOOKMARK:
				iv.setImageResource(R.drawable.bookmarks);
				break;
			case SearchItem.SEARCH_TYPE_RELATED_SEARCHES:
				iv.setImageResource(R.drawable.searchpage);
				break;
		}
		UIUtils.setViewsTag(si, convertView);
		return convertView;
	}
	public static final String BOOKMARKS_PROJECTION[] = new String[]{Browser.BookmarkColumns.TITLE,Browser.BookmarkColumns.URL};
	ArrayList<SearchItem> searchLocalForBookmarks(String res,int limit) throws Throwable
	{
		ArrayList<SearchItem> ret = new ArrayList<SearchItem>();
		String where = "bookmark = 1 ";
		String whereArgs[] = null;
		if(!TextUtils.isEmpty(res))
		{
			res = res.toLowerCase();
			String s = '%'+res;
			String sup = Character.toUpperCase(res.charAt(0))+res.substring(1)+'%';
			whereArgs = new String[]{s,sup};
			where+=" and (title like ? or title like ?)";
		}
		Cursor c;
		if(BrowserApp.DB_TYPE==BrowserApp.DB_OWN)
			c = Db.getBookmarksTable().getBookmarkCursor(BOOKMARKS_PROJECTION, where, whereArgs);
		else
			c = getContext().getContentResolver().query(Browser.BOOKMARKS_URI, BOOKMARKS_PROJECTION, where, whereArgs, null);
		if(c!=null)
		{
			if(c.moveToFirst())
			{
				int indexTitle = c.getColumnIndex(Browser.BookmarkColumns.TITLE);
				int indexUrl = c.getColumnIndex(Browser.BookmarkColumns.URL);
				do
				{
					SearchItem si = new SearchItem(c.getString(indexTitle),SearchItem.SEARCH_TYPE_BOOKMARK);
					si.url = c.getString(indexUrl);
					ret.add(si);
					if(ret.size()>=limit)
						break;
				}
				while(c.moveToNext());
			}
			c.close();
		}
		return ret;
	}
	ArrayList<SearchItem> searchLocalForRelatedSearch(String res,int limit) throws Throwable
	{
		res = res.toLowerCase(); 
		ArrayList<SearchItem> ret = new ArrayList<SearchItem>();
		Cursor c;
		Select sel;
		if(BrowserApp.DB_TYPE==BrowserApp.DB_OWN)
			sel = Db.getSearchTable().select();
		else	
			sel = new Select(st.STR_NULL);
			if(!TextUtils.isEmpty(res))
				sel.where().like(Browser.SearchColumns.SEARCH, res+"%");
			sel.orderBy(DATE, false);
			if(BrowserApp.DB_TYPE==BrowserApp.DB_OWN)
				c = sel.select(Db.getStringTable().getDb());
			else	
				c = sel.select(getContext().getContentResolver(), Browser.SEARCHES_URI);
		if(c!=null)
		{
			if(c.moveToFirst())
			{
				int index = c.getColumnIndex(Browser.SearchColumns.SEARCH);
				do
				{
					ret.add(new SearchItem(c.getString(index), SearchItem.SEARCH_TYPE_RELATED_SEARCHES));
					if(ret.size()>=limit)
						break;
				}
				while(c.moveToNext());
			}
			c.close();
		}
		return ret;
	}
	ArrayList<SearchItem> searchGoogleForSuggestions(String res) throws Throwable
	{
		String url = "http://google.com/complete/search?client=chrome&q="+WebDownload.enc(res);
		WebDownload wd = new WebDownload();
		wd.startGetHttpConnection(url);
		String result = WebDownload.streamToString(wd.input);
		JSONArray ar = new JSONArray(result);
		JSONArray arComp = ar.getJSONArray(1);
		ArrayList<SearchItem> ret = new ArrayList<SearchItem>();
		int sz = arComp.length();
		if(sz>MAX_SIZE)
			sz = MAX_SIZE;
		for(int i=0;i<sz;i++)
			ret.add(new SearchItem(arComp.getString(i),SearchItem.SEARCH_TYPE_WEB));
		return ret;
	}
	@Override
	public Filter getFilter() {
		return new Filter() {
			
			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence s, FilterResults res) {
				resultList = (ArrayList<SearchItem>) res.values;
				if(res!=null&&res.count>0)
					notifyDataSetChanged();
				else
					notifyDataSetInvalidated();
			}
			@Override
			protected FilterResults performFiltering(CharSequence s) {
				FilterResults filterResults = new FilterResults();
				ArrayList<SearchItem> results = null;
				String search = s==null?st.STR_NULL:s.toString();
				if(resultList!=null&&mLastSearch!=null&&mLastSearch.equals(search))
				{
					filterResults.values = resultList;
					filterResults.count = resultList.size();
					return filterResults;
				}
				mLastSearch = search;
				try{
					if(search.length()>0)
					{
						ArrayList<SearchItem> ar = searchLocalForBookmarks(search,4);//searchGoogleForRelatedSearch(search);
						ArrayList<SearchItem> arSearch = searchLocalForRelatedSearch(search,4);//searchGoogleForRelatedSearch(search);
						ArrayList<SearchItem> arWeb = null;
						if(NetworkChecker.inetAvaliable)
							arWeb = searchGoogleForSuggestions(search);
						results = new ArrayList<SearchItem>();
						if(ar!=null&&ar.size()>0)
							results.addAll(ar);
						if(arSearch!=null&&arSearch.size()>0)
							results.addAll(arSearch);
						if(arWeb!=null&&arWeb.size()>0)
							results.addAll(arWeb);
					}
					else
					{
						results = searchLocalForRelatedSearch(search,100);
					}
					if(mStackFromBottom)
						Collections.reverse(results);
				}
				catch(Throwable e)
				{
					Utils.log(e);
				}
				if(results==null)
					results = new ArrayList<SearchItem>();
				filterResults.values = results;
				filterResults.count = results.size();
				return filterResults;
			}
		};
	}
	public void setStackFromBottom(boolean stackFromBottom)
	{
		mStackFromBottom = stackFromBottom;
		if(mListViewRef!=null&&mListViewRef.get()!=null)
			mListViewRef.get().setStackFromBottom(stackFromBottom);
	}
}
