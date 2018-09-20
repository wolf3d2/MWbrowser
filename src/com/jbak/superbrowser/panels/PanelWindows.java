package com.jbak.superbrowser.panels;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import ru.mail.mailnews.st;

import com.jbak.superbrowser.Action;
import com.jbak.superbrowser.IConst;
import com.jbak.superbrowser.MainActivity;
import com.mw.superbrowser.R;
import com.jbak.superbrowser.ui.PanelLayout;
import com.jbak.superbrowser.ui.PanelSetting;
import com.jbak.superbrowser.ui.WindowsHorizontalPanel;
import com.jbak.superbrowser.ui.themes.MyTheme;

public class PanelWindows extends LinearLayout implements IConst{
// рисование панели
	WindowsHorizontalPanel mPanel;
	ImageView mAdd;
	public PanelWindows(Context context) {
		super(context);
		init();
	}
	void init()
	{
		MyTheme.get().setViews(MyTheme.ITEM_HORIZONTAL_PANEL_BACKGROUND, this);
		int plusPos = IConst.RIGHT;
		PanelSetting ps = PanelLayout.getPanelSettings().getPanelSetting(PanelLayout.PANEL_TABS);
		if(ps!=null&&ps.extraSettings!=null)
			plusPos = ps.extraSettings.optInt(IConst.PLUS_BUTTON, IConst.RIGHT);
		setOrientation(HORIZONTAL);
		setGravity(Gravity.CENTER_VERTICAL);
		LayoutParams lp;
		lp = new LayoutParams(0,LayoutParams.WRAP_CONTENT);
		lp.weight = 1;
		mPanel = new WindowsHorizontalPanel(getContext());
		mPanel.readValuesFromSettings();
		addView(mPanel, lp);
		mAdd = new ImageView(getContext());
		lp = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT);
		//mAdd.setImageResource(R.drawable.plus);
		st.setImageColor(getContext(), mAdd, R.drawable.plus);
		mAdd.setAdjustViewBounds(true);
		mAdd.setMaxWidth((int)getResources().getDimension(R.dimen.plus_button_max_size));
		
		if(plusPos==LEFT)
			addView(mAdd, 0, lp);
		else
			addView(mAdd, lp);
		if(plusPos==DISABLED)
			mAdd.setVisibility(GONE);
			
	}
	@Override
	public ArrayList<View> getTouchables() {
		ArrayList<View>ar =  super.getTouchables();
		return ar;
	}
	public void setRowsCount(int rowsCount)
	{
		mPanel.setRowsCount(rowsCount);
	}
	protected void runNewWindow()
	{
		if(getContext() instanceof MainActivity)
		{
			((MainActivity)getContext()).runAction(Action.create(Action.NEW_TAB));
		}
	}
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if(isTouchEventForViews(ev, null, mAdd))
		{
			if(ev.getAction()==MotionEvent.ACTION_DOWN)
				runNewWindow();
			return true;
		}
		boolean ret = super.dispatchTouchEvent(ev);
		return ret;
	}
    public static final boolean isTouchEventForViews(MotionEvent evt, Rect reusedRect, View ...views)
    {
    	if(reusedRect==null)
    		reusedRect = new Rect();
    	int x = (int) evt.getRawX();
    	int y = (int) evt.getRawY();
    	for(View v:views)
    	{
    		if(v.getVisibility()==View.VISIBLE)
    		{
	    		v.getGlobalVisibleRect(reusedRect);
	    		if(reusedRect.contains(x, y))
	    			return true;
    		}
    	}
    	return false;
    }
    public final WindowsHorizontalPanel getWindowsPanel()
    {
    	return mPanel;
    }

}
