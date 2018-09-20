package com.jbak.superbrowser.ui.dialogs;

import android.view.KeyEvent;

import com.jbak.superbrowser.ActArray;
import com.jbak.superbrowser.Action;
import com.jbak.superbrowser.MainActivity;
import com.jbak.superbrowser.Prefs;
import com.mw.superbrowser.R;

public class DialogEmptyInterface extends ThemedDialog {

	public DialogEmptyInterface(MainActivity activity) {
		super(activity);
		setAlert(R.string.magic_key_disable_confirm);
		setButtons(new ActArray(Action.CANCEL), 0);
	}
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_MENU)
		{
			onOk(true);
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override
	protected void onOk(boolean ok) {
		super.onOk(ok);
		Prefs.setBoolean(Prefs.MENU_KEY_CONFIRMED, ok);
		Prefs.setBoolean(Prefs.MAGIC_BUTTON_VISIBLE, !ok);
		((MainActivity)context()).setInterface(0);
	}
}
