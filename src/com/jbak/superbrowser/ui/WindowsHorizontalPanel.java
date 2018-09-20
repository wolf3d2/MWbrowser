package com.jbak.superbrowser.ui;


import android.content.Context;
import android.content.res.Configuration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.jbak.superbrowser.BrowserApp;
import com.jbak.superbrowser.BrowserApp.OnGlobalEventListener;
import com.jbak.superbrowser.IConst;
import com.jbak.superbrowser.MainActivity;
import com.jbak.superbrowser.OnBitmapLoadListener;
import com.jbak.superbrowser.Prefs;
import com.jbak.superbrowser.WebViewEvent;
import com.jbak.superbrowser.Tab;
import com.jbak.superbrowser.TabList;
import com.jbak.superbrowser.WindowsAdapter;
import com.jbak.superbrowser.adapters.BookmarkAdapter;
import com.jbak.superbrowser.recycleview.PanelButtonHolder;
import com.jbak.superbrowser.recycleview.PanelButtonRecyclerAdapter;
import com.jbak.superbrowser.ui.themes.MyTheme;
import com.jbak.ui.UIUtils;

public class WindowsHorizontalPanel extends HorizontalPanel implements OnGlobalEventListener,OnBitmapLoadListener,OnLongClickListener,WebViewEvent{
	int m_height = 0;
	WindowsAdapter mAdapt;
	int mCurWindowPos = -1;
	int mRowsCount = 3;
	int mMaxRowsCount = 3;
	int mMinTabWidth=0;
	int mMaxTabWidth=0;
	
	public WindowsHorizontalPanel(Context context) {
		super(context);
	}
	public WindowsHorizontalPanel(Context context, AttributeSet ats) {
		super(context, ats);
	}
	public void readValuesFromSettings()
	{
		PanelSetting ps = PanelLayout.getPanelSettings().getPanelSetting(PanelLayout.PANEL_TABS);
		if(ps!=null&&ps.extraSettings!=null)
		{
			try{
				mMaxRowsCount = ps.extraSettings.optInt(IConst.MAX_TAB_ROWS, 1);
				mMaxTabWidth = ps.extraSettings.optInt(IConst.MAX_TAB_WIDTH,0);
				mMinTabWidth = ps.extraSettings.optInt(IConst.MIN_TAB_WIDTH,0);
			}
			catch(Throwable e)
			{}
		}
	}
	public final void scrollToCurrent()
	{
		post(mScrollToCurPos);
	}
	void updateAdapterItems(boolean scrollToCurrent)
	{
		mAdapt.notifyDataSetChanged();
		getRealAdapter().notifyDataSetChanged();
		if(scrollToCurrent)
			scrollToCurrent();
	}
	@Override
	protected void onMeasure(int widthSpec, int heightSpec) {
		super.onMeasure(widthSpec, heightSpec);
// высота вкладоки
		int height = 0;
		if (Prefs.isTabsHeight())
			height = 25;
		int h = getMaxHeight()+height;
		calcCurrentRows(getMeasuredWidth());
		if(h!=getMaxHeight())
			setMeasuredDimension(getMeasuredWidth(), getMaxHeight()+height);
	}
	void calcCurrentRows(int w)
	{
		if(mAdapt==null)
		{
			mRowsCount = 1;
			return;
		}
		int count = mAdapt.getCount();
		if(count==0||mMinTabWidth==0||w==0)
			mRowsCount = 1;
		else
		{
			int fixSize = w/mMinTabWidth;
			mRowsCount = count/fixSize;
//			if(count%fixSize>0)
//				mRowsCount++;
			if(mRowsCount>mMaxRowsCount)
				mRowsCount = mMaxRowsCount;
		}
		if(mRowsCount<1)
			mRowsCount = 1;
	}
	void setChild(int pos,View v)
	{
		if(v==null)
			return;
		PanelButton pb = (PanelButton)v;
		Tab ww = mAdapt.getWindows().getWindowAt(pos, false);
		boolean cur = ww==mAdapt.getWindows().getCurrent(); 
		MyTheme.get().setView(pb, cur?MyTheme.ITEM_TAB_SELECTED:MyTheme.ITEM_TAB);
		ImageView iv = pb.getImageView();
		TextView tv = pb.getSmallText();
		if(iv!=null&&tv!=null)
		{
			boolean load = ww.isLoading();
			UIUtils.showViews(load, tv);
			UIUtils.showViews(!load, iv);
			if (m_height != 0)
				tv.setTextSize(m_height);
			if(load)
				tv.setText(Integer.valueOf(ww.getLoadProgress()).toString()+'%');
		}
	}
	@Override
	protected void init()
	{
		setCanAutofill(false);
		setCheckWidthWhileNotAutoFill(false);
		mMaxRowsCount = 3;
//		mRowsCount = 3;
		super.init();
		setButtonsType(PanelButton.TYPE_BUTTON_ROW);
		TabList ww = ((MainActivity)getContext()).getTabList();
		((MainActivity)getContext()).addWebViewListener(this);
		mAdapt = new WindowsAdapter(getContext(),ww) {
			@Override
			public void updateItems() {
				updateAdapterItems(true);
			}
			@Override
			public ViewGroup getViewGroup() {
				return WindowsHorizontalPanel.this;
			}
			@Override
			public boolean setSelectedItem(int pos, View v, LoadBitmapInfo info) {
				setChild(pos, v);
				return true;
			}
			@Override
			public void setLoadInfoToView(int pos,View v,LoadBitmapInfo info)
			{
				if(!(v instanceof PanelButton))
					return;
				Tab ww = (Tab)info.param;
				info.favicon = ww.getFavicon();
				info.loadImage = false;
				super.setLoadInfoToView(pos, v, info);
				PanelButton pb = (PanelButton)v;
				pb.setImage(info.favicon);
				v.setOnLongClickListener(WindowsHorizontalPanel.this);
				v.setOnLongClickListener(WindowsHorizontalPanel.this);
			}
			@Override
			public void onBitmapLoad(LoadBitmapInfo info) {
				super.onBitmapLoad(info);
			}
		};
		BrowserApp.INSTANCE.addGlobalListener(this);
		PanelButtonRecyclerAdapter ad =  setBookmarks(mAdapt);
		ad.setOnLongClickListener(mLongClickListener);
		mScrollToCurPos = new Runnable() {
			@Override
			public void run() {
				int pos = mAdapt.getWindows().getCurPos();
				if(pos>-1&&pos<mAdapt.getWindows().getCount())		
					scrollToPosition(pos);
			}
		};
		post(mScrollToCurPos);
	}
	@Override
	public void onGlobalEvent(int code, Object param) {
//		if(code==BrowserApp.GLOBAL_WINDOWS_CHANGED)
//		{
//			updateAdapterItems();
//		}
	}
	
