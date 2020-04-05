package com.jbak.superbrowser.ui.dialogs;

import com.jbak.superbrowser.ActArray;
import com.jbak.superbrowser.MainActivity;
import com.jbak.superbrowser.panels.SetInterfaceLayout;
import com.jbak.superbrowser.panels.PanelMainMenu;
import com.jbak.superbrowser.panels.PanelQuickTools;
import com.jbak.superbrowser.panels.SetMainMenuLayout;
import com.jbak.superbrowser.panels.SetToolsPanelLayout;

import android.content.Context;
import android.view.View;

public class DialogMainMenuParagraph extends ThemedDialog {

	SetMainMenuLayout mLayout;
	public DialogMainMenuParagraph(Context context,ActArray existActions) {
		super(context);
		mLayout = new SetMainMenuLayout(context, existActions);
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
		PanelMainMenu.setMainmenuPanelActions(mLayout.getExistActions());
		if(context() instanceof MainActivity)
		{
			MainActivity ma = (MainActivity) context();
//			ma.sendWebViewEvent(WebViewEvent.WWEVENT_UI_CHANGED, null, null, null);
			SetInterfaceLayout.checkMagicKeyCanDisabled(ma);
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
