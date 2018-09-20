package com.jbak.superbrowser.ui.dialogs;

import com.jbak.superbrowser.ActArray;
import com.jbak.superbrowser.MainActivity;
import com.jbak.superbrowser.panels.InterfaceSettingsLayout;
import com.jbak.superbrowser.panels.PanelQuickTools;
import com.jbak.superbrowser.panels.ToolsPanelSettingsLayout;

import android.content.Context;
import android.view.View;

public class DialogToolsPanelSettings extends ThemedDialog {

	ToolsPanelSettingsLayout mLayout;
	public DialogToolsPanelSettings(Context context,ActArray existActions) {
		super(context);
		mLayout = new ToolsPanelSettingsLayout(context, existActions);
		setView(mLayout);
		mTitle.setVisibility(View.GONE);
		mContainer.setBackgroundColor(0x00000000);
		mContentFrame.setPadding(1, 1, 1, 1);
	}
	@Override
	public void dismiss() {
		super.dismiss();
		onChangeActions();
	}
	public void onChangeActions()
	{
		PanelQuickTools.setMinipanelActions(mLayout.getExistActions());
		if(context() instanceof MainActivity)
		{
			MainActivity ma = (MainActivity) context();
//			ma.sendWebViewEvent(WebViewEvent.WWEVENT_UI_CHANGED, null, null, null);
			InterfaceSettingsLayout.checkMagicKeyCanDisabled(ma);
		}
	}
	@Override
	protected int getHorizontalMargins() {
		return 1;
	}
	@Override
	public void setContentMargins(boolean bigMargins)
	{
		mContentFrame.setPadding(1, 1, 1, 1);
	}

}
