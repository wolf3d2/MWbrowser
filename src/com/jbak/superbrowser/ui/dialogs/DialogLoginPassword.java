package com.jbak.superbrowser.ui.dialogs;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.HttpAuthHandler;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import com.mw.superbrowser.R;
import com.jbak.superbrowser.ui.themes.MyTheme;

public class DialogLoginPassword extends ThemedDialog {

	EditText mLogin;
	EditText mPassword;
	CheckBox mShowPassword;
	
	HttpAuthHandler mHandler;
	public DialogLoginPassword(Context context, int theme,HttpAuthHandler handler,String realm) {
		super(context, theme);
		init(handler,realm);
	}
	public DialogLoginPassword(Context context,HttpAuthHandler handler,String realm) {
		super(context);
		init(handler,realm);
	}
	void init(HttpAuthHandler handler,String realm)
	{
		mHandler = handler;
		setTitleText(realm);
		setButtons(true);
		View v = setView(R.layout.dialog_login_password);
		mShowPassword = (CheckBox)v.findViewById(R.id.showPassword);
		mShowPassword.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked)
					mPassword.setInputType(EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD|EditorInfo.TYPE_CLASS_TEXT);
				else
					mPassword.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD|EditorInfo.TYPE_CLASS_TEXT);
			}
		});
		mLogin = (EditText)v.findViewById(R.id.login);
		mPassword = (EditText)v.findViewById(R.id.password);
		MyTheme.get().setViews(MyTheme.ITEM_DIALOG_TEXT, mShowPassword);
	}
	@Override
	protected void onOk(boolean ok) {
		if(ok)
			mHandler.proceed(mLogin.getText().toString(), mPassword.getText().toString());
		else
			mHandler.cancel();
		super.onOk(ok);
	}
	
}
