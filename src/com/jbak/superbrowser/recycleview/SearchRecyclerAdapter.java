package com.jbak.superbrowser.recycleview;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.database.Cursor;
import android.provider.Browser;
import android.view.ViewGroup;

import com.jbak.superbrowser.Action;
import com.jbak.superbrowser.BrowserApp;
import com.jbak.superbrowser.Db;
import com.jbak.superbrowser.IConst;
import com.jbak.superbrowser.stat;
import com.jbak.superbrowser.adapters.BookmarkAdapter;
import com.jbak.superbrowser.ui.PanelButton;
import com.jbak.superbrowser.ui.themes.MyTheme;
import com.jbak.utils.DbUtils.Select;

public class SearchRecyclerAdapter extends PanelButtonRecyclerAdapter {
	Cursor mCursor;
	int mSearchColumn=-1;
	WeakReference<Context> mContext;
	public SearchRecyclerAdapter(Context c) {
		super((BookmarkAdapter)null, PanelButton.TYPE_BUTTON_TEXT_ONLY);
		mContext = new WeakReference<Context>(c);
	}
	@Override
	public void onBindViewHolder(PanelButtonHolder vh, int pos) {
		mCursor.moveToPosition(pos);
		String search = mCursor.getString(mCursor.getColumnIndex(Browser.SearchColumns.SEARCH));
		PanelButton pb = (PanelButton)vh.itemView;
		Action a = new Action(Action.ITEM_TEXT, search, null, null);
		pb.setAction(a);
		MyTheme.get().setPanelButton(pb,pos,false);
	}
	@Override
	public int getItemCount() {
		return mCursor == null?0:mCursor.getCount();
	}
	public void refreshCursor()
	{
		Cursor old = mCursor;
		mCursor = createSearchCursor(getContext());
		if(old!=null&&!old.isClosed())
			old.close();
	}
	@Override
	public PanelButtonHolder onCreateViewHolder(ViewGroup vg, int pos) {
		return super.onCreateViewHolder(vg, pos);
	}
	public static int deleteSearchResult(Context c,String text)
	{
		int ret = -1;
		try{
			if(BrowserApp.DB_TYPE==BrowserApp.DB_OWN)
				ret = Db.getStringTable().select().where().eq(Browser.SearchColumns.SEARCH, text).deleteOpt(Db.getStringTable().getDb());
			else
				ret = c.getContentResolver().delete(Browser.SEARCHES_URI, Browser.SearchColumns.SEARCH+" = ?", new String[]{text});
			BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_SEARCH_CHANGED, text);
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		return ret;
	}
	public static Cursor createSearchCursor(Context context)
	{
		Cursor c;
		Select sel;
		if(BrowserApp.DB_TYPE==BrowserApp.DB_OWN)
			sel = Db.getSearchTable().select();
		else	
			sel = new Select(stat.STR_NULL);
		sel.orderBy(IConst.DATE, false);
		if(BrowserApp.DB_TYPE==BrowserApp.DB_OWN)
			c = sel.select(Db.getStringTable().getDb());
		else	
			c = sel.select(context.getContentResolver(), Browser.SEARCHES_URI);
		return c;
	}
	public final Context getContext()
	{
		if(mContext==null)
			return null;
		return mContext.get();
	}
}
