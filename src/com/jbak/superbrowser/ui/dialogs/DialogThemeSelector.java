package com.jbak.superbrowser.ui.dialogs;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import ru.mail.mailnews.st;

import com.jbak.superbrowser.MainActivity;
import com.mw.superbrowser.R;
import com.jbak.superbrowser.ui.themes.MyTheme;

public class DialogThemeSelector extends ThemedDialog {
	TextView mThemeName;
	boolean mClickLeft = true;
	public DialogThemeSelector(Context c) {
		super(c);
		View v = setView(R.layout.dialog_theme);
		ImageView iv = null;
		iv = (ImageView)v.findViewById(R.id.prev);
		st.setImageColor(c, iv, R.drawable.back);
		iv = (ImageView)v.findViewById(R.id.next);
		st.setImageColor(c, iv, R.drawable.right);
		setTitleText(R.string.act_select_theme);
		mContainer.setBackgroundColor(0xffffffff);
		 MyTheme.get().setViews(MyTheme.ITEM_DIALOG_BACKGROUND, v);
		mTopContainer.setBackgroundColor(MyTheme.COLOR_TRANSPARENT);
		mThemeName = (TextView)v.findViewById(R.id.text);
		mThemeName.setText(MyTheme.get().getThemeInfo().name);
		v.findViewById(R.id.prev).setOnClickListener(this);
		v.findViewById(R.id.next).setOnClickListener(this);
		MyTheme.get().setViews(MyTheme.ITEM_DIALOG_TEXT, mThemeName);
		RelativeLayout.LayoutParams lp = (LayoutParams) mContainer.getLayoutParams();
		lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		mContainer.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_UP)
				{
					mClickLeft = event.getX()<mContainer.getWidth()/2;
				}
				return false;
			}
		});
		mContainer.setOnClickListener(this);
	}
	void setTheme(MyTheme th)
	{
		((MainActivity)context()).onThemeChanged(th);
		dismiss();
		new DialogThemeSelector(context()).show();
	}
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if(v == mContainer)
		{
			id = mClickLeft?R.id.prev:R.id.next;
		}
		int indexCur = 0;
		for(int i=0;i<MyTheme.THEMES.length;i++)
		{
			if(MyTheme.get()==MyTheme.THEMES[i])
			{
				indexCur = i;
				break;
			}
		}
		if(id==R.id.prev)
		{
			--indexCur;
			if(indexCur<0)
				indexCur = MyTheme.THEMES.length-1;
			setTheme(MyTheme.THEMES[indexCur]);
		}
		else if(id==R.id.next)
		{
			++indexCur;
			if(indexCur>MyTheme.THEMES.length-1)
				indexCur = 0;
			setTheme(MyTheme.THEMES[indexCur]);
		}
		super.onClick(v);
	}
}
