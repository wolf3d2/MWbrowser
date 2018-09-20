package com.jbak.superbrowser.panels;

import com.jbak.superbrowser.MainActivity;
import com.jbak.superbrowser.WebViewEvent;
import com.mw.superbrowser.R;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PanelNavigation extends LinearLayout
{
	TextView up = null;
	TextView down = null;
	TextView left = null;
	TextView right = null;
	static Context m_c;
	LinearLayout ll;
	
	public PanelNavigation(MainActivity  c) {
		super(c);
		m_c = c;
		createView();
	}
	public void createView()
	{
//		ll = (LinearLayout)m_c.fi
//		ll.setOrientation(LinearLayout.HORIZONTAL);
//		up = createTextView();
//		up.setText("△");
//		ll.addView(up);
//		down = createTextView();
//		down.setText("▽");
//		ll.addView(down);
//		left = createTextView();
//		left.setText("◁");
//		ll.addView(left);
//		right = createTextView();
//		right.setText("▷");
//		ll.addView(right);
//		ll.setGravity(Gravity.LEFT|Gravity.BOTTOM);
	}
	public TextView createTextView()
	{
		TextView tv = new TextView(m_c);
		tv.setOnClickListener(m_ClickListener);
		return tv;
	}
	
    View.OnClickListener m_ClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
            case R.id.action_panel:
                return;
            }
        }
    };
	
}
