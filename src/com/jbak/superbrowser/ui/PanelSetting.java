package com.jbak.superbrowser.ui;

import org.json.JSONObject;

import com.jbak.superbrowser.IConst;

public class PanelSetting implements IConst
{
	public int id;
	public int nameRes;
	public boolean top=true;
	public boolean visible=false;
	public JSONObject extraSettings;
	public PanelSetting(int id,boolean top,boolean visible)
	{
		this.id = id;
		this.top = top;
		this.visible = visible;
	}
	public PanelSetting(JSONObject jo)
	{
		id = jo.optInt(_ID,id);
		top = jo.optBoolean(TOP,false);
		visible = jo.optBoolean(VISIBLE,false);
		extraSettings = jo.optJSONObject(EXTRA_SETTINGS);
	}
	public JSONObject getJSON()
	{
		JSONObject jo = new JSONObject();
		try {
			jo.putOpt(_ID, id);
			jo.putOpt(TOP, top);
			jo.put(VISIBLE, visible);
			jo.putOpt(EXTRA_SETTINGS, extraSettings);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return jo;
	}
	@Override
	public String toString() {
		return getJSON().toString();
	}
}