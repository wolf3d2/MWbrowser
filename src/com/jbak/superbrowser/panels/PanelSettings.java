package com.jbak.superbrowser.panels;

import java.util.ArrayList;

import org.json.JSONArray;

import com.jbak.superbrowser.Prefs;
import com.jbak.superbrowser.adapters.SettingsAdapter;
import com.jbak.superbrowser.ui.PanelSetting;
import com.jbak.utils.ObjectKeyValues;

@SuppressWarnings("serial")
public class PanelSettings extends ArrayList<PanelSetting>{
	public static interface SetDefaultPanelSetting
	{
		public void setDefaultPanelSetting(PanelSetting ps);
		public void setDefaultExtraSettings(PanelSetting ps);
	}
	String mPrefName;
	private ObjectKeyValues<Integer, Integer> mPanelsStrings;
	SetDefaultPanelSetting mDefSet;
	SettingsAdapter mAdapt;
	public PanelSettings(String prefName,ObjectKeyValues<Integer, Integer> panelsStrings,SetDefaultPanelSetting defSet) {
		mPrefName = prefName;
		mPanelsStrings = panelsStrings;
		mDefSet = defSet;
		reload();
	}
	public void reload()
	{
		clear();
		ArrayList<PanelSetting> ar = getPanelSettings(mPrefName, mPanelsStrings, mDefSet); 
		addAll(ar);
	}
	public static ArrayList<PanelSetting> getPanelSettings(String prefName,ObjectKeyValues<Integer, Integer> panelsNames,SetDefaultPanelSetting defSettings)
	{
		ArrayList<PanelSetting>ar = new ArrayList<PanelSetting>();
		String set = Prefs.get().getString(prefName, null);
		if(set!=null)
		{
			try {
				JSONArray jar = new JSONArray(set);
				for(int i=0;i<jar.length();i++)
				{
					PanelSetting ps = new PanelSetting(jar.getJSONObject(i));
					ps.nameRes = panelsNames.getValueByKey(ps.id);
					if(ps.extraSettings==null)
						defSettings.setDefaultExtraSettings(ps);
					ar.add(ps);
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		else
		{
			for(Integer i:panelsNames.getKeys())
			{
				PanelSetting ps = new PanelSetting(i, false, true);
				defSettings.setDefaultPanelSetting(ps);
				defSettings.setDefaultExtraSettings(ps);
				ps.nameRes = panelsNames.getValueByKey(ps.id);
				ar.add(ps);
			}
		}
		return ar;
	}
	public void setPanel(PanelSetting set)
	{
		try {
			int pos = getPanelSettingIndex(set.id);
			set(pos, set);
			JSONArray ar = new JSONArray();
			for(PanelSetting ps:this)
				ar.put(ps.getJSON());
			Prefs.setString(mPrefName, ar.toString());
		} catch (Throwable e) {
		}
	}
	public final PanelSetting getPanelSetting(int panelId)
	{
		for(PanelSetting ps:this)
		{
			if(ps.id==panelId)
				return ps;
		}
		return null;
	}
	public final int getPanelSettingIndex(int panelId)
	{
		int pos = 0;
		for(PanelSetting ps:this)
		{
			if(ps.id==panelId)
				return pos;
			pos++;
		}
		return -1;
	}
	public final boolean isPanelVisible(int panelId)
	{
		PanelSetting ps = getPanelSetting(panelId);
		if(ps!=null)
			return ps.visible;
		return false;
	}
	public final String getPrefName()
	{
		return mPrefName;
	}
}
