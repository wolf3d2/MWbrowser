package com.jbak.ui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;

import ru.mail.mailnews.st;
import android.app.ListActivity;
import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

import com.jbak.superbrowser.MainActivity;
import com.mw.superbrowser.R;
import com.jbak.superbrowser.ui.themes.MyTheme;
import com.jbak.utils.GlobalHandler;
import com.jbak.utils.Utils;

public class CustomPopup extends PopupWindow implements GlobalHandler,OnDismissListener{
	
	static int COLOR_LOAD = 0xCC1B95E0;
	static int COLOR_ERROR = 0xCCF70743;
	private static final int WHAT_DISMISS = 1;
	private static final int WHAT_SHOW_LOAD_INDICATOR = 2;
	private static final int WHAT_CLEAR = 3;
	protected View mAnchor;
	ArrayList<Object> mParams;
	
	public CustomPopup(View anchor) {
		super(anchor.getContext());
		mAnchor = anchor;
	}
	public void toast(int textId,int duration)
	{
		toast(mAnchor.getContext().getString(textId), duration);
	}
	public CustomPopup showWrapContent(View content,int gravity)
	{
		return showWrapContent(content, gravity,0,0,0);
	}
	public CustomPopup showWrapContent(View content,int gravity,int xoffset,int yoffset,int animStyle)
	{
		setWidth(LayoutParams.WRAP_CONTENT);
		setHeight(LayoutParams.WRAP_CONTENT);
		setAnimationStyle(0);
		setBackgroundDrawable(null);
		setWindowLayoutMode(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		setContentView(content);
		try{
			setFixedListener();
			showAtLocation(mAnchor, gravity, xoffset, yoffset);
		}
		catch(Throwable e)
		{
			GlobalHandler.command.removeAllMesages(this);
		}
		return this;
	}
	public void toast(String text,int duration)
	{
//		setOnDismissListener(this);
//		View content = LayoutInflater.from(mAnchor.getContext()).inflate(R.layout.custom_dialog, null);
//		CustomDialog.setTextViewText(content, R.id.text, text);
//		int pad = St.dp2px(context(), context().getResources().getDimensionPixelSize(R.dimen.alert_padding));
//		View vt = content.findViewById(R.id.text);
//		vt.setBackgroundColor(MyTheme.COLOR_TRANSPARENT);
//		MyTheme.get().setViews(MyTheme.ITEM_DIALOG_BACKGROUND, content);
//		MyTheme.get().setViews(MyTheme.ITEM_DIALOG_TEXT, vt);
//		content.findViewById(R.id.text).setPadding(pad, pad, pad, pad);
//		content.findViewById(R.id.buttonsYesNo).setVisibility(View.GONE);
//		GlobalHandler.command.sendDelayedByObj(WHAT_DISMISS, this, duration);
//		showWrapContent(content, Gravity.CENTER);
		setOnDismissListener(this);
		TextView tv = new TextView(mAnchor.getContext());
		tv.setText(text);
		tv.setTextAppearance(tv.getContext(), R.style.textView);
		MyTheme.get().setViews(MyTheme.ITEM_DIALOG_BACKGROUND, tv);
		MyTheme.get().setViews(MyTheme.ITEM_DIALOG_TEXT, tv);
		int pad = st.dp2px(context(), context().getResources().getDimensionPixelSize(R.dimen.alert_padding));
		tv.setPadding(pad, pad, pad, pad);
		GlobalHandler.command.sendDelayedByObj(WHAT_DISMISS, this, duration);
		showWrapContent(tv, Gravity.CENTER);
	}
	private void setParams(Object ... obj)
	{
		mParams = new ArrayList<Object>();
		Collections.addAll(mParams, obj);
	}
	public void setLoadIndicator(String text,boolean error,int startDelay,int duration)
	{
		setParams(text,Boolean.valueOf(error),Integer.valueOf(duration));
		GlobalHandler.command.sendDelayedByObj(WHAT_SHOW_LOAD_INDICATOR, this, startDelay);
	}
	protected void internalDismiss()
	{
		super.dismiss();
	}
	@Override
	public void dismiss() {
		try{
			GlobalHandler.command.removeAllMesages(this);
			GlobalHandler.command.sendDelayedByObj(WHAT_CLEAR, this, 2000);
			super.dismiss();
		}
		catch(Throwable e){
			Utils.log(e);
		}
	}
	@Override
	public void onHandlerEvent(int what) {
		switch (what) {
			case WHAT_DISMISS:
				dismiss();
				break;
			case WHAT_CLEAR:
				mAnchor = null;
				mParams = null;
				break;
		}
	}
	@Override
	public void onDismiss() {
		
	}
	public static final View getAnchorFromContext(Context c)
	{
		if(c instanceof ListActivity)
			return ((ListActivity)c).getListView();
		else if(c instanceof MainActivity)
			return ((MainActivity)c).getTopContainer();
		return null;
	}
	public static final void toast(Context c,int textId)
	{
		toast(c, textId, 1000);
	}
	public static final void toast(Context c,int textId,int duration)
	{
		toast(c, c.getString(textId),duration);
	}
	public final Context context()
	{
		if(mAnchor!=null)
			return mAnchor.getContext();
		if(getContentView()!=null)
			return getContentView().getContext();
		return null;
	}
	public static final void toast(Context c,String text,int duration)
	{
		View anchor = getAnchorFromContext(c);
		if(anchor!=null)
			new CustomPopup(anchor).toast(text, duration);
		else
			Toast.makeText(c, text, Toast.LENGTH_LONG).show();
	}
	public static final void toast(Context c,String text)
	{
		toast(c, text, 700);
	}
	protected void setFixedListener()
	{
		if(Build.VERSION.SDK_INT<Build.VERSION_CODES.ICE_CREAM_SANDWICH){
		    try {
		      final Field fAnchor = PopupWindow.class.getDeclaredField("mAnchor");
		      fAnchor.setAccessible(true);
		      Field listener = PopupWindow.class.getDeclaredField("mOnScrollChangedListener");
		      listener.setAccessible(true);
		      final ViewTreeObserver.OnScrollChangedListener originalListener = (ViewTreeObserver.OnScrollChangedListener) listener.get(this);
		      ViewTreeObserver.OnScrollChangedListener newListener=
		                new ViewTreeObserver.OnScrollChangedListener() {
		                    public void onScrollChanged() {
		                        try {
		                            View mAnchor = (View) fAnchor.get(this);
		                            if(mAnchor==null){
		                                return;
		                            }else{
		                               originalListener.onScrollChanged();
		                            }
		                        } catch (Exception e) {
		                        	Utils.log(e);
		                        }
		                    }
		                };
		      listener.set(this,newListener);
		    } catch (Exception e) {
		    	Utils.log(e);
		    }
		  }
	}
}