	@Override
	public void onClick(View v) {
//		if(v.getId()==R.id.closeContainer&&v.getTag() instanceof WebWindow)
//		{
//			final WebWindow ww = (WebWindow)v.getTag();
//			final MainActivity a = (MainActivity)getContext();
//			new ThemedDialog(getContext()).setConfirm(getContext().getString(R.string.act_close_windows), a, new ConfirmOper() {
//				
//				@Override
//				public void onConfirm(Object userParam) {
//					a.windowClose(ww.windowId);
//				}
//			});
//			return;
//		}
		if(v instanceof PanelButton&&v.getTag() instanceof Tab)
		{
			Tab ww = (Tab)v.getTag();
			MainActivity a = (MainActivity)getContext();
			if(a.getTab().windowId==ww.windowId)
			{
				onLongClick(v);
				return;
			}
			a.tabOpen(ww.windowId);
			return;
		}
		super.onClick(v);
	}
	@Override
	public PanelButtonRecyclerAdapter createPanelButtonAdapter(BookmarkAdapter ba, int buttonType) {
		return new PanelButtonRecyclerAdapter(ba, buttonType)
		{
			@Override
			public PanelButtonHolder onCreateViewHolder(ViewGroup vg, int pos) {
				PanelButtonHolder vh = super.onCreateViewHolder(vg, pos);
				PanelButton pb = (PanelButton)vh.itemView;
				View v =pb.getChildAt(0);
				
				if(mMaxRowsCount==1)
				{
					if(v instanceof LinearLayoutEx)
						((LinearLayoutEx)v).setMinAndMaxWidth(mMinTabWidth, mMaxTabWidth);
				}
				return vh;
			}
		};
	}
	@Override
	public void onBitmapLoad(LoadBitmapInfo loadInfo) {
		
	}
	@Override
	public boolean onLongClick(View v) {
		if(v instanceof PanelButton&&v.getTag() instanceof Tab)
		{
			Tab ww = (Tab)v.getTag();
			MainActivity a = (MainActivity)getContext();
			a.showWindowLayout(ww);
		}
		return true;
	}
	Runnable mScrollToCurPos;
	@Override
	protected void checkLP(LayoutParams lp) 
	{
			
	};
	@Override
	public void onWebViewEvent(int code, EventInfo info) {
		switch(code)
		{
			case WebViewEvent.WWEVENT_WINDOWS_INVALIDATE:
				try{
					if(mAdapt==null)
						return;
					int cc = getChildCount();
					for(int i=cc-1;i>=0;i--)
					{
						View v = getChildAt(i);
						int pos = getChildPosition(v);
						mAdapt.processView(pos, v, false);
					}
				}
				catch(Throwable e)
				{
					e.printStackTrace();
				}
				break;
//				int pos = mAdapt.getWindows().getPos(info.getWebView());
//				if(pos>-1)
//					getRealAdapter().notifyItemChanged(pos);
//				else
//					updateAdapterItems(true);
//				break;
			case WWEVENT_WINDOW_LIST_CHANGED:	
				updateAdapterItems(true);
				break;
		}
	}
	
	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		postDelayed(mScrollToCurPos,300);
	}
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
	}
	@Override
	public void onBitmapsLoad() {
		
	}
	public final void setRowsCount(int rowsCount)
	{
		mRowsCount = rowsCount;
	}
	@Override
	public int getMaxHeight() {
		return super.getMaxHeight()*mRowsCount;
	}
	@Override
	protected LinearLayoutManager createLinearLayoutManager(int orientation,boolean fill) {
		calcCurrentRows(getWidth());
		if(mMaxRowsCount>1)
		{
			return new GridLayoutManager(getContext(), mRowsCount, orientation, mReverseLayout)
			{
				public boolean checkLayoutParams(android.support.v7.widget.RecyclerView.LayoutParams lp) 
				{
					lp.width = mMinTabWidth;
					return true;
				}
			};
		}
		return super.createLinearLayoutManager(orientation, fill);
	}
}
