package com.jbak.superbrowser.ui.dialogs;

import ru.mail.mailnews.st;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.graphics.Color;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.TextView;

import com.jbak.superbrowser.ActArray;
import com.jbak.superbrowser.Action;
import com.jbak.superbrowser.stat;
import com.mw.superbrowser.R;
import com.jbak.superbrowser.ui.HorizontalPanel;
import com.jbak.superbrowser.ui.LinearLayoutEx;
import com.jbak.superbrowser.ui.OnAction;
import com.jbak.superbrowser.ui.PanelButton;
import com.jbak.superbrowser.ui.themes.MyTheme;
import com.jbak.ui.ConfirmOper;
import com.jbak.ui.CustomDialog;

@SuppressLint("NewApi")
public class ThemedDialog extends CustomDialog implements OnAction{
	FrameLayout mContentFrame;
	protected LinearLayoutEx mContainer;
	HorizontalPanel mActionPanel;
	CheckBox cb_showKbd;
	TextView mTitle;
	public ThemedDialog(Context context) {
		super(context);
		init();
	}
	public ThemedDialog(Context context,int theme) {
		super(context,theme);
		init();
	}
	public ThemedDialog(Context context,boolean hideInput, int theme)
	{
		super(context,hideInput,theme);
		init();
	}
	protected int getHorizontalMargins()
	{
		return 20;
	}
	protected void init()
	{
		 MyTheme.get().onCreateThemedDialog(this);
		 mContainer=(LinearLayoutEx) LayoutInflater.from(context()).inflate(R.layout.dialog_themed, null);
		 mContainer.setMaxWidth(st.dp2px(context(), 480));
		 RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		 lp.leftMargin = lp.rightMargin = getHorizontalMargins();
		 inflate(mContainer, lp);
		 MyTheme.get().setView(mTopContainer, MyTheme.ITEM_DIALOG_SHADOW);
		 mContentFrame = (FrameLayout)mContainer.findViewById(R.id.contentFrame);
		 cb_showKbd = (CheckBox)mContainer.findViewById(R.id.cb_showkbd);
		 
		 mActionPanel = (HorizontalPanel) mContainer.findViewById(R.id.bottomPanel);
		 mTitle = (TextView)mContainer.findViewById(R.id.title);
		 MyTheme.get().setViews(MyTheme.ITEM_DIALOG_BACKGROUND, mContainer);
		 MyTheme.get().setViews(MyTheme.ITEM_TITLE, mTitle);
		 MyTheme.get().setViews(MyTheme.ITEM_HORIZONTAL_PANEL_BACKGROUND, mActionPanel);
		 mActionPanel.setVisibility(View.GONE);
		 mActionPanel.setOnActionListener(this);
	}
	public void setConfirm(String confirm,final Object userParam,final ConfirmOper op)
	{
		setAlert(confirm);
		setButtons(false);
		if(op.confirmTitle!=0)
			setTitleText(op.confirmTitle);
		setOnButtonClick(new OnButtonClick() {
			
			@Override
			public void onButtonClick(boolean yes) {
				if(yes)
					op.onConfirm(userParam);
				else
					op.onCancel(userParam);
			}
		});
		show();
	}
	public void setConfirmOk(String title,String confirm,final Object userParam,final ConfirmOper op)
	{
		setAlert(confirm);
		setAlert(confirm, title);
		if(title!=null)
			setTitleText(title);
		setOnButtonClick(new OnButtonClick() {
			
			@Override
			public void onButtonClick(boolean yes) {
				op.onConfirm(userParam);
			}
		});
		show();
	}
	public ThemedDialog setAlert(String text)
	{
		return setAlert(text, null);
	}
	public ThemedDialog setAlert(int text)
	{
		return setAlert(text, 0);
	}
	public ThemedDialog setAlert(int text,int title)
	{
		String txt = text==0?null:context().getString(text);
		String tt = title==0?null:context().getString(title);
		return setAlert(txt, tt);
	}
	public ThemedDialog setAlert(String text,String title)
	{
		setTitleText(title);
		TextView tv = new TextView(context());
		tv.setTextAppearance(context(), R.style.textView);
		tv.setMaxHeight(stat.getSizeHeight(context()));
		tv.setText(text);
		tv.setMovementMethod(new ScrollingMovementMethod());
		setView(tv);
		setButtons(new ActArray(Action.OK), 0);
		MyTheme.get().setViews(MyTheme.ITEM_DIALOG_TEXT, tv);
		return this;
	}
	public ThemedDialog setTitleText(int res)
	{
		mTitle.setText(res);
		return this;
	}
	public ThemedDialog setTitleText(String res)
	{
		mTitle.setText(res);
		return this;
	}
	public View setView(View v)
	{
		mContentFrame.addView(v);
		return v;
	}
	public View setView(int viewId)
	{
		return setView(LayoutInflater.from(context()).inflate(viewId, null));
	}
	public ThemedDialog setButtons(boolean okCancel)
	{
		if(okCancel)
			return setButtons(new ActArray(Action.OK,Action.CANCEL),0);
		else
			return setButtons(new ActArray(Action.YES,Action.NO),0);
	}
	public ThemedDialog setButtons(ActArray actions,int oneLine)
	{
		if(oneLine == 0)
			mActionPanel.setButtonsType(PanelButton.TYPE_BUTTON_TEXT_ONELINE);
		else if(oneLine == 1)
			mActionPanel.setButtonsType(PanelButton.TYPE_BUTTON_NORMAL);
		mActionPanel.setActions(actions);
		setContentMargins(true);
		mActionPanel.setVisibility(View.VISIBLE);
		return this;
	}
	@Override
	public void onAction(Action act) {
		if(act.command==Action.OK||act.command==Action.YES)
			processId(R.id.buttonOk);
		else if(act.command==Action.CANCEL||act.command==Action.NO)
			processId(R.id.buttonNo);
	}
	public void setDialogMaxWidth(int maxWidth)
	{
		if(maxWidth>0)
			mContainer.setMaxWidth(maxWidth);
		else
			mContainer.setMaxWidth(Integer.MAX_VALUE);
	}
	/** устанавливает текст для редактирования - если конечная строка пустая, 
	 * то устанавливается defText */
	public CustomDialog setInput(String title, String text, String defText, final OnUserInput listener)
	{
		mDefaultValueText = defText;
		return setInput(title, text, listener);
	}
	public CustomDialog setInput(String title, String text, final OnUserInput listener)
	{
		setTitleText(title);
		EditText et = new EditText(context());
		// если откоментить, тогда не виден кусор
		//et.setBackgroundColor(Color.WHITE);
		et.setTextAppearance(context(), R.style.textEdit);
		et.setText(text);
		st.showEditKeyboard(et);

		mText = et;
		et.setOnEditorActionListener(new OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_SEARCH||actionId == EditorInfo.IME_ACTION_NEXT||actionId==0&&(event==null||event.getAction()==KeyEvent.ACTION_DOWN)) {
                	processId(R.id.buttonYes);
                    return true;
                }
                return false;
            }
        });
		setView(et);
		setButtons(true);
		mInputListener = listener;
		return this;
	}
	@Override
	public void dismiss() {
		View foc = mTopContainer.findFocus();
		if(foc instanceof EditText)
		{
			InputMethodManager imm = (InputMethodManager)context().getSystemService(Service.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(foc.getWindowToken(), 0);
		}
		super.dismiss();
	}
	public void setContentMargins(boolean bigMargins)
	{
		int lrPad  = context().getResources().getDimensionPixelSize(R.dimen.magic_padding);
		int tbPad = context().getResources().getDimensionPixelSize(bigMargins?R.dimen.alert_padding:R.dimen.magic_padding);
		mContentFrame.setPadding(lrPad, tbPad, lrPad, tbPad);
	}
	public final String getString(int strId)
	{
		return context().getString(strId);
	}

}