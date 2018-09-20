package com.jbak.superbrowser.panels;

import ru.mail.mailnews.st;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

import com.jbak.superbrowser.ActArray;
import com.jbak.superbrowser.Action;
import com.jbak.superbrowser.BrowserApp;
import com.jbak.superbrowser.IConst;
import com.mw.superbrowser.R;
import com.jbak.superbrowser.ui.PanelButton;
import com.jbak.superbrowser.ui.PanelButtonsAdapter;
import com.jbak.superbrowser.ui.PanelLayout;
import com.jbak.superbrowser.ui.PanelSetting;
import com.jbak.superbrowser.ui.themes.MyTheme;

public class ToolsPanelSettingsLayout extends RelativeLayout implements IConst, OnClickListener{

	ActArray mExistsActions;
	ActArray mAddActions;
	ListView mExist;
	ListView mAdd;
	Bitmap mDragImage;
	TextView mButtons;
	public ToolsPanelSettingsLayout(Context c,ActArray existActions) {
		super(c);
		mExistsActions = existActions;
		mAddActions = createAddActions();
		checkAddActions();
		setBackgroundColor(0x00000000);
		LinearLayout ll = new LinearLayout(c);
		ll.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout layoutCenter = new LinearLayout(c);
		layoutCenter.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,LayoutParams.MATCH_PARENT);
		lp.weight = 0.9f;
		mExist = new ListView(c);
		mAdd = new ListView(c);
		TextView delim = (TextView) LayoutInflater.from(c).inflate(R.layout.textview_small, null);
		delim.setText(R.string.panelQuickTools_settings);
		mButtons = (TextView) LayoutInflater.from(c).inflate(R.layout.textview_title, null);
		setButtons();
		MyTheme.get().setView(delim,MyTheme.ITEM_DIALOG_BACKGROUND,MyTheme.ITEM_DIALOG_TEXT);
		MyTheme.get().setView(mButtons,MyTheme.ITEM_DIALOG_BACKGROUND,MyTheme.ITEM_DIALOG_TEXT);
		ll.addView(mExist,lp);
		LinearLayout.LayoutParams lpButtons = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		lpButtons.topMargin = st.dp2px(c, 5);
		LinearLayout.LayoutParams lpCenterLayout = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT);
		lpCenterLayout.leftMargin = lpCenterLayout.rightMargin = 2;
		lpCenterLayout.weight = 1.2f;
		layoutCenter.addView(delim);
		layoutCenter.addView(mButtons,lpButtons);
		LinearLayout.LayoutParams lpLeftBtn = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		lpLeftBtn.topMargin = 25;
		lpLeftBtn.leftMargin = lpLeftBtn.rightMargin = 2;
		
		// определяем ширину кнопок
		LinearLayout.LayoutParams lpleft = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		lpleft.topMargin = 25;
		lpleft.leftMargin = lpleft.rightMargin = 2;
		
		Button up = new Button(c);
		up.setText(R.string.up5);
		up.setId(1);
		up.setOnClickListener(m_ClickListener);
		MyTheme.get().setView(up,MyTheme.ITEM_DIALOG_BACKGROUND,MyTheme.ITEM_DIALOG_TEXT);
		layoutCenter.addView(up, lpleft);
		up.measure(0, 0);
		int w_up = up.getMeasuredWidth(); 
		layoutCenter.removeView(up);

		Button down = new Button(c);
		down.setText(R.string.down5);
		down.setId(2);
		down.setOnClickListener(m_ClickListener);
		MyTheme.get().setView(down,MyTheme.ITEM_DIALOG_BACKGROUND,MyTheme.ITEM_DIALOG_TEXT);
		layoutCenter.addView(down, lpleft);
		down.measure(0, 0);
		int w_down = down.getMeasuredWidth(); 
		layoutCenter.removeView(down);
		// теперь добавляем
		if (w_up>w_down)
			w_down = w_up;
		LinearLayout.LayoutParams lpLeftB = new LinearLayout.LayoutParams(w_down,LayoutParams.WRAP_CONTENT);
		lpLeftB.topMargin = 25;
		lpLeftB.leftMargin = lpLeftBtn.rightMargin = 2;
		layoutCenter.addView(up, lpLeftB);
		layoutCenter.addView(down, lpLeftB);

		ll.addView(layoutCenter,lpCenterLayout);
		ll.addView(mAdd,lp);
		mExist.setAdapter(new PanelButtonsAdapter(c, mExistsActions,PanelButton.TYPE_BUTTON_MEDIUM));
		mAdd.setAdapter(new PanelButtonsAdapter(c, mAddActions,PanelButton.TYPE_BUTTON_MEDIUM));
		RelativeLayout.LayoutParams rp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		addView(ll,rp);
	}
	int mDownPos = -1;
	Point mDown = new Point();
	Point mMove = new Point();
	ListView mDrag = null;
	boolean mTargetAdd=false;
	int getItemPos(ListView lv, Point touchPos)
	{
		return lv.pointToPosition(touchPos.x-lv.getLeft(),touchPos.y-lv.getTop());
	}
	void setButtons()
	{
		int but = PanelQuickTools.getButtonTypeMiniPanel();
		mButtons.setText(BUTTON_TYPES.getValueByKey(but));
		mButtons.setOnClickListener(this);
	}
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if(ev.getAction()==MotionEvent.ACTION_DOWN)
		{
			mDown.set((int)ev.getX(), (int)ev.getY());
			mDownPos = getItemPos(mAdd, mDown);
			mTargetAdd = true;
			if(mDownPos<0)
			{
				mDownPos = getItemPos(mExist, mDown);
				mTargetAdd = false;
			}
		}
		if(ev.getAction()==MotionEvent.ACTION_UP||ev.getAction()==MotionEvent.ACTION_CANCEL)
		{
			if(mDrag!=null)
			{
				endDrag(new Point((int)ev.getX(),(int)ev.getY()));
				mDrag = null;
				invalidate();
			}
		}
		if(ev.getAction()==MotionEvent.ACTION_MOVE)
		{
			if(mDownPos<0)
				return true;
			mMove.x = (int) ev.getX();
			mMove.y = (int) ev.getY();
			if(mDrag!=null)
			{
				if(mDrag==mExist&&getDragImageLeft(mMove)>mExist.getRight())
				{
					// Вытащили за пределы списка
					endDrag(mMove);
					return true;
				}
				invalidate();
				return true;
			}
			if(mDownPos<0)
				return false;
			if(mTargetAdd&&Math.abs(mMove.x-mDown.x)-Math.abs(mMove.y-mDown.y)>25
			  ||!mTargetAdd		
			)
			{
				startDrag();
			}
		}
		return super.dispatchTouchEvent(ev);
	}
	int getDragImageLeft(Point center)
	{
		return center.x-mDragImage.getWidth()/2;
	}
	int getDragImageTop(Point center)
	{
		return center.y-mDragImage.getHeight()/2;
	}
	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		if(mDrag!=null&&mDragImage!=null&&!mDragImage.isRecycled())
		{
			canvas.drawBitmap(mDragImage, getDragImageLeft(mMove), getDragImageTop(mMove),null);
		}

	}
	void updateListViews()
	{
		((PanelButtonsAdapter)mExist.getAdapter()).notifyDataSetChanged();
		((PanelButtonsAdapter)mAdd.getAdapter()).notifyDataSetChanged();
	}
	void endDrag(Point upPos)
	{
		if(mDownPos<0)
			return;
		if(mDrag==mAdd)
		{
			int pos = getItemPos(mExist, upPos);
			if(pos>=0)
			{
				Action a = mAddActions.remove(mDownPos);
				mExistsActions.add(pos, a);
			}
			else
			{
				Rect r = new Rect(mExist.getLeft(), mExist.getTop(), mExist.getRight(),mExist.getBottom());
				if(r.contains(upPos.x, upPos.y))
				{
					Action a = mAddActions.remove(mDownPos);
					mExistsActions.add(a);
				}
					
			}
		}
		else if(mDrag==mExist)
		{
			int pos = getItemPos(mExist, upPos);
			if(pos>-1)
			{
				Action aMov = mExistsActions.get(mDownPos);
				if(mDownPos>pos)
					++mDownPos;
				mExistsActions.add(pos,aMov);
				mExistsActions.remove(mDownPos);
			}
			else if(pos<0&&getDragImageLeft(upPos)<mExist.getRight())
			{
				Action aMov = mExistsActions.get(mDownPos);
				mExistsActions.add(aMov);
				mExistsActions.remove(mDownPos);
			}
			else{
				Action a = mExistsActions.remove(mDownPos);
				mAddActions.add(a);
			}
		}
		mDownPos = -1;
		mDrag = null;
		updateListViews();
	}
	void startDrag()
	{
		if(mDownPos<0)
			return;
		mDrag = mTargetAdd?mAdd:mExist;
		int first = mDrag.getFirstVisiblePosition();
		int posView = mDownPos-first;
		View v = mDrag.getChildAt(posView);
        mDragImage = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(mDragImage);
        v.draw(c);
	}
	private void checkAddActions()
	{
		for(int i=mAddActions.size()-1;i>=0;i--)
		{
			Action addAction = mAddActions.get(i);
			for(int j = mExistsActions.size()-1;j>=0;j--)
			{
				Action exAction = mExistsActions.get(j);
				if(addAction.command==exAction.command)
				{
					mAddActions.remove(i);
					break;
				}
			}
		}
	}
	public final ActArray getExistActions()
	{
		return mExistsActions;
	}
	
	public ActArray createAddActions()
	{
		return new ActArray(
				Action.SHOW_MAIN_PANEL,
				Action.MAINMENU_SETTING,
				Action.MINI_PANEL_SETTINGS,
				//Action.CODEPAGE,
				Action.SHOW_CLOSED_TABS,
				Action.SEARCH_ON_PAGE,
				Action.BOOKMARKS,
				Action.SETTINGS,
				Action.HISTORY,
				Action.SHARE_ELEMENT,
				Action.FONT_SCALE_SETTINGS,
				Action.NEW_TAB,
				Action.TAB_LIST,
				Action.TAB_HISTORY,
				Action.CLEAR_DATA,
				Action.CLOSE_ALL_TABS,
				Action.DOWNLOAD_LIST,
				Action.CLOSE_TAB,
				Action.REFRESH,
				Action.TO_TOP,
				Action.TO_BOTTOM,
				Action.EXIT,
				Action.ADD_BOOKMARK,
				Action.GO_HOME,
				Action.QUICK_SETTINGS,
				Action.VOICE_SEARCH,
				Action.COPY_URL_TO_CLIPBOARD,
				Action.OPENFILE,
				Action.INTERFACE_SETTINGS,
				Action.MAGIC_BUTTON_POS,
				Action.SYSTEM_WIFI_NETWORKS,
				Action.SYSTEM_SETTINGS,
				Action.SYSTEM_MOBILE_SETTINGS
				);
	}
	@Override
	public void onClick(View v) {
		if(v==mButtons)
		{
			int but = PanelQuickTools.getButtonTypeMiniPanel();
			int index = BUTTON_TYPES.getIndexByKey(but);
			++index;
			if(index==BUTTON_TYPES.size())
				index = 0;
			PanelSetting ps = PanelLayout.getPanelSetting(PanelLayout.PANEL_QUICK_TOOLS);
			if(ps!=null&&ps.extraSettings!=null)
			{
				try {
					ps.extraSettings.put(BUTTON_TYPE, BUTTON_TYPES.getKeyByIndex(index));
					setButtons();
					BrowserApp.sendGlobalEvent(BrowserApp.GLOBAL_SETTINGS_CHANGED, STRVAL_MINI_PANEL);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
	}
    View.OnClickListener m_ClickListener = new View.OnClickListener()
    {
		@Override
        public void onClick(View v)
        {
        	if (mExist==null)
        		return;
			int pos = 0;
        	// 1 = кнопка up
        	// 2 = кнопка down
            switch (v.getId())
            {
            case 1:
            	pos = mExist.getFirstVisiblePosition()-5;
            	if (pos<0)
            		pos = 0;
            		mExist.smoothScrollToPosition(pos);
            		break;
            case 2:
            	pos = mExist.getLastVisiblePosition()+5;
            	if (pos>=mExist.getCount())
            		pos=mExist.getCount();
            		mExist.smoothScrollToPosition(pos);
            		break;
            }
        }
    };

}
