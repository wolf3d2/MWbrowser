package com.jbak.superbrowser.adapters;

import android.content.Context;

import com.jbak.superbrowser.Bookmark;
import com.jbak.superbrowser.Prefs;
import com.jbak.superbrowser.panels.PanelSettings;
import com.jbak.superbrowser.ui.PanelSetting;
/** Элемент настроек */
public class SettingsBookmark extends Bookmark {
// флаг что страница загружена для настройки
// Загружать страницу для PC
	public boolean tab_load = false;
	/** Айдишник элемента и одновременно - строка из ресурсов */
	public int id;
	/** Ключ из настроек Prefs */
	public String prefKey;
	/** Id иконки, которая показывается в правой части. 0 - не показывается */
	public int rightButtonImage=0;
	/** Настройки панели. Для элементов, которые настраивают панели */
	public PanelSettings panelSettings;
	/** Если не null - показывается чекбокс  */
	public Boolean checkBox = null;
	/** Если true и задан чекбокс - он нажимается отдельно от основного элемента */
	public boolean checkBoxAndMenuSeparate = false;
	/** Текст элемента с чекбоксом, если чекбокс включен */
	public int checkBoxTrueRes=0;
	/** Текст элемента с чекбоксом, если чекбокс выключен */
	public int checkBoxFalseRes=0;
	/** Конструктор для простых значений
	 * @param title Название настройки
	 * @param desc Описание настройки
	 */
	public SettingsBookmark(String title,String desc)
	{
		this.setTitle(title);
		this.setUrl(desc);
	}
	/** Конструктор для настройки, содержащей несколько значений 
	 * @param title Название настройки
	 * @param desc Описание настройки
	 * @param values Значения, которые может принимать настройка */
	public SettingsBookmark(Context c,String prefKey,int stringId,int descId)
	{
		this(c,prefKey,stringId,descId!=0?c.getString(descId):null);
	}
	public SettingsBookmark(Context c,String prefKey,int stringId,Boolean checkBox,int checkBoxTrueRes,int checkBoxFalseRes, boolean load_pc)
	{
		this(c,prefKey,stringId,null);
		this.checkBox = checkBox;
		this.checkBoxTrueRes = checkBoxTrueRes;
		this.checkBoxFalseRes = checkBoxFalseRes;
		this.tab_load = load_pc;
	}
	public SettingsBookmark(Context c,String prefKey,int stringId,String descText)
	{
		this.prefKey = prefKey;
		this.id = stringId;
		this.setTitle(c.getString(stringId));
		this.setUrl(descText);
	}
	public SettingsBookmark setParam(Object param)
	{
		this.param = param;
		return this;
	}
	public final SettingsBookmark setDesc(String desc)
	{
		setUrl(desc);
		return this;
	}
	public final SettingsBookmark setRightImage(int image)
	{
		rightButtonImage = image;
		return this;
	}
	public final SettingsBookmark setPref(String val)
	{
		Prefs.get().edit().putString(prefKey, val).commit();
		return this;
	}
	public final PanelSetting getPanelSetting()
	{
		if(param instanceof PanelSetting)
			return (PanelSetting)param;
		return null;
	}
	public final void setCheckBox(Boolean val,int checkTrue,int checkFalse)
	{
		checkBox = val;
		checkBoxTrueRes = checkTrue;
		checkBoxFalseRes = checkFalse;
	}
}
