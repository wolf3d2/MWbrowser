package com.jbak.superbrowser.ui;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.jbak.superbrowser.Action;
import com.jbak.superbrowser.ui.themes.MyTheme;

public class PanelButtonsAdapter extends ArrayAdapter<Action>
{
	int mType = PanelButton.TYPE_BUTTON_NORMAL;
	public PanelButtonsAdapter(Context context, List<Action> objects) {
		super(context, 0, objects);
	}
	public PanelButtonsAdapter(Context context, List<Action> objects,int type) {
		super(context, 0, objects);
		mType = type;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView==null)
			convertView = new PanelButton(getContext(),mType);
		Action a = getItem(position);
		PanelButton pb = (PanelButton)convertView;
		pb.setAction(a);
		MyTheme.get().setPanelButton(pb,position,false);
		onSetItem(pb);
		return convertView;
	}
	public void onSetItem(PanelButton pb)
	{
		
	}
}